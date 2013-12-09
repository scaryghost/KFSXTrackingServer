/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket;
import com.github.etsai.kfsxtrackingserver.PacketParser.Result;
import java.util.Collection;

/**
 * Manages the packets that are sent for a player
 * @author etsai
 */
public interface PlayerContent {
    /**
     * Tuple storing the match information for the packet group
     * @author etsai
     */
    public class Match {
        public int wave, duration;
        public byte finalWave, finalWaveSurvived;
        public Result result;
    }
    /**
     * Exception representing a mismatch is steamID64
     * @author etsai
     */
    public class InvalidPacketIDException extends Exception {
        /**
         * Constructs the exception with the given message
         * @param msg Message associated with the exception
         */
        public InvalidPacketIDException(String msg) {
            super(msg);
        }
    }
    /**
     * Get the server address that the player content is from
     * @return Content's server address
     */
    public String getServerAddress();
    /**
     * Get the server port that the player content is from
     * @return Content's server port
     */
    public int getServerPort();
    /**
     *  Get the content's server information in $address:$port format
     * @return Content's server information in $address:$port format
     */
    public String getServerAddressPort();
    /**
     * Get the stat packets.  This does not include the match information
     * @return Collection of player packets
     */
    public Collection<PlayerPacket> getPackets();
    /**
     * Get the steamID64 of the stored packets
     * @return 
     */
    public String getSteamID64();
    /**
     * Returns true if the player content has received all the required stat packets and match information
     * @return True if content is complete
     */
    public boolean isCompleted();
    /**
     * Get the match information 
     * @return Match information
     */
    public Match getMatchInfo();
    
    /**
     * Add a packet to the contained content
     * @param packet Player packet to add
     * @throws InvalidPacketIDException If the steamID64 of the packet doesn't match the stored id of the content 
     * or packet's seqNo has already been filled
     */
    public void addPacket(PlayerPacket packet) throws InvalidPacketIDException;
    
}
