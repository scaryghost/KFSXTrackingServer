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
    public enum GetKeys {
        page,
        rows,
        order,
        group
    };

    public static final def defaults= [(GetKeys.page): 0, (GetKeys.rows): 25, (GetKeys.order): "none", (GetKeys.group): "name"]

    public static String fillBody(def xmlBuilder, def queries) {
        def getValues= [:]

        GetKeys.values().each {key ->
            def keyStr= key.toString()
            getValues[key]= queries[keyStr] == null ? defaults[key] : queries[keyStr]
        }
        def page= [getValues[GetKeys.page].toInteger(), defaults[GetKeys.page]].max()
        def rows= getValues[GetKeys.rows].toInteger()
        def order= queries
        def start= page * rows
        def end= start + rows
        
        Common.sql.eachRow("SELECT count(*) FROM records") {row ->
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
            'records'(page: page, rows: rows) {
                def pos= start
                def psValues= [start, end - start] 
                def sql= "SELECT * FROM records " 

                if (getValues[GetKeys.order] != defaults[GetKeys.order]) {
                    sql+= "ORDER BY ${getValues[GetKeys.group]} ${getValues[GetKeys.order]}  "
                }
                sql+= "LIMIT ?,?"

                Common.logger.info(sql)
                Common.sql.eachRow(sql, psValues) {row ->
                    def steamIdInfo= SteamIDInfo.getSteamIDInfo(row.steamid64)
                    def attr= [:]
                    
                    attr["pos"]= pos + 1
                    attr["steamid64"]= row.steamid64
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

