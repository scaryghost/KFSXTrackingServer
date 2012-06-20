/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.impl.MatchPacket;
import com.github.etsai.kfsxtrackingserver.impl.PlayerPacket;
import java.util.Map;

/**
 * Interface for parsing and getting at packet information
 * @author etsai
 */
public abstract class Packet {
    public static final String protocolMatch= "kfstatsx-match";
    public static final String protocolPlayer= "kfstatsx-player";
    
    private final String protocol;
    private final int version;
    
    protected Map<String, Object> data;
    protected boolean valid;
    
    public enum Type {
        Match, Player, Password;
    }
    
    public static Packet parse(String text) {
        Packet instance;
        String[] parts= text.split("\\|");
        
        switch (parts[0]) {
            case protocolMatch:
                instance= new MatchPacket(parts[0], Integer.valueOf(parts[1]), parts);
                break;
            case protocolPlayer:
                instance= new PlayerPacket(parts[0], Integer.valueOf(parts[1]), parts);
                break;
            default:
                throw new RuntimeException("Unrecognized protocol: "+parts[0]);
        }
        return instance;
    }
    protected Packet(String protocol, int version) {
        this.protocol= protocol;
        this.version= version;
    }
    public String getProtocol() {
        return protocol;
    }
    public int getVersion() {
        return version;
    }        
    public Iterable<String> getDataKeys() {
        return data.keySet();
    }
    public Object getData(String key) {
        return data.get(key);
    }
    public abstract Type getType();
    public abstract int getSeqnum();
    public abstract boolean isLast();
    public abstract boolean isValid();

}
