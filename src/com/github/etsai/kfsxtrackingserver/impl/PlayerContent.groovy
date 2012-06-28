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
 * @author etsai
 */
public class PlayerContent implements Content {
    private final def id
    private def sessions
    private def accumStats
    private def packets
    private def levels
    private def difficulties
    
    private def lastLevelName
    private def lastDiffKey
    private def lastSessionHash
    
    public PlayerContent(def id) {
        this.id= id
        sessions= [:]
        
        accumStats= [:]
        accumStats["player"]= [:]
        accumStats["actions"]= [:]
        accumStats["weapons"]= [:]
        accumStats["kills"]= [:]
        accumStats["perks"]= [:]
        
        difficulties= [:]
        levels= [:]
        
        packets= []
        
    }
    public boolean load() {
        def conn= DriverManager.getConnection("jdbc:sqlite:kfsxdb.sqlite")
        def stat = conn.createStatement()
        def rs
        
        rs= stat.executeQuery("select * from pLevels where steamid='${id}'")
        while(rs.next()) {
            def name= rs.getString("name")
            levels[name]= new Level(name)
            levels[name].addTime(rs.getString("time"))
            levels[name].addWins(rs.getInt("wins"))
            levels[name].addLosses(rs.getInt("losses"))
        }
        rs.close()
        
        rs= stat.executeQuery("select * from pDifficulties where steamid='${id}'")
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
        
        rs= stat.executeQuery("select * from pSessions where steamid='${id}'")
        while(rs.next()) {
            def sessionid= rs.getString("sessionid")
            def entries= [:]
            
            entries["timestamp"]= rs.getString("timestamp")
            ["player","actions","weapons", "kills", "perks","match"].each {group ->
                rs.getString(group).split(",").each {stats ->
                    def keyVal= stats.split("=")
                    entries[group][keyVal[0]]= keyVal[1]
                }
            }
            sessions[sessionid]= entries
            
        }
        rs.close()
        
        rs= stat.executeQuery("select * from pAccumulate where steamid='${id}'")
        while(rs.next()) {
            ["player", "actions", "weapons", "kills", "perks"].each {group ->
                rs.getString(group).split(",").each {stats ->
                    def keyVal= stats.split("=")
                    accumStats[group][keyVal[0]]= keyVal[1].toInteger()
                }
            }
        }
        rs.close()
        conn.close()
    }
    public boolean save() {
        def conn= DriverManager.getConnection("jdbc:sqlite:kfsxdb.sqlite");
        conn.setAutoCommit(false);
        
        def statement= "replace into pLevels (id, name, time, steamid, wins, losses) values "
        statement+= "(?,"
        statement+= "coalesce(( select name from pLevels where id=?),?),"
        statement+= "?,"
        statement+= "coalesce(( select steamid from pLevels where id=?),?),"
        statement+= "?,"
        statement+= "?);"
        def prep= conn.prepareStatement(statement);
        
        def levelId= "${lastLevelName}-${id}".hashCode()
        def level= levels[lastLevelName]
        prep.setInt(1, levelId)
        prep.setInt(2, levelId)
        prep.setString(3, level.getData(Level.keyName))
        prep.setString(4, level.getData(Level.keyTime).toString())
        prep.setInt(5, levelId)
        prep.setString(6, id)
        prep.setInt(7, level.getData(Level.keyWins))
        prep.setInt(8, level.getData(Level.keyLosses))
        prep.addBatch()
        prep.executeBatch()
        
        def diffStatement= "replace into pDifficulties (id, name, steamid, length, wins, losses, time, wave) values "
        diffStatement+= "(?,"
        diffStatement+= "coalesce(( select name from pDifficulties where id=?),?),"
        diffStatement+= "coalesce(( select steamid from pDifficulties where id=?),?),"
        diffStatement+= "coalesce(( select length from pDifficulties where id=?),?),"
        diffStatement+= "?,"
        diffStatement+= "?,"
        diffStatement+= "?,"
        diffStatement+= "?);"
        def diff= difficulties[lastDiffKey]
        def diffId= "${lastDiffKey[0]}-${lastDiffKey[1]}-${id}".hashCode()
        def diffprep= conn.prepareStatement(diffStatement)
        diffprep.setInt(1, diffId)
        diffprep.setInt(2, diffId)
        diffprep.setString(3, diff.getData(Difficulty.keyName))
        diffprep.setInt(4, diffId)
        diffprep.setString(5, id)
        diffprep.setInt(6, diffId)
        diffprep.setString(7, diff.getData(Difficulty.keyLength))
        diffprep.setInt(8, diff.getData(Difficulty.keyWins))
        diffprep.setInt(9, diff.getData(Difficulty.keyLosses))
        diffprep.setString(10, diff.getData(Difficulty.keyTime).toString())
        diffprep.setInt(11, diff.getData(Difficulty.keyWave))
        diffprep.addBatch()
        diffprep.executeBatch()
        
        def accumStatement= "replace into pAccumulate (id, steamid, player, actions, weapons, kills, perks) values "
        accumStatement+= "(?, "
        accumStatement+= "coalesce(( select steamid from pAccumulate where id=?),?), "
        accumStatement+= "?, "
        accumStatement+= "?, "
        accumStatement+= "?, "
        accumStatement+= "?, "
        accumStatement+= "?);"
        def accumPrep= conn.prepareStatement(accumStatement)
        def idHash= id.hashCode()
        accumPrep.setInt(1, idHash)
        accumPrep.setInt(2, idHash)
        accumPrep.setString(3, id)
        def index= 4
        ["player", "actions", "weapons", "kills", "perks"].each {group ->
            def accum= accumStats[group].inject([]) {list, key, val ->
                list << "${key}=${val}"
            }
            accumPrep.setString(index, accum.join(","))
            index++
        }
        accumPrep.addBatch()
        accumPrep.executeBatch()
        
        def sessionStatement= "insert into pSessions (sessionid, timestamp, steamid, player, actions, weapons, kills, perks, match) values (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        def sessionPrep= conn.prepareStatement(sessionStatement)
        def session= sessions[lastSessionHash]
        sessionPrep.setString(1, lastSessionHash.toString())
        sessionPrep.setString(2, session["timestamp"])
        sessionPrep.setString(3, id)
        index= 4
        ["player", "actions", "weapon", "kills", "perks", "match"].each {group ->
            def accum= session[group].inject([]) {list, key, val ->
                list << "${key}=${val}"
            }
            sessionPrep.setString(index, accum.join(","))
            index++
        }
        sessionPrep.addBatch()
        sessionPrep.executeBatch()
        
        conn.commit()
        conn.close()
        
        lastSessionHash= null
        lastDiffKey= null
        lastLevelName= null
    }
    public void accumulate(Packet packet) {
        def id= packet.getData(PlayerPacket.keyPlayerId)
        def seqnum= packet.getSeqnum()
        
        packets[seqnum]= packet
        def completed= packets.last().isLast() && 
                packets.inject(true) {acc, val -> acc && (val != null) }
        if (completed) {
            buildSession()
        }
    }
    
    private void buildSession() {
        def time
        def calendar= Calendar.getInstance(TimeZone.getTimeZone("PST"))
        def hash= calendar.hashCode()
        lastSessionHash= hash
        sessions[hash]= [:]
        sessions[hash]["timestamp"]= calendar.getTime().toString()
        def accum= {packet, map, group ->
            sessions[hash][group]= [:]

            packet.getData(PlayerPacket.keyStats).each {stat, value ->
                sessions[hash][group][stat]= value
                if (map[stat] == null) map[stat]= 0
                map[stat]+= value.toInteger()
                if (stat == "time connected") time= new Time(value)
            }
        }
        def groupActions= [:]
        groupActions["player"]= {packet ->
            accum(packet, accumStats["player"], "player")
        }
        groupActions["weapon"]= {packet ->
            accum(packet, accumStats["weapons"], "weapon")
        }
        groupActions["kills"]= {packet ->
            accum(packet, accumStats["kills"], "kills")
        }
        groupActions["perks"]= {packet ->
            accum(packet, accumStats["perks"], "perks")
        }
        groupActions["actions"]= {packet ->
            accum(packet, accumStats["actions"], "actions")
        }
        groupActions["match"]= {packet ->
            sessions[hash]["match"]= packet.getData(PlayerPacket.keyStats)
            def levelName= sessions[hash]["match"]["map"]
            def difficultyName= sessions[hash]["match"]["difficulty"]
            def length= sessions[hash]["match"]["length"]
            def wave= sessions[hash]["match"]["wave"].toInteger()
            def result= sessions[hash]["match"]["result"]
            
            lastLevelName= levelName
            lastDiffKey= [difficultyName, length]
            if (levels[levelName] == null) {
                levels[levelName]= new Level(levelName)
            }
            if (difficulties[[difficultyName, length]] == null) {
                difficulties[[difficultyName, length]]= new Difficulty(difficultyName, length, wave)
            }
            levels[levelName].addTime(time)
            difficulties[[difficultyName, length]].addTime(time)
            difficulties[[difficultyName, length]].addWave(wave)
            if (result != "2") {
                levels[levelName].addLosses()
                difficulties[[difficultyName, length]].addLosses()
            } else {
                levels[levelName].addWins()
                difficulties[[difficultyName, length]].addWins()
            }
        }
        
        packets.each {packet ->
            groupActions[packet.getData(PlayerPacket.keyGroup)](packet)
        }
        packets= []
        save()
    }
}

