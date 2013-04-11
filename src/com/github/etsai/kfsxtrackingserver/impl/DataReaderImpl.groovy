/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import static com.github.etsai.kfsxtrackingserver.DataReader.Order
import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.SteamIDInfo
import groovy.sql.Sql

/**
 *
 * @author eric
 */
public class DataReaderImpl implements DataReader {
    public def sql
    
    private def queryDB(def query, def ps) {
        def rows= []
        sql.eachRow(query, ps) {row ->
            rows << row.toResultSet()
        }
        return rows
    }
    public Iterable<Map<Object, Object>> getDifficulties() {
        return queryDB('select * from difficulties', [])
    }
    public Iterable<Map<Object, Object>> getLevels() {
        return queryDB('select * from levels', [])
    }
    public Iterable<Map<Object, Object>> getDeaths() {
        return queryDB('select * from deaths ORDER BY name ASC', [])
    }
    public Iterable<Map<Object, Object>> getRecords(String group, Order order, int start, int end) {
        def query= "SELECT s.name,r.wins,r.losses,r.disconnects,r.steamid64 FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64 "
        
        if (group != null && order != Order.NONE) {
            query+= "ORDER BY $group $order "
        }
        query+= "LIMIT ?, ?"
        return queryDB(query, [start, end - start])
    }
    public Iterable<Map<Object, Object>> getRecords() {
        return queryDB("SELECT s.name,r.wins,r.losses,r.disconnects,r.steamid64 FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64")
    }
    public Iterable<Map<Object, Object>> getSessions(String steamID64, String group, Order order, int start, int end) {
        def query= "SELECT * FROM sessions WHERE steamid64=?"
        
        if (group != null && order != Order.NONE) {
            query+= "ORDER BY $group $order "
        }
        query+= "LIMIT ?, ?"
        return queryDB(query, [steamID64, start, end - start])
    }
    public Iterable<Map<Object, Object>> getSessions(String steamID64) {
        return queryDB("SELECT * FROM sessions WHERE steamid64=?", [start, end - start])
    }
    public Iterable<String> getAggregateCategories() {
        return queryDB('SELECT category FROM aggregate GROUP BY category')
    }
    public Iterable<Map<Object, Object>> getAggregateData(String category) {
        return queryDB("SELECT * from aggregate where category=? ORDER BY stat ASC", [category])
    }
    public Iterable<Map<Object, Object>> getAggregateData(String category, String steamID64) {
        return queryDB("SELECT * from player where steamid64=? and category=? ORDER BY stat ASC", [steamID64, category])
    }
    public Map<Object, Object> getSteamIDInfo(String steamID64) {
        return sql.firstRow("SELECT * FROM records where steamid64=?", [steamID64])
    }
}

