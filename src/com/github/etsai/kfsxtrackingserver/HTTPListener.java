package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.logger;
import com.github.etsai.kfsxtrackingserver.web.Page;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

public class HTTPListener implements Runnable {
    private final int port;
    private ServerSocket httpSocket;

    public HTTPListener(int port) {
        this.port= port;
    }

    @Override
    public void run() {
        try {
            httpSocket= new ServerSocket(port);
            
            while(true) {
                Socket connection= httpSocket.accept();
                Thread th= new Thread(new Handler(connection));
                
                th.start();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    static class Handler implements Runnable {
        private Socket connection;

        public Handler(Socket connection) {
            this.connection= connection;
        }

        @Override
        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())) {
                    String[] request= input.readLine().split(" ");
                    
                    Page.generate(output, request);
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
}
