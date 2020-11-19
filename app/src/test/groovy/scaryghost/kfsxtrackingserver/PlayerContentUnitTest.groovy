package scaryghost.kfsxtrackingserver

import static scaryghost.kfsxtrackingserver.PacketParser.PlayerPacket.PROTOCOL
import static scaryghost.kfsxtrackingserver.PacketParser.PlayerPacket.VERSION
import scaryghost.kfsxtrackingserver.PlayerContent.InvalidPacketIDException
import scaryghost.kfsxtrackingserver.impl.PlayerContentImpl
import java.util.Random
import org.junit.Test
import static org.junit.Assert.*

/**
 *
 * @author etsai
 */
public class PlayerContentUnitTest {
    private static final def password= "server"
    
    private def header= "$PROTOCOL,$VERSION,$password|7707|76561197961630515", parser, content
    private def packets= ["$header|0|summary|Time Alive=417,Welding=1222,Damage=21627,Damage Taken=347,Deaths=2,Cash Spent=509,Healing=174,Kill Assists=16,Kills=72",
    "$header|1|weapons|PipeBomb=4,Knife=8,Lever Action Rifle=65,M79 Grenade Launcher=10",
    "$header|2|kills|Clot=32,Bloat=3,Crawler=17,Gorefast=12,Stalker=5,Husk=2,Siren=1",
    "$header|3|perks|SGDemolitions=417",
    "$header|4|actions|Decapitations=29,Healed Teammates=10,Husks Stunned=5,Shot By Husk=2,Received Heal=8,Healed Self=1",
    "$header|5|deaths|Clot=1,Siren=1",
    "$header|6|match|1|3|0|0|501|_close"]
    
    public PlayerContentUnitTest() {
        parser= new PacketParser(password)
        content= new PlayerContentImpl()
    }

    @Test
    public void checkSteamID64() {
        packets.each {packet ->
            content.addPacket(parser.parse(packet))
        }
        assertEquals(content.getSteamID64(), "76561197961630515")
    }
    @Test(expected= InvalidPacketIDException.class)
    public void checkMismatchSteamID64() {
        packets.each {packet ->
            content.addPacket(parser.parse(packet))
        }
        content.addPacket(parser.parse("$PROTOCOL,$VERSION,$password|7707|34896207|3|perks|SGSupportSpec=498"))
    }
    @Test
    public void checkCompleted1() {
        (0..3).each {index ->
            content.addPacket(parser.parse(packets[index]))
        }
        content.addPacket(parser.parse(packets[6]))
        assertFalse(content.isCompleted())
    }
    @Test
    public void checkCompleted2() {
        def random= new Random()
        
        while(!packets.isEmpty()) {
            def index= random.nextInt(packets.size())
            println "Selecting index: $index"
            content.addPacket(parser.parse(packets[index]))
            packets.remove(index)
        }
        assertTrue(content.isCompleted())
    }
    @Test
    public void checkCompleted3() {
        def random= new Random()
        
        while(packets.size() > 1) {
            def index= random.nextInt(packets.size())
            println "Selecting index: $index"
            content.addPacket(parser.parse(packets[index]))
            packets.remove(index)
        }
        assertFalse(content.isCompleted())
    }
    @Test(expected= InvalidPacketIDException.class)
    public void checkDuplicateSeqNo1() {
        def random= new Random()
        
        packets.each {packet ->
            content.addPacket(parser.parse(packet))
        }
        
        def index= random.nextInt(packets.size() - 1)
        println "checkDuplicateSeqNo1: Adding duplicate index: $index"
        content.addPacket(parser.parse(packets[index]))
    }
    @Test(expected= InvalidPacketIDException.class)
    public void checkDuplicateSeqNo2() {
        def random= new Random()
        
        packets.each {packet ->
            content.addPacket(parser.parse(packet))
        }
        content.addPacket(parser.parse(packets.last()))
    }
}
