/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Maintains a set of IP addresses allowed to broadcast to the server
 * @author etsai
 */
public class AuthorizedClients {
    private static Map<String, Thread> clientIps= Collections.synchronizedMap(new HashMap());
    
    /**
     * Handles how long the client IP is allowed to broadcast to the server.
     * @author etsai
     */
    private static class TimeOut implements Runnable {
        private final String ip;
        
        /**
         * Create a TimeOut object bound to the given IP address
         * @param ip IP address bound to the object
         */
        public TimeOut(String ip) {
            this.ip= ip;
        }
        
        /**
         * When run terminates, the IP address this thread is tied to is removed
         * from the clientIps map
         */
        @Override
        public void run() {
            boolean terminate= false;
            
            while(!terminate) {
                try {
                    Thread.sleep(30000);
                    terminate= true;
                } catch (InterruptedException ex) {
                    logger.log(Level.INFO, "Timeout renewed for the address: {0}", ip);
                }
            }
            clientIps.remove(ip);
        }
        
    }
    
    /**
     * Adds the IP to the client IP list, or if already present, renews the timeout
     * @param ip IP address to add
     */
    public static void addClient(String ip) {
        if (!clientIps.containsKey(ip)) {
            clientIps.put(ip, new Thread(new TimeOut(ip)));
        } else {
            clientIps.get(ip).interrupt();
        }
    }
}
