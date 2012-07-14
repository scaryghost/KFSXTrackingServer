/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

/**
 *
 * @author etsai
 */
public abstract class TableCommon {
    private final int id;
    private int wins= 0;
    private int losses= 0;
    
    protected TableCommon(int id) {
        this.id= id;
    }
    public int getId() {
        return id;
    }
    public int getWins() {
        return wins;
    }
    public int getLosses() {
        return losses;
    }
    public void addWins(int offset) {
        wins+= offset;
    }
    public void addLosses(int offset) {
        losses+= offset;
    }
}
