/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver

import java.io.FileWriter
import java.io.PrintStream
import java.io.OutputStream

/**
 *
 * @author etsai
 */

public class TeeLogger extends OutputStream {
    private def logFile, oldStream
    
    public TeeLogger(FileWriter logFile, PrintStream oldStream) {
        this.logFile= logFile
        this.oldStream= oldStream
    }
    
    @Override
    public void flush() {
        super.flush()
        logFile.flush()
        oldStream.flush()
    }
    
    @Override
    public void close() {
        super.close()
        logFile.close()
    }
    
    @Override
    public void write(int b) {
        logFile.write(b)
        oldStream.write(b)
    }
}