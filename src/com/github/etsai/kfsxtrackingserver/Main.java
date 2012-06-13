/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Core.logger;
import static com.github.etsai.kfsxtrackingserver.ServerProperties.UDP_PORT;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.*;

/**
 * Main entry point for the tracking server
 * @author etsai
 */
public class Main {
    private static FileHandler logFileHandler;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Properties props;
        CommandLine clom= CommandLine.parse(args);
        
        try {
            props= ServerProperties.load(clom.getPropertiesFilename());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            props= ServerProperties.getDefaults();
        }
        
        UDPListener listener= new UDPListener(Integer.valueOf(props.getProperty(UDP_PORT)));
        Thread udpTh= new Thread(listener);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logFileHandler.close();
            }
        });
        
        udpTh.start();
    }
    
    public static void initLogging() {
        String filename;
        String localHostAddress;
        
        try {
            localHostAddress= InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, "Error getting hostname of local host", ex);
            localHostAddress= "unknown";
        }
        
        filename= String.format("%s.%s.%tY%<tm%<td-%<tH%<tM%<tS.log", 
            "udpstatstracker", localHostAddress, new Date());
        try {
            logFileHandler= new FileHandler(filename);
            logFileHandler.setFormatter(new SimpleFormatter());
        
            for(Handler handler: logger.getHandlers()) {
                logger.removeHandler(handler);
            }
            logger.addHandler(logFileHandler);
        } catch (IOException|SecurityException ex) {
            logger.log(Level.SEVERE, "Cannot write to file: "+filename, ex);
        } 
    }
}