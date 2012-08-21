/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common

/**
 *
 * @author etsai
 */
public class Records extends Page {
    public static final def KEY_PAGE= "page"
    public static final def KEY_ROWS= "rows"
    public static final def DEFAULT_PAGE= 0
    public static final def DEFAULT_ROWS= 25
    private def page, rows
    
    public def Records(def queries) {
        page= queries[KEY_PAGE] == null ? DEFAULT_PAGE : [queries[KEY_PAGE].toInteger(), DEFAULT_PAGE].max()
        rows= queries[KEY_ROWS] == null ? DEFAULT_ROWS : queries[KEY_ROWS].toInteger()
    }
    
    public String fillBody(def xmlBuilder) {
        def allRecords= Common.statsData.getRecords().toArray()
        def start= page * rows
        def end= start + rows
        def numRecords= allRecords.size()
        
        if (numRecords == 0) {
            start= 0
            end= 0
        } else {
            if (start >= numRecords) {
                start= [0, numRecords - (numRecords % rows)].max()
                page= (start / numRecords).toInteger() + 1
            }
            if (end >= numRecords) end= numRecords
        }
        
        xmlBuilder.kfstatsx() {
            'records'(page: page, rows: rows) {
                (start ..< end).each {index ->
                    def record= allRecords[index]
                    def steamIdInfo= SteamIdInfo.getSteamIDInfo(record.getSteamId())
                    def attr= [:]
                    
                    attr["pos"]= index+1
                    attr["steamid"]= steamIdInfo.steamID64
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

