/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.logger;
import com.github.etsai.kfsxtrackingserver.stats.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for accessing and modifying player and match data
 * @author etsai
 */
public class Data {
    public static DataWriter writer;
    
    public static Data load(Connection conn) throws SQLException {
        Data data= new Data();
        Statement statement= conn.createStatement();
        ResultSet rs;
        
        logger.fine("Loading deaths");
        rs= statement.executeQuery("select * from deaths");
        while(rs.next()) {
            Death death= Death.build(rs);
            data.deaths.put(death.getId(), death);
        }
        rs.close();
        
        logger.fine("Loading player records");
        rs= statement.executeQuery("select * from records");
        while(rs.next()) {
            Record record= Record.build(rs);
            data.records.put(record.getSteamId(), record);
        }
        rs.close();
        
        logger.fine("Loading level stats");
        rs= statement.executeQuery("select * from levels");
        while(rs.next()) {
            Level level= Level.build(rs);
            data.levels.put(level.getId(),level);
        }
        rs.close();
        
        logger.fine("Loading difficulty stats");
        rs= statement.executeQuery("select * from difficulties");
        while(rs.next()) {
            Difficulty difficulty= Difficulty.build(rs);
            data.difficulties.put(difficulty.getId(), difficulty);
        }
        rs.close();
        
        logger.fine("Loading aggregate player stats");
        rs= statement.executeQuery("select * from aggregate");
        while(rs.next()) {
            Aggregate aggregate= Aggregate.build(rs);
            data.aggregate.put(aggregate.getId(), aggregate);
        }
        rs.close();
        
        logger.fine("Loading individual player stats");
        rs= statement.executeQuery("select * from player");
        while(rs.next()) {
            Player player= Player.build(rs);
            String steamid= player.getSteamId();
            if (!data.playerStats.containsKey(steamid)) {
                data.playerStats.put(steamid, new HashMap());
            }
            data.playerStats.get(steamid).put(player.getCategory(), player);
        }
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
    private static Integer genPlayerKey(String steamid, String category) {
        return String.format("%s-%s",steamid, category).hashCode();
    }
    
    private final Map<Integer, Difficulty> difficulties= new HashMap<>();
    private final Map<Integer, Level> levels= new HashMap<>();
    private final Map<String, Record> records= new HashMap<>();
    private final Map<Integer, Aggregate> aggregate= new HashMap<>();
    private final Map<Integer, Death> deaths= new HashMap<>();
    private final Map<String, Map<String, Player>> playerStats= new HashMap<>();
    
    public Difficulty getDifficulty(String name, String length) {
        return difficulties.get(genDifficultyKey(name, length));
    }
    public Collection<Difficulty> getDifficulties() {
        return Collections.unmodifiableCollection(difficulties.values());
    }
    public void accumulateDifficulty(String name, String length, int result, int wave, Time timeLength) {
        Difficulty tempDiff;
        Integer id= genDifficultyKey(name, length);
        
        synchronized(difficulties) {
            if (!difficulties.containsKey(id)) {
                tempDiff= new Difficulty(id.intValue(), name, length);
            } else {
                tempDiff= difficulties.get(id);
            }
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
        
        synchronized(difficulties) {
            difficulties.put(id, tempDiff);
            writer.addDiffId(name, length);
        }
    }
    
    public Level getLevel(String name) {
        return levels.get(genLevelKey(name));
    }
    public Collection<Level> getLevels() {
        return Collections.unmodifiableCollection(levels.values());
    }
    public void accumulateLevel(String name, int result, Time timeLength) {
        Level tempLevel;
        Integer id= genLevelKey(name);
        
        synchronized(levels) {
            if (!levels.containsKey(id)) {
                tempLevel= new Level(id.intValue(), name);
            } else {
                tempLevel= levels.get(id);
            }
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
        
        synchronized(levels) {
            levels.put(id, tempLevel);
            writer.addLevelId(name);
        }
    }
    
    public Record getRecord(String steamid) {
        return records.get(steamid);
    }
    public Collection<Record> getRecords() {
        return Collections.unmodifiableCollection(records.values());
    }
    public void accumulateRecord(String steamid, int result) {
        Record tempRecord;
        
        synchronized(records) {
            if (!records.containsKey(steamid)) {
                tempRecord= new Record(steamid);
            } else {
                tempRecord= records.get(steamid);
            }
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
        
        synchronized(records) {
            records.put(steamid, tempRecord);
            writer.addRecordId(steamid);
        }
    }
    
    public Collection<Aggregate> getAggregateStats() {
        return Collections.unmodifiableCollection(aggregate.values());
    }
    public void accumulateAggregateStat(String stat, int offset, String category) {
        Integer id= genAggregateKey(stat, category);
        Aggregate tempAggregate;
        
        synchronized(aggregate) {
            if (!aggregate.containsKey(id)) {
                tempAggregate= new Aggregate(id.intValue(), stat, category);
            } else {
                tempAggregate= aggregate.get(id);
            }
        }
        tempAggregate.addValue(offset);
        
        synchronized(aggregate) {
            aggregate.put(id, tempAggregate);
            writer.addAggregate(stat, category);
        }
    }
    
    public Collection<Death> getDeaths() {
        return Collections.unmodifiableCollection(deaths.values());
    }
    public void accumulateDeath(String stat, int offset) {
        Integer id= stat.hashCode();
        Death tempDeath;
        
        synchronized(deaths) {
            if (!deaths.containsKey(id)) {
                tempDeath= new Death(id, stat);
            } else {
                tempDeath= deaths.get(id);
            }
        }
        tempDeath.addValue(offset);
        writer.addDeath(stat);
        
        synchronized(deaths) {
            deaths.put(id, tempDeath);
        }
    }
    
    public Map<String, Player> getPlayerStats(String steamid) {
        return Collections.unmodifiableMap(playerStats.get(steamid));
    }
    public void accumulatePlayerStat(String steamid, String stat, int offset, String category) {
        Map<String, Player> categories;
        Player temp;
        
        synchronized(playerStats) {
            if (!playerStats.containsKey(steamid)) {
                playerStats.put(steamid, new HashMap());
            }
            categories= playerStats.get(steamid);
        }
        
        synchronized(categories) {
            if (!categories.containsKey(category)) {
                categories.put(category, new Player(genPlayerKey(steamid, category), steamid));
            }
            temp= categories.get(category);
            temp.accumulate(stat, offset);
        }
        
        synchronized(playerStats) {
            categories.put(category, temp);
            writer.addPlayer(steamid);
        }
    }
}
