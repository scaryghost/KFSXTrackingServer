/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.kfsxtrackingserver.DataWriter.SteamInfo
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
import java.lang.reflect.Constructor

/**
 *
 * @author eric
 * @TODO Add data reader and writer to steam poller
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
    
    private final def connPool, nThreads, writerCtor
    
    public SteamPoller(ConnectionPool connPool, Integer nThreads, Constructor<DataWriter> writerCtor) {
        this.connPool= connPool
        this.nThreads= nThreads
        this.writerCtor= writerCtor
    }
    
    @Override public void run() {
        def pollSteam= true
        def start= System.currentTimeMillis()
        def conn= connPool.getConnection()
        def sql= new Sql()
        def count= new AtomicInteger()
        
        def dataWriter= writerCtor.newInstance([conn] as List)

        Common.logger.config("Polling steamcommunity.com with $nThreads threads")
        while(pollSteam) {
            def pool= Executors.newFixedThreadPool(nThreads);
            def steamInfo= new ConcurrentHashMap()
            
            pollSteam= false
            count.set(0)
            dataWriter.getMissingSteamInfoIDs().each {row ->
                def steamid64= row.steamid64

                pool.submit(new Runnable() {
                    @Override public void run() {
                        def pollInfo= poll(steamid64)
                        steamInfo[steamid64]= new SteamInfo(steamID64: steamid64, name: info[0], avatar: info[1])

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
                dataWriter.writeSteamInfo(steamInfo.values())
                Common.logger.info("Attempted to poll ${PollerThread.count.get()} profiles.  Repolling missed steamid64s")
            }
        }
        def end= System.currentTimeMillis()
        connPool.release(conn)
        Common.logger.info(String.format("Steam community polling complete, %1\$.2f seconds", (end - start)/(double)1000))
    }
}

