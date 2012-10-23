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
    private static final def receivedPackets= [:]
    
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
    private final def writer
    public Accumulator(String data, DataWriter writer) {
        this.data= data
        this.writer= writer
    }
    
    @Override
    public void run() {
        try {
            logger.finest(data);
            def packet= Packet.parse(data);
            switch(packet.getType()) {
                case Type.Match:
                    writer.writeMatchData(packet)
                    break
                case Type.Player:
                    def id= packet.getData(PlayerPacket.keyPlayerId)
                    def category= packet.getData(PlayerPacket.keyCategory)

                    if (id == PlayerPacket.blankID) {
                        if (category != "match") {
                            logger.info("Blank ID received.  Adding to aggregate stats only")
                            packet.getData(PlayerPacket.keyStats).each {stat, value ->
                                if (stat != "")
                                    statsData.accumulateAggregateStat(stat, value, category)
                            }
                        }
                    } else {
                        def seqnum= packet.getSeqnum()
                        def packets
                        
                        synchronized(receivedPackets) {
                            if (receivedPackets[id] == null) {
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
                                receivedPackets.remove(id)
                                writer.writePlayerData(packets)
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
}

