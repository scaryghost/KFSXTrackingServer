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
    public enum GetKeys {
        page,
        rows,
        steamid64,
        order,
        group
    };

    public static final def defaults= [(GetKeys.page): 0, (GetKeys.rows): 25, (GetKeys.order): "asc", (GetKeys.group): "none"]
    
    public static String fillBody(def xmlBuilder, def queries) {
        def getValues= [:]

        GetKeys.values().each {key ->
            def keyStr= key.toString()
            getValues[key]= queries[keyStr] == null ? defaults[key] : queries[keyStr]
        }

        def page= [getValues[GetKeys.page].toInteger(), defaults[GetKeys.page]].max()
        def rows= getValues[GetKeys.rows].toInteger()
        def start= page * rows
        def end= start + rows
        
        Common.sql.eachRow("SELECT count(*) FROM sessions WHERE steamid64=?", [getValues[GetKeys.steamid64]]) {row ->
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
            def attrs= [category: "sessions", steamid64: getValues[GetKeys.steamid64], page: page, rows: rows, 
                query: "&group=${getValues[GetKeys.group]}&order=${getValues[GetKeys.order]}"]

            ["level", "difficulty", "length", "result", "wave", "timestamp"].each {col ->
                if (col == getValues[GetKeys.group] && getValues[GetKeys.order] == "desc") {
                    attrs[col]= "asc"
                } else {
                    attrs[col]= "desc"
                }
            }
            'stats'(attrs) {
                def sql= "SELECT * FROM sessions WHERE steamid64=? "

                if (getValues[GetKeys.group] != defaults[GetKeys.group]) {
                    sql+= "ORDER BY ${getValues[GetKeys.group]} ${getValues[GetKeys.order]} "
                }
                sql+= "LIMIT ?,?"
                
                Common.logger.finest(sql)
                Common.sql.eachRow(sql, [getValues[GetKeys.steamid64], start, end - start]) {row ->
                    def result= row.toRowResult()
                    
                    result.remove("steamid64")
                    'entry'(result)
                }
            }
        }
    }
}

