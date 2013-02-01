package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.sql
import com.github.etsai.utils.Time

public class Index {
    public static String fillBody(def xmlBuilder) {
        xmlBuilder.kfstatsx() {
            'stats'(category:"totals") {
                WebCommon.generateSummary().each {attr ->
                    'entry'(attr)
                }
            }
            
            'stats'(category:"difficulties") {
                def wins= 0, losses= 0, time= 0
                sql.eachRow('SELECT * from difficulties order by name ASC') {row ->
                    def attr= [:]
                    wins+= row.wins
                    losses+= row.losses
                    time+= row.time
                    
                    attr["name"]= row.name
                    attr["length"]= row.length
                    attr["wins"]= row.wins
                    attr["losses"]= row.losses
                    attr["wave"]= String.format("%.2f",row.wave / (row.wins + row.losses))
                    attr["rawtime"]= row.time
                    'entry'(attr)
                }
                'total'(name: "Total", length: "", wins: wins, losses:losses, 
                    wave: "", time:Time.secToStr(time))
            }
            'stats'(category:"levels") {
                def wins= 0, losses= 0, time= 0
                sql.eachRow('SELECT * FROM levels ORDER BY name ASC') {row ->
                    def attr= [:]
                    
                    wins+= row.wins
                    losses+= row.losses
                    //Accidentally didn't set the column type for time in levels table
                    time+= row.time.toInteger()
                    attr["name"]= row.name
                    attr["wins"]= row.wins
                    attr["losses"]= row.losses
                    attr["time"]= Time.secToStr(row.time.toInteger())
                    attr["rawtime"]= row.time
                    'entry'(attr)
                }
                'total'(name: "Total", wins: wins, losses:losses, time:Time.secToStr(time))
            }
            'stats'(category:"deaths") {
                sql.eachRow('SELECT * FROM deaths ORDER BY name ASC') {row ->
                    def attr= [:]
                    attr["name"]= row.name
                    attr["value"]= row.count
                    'entry'(attr)
                }
            }
            sql.eachRow('SELECT category FROM aggregate GROUP BY category') {row1 ->
                xmlBuilder.'stats'(category: row1.category) {
                    sql.eachRow("SELECT * from aggregate where category=? ORDER BY stat ASC", [row1.category]) {row2 ->
                        def key, val
                        def attrs= [:]
                        attrs["name"]= row2.stat
                        attrs["value"]= row2.value

                        if (row1.category == "perks") {
                            attrs["hint"]= Time.secToStr(attrs["value"])
                        } else if (attrs["name"].toLowerCase().contains("time")) {
                            attrs["value"]= Time.secToStr(attrs["value"])
                        }
                        'entry'(attrs)
                    }
                }
            }
        }
    }
}
