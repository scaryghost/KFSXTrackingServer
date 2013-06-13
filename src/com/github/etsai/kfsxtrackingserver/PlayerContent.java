/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket;
import java.util.List;

/**
 *
 * @author etsai
 */
public interface PlayerContent {
    public class InvalidPacketIDException extends Exception {
        public InvalidPacketIDException(String msg) {
            super(msg);
        }
    }
    public List<PlayerPacket> getPackets();
    public String getSteamID64();
    public boolean isCompleted();
    
    public void addPacket(PlayerPacket packet) throws InvalidPacketIDException;
    
}
