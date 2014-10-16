/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * Central variables used by all classes.  The objects must be thread safe
 * @author etsai
 */
public class Common {
    /** Logging object to use for all logging */
    public static final Logger logger= Logger.getLogger("KFSXTrackingServer");
    /** Streams to the default standard out and standard error */
    public static PrintStream oldStdOut, oldStdErr;
}
