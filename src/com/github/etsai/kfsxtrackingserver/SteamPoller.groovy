/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import com.github.etsai.kfsxtrackingserver.web.SteamIDInfo
import groovy.sql.Sql
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit

/**
 *
 * @author eric
 */
public class SteamPoller implements Runnable {
    static class PollerThread implements Runnable {
        public static final def count= new AtomicInteger()
        public def steamid64
        
        @Override public void run() {
            SteamIDInfo.verifySteamID64(steamid64)
            
            count.getAndAdd(1)
            if (count.get() % 50 == 0) {
                Common.logger.info("${count.get()} records polled")
            }
        }
    }
    
    private final def sql
    private final def nThreads
    
    public SteamPoller(Sql sql, Integer nThreads) {
        this.sql= sql
        this.nThreads= nThreads
    }
    
    @Override public void run() {
        def pollSteam= true
        def start= System.currentTimeMillis()
        
        Common.logger.info("Polling steamcommunity.com with $nThreads threads")
        while(pollSteam) {
            def pool= Executors.newFixedThreadPool(nThreads);
            pollSteam= false
            PollerThread.count.set(0)
            sql.eachRow("select steamid64 from records except select steamid64 from steaminfo") {row ->
                pool.submit(new PollerThread(steamid64: row.steamid64))
                pollSteam= true
            }

            pool.shutdown()
            while(!pool.awaitTermination(30, TimeUnit.SECONDS)) {
            }
            if (pollSteam) {
                Common.logger.fine("Attempted to poll ${PollerThread.count.get()} profiles.  Repolling missed steamid64s")
            }
        }
        def end= System.currentTimeMillis()
        Common.logger.info(String.format("Steam community polling complete, %1\$.2f seconds", (end - start)/(double)1000))
    }
}

