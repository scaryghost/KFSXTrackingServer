/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import static com.github.etsai.kfsxtrackingserver.Common.logger
import static com.github.etsai.kfsxtrackingserver.Packet.Type
import com.github.etsai.kfsxtrackingserver.Packet
import com.github.etsai.kfsxtrackingserver.Time
import java.util.logging.Level

/**
 *
 * @author eric
 */
public class MatchPacket extends Packet {
    public static final def packetVersion= 1
    public static final def keyMap= "map"
    public static final def keyDifficulty= "difficulty"
    public static final def keyLength= "length"
    public static final def keyTime= "time"
    public static final def keyResult= "result"
    public static final def keyWave= "wave"
    public static final def keyDeaths= "deaths"

    public MatchPacket(String protocol, int version, String[] parts) {
        super(protocol, version)
        
        if (version != packetVersion) {
            def msg= "Packet version required: ${packetVersion}.  Version received: ${version}"
            throw new RuntimeException(msg)
        }
        
        def deaths= [:]
        parts[7].tokenize(",").each {death ->
            def keyVal= death.split("=")
            deaths[keyVal[0]]= keyVal[1].toInteger()
        }

        data= [:]
        data[keyMap]= parts[1].toLowerCase()
        data[keyDifficulty]= parts[2]
        data[keyLength]= parts[3]
        data[keyTime]= new Time(Integer.valueOf(parts[4]))
        data[keyResult]= Integer.valueOf(parts[5])
        data[keyWave]= Integer.valueOf(parts[6])
        data[keyDeaths]= deaths        
    }
    public Type getType() {
        return Type.Match
    }
    public int getSeqnum() {
        return -1
    }
    public boolean isLast() {
        return true
    }
}

