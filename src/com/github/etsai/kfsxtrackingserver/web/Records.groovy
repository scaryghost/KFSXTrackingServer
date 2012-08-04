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
public class Records extends Page {
    private static def steamIdInfo= Collections.synchronizedMap([:])
    
    public static def getSteamId(def steamid) {
        if (steamIdInfo[steamid] == null) {
            steamIdInfo[steamid]= new SteamIdInfo(steamid)
        } else if (!steamIdInfo[steamid].isValid()) {
            steamIdInfo[steamid]= null
            steamIdInfo[steamid]= new SteamIdInfo(steamid)
        }
        return steamIdInfo[steamid]
    }
    public String fillBody(def xmlBuilder) {
        xmlBuilder.kfstatsx() {
            'records'() {
                statsData.getRecords().each {record ->
                    def steamIdInfo= getSteamId(record.getSteamId())
                    def attr= [:]
                    
                    attr["steamid"]= steamIdInfo.steamid
                    attr["name"]= steamIdInfo.name
                    attr["avatar"]= steamIdInfo.avatarSmall
                    attr["wins"]= record.getWins()
                    attr["losses"]= record.getLosses()
                    attr["disconnects"]= record.getDisconnects()
                    xmlBuilder.'record'(attr)
                }
            }
        }
    }
}

