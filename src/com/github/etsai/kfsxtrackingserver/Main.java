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
import java.util.logging.*;

/**
 * Main entry point for the tracking server
 * @author etsai
 */
public class Main {
    private static FileHandler logFileHandler;
    private static ConsoleHandler logConsoleHandler;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        CommandLine clom= CommandLine.parse(args);
        
        try {
            properties= ServerProperties.load(clom.getPropertiesFilename());
        } catch (IOException ex) {
            logger.warning(ex.getMessage());
            logger.warning("Using default properties...");
            properties= ServerProperties.getDefaults();
        }
        
        initLogging();
        
        Thread udpTh= new Thread(new UDPListener(Integer.valueOf(properties.getProperty(propUdpPort))));
        Thread httpTh= new Thread(new HTTPListener(Integer.valueOf(properties.getProperty(propHttpPort))));
                
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutting down server");
                logFileHandler.close();
                logConsoleHandler.close();
            }
        });
        
        Class.forName("org.sqlite.JDBC");
        String dbUri= String.format("jdbc:sqlite:%s", properties.getProperty(propDbName));
        Connection conn= DriverManager.getConnection(dbUri);
        conn.setAutoCommit(false);
        
        Timer timer = new Timer();
        Data.writer= new com.github.etsai.kfsxtrackingserver.impl.DataWriterImpl(conn);
        Long dbWritePeriod= Long.valueOf(properties.getProperty(propDbWritePeriod));
        timer.scheduleAtFixedRate(Data.writer, dbWritePeriod, dbWritePeriod);
        Runtime.getRuntime().addShutdownHook( 
            new Thread(Data.writer)
        );
        
        logger.log(Level.INFO,"Loading stats from databse: {0}", properties.getProperty(propDbName));
        Common.statsData= Data.load(conn);
        udpTh.start();
        httpTh.start();
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
            Level logLevel= Level.parse(properties.getProperty(propLogLevel));
            logFileHandler= new FileHandler(filename);
            logConsoleHandler= new ConsoleHandler();
            
            logFileHandler.setFormatter(new SimpleFormatter());
            logFileHandler.setLevel(logLevel);
            logConsoleHandler.setLevel(logLevel);
            
            for(Handler handler: logger.getHandlers()) {
                logger.removeHandler(handler);
            }
            logger.addHandler(logFileHandler);
            logger.addHandler(logConsoleHandler);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
        } catch (IOException|SecurityException ex) {
            logger.warning(ex.getMessage());
            logger.warning("Logging to output stream...");
        }
    }
}
