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
public class PasswordPacket extends Packet{
    public static final keyPassword= "password"
    
    public PasswordPacket(String protocol, int version, String[] parts) {
        super(protocol, version)
        
        try {
            data= [:]
            data[keyPassword]= parts[1]
            valid= true
        } catch (Exception e) {
            valid= false
        }
    }
    
    public Type getType() {
        return Type.Password
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

