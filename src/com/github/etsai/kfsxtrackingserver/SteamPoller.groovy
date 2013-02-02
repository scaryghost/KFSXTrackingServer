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
        private static final def count= new AtomicInteger()
        public def steamid64
        
        public static void resetCounter() {
            count.set(0)
        }
        
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
        
        Common.logger.info("Polling steamcommunity.com with $nThreads threads")
        while(pollSteam) {
            def pool= Executors.newFixedThreadPool(nThreads);
            pollSteam= false
            PollerThread.resetCounter()
            sql.eachRow("select steamid64 from records except select steamid64 from steaminfo") {row ->
                pool.submit(new PollerThread(steamid64: row.steamid64))
                pollSteam= true
            }

            pool.shutdown()
            while(!pool.awaitTermination(30, TimeUnit.SECONDS)) {
            }
            if (pollSteam) {
                Common.logger.fine("Repolling missing steamid64s")
            }
        }
        Common.logger.info("Polling complete")
    }
}

