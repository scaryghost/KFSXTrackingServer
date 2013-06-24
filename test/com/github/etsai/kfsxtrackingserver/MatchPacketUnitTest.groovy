/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import static com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket.PROTOCOL
import static com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket.VERSION
import com.github.etsai.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import org.junit.Test
import static org.junit.Assert.*

/**
 * Tests the functions specific to the MatchPacket interface
 * @author etsai
 */
class MatchPacketUnitTest {
    private static final def password= "server"
    
    private final def header
    public MatchPacketUnitTest() {
        header= "$PROTOCOL,$VERSION,$password"
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    private def buildPacket() {
        new PacketParser(password).parse("$header|result|Hell on Earth|Medium|2|kf-icebreaker|305|1|_close")
    }
    @Test
    public void checkDifficulty() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getDifficulty(), "Hell on Earth")
    }
    @Test
    public void checkLength() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getLength(), "Medium")
    }
    @Test
    public void checkLevel() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getLevel(), "kf-icebreaker")
    }
    @Test
    public void checkWave() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getWave(), 2)
    }
    @Test
    public void checkCategory() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getCategory(), "result")
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidWave() {
        def matchPacket= new PacketParser(password).parse("$header|result|Hell on Earth|Medium|abcd|kf-icebreaker|305|1|_close")
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidNumParts() {
        def matchPacket= new PacketParser(password).parse("$header|result|kf-icebreaker|_close")
    }
}
