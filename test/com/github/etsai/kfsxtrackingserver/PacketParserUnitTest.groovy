/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
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
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidPassword() {
        String msg= "kfstatsx-match,2,invalidpwd|result|Hell on Earth|Long|3|kf-offices|501|1|_close"
        parser.parse(msg)
    }
    
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidProtocol() {
        String msg= "kfstatsx-invalidprotocol,2,invalidpwd|result|Hell on Earth|Long|3|kf-offices|501|1|_close"
        parser.parse(msg)
    }
    
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidMatchVersion() {
        String msg= "kfstatsx-match,1,invalidpwd|result|Hell on Earth|Long|3|kf-offices|501|1|_close"
        parser.parse(msg)
    }
    
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidPlayerVersion() {
        String msg= "kfstatsx-player,1,server|1364787|0|summary|Time Alive=417"
        parser.parse(msg)
    }
    
    @Test(expected= ArrayIndexOutOfBoundsException.class)
    public void invalidHeader() {
        String msg= "kfstatsx-player|1364787|0|summary|Time Alive=417"
        parser.parse(msg)
    }
}