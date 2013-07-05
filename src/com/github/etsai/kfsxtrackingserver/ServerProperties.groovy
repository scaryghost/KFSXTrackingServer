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
    
    public ServerProperties(Properties properties) {
        this.properties= properties
    }
    public ServerProperties(String filename) throws IOException {
        this.properties= new Properties()
        this.properties.load(new FileReader(filename))
    }
    private String getManualProperty(def key) {
        if (properties[key] == null) {
            throw new RequiredPropertyError("Property '$key' must be explicitly set in the properties file")
        }
        return properties[key]
    }
    public Integer getUdpPort() {
        def udpPort, defaultValue= "6000"
        
        try {
            udpPort= properties.getProperty("udp.port", defaultValue).toInteger()
        } catch (NumberFormatException ex) {
            Common.logger.log(Level.WARNING, "Invalid number given for udp port.  Using default port: $defaultValue", ex)
            udpPort= defaultValue.toInteger()
        }
        return udpPort
    }
    public Integer getHttpPort() {
        def httpPort, defaultValue= 8080

        try {
            httpPort= properties["http.port"].toInteger()
        } catch (NullPointerException ex) {
            Common.logger.log(Level.WARNING, "HTTP server disabled");
        } catch (NumberFormatException ex) {
            Common.logger.log(Level.WARNING, "Invalid number given for http port.  Using default port: $defaultValue", ex)
            httpPort= defaultValue
        }
        return httpPort
    }
    public Path getHttpRootDir() {
        return Paths.get(properties.getProperty("http.root.dir", "http"))
    }
    public String getPassword() {
        return getManualProperty("password")
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
        return getManualProperty("db.url")
    }
    public String getDbDriver() {
        return properties["db.driver"]
    }
    public String getDbUser() {
        return properties["db.user"]
    }
    public String getDbPassword() {
        return properties["db.password"]
    }
    public String getDbReaderScript() {
        return properties["db.reader.script"]
    }
    public String getDbWriterScript() {
        return properties["db.writer.script"]
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
        def nThreads, steamPollingThreads= "steam.polling.threads", defaultValue= 1

        try {
            nThreads= properties[steamPollingThreads].toInteger()
        } catch (NullPointerException ex) {
            Common.logger.log(Level.WARNING, "Disabling steam community polling", ex)
        } catch (NumberFormatException ex) {
            Common.logger.log(Level.WARNING, "Invalid number given for $steamPollingThreads.  Using default value of $defaultValue", ex)
            nThreads= defaultValue
        }
        return nThreads
    }
}
