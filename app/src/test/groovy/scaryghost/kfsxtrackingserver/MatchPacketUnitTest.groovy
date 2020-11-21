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
    private def buildPacket() {
        new PacketParser(password).parse("$header|7707|wave|1|kills|KFVetCommando|Clot=29,Gorefast=2,Bloat=3")
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
        assertEquals(matchPacket.getCategory(), "wave")
    }
    @Test
    public void checkType() {
        def matchPacket= buildPacket()
        assertEquals(matchPacket.getAttributes()["type"], "kills")
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidWave() {
        def matchPacket= new PacketParser(password).parse("$header|7707|wave|abcd|kills|Bloat=1,Clot=10")
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidNumParts() {
        def matchPacket= new PacketParser(password).parse("$header|7707|killsBloat=1,Clot=10")
    }
}
