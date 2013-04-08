/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.web.Resource
import com.github.etsai.utils.Time
import groovy.json.JsonBuilder
import groovy.sql.Sql

/**
 * Generates the json data for the page data.json
 * @author etsai
 */
public class DataJson implements Resource {
    private static def colStyle= "text-align:center"
    
    public String generatePage(Sql sql, Map<String, String> queries) {
        def columns
        def data= []
        def builder= new JsonBuilder()
        def queryValues= Queries.parseQuery(queries)

        switch(queryValues[Queries.table]) {
            case "difficulties":
                def totals= [wins: 0, losses: 0, time: 0]
                columns= [["Name", "string"], ["Length", "string"], ["Wins", "number"],
                    ["Losses", "number"], ["Avg Wave", "number"], ["Time", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                sql.eachRow('select * from difficulties') {row ->
                    data << [c: [[v: row.name, f:null, p: null], 
                        [v: row.length, f: null, p:[style: colStyle]],
                        [v: row.wins, f: null, p:[style: colStyle]],
                        [v: row.losses, f: null, p:[style: colStyle]],
                        [v: String.format("%.2f",row.wave / (row.wins + row.losses)), f: null, p:[style: colStyle]],
                        [v: row.time, f: Time.secToStr(row.time), p:[style: colStyle]]
                    ]]
                    totals["wins"]+= row.wins
                    totals["losses"]+= row.losses
                    totals["time"]+= row.time
                }
                data << [c: [[v: "Totals", f:null, p: null], 
                    [v: "", f: "---", p:[style: colStyle]],
                    [v: totals["wins"], f: null, p:[style: colStyle]],
                    [v: totals["losses"], f: null, p:[style: colStyle]],
                    [v: 0, f: "---", p:[style: colStyle]],
                    [v: totals["time"], f: Time.secToStr(totals["time"]), p:[style: colStyle]],
                ]]
               break
            case "levels":
                def totals= [wins: 0, losses: 0, time: 0]
                columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Time", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                sql.eachRow('select * from levels') {row ->
                    data << [c: [[v: row.name, f:null, p: null], 
                        [v: row.wins, f: null, p:[style: colStyle]],
                        [v: row.losses, f: null, p:[style: colStyle]],
                        [v: row.time, f: Time.secToStr(row.time), p:[style: colStyle]],
                    ]]
                    totals["wins"]+= row.wins
                    totals["losses"]+= row.losses
                    totals["time"]+= row.time.toInteger()
                }
                data << [c: [[v: "Totals", f:null, p: null], 
                    [v: totals["wins"], f: null, p:[style: colStyle]],
                    [v: totals["losses"], f: null, p:[style: colStyle]],
                    [v: totals["time"], f: Time.secToStr(totals["time"]), p:[style: colStyle]],
                ]]
                break
            case "deaths":
                columns= [["Death", "string"], ["Count", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                sql.eachRow('select * from deaths ORDER BY name ASC') {row ->
                    data << [c: [[v: row.name, f:null, p: null], 
                        [v: row.count, f: null, p:[style: colStyle]],
                    ]]
                }
                break
            case "records":
                columns= [["Name", "string"], ["Wins", "numbers"], ["Losses", "number"], ["Disconnects", "number"]].collect {
                    [label: it[0], type: it[1]]
                }

                WebCommon.partialQuery(sql, queryValues, "SELECT s.name,r.wins,r.losses,r.disconnects,r.steamid64 FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64", 
                    [], {row ->
                    data << [c: [[v: row.name, f: "<a href=profile.html?steamid64=${row.steamid64}>${row.name}</a>", p: null], 
                        [v: row.wins, f: null, p:[style: colStyle]],
                        [v: row.losses, f: null, p:[style: colStyle]],
                        [v: row.disconnects, f: null, p:[style: colStyle]]]]
                })
                break
            case "sessions":
                columns= [["Level", "string"], ["Difficulty", "string"], ["Length", "string"],
                        ["Result", "string"], ["Wave", "number"], ["Timestamp", "string"]].collect {
                    [label: it[0], type: it[1]]
                }
                WebCommon.partialQuery(sql, queryValues, "SELECT * FROM sessions WHERE steamid64=?", [queryValues[Queries.steamid64]], {row ->
                    data << [c: [[v: row.level, f:null, p: null], 
                        [v: row.difficulty, f: null, p:[style: colStyle]],
                        [v: row.length, f: null, p:[style: colStyle]],
                        [v: row.result, f: null, p:[style: colStyle]],
                        [v: row.wave, f: null, p:[style: colStyle]],
                        [v: row.timestamp, f: null, p:[style: colStyle]],
                    ]]
                })
                break
            default:
                def query, psValues

                columns= [["Stat", "string"], ["Count", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                if (queryValues[Queries.steamid64] == null) {
                    query= 'SELECT * from aggregate where category=? ORDER BY stat ASC'
                    psValues= [queryValues[Queries.table]]
                } else {
                    query= 'SELECT * from player where steamid64=? and category=? ORDER BY stat ASC'
                    psValues= [queryValues[Queries.steamid64], queryValues[Queries.table]]
                }
                sql.eachRow(query, psValues) {row ->
                    def fVal= null
                    def lower= row.stat.toLowerCase()

                    if (queryValues[Queries.table] == "perks" || lower.contains('time')) {
                        fVal= Time.secToStr(row.value)
                    }
                    data << [c: [[v: row.stat, f:null, p: null], 
                        [v: row.value, f: fVal, p:[style: colStyle]],
                    ]]
                }
                break
        }
        
        def root= builder {
            cols(columns)
            rows(data)
        }
        return builder
    }
}

