/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author etsai
 */
public class Record extends TableCommon {
    public static Record build(ResultSet rs) throws SQLException {
        Record record= new Record(rs.getString("steamid"));
        
        record.disconnects= rs.getInt("disconnects");
        record.addLosses(rs.getInt("losses"));
        record.addWins(rs.getInt("wins"));
        return record;
    }
    
    private final String steamid;
    private int disconnects;
    
    public Record(String steamid) {
        super(steamid.hashCode());
        this.steamid= steamid;
        disconnects= 0;
    }
    public String getSteamId() {
        return steamid;
    }
    public int getDisconnects() {
        return disconnects;
    }
    public void addDisconnects(int offset) {
        disconnects+= offset;
    }
}
