package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import groovy.json.JsonBuilder

public class SessionsJson {
    private static def columns= [["Level", "string"], ["Difficulty", "string"], ["Length", "string"],
            ["Result", "string"], ["Wave", "number"], ["Timestamp", "string"]].collect {
        [label: it[0], type: it[1]]
    }
    public static String fillBody(def queries) {
        def steamid64= queries["steamid64"]
        def params= queries["tq"].tokenize(",")

        def page= [params[0].toInteger(), 0].max()
        def pageSize= params[1].toInteger()
        def start= page * pageSize
        def end= start + pageSize

        WebCommon.adjustStartEnd("SELECT count(*) FROM sessions WHERE steamid64=?", [steamid64], start, end)
        
        def psValues= [steamid64, start, end - start] 
        def sql= "SELECT * FROM sessions WHERE steamid64=?" 
        if (params.size() >= 4) {
            sql+= "ORDER BY ${columns[params[2].toInteger()]['label']} ${params[3]} "
        }
        sql+= "LIMIT ?,?"

        def records= []
        Common.logger.finest(sql)
        Common.sql.eachRow(sql, psValues) {row ->
            records << [c: [[v: row.level, f:null, p: null], 
                [v: row.difficulty, f: null, p:[style: "text-align:center"]],
                [v: row.length, f: null, p:[style: "text-align:center"]],
                [v: row.result, f: null, p:[style: "text-align:center"]],
                [v: row.wave, f: null, p:[style: "text-align:center"]],
                [v: row.timestamp, f: null, p:[style: "text-align:center"]],
            ]]
        }

        def builder= new JsonBuilder()
        def root= builder {
            cols(columns)
            rows(records)
        }

        return "google.visualization.Query.setResponse({version: '0.6', reqId: '0', status: 'ok', table:$builder});"
    }
}
