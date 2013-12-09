/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.PacketParser.MatchPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.PlayerPacket
import com.github.etsai.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import org.junit.Test

/**
 * Tests the error checking the parse function does
 * @author etsai
 */
public class PacketParserUnitTest {
    private static final def password="server";
    private final PacketParser parser;
    
    public PacketParserUnitTest() {
        parser= new PacketParser(password);
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidPort() {
        String msg= "${MatchPacket.PROTOCOL},${MatchPacket.VERSION},$password|707ab|result|Hell on Earth|Long|3|kf-offices|501|1|_close"
        parser.parse(msg)
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidPassword() {
        String msg= "${MatchPacket.PROTOCOL},${MatchPacket.VERSION},invalidpwd|7707|result|Hell on Earth|Long|3|kf-offices|501|1|_close"
        parser.parse(msg)
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidProtocol() {
        String msg= "kfstatsx-invalidprotocol,${MatchPacket.VERSION},invalidpwd|7707|result|Hell on Earth|Long|3|kf-offices|501|1|_close"
        parser.parse(msg)
    }
    
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidMatchVersion() {
        String msg= "${MatchPacket.PROTOCOL},${MatchPacket.VERSION - 1},invalidpwd|7707|result|Hell on Earth|Long|3|kf-offices|501|1|_close"
        parser.parse(msg)
    }
    
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidPlayerVersion() {
        String msg= "${PlayerPacket.PROTOCOL},${PlayerPacket.VERSION - 1},server|7707|1364787|0|summary|Time Alive=417"
        parser.parse(msg)
    }
    
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidHeader() {
        String msg= "${PlayerPacket.PROTOCOL}|7707|1364787|0|summary|Time Alive=417"
        parser.parse(msg)
    }
}