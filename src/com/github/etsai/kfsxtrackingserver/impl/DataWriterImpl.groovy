/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import static com.github.etsai.kfsxtrackingserver.Common.statsData
import static com.github.etsai.kfsxtrackingserver.Common.logger
import com.github.etsai.kfsxtrackingserver.DataWriter
import com.github.etsai.kfsxtrackingserver.stats.*

import java.sql.*

/**
 *
 * @author etsai
 */
public class DataWriterImpl extends DataWriter {
    private static final def deathSql= 
    '''replace into deaths (id, name, count) values
    (?, coalesce(( select name from deaths where id=?),?), ?);'''
    private static final def recordSql= 
    '''replace into records (id, steamid, wins, losses, disconnects) values 
    (?, coalesce(( select steamid from records where id=?),?), ?, ?, ?);'''
    private static final def aggregateSql= 
    '''replace into aggregate (id, stat, value, category) values 
    (?, coalesce(( select stat from aggregate where id=?),?), ?, 
    coalesce(( select category from aggregate where id=?),?));'''
    private static final def difficultySql= 
    '''replace into difficulties (id, name, length, wins, losses, wave, time) values
    (?, coalesce(( select name from difficulties where id=?),?),
    coalesce(( select length from difficulties where id=?),?), ?, ?, ?, ?);'''
    private static final def levelSql=
    '''replace into levels (id, name, wins, losses, time) values 
    (?, coalesce(( select name from levels where id=?),?), ?, ?, ?);'''
    private static final def playerSql= 
    '''replace into player (id, steamid, stats, category) values
    (?, coalesce(( select steamid from player where id=?),?), ?, 
    coalesce(( select category from player where id=?),?));'''
    
    private def conn
    private def statIDs= [:]
    private def preparedStatements
    private def saveDeaths, saveAggregate, playerSteamIds

    public DataWriterImpl(def conn) {
        this.conn= conn
        reset()
    }
    private synchronized void reset() {
        preparedStatements= [:]
        [deathSql, recordSql, aggregateSql, difficultySql, 
            levelSql, playerSql].each {sql ->
            statIDs[sql]= new HashSet()
            preparedStatements[sql]= conn.prepareStatement(sql)
        }
        saveDeaths= false
        saveAggregate= false
        playerSteamIds= new HashSet()
    }
    public synchronized void addDiff(Difficulty diff) {
        statIDs[difficultySql].add(diff)
    }
    public synchronized void addLevel(Level level) {
        statIDs[levelSql].add(level)
    }
    public synchronized void addRecord(Record record) {
        statIDs[recordSql].add(record)
    }
    public synchronized void addDeath(Death death) {
        statIDs[deathSql].add(death)
    }
    public synchronized void addAggregate(Aggregate aggr) {
        statIDs[aggregateSql].add(aggr)
    }
    public synchronized void addPlayer(Player player) {
        statIDs[playerSql].add(player)
    }
    
    @Override
    public synchronized void run() {
        statIDs[difficultySql].each {diff ->
            logger.finer("Saving difficulty: ${diff.getName()}/${diff.getLength()}")
            def ps= preparedStatements[difficultySql]
            def id= diff.getId()
        
            ps.setInt(1, id)
            ps.setInt(2, id)
            ps.setString(3, diff.getName())
            ps.setInt(4, id)
            ps.setString(5, diff.getLength())
            ps.setInt(6, diff.getWins())
            ps.setInt(7, diff.getLosses())
            ps.setInt(8, diff.getWave())
            ps.setString(9, diff.getTime().toString())
            ps.addBatch()
        }
   
        statIDs[levelSql].each {level ->
            logger.finer("Saving level: ${level.getName()}")
            def ps= preparedStatements[levelSql]
            def id= level.getId()
        
            ps.setInt(1, id)
            ps.setInt(2, id)
            ps.setString(3, level.getName())
            ps.setInt(4, level.getWins())
            ps.setInt(5, level.getLosses())
            ps.setString(6, level.getTime().toString())
            ps.addBatch()
        }
 
        statIDs[recordSql].each {record ->
            logger.finer("Saving record for steamID64: ${record.getSteamId()}")
            def ps= preparedStatements[recordSql]
            def id= record.getId()

            ps.setInt(1, id)
            ps.setInt(2, id)
            ps.setString(3, record.getSteamId())
            ps.setInt(4, record.getWins())
            ps.setInt(5, record.getLosses())
            ps.setInt(6, record.getDisconnects())
            ps.addBatch()
        }
        
        logger.finer("Saving aggregate player stats")
        statIDs[aggregateSql].each {aggregate ->    
            def ps= preparedStatements[aggregateSql]
            def id= aggregate.getId()

            ps.setInt(1, id)
            ps.setInt(2, id)
            ps.setString(3, aggregate.getStat())
            ps.setString(4, aggregate.getValue())
            ps.setInt(5, id)
            ps.setString(6, aggregate.getCategory())
            ps.addBatch()
        }
        
        
        logger.finer("Saving deaths")
        statIDs[deathSql].each {death ->
            def ps= preparedStatements[deathSql]
            def id= death.getId()

            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setString(3, death.getStat());
            ps.setInt(4, death.getValue().toInteger());
            ps.addBatch();
        }
        
        logger.finer("Saving individual player stats")
        statIDs[playerSql].each {player ->
            def ps= preparedStatements[playerSql]
            
            def id= player.getId()
            def stats= []
            player.getStats().each {stat, value ->
                stats << "${stat}=${value}"
            }
            ps.setInt(1, id)
            ps.setInt(2, id)
            ps.setString(3, player.getSteamID64())
            ps.setString(4, stats.join(","))
            ps.setInt(5, id)
            ps.setString(6, player.getCategory())
            ps.addBatch()
        }

        preparedStatements.each {sql, ps ->
            ps.executeBatch()
        }
        
        logger.info("Committing changes to database")
        conn.commit()
        reset()
    }
}

