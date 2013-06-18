/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket;
import java.util.Collection;

/**
 *
 * @author etsai
 */
public interface DataWriter {
    public static class SteamInfo {
        public String steamID64;
        public String name;
        public String avatar;
    }

    public void writeSteamInfo(Collection<SteamInfo> steamInfo);
    public void writeSteamInfo(SteamInfo steamInfo);
    public void writeMatchData(MatchPacket packet);
    public void writePlayerData(PlayerContent content);
}
