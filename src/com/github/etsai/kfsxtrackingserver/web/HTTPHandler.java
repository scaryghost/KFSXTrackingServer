/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.web;

import com.github.etsai.kfsxtrackingserver.Common;
import com.github.etsai.kfsxtrackingserver.impl.SQLiteReader;
import com.github.etsai.kfsxtrackingserver.TCPListener;
import groovy.lang.GroovyClassLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 *
 * @author etsai
 */
public abstract class HTTPHandler implements Runnable {
        private final Socket connection;
        
        public HTTPHandler(Socket connection) {
            this.connection= connection;
        }
        
        public String generatePage(Path classpath, Path groovyFile, Connection conn, Map<String, String> queries) 
                throws SQLException, CompilationFailedException, IOException, InstantiationException, IllegalAccessException {
            GroovyClassLoader gcl= new GroovyClassLoader();
            gcl.addClasspath(classpath.toString());
            
            Resource webResource= (Resource)gcl.parseClass(groovyFile.toFile()).newInstance();
            webResource.setQueries(queries);
            webResource.setDataReader(new SQLiteReader(conn));

            return webResource.generatePage();
        }
        
        public abstract void processRequest(String request, OutputStream output);
        
        @Override
        public void run() {
            try (BufferedReader input= new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    OutputStream output= connection.getOutputStream()) {
                processRequest(input.readLine(), output);
            } catch (IOException ex) {
                Logger.getLogger(TCPListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }