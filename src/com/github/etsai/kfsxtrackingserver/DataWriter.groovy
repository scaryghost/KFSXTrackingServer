/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.kfsxtrackingserver.impl.MatchPacket
import com.github.etsai.kfsxtrackingserver.impl.PlayerPacket
import com.github.etsai.kfsxtrackingserver.impl.PlayerPacket.MatchInfo
import groovy.sql.Sql;

/**
 *
 * @author etsai
 */
public class DataWriter {
    private final def sql
    
    public DataWriter(Sql sql) {
        this.sql= sql
    }
    
    public synchronized void writeSteamInfo(String steamID64, String name, String avatar) {
        sql.withTransaction {
            sql.execute("insert or ignore into steaminfo values (?, ?, ?)", [steamID64, "null", "null"])
            sql.execute("update steaminfo set name=?, avatar=? where steamid64=?", [name, avatar, steamID64])
        }
    }
    public synchronized void writeMatchData(MatchPacket packet) {
        def result= packet.getResult()
        def wins= (result == MatchPacket.Result.WIN) ? 1 : 0
        def losses= (result == MatchPacket.Result.LOSS) ? 1 : 0

        Common.logger.finer("Match data: $packet")
        sql.withTransaction {
            packet.getStats().each {stat, value ->
                sql.execute("insert or ignore into deaths (name) values (?);", [stat])
                sql.execute("update deaths set count= count + ? where name=?", [value, stat])
            }
            sql.execute("insert or ignore into difficulties (name, length) values(?, ?)", 
                [packet.getDifficulty(), packet.getLength()])
            sql.execute("""update difficulties set wins= wins + ?, losses= losses + ?, wave= wave + ?, 
                time= time + ? where name= ? and length= ?""", 
                [wins, losses, packet.getWave(), packet.getElapsedTime(), packet.getDifficulty(), packet.getLength()])
            sql.execute("insert or ignore into levels (name) values(?)", [packet.getLevelName()])
            sql.execute("""update levels set wins= wins + ?, losses= losses + ?, 
                time= time + ? where name=?""", 
                [wins, losses, packet.getElapsedTime(), packet.getLevelName()])
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
                                sql.execute("insert or ignore into player (steamid64, stat, category) values(?, ?, ?)", 
                                    [steamID64, stat, category])
                                sql.execute("update player set value=value + ? where steamid64=? and stat=? and category=?", 
                                    [value, steamID64, stat, category])
                            }
                        }
                    }
                } else {
                    def matchInfo= packet.getMatchInfo()

                    sql.execute("insert or ignore into records (steamid64) values (?);", [steamID64])
                    sql.execute("update records set wins= wins + ?, losses= losses + ?, disconnects= disconnects + ? where steamid64=?", 
                        [matchInfo.result == MatchInfo.Result.WIN ? 1 : 0, matchInfo.result == MatchInfo.Result.LOSS ? 1 : 0, 
                        matchInfo.result == MatchInfo.Result.DISCONNECT ? 1 : 0, steamID64])
                    sql.execute("insert into sessions (steamid64, level, difficulty, length, result, wave) values (?,?,?,?,?,?)",
                        [steamID64, matchInfo.level, matchInfo.difficulty, matchInfo.length, matchInfo.result.toString().toLowerCase(), matchInfo.wave])
                }
            }
        }
    }
}


