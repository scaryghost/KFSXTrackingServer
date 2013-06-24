/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.impl.PlayerPacketImpl;
import com.github.etsai.kfsxtrackingserver.impl.MatchPacketImpl;
import java.util.Map;

/**
 * Interprets the messages received from the mutator
 * @author etsai
 */
public class PacketParser {
    /**
     * Possible results of a match
     * @author etsai
     */
    public enum Result {
        /** Match was won */
        WIN,
        /** Match was lost */
        LOSS,
        /** Player disconnected before match finished */
        DISCONNECT
    }
    
    
    /**
     * Exception signaling that the received packet did not match the expected format
     * @author etsai
     */
    public static class InvalidPacketFormatException extends Exception {
        /**
         * Constructs an exception with the given message
         * @param msg Message to display
         */
        public InvalidPacketFormatException(String msg) {
            super(msg);
        }
    }
    
    /**
     * Object wrapper for the received data packet
     * @author etsai
     */
    public interface StatPacket {
        /**
         * Get the category name of the packet
         * @return Category name
         */
        public String getCategory();
        /**
         * Get the statistics in (key, value) format.  The name of the statistic is 
         * the key its numerical value is the entry value.
         * @return Map of the statistics
         */
        public Map<String, Integer> getStats();
        /**
         * Get non-statistics attributes of the packet.  The values may be string or numbers
         * @return Packet attributes
         */
        public Map<String, Object> getAttributes();
    }
    /**
     * Packet specifically containing match information
     * @author etsai
     */
    public interface MatchPacket extends StatPacket {
        /** Protocol name for match statistics */ 
        public static String PROTOCOL= "kfstatsx-match";
        /** Current protocol version */
        public static Integer VERSION= 2;
        
        /**
         * Get the difficulty the information relates to.  Difficulty should be one of :
         * <ul>
         * <li>Hell on Earth</li>
         * <li>Suicidal</li>
         * <li>Hard</li>
         * <li>Normal</li>
         * <li>Beginner</li>
         * </ul>
         * @return The difficulty the packet is holding statistics for
         */
        public String getDifficulty();
        /**
         * Get the game length the information relates to.  Length should be one of :
         * <ul>
         * <li>Long</li>
         * <li>Medium</li>
         * <li>Short</li>
         * <li>Custom</li>
         * </ul>
         * @return The game length the packet is holding statistics for
         */
        public String getLength();
        /**
         * Get the name of the level the information relates to.
         * @return The level name the packet is holding statistics for
         */
        public String getLevel();
        /**
         * Get the wave number the match information relates to
         * @return Wave number of the match
         */
        public int getWave();
    }
    /**
     * Packet specifically containing player information
     * @author etsai
     */
    public interface PlayerPacket extends StatPacket {
        /** Protocol name for player statistics */ 
        public static String PROTOCOL= "kfstatsx-player";
        /** Current protocol version */
        public static Integer VERSION= 2;
        
        /**
         * Get the sequence number of the packet.  Player packets are stored grouped together 
         * and stored as one rather than individually
         * @return Sequence number
         */
        public int getSeqNo();
        /**
         * Get the close state of the packet.  If the packet is the last in the sequence, returns true
         * @return True if packet is last in sequence
         */
        public boolean isClose();
        /**
         * Get the steamID64 this packet stores information for
         * @return SteamID64
         */
        public String getSteamID64();
    }
    
    /** Password packets need to have to be considered valid */
    private final String password;
    
    /**
     * Creates a new packet parser, filtering on the given password
     * @param password Password for valid packets
     */
    public PacketParser(String password) {
        this.password= password;
    }
    /**
     * Parses the message and constructs the appropriate StatPacket object
     * @param msg UDP packet received from the KFStatsX mutator
     * @return Appropriate StatPacket object
     * @throws InvalidPacketFormatException If the msg does not match any known packet formats
     */
    public StatPacket parse(String msg) throws InvalidPacketFormatException {
        StatPacket packet= null;
        
        try {
            String[] parts= msg.split("\\|");
            String[] header= parts[0].split(",");

            if (header[2] == null ? password != null : !header[2].equals(password)) {
                throw new InvalidPacketFormatException(String.format("Invalid password given, ignoring packet: %s", msg));
            }
            switch (header[0]) {
                case PlayerPacket.PROTOCOL:
                    if (Integer.valueOf(header[1])!= PlayerPacket.VERSION) {
                        throw new InvalidPacketFormatException(String.format("Wrong protocol version for player packet.  Read %s, expecting %d", 
                                header[1], PlayerPacket.VERSION));
                    }
                    packet= new PlayerPacketImpl(parts);
                    break;
                case MatchPacket.PROTOCOL:
                    if (Integer.valueOf(header[1])!= MatchPacket.VERSION) {
                        throw new InvalidPacketFormatException(String.format("Wrong protocol version for player packet.  Read %s, expecting %d", 
                                header[1], MatchPacket.VERSION));
                    }
                    packet= new MatchPacketImpl(parts);
                    break;
                default:
                    throw new InvalidPacketFormatException(String.format("Unrecognized packet protocol: %s", header[0]));
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new InvalidPacketFormatException(ex.getMessage());
        }
        return packet;
    }
}

