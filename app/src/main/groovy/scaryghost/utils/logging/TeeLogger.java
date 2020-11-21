package scaryghost.utils.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Logs all data being sent to a PrintStream
 * @author etsai
 */
public class TeeLogger extends OutputStream {
    /**
     * Creates a FileWriter that will write to log file
     * @param execName Executable name
     * @param baseDir Directory to store the log file
     * @return FileWriter object ready to write to file
     * @throws IOException If the FileWriter object cannot be created
     */
    public static FileWriter getFileWriter(String execName, File baseDir) throws IOException {
        String localHostAddress;
            
        try {
            localHostAddress= InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            ex.printStackTrace(System.err);
            localHostAddress= "unknown";
        }
        
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        String filename= String.format("%s.%s.%tY%<tm%<td-%<tH%<tM%<tS.log", 
                execName, localHostAddress, new Date());
        
        return new FileWriter(new File(baseDir, filename)); 
    }
    
    FileWriter log;
    PrintStream stream;
    
    /**
     * Create a TeeLogger object, logging data from the given stream to the writer
     * @param log FileWriter to send the output to
     * @param stream Stream to log
     */
    public TeeLogger(FileWriter log, PrintStream stream) {
        this.log= log;
        this.stream= stream;
    }
    
    /**
     * Flush both writer and original stream
     * @throws IOException If an error occurred while flushing both streams
     */
    @Override public void flush() throws IOException {
        super.flush();
        log.flush();
        stream.flush();
    }
    
    /**
     * Close both writer and original stream
     * @throws IOException If an error occurred while attempting to close either stream
     */
    @Override public void close() throws IOException {
        super.close();
        log.close();
    }
    
    /**
     * Write the character to both log and original stream
     * @param b Character to write
     * @throws IOException If an error occurred while writing to log and stream
     */
    @Override public void write(int b) throws IOException {
        log.write(b);
        stream.write(b);
    }
    
}
