package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common

public class WebCommon {
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
