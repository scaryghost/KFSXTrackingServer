/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common


/**
 * Generates the html data for the page data.html
 * @author etsai
 */
public class DataHtml {
    private static def colStyle= "text-align:center"
    
    public static String fillBody(def queries) {
        def data= ""
        def queryValues= Queries.parseQuery(queries)
        
        switch(queryValues[Queries.table]) {
            case "totals":
                data= "<center><table class='content-table'><tbody>"
                WebCommon.generateSummary().each {attr ->
                    data+= "<tr><td>${attr['name']}</td><td>${attr['value']}</td></tr>"
                    }
                data+= "</tbody></table></center>"
                break
            case "profile":
                def steamid64= queryValues[Queries.steamid64]
                def row=  Common.sql.firstRow("SELECT * FROM records where steamid64=?", [steamid64])

                if (row == null) {
                    data= "<center>No records found for SteamID64: <a href='http://steamcommunity.com/profiles/${steamid64}'>$steamid64</a></center>"
                } else {
                    def steamIdInfo= SteamIDInfo.getSteamIDInfo(steamid64)
                    data= "<center><table class='content-table'><tbody>"

                    data+= "<tr><td>Name</td><td>${steamIdInfo.name}</td></tr>"
                    data+= "<tr><td>Wins</td><td>${row.wins}</td><td rowspan='4'><img src='${steamIdInfo.avatar}' /></td>"
                    data+= "</tr><tr><td>Losses</td><td>${row.losses}</td></tr><tr><td>Disconnects</td>"
                    data+= "<td>${row.disconnects}</td></tr><tr><td>Steam Community</td><td><a target='_blank' href='"
                    data+= "http://steamcommunity.com/profiles/${steamid64}'>${steamid64}</a></td></tr>";
                    data+= "</tbody></table></center>"
                }
                break
        }
        return data
    }
}

