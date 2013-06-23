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
import com.github.etsai.kfsxtrackingserver.impl.PlayerContentImpl
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.TimerTask
import java.util.Timer
import com.github.etsai.kfsxtrackingserver.DataWriter.SteamInfo

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
        def steamID64
        
        try {
            def packet= packetParser.parse(data);
            if (packet instanceof MatchPacket) {
                writer.writeMatchData((MatchPacket)packet)
            } else if (packet instanceof PlayerPacket) {
                def playerPacket= (PlayerPacket)packet, content

                steamID64= playerPacket.getSteamID64()
                if (receivedPackets[steamID64] == null) {
                    receivedPackets[steamID64]= new PlayerContentImpl()
                    tasks[steamID64]= new TimerTask() {
                        @Override public void run() {
                            Common.logger.info("Discarding packets for steamID64: $steamID64")
                            receivedPackets.remove(steamID64)
                        }
                    }
                    timer.schedule(tasks[steamID64], statMsgTTL)
                }
                content= receivedPackets[steamID64]
                content.addPacket(playerPacket)
                
                if (content.isCompleted()) {
                    receivedPackets.remove(steamID64)
                    tasks[steamID64].cancel()
                    tasks.remove(steamID64)
                    Common.logger.info("Saving packets for steamID64: $steamID64")
                    try {
                        def info= SteamPoller.poll(steamID64)
                        writer.writeSteamInfo(new SteamInfo(steamID64: steamID64, name: info[0], avatar: info[1]))
                        writer.writePlayerData(content)
                    } catch (IOException ex) {
                        Common.logger.log(Level.WARNING, "Error contacting steam community.  Saving player statistics", ex)
                        writer.writePlayerData(content)
                    } catch (InvalidSteamIDException ex) {
                        Common.logger.log(Level.SEVERE, "Invalid steamID64: $steamID64", ex)
                    } catch (Exception ex) {
                        Common.logger.log(Level.SEVERE, "Error saving player statistics", ex)
                    }
                }
            }
        } catch (IllegalStateException ex) {
            timer= new Timer()
            tasks[steamID64]= new TimerTask() {
                @Override public void run() {
                    Common.logger.info("Discarding packets for steamID64: $steamID64")
                    receivedPackets.remove(steamID64)
                }
            }
            timer.schedule(tasks[steamID64], statMsgTTL)
        } catch (InvalidPacketFormatException ex) {
            Common.logger.log(Level.SEVERE, "Error parsing the packet", ex)
        } catch (Exception ex) {
            Common.logger.log(Level.SEVERE, null, ex)
        }
    }
}    
