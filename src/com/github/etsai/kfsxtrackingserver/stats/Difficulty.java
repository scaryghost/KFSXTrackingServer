/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

/**
 *
 * @author etsai
 */
public class Difficulty extends Level {
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
