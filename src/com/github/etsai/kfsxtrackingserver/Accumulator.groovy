/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import static com.github.etsai.kfsxtrackingserver.Common.*
import static com.github.etsai.kfsxtrackingserver.ServerProperties.*
import static com.github.etsai.kfsxtrackingserver.Packet.Type
import com.github.etsai.kfsxtrackingserver.impl.MatchPacket
import com.github.etsai.kfsxtrackingserver.impl.PlayerPacket
import com.github.etsai.kfsxtrackingserver.web.SteamIdInfo.SteamPoller
import java.util.logging.Level
import java.util.TimerTask

/**
 * Accumulates and holds packets for processing
 * @author etsai
 */
public class Accumulator implements Runnable {
    private static def receivedPackets= [:]
    
    static class PacketCleaner extends TimerTask {
        public def steamID64
        
        @Override
        public void run() {
            synchronized(receivedPackets) {
                if (receivedPackets[steamID64] != null) {
                    logger.info("Discarding packets for steamID64: ${steamID64}")
                    receivedPackets[steamID64]= null
                }
            }
        }
    }
    
    private final def data
    public Accumulator(String data) {
        this.data= data
    }
    
    @Override
    public void run() {
        try {
            logger.finest(data);
            def packet= Packet.parse(data);
            switch(packet.getType()) {
                case Type.Match:
                    def levelName= packet.getData(MatchPacket.keyMap)
                    def time= packet.getData(MatchPacket.keyTime)
                    def diff= packet.getData(MatchPacket.keyDifficulty)
                    def length= packet.getData(MatchPacket.keyLength)
                    def wave= packet.getData(MatchPacket.keyWave).toInteger()
                    def result= packet.getData(MatchPacket.keyResult).toInteger()

                    logger.finer("Match data: ${levelName}, ${time}, ${diff}, ${length}, ${wave}, ${result}")
                    statsData.accumulateLevel(levelName, result, time)
                    statsData.accumulateDifficulty(diff, length, result, wave, time)
                    packet.getData(MatchPacket.keyDeaths).each {stat, value ->
                        statsData.accumulateDeath(stat, value.toInteger())
                    }
                    break
                case Type.Player:
                    def id= packet.getData(PlayerPacket.keyPlayerId)
                    def group= packet.getData(PlayerPacket.keyGroup)

                    if (id == PlayerPacket.blankID && group != "match") {
                        logger.info("Blank ID received.  Adding to aggregate stats only")
                        packet.getData(PlayerPacket.keyStats).each {stat, value ->
                            if (stat != "")
                                statsData.accumulateAggregateStat(stat, value, group)
                        }
                    } else {
                        def seqnum= packet.getSeqnum()
                        def packets
                        
                        synchronized(receivedPackets) {
                            if (receivedPackets[id] == null) {
                                println "Hello world!"
                                receivedPackets[id]= []
                                timer.schedule(new PacketCleaner(steamID64: id), 
                                        properties[propStatsMsgTTL].toLong())
                            }
                            packets= receivedPackets[id]
                        }
                        
                        synchronized(packets) {
                            packets[seqnum]= packet
                            def completed= packets.last().isLast() && 
                                packets.inject(true) {acc, val -> acc && (val != null) }
                            if (completed) {
                                savePackets(id, packets)
                                receivedPackets[id]= null
                            }
                        }
                    }
                    break
                default:
                    logger.info("Unrecognized packet type: ${packet.getType()}")
                    break
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex)
        }
    }
    
    private def savePackets(def steamID64, def packets) {
        logger.info("Saving stats for player: ${steamID64}")
        
        def poller= new SteamPoller(steamID64: steamID64)
        pool.submit(poller)
        packets.each {packet ->
            def group= packet.getData(PlayerPacket.keyGroup)

            if (group == "match") {
                statsData.accumulateRecord(steamID64, packet.getData(PlayerPacket.keyStats)["result"].toInteger())
            } else {
                packet.getData(PlayerPacket.keyStats).each {stat, value ->
                    if (stat != "")
                        statsData.accumulateAggregateStat(stat, value, group)
                    statsData.accumulatePlayerStat(steamID64, stat, value, group)
                }
            }
        }
    }
}

