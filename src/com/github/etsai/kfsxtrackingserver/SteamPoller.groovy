/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.utils.sql.ConnectionPool;
import groovy.sql.Sql
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.sql.Connection

/**
 *
 * @author eric
 */
public class SteamPoller implements Runnable {
    public static class InvalidSteamIDException extends Exception {
        InvalidSteamIDException(String msg) {
            super(msg)
        }
    }
    
    public static def poll(def steamID64) throws InvalidSteamIDException, IOException {
        def url= new URL("http://steamcommunity.com/profiles/${steamID64}?xml=1")
        def content= url.getContent().readLines().join("\n")
        def steamXmlRoot= new XmlSlurper().parseText(content)
        def name, avatar

        if (steamXmlRoot.error != "") {
            throw new InvalidSteamIDException("Invalid steamID64: $steamID64")
        } else if (steamXmlRoot.privacyMessage != "") {
            name= "---Profile not setup---"
            avatar= ""
        } else {
            def tempName= steamXmlRoot.steamID.text()
            name= new String(tempName.getBytes(Charset.availableCharsets()["US-ASCII"]))
            avatar= steamXmlRoot.avatarFull.text()
        }
        return [name, avatar]
    }
    
    private final def connPool, nThreads
    
    public SteamPoller(ConnectionPool connPool, Integer nThreads) {
        this.connPool= connPool
        this.nThreads= nThreads
    }
    
    @Override public void run() {
        def pollSteam= true
        def start= System.currentTimeMillis()
        def sql= new Sql(connPool.getConnection())
        def count= new AtomicInteger()

        Common.logger.config("Polling steamcommunity.com with $nThreads threads")
        while(pollSteam) {
            def pool= Executors.newFixedThreadPool(nThreads);
            def steamInfo= new ConcurrentHashMap()
            
            pollSteam= false
            count.set(0)
            sql.eachRow("select steamid64 from record where id in (SELECT id from record except select record_id from steam_info)") {row ->
                def steamid64= row.steamid64

                pool.submit(new Runnable() {
                    @Override public void run() {
                        steamInfo[steamid64]= poll(steamid64)

                        count.getAndAdd(1)
                        if (count.get() % 50 == 0) {
                            Common.logger.info("${count.get()} records polled")
                        }
                    }
                })
                pollSteam= true
            }

            pool.shutdown()
            while(!pool.awaitTermination(30, TimeUnit.SECONDS)) {
            }
            if (pollSteam) {
                sql.withTransaction {
                    steamInfo.each {steamID64, info ->
                        sql.execute("insert or ignore into steam_info (record_id) select r.id from record r where steamid64=?", [steamID64])
                        sql.execute("update steam_info set name=?, avatar=? where record_id=(select id from record where steamid64=?)", [info[0], info[1], steamID64])
                    }
                }
                Common.logger.info("Attempted to poll ${PollerThread.count.get()} profiles.  Repolling missed steamid64s")
            }
        }
        def end= System.currentTimeMillis()
        Common.logger.info(String.format("Steam community polling complete, %1\$.2f seconds", (end - start)/(double)1000))
        connPool.release(conn)
    }
}

