/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import static com.github.etsai.kfsxtrackingserver.Common.logger
import static com.github.etsai.kfsxtrackingserver.Packet.Type
import com.github.etsai.kfsxtrackingserver.Packet
import java.util.logging.Level

/**
 *
 * @author eric
 */
public class PlayerPacket extends Packet {
    public static final def blankID= ""
    public static final def packetVersion= 1
    public static final String keyPlayerId= "playerid"
    public static final String keyCategory= "category"
    public static final String keyStats= "stats"
    
    private def seqnum
    private def last
    
    public PlayerPacket(String protocol, int version, String[] parts) {
        super(protocol, version)
        
        if (version != packetVersion) {
            def msg= "Packet version required: ${packetVersion}.  Version received: ${version}"
            throw new RuntimeException(msg)
        }
        def playerStats= [:]
        def body
        def id

        data= [:]
        seqnum= Integer.valueOf(parts[2])
        last= parts[parts.length-1].equals("_close")

        if (parts[1].length() < 17) {
            def offset= new BigInteger("76561197960265728")
            if (parts[1] == "") {
                id= blankID
            } else {
                def linuxId= new BigInteger(parts[1])

                linuxId+= offset
                id= linuxId.toString()
            }
        } else {
            id= parts[1]
        }

        data[keyPlayerId]= id
        data[keyCategory]= parts[3]
        if (data[keyCategory] == "match") {
            def items= ["map=${parts[4]}", "difficulty=${parts[5]}", "length=${parts[6]}", 
                "result=${parts[7]}", "wave=${parts[8]}"]
            body= items.join(",")
        } else {
            body= parts.size() > 4 ? parts[4] : ""
        }
        body.tokenize(",").each {stats ->
            def keyVal= stats.split("=")
            def val= data[keyCategory] == "match" ? keyVal[1] : keyVal[1].toInteger()
            playerStats[keyVal[0]]= val
        }
        data[keyStats]= playerStats
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
}

