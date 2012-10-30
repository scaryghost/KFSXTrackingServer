package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData
import static com.github.etsai.kfsxtrackingserver.Common.sql
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder

public class Index extends Page {
    public String fillBody(def xmlBuilder) {
        xmlBuilder.kfstatsx() {
            def totalGames= 0
            def totalPlayTime= 0
            
            sql.eachRow('select * from difficulties') {row ->
                totalGames+= row.wins + row.losses
                totalPlayTime+= row.time
            }
            sql.eachRow('SELECT count(*) FROM records') {row ->
                'stats'(category:"totals") {
                    'entry'(name:"Games", value:totalGames)
                    'entry'(name:"Play Time", value:new Time(totalPlayTime))
                    'entry'(name:"Player Count", value:row[0])
                }
            }
            
            'stats'(category:"difficulties") {
                def wins= 0, losses= 0, time= 0
                sql.eachRow('SELECT * from difficulties') {row ->
                    def attr= [:]
                    def accum= [row.wins, row.losses, row.time]
                    wins+= accum[0]
                    losses+= accum[1]
                    time+= accum[2]
                    
                    attr["name"]= row.name
                    attr["length"]= row.length
                    attr["wins"]= accum[0]
                    attr["losses"]= accum[1]
                    attr["wave"]= String.format("%.2f",row.wave / (accum[0] + accum[1]))
                    attr["time"]= accum[2].toString()
                    'entry'(attr)
                }
                'total'(name: "Total", length: "", wins: wins, losses:losses, 
                        wave: "", time:new Time(time))
            }
            'stats'(category:"levels") {
                def wins= 0, losses= 0, time= 0
                sql.eachRow('SELECT * FROM levels') {row ->
                    def attr= [:]
                    def accum= [row.wins, row.losses, row.time]
                    
                    wins+= accum[0]
                    losses+= accum[1]
                    time+= accum[2]
                    attr["name"]= row.name
                    attr["wins"]= accum[0]
                    attr["losses"]= accum[1]
                    attr["time"]= accum[2].toString()
                    'entry'(attr)
                }
                'total'(name: "Total", wins: wins, losses:losses, time:new Time(time))
            }
            'stats'(category:"deaths") {
                sql.eachRow('SELECT * FROM deaths') {row ->
                    def attr= [:]
                    attr["name"]= row.name
                    attr["value"]= row.count
                    'entry'(attr)
                }
            }
            sql.eachRow('SELECT category FROM aggregate GROUP BY category') {row1 ->
                xmlBuilder.'stats'(category: row1.category) {
                    sql.eachRow("SELECT * from aggregate where category='${row1.category}'") {row2 ->
                        def key, val
                        def attrs= [:]
                        attrs["name"]= row2.stat
                        attrs["value"]= row2.value

                        if (row1.category == "perks") {
                            attrs["hint"]= new Time(attrs["value"]).toString()
                        } else if (attrs["name"].toLowerCase().contains("time")) {
                            attrs["value"]= new Time(attrs["value"]).toString()
                        }
                        'entry'(attrs)
                    }
                }
            }
        }
    }
}
