/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

import com.github.etsai.kfsxtrackingserver.Time;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Tracks wins, losses, and play time of each level
 * @author etsai
 */
public class Level extends TableCommon {
    public static Level build(ResultSet rs) throws SQLException {
        Level level= new Level(rs.getInt("id"), rs.getString("name"));
        
        level.time= new Time(rs.getString("time"));
        level.addLosses(rs.getInt("losses"));
        level.addWins(rs.getInt("wins"));
        return level;
    }
    
    private Time time;
    private final String name;
    
    public Level(int id, String name) {
        super(id);
        this.name= name;
        time= new Time(0);
    }
    public String getName() {
        return name;
    }
    public synchronized Time getTime() {
        return time;
    }
    public synchronized void addTime(String offset) {
        time.add(offset);
    }
    public synchronized void addTime(Time offset) {
        time.add(offset);
    }
}
