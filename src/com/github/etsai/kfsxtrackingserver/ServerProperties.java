/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Stores properties for the tracking server
 * @author etsai
 */
public class ServerProperties {
    private static ServerProperties defaults;
    
    public static final String udpPort= "udp.port";
    public static final String httpPort= "http.port";
    public static final String httpRootDir= "http.root.dir";
    public static final String password= "password";
    public static final String statsMsgTTL= "stats.msg.ttl";
    public static final String dbUrl= "db.url";
    public static final String dbDriver= "db.driver";
    public static final String dbLibJar= "db.lib.jar";
    public static final String dbReaderClass= "db.reader.class";
    public static final String dbWriterClass= "db.writer.class";
    public static final String dbUser= "db.user";
    public static final String dbPassword= "db.password";
    public static final String numDbConn= "num.db.conn";
    public static final String logLevel= "log.level";
    public static final String steamPollingThreads= "steam.polling.threads";
    
    public static ServerProperties load(String filename) throws IOException {
        Properties props= new Properties();
        props.load(new FileReader(filename));
        
        return new ServerProperties(props);
    }
    
    public synchronized static ServerProperties getDefaults() {
        if (defaults == null) {
            Properties props= new Properties();
            props.setProperty(udpPort, "6000");
            props.setProperty(httpPort, "8080");
            props.setProperty(httpRootDir, "http");
            props.setProperty(password, "server");
            props.setProperty(statsMsgTTL, "60000");
            props.setProperty(dbUrl, "jdbc:sqlite:share/etc/kfsxdb.sqlite3");
            props.setProperty(dbDriver, "org.sqlite.JDBC");
            props.setProperty(dbLibJar, "jar:file:lib/SQLiteDataConnection.jar!/");
            props.setProperty(dbReaderClass, "SQLiteReader");
            props.setProperty(dbWriterClass, "SQLIteWriter");
            props.setProperty(numDbConn, "10");
            props.setProperty(logLevel, "INFO");
            props.setProperty(steamPollingThreads, "1");
            defaults= new ServerProperties(props);
        }
        return defaults;
    }
    
    public final Properties properties;
    
    private ServerProperties(Properties properties) {
        this.properties= properties;
    }
    public Integer getUdpPort() {
        return Integer.valueOf(properties.getProperty(udpPort));
    }
    public Integer getHttpPort() {
        return Integer.valueOf(properties.getProperty(httpPort));
    }
    public Path getHttpRootDir() {
        return Paths.get(properties.getProperty(httpRootDir));
    }
    public String getPassword() {
        return properties.getProperty(password);
    }
    public Long getStatsMsgTTL() {
        return Long.valueOf(properties.getProperty(statsMsgTTL));
    }

    public String getDbURL() {
        return properties.getProperty(dbUrl);
    }
    public String getDbDriver() {
        return properties.getProperty(dbDriver);
    }
    public String getDbUser() {
        return properties.getProperty(dbUser);
    }
    public String getDbPassword() {
        return properties.getProperty(dbPassword);
    }
    public String getDbLibJar() {
        return properties.getProperty(dbLibJar);
    }
    public String getDbReaderClass() {
        return properties.getProperty(dbReaderClass);
    }
    public String getDbWriterClass() {
        return properties.getProperty(dbWriterClass);
    }
    public Integer getNumDbConn() {
        try {
            Integer nDbConn= Integer.valueOf(properties.getProperty(numDbConn));
            if (nDbConn < 4) {
                Common.logger.log(Level.WARNING, "Property num.db.conn requires a minimum values of 4, only {0} set.  Using minimum value of 4", nDbConn);
                return 4;
            }
            return nDbConn;
        } catch (NumberFormatException ex) {
            Common.logger.log(Level.WARNING, "Invalid number given for num.db.conn.  Using default value of 10", ex);
            return 10;
        }
    }

    public Level getLogLevel() {
        return Level.parse(properties.getProperty(logLevel));
    }
    public Integer getSteamPollingThreads() {
        try {
            Integer nthreads= Integer.valueOf(properties.getProperty(steamPollingThreads));
            if (nthreads <= 0) {
                Common.logger.log(Level.WARNING, "Property steam.polling.threads requires a minimum of 1  thread, only {0} set.  Using default value of 1", nthreads);
                return 1;
            }
            return nthreads;
        } catch (NumberFormatException ex) {
            Common.logger.log(Level.WARNING, "Invalid number given for steam.polling.threads.  Using default value of 1", ex);
            return 1;
        }
        
    }
}
