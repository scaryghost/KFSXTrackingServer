/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.DataWriter
import static com.github.etsai.kfsxtrackingserver.Common.statsData

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
    (?, coalesce(( select stat from aggregate where id=?),?), ?, coalesce(( select category from aggregate where id=?),?);'''
    private static def difficultySql= 
    '''replace into difficulties (id, name, length, wins, losses, wave, time) values
    (?, coalesce(( select name from difficulties where id=?),?),
    coalesce(( select length from difficulties where id=?),?), ?, ?, ?, ?);'''
    private static def levelSql=
'''replace into levels (id, name, wins, losses, time) values 
    (?, coalesce(( select name from levels where id=?),?), ?, ?, ?);'''
    
    private enum Table {
        Difficulty(deathSql), Level(levelSql), Record(recordSql), Aggregate(aggregateSql), Death(deathSql);
        
        public final def sql
        
        public Table(def sql) {
            this.sql= sql
        }
    }
    
    private def conn
    private def preparedStatements

    public DataWriterImpl() {
        conn= DriverManager.getConnection("jdbc:sqlite:kfsxdb.sqlite");
        conn.setAutoCommit(false);
        
        reset()
    }
    private void reset() {
        preparedStatements= [:]
        Table.getValues().each {value ->
            preparedStatements[value]= conn.prepareStatement(value.sql)
        }
    }
    public void addDiffId(String name, String length) {
        def ps= preparedStatements[Difficulty]
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
        def ps= preparedStatements[Level]
        def level= statsData.getLevel(name)
        def id= level.getId()
        
        ps.setInt(1, id)
        ps.setInt(2, id)
        ps.setString(3, level.getName())
        ps.setString(4, level.getWins())
        ps.setString(5, level.getLosses())
        ps.setString(6, level.getTime().toString())
        ps.addBatch()
    }
    public void addRecordId(String steamid) {
        def ps= preparedStatements[Record]
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
    
    @Override
    public void run() {
        statsData.getAggregateStats().each {aggregate ->
            def ps= preparedStatements[Aggregate]
            def id= aggregate.getId()
            
            ps.setInt(1, id)
            ps.setInt(2, id)
            ps.setString(3, aggregate.getStat())
            ps.setString(4, aggregate.getValue())
            ps.setString(5, id)
            ps.setString(6, aggregate.getCategory())
            ps.addBatch()
        }
        
        statsData.getDeaths().each {death ->
            def ps= preparedStatements[Death]
            def id= death.getId()
            
            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setString(3, death.getStat());
            ps.setInt(4, (int)death.getValue());
            ps.addBatch();
        }
        
        preparedStatements.each {ps ->
            ps.executeBatch()
        }
        conn.commit()
        reset()
    }
}

