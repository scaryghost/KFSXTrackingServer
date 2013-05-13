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
    private final String password;
    //kfstatsx-player,2,$pwd|$steamid|$seq_no|match|$map_name|$difficulty|$length|$result|$wave_no|$final_wave_reached|$final_wave_survived|$duration|_close    
    
    public enum Result {
        WIN,
        LOSS,
        DISCONNECT
    }
    
    public interface StatPacket {
        public String getCategory();
        public Map<String, Integer> getStats();
        public Map<String, Object> getAttributes();
    }
    public interface MatchPacket extends StatPacket {
        /** Protocol name for match stats */ 
        public static String PROTOCOL= "kfstatsx-match";
        /** Current protocol version */
        public static Integer VERSION= 2;
        
        public String getDifficulty();
        public String getLength();
        public String getLevel();
        public int getWave();
    }
    
    public interface PlayerPacket extends StatPacket {
        /** Protocol name for player stats */ 
        public static String PROTOCOL= "kfstatsx-player";
        /** Current protocol version */
        public static Integer VERSION= 2;
        
        public int getSeqNo();
        public boolean isClose();
        public String getSteamID64();
    }
    
    public PacketParser(String password) {
        this.password= password;
    }
    
    public StatPacket parse(String msg) {
        String[] parts= msg.split("\\|");
        String[] header= parts[0].split(",");
        StatPacket packet;
        
        if (header[2] == null ? password != null : !header[2].equals(password)) {
            throw new RuntimeException(String.format("Invalid password given, ignoring packet: %s", msg));
        }
        switch (header[0]) {
            case PlayerPacket.PROTOCOL:
                if (Integer.valueOf(header[1])!= PlayerPacket.VERSION) {
                    throw new RuntimeException(String.format("Wrong protocol version for player packet.  Read %s, expecting %d", header[1], PlayerPacket.VERSION));
                }
                packet= new PlayerPacketImpl(parts);
                break;
            case MatchPacket.PROTOCOL:
                if (Integer.valueOf(header[1])!= MatchPacket.VERSION) {
                    throw new RuntimeException(String.format("Wrong protocol version for player packet.  Read %s, expecting %d", header[1], MatchPacket.VERSION));
                }
                packet= new MatchPacketImpl(parts);
                break;
            default:
                throw new RuntimeException(String.format("Unrecognized packet protocol: %s", header[0]));
        }
        return packet;
    }
}

