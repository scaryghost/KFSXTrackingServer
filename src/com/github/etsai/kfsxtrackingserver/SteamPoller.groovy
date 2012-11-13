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
        private static def count
        public def steamid64
        
        public static void resetCounter() {
            count= new AtomicInteger()
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
        def count= 1
        
        Common.logger.info("Polling steamcommunity.com with $nThreads threads")
        while(count != 0) {
            def pool= Executors.newFixedThreadPool(nThreads);
            count= 0
            PollerThread.resetCounter()
            sql.eachRow("select steamid64 from records except select steamid64 from steaminfo") {row ->
                pool.submit(new PollerThread(steamid64: row.steamid64))
                count++
            }

            pool.shutdown()
            while(!pool.awaitTermination(30, TimeUnit.SECONDS)) {
            }
            if (count != 0) {
                Common.logger.fine("Repolling missing steamid64s")
            }
        }
        Common.logger.info("Polling complete")
    }
}

