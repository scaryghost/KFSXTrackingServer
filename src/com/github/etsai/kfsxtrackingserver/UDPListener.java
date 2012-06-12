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
 * Listens for UDP packets from the KFStatsX mutator
 * @author etsai
 */
public class UDPListener implements Runnable {
    public static final Integer port= 6000;
    public static final Integer bufferSize= 1024;
    
    @Override
    public void run() {
        try {
            byte[] buffer= new byte[bufferSize];
            DatagramSocket socket= new DatagramSocket(port);
            DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
            
            while(true) {
                try {
                    socket.receive(packet);
                    String data= new String(packet.getData(), 0, packet.getLength()).toLowerCase();
                    
                    System.out.format("%s:%s-%s\n", packet.getAddress(), packet.getPort(), data);
                } catch (IOException ex) {
                    Logger.getLogger(UDPListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(UDPListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
