/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import static com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket.PROTOCOL
import static com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket.VERSION
import org.junit.Test
import static org.junit.Assert.*

/**
 *
 * @author etsai
 */
public class PlayerPacketUnitTest {
    private static final def password= "server"

    private final def header
    public PlayerPacketUnitTest() {
        header= "$PROTOCOL,$VERSION,$password|7707"
    }
    
    private def getPlayerStatPacket(steamID64) {
        new PacketParser(password).parse("$header|$steamID64|1|weapons|FlameThrower=206,Katana=21,MKb42=110")
    }
    private def getPlayerMatchPacket() {
        new PacketParser(password).parse("$header|1364787|6|match|1|2|0|0|305|_close")
    }
    
    @Test
    public void checkSeqNo() {
        def playerPacket= getPlayerStatPacket(1364787)
        assertEquals(playerPacket.getSeqNo(), 1)
    }
    @Test
    public void checkIsClose1() {
        def playerPacket= getPlayerStatPacket(1364787)
        assertEquals(playerPacket.isClose(), false)
    }
    @Test
    public void checkIsClose2() {
        def playerPacket= getPlayerMatchPacket()
        assertEquals(playerPacket.isClose(), true)
    }
    @Test
    public void checkSteamID64() {
        def playerPacket= getPlayerStatPacket(1364787)
        assertEquals(playerPacket.getSteamID64(), "1364787")
    }
    @Test
    public void checkCategory() {
        def playerPacket= getPlayerStatPacket(1364787)
        assertEquals(playerPacket.getCategory(), "weapons")
    }
}
