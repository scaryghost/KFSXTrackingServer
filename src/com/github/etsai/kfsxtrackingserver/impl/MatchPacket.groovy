/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Packet
import com.github.etsai.kfsxtrackingserver.Packet.Type
import com.github.etsai.kfsxtrackingserver.Time

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
    public final def keyDeaths= "deaths"

    public MatchPacket(String protocol, int version, String[] parts) {
        super(protocol, version)
        
        try {
            def deaths= new Properties()
            deaths.load(new StringReader(parts[7].replace(',','\n')))
            
            data= [:]
            data[keyMap]= parts[1]
            data[keyDifficulty]= parts[2]
            data[keyLength]= parts[3]
            data[keyTime]= new Time(Integer.valueOf(parts[4]))
            data[keyResult]= Integer.valueOf(parts[5])
            data[keyWave]= Integer.valueOf(parts[6])
            data[keyDeaths]= deaths
            
            valid= true
        } catch (Exception ex) {
            valid= false
        }
        
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
    public boolean isValid() {
        return valid
    }
}

