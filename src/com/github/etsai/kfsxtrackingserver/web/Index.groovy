package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData
import com.github.etsai.kfsxtrackingserver.Time
import groovy.xml.MarkupBuilder

public class Index extends Page {
    public String fillBody(def xmlBuilder) {
        xmlBuilder.kfstatsx() {
            def totalGames= 0
            def totalPlayTime= new Time(0)
            
            'stats'(category:"difficulties") {
                def wins= 0, losses= 0, wave= 0, time= new Time(0)
                statsData.getDifficulties().sort{"${it.getName()}/${it.getLength()}"}each {diff ->
                    def attr= [:]
                    def accum= [diff.getWins(), diff.getLosses(), diff.getWave(), diff.getTime()]
                    wins+= accum[0]
                    losses+= accum[1]
                    wave+= accum[2]
                    time.add(accum[3])
                    
                    attr["name"]= diff.getName()
                    attr["length"]= diff.getLength()
                    attr["wins"]= accum[0]
                    attr["losses"]= accum[1]
                    attr["wave"]= (accum[2] / (accum[0] + accum[1])).toInteger()
                    attr["time"]= accum[3].toString()
                    'entry'(attr)
                    
                    totalGames+= (accum[0] + accum[1])
                    totalPlayTime.add(accum[3])
                }
                wave/= (wins + losses).toInteger()
                'total'(name: "Total", length: "", wins: wins, losses:losses, 
                        wave: wave, time:time.toString())
            }
            'stats'(category:"levels") {
                def wins= 0, losses= 0, time= new Time(0)
                statsData.getLevels().sort{it.getName()}.each {level ->
                    def attr= [:]
                    def accum= [level.getWins(), level.getLosses(), level.getTime()]
                    
                    wins+= accum[0]
                    losses+= accum[1]
                    time.add(accum[2])
                    attr["name"]= level.getName()
                    attr["wins"]= accum[0]
                    attr["losses"]= accum[1]
                    attr["time"]= accum[2].toString()
                    'entry'(attr)
                }
                'total'(name: "Total", wins: wins, losses:losses, time:time.toString())
            }
            'stats'(category:"deaths") {                
                statsData.getDeaths().sort{it.getStat()}.each {death ->
                    def attr= [:]
                    attr["name"]= death.getStat()
                    attr["value"]= death.getValue()
                    'entry'(attr)
                }
            }
            def categories= [:]
            statsData.getAggregateStats().each {stat ->
                def cat= stat.getCategory()
                if (categories[cat] == null) {
                    categories[cat]= []
                }
                categories[cat] << stat
            }
            categories.each {cat, stats ->
                xmlBuilder.'stats'(category: cat) {
                    stats.sort{it.getStat()}.each {stat ->
                        def attrs= [:]
                        attrs["name"]= stat.getStat()
                        attrs["value"]= stat.getValue()
                        'entry'(attrs)
                    }
                }
            }
            
            'stats'(category:"totals") {
                'entry'(name:"games", value:totalGames)
                'entry'(name:"play time", value:totalPlayTime)
                'entry'(name:"player count", value:statsData.getRecords().size())
            }
        }
    }
}
