/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import static com.github.etsai.kfsxtrackingserver.Common.*;
import com.github.etsai.kfsxtrackingserver.impl.MatchPacket
import com.github.etsai.kfsxtrackingserver.impl.PlayerPacket
import groovy.sql.Sql;

/**
 *
 * @author etsai
 */
public class DataWriter {
    public synchronized void writeMatchData(MatchPacket packet) {
        def levelName= packet.getData(MatchPacket.keyMap)
        def time= packet.getData(MatchPacket.keyTime)
        def diff= packet.getData(MatchPacket.keyDifficulty)
        def length= packet.getData(MatchPacket.keyLength)
        def wave= packet.getData(MatchPacket.keyWave)
        def result= packet.getData(MatchPacket.keyResult)
        def wins= (result == 2) ? 1 : 0
        def losses= (result != 2) ? 1 : 0

        logger.finer("Match data: [${levelName}, ${diff}, ${length}, ${wave}, ${result}, ${time}]")
        
        sql.withTransaction {
            sql.withBatch {stmt ->
                packet.getData(MatchPacket.keyDeaths).each {stat, value ->
                    stmt.addBatch("insert or ignore into deaths values ('$stat', 0);")
                    stmt.addBatch("update deaths set count= count + $value where name='$stat'")
                }
                
                stmt.addBatch("insert or ignore into difficulties values('$diff', '$length', 0, 0, 0, 0)")
                stmt.addBatch("""update difficulties set wins= wins + $wins, losses= losses + $losses, wave= wave + $wave, 
                    time= time + $time where name= '$diff' and length= '$length'""")
                stmt.addBatch("insert or ignore into levels values('$levelName', 0, 0, 0)")
                stmt.addBatch("""update levels set wins= wins + $wins, losses= losses + $losses, time= time + $time where name='$levelName'""")
            }
        }
    }
    
    public synchronized void writePlayerData(Iterable<PlayerPacket> packets) {
        def start= System.nanoTime()
        sql.withTransaction {
            sql.withBatch {stmt ->
                packets.each {packet ->
                    def category= packet.getData(PlayerPacket.keyCategory)
                    def steamID64= packet.getData(PlayerPacket.keyPlayerId)
                    if (category != "match") {
                        packet.getData(PlayerPacket.keyStats).each {stat, value ->
                            stmt.addBatch("insert or ignore into aggregate values ('$stat', '$category', 0);")
                            stmt.addBatch("update aggregate set value= value + $value where stat='$stat' and category='$category'")
                            stmt.addBatch("insert or ignore into player values('$steamID64', '$stat', '$category', 0)")
                            stmt.addBatch("update player set value=value + $value where stat='$stat' and category='$category'")
                        }
                    } else {
                        def result= packet.getData(PlayerPacket.keyStats)["result"].toInteger()
                        def map= packet.getData(PlayerPacket.keyStats)["map"]
                        def diff= packet.getData(PlayerPacket.keyStats)["difficulty"]
                        def length= packet.getData(PlayerPacket.keyStats)["length"]
                        def wave= packet.getData(PlayerPacket.keyStats)["wave"].toInteger()
                        def resultStr= ["disconnected", "lost", "won"]

                        stmt.addBatch("insert or ignore into records values ('$steamID64', 0, 0, 0);")
                        stmt.addBatch("update records set wins= wins + ${result == 2 ? 1 : 0}, losses= losses + ${result == 1 ? 1 : 0}, disconnects= disconnects + ${result == 0 ? 1 : 0} where steamid64='$steamID64'")
                        stmt.addBatch("insert into sessions values ('$steamID64', '$map', '$diff', '$length', '${resultStr[result]}', $wave, NULL);")
                    }
                }
            }
        }
        println System.nanoTime() - start
    }
}


