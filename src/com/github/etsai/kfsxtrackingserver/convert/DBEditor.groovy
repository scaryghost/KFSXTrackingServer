/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.convert

import com.github.etsai.utils.Time
import com.github.etsai.utils.TimeFormatException
import groovy.sql.Sql

/**
 * Groovy warpper for executing the sql to convert data from v1.0 to v2.0 database
 * @author etsai
 */
public class DBEditor {
    /**
     * Convert the data, reading from src and writing to dest
     * @param   src     Source database to read from
     * @param   dest    Destination database to write to
     */
    public static void convert(Sql src, Sql dest) {
        dest.withTransaction {
            println "Converting deaths"
            dest.withBatch("insert into deaths values (?, ?)") {ps ->
                src.eachRow("select * from deaths") {row ->
                    ps.addBatch([row.name, row.count])
                }
            }
            
            println "Converting aggregate stats"
            dest.withBatch('insert into aggregate values (?, ?, ?)') { ps ->
                src.eachRow("select * from aggregate") {row ->
                    ps.addBatch([row.stat, row.category, row.value])
                }
            }
            
            println "Converting difficulties"
            dest.withBatch('insert into difficulties values (?, ?, ?, ?, ?, ?)') {ps  ->
                src.eachRow("select * from difficulties") {row ->
                    try {
                        def time= new Time(row.time)
                        ps.addBatch([row.name, row.length, row.wins, row.losses, row.wave, time.toSeconds()])
                        
                    } catch (TimeFormatException ex) {
                        println ex.getMessage()
                    }
                }
            }
            
            println "Converting levels"
            dest.withBatch('insert into levels values (?, ?, ?, ?)') {ps ->
                src.eachRow("select * from levels") {row ->
                    try {
                        def time= new Time(row.time)
                        ps.addBatch([row.name, row.wins, row.losses, time.toSeconds()])
                    } catch (TimeFormatException ex) {
                        println ex.getMessage()
                    }
                }
            }
                
            println "Converting player records"
            dest.withBatch('insert into records values (?, ?, ?, ?)') {ps ->
                src.eachRow("select * from records") {row ->
                    ps.addBatch([row.steamid, row.wins, row.losses, row.disconnects])
                }
            }
            
            println "Converting players"
            dest.withBatch('insert into player values (?, ?, ?, ?)') {ps ->
                src.eachRow("select * from player") {row ->
                    row.stats.split(",").each {statval ->
                        def split= statval.split("=")
                        ps.addBatch([row.steamid, split[0], row.category, split[1]])
                    }
                }
            }
        }
    }
}

