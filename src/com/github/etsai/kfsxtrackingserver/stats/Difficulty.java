/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks the wins, losses, waves, and playtime of each difficulty
 * @author etsai
 */
public class Difficulty extends Level {
    public static Difficulty build(ResultSet rs) throws SQLException {
        Difficulty difficulty= new Difficulty(rs.getInt("id"), rs.getString("name"),
            rs.getString("length"));
        
        difficulty.wave.getAndSet(rs.getInt("wave"));
        difficulty.addWins(rs.getInt("wins"));
        difficulty.addLosses(rs.getInt("losses"));
        difficulty.addTime(rs.getString("time"));
        return difficulty;
    }
    private final String length;
    private AtomicInteger wave;
    
    public Difficulty(int id, String name, String length) {
        super(id, name);
        this.length= length;
        wave= new AtomicInteger();
    }
    
    public String getLength() {
        return length;
    }
    public int getWave() {
        return wave.get();
    }
    public void addWave(int offset) {
        wave.getAndAdd(offset);
    }
}
