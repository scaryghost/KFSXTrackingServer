/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.PlayerContent
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import com.github.etsai.kfsxtrackingserver.PlayerContent.InvalidPacketIDException
import com.github.etsai.kfsxtrackingserver.PlayerContent.Match

/**
 *
 * @author etsai
 */
public class PlayerContentImpl implements PlayerContent {
    private def packets, steamID64, matchInfo, maxSeqNo
    
    @Override
    public Collection<PlayerPacket> getPackets() {
        return Collections.unmodifiableList(packets)
    }
    @Override
    public String getSteamID64() {
        return steamID64
    }
    @Override
    public boolean isCompleted() {
        if (packets == null || packets.isEmpty()) {
            return false
        }
        return (matchInfo != null) && (packets.size() == maxSeqNo) && packets.inject(true) {acc, val -> acc && (val != null) }
    }
    @Override
    public Match getMatchInfo() {
        return matchInfo
    }
    
    @Override
    public void addPacket(PlayerPacket packet) throws InvalidPacketIDException {
        if (packets == null) {
            packets= []
            steamID64= packet.getSteamID64()
            maxSeqNo= packet.getSeqNo()
        } else {
            if (steamID64 != packet.getSteamID64()) {
                throw new InvalidPacketIDException("Player content for $steamID64.  Received packet for ${packet.getSteamID64()}")
            } else if (packets[packet.getSeqNo()] != null || (packet.isClose() && matchInfo != null)) {
                throw new InvalidPacketIDException("Player content already received packet for seqNo: ${packet.getSeqNo()}")
            }
            maxSeqNo= packet.getSeqNo() > maxSeqNo ? packet.getSeqNo() : maxSeqNo
        }
        if (packet.getCategory() == PlayerPacketImpl.matchCategory){
            matchInfo= new Match(packet.getAttributes())
        } else {
            packets[packet.getSeqNo()]= packet
        }
    }
}

