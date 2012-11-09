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
    
    private static def poll(def steamID64) {
        def url= new URL("http://steamcommunity.com/profiles/${steamID64}?xml=1")
        def content= url.getContent().readLines().join("\n")
        def steamXmlRoot= new XmlSlurper().parseText(content)
        def name, avatar

        Common.logger.finest("Polling steamcommunity for steamID64: ${steamID64}")
        if (steamXmlRoot.error != "") {
            throw new RuntimeException("Invalid steamID64: $steamID64")
        } else if (steamXmlRoot.privacyMessage != "") {
            name= "---Profile not setup---"
        } else {
            def tempName= steamXmlRoot.steamID.text()
            name= new String(tempName.getBytes(Charset.availableCharsets()["US-ASCII"]))
            avatar= steamXmlRoot.avatarMedium
        }
        return new SteamIDInfo(name: name, avatar: avatar)
    }
    
    public synchronized static def getSteamIDInfo(def steamID64) throws RuntimeException {
        def row= Common.sql.firstRow("select * from steaminfo where steamid64=?", [steamID64])
        
        if (row == null) {
            try {
                def info= poll(steamID64)
                    Common.sql.execute("insert into steaminfo values (?, ?, ?);", [steamID64, info.name, info.avatar])
                return info
            } catch (IOException ex) {
                Common.logger.log(Level.SEVERE, "Error polling steamcommunity.com", ex)
                return new SteamIDInfo()
            }
        }
        return new SteamIDInfo(name: row.name, avatar: row.avatar)
    }
    
    public static def verifySteamID64(def steamID64) {
        try {
            def info= poll(steamID64)
                Common.sql.execute("insert or ignore into steaminfo values (?, ?, ?)", [steamID64, "null", "null"])
                Common.sql.execute("update steaminfo set name=?, avatar=? where steamid64=?", [info.name, info.avatar, steamID64])
            return true;
        } catch (RuntimeException ex) {
            return false;
        } catch (IOException ex) {
            return true;
        }
    }
}

