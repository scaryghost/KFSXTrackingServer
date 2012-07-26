package com.github.etsai.kfsxtrackingserver;


import java.util.Properties;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Central variables used by all classes.
 * The objects must be thread safe
 * @author etsai
 */
public class Common {
    public static final Logger logger= Logger.getLogger("KFSXTrackingServer");
    public static Properties properties;
    public static Data statsData;
}