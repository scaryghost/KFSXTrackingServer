/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.kfsxtrackingserver.DataWriter
import com.github.etsai.kfsxtrackingserver.PacketParser.Result
import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket
import static com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket.*
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import com.github.etsai.kfsxtrackingserver.PlayerContent
import groovy.sql.Sql
import java.sql.Connection
import com.github.etsai.kfsxtrackingserver.DataWriter.SteamInfo

/**
 *
 * @author etsai
 */
public class SQLiteWriter implements DataWriter {
    private static def matchState= [:]
    private static def wavedataSqlInsert= """insert or ignore into wave_data (difficulty_id, level_id, wave, category, stat) select d.id, l.id, ?, ?, ? 
            from difficulty d, level l where d.name=? and d.length=? and l.name=?"""
    private static def wavedataSqlUpdate= """update wave_data set value= value + ? where stat=? and category=? and wave=? and difficulty_id=(select id from 
            difficulty where name=? and length=?) and level_id=(select id from level where name=?)"""
    public static def waveCompletedCategory= "completed", perksCategory= "perks"
    
    private final def sql
    
    public SQLiteWriter(Connection conn) {
        this.sql= new Sql(conn)
    }
    
    public List<String> getMissingSteamInfoIDs() {
        def steamID64s= []
        sql.eachRow("select steamid64 from record where id in (SELECT id from record except select record_id from steam_info)") {
            steamID64s << it.steamid64
        }
        return steamID64s
    }
    public void writeSteamInfo(Collection<SteamInfo> steamInfo) {
        sql.withTransaction {
            steamInfo.each {info ->
                sql.execute("insert or ignore into record (steamid64) values (?);", [info.steamID64])
                sql.execute("insert or ignore into steam_info (record_id) select r.id from record r where steamid64=?", [info.steamID64])
                sql.execute("update steam_info set name=?, avatar=? where record_id=(select id from record where steamid64=?)", 
                    [info.name, info.avatar, info.steamID64])
            }
        }
    }
    public void writeSteamInfo(SteamInfo steamInfo) {
        writeSteamInfo([steamInfo])
    }
    public void writeMatchData(MatchPacket packet) {
        Common.logger.finer("Match data= $packet")
        def category= packet.getCategory()
        def key= packet.getServerAddressPort()
        def state= matchState[key]
        
        switch (category) {
            case "info":
                matchState[key]= packet.getAttributes()
                break
            case "result":
                state.putAll(packet.getAttributes())
                def wins= (state.result == Result.WIN) ? 1 : 0
                def losses= (state.result == Result.LOSS || state.result == Result.MID_GAME_VOTE) ? 1 : 0

                sql.withTransaction {
                    sql.execute("insert or ignore into difficulty (name, length) values(?, ?)", 
                        [state[ATTR_DIFFICULTY], state[ATTR_LENGTH]])
                    sql.execute("update difficulty set wins= wins + ?, losses= losses + ?, wave_sum= wave_sum + ?, time= time + ? where name= ? and length= ?", 
                        [wins, losses, packet.getWave(), state.duration, state[ATTR_DIFFICULTY], state[ATTR_LENGTH]])
                    sql.execute("insert or ignore into level (name) values(?)", [state[ATTR_MAP]])
                    sql.execute("update level set wins= wins + ?, losses= losses + ?, time= time + ? where name=?", 
                        [wins, losses, state.duration, state[ATTR_MAP]])
                    sql.execute("insert or ignore into level_difficulty_join (difficulty_id, level_id) select d.id, l.id from difficulty d, level l where d.name=? and d.length=? and l.name=?",
                        [state[ATTR_DIFFICULTY], state[ATTR_LENGTH], state[ATTR_MAP]])
                    sql.execute("""update level_difficulty_join set wins= wins + ?, losses= losses + ?, wave_sum= wave_sum + ?, time= time + ? where 
                        difficulty_id=(select id from  difficulty where name=? and length=?) and level_id=(select id from level where name=?)""",
                        [wins, losses, packet.getWave(), state.duration, state[ATTR_DIFFICULTY], state[ATTR_LENGTH], state[ATTR_MAP]])
                }
                break
            case "wave":
                def attrs= packet.getAttributes()
                switch (attrs.type) {
                    case "summary":
                        sql.withTransaction {
                            if (attrs.completed) {
                                def stat= "${state[ATTR_DIFFICULTY]}, ${state[ATTR_LENGTH]}"
                                sql.execute(wavedataSqlInsert, [packet.getWave(), waveCompletedCategory, stat, state[ATTR_DIFFICULTY], state[ATTR_LENGTH], state[ATTR_MAP]])
                                sql.execute(wavedataSqlUpdate, [1, stat, waveCompletedCategory, packet.getWave(), state[ATTR_DIFFICULTY], state[ATTR_LENGTH], state[ATTR_MAP]])
                            }
                            packet.getStats().each {stat, value ->
                                sql.execute(wavedataSqlInsert, [packet.getWave(), perksCategory, stat, state[ATTR_DIFFICULTY], state[ATTR_LENGTH], state[ATTR_MAP]])
                                sql.execute(wavedataSqlUpdate, [value, stat, perksCategory, packet.getWave(), state[ATTR_DIFFICULTY], state[ATTR_LENGTH], state[ATTR_MAP]])
                            }
                        }
                        break
                    default:
                        sql.withTransaction {
                            sql.execute("insert or ignore into difficulty (name, length) values(?, ?)", [state[ATTR_DIFFICULTY], state[ATTR_LENGTH]])
                            sql.execute("insert or ignore into level (name) values(?)", [state[ATTR_MAP]])
                            packet.getStats().each {stat, value ->
                                sql.execute(wavedataSqlInsert, [packet.getWave(), attrs.type, stat, state[ATTR_DIFFICULTY], state[ATTR_LENGTH], state[ATTR_MAP]])
                                sql.execute(wavedataSqlUpdate, [value, stat, attrs.type, packet.getWave(), state[ATTR_DIFFICULTY], state[ATTR_LENGTH], state[ATTR_MAP]])
                            }
                        }
                        break
                }
                break
        }
    }
    
    public void writePlayerData(PlayerContent content) {
        def key= content.getServerAddressPort()
        def state= matchState[key]
        
        sql.withTransaction {
            def steamID64= content.getSteamID64()
            
            def matchInfo= content.getMatchInfo()
            Common.logger.finer("Match data= $matchInfo")
            sql.execute("insert or ignore into level (name) values(?)", [state[ATTR_MAP]])
            sql.execute("insert or ignore into record (steamid64) values (?);", [steamID64])
            sql.execute("""update record set wins= wins + ?, losses= losses + ?, disconnects= disconnects + ?, 
                finales_survived= finales_survived + ?, finales_played= finales_played + ?, time= time + ? where steamid64=?""", 
                [!matchInfo.disconnected && state.result == Result.WIN ? 1 : 0, !matchInfo.disconnected && (state.result == Result.LOSS || state.result == Result.MID_GAME_VOTE) ? 1 : 0, 
                matchInfo.disconnected ? 1 : 0, matchInfo.finalWaveSurvived, matchInfo.finalWave, matchInfo.duration, steamID64])
            sql.execute("""insert into match_history (record_id, level_id, difficulty_id, result, wave, duration) select r.id,l.id,d.id,?,?,? from record r, 
                    difficulty d, level l where l.name=? and r.steamid64=? and d.name=? and d.length=?""",
                [matchInfo.disconnected ? "disconnected" : state.result.toString().toLowerCase(), matchInfo.wave, matchInfo.duration, state[ATTR_MAP], steamID64, state[ATTR_DIFFICULTY], state[ATTR_LENGTH]])
            
            content.getPackets().each {packet ->
                Common.logger.finer("Player data= $packet")
                def category= packet.getCategory()
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
            }
        }
    }
}


