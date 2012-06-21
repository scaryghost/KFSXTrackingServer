/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Time

/**
 *
 * @author eric
 */
public class Level {
    public static final def keyName= "name"
    public static final def keyWins= "wins"
    public static final def keyLosses= "losses"
    public static final def keyTime= "time"
        
    protected def data
        
    public Level(def name) {
        data= [:]
        data[keyName]= name
        data[keyWins]= 0
        data[keyLosses]= 0
        data[keyTime]= new Time(0)
    }
        
    public void addWins() {
        data[keyWins]++
    }
    public void addWins(def offset) {
        data[keyWins]+= offset
    }
        
    public void addLosses() {
        data[keyLosses]++
    }
    public void addLosses(def offset) {
        data[keyLosses]+= offset
    }   
    public void addTime(def time) {
        data[keyTime].add(time)
    }
    public Iterable<String> getKeys() {
        return data.keySet()
    }
    public def getData(String key) {
        return data[key]
    }
}

