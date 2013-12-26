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
            serverAddress, serverPort
    
    public PlayerPacketImpl(String[] parts, String serverAddress) {
        this.serverAddress= serverAddress
        serverPort= parts[1].toInteger()
        steamID64= parts[2]
        seqNo= parts[3].toInteger()
        category= parts[4]
        close= parts.last() == "_close"
        stats= [:]
        attrs= [:]

        if (category == matchCategory) {
            attrs= [wave: parts[6].toInteger(), finalWave: parts[7].toInteger(), duration: parts[9].toInteger()]
            attrs.finalWaveSurvived= attrs.finalWave != 0 ? parts[8].toInteger() : 0
            attrs.disconnected= parts[5] == "0"
        } else {
            if (parts.size() >= 6) {
                parts[5].tokenize(",").each {
                    def statParts= it.tokenize("=")
                    stats[statParts[0]]= statParts[1].toInteger()
                }
            }
        }
    }
    /**
     * Constructs object given the pipe separated string of stat information
     */
    public PlayerPacketImpl(String[] parts) {
        this(parts, null)
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
        return [steamID64, category, seqNo, close, stats, attrs, serverAddress, serverPort]
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
