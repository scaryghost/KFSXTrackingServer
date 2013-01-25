/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.utils.Time
import groovy.json.JsonBuilder

/**
 *
 * @author etsai
 */
class DataJson {
    private static def colStyle= "text-align:center"
    
    public static String fillBody(def queries) {
        def columns
        def data= []
        def builder= new JsonBuilder()
        
        switch(queries["table"]) {
            case "difficulty":
                columns= [["Name", "string"], ["Length", "string"], ["Wins", "number"],
                    ["Losses", "number"], ["Avg Wave", "number"], ["Time", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                Common.sql.eachRow('select * from difficulties') {row ->
                    data << [c: [[v: row.name, f:null, p: null], 
                        [v: row.length, f: null, p:[style: colStyle]],
                        [v: row.wins, f: null, p:[style: colStyle]],
                        [v: row.losses, f: null, p:[style: colStyle]],
                        [v: row.wave, f: null, p:[style: colStyle]],
                        [v: row.time, f: Time.secToStr(row.time), p:[style: colStyle]],
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

