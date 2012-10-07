/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point into the lite version of the tracking server
 * @author etsai
 */
public class LiteMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException {
        byte[] buffer= new byte[Integer.MAX_VALUE];
        DatagramSocket socket= new DatagramSocket(6000);
        DatagramPacket udpPacket= new DatagramPacket(buffer, buffer.length);
        
        Common.properties= ServerProperties.getDefaults();
        
        while(true) {
            try {
                socket.receive(udpPacket);
                String data= new String(udpPacket.getData(), 0, udpPacket.getLength());
                
                Common.pool.submit(new Accumulator(data));
            } catch (IOException ex) {
                Logger.getLogger(LiteMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
