/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.ServerProperties.UDP_PORT;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the tracking server
 * @author etsai
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Properties props;
        UDPListener listener;
        Thread udpTh;
        CommandLine clom= CommandLine.parse(args);
        
        try {
            props= ServerProperties.load(clom.getPropertiesFilename());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            props= ServerProperties.getDefaults();
        }
        listener= new UDPListener(Integer.valueOf(props.getProperty(UDP_PORT)));
        udpTh= new Thread(listener);
        
        udpTh.start();
    }
}
