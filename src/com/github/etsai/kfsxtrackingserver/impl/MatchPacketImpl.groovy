/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.Result

/**
 * Represents a match message
 * @author etsai
 */
public class MatchPacketImpl implements MatchPacket {
    private final def difficulty, length, level, wave, attrs, stats, 
            category, serverAddress, serverPort
    
    public MatchPacketImpl(String[] parts, String serverAddress, int serverPort) throws InvalidPacketFormatException {
        this.serverAddress= serverAddress
        this.serverPort= serverPort

        try {
            difficulty= parts[2]
            length= parts[3]
            wave= parts[4].toInteger()
            category= parts[1]
            level= parts[5]
            attrs= [:]
            stats= [:]

            if (parts[1] == "result") {
                attrs.duration= parts[6].toInteger()
                switch (parts[7]) {
                    case "1":
                        attrs.result= Result.LOSS
                        break
                    case "2":
                        attrs.result= Result.WIN
                        break
                    default:
                        throw new InvalidPacketFormatException("Unrecognized result value: ${parts[7]}")
                }
            } else {
                parts[6].tokenize(",").each {
                    def statParts= it.tokenize("=")
                    stats[statParts[0]]= statParts[1].toInteger()
                }
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new InvalidPacketFormatException(ex.getMessage())
        }
    }
    public MatchPacketImpl(String[] parts) throws InvalidPacketFormatException {
        this(parts, null, null)
    }
    public String getCategory() {
        return category
    }
    public String getDifficulty() {
        return difficulty
    }
    public String getLevel() {
        return level
    }
    
    public String getLength() {
        return length
    }
    
    public int getWave() {
        return wave
    }
    
    public Map<String, Object> getAttributes() {
        return attrs
    }

    public Map<String, Integer> getStats() {
        return stats
    }
    
    @Override
    public String toString() {
        return [difficulty, length, level, wave, attrs, stats]
    }

    @Override
    public int getServerPort() {
        return serverPort
    }
    @Override
    public String getServerAddress() {
        return serverAddress
    }
    @Override
    public String getServerAddressPort() {
        return "$serverAddress:$serverPort"
    }
}
