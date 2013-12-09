/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import static com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket.PROTOCOL
import static com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket.VERSION
import com.github.etsai.kfsxtrackingserver.DataWriter.SteamInfo
import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket
import org.junit.Test
import static org.junit.Assert.*

/**
 *
 * @author etsai
 */
public class AccumulatorUnitTest {
    private class DummyDataWriter implements DataWriter {
        public List<String> getMissingSteamInfoIDs() {
            
        }
    
        public void writeSteamInfo(Collection<SteamInfo> steamInfo) {
            steamInfo.each {info ->
                println "Saving SteamInfo: $info"
            }
        }

        public void writeSteamInfo(SteamInfo steamInfo) {
            writeSteamInfo([steamInfo])
        }

        public void writeMatchData(MatchPacket packet) {
            
        }

        public void writePlayerData(PlayerContent content) {
            
        }
    }
    private static def password= "server", ttl= 5000
    private static def steamid1= "76561197961630515", steamid2= "76561197995161935"
    
    private def header= "$PROTOCOL,$VERSION,$password|7707", accumulator
    private def packets= [
    "$header|$steamid1|0|summary|Time Alive=417,Welding=1222,Damage=21627,Damage Taken=347,Deaths=2,Cash Spent=509,Healing=174,Kill Assists=16,Kills=72",
    "$header|$steamid1|1|weapons|PipeBomb=4,Knife=8,Lever Action Rifle=65,M79 Grenade Launcher=10",
    "$header|$steamid1|2|kills|Clot=32,Bloat=3,Crawler=17,Gorefast=12,Stalker=5,Husk=2,Siren=1",
    "$header|$steamid1|3|perks|SGDemolitions=417",
    "$header|$steamid1|4|actions|Decapitations=29,Healed Teammates=10,Husks Stunned=5,Shot By Husk=2,Received Heal=8,Healed Self=1",
    "$header|$steamid1|5|deaths|Clot=1,Siren=1",
    "$header|$steamid1|6|match|1|3|0|0|501|_close",
    "$header|$steamid2|0|summary|Time Alive=498,Damage=41690,Damage Taken=254,Armor Lost=102,Welding=108,Cash Spent=1119,Healing=147,Deaths=1,Kill Assists=26,Kills=143",
    "$header|$steamid2|1|weapons|9mm Tactical=152,Hunting Shotgun=32,Nade=3,Shotgun=58,Dual 9mms=39,Knife=11",
    "$header|$steamid2|2|kills|Clot=98,Gorefast=13,Bloat=5,Crawler=18,Husk=3,Stalker=6",
    "$header|$steamid2|3|perks|SGSupportSpec=498",
    "$header|$steamid2|4|actions|Decapitations=56,Received Heal=11,Shot By Husk=3,Healed Teammates=7,Healed Self=1",
    "$header|$steamid2|5|deaths|Gorefast=1",
    "$header|$steamid2|6|match|1|3|0|0|501|_close"]
    
    public AccumulatorUnitTest() {
        accumulator= new Accumulator(new DummyDataWriter(), password, ttl)
        
    }

    @Test
    public void checkPlayerContentCleaner() {
        accumulator.accumulate("kfstatsx-player,3,server|7707|1364787|3|perks|SGDemolitions=417")
        Thread.sleep(5000)
        assertNull(accumulator.getPlayerContent("76561197961630515"))
    }
    @Test
    public void checkCompletedPlayerContent() {
        (0..6).each {index ->
            accumulator.accumulate(packets[index])
        }
        assertNull(accumulator.getPlayerContent("76561197961630515"))
    }
    @Test
    public void checkMixedPlayerContent() {
        def indices= [6, 2, 9, 7, 4, 5, 2, 2, 2, 0, 1, 0, 1, 0]
        
        while(!packets.isEmpty()) {
            def index= indices.head()
            println "Index: $index -> ${packets[index]}"
            accumulator.accumulate(packets[index])
            packets.remove(index)
            indices= indices.tail()
        }
        assertTrue(accumulator.getPlayerContent("76561197961630515") == null && accumulator.getPlayerContent("76561197995161935") == null)
    }
}
