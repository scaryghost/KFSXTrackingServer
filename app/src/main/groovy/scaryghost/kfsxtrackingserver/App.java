/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package scaryghost.kfsxtrackingserver;

import scaryghost.kfsxtrackingserver.web.WebHandler;
import scaryghost.utils.logging.TeeLogger;
import scaryghost.utils.sql.ConnectionPool;
import fi.iki.elonen.NanoHTTPD;
import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Main entry point for the tracking server
 * @author etsai
 */
public class App {
    private static ConsoleHandler logConsoleHandler;
    private static FileWriter logWriter;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            CommandLine clom= new CommandLine(args);
            ServerProperties props= new ServerProperties(clom.getPropertiesFilename());
            
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

            GroovyClassLoader loader= new GroovyClassLoader();

            Class<DataWriter> dataWriterClass;
            if (props.getDbWriterScript() == null) {
                dataWriterClass= (Class<DataWriter>)Class.forName("scaryghost.kfsxtrackingserver.impl.SQLiteWriter");
            } else {
                dataWriterClass= loader.parseClass(new File(props.getDbWriterScript()));
            }
            Constructor<DataWriter> dataWriterCtor= dataWriterClass.getConstructor(new Class<?>[] {Connection.class});
            DataWriter writer= dataWriterCtor.newInstance(new Object[] {connPool.getConnection()});
            String refactorGroup= clom.getRefactorGroup();

            if (refactorGroup != null) {
                Common.logger.log(Level.INFO, "Refactoring data group: %s", refactorGroup);
                writer.refactor(refactorGroup, clom.getRefactorInfo());
                System.exit(0);
            }            

            ExecutorService threadPool= Executors.newCachedThreadPool();
            threadPool.submit(new UDPListener(props.getUdpPort(), new Accumulator(writer, props.getPassword(), 
                    props.getStatsMsgTTL())));
            if (props.getSteamPollingThreads() != null) {
                threadPool.submit(new SteamPoller(connPool, props.getSteamPollingThreads(), dataWriterCtor));
            }

            if (props.getHttpPort() != null) {
                Class<?> dataReaderClass;
                if (props.getDbReaderScript() == null) {
                    dataReaderClass= Class.forName("scaryghost.kfsxtrackingserver.DefaultReader");
                } else {
                    dataReaderClass= loader.parseClass(new File(props.getDbReaderScript()));
                }
                final NanoHTTPD webHandler= new WebHandler(props.getHttpPort(), props.getHttpRootDir(), connPool, (Class<Object>) dataReaderClass);
                webHandler.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        webHandler.stop();
                        Common.logger.info("Shutting down web server");
                    }
                });
            }

        } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException | NoSuchMethodException | 
                SecurityException | IOException | IllegalArgumentException | InvocationTargetException ex) {
            Common.logger.log(Level.SEVERE, "Error starting the server", ex);
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
