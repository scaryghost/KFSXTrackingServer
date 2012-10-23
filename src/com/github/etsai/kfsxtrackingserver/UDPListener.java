/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.logger;
import static com.github.etsai.kfsxtrackingserver.Common.pool;
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
    public synchronized void run() {
        try {
            DataWriter writer= new DataWriter();
            byte[] buffer= new byte[bufferSize];
            DatagramSocket socket= new DatagramSocket(port);
            DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
            logger.log(Level.INFO, "Listening for stats packets on port: {0}", port);
            
            while(true) {
                try {
                    socket.receive(packet);
                    String data= new String(packet.getData(), 0, packet.getLength());
                    logger.info(String.format("Received UDP packet from %s:%d", 
                            packet.getAddress().getHostAddress(), packet.getPort()));
                    pool.submit(new Accumulator(data, writer));
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error reading data on UDP socket", ex);
                }
            }
        } catch (SocketException ex) {
            logger.log(Level.SEVERE, "Error creating DatagramSocket", ex);
        }
        
    }
}
