package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData
import groovy.xml.MarkupBuilder

public class Index extends Page {
    public String fillBody(def xmlBuilde) {
        xmlBuilder.kfstatsx() {
            'difficulties'() {
                def attr= [:]
                statsData.getDifficulties().each {diff ->
                    attr["name"]= diff.getName()
                    attr["length"]= diff.getLength()
                    attr["wins"]= diff.getWins()
                    attr["losses"]= diff.getLosses()
                    attr["wave"]= diff.getWave()
                    attr["time"]= diff.getTime().toString()
                }

                'difficulty'(attr)
            }

            'levels'() {
                def attr= [:]
                statsData.getLevels().each {level ->
                    
                }
            }
        }
    }
}
