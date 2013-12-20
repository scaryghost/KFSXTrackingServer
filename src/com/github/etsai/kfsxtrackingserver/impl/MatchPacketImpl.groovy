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
    
    public MatchPacketImpl(String[] parts, String serverAddress) {
        serverAddress= serverAddress
        serverPort= parts[1].toInteger()
        category= parts[2]
        attrs= [:]
        stats= [:]

        switch(category) {
            case "info":
                attrs[MatchPacket.ATTR_DIFFICULTY]= parts[3]
                attrs[MatchPacket.ATTR_LENGTH]= parts[4]
                attrs[MatchPacket.ATTR_MAP]= parts[5]
                break
            case "result":
                wave= parts[3].toInteger()
                attrs.duration= parts[4].toInteger()
                switch (parts[5]) {
                    case "0":
                        attrs.result= Result.MID_GAME_VOTE
                        break
                    case "1":
                        attrs.result= Result.LOSS
                        break
                    case "2":
                        attrs.result= Result.WIN
                        break
                    default:
                        throw new InvalidPacketFormatException("Unrecognized result value: ${parts[5]}")
                }
                break
            case "wave":
                wave= parts[3].toInteger()
                attrs.type= parts[4]
                switch(attrs.type) {
                    case "summary":
                        attrs.duration= parts[6].toInteger()
                        attrs.completed= parts[5].toBoolean()
                        parts[7].tokenize(",").each {
                            def perkCount= it.tokenize("=")
                            stats[perkCount[0]]= perkCount[1].toInteger()
                        }
                        break
                    default:
                        attrs.perk= parts[5]
                        parts[6].tokenize(",").each {
                            def statInfo= it.tokenize("=")
                            stats[statInfo[0]]= statInfo[1].toInteger()
                        }
                        break
                }
                break
            default:
                throw new InvalidPacketFormatException("Unrecognized match category: $category")
        }
    }
    public MatchPacketImpl(String[] parts) {
        this(parts, null)
    }
    public String getCategory() {
        return category
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
        return [difficulty, length, level, wave, attrs, stats, serverAddress, serverPort]
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
