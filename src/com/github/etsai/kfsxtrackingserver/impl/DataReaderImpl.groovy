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
        return queryDB('select * from difficulties', [])
    }
    public List<Map<Object, Object>> getLevels() {
        return queryDB('select * from levels', [])
    }
    public List<Map<Object, Object>> getDeaths() {
        return queryDB('select * from deaths ORDER BY name ASC', [])
    }
    public Map<Object, Object> getRecord(String steamID64) {
        return sql.firstRow('SElECT * FROM records WHERE steamid64=?', [steamID64])
    }
    public List<Map<Object, Object>> getRecords(String group, Order order, int start, int end) {
        def query= "SELECT s.name,r.wins,r.losses,r.disconnects,r.steamid64 FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64 "
        
        if (group != null && order != Order.NONE) {
            query+= "ORDER BY $group $order "
        }
        query+= "LIMIT ?, ?"
        return queryDB(query, [start, end - start])
    }
    public Integer getNumRecords() {
        return sql.firstRow('SELECT count(*) FROM records')[0]
    }
    public List<Map<Object, Object>> getRecords() {
        return queryDB("SELECT s.name,r.wins,r.losses,r.disconnects,r.steamid64 FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64", [])
    }
    public List<Map<Object, Object>> getSessions(String steamID64, String group, Order order, int start, int end) {
        def query= "SELECT * FROM sessions WHERE steamid64=?"
        
        if (group != null && order != Order.NONE) {
            query+= "ORDER BY $group $order "
        }
        query+= "LIMIT ?, ?"
        return queryDB(query, [steamID64, start, end - start])
    }
    public List<Map<Object, Object>> getSessions(String steamID64) {
        return queryDB("SELECT * FROM sessions WHERE steamid64=?", [start, end - start])
    }
    public List<String> getAggregateCategories() {
        return queryDB('SELECT category FROM aggregate GROUP BY category', []).collect { it.category }
    }
    public List<Map<Object, Object>> getAggregateData(String category) {
        return queryDB("SELECT * from aggregate where category=? ORDER BY stat ASC", [category])
    }
    public List<Map<Object, Object>> getAggregateData(String category, String steamID64) {
        return queryDB("SELECT * from player where steamid64=? and category=? ORDER BY stat ASC", [steamID64, category])
    }
    public Map<Object, Object> getSteamIDInfo(String steamID64) {
        def row= sql.firstRow("select * from steaminfo where steamid64=?", [steamID64])
        
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
}

