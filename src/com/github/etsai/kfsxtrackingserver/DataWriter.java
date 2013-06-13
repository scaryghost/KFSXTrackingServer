/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket;

/**
 *
 * @author etsai
 */
public interface DataWriter {
    public void writeSteamInfo(String steamID64, String name, String avatar);
    public void writeMatchData(MatchPacket packet);
    public void writePlayerData(PlayerContent content);
}
