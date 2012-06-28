/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Packet
import com.github.etsai.kfsxtrackingserver.Packet.Type

/**
 *
 * @author eric
 */
public class PlayerPacket extends Packet {
    public static final String keyPlayerId= "playerid"
    public static final String keyGroup= "group"
    public static final String keyStats= "stats"
    
    private def seqnum
    private def last
    
    public PlayerPacket(String protocol, int version, String[] parts) {
        super(protocol, version)
        
        try {
            def playerStats= [:]
            def body
            
            data= [:]
            seqnum= Integer.valueOf(parts[2])
            last= parts[parts.length-1].equals("_close")
            
            data[keyPlayerId]= parts[1]
            data[keyGroup]= parts[3]
            
            if (data[keyGroup] == "match") {
                def items= ["map=${parts[4]}", "difficulty=${parts[5]}", "length=${parts[6]}", 
                    "result=${parts[7]}", "wave=${parts[8]}"]
                body= items.join(",")
            } else {
                body= parts[4]
            }
            body.split(",").each {stats ->
                def keyVal= stats.split("=")
                playerStats[keyVal[0]]= keyVal[1]
            }
            data[keyStats]= playerStats
            valid= true
        } catch (Exception e) {
            valid= false
        }
    }
    
    public Type getType() {
        return Type.Player
    }
    public int getSeqnum() {
        return seqnum
    }
    public boolean isLast() {
        return last
    }
    public boolean isValid() {
        return valid
    }
	
}

