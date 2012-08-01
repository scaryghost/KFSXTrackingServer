package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData
import groovy.xml.MarkupBuilder

public class Index extends Page {
    public String fillBody(def xmlBuilder) {
        xmlBuilder.kfstatsx() {
            'difficulties'() {
                statsData.getDifficulties().each {diff ->
                    def attr= [:]
                    attr["name"]= diff.getName()
                    attr["length"]= diff.getLength()
                    attr["wins"]= diff.getWins()
                    attr["losses"]= diff.getLosses()
                    attr["wave"]= diff.getWave()
                    attr["time"]= diff.getTime().toString()
                    'difficulty'(attr)
                }

                
            }
/*
            'levels'() {
                def attr= [:]
                statsData.getLevels().each {level ->
                    
                }
            }
            */
        }
    }
}
