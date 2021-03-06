package scaryghost.kfsxtrackingserver

import static scaryghost.kfsxtrackingserver.PacketParser.MatchPacket.PROTOCOL
import static scaryghost.kfsxtrackingserver.PacketParser.MatchPacket.VERSION
import org.junit.Test
import static org.junit.Assert.*

/**
 * Tests the MatchPacket's stat packet format
 * @author etsai
 */
class MatchStatUnitTest {
    private static final def password= "server"

    private final def header, category= "wave"
    public MatchStatUnitTest() {
        header= "$PROTOCOL,$VERSION,$password"
    }

    private def buildPacket(def statStr) {
        new PacketParser(password).parse("$header|7707|$category|2|kills|KFVetCommando|$statStr")
    }
    @Test
    public void checkPort() {
        def matchPacket= buildPacket("")
        assertEquals(matchPacket.getServerPort(), 7707)
    }
    @Test
    public void checkAttributes() {
        def matchPacket= buildPacket("")
        assertEquals(matchPacket.getAttributes(), ["type": "kills", "perk": "KFVetCommando"])
    }
    @Test
    public void checkStats() {
        def matchPacket= buildPacket("Clot=33,Gorefast=9,Crawler=9,Stalker=5,Bloat=1")
        assertEquals(matchPacket.getStats(), [Clot:33, Gorefast:9, Crawler:9, Stalker:5, Bloat:1])
    }
    @Test
    public void checkBlankStats() {
        def matchPacket= buildPacket("")
        assertEquals(matchPacket.getStats().isEmpty(), true)
    }
    @Test
    public void checkCategory() {
        def matchPacket= buildPacket("")
        assertEquals(matchPacket.getCategory(), category)
    }
}
