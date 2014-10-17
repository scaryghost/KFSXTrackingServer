/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.DataWriter.SteamInfo;
import com.github.etsai.kfsxtrackingserver.PacketParser.*;
import com.github.etsai.kfsxtrackingserver.SteamPoller.InvalidSteamIDException;
import com.github.etsai.kfsxtrackingserver.impl.PlayerContentImpl;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.Timer;

/**
 * Accumulates and holds packets for processing
 * @author etsai
 */
public class Accumulator {
    private final ConcurrentHashMap<String, PlayerContent> incompletePlayerContent;
    private final DataWriter writer;
    private final long statMsgTTL;
    private final PacketParser packetParser;
    private final HashMap<String, TimerTask> tasks;
    private Timer timer;
    
    public Accumulator(DataWriter writer, String password, long statMsgTTL) {
        this.incompletePlayerContent= new ConcurrentHashMap<>();
        this.writer= writer;
        this.statMsgTTL= statMsgTTL;
        this.packetParser= new PacketParser(password);
        this.timer= new Timer();
        tasks= new HashMap<>();
    }
    
    public PlayerContent getPlayerContent(String steamID64) {
        return incompletePlayerContent.get(steamID64);
    }
    private void accumulateHelper(StatPacket packet) {
        try {
            if (packet instanceof MatchPacket) {
                writer.writeMatchData((MatchPacket)packet);
            } else if (packet instanceof PlayerPacket) {
                PlayerPacket playerPacket= (PlayerPacket)packet;
                PlayerContent content;

                final String steamID64= playerPacket.getSteamID64();
                if (!incompletePlayerContent.containsKey(steamID64)) {
                    incompletePlayerContent.put(steamID64, new PlayerContentImpl());
                    tasks.put(steamID64, new TimerTask() {
                        @Override public void run() {
                            Common.logger.log(Level.INFO, "Discarding packets for steamID64: {0}", steamID64);
                            incompletePlayerContent.remove(steamID64);
                        }
                    });
                    try {
                        timer.schedule(tasks.get(steamID64), statMsgTTL);
                    } catch (IllegalStateException ex) {
                        Common.logger.log(Level.WARNING, "Error scheduling task.  Reinitializing timer", ex);
                        timer= new Timer();
                        timer.schedule(tasks.get(steamID64), statMsgTTL);
                    }
                }
                content= incompletePlayerContent.get(steamID64);
                content.addPacket(playerPacket);

                if (content.isCompleted()) {
                    incompletePlayerContent.remove(steamID64);
                    tasks.remove(steamID64).cancel();
                    Common.logger.log(Level.INFO, "Saving packets for steamID64: {0}", steamID64);
                    try {
                        String[] info= SteamPoller.poll(steamID64);
                        writer.writeSteamInfo(new SteamInfo(steamID64, info[0], info[1]));
                        writer.writePlayerData(content);
                    } catch (IOException ex) {
                        Common.logger.log(Level.WARNING, "Error contacting steam community.  Saving player statistics", ex);
                        writer.writePlayerData(content);
                    } catch (InvalidSteamIDException ex) {
                        Common.logger.log(Level.SEVERE, "Invalid steamID64: " + steamID64, ex);
                    } catch (Exception ex) {
                        Common.logger.log(Level.SEVERE, "Error saving player statistics", ex);
                    }
                }
            }
        } catch (Exception ex) {
            Common.logger.log(Level.SEVERE, null, ex);
        }
        
    }
    public void accumulate(DatagramPacket udpPacket) {
        try {
            accumulateHelper(packetParser.parse(udpPacket));
        } catch (InvalidPacketFormatException ex) {
            Common.logger.log(Level.SEVERE, "Error parsing the packet", ex);
        }
    }
    public void accumulate(String data) {
        try {
            accumulateHelper(packetParser.parse(data));
        } catch (InvalidPacketFormatException ex) {
            Common.logger.log(Level.SEVERE, "Error parsing the packet", ex);
        }
    }
}    
