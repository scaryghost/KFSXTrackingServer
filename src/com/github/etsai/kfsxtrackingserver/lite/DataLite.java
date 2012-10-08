/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.lite;

import com.github.etsai.kfsxtrackingserver.Data;
import com.github.etsai.kfsxtrackingserver.Time;
import com.github.etsai.kfsxtrackingserver.stats.Aggregate;
import com.github.etsai.kfsxtrackingserver.stats.Death;
import com.github.etsai.kfsxtrackingserver.stats.Difficulty;
import com.github.etsai.kfsxtrackingserver.stats.Level;
import com.github.etsai.kfsxtrackingserver.stats.Player;
import com.github.etsai.kfsxtrackingserver.stats.Record;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of the Data class, for the lite server
 * @author etsai
 */
public class DataLite extends Data {
    @Override
    public Difficulty getDifficulty(String name, String length) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public Collection<Difficulty> getDifficulties() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void accumulateDifficulty(String name, String length, int result, int wave, Time timeLength) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public Level getLevel(String name) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public Collection<Level> getLevels() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void accumulateLevel(String name, int result, Time timeLength) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public Record getRecord(String steamID64) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public Collection<Record> getRecords() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void accumulateRecord(String steamID64, int result) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public Collection<Aggregate> getAggregateStats() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void accumulateAggregateStat(String stat, int offset, String category) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public Collection<Death> getDeaths() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void accumulateDeath(String stat, int offset) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Override
    public Map<String, Player> getPlayerStats(String steamID64) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void accumulatePlayerStat(String steamID64, String stat, int offset, String category) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
