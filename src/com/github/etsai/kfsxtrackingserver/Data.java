/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.stats.*;
import java.util.Collections;
import java.util.Map;
import java.sql.*;

/**
 * Interface for accessing and modifying the server data
 * @author etsai
 */
public class Data {
    public static Data load(Connection conn) throws SQLException {
        Data data= new Data();
        Statement statement= conn.createStatement();
        ResultSet rs;
        
        rs= statement.executeQuery("select * from deaths");
        while(rs.next()) {
            Death death= Death.build(rs);
            data.deaths.put(death.getId(), death);
        }
        rs.close();
        
        rs= statement.executeQuery("select * from records");
        while(rs.next()) {
            Record record= Record.build(rs);
            data.records.put(record.getSteamId(), record);
        }
        rs.close();
        
        rs= statement.executeQuery("select * from levels");
        while(rs.next()) {
            Level level= Level.build(rs);
            data.levels.put(level.getId(),level);
        }
        rs.close();
        
        rs= statement.executeQuery("select * from difficulties");
        while(rs.next()) {
            Difficulty difficulty= Difficulty.build(rs);
            data.difficulties.put(difficulty.getId(), difficulty);
        }
        rs.close();
        
        rs= statement.executeQuery("select * from aggregate");
        while(rs.next()) {
            Aggregate aggregate= Aggregate.build(rs);
            data.aggregate.put(aggregate.getId(), aggregate);
        }
        rs.close();
        return data;
    }
    
    private static Integer genDifficultyKey(String name, String length) {
        return String.format("%s-%s",name, length).hashCode();
    }
    private static Integer genLevelKey(String name) {
        return name.hashCode();
    }
    private static Integer genAggregateKey(String stat, String category) {
        return String.format("%s-%s",stat, category).hashCode();
    }
    
    private Map<Integer, Difficulty> difficulties;
    private Map<Integer, Level> levels;
    private Map<String, Record> records;
    private Map<Integer, Aggregate> aggregate;
    private Map<Integer, Death> deaths;
    
    public Difficulty getDifficulty(String name, String length) {
        return difficulties.get(genDifficultyKey(name, length));
    }
    public Iterable<Difficulty> getDifficulties() {
        return Collections.unmodifiableCollection(difficulties.values());
    }
    public void accumulateDifficulty(String name, String length, int result, int wave, Time timeLength) {
        Difficulty tempDiff;
        Integer id= genDifficultyKey(name, length);
        
        if (!difficulties.containsKey(id)) {
            tempDiff= new Difficulty(id.intValue(), name, length);
        } else {
            tempDiff= difficulties.get(id);
        }
        switch (result) {
            case 1:
                tempDiff.addLosses(1);
                break;
            case 2:
                tempDiff.addWins(1);
                break;
            default:
                throw new RuntimeException("Unrecognized result value: "+result);
        }
        tempDiff.addWave(wave);
        tempDiff.addTime(timeLength);
        difficulties.put(id, tempDiff);
    }
    
    public Level getLevel(String name) {
        return levels.get(genLevelKey(name));
    }
    public Iterable<Level> getLevels() {
        return Collections.unmodifiableCollection(levels.values());
    }
    public void accumulateLevel(String name, int result, Time timeLength) {
        Level tempLevel;
        Integer id= genLevelKey(name);
        
        if (!levels.containsKey(id)) {
            tempLevel= new Level(id.intValue(), name);
        } else {
            tempLevel= levels.get(id);
        }
        switch (result) {
            case 1:
                tempLevel.addLosses(1);
                break;
            case 2:
                tempLevel.addWins(1);
                break;
            default:
                throw new RuntimeException("Unrecognized result value: "+result);
        }
        tempLevel.addTime(timeLength);
        levels.put(id, tempLevel);
    }
    
    public Record getRecord(String steamid) {
        return records.get(steamid);
    }
    public Iterable<Record> getRecords() {
        return Collections.unmodifiableCollection(records.values());
    }
    public void accumulateRecord(String steamid, int result) {
        Record tempRecord;
        
        if (!records.containsKey(steamid)) {
            tempRecord= new Record(steamid);
        } else {
            tempRecord= records.get(steamid);
        }
        switch (result) {
            case 0:
                tempRecord.addDisconnects(1);
                break;
            case 1:
                tempRecord.addLosses(1);
                break;
            case 2:
                tempRecord.addWins(1);
                break;
            default:
                throw new RuntimeException("Unrecognized result value: "+result);
        }
        records.put(steamid, tempRecord);
    }
    
    public Iterable<Aggregate> getAggregateStats() {
        return Collections.unmodifiableCollection(aggregate.values());
    }
    public void accumulateAggregateStat(String stat, int offset, String category) {
        Integer id= genAggregateKey(stat, category);
        Aggregate tempAggregate;
        
        if (!aggregate.containsKey(id)) {
            tempAggregate= new Aggregate(id.intValue(), stat, category);
        } else {
            tempAggregate= aggregate.get(id);
        }
        tempAggregate.addValue(offset);
        aggregate.put(id, tempAggregate);
    }
    
    public Iterable<Death> getDeaths() {
        return Collections.unmodifiableCollection(deaths.values());
    }
    public void accumulateDeath(String stat, int offset) {
        Integer id= stat.hashCode();
        Death tempDeath;
        
        if (!deaths.containsKey(id)) {
            tempDeath= new Death(id, stat);
        } else {
            tempDeath= deaths.get(id);
        }
        tempDeath.addValue(offset);
        deaths.put(id, tempDeath);
    }
}
