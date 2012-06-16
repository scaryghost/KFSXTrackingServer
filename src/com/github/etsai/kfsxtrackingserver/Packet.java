/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

/**
 * Interface for parsing and getting at packet information
 * @author etsai
 */
public abstract class Packet {
    private final String protocol;
    private final int version;
    
    public enum Type {
        Match, Player, Password;
    }
    
    public static void parse(String text) {
        String[] parts= text.split("\\|");
        
    
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
    public abstract Type getType();
    public abstract int getSeqnum();
    public abstract boolean isLast();
    public abstract String getBody();
    
}
