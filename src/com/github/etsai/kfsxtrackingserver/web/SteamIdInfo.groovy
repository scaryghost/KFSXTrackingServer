/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import java.util.logging.Level

/**
 *
 * @author etsai
 */
public class SteamIdInfo {
    private def valid
    
    public final def steamid
    public def name, avatarFull, avatarMedium, avatarSmall
    
    public SteamIdInfo(String steamid) {
        this.steamid= steamid
        
        try  {
            def url= new URL("http://steamcommunity.com/profiles/${steamid}?xml=1")
            def content= url.getContent().readLines().join("\n")
            def steamXmlRoot= new XmlSlurper().parseText(content)
            
            if (steamXmlRoot.error != "") {
                valid= false
            } else {
                name= steamXmlRoot.steamID.text()
                avatarFull= steamXmlRoot.avatarFull.text()
                avatarMedium= steamXmlRoot.avatarMedium.text()
                avatarSmall= steamXmlRoot.avatarIcon.text()
                valid= true
            }
        } catch (Exception ex) {
            Common.logger.log(Level.SEVERE, "Error polling steamcommunity.com", ex);
            valid= false
        }
    }
    
    public boolean isValid() {
        return valid
    }
}

