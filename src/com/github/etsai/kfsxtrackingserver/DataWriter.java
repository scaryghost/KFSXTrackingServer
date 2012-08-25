/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.stats.*;
import java.util.TimerTask;

/**
 *
 * @author etsai
 */
public abstract class DataWriter extends TimerTask {
    public abstract void addDiff(Difficulty diff);
    public abstract void addLevel(Level level);
    public abstract void addRecord(Record record);
    public abstract void addDeath(Death death);
    public abstract void addAggregate(Aggregate aggr);
    public abstract void addPlayer(Player player);
}
