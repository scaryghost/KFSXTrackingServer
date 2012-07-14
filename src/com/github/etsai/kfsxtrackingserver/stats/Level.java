/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

import com.github.etsai.kfsxtrackingserver.Time;

/**
 *
 * @author etsai
 */
public class Level extends TableCommon {
    private Time time;
    private final String name;
    
    public Level(int id, String name) {
        super(id);
        this.name= name;
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
