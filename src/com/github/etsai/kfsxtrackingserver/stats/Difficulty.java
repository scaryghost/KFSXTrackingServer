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
public class Difficulty extends Level {
    public static Difficulty build(ResultSet rs) throws SQLException {
        Difficulty difficulty= new Difficulty(rs.getInt("id"), rs.getString("name"),
            rs.getString("length"));
        
        difficulty.wave= rs.getInt("wave");
        difficulty.addWins(rs.getInt("wins"));
        difficulty.addLosses(rs.getInt("losses"));
        difficulty.addTime(rs.getString("time"));
        return difficulty;
    }
    private final String length;
    private int wave= 0;
    
    public Difficulty(int id, String name, String length) {
        super(id, name);
        this.length= length;
    }
    
    public String getLength() {
        return length;
    }
    public int getWave() {
        return wave;
    }
    public void addWave(int offset) {
        wave+= offset;
    }
}
