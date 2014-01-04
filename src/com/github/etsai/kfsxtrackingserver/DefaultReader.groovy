/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.annotations.Query
import groovy.sql.Sql
import java.sql.Connection
import java.util.Collection

/**
 * Defines functions for reading statistics from the database.  The interface allows the user to access the data 
 * without having to know the underlying database structure
 * @author etsai
 */
public class DefaultReader {
    /**
     * Order for a set of data
     * @author etsai
     */
    public enum Order {
        /** Sort in ascending order */
        ASC,
        /** Sort in descending order */
        DESC,
        /** Do not sort */
        NONE
    }
    /**
     * Base class for any tuple containing accumulated data
     * @author etsai
     */
    public class Record {
        /** Number of wins */
        public int wins;
        /** Number of losses */
        public int losses;
        /** Accumulated play time */
        public int time;
    }
    /**
     * Stores information about a difficulty setting
     * @author etsai
     */
    public class Difficulty extends Record {
        /** Name of the difficulty */
        public String name;
        /** Game length */
        public String length;
        /** Accumulated sum of waves each game ended on  */
        public int wave_sum;
    }
    /**
     * Stores information about a level
     * @author etsai
     */
    public class Level extends Record {
        /** Name of the level */
        public String name;
    }
    public class LevelDifficulty extends Record {
        /** Level name */
        public String level;
        /** Difficulty setting */
        public String difficulty;
        /** Game length */
        public String length;
        /** Accumulated sum of waves */
        public int wave_sum;
    }
    /**
     * Stores steam community information
     * @author etsai
     */
    public class SteamIDInfo {
        /** Steam community name of the steamID64 */
        public String name;
        /** Picture of the steam community profile */
        public String avatar;
    }
    /**
     * Store a players cumulative record
     * @author etsai
     */
    public class PlayerRecord extends Record {
        /** Unique steam id of the player */
        public String steamid64;
        /** Number of premature exits from a game */
        public int disconnects;
        /** Number of boss waves the player participated in */
        public int finales_played;
        /** Number of boss waves the player survived */
        public int finales_survived;
    }
    /**
     * Stores information about a player's match
     * @author etsai
     */
    public class Match {
        /** Result of the match */
        public String result;
        /** Wave reached upon disconnecting or match ending */
        public int wave;
        /** How long the player was in the match, in seconds */
        public int duration;
        /** Date and time of when the player left or match ended.  Time zone will be where the tracking server is hosted */
        public String timestamp;
        /** Difficulty setting of the match */
        public String difficulty;
        /** Game length of the match */
        public String length;
        /** Level the match took place on */
        public String level;
    }
    /**
     * Generic pair storing a statistic and its value
     * @author etsai
     */
    public class Stat {
        /** Name of the statistic */
        public String stat;
        /** Store value corresponding to the statistic */
        public int value;
    }
    /**
     * Tuple storing wave specific statistics
     * @author etsai
     */
    public class WaveStat extends Stat {
        /** Wave the statistic corresponds to */
        public int wave;
    }
    
    private final def matchHistorySql= """SELECT s.*,datetime(s.timestamp, 'localtime') as timestamp, d.name as difficulty,d.length,l.name as level FROM match_history s 
            inner join difficulty d inner join level l where s.difficulty_id=d.id and s.level_id=l.id and record_id=(select id from record r where r.steamid64=?) """
    private final def sql
    
    public DefaultReader(Connection conn) {
        this.sql= new Sql(conn)
    }
    
    private def queryDB(def query, def ps, def filteredCols) {
        def rows= []
        sql.eachRow(query, ps) {row ->
            def result= row.toRowResult()
            filteredCols.each {col ->
                result.remove(col)
            }
            rows << result
        }
        return rows
    }
    
    /**
     * Get the difficulty information for each difficulty setting.
     * @return  Collection of difficulty information
     */
    @Query(name="server_difficulties")
    public Collection<Difficulty> getDifficulties()  {
        return queryDB('select * from difficulty', [], ['id']).collect {attr ->
            println "Attr: $attr"
            new Difficulty(attr)
        }
    }
    /**
     * Get the level breakdown for the given difficulty setting
     * @param   difficulty    Difficulty name
     * @param   length      Game length
     * @return  Collection of statistics for each map played on the given difficulty setting
     */
    @Query(name="server_difficulty_data")
    public Collection<LevelDifficulty> getDifficultyData(String difficulty, String length) {
        return queryDB("""SELECT ld.*, d.name as difficulty, d.length FROM level_difficulty_join ld INNER JOIN level l ON l.id=ld.level_id 
                INNER JOIN difficulty d ON d.id=ld.difficulty_id where l.name=?""", [level], ['difficulty_id', 'level_id']).collect {attr ->
            attr.level= level
            new LevelDifficulty(attr)
        }
    }
    /**
     * Get the totals for each played level, across all difficulty settings
     * @return  Collection of totals for all levels
     */
    @Query(name="server_levels")
    public Collection<Level> getLevels() {
        return queryDB('select * from level', [], ['id']).collect {attr ->
            // Need to be explicit to avoid confusion with logging level
            new Level(attr)
        }
    }
    /**
     * Get the difficulty breakdown for specific level
     * @param   level      Name of the level to lookup
     * @return  Collection of difficulty breakdowns for all levels played
     */
    @Query(name="server_level_data")
    public Collection<LevelDifficulty> getLevelData(String level) {
        return queryDB("""SELECT ld.*, l.name as level FROM level_difficulty_join ld INNER JOIN difficulty d on d.id=ld.difficulty_id  
                INNER JOIN level l ON l.id=ld.level_id where d.name=? and d.length=?""", [difficulty, length], ['difficulty_id', 'level_id']).collect {attr ->
            attr.difficulty= difficulty
            attr.length= length
            new LevelDifficulty(attr)
        }
    }
    /**
     * Get the number of player records in the database
     * @return  Number of players in the database
     */
    @Query(name="server_num_record")
    public Integer getNumRecords() {
        return sql.firstRow('SELECT count(*) FROM record')[0]
    }
    /**
     * Get the record for the given player.  If the steamID64 is invalid, null is returned.
     * @param   steamID64   SteamID64 of the desired player.
     * @return  Record for the given player, null if invalid steamID64 is given or id not in db
     * @see DataReader#getRecords()
     */
    @Query(name="server_record")
    public PlayerRecord getRecord(String steamID64) {
        def row= sql.firstRow('SElECT * FROM record WHERE steamid64=?', [steamID64])

        if (row != null) {
            ['id', 'record_id'].each {column ->
                row.remove(column)
            }
            return new PlayerRecord(row)
        }
        return null
    }
    /**
     * Get a subset of all the player records, sorted by a specific category in a given order.
     * @param   group   The group to sort on.  If order is NONE, this parameter is ignored
     * @param   order   Order to sort the group if desired
     * @param   start   The first row to return in the given ordering
     * @param   end     The last row to return in the given ordering
     * @return  Ordered list of player records, limited by a start and end.
     * @see DataReader#getRecords()
     */
    @Query(name="server_records")
    public Collection<PlayerRecord> getRecords(String group, Order order, int start, int end) {
        def query= "SELECT r.*,name FROM record r inner join steam_info s on r.id=s.record_id "
        
        if (group != null && order != Order.NONE) {
            query+= "ORDER BY $group $order "
        }
        query+= "LIMIT ?, ?"
        return queryDB(query, [start, end - start], ['id', 'record_id', 'name']).collect {attr ->
            new PlayerRecord(attr)
        }
    }
    /**
     * Get all stored player records
     * @return  All player records
     */
    @Query(name="server_all_records")
    public Collection<PlayerRecord> getRecords() {
        return queryDB("SELECT * FROM record ", [], ['id', 'record_id']).collect {attr ->
            new PlayerRecord(attr)
        }
    }
    /**
     * Get a subset of the match history for the specific player.  The list can be ordered based on a grouping.  
     * @param   steamID64   SteamID64 of the player to lookup
     * @param   group   The group to sort on.  If order is NONE, this parameter is ignored
     * @param   order   Order to sort the group if desired
     * @param   start   The first row to return in the given ordering
     * @param   end     The last row to return in the given ordering
     * @return  Ordered list of the match history for a player.
     * @see DataReader#getMatchHistory(String)
     */
    @Query(name="player_history")
    public Collection<Match> getMatchHistory(String steamID64, String group, Order order, int start, int end) {
        def query= matchHistorySql
        
        if (group != null && order != Order.NONE) {
            query+= "ORDER BY $group $order "
        }
        query+= "LIMIT ?, ?"
        return queryDB(query, [steamID64, start, end - start], ['record_id', 'level_id', 'difficulty_id']).collect {attr ->
            new Match(attr)
        }
    }
    /**
     * Get all games in a player's match history
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  All games played by the player
     */
    @Query(name="player_all_histories")
    public Collection<Match> getMatchHistory(String steamID64) {
        return queryDB(matchHistorySql, [steamID64], ['record_id', 'level_id', 'difficulty_id']).collect {attr ->
            new Match(attr)
        }
    }
    /**
     * Get the number of matches for a player
     * @param   steamID64   SteamID4 of the player to lookup
     * @return  Number of matches the player has participated in
     */
    @Query(name="player_num_matches")
    public Integer getNumMatches(String steamID64) {
        return sql.firstRow('SELECT count(*) from match_history h where h.record_id=(select id from record r where r.steamid64=?)', [steamID64])[0]
    }
    /**
     * Get all stat categories that have an aggregate sum over all players
     * @return  Stat categories for aggregate stats
     */
    @Query(name="server_categories")
    public Collection<String> getStatCategories() {
        return queryDB('SELECT category FROM aggregate GROUP BY category', [], []).collect { it.category }
    }
    /**
     * Get the aggregate statistics for a specific category.  Map keys are: 
     * @param   category    Aggregate category to lookup
     * @return  Collection of all statistics for a category
     */
    @Query(name="server_aggregate_data")
    public Collection<Stat> getAggregateData(String category) {
        return queryDB("SELECT * from aggregate where category=? ORDER BY stat ASC", [category], ['category']).collect {attr ->
            new Stat(attr)
        }
    } 
    /**
     * Get the aggregate statistics for a specific category and player
     * @param   category    Aggregate category to lookup
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  Collection of all statistics for a category and player
     */
    @Query(name="player_aggregate_data")
    public Collection<Stat> getAggregateData(String category, String steamID64) {
        return queryDB("SELECT * from player where record_id=(select id from record where steamid64=?) and category=? ORDER BY stat ASC", 
                [steamID64, category],  ['steamid64', 'category', 'record_id']).collect {attr ->
            new Stat(attr)
        }
    }
    /**
     * Get the saved steam community info for a player from the database.  Null will be returned if the steamID64 
     * is not present in the db
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  Steam community info for a player, or null if id not found in db
     */
    @Query(name="player_info")
    public SteamIDInfo getSteamIDInfo(String steamID64) {
        def row= sql.firstRow("select * from steam_info where record_id=(select id from record where steamid64=?)", [steamID64])

        if (row != null) {
            row.remove("record_id")        
            return new SteamIDInfo(row)
        }
        return null
    }
    /**
     * Get categories for statistics that support wave by wave analytics
     * @return  Collection of support statistics that have wave by wave numbers
     */
    @Query(name="server_wave_categories")
    public Collection<String> getWaveDataCategories() {
        return queryDB('SELECT category FROM wave_data GROUP BY category', [], []).collect { it.category }
    }
    /**
     * Get detailed wave by wave numbers for a given difficulty setting
     * @param   difficulty      Difficulty name
     * @param   length          Game length
     * @param   category        Category of statistics to retrieve
     * @return  Collection of wave by wave statistics
     */
    @Query(name="server_wave_data")
    public Collection<WaveStat> getWaveData(String difficulty, String length, String category) {
        return queryDB("select wave, stat, sum(value) as value from wave_data where difficulty_id=(select id from difficulty where name=? and length=?) and category=? group by wave, stat", 
                [difficulty, length, category], []).collect {attr ->
            new WaveStat(attr)
        }
    }
    /**
     * Get detailed wave by wave numbers for a given difficulty setting and map.  See getWaveData(String, String, String) for map keys.
     * @param   level           Name of the level
     * @param   difficulty      Difficulty name
     * @param   length          Game length
     * @param   category        Category of statistics to retrieve
     * @return  Collection of wave by wave statistics
     * @see DataReader#getWaveData(String, String, String)
     */
    @Query(name="server_level_wave_data")
    public Collection<WaveStat> getWaveData(String level, String difficulty, String length, String category) {
        return queryDB("""SELECT wave, stat, value FROM wave_data WHERE difficulty_id=(select id from difficulty where name=? and length=?) 
                and level_id=(select id from level where name=?) and category=?""", [difficulty, length, level, category], []).collect {attr ->
            new WaveStat(attr)
        }
    }
}
