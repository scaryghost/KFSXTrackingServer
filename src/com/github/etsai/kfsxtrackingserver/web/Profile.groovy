/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData

/**
 *
 * @author eric
 */
public class Profile extends Page {
    private final def steamid
    
    public Profile(def steamid) {
        this.steamid= steamid
    }
    
    public String fillBody(def xmlBuilder) {
        def record= statsData.getRecord(steamid)
        
        xmlBuilder.kfstatsx() {
            def url= new URL("http://steamcommunity.com/profiles/${steamid}?xml=1")
            def steamXmlRoot= new XmlSlurper().parseText(url.getContent().readLines().join("\n"))
                
            def profileAttr= [:]
            profileAttr["steamid"]= steamid
            profileAttr["name"]= steamXmlRoot.steamID.text()
            profileAttr["avatar"]= steamXmlRoot.avatarMedium.text()
            profileAttr["wins"]= record.getWins()
            profileAttr["losses"]= record.getLosses()
            profileAttr["disconnects"]= record.getDisconnects()
            
            'profile'(profileAttr) {
                statsData.getPlayerStats(steamid).each {cat, player ->
                    'stats'(category: cat) {
                        player.getStats().each {stat, value ->
                            def attr= [:]
                            attr["name"]= stat
                            attr["value"]= value
                            xmlBuilder.'stat'(attr)
                        }
                        
                    }
                }
            }
        }
    }
}

