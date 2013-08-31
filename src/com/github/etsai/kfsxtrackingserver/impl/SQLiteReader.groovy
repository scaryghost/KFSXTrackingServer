/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Accumulator
import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.DataReader.Order
import com.github.etsai.kfsxtrackingserver.DataReader.Difficulty
import com.github.etsai.kfsxtrackingserver.DataReader.Level
import com.github.etsai.kfsxtrackingserver.DataReader.LevelDifficulty
import com.github.etsai.kfsxtrackingserver.DataReader.SteamIDInfo
import com.github.etsai.kfsxtrackingserver.DataReader.Stat
import com.github.etsai.kfsxtrackingserver.DataReader.WaveStat
import com.github.etsai.kfsxtrackingserver.DataReader.PlayerRecord
import com.github.etsai.kfsxtrackingserver.DataReader.Match
import com.github.etsai.kfsxtrackingserver.SteamPoller
import com.github.etsai.kfsxtrackingserver.SteamPoller.InvalidSteamIDException
import groovy.sql.Sql
import java.util.logging.Level
import java.sql.Connection

/**
 *
 * @author etsai
 */
public class SQLiteReader implements DataReader {
    private final def matchHistorySql= """SELECT s.*,datetime(s.timestamp, 'localtime') as timestamp, d.name as difficulty,d.length,l.name as level FROM match_history s 
            inner join difficulty d inner join level l where s.difficulty_id=d.id and s.level_id=l.id and record_id=(select id from record r where r.steamid64=?) """
    private final def sql
    
    public SQLiteReader(Connection conn) {
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
    public Collection<Difficulty> getDifficulties() {
        return queryDB('select * from difficulty', [], ['id']).collect {attr ->
            new Difficulty(attr)
        }
    }
    public Collection<Level> getLevels() {
        return queryDB('select * from level', [], ['id']).collect {attr ->
            // Need to be explicit to avoid confusion with logging level
            new com.github.etsai.kfsxtrackingserver.DataReader.Level(attr)
        }
    }
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
    public Integer getNumRecords() {
        return sql.firstRow('SELECT count(*) FROM record')[0]
    }
    public Collection<PlayerRecord> getRecords() {
        return queryDB("SELECT * FROM record ", [], ['id', 'record_id']).collect {attr ->
            new PlayerRecord(attr)
        }
    }
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
    public Collection<Match> getMatchHistory(String steamID64) {
        return queryDB(matchHistorySql, [steamID64], ['record_id', 'level_id', 'difficulty_id']).collect {attr ->
            new Match(attr)
        }
    }
    public Collection<String> getStatCategories() {
        return queryDB('SELECT category FROM aggregate GROUP BY category', [], []).collect { it.category }
    }
    public Collection<Stat> getAggregateData(String category) {
        return queryDB("SELECT * from aggregate where category=? ORDER BY stat ASC", [category], ['category']).collect {attr ->
            new Stat(attr)
        }
    }
    public Collection<Stat> getAggregateData(String category, String steamID64) {
        return queryDB("SELECT * from player where record_id=(select id from record where steamid64=?) and category=? ORDER BY stat ASC", 
                [steamID64, category],  ['steamid64', 'category', 'record_id']).collect {attr ->
            new Stat(attr)
        }
    }
    public SteamIDInfo getSteamIDInfo(String steamID64) {
        def row= sql.firstRow("select * from steam_info where record_id=(select id from record where steamid64=?)", [steamID64])

        if (row != null) {
            row.remove("record_id")        
            return new SteamIDInfo(row)
        }
        return null
    }
    public Collection<WaveStat> getWaveData(String difficulty, String length, String category) {
        return queryDB("select wave, stat, sum(value) as value from wave_data where difficulty_id=(select id from difficulty where name=? and length=?) and category=? group by wave, stat", 
                [difficulty, length, category], []).collect {attr ->
            new WaveStat(attr)
        }
    }
    public Collection<WaveStat> getWaveData(String level, String difficulty, String length, String category) {
        return queryDB("""SELECT wave, stat, value FROM wave_data WHERE difficulty_id=(select id from difficulty where name=? and length=?) 
                and level_id=(select id from level where name=?) and category=?""", [difficulty, length, level, category], []).collect {attr ->
            new WaveStat(attr)
        }
    }

    public Collection<String> getWaveDataCategories() {
        return queryDB('SELECT category FROM wave_data GROUP BY category', [], []).collect { it.category }
    }
    public Collection<LevelDifficulty> getLevelData(String level) {
        return queryDB("""SELECT ld.*, d.name as difficulty, d.length FROM level_difficulty_join ld INNER JOIN level l ON l.id=ld.level_id 
                INNER JOIN difficulty d ON d.id=ld.difficulty_id where l.name=?""", [level], ['difficulty_id', 'level_id']).collect {attr ->
            attr.level= level
            new LevelDifficulty(attr)
        }
    }
    public Collection<LevelDifficulty> getDifficultyData(String difficulty, String length) {
        return queryDB("""SELECT ld.*, l.name as level FROM level_difficulty_join ld INNER JOIN difficulty d on d.id=ld.difficulty_id  
                INNER JOIN level l ON l.id=ld.level_id where d.name=? and d.length=?""", [difficulty, length], ['difficulty_id', 'level_id']).collect {attr ->
            attr.difficulty= difficulty
            attr.length= length
            new LevelDifficulty(attr)
        }
    }
}

