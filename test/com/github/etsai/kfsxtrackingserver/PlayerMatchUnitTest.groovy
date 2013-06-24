/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import static com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket.PROTOCOL
import static com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket.VERSION
import com.github.etsai.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import com.github.etsai.kfsxtrackingserver.PacketParser.Result
import org.junit.Test
import static org.junit.Assert.*

/**
 *
 * @author etsai
 */
public class PlayerMatchUnitTest {
    private static final def password= "server"

    private final def header
    public PlayerMatchUnitTest() {
        header= "$PROTOCOL,$VERSION,$password"
    }
    
    private def buildPacket(level, result, wave, finalWave, finalWaveSurvived) {
        new PacketParser(password).parse("$header|1364787|6|match|$level|Hell on Earth|Medium|$result|$wave|$finalWave|$finalWaveSurvived|305|_close")
    }
    @Test
    public void checkBlankStats() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 0, 0)
        assertEquals(playerPacket.getStats().isEmpty(), true)
    }
    @Test
    public void checkDifficulty() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 0, 0)
        assertEquals(playerPacket.getAttributes().difficulty, "Hell on Earth")
    }
    @Test
    public void checkLength() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 0, 0)
        assertEquals(playerPacket.getAttributes().length, "Medium")
    }
    @Test
    public void checkDuration() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 0, 0)
        assertEquals(playerPacket.getAttributes().duration, 305)
    }
    @Test
    public void checkLevel() {
        def playerPacket= buildPacket("KF-Icebreaker", 1, 2, 0, 0)
        assertEquals(playerPacket.getAttributes().level, "kf-icebreaker")
    }
    @Test
    public void checkWave() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 0, 0)
        assertEquals(playerPacket.getAttributes().wave, 2)
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void checkInvalidWave() {
        def playerPacket= buildPacket("kf-icebreaker", 1, "no number here", 0, 0)
    }
    @Test
    public void checkResultLoss() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 0, 0)
        assertEquals(playerPacket.getAttributes().result, Result.LOSS)
    }
    @Test
    public void checkResultWin() {
        def playerPacket= buildPacket("kf-icebreaker", 2, 2, 0, 0)
        assertEquals(playerPacket.getAttributes().result, Result.WIN)
    }
    @Test
    public void checkResultDisconnect() {
        def playerPacket= buildPacket("kf-icebreaker", 0, 2, 0, 0)
        assertEquals(playerPacket.getAttributes().result, Result.DISCONNECT)
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void checkInvalidResult() {
        def playerPacket= buildPacket("kf-icebreaker", -1, 2, 0, 0)
    }
    @Test
    public void checkFinaleSurvived1() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 0, 1)
        assertEquals(playerPacket.getAttributes()["finalWaveSurvived"], 0)
    }
    @Test
    public void checkFinaleSurvived2() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 1, 0)
        assertEquals(playerPacket.getAttributes()["finalWaveSurvived"], 0)
    }
    @Test
    public void checkFinaleSurvived3() {
        def playerPacket= buildPacket("kf-icebreaker", 1, 2, 1, 1)
        assertEquals(playerPacket.getAttributes()["finalWaveSurvived"], 1)
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidNumParts() {
        new PacketParser(password).parse("$header|1364787|6|match|Hell on Earth|Medium|305|_close")
    }
}
