/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import static com.github.etsai.kfsxtrackingserver.DataReader.Order
import com.github.etsai.kfsxtrackingserver.Accumulator
import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.SteamPoller
import groovy.sql.Sql
import java.sql.Connection

/**
 *
 * @author eric
 */
public class DataReaderImpl implements DataReader {
    private final def sql
    
    public def DataReaderImpl(def conn) {
        this.sql= new Sql(conn)
    }
    private def queryDB(def query, def ps) {
        def rows= []
        sql.eachRow(query, ps) {row ->
            rows << row.toRowResult()
        }
        return rows
    }
    public List<Map<Object, Object>> getDifficulties() {
        return queryDB('select * from difficulty', [])
    }
    public List<Map<Object, Object>> getLevels() {
        return queryDB('select * from level', [])
    }
    public Map<Object, Object> getRecord(String steamID64) {
        return sql.firstRow('SElECT * FROM record r INNER JOIN steaminfo s on r.id=s.record_id WHERE steamid64=?', [steamID64])
    }
    public List<Map<Object, Object>> getRecords(String group, Order order, int start, int end) {
        def query= "SELECT * FROM record r INNER JOIN steaminfo s ON r.id=s.record_id "
        
        if (group != null && order != Order.NONE) {
            query+= "ORDER BY $group $order "
        }
        query+= "LIMIT ?, ?"
        return queryDB(query, [start, end - start])
    }
    public Integer getNumRecords() {
        return sql.firstRow('SELECT count(*) FROM record')[0]
    }
    public List<Map<Object, Object>> getRecords() {
        return queryDB("SELECT * FROM record r INNER JOIN steaminfo s ON r.id=s.record_id", [])
    }
    public List<Map<Object, Object>> getSessions(String steamID64, String group, Order order, int start, int end) {
        def query= "SELECT s.*,d.name,d.length FROM session s inner join difficulty d where s.difficulty_id=d.id and record_id=(select id from record r where r.steamid64=?) "
        
        if (group != null && order != Order.NONE) {
            query+= "ORDER BY $group $order "
        }
        query+= "LIMIT ?, ?"
        return queryDB(query, [steamID64, start, end - start])
    }
    public List<Map<Object, Object>> getSessions(String steamID64) {
        return queryDB("SELECT s.*,d.name,d.length FROM session s inner join difficulty d where s.difficulty_id=d.id and record_id=(select id from record r where r.steamid64=?)", [steamID64])
    }
    public List<String> getAggregateCategories() {
        return queryDB('SELECT category FROM aggregate GROUP BY category', []).collect { it.category }
    }
    public List<Map<Object, Object>> getAggregateData(String category) {
        return queryDB("SELECT * from aggregate where category=? ORDER BY stat ASC", [category])
    }
    public List<Map<Object, Object>> getAggregateData(String category, String steamID64) {
        return queryDB("SELECT * from player where record_id=(select id from record where steamid64=?) and category=? ORDER BY stat ASC", [steamID64, category])
    }
    public Map<Object, Object> getSteamIDInfo(String steamID64) {
        def row= sql.firstRow("select * from steaminfo where record_id=(select id from record where steamid64=?)", [steamID64])
        
        if (row == null) {
            try {
                def info= SteamPoller.poll(id)
                row= [:]
                Accumulator.writer.writeSteamInfo(steamID64, info[0], info[1])
            } catch (IOException ex) {
                row= [:]
                row["steamid64"]= steamID64
                row["name"]= "----Unavailable----"
            } catch (Exception ex) {
                Common.logger.log(Level.SEVERE, "Invalid steamID64: $steamID64", ex)
            } 
        }
        return row
    }
    public List<Map<Object, Object>> getWaveData(String diffName, String diffLength, String category) {
        return queryDB("select wave, stat, sum(value) as value from wavedata where difficulty_id=(select id from difficulty where name=? and length=?) and category=? group by wave, stat", 
                [diffName, diffLength, category])
    }
    public List<Map<Object, Object>> getWaveData(String levelName, String diffName, String diffLength, String category) {
        return queryDB("""SELECT wave, stat, value FROM wavedata WHERE difficulty_id=(select id from difficulty where name=? and length=?) 
                and level_id=(select id from level where name=?) and category=?""", 
                [diffName, diffLength, levelName, category])
    }

    public List<String> getWaveDataCategories() {
        return queryDB('SELECT category FROM wavedata GROUP BY category', []).collect { it.category }
    }
    public List<Map<Object, Object>> getLevelData(String levelName) {
        return queryDB("""SELECT ld.*,d.name,d.length FROM level_difficulty_join ld INNER JOIN level l ON l.id=ld.level_id 
                INNER JOIN difficulty d ON d.id=ld.difficulty_id where l.name=?""", [levelName])
    }
    public List<Map<Object, Object>> getDifficultyData(String diffName, String length) {
        return queryDB("""SELECT ld FROM level_difficulty_join ld INNER JOIN difficulty d on d.id=ld.difficulty_id  
                INNER JOIN level l ON l.id=ld.level_id where d.name=? and d.length""", [diffname, length])
    }
}

