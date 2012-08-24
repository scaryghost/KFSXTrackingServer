/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common functions used by the other classes
 * @author etsai
 */
public abstract class TableCommon {
    private final int id;
    private AtomicInteger wins;
    private AtomicInteger losses;
    
    protected TableCommon(int id) {
        this.id= id;
        wins= new AtomicInteger();
        losses= new AtomicInteger();
    }
    public int getId() {
        return id;
    }
    public int getWins() {
        return wins.get();
    }
    public int getLosses() {
        return losses.get();
    }
    public void addWins(int offset) {
        wins.getAndAdd(offset);
    }
    public void addLosses(int offset) {
        losses.getAndAdd(offset);
    }
}
