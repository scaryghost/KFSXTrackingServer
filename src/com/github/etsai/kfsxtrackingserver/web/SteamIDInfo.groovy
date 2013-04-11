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
}

