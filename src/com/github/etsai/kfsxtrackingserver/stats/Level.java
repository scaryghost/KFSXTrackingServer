/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

import com.github.etsai.kfsxtrackingserver.Time;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
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
    public Time getTime() {
        return time;
    }
    public void addTime(String offset) {
        time.add(offset);
    }
    public void addTime(Time offset) {
        time.add(offset);
    }
}
