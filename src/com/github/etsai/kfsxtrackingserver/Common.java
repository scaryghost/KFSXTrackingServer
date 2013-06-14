/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.utils.sql.ConnectionPool;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Central variables used by all classes.  The objects must be thread safe
 * @author etsai
 */
public class Common {
    /** Pool of open connections to the database */
    public static ConnectionPool connPool;
    /** Logging object to use for all logging */
    public static final Logger logger= Logger.getLogger("KFSXTrackingServer");
    /** Streams to the default standard out and standard error */
    public static PrintStream oldStdOut, oldStdErr;
    public static Class<DataWriter> dataWriterClass;
    public static Class<DataReader> dataReaderClass;
    /**
     * Retrieves an open connection, executes the desired SQL, and releases the connection
     * @param stmt SQL statement to execute
     * @throws SQLException If a connection cannot be made to the database
     */
    public static void executeStmt(String stmt) throws SQLException {
        Connection conn= connPool.getConnection();
        conn.createStatement().execute(stmt);
        connPool.release(conn);
    }
}