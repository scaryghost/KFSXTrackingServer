/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import java.util.TimerTask
import java.util.logging.Level
import java.nio.charset.Charset

/**
 *
 * @author etsai
 */
public class SteamIDInfo {
    public def name, avatar
    
    public synchronized static def getSteamIDInfo(def steamID64) throws RuntimeException {
        def row= Common.sql.firstRow("select * from steaminfo where steamid64=?", [steamID64])
        
        if (row == null) {
            Common.logger.info("Polling steamcommunity for steamID64: ${steamID64}")
            try {
                def url= new URL("http://steamcommunity.com/profiles/${steamID64}?xml=1")
                def content= url.getContent().readLines().join("\n")
                def steamXmlRoot= new XmlSlurper().parseText(content)
                def name, avatar
            
                if (steamXmlRoot.error != "") {
                    throw new RuntimeException("Invalid steamID64: $steamID64")
                } else if (steamXmlRoot.privacyMessage != "") {
                    name= "---Profile not setup---"
                } else {
                    def tempName= steamXmlRoot.steamID.text()
                    name= new String(tempName.getBytes(Charset.availableCharsets()["US-ASCII"]))
                    avatar= steamXmlRoot.avatarMedium
                }
                
                Common.sql.execute("insert into steaminfo values (?, ?, ?);", [steamID64, name, avatar])
                return new SteamIDInfo(name: name, avatar: avatar)
            } catch (IOException ex) {
                Common.logger.log(Level.SEVERE, "Error polling steamcommunity.com", ex)
                return new SteamIDInfo()
            }
        }
        return new SteamIDInfo(name: row.name, avatar: row.avatar)
    }
}

