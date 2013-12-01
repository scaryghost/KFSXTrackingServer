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
    private def packets, steamID64, matchInfo, maxSeqNo,
            serverAddress, serverPort
    
    @Override
    public String getServerAddress() {
        return serverAddress
    }
    @Override
    public int getServerPort() {
        return serverPort
    }
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
            serverAddress= packet.getSenderAddress()
            serverPort= packet.getSenderPort()
        } else {
            def senderAttrs= [packet.getSteamID64(), packet.getSenderAddress(), packet.getSenderPort()]
            if (steamID64 != senderAttrs[0] || serverAddress != senderAttrs[1] || serverPort != senderAttrs[2]) {
                throw new InvalidPacketIDException("Player content mismatch.  Expecting: ${[steamID64, serverAddress, serverPort]}, recieved: ${senderAttrs}")
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

