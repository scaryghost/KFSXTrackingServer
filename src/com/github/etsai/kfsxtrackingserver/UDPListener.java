/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Core.logger;
import static com.github.etsai.kfsxtrackingserver.Packet.Type.Match;
import static com.github.etsai.kfsxtrackingserver.Packet.Type.Player;
import com.github.etsai.kfsxtrackingserver.impl.PlayerPacket;
import com.github.etsai.kfsxtrackingserver.impl.PlayerContent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            Handler handler= new Handler();
            Thread handlerTh= new Thread(handler);
            byte[] buffer= new byte[bufferSize];
            DatagramSocket socket= new DatagramSocket(port);
            DatagramPacket packet= new DatagramPacket(buffer, buffer.length);
            logger.log(Level.INFO, "Listening on port: {0}", port);
            
            handlerTh.start();
            while(true) {
                try {
                    socket.receive(packet);
                    String data= new String(packet.getData(), 0, packet.getLength()).toLowerCase();
                    
                    handler.add(data);
                    System.out.format("%s:%s-%s\n", packet.getAddress(), packet.getPort(), data);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Error reading data on UDP socket", ex);
                }
            }
        } catch (SocketException ex) {
            logger.log(Level.SEVERE, "Error creating DatagramSocket", ex);
        }
        
    }
    
    static class Handler implements Runnable {
        private static List<String> packets= Collections.synchronizedList(new ArrayList());
        
        public synchronized void add(String data) {
            packets.add(data);
            notify();
        }
        @Override
        public synchronized void run() {
            while(true) {
                if (packets.isEmpty()) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UDPListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Packet packet= Packet.parse(packets.remove(0));
                
                switch (packet.getType()) {
                    case Match:
                        Core.matchContent.accumulate(packet);
                        break;
                    case Player:
                        String playerId= (String) packet.getData(PlayerPacket.keyPlayerId);
                        if (!Core.playerContents.containsKey(playerId)) {
                            Core.playerContents.put(playerId, new PlayerContent(playerId));
                        }
                        Core.playerContents.get(playerId).accumulate(packet);
                        break;
                    default:
                        System.err.println("Unrecognized packet type");
                        break;
                }
            }
        }
        
    }
}
