/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import static org.junit.Assert.*

/**
 * Tests the MatchPacket's result packet format
 * @author etsai
 */
@RunWith(value = Parameterized.class)
public class MatchResultUnitTest {    
    private static final def password= "server"
    
    private final def parser, matchPacket, expectedResult
    
    @Parameters
    public static Collection<Object[]> data() {
        [[1, PacketParser.Result.LOSS], [2, PacketParser.Result.WIN]].collect {value, expectedResult ->
            ["kfstatsx-match,2,server|result|Hell on Earth|Medium|2|kf-icebreaker|305|$value|_close", expectedResult] as Object[]
        }
    }
         
    public MatchResultUnitTest(def msg, def expectedResult) {
        parser= new PacketParser(password)
        matchPacket= parser.parse(msg)
        this.expectedResult= expectedResult
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
    
    @Test
    public void checkDifficulty() {
        assertEquals(matchPacket.getDifficulty(), "Hell on Earth")
    }
    @Test
    public void checkLength() {
        assertEquals(matchPacket.getLength(), "Medium")
    }
    @Test
    public void checkLevel() {
        assertEquals(matchPacket.getLevel(), "kf-icebreaker")
    }
    @Test
    public void checkWave() {
        assertEquals(matchPacket.getWave(), 2)
    }
    @Test
    public void checkCategory() {
        assertEquals(matchPacket.getCategory(), "result")
    }
    @Test
    public void checkStats() {
        assertEquals(matchPacket.getStats().isEmpty(), true)
    }
    @Test
    public void checkResult() {
        assertEquals(matchPacket.getAttributes().result, expectedResult)
    }
    @Test
    public void checkDuration() {
        assertEquals(matchPacket.getAttributes().duration, 305)
    }
}
