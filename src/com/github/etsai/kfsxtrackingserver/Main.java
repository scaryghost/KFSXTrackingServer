/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.kfsxtrackingserver.impl.SQLiteWriter;
import com.github.etsai.kfsxtrackingserver.web.WebHandler;
import com.github.etsai.utils.logging.TeeLogger;
import com.github.etsai.utils.sql.ConnectionPool;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Main entry point for the tracking server
 * @author etsai
 */
public class Main {
    private static ConsoleHandler logConsoleHandler;
    private static FileWriter logWriter;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine clom= new CommandLine(args);
            ServerProperties props= ServerProperties.load(clom.getPropertiesFilename());

            initLogging(props.getLogLevel());

            Common.logger.log(Level.CONFIG,"Loading stats from database: {0}", props.getDbURL());
            final ConnectionPool connPool= new ConnectionPool(props.getNumDbConn());
            connPool.setJdbcUrl(props.getDbURL());
            if (props.getDbDriver() != null) {
                connPool.setDbDriver(props.getDbDriver());
            }
            if (props.getDbUser() != null) {
                connPool.setDbUser(props.getDbUser());
            }
            if (props.getDbPassword() != null) {
                connPool.setDbPassword(props.getDbPassword());
            }
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        connPool.close();
                    } catch (SQLException ex) {
                        Common.logger.log(Level.SEVERE, "Error shutting down connections", ex);
                    }
                    Common.logger.info("Closing db connections");
                }
            });

            URL[] urls= {new URL(props.getDbLibJar())};
            URLClassLoader urlCl= new URLClassLoader(urls, Main.class.getClassLoader());
            Class<DataWriter> dataWriterClass= (Class<DataWriter>)Class.forName(props.getDbWriterClass(), true, urlCl);
            Class<DataReader> dataReaderClass= (Class<DataReader>)Class.forName(props.getDbReaderClass(), true, urlCl);

            if (props.getHttpPort() > 0) {
                final NanoHTTPD webHandler= new WebHandler(props.getHttpPort(), props.getHttpRootDir(), connPool, dataReaderClass.getConstructor(new Class<?>[] {Connection.class}));
                webHandler.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        webHandler.stop();
                        Common.logger.info("Shutting down web server");
                    }
                });
            } else {
                Common.logger.log(Level.CONFIG, "HTTP server disabled");
            }
            DataWriter writer= dataWriterClass.getConstructor(new Class<?>[] {Connection.class}).newInstance(new Object[] {connPool.getConnection()});
            ExecutorService threadPool= Executors.newCachedThreadPool();
            threadPool.submit(new UDPListener(props.getUdpPort(), new Accumulator(writer, props.getPassword(), props.getStatsMsgTTL())));
            threadPool.submit(new SteamPoller(connPool, props.getSteamPollingThreads()));
        } catch (Exception ex) {
            Common.logger.log(Level.SEVERE, null, ex);
        }
    }

    public static void initLogging(Level logLevel) {
        try {
            logWriter= TeeLogger.getFileWriter("kfsxtracking", new File("log"));
            Common.oldStdOut= System.out;
            Common.oldStdErr= System.err;
            System.setOut(new PrintStream(new TeeLogger(logWriter, Common.oldStdOut), true));
            System.setErr(new PrintStream(new TeeLogger(logWriter, Common.oldStdErr), true));
            
            for(Handler handler: Common.logger.getHandlers()) {
                Common.logger.removeHandler(handler);
            }
            logConsoleHandler= new ConsoleHandler();
            logConsoleHandler.setLevel(logLevel);
            Common.logger.setLevel(Level.ALL);
            Common.logger.addHandler(logConsoleHandler);
            Common.logger.setUseParentHandlers(false);
            
            NanoHTTPD.logger= Common.logger;
            NanoHTTPD.logLevel= logLevel;
        } catch (IOException ex) {
            Common.logger.log(Level.WARNING, "Output will not be saved to file...", ex);
        }

        
    }
}
