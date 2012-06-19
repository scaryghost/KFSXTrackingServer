/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Packet
import static com.github.etsai.kfsxtrackingserver.Packet.Type.Player

/**
 *
 * @author eric
 */
public class PlayerPacket extends Packet {
    public static final keyPlayerId= "playerid"
    public static final keyGroup= "group"
    public static final keyStats= "stats"
    
    private def seqnum
    private def last
    
    public PlayerPacket(String protocol, int version, String[] parts) {
        super(protocol, version)
        
        try {
            def playerStats= new Properties()
            def body
            
            data= [:]
            seqnum= Integer.valueOf(parts[2])
            last= parts[parts.length-1].equals("_close")
            
            data[keyPlayerId]= parts[1]
            data[keyGroup]= parts[3]
            
            if (seqnum == 4) {
                def items= ["map=${parts[4]}", "difficulty=${parts[5]}", "length=${parts[6]}", 
                    "result=${parts[7]}", "wave=${parts[8]}"]
                body= items.join("\n")
            } else {
                body= parts[4].replace(',','\n')
            }
            playerStats.load(new StringReader(body))
            data[keyStats]= playerStats
            
            valid= true
        } catch (Exception e) {
            valid= false
        }
    }
    
    public Packet.Type getType() {
        return Player
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

