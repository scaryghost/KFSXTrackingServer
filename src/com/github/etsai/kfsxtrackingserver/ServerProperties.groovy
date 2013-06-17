/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import java.io.FileReader
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import java.util.logging.Level

/**
 * Stores properties for the tracking server
 * @author etsai
 */
public class ServerProperties {
    public class RequiredPropertyError extends Error {
        public RequiredPropertyError(String msg) {
            super(msg)
        }
    }

    private final Properties properties
    
    public ServerProperties(String filename) throws IOException {
        this.properties= new Properties()
        this.properties.load(new FileReader(filename))
    }
    public Integer getUdpPort() {
        return properties.getProperty("udp.port", "6000").toInteger()
    }
    public Integer getHttpPort() {
        return properties.getProperty("http.port", "8080").toInteger()
    }
    public Path getHttpRootDir() {
        return Paths.get(properties.getProperty("http.root.dir", "http"))
    }
    public String getPassword() {
        def password= "password"
        if (properties[password] == null) {
            throw new RequiredPropertyError("Missing required property: $password")
        }
        return properties[password]
    }
    public Long getStatsMsgTTL() {
        def ttl, statsMsgTTL= "stats.msg.ttl", defaultValue= "60000"

        try {
            ttl= properties.getProperty(statsMsgTTL, defaultValue).toLong()
        } catch (NumberFormatException ex) {
            Common.logger.log(Level.WARNING, "Invalid number given for $statsMsgTTL.  Using default value of $defaultValue", ex)
            ttl= defaultValue.toLong()
        }
        return ttl
    }

    public String getDbURL() {
        return properties.getProperty("db.url", "jdbc:sqlite:share/etc/kfsxdb.sqlite3")
    }
    public String getDbDriver() {
        return properties.getProperty("db.driver", "org.sqlite.JDBC")
    }
    public String getDbUser() {
        return properties["db.user"]
    }
    public String getDbPassword() {
        return properties["db.password"]
    }
    public String getDbLibJar() {
        return properties["db.lib.jar"]
    }
    public String getDbReaderClass() {
        return properties.getProperty("db.reader.class", "com.github.etsai.kfsxtrackingserver.impl.SQLiteReader")
    }
    public String getDbWriterClass() {
        return properties.getProperty("db.writer.class", "com.github.etsai.kfsxtrackingserver.impl.SQLiteWriter")
    }
    public Integer getNumDbConn() {
        def nDbConn, numDbConn= "num.db.conn", defaultValue= "10"

        try {
            nDbConn= properties.getProperty(numDbConn, defaultValue).toInteger()
            if (nDbConn < 2) {
                Common.logger.log(Level.WARNING, "Property $numDbConn requires a minimum values of 2, only $nDbConn set.  Using minimum value of 2")
                nDbConn= 2
            }
        } catch (NumberFormatException ex) {
            Common.logger.log(Level.WARNING, "Invalid number given for $numDbConn.  Using default value of $defaultValue", ex)
            nDbConn= defaultValue.toInteger()
        }
        return nDbConn
    }

    public Level getLogLevel() {
        return Level.parse(properties.getProperty("log.level", "INFO"))
    }
    public Integer getSteamPollingThreads() {
        def nThreads, steamPollingThreads= "steam.polling.threads", defaultValue= "1"

        try {
            nThreads= properties.getProperty(steamPollingThreads, defaultValue).toInteger()
            if (nThreads <= 0) {
                Common.logger.log(Level.WARNING, "Property $steamPollingThreads requires a minimum of 1 thread, only $nThreads set.  Using default value of 1")
                nThreads= 1
            }
        } catch (NumberFormatException ex) {
            Common.logger.log(Level.WARNING, "Invalid number given for $steamPollingThreads.  Using default value of $defaultValue", ex)
            nThreads= defaultValue.toInteger()
        }
        return nThreads
    }
}
