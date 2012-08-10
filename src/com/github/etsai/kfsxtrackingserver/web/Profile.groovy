/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData

/**
 *
 * @author etsai
 */
public class Profile extends Page {
    private final def steamid
    
    public Profile(def steamid) {
        this.steamid= steamid
    }
    
    public String fillBody(def xmlBuilder) {
        def record= statsData.getRecord(steamid)
        
        xmlBuilder.kfstatsx() {
            def profileAttr= [:]
            def steamIdInfo= Records.getSteamId(steamid)
            
            profileAttr["steamid"]= steamid
            profileAttr["name"]= steamIdInfo.name
            profileAttr["avatar"]= steamIdInfo.avatarMedium
            profileAttr["wins"]= record.getWins()
            profileAttr["losses"]= record.getLosses()
            profileAttr["disconnects"]= record.getDisconnects()
            
            'profile'(profileAttr) {
                statsData.getPlayerStats(steamid).each {cat, player ->
                    'stats'(category: cat) {
                        player.getStats().sort{it.key}.each {stat, value ->
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

