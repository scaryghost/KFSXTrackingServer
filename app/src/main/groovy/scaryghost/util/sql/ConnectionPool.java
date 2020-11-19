package scaryghost.utils.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a pool of connections to an SQL database.
 * @author etsai
 */
public class ConnectionPool {
    /** Max number of connections in the pool */
    private int maxConnections;
    /** URL to the database */
    private String url;
    /** Properties for connecting to the database */
    private Properties dbProps;
    /** Open connections that are not being used, ordered by last used date */
    private final List<Connection> availableConnections;
    /** Connections that are currently being used */
    private final Set<Connection> usedConnections;
    
    /**
     * Creates a pool with a configurable connection limit
     * @param   maxConnections      Max number of connections in the pool
     */
    public ConnectionPool(int maxConnections) {
        this.maxConnections= maxConnections;
        dbProps= new Properties();
        availableConnections= new ArrayList<>(maxConnections);
        usedConnections= new HashSet<>();
    }
    /**
     * Creates a pool with the default number of 5 connections
     */
    public ConnectionPool() {
        this(5);
    }
    /**
     * Set the URL to the database
     * @param   url     URL to the database
     */
    public void setJdbcUrl(String url) {
        this.url= url;
    }
    /**
     * Set the user name for logging into the database
     * @param   user    Username to login
     */
    public void setDbUser(String user) {
        dbProps.setProperty("user", user);
    }
    /**
     * Set the password for logging into the database
     * @param   password    Password to login
     */
    public void setDbPassword(String password) {
        dbProps.setProperty("password", password);
    }
    /**
     * Set the max number of connections the pool will hold
     * @param   maxConnections  Max number of open connections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections= maxConnections;
    }
    /**
     * Set the JDBC driver name that is needed to connect to the database.  This version should be used 
     * if the driver is part of the default class path
     * @param   driverClassName  JDBC driver name
     * @throws ClassNotFoundException If the driver class cannot be loaded
     */
    public void setDbDriver(String driverClassName) throws ClassNotFoundException {
        Class.forName(driverClassName);
    }

    /**
     * Release a connection, adding it back to the pool of available connections.  If the connection 
     * to release is null or not a valid used connection in the pool, this function will not do anything
     * @param   conn    Connection to release
     */
    public synchronized void release(Connection conn) {
        if (conn != null && usedConnections.contains(conn)) {
            availableConnections.add(conn);
            usedConnections.remove(conn);
            notifyAll();
        }
    }
    /**
     * Get a connection from the pool.  If the pool is empty but max connections not reached, a new 
     * connection will be opened.  If max connections has been reached, the function will block until 
     * a connection is available.
     * @throws SQLException If a connection cannot be made to the database
     */
    public synchronized Connection getConnection() throws SQLException {
        if (availableConnections.isEmpty()) {
            if (usedConnections.size() >= maxConnections) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConnectionPool.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                availableConnections.add(DriverManager.getConnection(url, dbProps));
            }
        }
        usedConnections.add(availableConnections.get(0));
        return availableConnections.remove(0);
    }
    /**
     * Close all connections in the pool
     * @throws SQLException If an error occurred from closing a connection
     */
    public void close() throws SQLException {
        for(Connection conn: availableConnections) {
            conn.close();
        }
        for(Connection conn: usedConnections) {
            conn.close();
        }
    }
}

