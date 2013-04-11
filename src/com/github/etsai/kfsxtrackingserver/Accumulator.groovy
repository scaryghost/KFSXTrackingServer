/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.kfsxtrackingserver.impl.MatchPacket
import com.github.etsai.kfsxtrackingserver.impl.PlayerPacket
import java.util.logging.Level
import java.util.TimerTask
import java.util.Timer

/**
 * Accumulates and holds packets for processing
 * @author etsai
 */
public class Accumulator {
    private static final def receivedPackets= [:]
    private static def timer= new Timer()
    public static def writer
    public static Long statMsgTTL
    
    public synchronized static def accumulate(String data) {
        def id
        
        try {
            Common.logger.finest(data);
            def packet= Packet.parse(data);
            if (packet instanceof MatchPacket) {
                writer.writeMatchData((MatchPacket)packet)
            } else if (packet instanceof PlayerPacket) {
                def playerPacket= (PlayerPacket)packet
                id= playerPacket.getSteamID64()
                def category= playerPacket.getCategory()

                if (id == null) {
                    if (category != "match") {
                        Common.logger.info("Blank ID received.  Adding to aggregate stats only")
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

                    packets[seqNo]= playerPacket
                    def completed= packets.last().isClose() && 
                        packets.inject(true) {acc, val -> acc && (val != null) }
                    if (completed) {
                        receivedPackets.remove(id)
                        Common.logger.info("Saving packets for steamID64: $id")
                        try {
                            def info= SteamPoller.poll(id)
                            writer.writeSteamInfo(id, info[0], infio[1])
                            writer.writePlayerData(packets)
                        } catch (IOException ex) {
                            writer.writePlayerData(packets)
                        } catch (Exception ex) {
                            Common.logger.log(Level.SEVERE, "Invalid steamID64: $id", ex)
                        } 
                    }
                }
            }
        } catch (IllegalStateException ex) {
            timer= new Timer()
            timer.schedule(new PacketCleaner(steamID64: id), statMsgTTL)
        } catch (Exception ex) {
            Common.logger.log(Level.SEVERE, null, ex)
        }
    }
    
    static class PacketCleaner extends TimerTask {
        public def steamID64
        
        @Override
        public void run() {
            synchronized(receivedPackets) {
                if (receivedPackets[steamID64] != null) {
                    Common.logger.info("Discarding packets for steamID64: ${steamID64}")
                    receivedPackets.remove(steamID64)
                }
            }
        }
    }
}

