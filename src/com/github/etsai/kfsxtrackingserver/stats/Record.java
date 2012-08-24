/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stores the wins, losses, and disconnects for each player
 * @author etsai
 */
public class Record extends TableCommon {
    public static Record build(ResultSet rs) throws SQLException {
        Record record= new Record(rs.getString("steamid"));
        
        record.disconnects.getAndSet(rs.getInt("disconnects"));
        record.addLosses(rs.getInt("losses"));
        record.addWins(rs.getInt("wins"));
        return record;
    }
    
    private final String steamid;
    private AtomicInteger disconnects;
    
    public Record(String steamid) {
        super(steamid.hashCode());
        this.steamid= steamid;
        disconnects= new AtomicInteger();
    }
    public String getSteamId() {
        return steamid;
    }
    public synchronized int getDisconnects() {
        return disconnects.get();
    }
    public synchronized void addDisconnects(int offset) {
        disconnects.getAndAdd(offset);
    }
}
