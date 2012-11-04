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
import java.util.logging.Level
import java.util.TimerTask
import java.util.Timer

/**
 * Accumulates and holds packets for processing
 * @author etsai
 */
public class Accumulator implements Runnable {
    private static final def receivedPackets= [:]
    private static def timer= new Timer()
    public static DataWriter writer
    public static Long statMsgTTL
    
    static class PacketCleaner extends TimerTask {
        public def steamID64
        
        @Override
        public void run() {
            synchronized(receivedPackets) {
                if (receivedPackets[steamID64] != null) {
                    logger.info("Discarding packets for steamID64: ${steamID64}")
                    receivedPackets.remove(steamID64)
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
        def id
        
        try {
            logger.finest(data);
            def packet= Packet.parse(data);
            if (packet instanceof MatchPacket) {
                writer.writeMatchData((MatchPacket)packet)
            } else if (packet instanceof PlayerPacket) {
                def playerPacket= (PlayerPacket)packet
                id= playerPacket.getSteamID64()
                def category= playerPacket.getCategory()

                if (id == "") {
                    if (category != "match") {
                        logger.info("Blank ID received.  Adding to aggregate stats only")
                        writer.writePlayerData([playerPacket])
                    }
                } else {
                    def seqNo= playerPacket.getSeqNo()
                    def packets

                    synchronized(receivedPackets) {
                        if (receivedPackets[id] == null) {
                            receivedPackets[id]= []
                            timer.schedule(new PacketCleaner(steamID64: id), statMsgTTL)
                        }
                        packets= receivedPackets[id]
                    }

                    synchronized(packets) {
                        packets[seqNo]= playerPacket
                        def completed= packets.last().isClose() && 
                            packets.inject(true) {acc, val -> acc && (val != null) }
                        if (completed) {
                            receivedPackets.remove(id)
                            writer.writePlayerData(packets)
                        }
                    }
                }
            }
        } catch (IllegalStateException ex) {
            timer= new Timer()
            timer.schedule(new PacketCleaner(steamID64: id), statMsgTTL)
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex)
        }
    }
}

