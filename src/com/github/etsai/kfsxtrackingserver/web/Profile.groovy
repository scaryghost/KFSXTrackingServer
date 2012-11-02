/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.sql
import com.github.etsai.utils.Time

/**
 *
 * @author etsai
 */
public class Profile extends Page {
    public static final KEY_STEAMID= "steamid"
    private final def steamid64
    
    public Profile(def queries) {
        steamid64= queries[KEY_STEAMID]
    }
    
    public String fillBody(def xmlBuilder) {
        def profileAttr= null
        def steamIdInfo= SteamIdInfo.getSteamIDInfo(steamid64)
        
        sql.eachRow("SELECT * FROM records where steamid64=?", [steamid64]) {row ->
            profileAttr= [:]
            profileAttr["steamid"]= steamid64
            profileAttr["name"]= steamIdInfo.name
            profileAttr["avatar"]= steamIdInfo.avatar
            profileAttr["wins"]= row.wins
            profileAttr["losses"]= row.losses
            profileAttr["disconnects"]= row.disconnects
        }
        
        xmlBuilder.kfstatsx() {
            if (profileAttr == null) {
                'error'("No stats available for steamdID64: ${steamid64}")
            } else {
                'profile'(profileAttr) {
                    sql.eachRow("SELECT category FROM player where steamid64=? group by category", [steamid64]) {row1 ->
                        'stats'(category: row1.category) {
                            sql.eachRow("SELECT * FROM player where steamid64=? AND category=?", [steamid64, row1[0]]) {row ->
                                def attr= [:]
                                attr["name"]= row.stat
                                attr["value"]= row.value

                                if (row1.category == "perks") {
                                    attr["hint"]= new Time(attr["value"]).toString()
                                } else if (attr["name"].toLowerCase().contains("time")) {
                                    attr["value"]= new Time(attr["value"]).toString()
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

