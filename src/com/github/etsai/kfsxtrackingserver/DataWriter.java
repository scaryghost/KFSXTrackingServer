/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket;
import java.util.Collection;
import java.util.List;

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
    /**
     * Get the steamID64 of records that do not have any steam community information stored in the database
     * @return List of steamID64 that do not have steam community info
     */
    public List<String> getMissingSteamInfoIDs();
    public void writeSteamInfo(Collection<SteamInfo> steamInfo);
    public void writeSteamInfo(SteamInfo steamInfo);
    public void writeMatchData(MatchPacket packet);
    public void writePlayerData(PlayerContent content);
}
