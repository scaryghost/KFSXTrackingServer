/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import java.util.TimerTask;

/**
 *
 * @author etsai
 */
public abstract class DataWriter extends TimerTask {
    public abstract void addDiffId(String name, String length);
    public abstract void addLevelId(String name);
    public abstract void addRecordId(String steamid);
    public abstract void addDeath(String death);
    public abstract void addAggregate(String stat, String category);
}
