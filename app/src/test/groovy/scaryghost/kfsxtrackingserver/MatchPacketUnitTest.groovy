package scaryghost.kfsxtrackingserver

import static scaryghost.kfsxtrackingserver.PacketParser.MatchPacket.PROTOCOL
import static scaryghost.kfsxtrackingserver.PacketParser.MatchPacket.VERSION
import scaryghost.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
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
        new PacketParser(password).parse("$header|7707|kills|1|Bloat=1,Clot=10")
    }
    @Test
    public void checkPort() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getServerPort(), 7707)
    }
    @Test
    public void checkWave() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getWave(), 1)
    }
    @Test
    public void checkCategory() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getCategory(), "kills")
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidWave() {
        def matchPacket= new PacketParser(password).parse("$header|7707|kills|abcd|Bloat=1,Clot=10")
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidNumParts() {
        def matchPacket= new PacketParser(password).parse("$header|7707|killsBloat=1,Clot=10")
    }
}
