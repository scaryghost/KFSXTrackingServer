/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.logger;
import static com.github.etsai.kfsxtrackingserver.Common.properties;
import static com.github.etsai.kfsxtrackingserver.ServerProperties.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
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
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
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
        String dbUri= String.format("jdbc:sqlite:%s", properties.getProperty(propDbName));
        Connection conn= DriverManager.getConnection(dbUri);
        conn.setAutoCommit(false);
        
        Timer timer = new Timer();
        Data.writer= new com.github.etsai.kfsxtrackingserver.impl.DataWriterImpl(conn);
        timer.scheduleAtFixedRate(Data.writer, 0, Long.valueOf(properties.getProperty(propDbWritePeriod)));
        Runtime.getRuntime().addShutdownHook( 
            new Thread(Data.writer)
        );
        
        Common.statsData= Data.load(conn);
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
