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
 * Interprets the messages received from the KFStatsX mutator
 * @author etsai
 */
public class PacketParser {
    /**
     * Possible results of a match
     * @author etsai
     */
    public enum Result {
        /** Match was not finished */
        INCOMPLETE,
        /** Match was won */
        WIN,
        /** Match was lost */
        LOSS,
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
        /**
         * Typecast another throwable into an InvalidPacketFormatException
         * @param cause Another throwable that triggered an exception
         */
        public InvalidPacketFormatException(Throwable cause) {
            super(cause);
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
        /**
         * Get the port number of server the packet corresponds to
         * @return Server's port number, null if server information not available
         */
        public int getServerPort();
        /**
         * Get the address of server the packet corresponds to
         * @return Server's address, null if server information not available
         */
        public String getServerAddress();
        /**
         * Get the address and port of the server concatenated in the form $address:$port
         * @return Address and port in the form $address:$port
         */
        public String getServerAddressPort();
    }
    /**
     * Packet specifically containing match information
     * @author etsai
     */
    public interface MatchPacket extends StatPacket {
        /** Protocol name for match statistics */ 
        public final static String PROTOCOL= "kfstatsx-match";
        /** Current protocol version */
        public final static int VERSION= 3;

        public final static String ATTR_DIFFICULTY= "difficulty";
        public final static String ATTR_LENGTH= "length";
        public final static String ATTR_MAP= "map";
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
        public final static String PROTOCOL= "kfstatsx-player";
        /** Current protocol version */
        public final static int VERSION= 3;
        
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
    
    private interface PacketBuilder {
        public PlayerPacket buildPlayerPacket(String[] parts);
        public MatchPacket buildMatchPacket(String[] parts);
    }
    
    private class SenderPacketBuilder implements PacketBuilder {
        private final String senderAddress;

        private SenderPacketBuilder(String hostAddress) {
            this.senderAddress= hostAddress;
        }
        
        @Override
        public PlayerPacket buildPlayerPacket(String[] parts) {
            return new PlayerPacketImpl(parts, senderAddress);
        }
        @Override
        public MatchPacket buildMatchPacket(String[] parts) {
            return new MatchPacketImpl(parts, senderAddress);
        }
    }
    
    private class NoSenderPacketBuilder implements PacketBuilder {
        @Override
        public PlayerPacket buildPlayerPacket(String[] parts) {
            return new PlayerPacketImpl(parts);
        }
        @Override
        public MatchPacket buildMatchPacket(String[] parts) {
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
     * accounting for the kf server's address and port number
     * @param udpPacket UDP packet received from the KFStatsX mutator
     * @return Appropriate StatPacket object
     * @throws InvalidPacketFormatException If the message does not match any known 
     * packet formats or contains an invalid password
     */
    public StatPacket parse(DatagramPacket udpPacket) throws InvalidPacketFormatException {
        String msg= new String(udpPacket.getData(), 0, udpPacket.getLength());
        return parseHelper(msg, new SenderPacketBuilder(udpPacket.getAddress().getHostAddress()));
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

            if (header[2] == null ? password != null : !header[2].equals(password)) {
                throw new InvalidPacketFormatException(String.format("Invalid password given, ignoring packet: %s", msg));
            }
            switch (header[0]) {
                case PlayerPacket.PROTOCOL:
                    if (Integer.valueOf(header[1])!= PlayerPacket.VERSION) {
                        throw new InvalidPacketFormatException(String.format("Wrong protocol version for player packet.  Read %s, expecting %d", 
                                header[1], PlayerPacket.VERSION));
                    }
                    packet= pBuilder.buildPlayerPacket(parts);
                    break;
                case MatchPacket.PROTOCOL:
                    if (Integer.valueOf(header[1])!= MatchPacket.VERSION) {
                        throw new InvalidPacketFormatException(String.format("Wrong protocol version for match packet.  Read %s, expecting %d", 
                                header[1], MatchPacket.VERSION));
                    }
                    packet= pBuilder.buildMatchPacket(parts);
                    break;
                default:
                    throw new InvalidPacketFormatException(String.format("Unrecognized packet protocol: %s", header[0]));
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new InvalidPacketFormatException(ex);
        }
        return packet;
        
    }
}

