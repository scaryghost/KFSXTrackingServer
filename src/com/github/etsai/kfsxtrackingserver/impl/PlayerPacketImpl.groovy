/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.Result

/**
 * Represents a player message
 * @author etsai
 */
public class PlayerPacketImpl implements PlayerPacket {
    /** Offset for converting between linux and windows steamID64 */
    public static final Long linuxOffset= 76561197960265728
    /** Name of the category containing match information */
    public static final String matchCategory= "match"
    
    private final def steamID64, category, seqNo, close, stats, attrs,
            senderAddress, senderPort
    
    public PlayerPacketImpl(String[] parts, String senderAddress, int senderPort) throws InvalidPacketFormatException {
        this.senderAddress= senderAddress
        this.senderPort= senderPort
        try {
            def idLong= parts[1].toLong()
            if (parts[1].length() < 17) {
                steamID64= (idLong + linuxOffset).toString()
            } else {
                steamID64= parts[1]
            }
            category= parts[3]
            seqNo= parts[2].toInteger()
            close= parts.last() == "_close"
            stats= [:]
            attrs= [:]

            if (category == matchCategory) {
                attrs= [level: parts[4].toLowerCase(), difficulty: parts[5], length: parts[6], 
                    wave: parts[8].toInteger(), finalWave: parts[9].toInteger(), duration: parts[11].toInteger()]

                attrs["finalWaveSurvived"]= attrs.finalWave != 0 ? parts[10].toInteger() : 0
                switch(parts[7]) {
                    case "0":
                        attrs["result"]= Result.DISCONNECT
                        break
                    case "1":
                        attrs["result"]= Result.LOSS
                        break
                    case "2":
                        attrs["result"]= Result.WIN
                        break
                    default:
                        throw new InvalidPacketFormatException("Unrecognized result: ${parts[7]}")
                }

            } else {
                if (parts.size() >= 5) {
                    parts[4].tokenize(",").each {
                        def statParts= it.tokenize("=")
                        stats[statParts[0]]= statParts[1].toInteger()
                    }
                }
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new InvalidPacketFormatException(ex.getMessage())
        }
    }
    /**
     * Constructs object given the pipe separated string of stat information
     */
    public PlayerPacketImpl(String[] parts) throws InvalidPacketFormatException {
        this(parts, null, -1)
    }
    
    /**
     * Get the steamID64 of the player stat.  If the field was blank, null is returned
     * @return SteamID64 or null if blank
     */
    public String getSteamID64() {
        return steamID64
    }
    /**
     * Get the stat category the set of stats belong to
     * @return Stat category
     */
    public String getCategory() {
        return category
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
    /**    
     * Get match information stored by the stat packet.  If category is not "match", null is returned
     * @return Match information, or null if packet is not in the "match" category
     */
    public Map<String, Object> getAttributes() {
       return attrs 
    }
    /**
     * Generate string representation of the object
     * @return String representation
     */
    @Override
    public String toString() {
        return [steamID64, category, seqNo, close, stats, attrs]
    }

    public int getSenderPort() {
        return senderPort
    }
    public String getSenderAddress() {
        return senderAddress
    }
}
