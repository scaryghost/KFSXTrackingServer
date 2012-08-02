/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.DataWriter
import static com.github.etsai.kfsxtrackingserver.Common.statsData
import static com.github.etsai.kfsxtrackingserver.Common.logger

import java.sql.*

/**
 *
 * @author etsai
 */
public class DataWriterImpl extends DataWriter {
    private static def deathSql= 
    '''replace into deaths (id, name, count) values
    (?, coalesce(( select name from deaths where id=?),?), ?);'''
    private static def recordSql= 
    '''replace into records (id, steamid, wins, losses, disconnects) values 
    (?, coalesce(( select steamid from records where id=?),?), ?, ?, ?);'''
    private static def aggregateSql= 
    '''replace into aggregate (id, stat, value, category) values 
    (?, coalesce(( select stat from aggregate where id=?),?), ?, 
    coalesce(( select category from aggregate where id=?),?));'''
    private static def difficultySql= 
    '''replace into difficulties (id, name, length, wins, losses, wave, time) values
    (?, coalesce(( select name from difficulties where id=?),?),
    coalesce(( select length from difficulties where id=?),?), ?, ?, ?, ?);'''
    private static def levelSql=
    '''replace into levels (id, name, wins, losses, time) values 
    (?, coalesce(( select name from levels where id=?),?), ?, ?, ?);'''
    private static def playerSql= 
    '''replace into player (id, steamid, stats, category) values
    (?, coalesce(( select steamid from player where id=?),?), ?, 
    coalesce(( select category from player where id=?),?));'''
    
    private def conn
    private def preparedStatements
    private def saveDeaths, saveAggregate, playerSteamIds

    public DataWriterImpl(def conn) {
        this.conn= conn
        reset()
    }
    private void reset() {
        preparedStatements= [:]
        [deathSql, recordSql, aggregateSql, difficultySql, 
            levelSql, playerSql].each {sql ->
            preparedStatements[sql]= conn.prepareStatement(sql)
        }
        saveDeaths= false
        saveAggregate= false
        playerSteamIds= new HashSet()
    }
    public void addDiffId(String name, String length) {
        def ps= preparedStatements[difficultySql]
        def diff= statsData.getDifficulty(name, length)
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
    public void addLevelId(String name) {
        def ps= preparedStatements[levelSql]
        def level= statsData.getLevel(name)
        def id= level.getId()
        
        ps.setInt(1, id)
        ps.setInt(2, id)
        ps.setString(3, level.getName())
        ps.setInt(4, level.getWins())
        ps.setInt(5, level.getLosses())
        ps.setString(6, level.getTime().toString())
        ps.addBatch()
    }
    public void addRecordId(String steamid) {
        def ps= preparedStatements[recordSql]
        def record= statsData.getRecord(steamid)
        def id= record.getId()
        
        ps.setInt(1, id)
        ps.setInt(2, id)
        ps.setString(3, record.getSteamId())
        ps.setInt(4, record.getWins())
        ps.setInt(5, record.getLosses())
        ps.setInt(6, record.getDisconnects())
        ps.addBatch()
    }
    
    public void addDeath(String death) {
        saveDeaths= true
    }
    public void addAggregate(String stat, String category) {
        saveAggregate= true
    }
    
    public void addPlayer(String steamid) {
        playerSteamIds.add(steamid)
    }
    
    @Override
    public void run() {
        if (saveDeaths) {
            statsData.getAggregateStats().each {aggregate ->
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
        }
        if (saveAggregate) {
            statsData.getDeaths().each {death ->
                def ps= preparedStatements[deathSql]
                def id= death.getId()

                ps.setInt(1, id);
                ps.setInt(2, id);
                ps.setString(3, death.getStat());
                ps.setInt(4, death.getValue().toInteger());
                ps.addBatch();
            }
        }
        playerSteamIds.each {steamid ->
            statsData.getPlayerStats(steamid).each {category, player ->
                def ps= preparedStatements[playerSql]
                def id= player.getId()
                def stats= []
                player.getStats().each {stat, value ->
                    stats << "${stat}=${value}"
                }
                ps.setInt(1, id)
                ps.setInt(2, id)
                ps.setString(3, steamid)
                ps.setString(4, stats.join(","))
                ps.setInt(5, id)
                ps.setString(6, category)
                ps.addBatch()
            }
        }
        
        preparedStatements.each {sql, ps ->
            ps.executeBatch()
        }
        
        conn.commit()
        reset()
        logger.info("Writing changes to database")
    }
}

