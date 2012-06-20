/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Core.logger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;

/**
 * Listens for UDP packets from the KFStatsX mutator
 * @author etsai
 */
public class UDPListener implements Runnable {
    private final Integer port;
    public static final Integer bufferSize= 1024;
    
    public UDPListener(Integer port) {
        this.port= port;
    }
    
    @Override
    public void run() {
        try {
            byte[] buffer= new byte[bufferSize];
            DatagramSocket socket= new DatagramSocket(port);
            DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
            logger.log(Level.INFO, "Listening on port: {0}", port);
            
            while(true) {
                try {
                    socket.receive(packet);
                    String data= new String(packet.getData(), 0, packet.getLength()).toLowerCase();
                    
                    System.out.format("%s:%s-%s\n", packet.getAddress(), packet.getPort(), data);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error reading data on UDP socket", ex);
                }
            }
        } catch (SocketException ex) {
            logger.log(Level.SEVERE, "Error creating DatagramSocket", ex);
        }
        
    }
}
