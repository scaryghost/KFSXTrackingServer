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
public class Records {
    public static final def KEY_PAGE= "page"
    public static final def KEY_ROWS= "rows"
    public static final def DEFAULT_PAGE= 0
    public static final def DEFAULT_ROWS= 25

    public static String fillBody(def xmlBuilder, def queries) {
        def page= queries[KEY_PAGE] == null ? DEFAULT_PAGE : [queries[KEY_PAGE].toInteger(), DEFAULT_PAGE].max()
        def rows= queries[KEY_ROWS] == null ? DEFAULT_ROWS : queries[KEY_ROWS].toInteger()
        def start= page * rows
        def end= start + rows
        
        Common.sql.eachRow("SELECT count(*) FROM records") {row ->
            if (row[0] == 0) {
                start= 0
                end= 0
            } else {
                if (start >= row[0]) {
                    start= [0, row[0] - (row[0] % rows)].max()
                    page= (start / row[0]).toInteger() + 1
                }
                if (end >= row[0]) end= row[0]
            }
        }
        
        xmlBuilder.kfstatsx() {
            'records'(page: page, rows: rows) {
                def pos= start
                Common.sql.eachRow("SELECT * FROM records LIMIT ?,?", [start, end - start]) {row ->
                    def steamIdInfo= SteamIDInfo.getSteamIDInfo(row.steamid64)
                    def attr= [:]
                    
                    attr["pos"]= pos + 1
                    attr["steamid"]= row.steamid64
                    attr["name"]= steamIdInfo.name
                    attr["wins"]= row.wins
                    attr["losses"]= row.losses
                    attr["disconnects"]= row.disconnects
                    xmlBuilder.'record'(attr)
                    pos++
                }
            }
        }
    }
}

