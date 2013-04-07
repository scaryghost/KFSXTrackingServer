/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web
import com.github.etsai.kfsxtrackingserver.Common

/**
 * Generates the xml for a player's session history
 * @author etsai
 */
public class Sessions {
    public static String fillBody(def xmlBuilder, def queries) {
        def queryValues= Queries.parseQuery(queries)

        xmlBuilder.kfstatsx() {
            def attrs= [category: "sessions", steamid64: queryValues[Queries.steamid64], page: queryValues[Queries.page], rows: queryValues[Queries.rows], 
                query: "&group=${queryValues[Queries.group]}&order=${queryValues[Queries.order]}"]

            ["level", "difficulty", "length", "result", "wave", "timestamp"].each {col ->
                if (col == queryValues[Queries.group] && queryValues[Queries.order] == "desc") {
                    attrs[col]= "asc"
                } else {
                    attrs[col]= "desc"
                }
            }
            'stats'(attrs) {
                WebCommon.partialQuery(queryValues, "SELECT * FROM sessions WHERE steamid64=? ", 
                        [queryValues[Queries.steamid64]], {row ->
                    def result= row.toRowResult()
                    
                    result.remove("steamid64")
                    'entry'(result)
                })
            }
        }
    }
}

