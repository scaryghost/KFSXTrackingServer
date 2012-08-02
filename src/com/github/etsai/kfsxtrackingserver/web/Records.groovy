/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.statsData

/**
 *
 * @author etsai
 */
public class Records extends Page {
    public String fillBody(def xmlBuilder) {
        xmlBuilder.kfstatsx() {
            'records'() {
                statsData.getRecords().each {record ->
                    def attr= [:]
                    attr["steamid"]= record.getSteamId()
                    attr["wins"]= record.getWins()
                    attr["losses"]= record.getLosses()
                    attr["disconnects"]= record.getDisconnects()
                    xmlBuilder.'record'(attr)
                }
            }
        }
    }
}

