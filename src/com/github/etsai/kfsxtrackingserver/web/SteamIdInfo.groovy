/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.ServerProperties.*
import com.github.etsai.kfsxtrackingserver.Common
import java.util.TimerTask
import java.util.logging.Level
import java.nio.charset.Charset

/**
 *
 * @author etsai
 */
public class SteamIdInfo {
    public static final def charset= "US-ASCII"
    private static def polledSteamIDs= Collections.synchronizedMap([:])
    
    public static def getSteamIDInfo(def steamID64) {
        if (polledSteamIDs[steamID64] == null || polledSteamIDs[steamID64].repoll) {
            def th= new Thread(new SteamPoller(steamID64:steamID64))
            th.start()
            th.join()
        } 
        return polledSteamIDs[steamID64]
    }
    
    static class SteamPoller implements Runnable {
        public def steamID64
        
        @Override
        public void run() {
            def newId= new SteamIdInfo(steamID64)
            
            Common.logger.info("Polling steamcommunity for steamID64: ${steamID64}")
            try  {
                def url= new URL("http://steamcommunity.com/profiles/${steamID64}?xml=1")
                def content= url.getContent().readLines().join("\n")
                def steamXmlRoot= new XmlSlurper().parseText(content)
            
                newId.valid= true
                if (steamXmlRoot.error != "") {
                    newId.valid= false
                } else if (steamXmlRoot.privacyMessage != "") {
                    newId.name= "null"
                } else {
                    def tempName= steamXmlRoot.steamID.text()

                    newId.name= new String(tempName.getBytes(Charset.availableCharsets()[charset]))
                    newId.avatarFull= steamXmlRoot.avatarFull.text()
                    newId.avatarMedium= steamXmlRoot.avatarMedium.text()
                    newId.avatarSmall= steamXmlRoot.avatarIcon.text()
                }
                
                polledSteamIDs[steamID64]= newId
            } catch (Exception ex) {
                Common.logger.log(Level.SEVERE, "Error polling steamcommunity.com", ex)
                
                newId.repoll= true
                if (polledSteamIDs[steamID64] == null) {
                    polledSteamIDs[steamID64]= newId
                }
            }
        }
    }
    
    public static class SteamIDUpdater extends TimerTask {
        @Override
        public void run() {
            Common.statsData.getRecords().each {record ->
                def poller= new SteamPoller(steamID64: record.getSteamId())
                Common.pool.submit poller
            }
        }
    }
    
    private def valid, repoll= false
    public final def steamID64
    public def name, avatarFull, avatarMedium, avatarSmall
    
    private SteamIdInfo(String steamID64) {
        this.steamID64= steamID64
    }
    
    public boolean isValid() {
        return valid
    }
    
    public boolean repoll() {
        return repoll
    }
}

