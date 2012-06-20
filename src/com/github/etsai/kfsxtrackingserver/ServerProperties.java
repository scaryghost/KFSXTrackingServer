/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Stores properties for the tracking server
 * @author eric
 */
public class ServerProperties {
    private static Properties props;
    private static Properties defaults;
    
    public static final String propUdpPort= "udp.port";
    public static final String propPassword= "password";
    
    public synchronized static Properties load(String filename) throws IOException {
        if (props == null) {
            props= new Properties();
            props.load(new FileReader(filename));
        }
        
        return props;
    }
    
    public synchronized static Properties getDefaults() {
        if (defaults == null) {
            defaults= new Properties();
            defaults.setProperty(propUdpPort, "6000");
            defaults.setProperty(propPassword, "server");
        }
        return defaults;
    }
}
