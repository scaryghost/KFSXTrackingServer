/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import groovy.sql.Sql;
import java.sql.Connection

/**
 *
 * @author etsai
 */
public class DataWriter {
    public static def waveCountCategory= "frequency"
    public static def waveReached= "Played"
    
    private final def sql
    
    public DataWriter(Connection conn) {
        this.sql= new Sql(conn)
    }
    
    public synchronized void writeSteamInfo(String steamID64, String name, String avatar) {
        sql.withTransaction {
            sql.execute("insert or ignore into record (steamid64) values (?);", [steamID64])
            sql.execute("insert or ignore into steaminfo (record_id) select r.id from record r where steamid64=?", [steamID64])
            sql.execute("update steaminfo set name=?, avatar=? where record_id=(select id from record where steamid64=?)", [name, avatar, steamID64])
        }
    }
    public synchronized void writeMatchData(MatchPacket packet) {
        Common.logger.finer("Match data: $packet")
        def category= packet.getCategory()

        if (category == "result") {
            def attrs= packet.getAttributes()
            def wins= (attrs.result == PacketParser.Result.WIN) ? 1 : 0
            def losses= (attrs.result == PacketParser.Result.LOSS) ? 1 : 0

            sql.withTransaction {
                sql.execute("insert or ignore into difficulty (name, length) values(?, ?)", 
                    [packet.getDifficulty(), packet.getLength()])
                sql.execute("update difficulty set wins= wins + ?, losses= losses + ?, waveaccum= waveaccum + ?, time= time + ? where name= ? and length= ?", 
                    [wins, losses, packet.getWave(), attrs.duration, packet.getDifficulty(), packet.getLength()])
                sql.execute("insert or ignore into level (name) values(?)", [attrs.level])
                sql.execute("update level set wins= wins + ?, losses= losses + ?, time= time + ? where name=?", 
                    [wins, losses, attrs.duration, attrs.level])
                sql.execute("insert or ignore into leveldata (level_id, difficulty_id) select l.id, d.id from level l, difficulty d where l.name=? and d.name=? and d.length=?",
                    [attrs.level, packet.getDifficulty(), packet.getLength()])
                sql.execute("""update leveldata set wins= wins + ?, losses= losses + ?, time= time + ?, waveaccum= waveaccum + ? where 
                        level_id=(select id from level where name=?) and difficulty_id=(select id from difficulty where name=? and length=?)""",
                    [wins, losses, attrs.duration, packet.getWave(), attrs.level, packet.getDifficulty(), packet.getLength()])
                (1 .. packet.getWave()).each {waveNum ->
                    sql.execute("""insert or ignore into wavedata (difficulty_id, wave, category, stat) select d.id, ?, ?, ? from difficulty d 
                        where d.name=? and d.length=?""", [waveNum, waveCountCategory, waveReached, packet.getDifficulty(), packet.getLength()])
                    sql.execute("""update wavedata set value= value + ? where stat=? and category=? and wave=? and difficulty_id=(select id from 
                        difficulty where name=? and length=?)""", [1, waveReached, waveCountCategory, waveNum, packet.getDifficulty(), packet.getLength()])
                }
            }
        } else {
            sql.withTransaction {
                sql.execute("insert or ignore into difficulty (name, length) values(?, ?)", 
                    [packet.getDifficulty(), packet.getLength()])
                packet.getStats().each {stat, value ->
                    sql.execute("""insert or ignore into wavedata (difficulty_id, wave, category, stat) select d.id, ?, ?, ? from difficulty d 
                        where d.name=? and d.length=?""", [packet.getWave(), category, stat, packet.getDifficulty(), packet.getLength()])
                    sql.execute("""update wavedata set value= value + ? where stat=? and category=? and wave=? and difficulty_id=(select id from 
                        difficulty where name=? and length=?)""", [value, stat, category, packet.getWave(), packet.getDifficulty(), packet.getLength()])
                }
            }
        }
    }
    
    public synchronized void writePlayerData(Iterable<PlayerPacket> packets) {
        sql.withTransaction {
            packets.each {packet ->
                def category= packet.getCategory()
                def steamID64= packet.getSteamID64()
                if (category != "match") {
                    packet.getStats().each {stat, value ->
                        if (stat != "") {
                            sql.execute("insert or ignore into aggregate (stat, category) values (?,?);", [stat, category])
                            sql.execute("update aggregate set value= value + ? where stat=? and category=?", [value, stat, category])
                            if (steamID64 != "") {
                                sql.execute("insert or ignore into player (record_id, stat, category) select r.id, ?, ? from record r where r.steamid64=?", 
                                    [stat, category, steamID64])
                                sql.execute("update player set value=value + ? where stat=? and category=? and record_id=(select id from record where steamid64=?)", 
                                    [value, stat, category, steamID64])
                            }
                        }
                    }
                } else {
                    def attrs= packet.getAttributes()

                    sql.execute("insert or ignore into record (steamid64) values (?);", [steamID64])
                    sql.execute("""update record set wins= wins + ?, losses= losses + ?, disconnects= disconnects + ?, 
                        finale_survived= finale_survived + ?, finale_played= finale_played + ?, time_connected= time_connected + ? where steamid64=?""", 
                        [attrs.result == PacketParser.Result.WIN ? 1 : 0, attrs.result == PacketParser.Result.LOSS ? 1 : 0, 
                        attrs.result == PacketParser.Result.DISCONNECT ? 1 : 0, attrs.finalWaveSurvived, attrs.finalWave, attrs.duration, steamID64])
                    sql.execute("""insert into session (record_id, level, difficulty_id, result, wave, duration) select r.id,?,d.id,?,?,? from record r 
                            inner join difficulty d where r.steamid64=? and d.name=? and d.length=?""",
                        [attrs.level, attrs.result.toString().toLowerCase(), attrs.wave, attrs.duration, steamID64, attrs.difficulty, attrs.length])
                }
            }
        }
    }
}


