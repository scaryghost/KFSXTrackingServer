/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import com.github.etsai.kfsxtrackingserver.SteamPoller.InvalidSteamIDException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.TimerTask
import java.util.Timer

/**
 * Accumulates and holds packets for processing
 * @author etsai
 */
public class Accumulator {
    private final def receivedPackets, writer, statMsgTTL, packetParser, tasks
    private def timer
    
    public Accumulator(DataWriter writer, String password, long statMsgTTL) {
        this.receivedPackets= new ConcurrentHashMap()
        this.writer= writer
        this.statMsgTTL= statMsgTTL
        this.packetParser= new PacketParser(password)
        this.timer= new Timer()
        tasks= [:]
    }
    
    public def accumulate(String data) {
        def id
        
        try {
            def packet= packetParser.parse(data);
            if (packet instanceof MatchPacket) {
                writer.writeMatchData((MatchPacket)packet)
            } else if (packet instanceof PlayerPacket) {
                def playerPacket= (PlayerPacket)packet, storedPackets

                id= playerPacket.getSteamID64()
                if (receivedPackets[id] == null) {
                    receivedPackets[id]= []
                    tasks[id]= new PacketCleaner(steamID64: id, receivedPackets: receivedPackets)
                    timer.schedule(tasks[id], statMsgTTL)
                }
                storedPackets= receivedPackets[id]
                storedPackets[playerPacket.getSeqNo()]= playerPacket
                
                def completed= storedPackets.last().isClose() && 
                    storedPackets.inject(true) {acc, val -> acc && (val != null) }
                if (completed) {
                    receivedPackets.remove(id)
                    tasks[id].cancel()
                    tasks.remove(id)
                    Common.logger.info("Saving packets for steamID64: $id")
                    try {
                        def info= SteamPoller.poll(id)
                        writer.writeSteamInfo(id, info[0], info[1])
                        writer.writePlayerData(storedPackets.reverse())
                    } catch (IOException ex) {
                        writer.writePlayerData(storedPackets.reverse())
                    } catch (InvalidSteamIDException ex) {
                        Common.logger.log(Level.SEVERE, "Invalid steamID64: $id", ex)
                    } catch (Exception ex) {
                        Common.logger.log(Level.SEVERE, "Error saving player statistics", ex)
                    }
                }
            }
        } catch (IllegalStateException ex) {
            timer= new Timer()
            tasks[id]= new PacketCleaner(steamID64: id, receivedPackets: receivedPackets)
            timer.schedule(tasks[id], statMsgTTL)
        } catch (InvalidPacketFormatException ex) {
            Common.logger.log(Level.SEVERE, "Error parsing the packet", ex)
        } catch (Exception ex) {
            Common.logger.log(Level.SEVERE, null, ex)
        }
    }
    
    static class PacketCleaner extends TimerTask {
        public def steamID64, receivedPackets
        
        @Override
        public void run() {
            Common.logger.info("Discarding packets for steamID64: ${steamID64}")
            receivedPackets.remove(steamID64)
        }
    }
}

