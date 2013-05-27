/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import groovy.sql.Sql
import java.sql.Connection

/**
 *
 * @author etsai
 */
public class DataWriter {
    private static def wavedataSqlInsert= """insert or ignore into wave_data (difficulty_id, level_id, wave, category, stat) select d.id, l.id, ?, ?, ? 
            from difficulty d, level l where d.name=? and d.length=? and l.name=?"""
    private static def wavedataSqlUpdate= """update wave_data set value= value + ? where stat=? and category=? and wave=? and difficulty_id=(select id from 
            difficulty where name=? and length=?) and level_id=(select id from level where name=?)"""
    public static def waveCountCategory= "frequency"
    public static def waveReached= "Played"
    
    private final def sql
    
    public DataWriter(Connection conn) {
        this.sql= new Sql(conn)
    }
    
    public synchronized void writeSteamInfo(String steamID64, String name, String avatar) {
        sql.withTransaction {
            sql.execute("insert or ignore into record (steamid64) values (?);", [steamID64])
            sql.execute("insert or ignore into steam_info (record_id) select r.id from record r where steamid64=?", [steamID64])
            sql.execute("update steam_info set name=?, avatar=? where record_id=(select id from record where steamid64=?)", [name, avatar, steamID64])
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
                sql.execute("insert or ignore into level (name) values(?)", [packet.getLevel()])
                sql.execute("update level set wins= wins + ?, losses= losses + ?, time= time + ? where name=?", 
                    [wins, losses, attrs.duration, packet.getLevel()])
                sql.execute("insert or ignore into level_difficulty_join (difficulty_id, level_id) select d.id, l.id from difficulty d, level l where d.name=? and d.length=? and l.name=?",
                    [packet.getDifficulty(), packet.getLength(), packet.getLevel()])
                sql.execute("""update level_difficulty_join set wins= wins + ?, losses= losses + ?, waveaccum= waveaccum + ?, time= time + ? where 
                    difficulty_id=(select id from  difficulty where name=? and length=?) and level_id=(select id from level where name=?)""",
                    [wins, losses, packet.getWave(), attrs.duration, packet.getDifficulty(), packet.getLength(), packet.getLevel()])
                (1 .. packet.getWave()).each {waveNum ->
                    sql.execute(wavedataSqlInsert, [waveNum, waveCountCategory, waveReached, packet.getDifficulty(), packet.getLength(), packet.getLevel()])
                    sql.execute(wavedataSqlUpdate, [1, waveReached, waveCountCategory, waveNum, packet.getDifficulty(), packet.getLength(), packet.getLevel()])
                }
            }
        } else {
            sql.withTransaction {
                sql.execute("insert or ignore into difficulty (name, length) values(?, ?)", [packet.getDifficulty(), packet.getLength()])
                sql.execute("insert or ignore into level (name) values(?)", [packet.getLevel()])
                packet.getStats().each {stat, value ->
                    sql.execute(wavedataSqlInsert, [packet.getWave(), category, stat, packet.getDifficulty(), packet.getLength(), packet.getLevel()])
                    sql.execute(wavedataSqlUpdate, [value, stat, category, packet.getWave(), packet.getDifficulty(), packet.getLength(), packet.getLevel()])
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
                        finales_survived= finales_survived + ?, finales_played= finales_played + ?, time_connected= time_connected + ? where steamid64=?""", 
                        [attrs.result == PacketParser.Result.WIN ? 1 : 0, attrs.result == PacketParser.Result.LOSS ? 1 : 0, 
                        attrs.result == PacketParser.Result.DISCONNECT ? 1 : 0, attrs.finalWaveSurvived, attrs.finalWave, attrs.duration, steamID64])
                    sql.execute("""insert into match_history (record_id, level_id, difficulty_id, result, wave, duration) select r.id,l.id,d.id,?,?,? from record r, 
                            difficulty d, level l where l.name=? and r.steamid64=? and d.name=? and d.length=?""",
                        [attrs.result.toString().toLowerCase(), attrs.wave, attrs.duration, attrs.level, steamID64, attrs.difficulty, attrs.length])
                }
            }
        }
    }
}


