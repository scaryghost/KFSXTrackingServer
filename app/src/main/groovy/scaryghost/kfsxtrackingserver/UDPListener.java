/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scaryghost.kfsxtrackingserver;

import static scaryghost.kfsxtrackingserver.Common.logger;
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
    private final Accumulator dataAccumulator;
    public static final Integer bufferSize= 32767;
    
    public UDPListener(Integer port, Accumulator dataAccumulator) {
        this.port= port;
        this.dataAccumulator= dataAccumulator;
    }
    
    @Override
    public synchronized void run() {
        try {
            byte[] buffer= new byte[bufferSize];
            DatagramSocket socket= new DatagramSocket(port);
            DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
            logger.log(Level.CONFIG, "Listening for stats packets on port: {0}", port);
            
            while(true) {
                try {
                    socket.receive(packet);
                    String data= new String(packet.getData(), 0, packet.getLength());
                    logger.info(String.format("Received UDP packet from %s:%d", 
                            packet.getAddress().getHostAddress(), packet.getPort()));
                    logger.info(String.format("Data= %s", data));
                    dataAccumulator.accumulate(packet);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error reading data on UDP socket", ex);
                }
            }
        } catch (SocketException ex) {
            logger.log(Level.SEVERE, "Error creating DatagramSocket", ex);
        }
        
    }
}
