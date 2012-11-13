/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Packet

/**
 * Represents a player message
 * @author etsai
 */
public class PlayerPacket extends Packet {
    static class MatchInfo {
        public enum Result {
            WIN, LOSS, DISCONNECT
        }

        public def level
        public def difficulty
        public def length
        public def wave
        public def result

    
        @Override
        public String toString() {
            return [level, difficulty, length, wave, result].toString()
        }
    }
    
    /** Protocol name for player stats */ 
    public static String PROTOCOL= "kfstatsx-player"
    /** Current protocol version */
    public static Integer VERSION= 1
    /** Offset for converting between linux and windows steamID64 */
    public static Long linuxOffset= 76561197960265728
    
    private final def steamID64
    private final def category
    private final def matchInfo
    
    /**
     * Constructs object given the pipe separated string of stat information
     */
    public PlayerPacket(def parts) {
        super((parts[3] == "match") ? "" : (parts.size() < 5 ? "" : parts[4]))
        
        steamID64= parts[1]
        if (steamID64 == "") {
            steamID64= null
        } else if (steamID64.length() < 17) {
            steamID64= (steamID64.toLong() + linuxOffset).toString()
        }
        category= parts[3]
        
        if (category == "match") {
            matchInfo= new MatchInfo(level: parts[4].toLowerCase(), difficulty: parts[5], 
                length: parts[6], wave: parts[8].toInteger())

            switch(parts[7].toInteger()) {
                case 0:
                    matchInfo.result= MatchInfo.Result.DISCONNECT
                    break
                case 1:
                    matchInfo.result= MatchInfo.Result.LOSS
                    break
                case 2:
                    matchInfo.result= MatchInfo.Result.WIN
                    break
                default:
                    throw new RuntimeException("Unrecognized result: ${parts[7]}")
            }
        }
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
    /**
     * Get match information stored by the stat packet.  If category is not "match", null is returned
     * @return Match information, or null if packet is not in the "match" category
     */
    public MatchInfo getMatchInfo() {
        return matchInfo
    }

    /**
     * Generate string representation of the object
     * @return String representation
     */
    @Override
    public String toString() {
        def attrs= [steamID64, seqNo, category,]
        if (matchInfo == null) {
            return attrs << stats
        }
        return attrs << matchInfo
    }
}