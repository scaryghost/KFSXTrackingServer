/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import java.util.Collection;

/**
 * Defines functions for reading statistics from the database.  The interface allows the user to access the data 
 * without having to know the underlying database structure
 * @author etsai
 */
public interface DataReader {
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
     * Stores information about a difficulty setting.  The data can be an aggregate amount 
     * or values for a specific level
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
     * Stores information about a level.  The data can be an aggregate amount or 
     * values for a specific difficulty setting
     * @author etsai
     */
    public class Level extends Record {
        /** Name of the level */
        public String name;
        /**
         * Accumulate sum of waves each game ended on.  This value will be null if 
         * the data is an aggregate amount 
         */
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
        public int disonnects;
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
    
    /**
     * Get the difficulty information for each difficulty setting.
     * @return  Collection of difficulty information
     */
    public Collection<Difficulty> getDifficulties();
    /**
     * Get the level breakdown for the given difficulty setting
     * @param   difficulty    Difficulty name
     * @param   length      Game length
     * @return  Collection of statistics for each map played on the given difficulty setting
     */
    public Collection<Level> getDifficultyData(String difficulty, String length);
    /**
     * Get the totals for each played level, across all difficulty settings
     * @return  Collection of totals for all levels
     */
    public Collection<Level> getLevels();
    /**
     * Get the difficulty breakdown for specific level
     * @param   level      Name of the level to lookup
     * @return  Collection of difficulty breakdowns for all levels played
     */
    public Collection<Difficulty> getLevelData(String level);
    /**
     * Get the number of player records in the database
     * @return  Number of players in the database
     */
    public Integer getNumRecords();
    /**
     * Get the record for the given player.  If the steamID64 is invalid, null is returned.
     * @param   steamID64   SteamID64 of the desired player.
     * @return  Record for the given player, null if invalid steamID64 is given
     * @see DataReader#getRecords()
     */
    public PlayerRecord getRecord(String steamID64);
    /**
     * Get a subset of all the player records, sorted by a specific category in a given order.
     * @param   group   The group to sort on.  If order is NONE, this parameter is ignored
     * @param   order   Order to sort the group if desired
     * @param   start   The first row to return in the given ordering
     * @param   end     The last row to return in the given ordering
     * @return  Ordered list of player records, limited by a start and end.
     * @see DataReader#getRecords()
     */
    public Collection<PlayerRecord> getRecords(String group, Order order, int start, int end);
    /**
     * Get all stored player records
     * @return  All player records
     */
    public Collection<PlayerRecord> getRecords();
    /**
     * Get a subset of the match history for the specific player.  The list can be ordered based on a grouping.  See getMatchHistory(String) for the map keys.  
     * @param   steamID64   SteamID64 of the player to lookup
     * @param   group   The group to sort on.  If order is NONE, this parameter is ignored
     * @param   order   Order to sort the group if desired
     * @param   start   The first row to return in the given ordering
     * @param   end     The last row to return in the given ordering
     * @return  Ordered list of the match history for a player.
     * @see DataReader#getMatchHistory(String)
     */
    public Collection<Match> getMatchHistory(String steamID64, String group, Order order, int start, int end);
    /**
     * Get all games in a player's match history
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  All games played by the player
     */
    public Collection<Match> getMatchHistory(String steamID64);
    /**
     * Get all stat categories that have an aggregate sum over all players
     * @return  Stat categories for aggregate stats
     */
    public Collection<String> getStatCategories();
    /**
     * Get the aggregate statistics for a specific category.  Map keys are: 
     * @param   category    Aggregate category to lookup
     * @return  Collection of all statistics for a category
     */
    public Collection<Stat> getAggregateData(String category);
    /**
     * Get the aggregate statistics for a specific category and player
     * @param   category    Aggregate category to lookup
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  Collection of all statistics for a category and player
     */
    public Collection<Stat> getAggregateData(String category, String steamID64);
    /**
     * Get the steam community info for a player
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  Steam community info for a player
     */
    public SteamIDInfo getSteamIDInfo(String steamID64);
    /**
     * Get categories for statistics that support wave by wave analytics
     * @return  Collection of support statistics that have wave by wave numbers
     */
    public Collection<String> getWaveDataCategories();
    /**
     * Get detailed wave by wave numbers for a given difficulty setting
     * @param   difficulty      Difficulty name
     * @param   length          Game length
     * @param   category        Category of statistics to retrieve
     * @return  Collection of wave by wave statistics
     */
    public Collection<WaveStat> getWaveData(String difficulty, String length, String category);
    /**
     * Get detailed wave by wave numbers for a given difficulty setting and map.  See getWaveData(String, String, String) for map keys.
     * @param   level           Name of the level
     * @param   difficulty      Difficulty name
     * @param   length          Game length
     * @param   category        Category of statistics to retrieve
     * @return  Collection of wave by wave statistics
     * @see DataReader#getWaveData(String, String, String)
     */
    public Collection<WaveStat> getWaveData(String level, String difficulty, String length, String category);
}
