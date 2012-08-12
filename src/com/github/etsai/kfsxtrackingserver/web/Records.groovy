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
    
    public static final def KEY_PAGE= "page"
    public static final def KEY_ROWS= "rows"
    public static final def DEFAULT_PAGE= 1
    public static final def DEFAULT_ROWS= 50
    private def page, rows
    
    public def Records(def queries) {
        page= queries[KEY_PAGE] == null ? DEFAULT_PAGE : [queries[KEY_PAGE].toInteger(), 1].max()
        rows= queries[KEY_ROWS] == null ? DEFAULT_ROWS : queries[KEY_ROWS].toInteger()
        
        println "page: ${page} -> rows: ${rows}"
    }
    
    public String fillBody(def xmlBuilder) {
        def start, end
        def allRecords= statsData.getRecords().toArray()
        
        start= (page-1)* rows
        end= start + rows
        if (start >= allRecords.size()) start= [0, allRecords.size() - rows].max()
        if (end >= allRecords.size()) end= allRecords.size()-1
        
        xmlBuilder.kfstatsx() {
            'records'(page: page, rows: rows) {
                (start .. end).each {index ->
                    def record= allRecords[index]
                    def steamIdInfo= getSteamId(record.getSteamId())
                    def attr= [:]
                    
                    attr["pos"]= index+1
                    attr["steamid"]= steamIdInfo.steamid
                    attr["name"]= steamIdInfo.name
                    attr["wins"]= record.getWins()
                    attr["losses"]= record.getLosses()
                    attr["disconnects"]= record.getDisconnects()
                    xmlBuilder.'record'(attr)
                }
            }
        }
    }
}

