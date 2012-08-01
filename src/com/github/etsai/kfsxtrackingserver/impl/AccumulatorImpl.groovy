/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import static com.github.etsai.kfsxtrackingserver.Common.statsData
import static com.github.etsai.kfsxtrackingserver.Common.logger
import com.github.etsai.kfsxtrackingserver.Accumulator
import com.github.etsai.kfsxtrackingserver.Packet
import com.github.etsai.kfsxtrackingserver.Packet.Type
import java.util.logging.Level

/**
 *
 * @author etsai
 */
public class AccumulatorImpl implements Accumulator {
    private def receivedPackets= [:]
    private def packets= Collections.synchronizedList([]);
    
    public synchronized void add(String data) {
        try {
            Packet packet= Packet.parse(data);

            if (packet.isValid()) {
                packets.add(packet);
                notify();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error parsing message: ${data}", ex);
        }
    }
    
    @Override
    public synchronized void run() {
        while(true) {
                if (packets.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
                
                def packet= packets.remove(0);
                switch(packet.getType()) {
                    case Type.Match:
                        def levelName= packet.getData(MatchPacket.keyMap)
                        def time= packet.getData(MatchPacket.keyTime)
                        def map= packet.getData(MatchPacket.keyMap)
                        def diff= packet.getData(MatchPacket.keyDifficulty)
                        def length= packet.getData(MatchPacket.keyLength)
                        def wave= packet.getData(MatchPacket.keyWave).toInteger()
                        def result= packet.getData(MatchPacket.keyResult).toInteger()

                        statsData.accumulateLevel(levelName, result, time)
                        statsData.accumulateDifficulty(diff, length, result, wave, time)
                        packet.getData(MatchPacket.keyDeaths).each {stat, value ->
                            statsData.accumulateDeath(stat, value.toInteger())
                        }
                        break
                    case Type.Player:
                        def id= packet.getData(PlayerPacket.keyPlayerId)
                        def seqnum= packet.getSeqnum()

                        if (receivedPackets[id] == null) {
                            receivedPackets[id]= []
                        }
                        receivedPackets[id][seqnum]= packet
                        def completed= receivedPackets[id].last().isLast() && 
                            receivedPackets[id].inject(true) {acc, val -> acc && (val != null) }
                        if (completed) {
                            savePlayer(id)
                            receivedPackets[id]= null
                        }
                        break
                    default:
                        logger.info("Unrecognized packet type: ${packet.getType()}")
                        break
                }
            }
    }
    private void savePlayer(def steamid) {
        receivedPackets[steamid].each {packet ->
            def group= packet.getData(PlayerPacket.keyGroup)
            
            if (group == "match") {
                statsData.accumulateRecord(steamid, packet.getData(PlayerPacket.keyStats)["result"].toInteger())
            } else {
                packet.getData(PlayerPacket.keyStats).each {stat, value ->
                    statsData.accumulateAggregateStat(stat, value.toInteger(), group)
                }
            }
        }
    }
}

