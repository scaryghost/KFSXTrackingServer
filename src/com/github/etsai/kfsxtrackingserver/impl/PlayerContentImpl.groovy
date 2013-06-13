/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.PlayerContent
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import com.github.etsai.kfsxtrackingserver.PlayerContent.InvalidPacketIDException

/**
 *
 * @author etsai
 */
public class PlayerContentImpl implements PlayerContent {
    private def packets, steamID64
    
    @Override
    public List<PlayerPacket> getPackets() {
        return Collections.unmodifiableList(packets)
    }
    @Override
    public String getSteamID64() {
        return steamID64
    }
    @Override
    public boolean isCompleted() {
        return packets.last().isClose() && packets.inject(true) {acc, val -> acc && (val != null) }
    }
    
    @Override
    public void addPacket(PlayerPacket packet) throws InvalidPacketIDException {
        if (packets == null) {
            packets= []
            steamID64= packet.getSteamID64()
        } else {
            if (steamID64 != packet.getSteamID64()) {
                throw new InvalidPacketIDException("Player content for $steamID64.  Received packet for ${packet.getSteamID64()}")
            }
            packets[packet.getSeqNo()]= packet
        }
    }
}

