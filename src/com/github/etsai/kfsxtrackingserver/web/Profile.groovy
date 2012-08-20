/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData
import com.github.etsai.kfsxtrackingserver.Time

/**
 *
 * @author etsai
 */
public class Profile extends Page {
    public static final KEY_STEAMID= "steamid"
    private final def steamid
    
    public Profile(def queries) {
        steamid= queries[KEY_STEAMID]
    }
    
    public String fillBody(def xmlBuilder) {
        def record= statsData.getRecord(steamid)
        
        xmlBuilder.kfstatsx() {
            def profileAttr= [:]
            def steamIdInfo= SteamIdInfo.getSteamIDInfo(steamid)
            
            if (record == null) {
                'error'("No stats available for steamdID64: ${steamid}")
            } else {
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
                                
                                if (cat == "perks") {
                                    attr["hint"]= new Time(attr["value"].toInteger()).toString()
                                } else if (attr["name"].contains("time")) {
                                    attr["value"]= new Time(attr["value"].toInteger()).toString()
                                }                
                                xmlBuilder.'entry'(attr)
                            }

                        }
                    }
                }
            }
        }
    }
}

