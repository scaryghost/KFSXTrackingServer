/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.utils.Time
import groovy.json.JsonBuilder

/**
 * Generates the json data for the page data.json
 * @author etsai
 */
public class DataJson {
    private static def colStyle= "text-align:center"
    
    public static String fillBody(def queries) {
        def columns
        def data= []
        def builder= new JsonBuilder()
        
        switch(queries["table"]) {
            case "difficulties":
                columns= [["Name", "string"], ["Length", "string"], ["Wins", "number"],
                    ["Losses", "number"], ["Avg Wave", "number"], ["Time", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                Common.sql.eachRow('select * from difficulties ORDER BY name ASC') {row ->
                    data << [c: [[v: row.name, f:null, p: null], 
                        [v: row.length, f: null, p:[style: colStyle]],
                        [v: row.wins, f: null, p:[style: colStyle]],
                        [v: row.losses, f: null, p:[style: colStyle]],
                        [v: String.format("%.2f",row.wave / (row.wins + row.losses)), f: null, p:[style: colStyle]],
                        [v: row.time, f: Time.secToStr(row.time), p:[style: colStyle]],
                    ]]
                }
               break
            case "levels":
                columns= [["Name", "string"], ["Wins", "number"], ["Losses", "number"], ["Time", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                Common.sql.eachRow('select * from levels ORDER BY name ASC') {row ->
                    data << [c: [[v: row.name, f:null, p: null], 
                        [v: row.wins, f: null, p:[style: colStyle]],
                        [v: row.losses, f: null, p:[style: colStyle]],
                        [v: row.time, f: Time.secToStr(row.time), p:[style: colStyle]],
                    ]]
                }
                break
            case "deaths":
                columns= [["Death", "string"], ["Count", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                Common.sql.eachRow('select * from deaths ORDER BY name ASC') {row ->
                    data << [c: [[v: row.name, f:null, p: null], 
                        [v: row.count, f: null, p:[style: colStyle]],
                    ]]
                }
                break
            case "records":
                columns= [["Name", "string"], ["Wins", "numbers"], ["Losses", "number"], ["Disconnects", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                def params= queries["tq"].tokenize(",")
                def page= [params[0].toInteger(), 0].max()
                def pageSize= params[1].toInteger()
                def start= page * pageSize
                def end= start + pageSize
        
                WebCommon.adjustStartEnd("SELECT count(*) FROM records", [], start, end)
        
                def sql= "SELECT s.name,r.wins,r.losses,r.disconnects,r.steamid64 FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64 "

                if (params.size() >= 4) {
                    sql+= "ORDER BY ${columns[params[2].toInteger()]['label']} ${params[3]} "
                }
                sql+= "LIMIT ?,?"

                Common.logger.finest(sql)
                Common.sql.eachRow(sql, [start, end - start]) {row ->
                    data << [c: [[v: row.name, f: "<a href=profile.html?steamid64=${row.steamid64}>${row.name}</a>", p: null], 
                        [v: row.wins, f: null, p:[style: colStyle]],
                        [v: row.losses, f: null, p:[style: colStyle]],
                        [v: row.disconnects, f: null, p:[style: colStyle]]]]
                }

                break
            case "sessions":
                columns= [["Level", "string"], ["Difficulty", "string"], ["Length", "string"],
                        ["Result", "string"], ["Wave", "number"], ["Timestamp", "string"]].collect {
                    [label: it[0], type: it[1]]
                }
                def steamid64= queries["steamid64"]
                def params= queries["tq"].tokenize(",")

                def page= [params[0].toInteger(), 0].max()
                def pageSize= params[1].toInteger()
                def start= page * pageSize
                def end= start + pageSize

                WebCommon.adjustStartEnd("SELECT count(*) FROM sessions WHERE steamid64=?", [steamid64], start, end)
        
                def sql= "SELECT * FROM sessions WHERE steamid64=?" 
                if (params.size() >= 4) {
                    sql+= "ORDER BY ${columns[params[2].toInteger()]['label']} ${params[3]} "
                }
                sql+= "LIMIT ?,?"

                Common.logger.finest(sql)
                Common.sql.eachRow(sql, [steamid64, start, end - start]) {row ->
                    data << [c: [[v: row.level, f:null, p: null], 
                        [v: row.difficulty, f: null, p:[style: colStyle]],
                        [v: row.length, f: null, p:[style: colStyle]],
                        [v: row.result, f: null, p:[style: colStyle]],
                        [v: row.wave, f: null, p:[style: colStyle]],
                        [v: row.timestamp, f: null, p:[style: colStyle]],
                    ]]
                }
                break
            default:
                def sql, psValues

                columns= [["Stat", "string"], ["Count", "number"]].collect {
                    [label: it[0], type: it[1]]
                }
                if (queries["steamid64"] == null) {
                    sql= 'SELECT * from aggregate where category=? ORDER BY stat ASC'
                    psValues= [queries["table"]]
                } else {
                    sql= 'SELECT * from player where steamid64=? and category=? ORDER BY stat ASC'
                    psValues= [queries["steamid64"], queries["table"]]
                }
                Common.sql.eachRow(sql,psValues) {row ->
                    def fVal= queries["table"] == "perks" ? Time.secToStr(row.value) : null
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

