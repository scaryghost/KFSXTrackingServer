/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.kfsxtrackingserver.ServerProperties.RequiredPropertyError
import java.nio.file.Paths
import org.junit.Test
import static org.junit.Assert.*

/**
 *
 * @author etsai
 */
public class ServerPropertiesUnitTest {

    public ServerPropertiesUnitTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    public void checkUdpPort() {
        def properties= new Properties()
        properties["udp.port"]= "12345"
        
        def serverProperties= new ServerProperties(properties)
        assertEquals(serverProperties.getUdpPort(), 12345)
    }
    @Test
    public void checkBlankUdpPort() {
        def properties= new Properties()
        def serverProperties= new ServerProperties(properties)
        assertEquals(serverProperties.getUdpPort(), 6000)
    }
    @Test
    public void checkInvalidUdpPort() {
        def properties= new Properties()
        properties["udp.port"]= "abcdefg"
        def serverProperties= new ServerProperties(properties)
        assertEquals(serverProperties.getUdpPort(), 6000)
    }
    @Test
    public void checkHttpPort() {
        def properties= new Properties()
        properties["http.port"]= "12345"
        def serverProperties= new ServerProperties(properties)
        assertEquals(serverProperties.getHttpPort(), 12345)
    }
    @Test
    public void checkBlankHttpPort() {
        def properties= new Properties()
        def serverProperties= new ServerProperties(properties)
        assertNull(serverProperties.getHttpPort())
    }
    @Test
    public void checkInvalidHttpPort() {
        def properties= new Properties()
        properties["http.port"]= "abcde"
        def serverProperties= new ServerProperties(properties)
        assertEquals(serverProperties.getHttpPort(), 8080)
    }
    @Test
    public void checkHttpRootDir() {
        def properties= new Properties()
        properties["http.root.dir"]= "mypath"
        def serverProperties= new ServerProperties(properties)
        
        def path= Paths.get("mypath")
        assertEquals(serverProperties.getHttpRootDir(), path)
    }
    @Test
    public void checkBlankHttpRootDir() {
        def properties= new Properties()
        def serverProperties= new ServerProperties(properties)
        
        def path= Paths.get("http")
        assertEquals(serverProperties.getHttpRootDir(), path)
    }
    @Test
    public void checkPassword() {
        def properties= new Properties()
        properties.password= "server"
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getPassword(), "server")
    }
    @Test(expected= RequiredPropertyError.class)
    public void checkBlankPassword() {
        def properties= new Properties()
        def serverProperties= new ServerProperties(properties)
        
        println serverProperties.getPassword()
    }
    @Test
    public void checkStatsMsgTTL() {
        def properties= new Properties()
        properties["stats.msg.ttl"]= "1000000"
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getStatsMsgTTL(), 1000000)
    }
    @Test
    public void checkBlankStatsMsgTTL() {
        def properties= new Properties()
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getStatsMsgTTL(), 60000)
    }
    @Test
    public void checkInvalidStatsMsgTTL() {
        def properties= new Properties()
        properties["stats.msg.ttl"]= "abcdefghi"
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getStatsMsgTTL(), 60000)
    }
    @Test
    public void checkNumDbConn() {
        def properties= new Properties()
        properties["num.db.conn"]= "20"
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getNumDbConn(), 20)
    }
    @Test
    public void checkMinNumDbConn() {
        def properties= new Properties()
        properties["num.db.conn"]= "1"
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getNumDbConn(), 2)
    }
    @Test
    public void checkInvalidNumDbConn() {
        def properties= new Properties()
        properties["num.db.conn"]= "abcdefg"
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getNumDbConn(), 10)
    }
    @Test
    public void checkSteamPollingThreads() {
        def properties= new Properties()
        properties["steam.polling.threads"]= "8"
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getSteamPollingThreads(), 8)
    }
    @Test
    public void checkBlankSteamPollingThreads() {
        def properties= new Properties()
        def serverProperties= new ServerProperties(properties)
        
        assertNull(serverProperties.getSteamPollingThreads())
    }
    @Test
    public void checkInvalidSteamPollingThreads() {
        def properties= new Properties()
        properties["steam.polling.threads"]= "abcdefg"
        def serverProperties= new ServerProperties(properties)
        
        assertEquals(serverProperties.getSteamPollingThreads(), 1)
    }
}
