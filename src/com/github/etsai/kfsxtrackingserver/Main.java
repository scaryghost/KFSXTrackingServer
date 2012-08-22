/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.*;
import static com.github.etsai.kfsxtrackingserver.ServerProperties.*;
import com.github.etsai.kfsxtrackingserver.web.SteamIdInfo.SteamIDUpdater;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.*;

/**
 * Main entry point for the tracking server
 * @author etsai
 */
public class Main {
    private static ConsoleHandler logConsoleHandler;
    private static PrintStream loggedStream;
    
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
                
        logger.log(Level.INFO,"Loading stats from databse: {0}", properties.getProperty(propDbName));
        Class.forName("org.sqlite.JDBC");
        String dbUri= String.format("jdbc:sqlite:%s", properties.getProperty(propDbName));
        Connection conn= DriverManager.getConnection(dbUri);
        conn.setAutoCommit(false);
        Common.statsData= Data.load(conn);
        
        Data.writer= new com.github.etsai.kfsxtrackingserver.impl.DataWriterImpl(conn);
        Long dbWritePeriod= Long.valueOf(properties.getProperty(propDbWritePeriod));
        timer.scheduleAtFixedRate(Data.writer, dbWritePeriod, dbWritePeriod);
        timer.scheduleAtFixedRate(new SteamIDUpdater(), 0, Integer.valueOf(properties.getProperty(propSteamPollingPeriod)));
        
        Runtime.getRuntime().addShutdownHook( 
            new Thread(Data.writer)
        );
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Shutting down server");
                logConsoleHandler.close();
                loggedStream.close();
            }
        });
        
        
        udpTh.start();
        httpTh.start();
    }
    
    public static void initLogging() {
        String localHostAddress;
            
        try {
            localHostAddress= InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            logger.warning(ex.getMessage());
            localHostAddress= "unknown";
        }
            
        try {
            String filename;
            File logFile;
            filename= String.format("%s.%s.%tY%<tm%<td-%<tH%<tM%<tS.log", 
                "kfsxtrackingserver", localHostAddress, new Date());
            
            logFile= new File(filename);
            loggedStream = new PrintStream(logFile);
            oldStdOut= System.out;
            oldStdErr= System.err;
            System.setOut(loggedStream);
            System.setErr(loggedStream);
            
            Level logLevel= Level.parse(properties.getProperty(propLogLevel));
            for(Handler handler: logger.getHandlers()) {
                logger.removeHandler(handler);
            }
            logConsoleHandler= new ConsoleHandler();
            logConsoleHandler.setLevel(logLevel);
            logger.setLevel(Level.ALL);
            logger.addHandler(logConsoleHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Output will not be saved to file...", ex);
        }

        
    }
}
