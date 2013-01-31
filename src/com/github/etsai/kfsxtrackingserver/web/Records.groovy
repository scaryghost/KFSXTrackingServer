/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common

/**
 * Generates the records.xml page
 * @author etsai
 */
public class Records {
    public static String fillBody(def xmlBuilder, def queries) {
        def queryValues= Queries.parseQuery(queries)

        xmlBuilder.kfstatsx() {
            def attrs= [page: queryValues[Queries.page], rows: queryValues[Queries.rows], query: "&group=${queryValues[Queries.group]}&order=${queryValues[Queries.order]}"]
            def pos= queryValues[Queries.page].toInteger() * queryValues[Queries.rows].toInteger()

            ["name", "wins", "losses", "disconnects"].each {col ->
                if (col == queryValues[Queries.group] && queryValues[Queries.order] == "desc") {
                    attrs[col]= "asc"
                } else {
                    attrs[col]= "desc"
                }
            }
            
            'records'(attrs) {
                WebCommon.partialQuery(queryValues, 
                        "SELECT r.steamid64,r.wins,r.losses,r.disconnects,s.name FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64 ", [], {row ->
                    def result= row.toRowResult()
                    result["pos"]= pos + 1

                    xmlBuilder.'record'(result)
                    pos++
                })
            }
        }
    }
}

