/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Content
import com.github.etsai.kfsxtrackingserver.Packet
import com.github.etsai.kfsxtrackingserver.Time

import java.sql.*;

/**
 *
 * @author eric
 */
public class MatchContent implements Content {
    private static def levelsSql=
'''replace into levels (id, name, time, losses, wins) values 
    (?, coalesce(( select name from levels where id=?),?), ?, ?, ?);'''
    private static def difficultiesSql=
'''replace into difficulties (id, name, length, wave, wins, losses, time) values
    (?, coalesce(( select name from difficulties where id=?),?),
    coalesce(( select length from difficulties where id=?),?), ?, ?, ?, ?);'''
    private static def deathsSql=
'''replace into deaths (id, name, count) values
    (?, coalesce(( select name from deaths where id=?),?), ?);'''
        
    private def difficulties
    private def levels
    private def deaths
    private def lastLevelName
    private def lastDiffKey
    
    public MatchContent() {
        difficulties= [:]
        levels= [:]
        deaths= [:]
    }
    public boolean load() {
        def conn= DriverManager.getConnection("jdbc:sqlite:kfsxdb.sqlite")
        def stat = conn.createStatement()
        def rs = stat.executeQuery("select * from levels")
        
        while(rs.next()) {
            def name= rs.getString("name")
            levels[name]= new Level(name)
            levels[name].addTime(rs.getString("time"))
            levels[name].addLosses(rs.getInt("losses"))
            levels[name].addWins(rs.getInt("wins"))
        }
        rs.close()
        
        rs = stat.executeQuery("select * from difficulties")
        while(rs.next()) {
            def name= rs.getString("name")
            def length= rs.getString("length")
            def wave= rs.getInt("wave")
            difficulties[[name, length]]= new Difficulty(name, length, wave)
            difficulties[[name, length]].addTime(rs.getString("time"))
            difficulties[[name, length]].addLosses(rs.getInt("losses"))
            difficulties[[name, length]].addWins(rs.getInt("wins"))
        }
        rs.close()
        
        rs = stat.executeQuery("select * from deaths")
        while(rs.next()) {
            deaths[rs.getString("name")]= rs.getInt("count")
        }
        rs.close()
        conn.close()
    }
    public boolean save() {
        
        def conn= DriverManager.getConnection("jdbc:sqlite:kfsxdb.sqlite");
        conn.setAutoCommit(false);
        
        
        def prep= conn.prepareStatement(levelsSql);
        
        def level= levels[lastLevelName]
        prep.setInt(1, level.hashCode())
        prep.setInt(2, level.hashCode())
        prep.setString(3, level.getData(Level.keyName))
        prep.setString(4, level.getData(Level.keyTime).toString())
        prep.setInt(5, level.getData(Level.keyLosses))
        prep.setInt(6, level.getData(Level.keyWins))
        prep.addBatch()
        prep.executeBatch()
        
        
        def diff= difficulties[lastDiffKey]
        def diffprep= conn.prepareStatement(difficultiesSql)
        diffprep.setInt(1, diff.hashCode())
        diffprep.setInt(2, diff.hashCode())
        diffprep.setString(3, diff.getData(Difficulty.keyName))
        diffprep.setInt(4, diff.hashCode())
        diffprep.setString(5, diff.getData(Difficulty.keyLength))
        diffprep.setInt(6, diff.getData(Difficulty.keyWave))
        diffprep.setInt(7, diff.getData(Difficulty.keyWins))
        diffprep.setInt(8, diff.getData(Difficulty.keyLosses))
        diffprep.setString(9, diff.getData(Difficulty.keyTime).toString())
        diffprep.addBatch()
        diffprep.executeBatch()
        
        
        def deathPrep= conn.prepareStatement(deathsSql)
        deaths.each {source, count ->
            deathPrep.setInt(1, source.hashCode())
            deathPrep.setInt(2, source.hashCode())
            deathPrep.setString(3, source)
            deathPrep.setInt(4, count)
            deathPrep.addBatch()
        }
        deathPrep.executeBatch()
        
        conn.commit()
        conn.close()
        lastLevelName= null
    }
    public void accumulate(Packet packet) {
        def time= packet.getData(MatchPacket.keyTime)
        
        lastLevelName= packet.getData(MatchPacket.keyMap)
        lastDiffKey= [packet.getData(MatchPacket.keyDifficulty), packet.getData(MatchPacket.keyLength)]
        def tempDiff= difficulties[lastDiffKey] == null ? 
                new Difficulty(lastDiffKey[0], lastDiffKey[1], 0) : difficulties[lastDiffKey]
        def tempLevel= levels[lastLevelName] == null ? new Level(lastLevelName) : levels[levelName]
        def tempDeaths= deaths.clone()
        
        tempDiff.addWave(packet.getData(MatchPacket.keyWave).toInteger())
        tempLevel.addTime(time)
        tempDiff.addTime(time)
        if (packet.getData(MatchPacket.keyResult).toString() == "1") {
            tempLevel.addLosses()
            tempDiff.addLosses()
        } else {
            tempLevel.addWins()
            tempDiff.addWins()
        }
        
        packet.getData(MatchPacket.keyDeaths).each {source, count ->
            if (tempDeaths[source] == null) {
                tempDeaths[source]= count.toInteger()
            } else {
                tempDeaths[source]+= count.toInteger()
            }
        }
        difficulties[lastDiffKey]= tempDiff
        levels[lastLevelName]= tempLevel
        deaths= tempDeaths
        save()
    }
}

