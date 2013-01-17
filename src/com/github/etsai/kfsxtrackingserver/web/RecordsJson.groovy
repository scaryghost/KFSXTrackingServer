package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import groovy.json.JsonBuilder

public class RecordsJson {
    private static def columns= [["Name", "string"], ["Wins", "numbers"], ["Losses", "number"], ["Disconnects", "number"]].collect {
        [label: it[0], type: it[1]]
    }
    public static String fillBody(def queries) {
        def params= queries["tq"].tokenize(",")
        def page= [params[0].toInteger(), 0].max()
        def pageSize= params[1].toInteger()
        def start= page * pageSize
        def end= start + pageSize
        
        Common.sql.eachRow("SELECT count(*) FROM records") {row ->
            if (row[0] == 0) {
                start= 0
                end= 0
            } else {
                if (start >= row[0]) {
                    start= [0, row[0] - (row[0] % pageSize)].max()
                    page= (row[0] / pageSize).toInteger()
                }
                if (end >= row[0]) end= row[0]
            }
        }
        
        def psValues= [start, end - start] 
        def sql= """SELECT s.name,r.wins,r.losses,r.disconnects,r.steamid64 FROM records r INNER JOIN steaminfo s 
                ON r.steamid64=s.steamid64 """

        if (params.size() >= 4) {
            sql+= "ORDER BY ${columns[params[2].toInteger()]['label']} ${params[3]} "
        }
        sql+= "LIMIT ?,?"

        def records= []
        Common.logger.finest(sql)
        Common.sql.eachRow(sql, psValues) {row ->
            records << [c: [[v: row.name, f: "<a href=profile.xml?steamid64=${row.steamid64}>${row.name}</a>", p: null], 
                [v: row.wins, f: null, p:[style: "text-align:center"]],
                [v: row.losses, f: null, p:[style: "text-align:center"]],
                [v: row.disconnects, f: null, p:[style: "text-align:center"]]]]
        }

        def builder= new JsonBuilder()
        def root= builder {
            cols(columns)
            rows(records)
        }

        return "google.visualization.Query.setResponse({version: '0.6', reqId: '0', status: 'ok', table:$builder});"
    }
}
