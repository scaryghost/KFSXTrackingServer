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
    private def difficulties
    private def levels
    private def deaths
    
    public MatchContent() {
        difficulties= [:]
        levels= [:]
        deaths= [:]
    }
    public boolean load() {
        def conn= DriverManager.getConnection("jdbc:sqlite:kfsxdb.sqlite")
        def rs = stat.executeQuery("select * from levels")
        
        while(rs.next()) {
            def name= rs.getString("name")
            levels[name]= new Level(name)
            levels[name].addTime(rs.getString("time"))
            levels[name].addLosses(rs.getString("losses").toInteger())
            levels[name].addWins(rs.getString("wins").toInteger())
        }
        conn.close()
    }
    public boolean save() {
        Class.forName("org.sqlite.JDBC")
        def conn= DriverManager.getConnection("jdbc:sqlite:kfsxdb.sqlite");
        def statement= "replace into levels (id, name, time, losses, wins) values "
        statement+= "(?,"
        statement+= "coalesce(( select name from levels where id=?),?),"
        statement+= "?,"
        statement+= "?,"
        statement+= "?);"
        def prep= conn.prepareStatement(statement);
        
        levels.each {name, level ->
            prep.setInt(1, level.getData(Level.keyName).hashCode())
            prep.setInt(2, level.getData(Level.keyName).hashCode())
            prep.setString(3, level.getData(Level.keyName))
            prep.setString(4, level.getData(Level.keyTime).toString())
            prep.setInt(5, level.getData(Level.keyLosses))
            prep.setInt(6, level.getData(Level.keyWins))
            prep.addBatch()
        }
        conn.setAutoCommit(false);
        prep.executeBatch()
        conn.commit()
        conn.close()
    }
    public void accumulate(Packet packet) {
        def levelName= packet.getData(MatchPacket.keyMap)
        def difficultyName= packet.getData(MatchPacket.keyDifficulty)
        def length= packet.getData(MatchPacket.keyLength)
        def wave= packet.getData(MatchPacket.keyWave).toInteger()
        def time= packet.getData(MatchPacket.keyTime)
        
        if (levels[levelName] == null) {
            levels[levelName]= new Level(levelName)
        }
        if (difficulties[[difficultyName, length]] == null) {
            difficulties[[difficultyName, length]]= new Difficulty(difficultyName, length, wave)
        }
        
        levels[levelName].addTime(time)
        difficulties[[difficultyName, length]].addTime(time)
        difficulties[[difficultyName, length]].addWave(wave)        
        if (packet.getData(MatchPacket.keyResult).toString() == "1") {
            levels[levelName].addLosses()
            difficulties[[difficultyName, length]].addLosses()
        } else {
            levels[levelName].addWins()
            difficulties[[difficultyName, length]].addWins()
        }
        
        packet.getData(MatchPacket.keyDeaths).each {source, count ->
            if (deaths[source] == null) {
                deaths[source]= count.toInteger()
            } else {
                deaths[source]+= count.toInteger()
            }
        }
        save()
    }
}

