/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.impl.PlayerPacketImpl;
import com.github.etsai.kfsxtrackingserver.impl.MatchPacketImpl;
import java.net.DatagramPacket;
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
    public abstract class StatPacket {
        private final int senderPort;
        private final String senderAddress;
        
        public StatPacket(String senderAddress, int senderPort) {
            this.senderAddress= senderAddress;
            this.senderPort= senderPort;
        }
        /**
         * Get the category name of the packet
         * @return Category name
         */
        public abstract String getCategory();
        /**
         * Get the statistics in (key, value) format.  The name of the statistic is 
         * the key its numerical value is the entry value.
         * @return Map of the statistics
         */
        public abstract Map<String, Integer> getStats();
        /**
         * Get non-statistics attributes of the packet.  The values may be string or numbers
         * @return Packet attributes
         */
        public abstract Map<String, Object> getAttributes();
        /**
         * Get the port number of machine that sent the packet
         * @return Sender's port number, null if sender information not available
         */
        public int getSenderPort() {
            return senderPort;
        }
        /**
         * Get the address of machine that sent the packet
         * @return Sender's address, -1 if sender information not available
         */
        public String getSenderAddress() {
            return senderAddress;
        }
    }
    /**
     * Packet specifically containing match information
     * @author etsai
     */
    public abstract class MatchPacket extends StatPacket {
        /** Protocol name for match statistics */ 
        public final static String PROTOCOL= "kfstatsx-match";
        /** Current protocol version */
        public final static int VERSION= 2;
        
        public MatchPacket(String senderAddress, int port) {
            super(senderAddress, port);
        }
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
        public abstract String getDifficulty();
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
        public abstract String getLength();
        /**
         * Get the name of the level the information relates to.
         * @return The level name the packet is holding statistics for
         */
        public abstract String getLevel();
        /**
         * Get the wave number the match information relates to
         * @return Wave number of the match
         */
        public abstract int getWave();
    }
    /**
     * Packet specifically containing player information
     * @author etsai
     */
    public abstract class PlayerPacket extends StatPacket {
        /** Protocol name for player statistics */ 
        public final static String PROTOCOL= "kfstatsx-player";
        /** Current protocol version */
        public final static int VERSION= 2;
        
        public PlayerPacket(String senderAddress, int senderPort) {
            super(senderAddress, senderPort);
        }
        /**
         * Get the sequence number of the packet.  Player packets are stored grouped together 
         * and stored as one rather than individually
         * @return Sequence number
         */
        public abstract int getSeqNo();
        /**
         * Get the close state of the packet.  If the packet is the last in the sequence, returns true
         * @return True if packet is last in sequence
         */
        public abstract boolean isClose();
        /**
         * Get the steamID64 this packet stores information for
         * @return SteamID64
         */
        public abstract String getSteamID64();
    }
    
    private abstract class PacketBuilder {
        protected String[] parts;
        
        public void setParts(String[] parts) {
            this.parts= parts;
        }
        public abstract PlayerPacket buildPlayerPacket();
        public abstract MatchPacket buildMatchPacket();
    }
    
    private class SenderPacketBuilder extends PacketBuilder {
        private final String senderAddress;
        private final int senderPort;

        private SenderPacketBuilder(String hostAddress, int port) {
            this.senderAddress= hostAddress;
            this.senderPort= port;
        }
        
        @Override
        public PlayerPacket buildPlayerPacket() {
            return new PlayerPacketImpl(parts, senderAddress, senderPort);
        }
        @Override
        public MatchPacket buildMatchPacket() {
            return new MatchPacketImpl(parts, senderAddress, senderPort);
        }
    }
    
    private class NoSenderPacketBuilder extends PacketBuilder {
        @Override
        public PlayerPacket buildPlayerPacket() {
            return new PlayerPacketImpl(parts);
        }
        @Override
        public MatchPacket buildMatchPacket() {
            return new MatchPacketImpl(parts);
        }
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
     * Parses the message and constructs the appropriate StatPacket object while 
     * accounting for the sender's address and port number
     * @param udpPacket UDP packet received from the KFStatsX mutator
     * @return Appropriate StatPacket object
     * @throws InvalidPacketFormatException If the msg does not match any known 
     * packet formats or contains an invalid password
     */
    public StatPacket parse(DatagramPacket udpPacket) throws InvalidPacketFormatException {
        String msg= new String(udpPacket.getData(), 0, udpPacket.getLength());
        return parseHelper(msg, new SenderPacketBuilder(udpPacket.getAddress().getHostAddress(), 
                udpPacket.getPort()));
    }
    /**
     * Parses the message and constructs the appropriate StatPacket object
     * @param msg UDP packet received from the KFStatsX mutator
     * @return Appropriate StatPacket object
     * @throws InvalidPacketFormatException If the msg does not match any known 
     * packet formats or contains an invalid password
     */
    public StatPacket parse(String msg) throws InvalidPacketFormatException {
        return parseHelper(msg, new NoSenderPacketBuilder());
    }
    private StatPacket parseHelper(String msg, PacketBuilder pBuilder) throws InvalidPacketFormatException {
        StatPacket packet= null;
        try {
            String[] parts= msg.split("\\|");
            String[] header= parts[0].split(",");

            pBuilder.setParts(parts);
            if (header[2] == null ? password != null : !header[2].equals(password)) {
                throw new InvalidPacketFormatException(String.format("Invalid password given, ignoring packet: %s", msg));
            }
            switch (header[0]) {
                case PlayerPacket.PROTOCOL:
                    if (Integer.valueOf(header[1])!= PlayerPacket.VERSION) {
                        throw new InvalidPacketFormatException(String.format("Wrong protocol version for player packet.  Read %s, expecting %d", 
                                header[1], PlayerPacket.VERSION));
                    }
                    packet= pBuilder.buildPlayerPacket();
                    break;
                case MatchPacket.PROTOCOL:
                    if (Integer.valueOf(header[1])!= MatchPacket.VERSION) {
                        throw new InvalidPacketFormatException(String.format("Wrong protocol version for match packet.  Read %s, expecting %d", 
                                header[1], MatchPacket.VERSION));
                    }
                    packet= pBuilder.buildMatchPacket();
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

