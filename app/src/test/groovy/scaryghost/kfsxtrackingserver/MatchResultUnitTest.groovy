package scaryghost.kfsxtrackingserver

import static scaryghost.kfsxtrackingserver.PacketParser.MatchPacket.PROTOCOL
import static scaryghost.kfsxtrackingserver.PacketParser.MatchPacket.VERSION
import scaryghost.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import org.junit.Test
import static org.junit.Assert.*

/**
 * Tests the MatchPacket's result packet format
 * @author etsai
 */
public class MatchResultUnitTest {    
    private static final def password= "server"
    
    private final def header
    public MatchResultUnitTest() {
        header= "$PROTOCOL,$VERSION,$password"
    }
    private def buildPacket(def result) {
        new PacketParser(password).parse("$header|7707|result|2|305|$result")
    }
    @Test
    public void checkPort() {
        def matchPacket= buildPacket("2")
        assertEquals(matchPacket.getServerPort(), 7707)
    }
    @Test
    public void checkWin() {
        def matchPacket= buildPacket("2")
        assertEquals(matchPacket.getAttributes().result, PacketParser.Result.WIN)
    }
    @Test
    public void checkLoss() {
        def matchPacket= buildPacket("1")
        assertEquals(matchPacket.getAttributes().result, PacketParser.Result.LOSS)
    }
    @Test
    public void checkMidGameVote() {
        def matchPacket= buildPacket("0")
        assertEquals(matchPacket.getAttributes().result, PacketParser.Result.MID_GAME_VOTE)
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void checkInvalidResult() {
        def matchPacket= buildPacket("3")
    }
    @Test
    public void checkDuration() {
        def matchPacket= buildPacket("1")
        assertEquals(matchPacket.getAttributes().duration, 305)
    }
    @Test
    public void checkStats() {
        def matchPacket= buildPacket("1")
        assertEquals(matchPacket.getStats().isEmpty(), true)
    }
}
