/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common

/**
 * Generates the records page
 * @author etsai
 */
public class Records {
    public static String fillBody(def xmlBuilder, def queries) {
        def queryValues= Queries.parseQuery(queries)
        def page= [queryValues[Queries.page].toInteger(), Queries.defaults[Queries.page]].max()
        def rows= queryValues[Queries.rows].toInteger()
        def start= page * rows
        def end= start + rows

        WebCommon.adjustStartEnd("SELECT count(*) FROM records", [], start, end)
        
        xmlBuilder.kfstatsx() {
            def attrs= [page: page, rows: rows, query: "&group=${queryValues[Queries.group]}&order=${queryValues[Queries.order]}"]

            ["name", "wins", "losses", "disconnects"].each {col ->
                if (col == queryValues[Queries.group] && queryValues[Queries.order] == "desc") {
                    attrs[col]= "asc"
                } else {
                    attrs[col]= "desc"
                }
            }
            
            'records'(attrs) {
                def pos= start
                def psValues= [start, end - start] 
                def sql= "SELECT r.steamid64,r.wins,r.losses,r.disconnects,s.name FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64 " 

                if (queryValues[Queries.group] != Queries.defaults[Queries.group]) {
                    sql+= "ORDER BY ${queryValues[Queries.group]} ${queryValues[Queries.order]} "
                }
                sql+= "LIMIT ?,?"

                Common.logger.finest(sql)
                Common.sql.eachRow(sql, psValues) {row ->
                    def attr= [:]
                    
                    attr["pos"]= pos + 1
                    attr["steamid64"]= row.steamid64
                    attr["name"]= row.name
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

