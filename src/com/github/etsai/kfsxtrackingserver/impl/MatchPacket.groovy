/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Packet
import com.github.etsai.kfsxtrackingserver.Time
import static com.github.etsai.kfsxtrackingserver.Packet.Type.Match

/**
 *
 * @author eric
 */
public class MatchPacket extends Packet {
    public final def keyMap= "map"
    public final def keyDifficulty= "difficulty"
    public final def keyLength= "length"
    public final def keyTime= "time"
    public final def keyResult= "result"
    public final def keyWave= "wave"
    
    private final def valid
    
    public MatchPacket(String protocol, int version, String[] parts) {
        super(protocol, version)
        
        try {
            data= [:]
            data[keyMap]= parts[1]
            data[keyDifficulty]= parts[2]
            data[keyLength]= parts[3]
            data[keyTime]= new Time(Integer.valueOf(parts[4]))
            data[keyResult]= Integer.valueOf(parts[5])
            data[keyWave]= Integer.valueOf(parts[6])
            valid= true
        } catch (Exception ex) {
            valid= false
        }
        
    }
    public Packet.Type getType() {
        return Match
    }
    public int getSeqnum() {
        return -1
    }
    public boolean isLast() {
        return true
    }
    public Map<String, Object> getData() {
        throw new UnsupportedOperationException("Not yet implemented")
    }
    public abstract boolean isValid() {
        return valid
    }
}

