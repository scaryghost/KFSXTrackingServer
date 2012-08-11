package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData
import groovy.xml.MarkupBuilder

public class Index extends Page {
    public String fillBody(def xmlBuilder) {
        xmlBuilder.kfstatsx() {
            'stats'(category:"difficulties") {
                statsData.getDifficulties().sort{"${it.getName()}/${it.getLength()}"}each {diff ->
                    def attr= [:]
                    attr["name"]= diff.getName()
                    attr["length"]= diff.getLength()
                    attr["wins"]= diff.getWins()
                    attr["losses"]= diff.getLosses()
                    attr["wave"]= diff.getWave()
                    attr["time"]= diff.getTime().toString()
                    'entry'(attr)
                }
            }
            'stats'(category:"levels") {
                statsData.getLevels().sort{it.getName()}.each {level ->
                    def attr= [:]
                    attr["name"]= level.getName()
                    attr["wins"]= level.getWins()
                    attr["losses"]= level.getLosses()
                    attr["time"]= level.getTime().toString()
                    'entry'(attr)
                }
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
        }
    }
}
