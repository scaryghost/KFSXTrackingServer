/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.*;
import static com.github.etsai.kfsxtrackingserver.ServerProperties.propUdpPort;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.SimpleFormatter;

/**
 * Main entry point for the tracking server
 * @author etsai
 */
public class Main {
    private static FileHandler logFileHandler;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException {
        CommandLine clom= CommandLine.parse(args);
        
        initLogging();
        
        try {
            properties= ServerProperties.load(clom.getPropertiesFilename());
        } catch (IOException ex) {
            logger.warning(ex.getMessage());
            logger.warning("Using default properties...");
            properties= ServerProperties.getDefaults();
        }
        
        UDPListener listener= new UDPListener(Integer.valueOf(properties.getProperty(propUdpPort)));
        Thread udpTh= new Thread(listener);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logFileHandler.close();
            }
        });
        
        Class.forName("org.sqlite.JDBC");
        matchContent= (Content) new com.github.etsai.kfsxtrackingserver.impl.MatchContent();
        matchContent.load();
        
        playerContents= new HashMap();
        udpTh.start();
    }
    
    public static void initLogging() {
        String filename;
        String localHostAddress;
        
        try {
            localHostAddress= InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            logger.warning(ex.getMessage());
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
            logger.warning(ex.getMessage());
            logger.warning("Logging to output stream...");
        } 
    }
}
