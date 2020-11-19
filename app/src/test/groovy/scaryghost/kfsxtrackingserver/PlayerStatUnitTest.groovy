package scaryghost.kfsxtrackingserver

import scaryghost.kfsxtrackingserver.PacketParser.InvalidPacketFormatException
import static scaryghost.kfsxtrackingserver.PacketParser.PlayerPacket.PROTOCOL
import static scaryghost.kfsxtrackingserver.PacketParser.PlayerPacket.VERSION
import org.junit.Test
import static org.junit.Assert.*

/**
 * Tests the PlayerPacket's stat packet format
 * @author etsai
 */
public class PlayerStatUnitTest {
    private static final def password= "server"

    private final def header
    public PlayerStatUnitTest() {
        header= "$PROTOCOL,$VERSION,$password|7707"
    }

    private def buildPacket() {
        new PacketParser(password).parse("$header|1364787|1|weapons|FlameThrower=206,Katana=21,MKb42=110")
    }

    @Test
    public void checkStats() {
        def stats= [FlameThrower:206, Katana:21, MKb42:110]
        def playerPacket= buildPacket()
        assertEquals(playerPacket.getStats(), stats)
    }
    @Test
    public void checkBlankStats() {
        def playerPacket= new PacketParser(password).parse("$header|1364787|1|weapons|")
        assertEquals(playerPacket.getStats().isEmpty(), true)
    }
    @Test
    public void checkBlankAttributes() {
        def playerPacket= buildPacket()
        assertEquals(playerPacket.getAttributes().isEmpty(), true)
    }
    @Test(expected= InvalidPacketFormatException.class)
    public void invalidNumParts() {
        def playerPacket= new PacketParser(password).parse("$header|1364787")
    }
}
