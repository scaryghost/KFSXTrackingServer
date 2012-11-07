/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web
import com.github.etsai.kfsxtrackingserver.Common

/**
 *
 * @author eric
 */
public class Sessions {
    public static final def KEY_PAGE= "page"
    public static final def KEY_ROWS= "rows"
    public static final def KEY_STEAMID64= "steamid64"
    public static final def DEFAULT_PAGE= 0
    public static final def DEFAULT_ROWS= 25
    
    public static String fillBody(def xmlBuilder, def queries) {
        def steamid64= queries[KEY_STEAMID64]
        def page= queries[KEY_PAGE] == null ? DEFAULT_PAGE : [queries[KEY_PAGE].toInteger(), DEFAULT_PAGE].max()
        def rows= queries[KEY_ROWS] == null ? DEFAULT_ROWS : queries[KEY_ROWS].toInteger()
        def start= page * rows
        def end= start + rows
        
        Common.sql.eachRow("SELECT count(*) FROM sessions WHERE steamid64=?", [steamid64]) {row ->
            if (row[0] == 0) {
                start= 0
                end= 0
            } else {
                if (start >= row[0]) {
                    start= [0, row[0] - (row[0] % rows)].max()
                    page= (row[0] / rows).toInteger()
                }
                if (end >= row[0]) end= row[0]
            }
        }
        
        xmlBuilder.kfstatsx() {
            'stats'(category: "sessions", steamid64: steamid64, page: page, rows: rows) {
                
                Common.sql.eachRow("SELECT * FROM sessions WHERE steamid64=? ORDER BY timestamp DESC LIMIT ?,?", [steamid64, start, end - start]) {row ->
                    def result= row.toRowResult()
                    
                    result.remove("steamid64")
                    'entry'(result)
                }
            }
        }
    }
}

