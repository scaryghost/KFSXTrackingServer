/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.impl.MatchPacket;
import com.github.etsai.kfsxtrackingserver.impl.PlayerPacket;
import java.util.Map;

/**
 * Interprets the messages received from the mutator
 * @author etsai
 */
public abstract class Packet {
    public static String password
        
    public enum Type {
        MATCH, PLAYER
    }
    
    protected def msgType
    protected def seqNo
    protected def close
    protected def stats
    
    public static Packet parse(String msg) {
        def parts= msg.split("\\|")
        def packetInfo= parts[0].split(",")
        def packet
        
        if (packetInfo[2] != password) {
            throw new RuntimeException("Invalid password given, ignoring packet: ${msg}")
        }
        switch (packetInfo[0]) {
            case PlayerPacket.PROTOCOL:
                if (packetInfo[1].toInteger() != PlayerPacket.VERSION)
                    throw new RuntimeException("Player protocol is incorrect version.  Received ${packetInfo[1]}, expecting ${PlayerPacket.VERSION}")
                packet= new PlayerPacket(parts)
                packet.msgType= Type.PLAYER
                packet.seqNo= parts[2].toInteger()
                break;
            case MatchPacket.PROTOCOL:
                if (packetInfo[1].toInteger() != MatchPacket.VERSION)
                    throw new RuntimeException("Match protocol is incorrect version.  Received ${packetInfo[1]}, expecting ${MatchPacket.VERSION}")
                packet= new MatchPacket(parts)
                packet.msgType= Type.MATCH
                packet.seqNo= 0
                break;
            default:
                throw new RuntimeException("Unrecognized message type: ${packetInfo[0]}")
                    
        }
        packet.close= parts.last() == "_close"
        return packet
    }
    
    public Packet(def statStr) {
        stats= [:]
        statStr.tokenize(",").each {keyval ->
            def keyvalSplit= keyval.tokenize("=")
            stats[keyvalSplit[0]]= keyvalSplit[1].toInteger()
        }
    }
    
    public Type getType() {
        return msgType
    }
    
    public int getSeqNo() {
        return seqNo
    }
    public boolean isClose() {
        return close
    }
    
    public Map<String, Integer> getStats() {
        return stats
    }
}

