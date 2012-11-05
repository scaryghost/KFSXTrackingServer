/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import java.io.FileReader;
import java.io.IOException;
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
    public static final String password= "password";
    public static final String statsMsgTTL= "stats.msg.ttl";
    public static final String dbName= "db.name";
    public static final String logLevel= "log.level";
    public static final String numThreads= "num.threads";
    
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
            props.setProperty(password, "server");
            props.setProperty(statsMsgTTL, "60000");
            props.setProperty(dbName, "kfsxdb.sqlite");
            props.setProperty(logLevel, "INFO");
            props.setProperty(numThreads, "12");
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
    public String getPassword() {
        return properties.getProperty(password);
    }
    public Long getStatsMsgTTL() {
        return Long.valueOf(properties.getProperty(statsMsgTTL));
    }
    public String getDbName() {
        return properties.getProperty(dbName);
    }
    public Level getLogLevel() {
        return Level.parse(properties.getProperty(logLevel));
    }
    public Integer getNumThreads() {
        return Integer.valueOf(properties.getProperty(numThreads));
    }
}
