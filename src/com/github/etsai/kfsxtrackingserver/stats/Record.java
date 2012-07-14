/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

/**
 *
 * @author eric
 */
public class Record extends TableCommon {
    private final String steamid;
    private int disconnects;
    
    public Record(String steamid) {
        super(steamid.hashCode());
        this.steamid= steamid;
        disconnects= 0;
    }
    public String getSteamId() {
        return steamid;
    }
    public int getDisconnects() {
        return disconnects;
    }
    public void addDisconnects(int offset) {
        disconnects+= offset;
    }
}
