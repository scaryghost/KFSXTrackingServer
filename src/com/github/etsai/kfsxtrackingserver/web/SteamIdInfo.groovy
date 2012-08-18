/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.ServerProperties.*
import com.github.etsai.kfsxtrackingserver.Common
import java.util.logging.Level
import java.nio.charset.Charset

/**
 *
 * @author etsai
 */
public class SteamIdInfo {
    public static final def charset= "US-ASCII"
    
    private def valid, expired
    
    public final def steamid
    public def name, avatarFull, avatarMedium, avatarSmall
    
    public SteamIdInfo(String steamid) {
        this.steamid= steamid
        
        expired= Calendar.getInstance()
        try  {
            def url= new URL("http://steamcommunity.com/profiles/${steamid}?xml=1")
            def content= url.getContent().readLines().join("\n")
            def steamXmlRoot= new XmlSlurper().parseText(content)
            
            valid= true
            if (steamXmlRoot.error != "") {
                valid= false
            } else if (steamXmlRoot.privacyMessage != "") {
                name= "null"
                avatarFull= ""
                avatarMedium= ""
                avatarSmall= ""
            } else {
                def tempName= steamXmlRoot.steamID.text()
                
                name= new String(tempName.getBytes(Charset.availableCharsets()[charset]))
                avatarFull= steamXmlRoot.avatarFull.text()
                avatarMedium= steamXmlRoot.avatarMedium.text()
                avatarSmall= steamXmlRoot.avatarIcon.text()
            }
            
            expired.add(Calendar.MILLISECOND, 
                Common.properties[propSteamPollingPeriod].toInteger())
        } catch (Exception ex) {
            Common.logger.log(Level.SEVERE, "Error polling steamcommunity.com", ex);
            name= "null"
        }
    }
    
    public boolean isValid() {
        return valid
    }
    
    public boolean isExpired() {
        return expired.compareTo(Calendar.getInstance()) > 0
    }
}

