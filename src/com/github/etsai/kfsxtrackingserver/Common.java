/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import com.github.etsai.utils.sql.ConnectionPool;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Central variables used by all classes.
 * The objects must be thread safe
 * @author etsai
 */
public class Common {
    public static ConnectionPool connPool;
    public static final Logger logger= Logger.getLogger("KFSXTrackingServer");
    public static ExecutorService pool;
    public static PrintStream oldStdOut, oldStdErr;
    
    public static void executeStmt(String stmt) throws SQLException {
        Connection conn= connPool.getConnection();
        conn.createStatement().execute(stmt);
        connPool.release(conn);
    }
}