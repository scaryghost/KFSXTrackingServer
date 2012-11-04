/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.convert

import com.github.etsai.utils.Time
import groovy.sql.Sql

/**
 *
 * @author eric
 */
public class DBEditor {
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
                    def time= new Time(row.time)
                    ps.addBatch([row.name, row.length, row.wins, row.losses, row.wave, time.toSeconds()])
                }
            }
            
            println "Converting levels"
            dest.withBatch('insert into levels values (?, ?, ?, ?)') {ps ->
                src.eachRow("select * from levels") {row ->
                    def time= new Time(row.time)
                    ps.addBatch([row.name, row.wins, row.losses, time.toSeconds()])
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
