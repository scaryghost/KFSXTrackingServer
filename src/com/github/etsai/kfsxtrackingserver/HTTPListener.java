package com.github.etsai.kfsxtrackingserver;

import static com.github.etsai.kfsxtrackingserver.Common.logger;
import static com.github.etsai.kfsxtrackingserver.Common.pool;
import com.github.etsai.kfsxtrackingserver.web.Page;
import groovy.sql.Sql;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.logging.Level;

public class HTTPListener implements Runnable {
    public static Path httpRootDir;
    
    private final int port;
    private Sql sql;

    public HTTPListener(int port, Sql sql) {
        this.port= port;
        this.sql= sql;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Listening for http requests on port: {0}", port);
        
        try {
            ServerSocket httpSocket= new ServerSocket(port);
            while(true) {
                Socket connection= httpSocket.accept();
                logger.info(String.format("Received TCP connection from %s:%d", 
                        connection.getInetAddress().getHostAddress(), connection.getPort()));
                
                pool.submit(new Handler(connection));
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
            try (BufferedReader input= new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String request= input.readLine();
                String[] requestParts= request.split(" ");

                logger.log(Level.FINEST, "HTTP request: {0}", request);
                Page.generate(connection.getOutputStream(), requestParts, httpRootDir);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
}
