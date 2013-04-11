/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.*;
import com.github.etsai.utils.logging.TeeLogger;
import groovy.sql.Sql;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Main entry point for the tracking server
 * @author etsai
 */
public class Main {
    private static Sql[] sqlConnections= new Sql[2];
    private static ConsoleHandler logConsoleHandler;
    private static FileWriter logWriter;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        CommandLine clom= new CommandLine(args);
        ServerProperties props;
        
        try {
            props= ServerProperties.load(clom.getPropertiesFilename());
        } catch (IOException ex) {
            logger.warning(ex.getMessage());
            logger.warning("Using default properties...");
            props= ServerProperties.getDefaults();
        }
        
        initLogging(props.getLogLevel());
        initModules(props);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for(Sql sqlConn: sqlConnections) {
                    sqlConn.close();
                }
                logger.info("Shutting down server");
            }
        });
        
        Common.pool.submit(new UDPListener(props.getUdpPort()));
        Common.pool.submit(new HTTPListener(props.getHttpPort(), sqlConnections[1]));
    }
    
    public static void initModules(ServerProperties props) throws ClassNotFoundException, SQLException {
        logger.log(Level.INFO,"Loading stats from databse: {0}", props.getDbName());
        
        Class.forName("org.sqlite.JDBC");
        Common.pool= Executors.newFixedThreadPool(props.getNumThreads());
        for(int i= 0; i < sqlConnections.length; i++) {
            sqlConnections[i]= Sql.newInstance(String.format("jdbc:sqlite:%s", props.getDbName()));
        }
        sqlConnections[0].execute("CREATE TABLE IF NOT EXISTS steaminfo (steamid64 TEXT PRIMARY KEY  NOT NULL , name TEXT, avatar TEXT)");
        Common.pool.submit(new SteamPoller(Sql.newInstance(String.format("jdbc:sqlite:%s", props.getDbName())), 
                props.getSteamPollingThreads()));
        
        Accumulator.writer= new DataWriter(sqlConnections[0]);
        Accumulator.statMsgTTL= props.getStatsMsgTTL();
        Packet.password= props.getPassword();
        HTTPListener.httpRootDir= props.getHttpRootDir();
        
    }
    public static void initLogging(Level logLevel) {
        try {
            logWriter= TeeLogger.getFileWriter("kfsxtracking", new File("log"));
            oldStdOut= System.out;
            oldStdErr= System.err;
            System.setOut(new PrintStream(new TeeLogger(logWriter, oldStdOut), true));
            System.setErr(new PrintStream(new TeeLogger(logWriter, oldStdErr), true));
            
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
