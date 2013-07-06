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
            dest.withBatch("insert into aggregate values (?, ?, ?)") {ps ->
                src.eachRow("select * from deaths") {row ->
                    ps.addBatch(["deaths", row.name, row.count])
                }
                src.eachRow("select * from aggregate") {row ->
                    def category= row.category == "player" ? "summary" : row.category
                    if (row.stat != "Time Connected") {
                        ps.addBatch([category, row.stat, row.value])
                    }
                }
            }

            dest.withBatch("insert into difficulty (name, length, wins, losses, wave_sum, time) values (?, ?, ?, ?, ?, ?)") {ps ->
                src.eachRow("select * from difficulties") {row ->
                    ps.addBatch([row.name, row.length, row.wins, row.losses, row.wave, row.time])
                }
            }

            dest.withBatch("insert into level (name, wins, losses, time) values (?, ?, ?, ?)") {ps ->
                src.eachRow("select * from levels") {row ->
                    ps.addBatch([row.name, row.wins, row.losses, row.time])
                }
            }

            dest.withBatch("insert into record (steamid64, wins, losses, disconnects, finales_played, finales_survived, time) values (?, ?, ?, ?, ?, ?, ?)") {ps ->
                src.eachRow("select * from records") {row ->
                    ps.addBatch([row.steamid64, row.wins, row.losses, row.disconnects, 0, 0, 0])
                }
            }

            def durations= [:]
            dest.withBatch("insert into player (record_id, category, stat, value) select r.id, ?, ?, ? from record r where r.steamid64=?") {ps ->
                src.eachRow("select * from player") {row ->
                    def category= row.category == "player" ? "summary" : row.category
                    if (row.stat == "Time Connected") {
                        durations[row.steamid64]= row.value
                    } else {
                        ps.addBatch([category, row.stat, row.value, row.steamid64])
                    }
                }
            }

            dest.withBatch("update record set time_connected=? where steamid64=?") {ps ->
                durations.each {steamid64, time ->
                    ps.addBatch([time, steamid64])
                }
            }

            dest.withBatch("insert into match_history (record_id, level_id, difficulty_id, result, wave, duration, timestamp) select r.id, l.id, d.id, ?, ?, 0, ? from record r, level l, difficulty d where r.steamid64=? and d.name=? and d.length=? and l.name=?") {ps ->
                src.eachRow("select * from sessions") {row ->
                    ps.addBatch([row.result, row.wave, row.timestamp, row.steamid64, row.difficulty, row.length, row.level])
                }
            }

        }
    }
}

