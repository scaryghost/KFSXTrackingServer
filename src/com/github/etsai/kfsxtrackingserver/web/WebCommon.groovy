package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.utils.Time

public class WebCommon {

    public static def generateSummary() {
        def games= 0, playTime= 0, playerCount
        Common.sql.eachRow('select * from difficulties') {row ->
                games+= row.wins + row.losses
                playTime+= row.time
        }
        Common.sql.eachRow('SELECT count(*) FROM records') {row ->
            playerCount= row[0]
        }
        return [["Games", games], ["Play Time", Time.secToStr(playTime)], ["Player Count", playerCount]].collect {
            [name: it[0], value: it[0]]
        }
    }
    public static def generateResponse(def jsonData) {
        return "google.visualization.Query.setResponse({version: '0.6', reqId: '0', status: 'ok', table: $jsonData});"
    }
    
    public static void adjustStartEnd(def sql, def psValues, def start, def end) {
        Common.sql.eachRow(sql, psValues) {row ->
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
    }
}
