package com.github.etsai.kfsxtrackingserver;

import java.net.ServerSocket;
import java.net.Socket;

public class HTTPListener implements Runnable {
    private final int port;
    private ServerSocket httpSocket;

    public HTTPListener(int port) {
        this.port= port;
    }

    @Override
    public void run() {
        httpSocket= new ServerSocket(port);
        
        while(true) {
            Socket connection= webSocket.accept();
        }
    }

    static class Handler implements Runnable {
        private Socket connection;

        public Handler(Socket connection) {
            this.connection= connection;
        }

        @Override
        public void run() {
            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            DataOutputSream output= new DataOutputStream(connection.getOutputStream());

            String[] request= input.readLine().tokenize(" ");
            String[] fileSplit= request[1].tokenize("?=");
            String filepath= fileSplit[0] == "/" ? "/index.xml" : fileSplit[0];
            String extension= filepath.substring(filepath.lastIndexOf(".")+1, filepath.length());

            output.close();
        }
    }
}
