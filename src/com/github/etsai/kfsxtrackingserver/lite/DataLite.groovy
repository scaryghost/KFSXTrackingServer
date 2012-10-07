/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.lite

import com.github.etsai.kfsxtrackingserver.Data
import com.github.etsai.kfsxtrackingserver.stats.Difficulty
import com.github.etsai.kfsxtrackingserver.Time
import java.util.logging.Level
import com.github.etsai.kfsxtrackingserver.stats.Record
import com.github.etsai.kfsxtrackingserver.stats.Aggregate
import com.github.etsai.kfsxtrackingserver.stats.Death
import com.github.etsai.kfsxtrackingserver.stats.Player

/**
 * Implementation of the Data interface, used for the lite server
 * @author etsai
 */
public class DataLite extends Data {
    public Difficulty getDifficulty(String name, String length) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public Collection<Difficulty> getDifficulties() {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public void accumulateDifficulty(String name, String length, int result, int wave, Time timeLength) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    
    public Level getLevel(String name) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public Collection<Level> getLevels() {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public void accumulateLevel(String name, int result, Time timeLength) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    
    public Record getRecord(String steamID64) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public Collection<Record> getRecords() {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public void accumulateRecord(String steamID64, int result) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    
    public Collection<Aggregate> getAggregateStats() {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public void accumulateAggregateStat(String stat, int offset, String category) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    
    public Collection<Death> getDeaths() {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public void accumulateDeath(String stat, int offset) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    
    public Map<String, Player> getPlayerStats(String steamID64) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public void accumulatePlayerStat(String steamID64, String stat, int offset, String category) {
        throw new UnsupportedOperationException("Not yet implemented")
    }
}

