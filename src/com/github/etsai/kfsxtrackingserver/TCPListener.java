package com.github.etsai.kfsxtrackingserver;

import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;

public class TCPListener extends NanoHTTPD implements Runnable {
    final Path httpRootDir;
    
    public TCPListener(int port, Path httpRootDir){
        super(port);
        this.httpRootDir= httpRootDir;
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Debug Server</title></head>");
        sb.append("<body>");
        sb.append("<h1>Response</h1>");
        sb.append("<p><blockquote><b>URI -</b> ").append(String.valueOf(uri)).append("<br />");
        sb.append("<b>Method -</b> ").append(String.valueOf(method)).append("</blockquote></p>");
        sb.append("<h3>Headers</h3><p><blockquote>").append(String.valueOf(header)).append("</blockquote></p>");
        sb.append("<h3>Parms</h3><p><blockquote>").append(String.valueOf(parms)).append("</blockquote></p>");
        sb.append("<h3>Files</h3><p><blockquote>").append(String.valueOf(files)).append("</blockquote></p>");
        sb.append("</body>");
        sb.append("</html>");
        return new Response(sb.toString());
    }

    @Override
    public void run() {
        try {
            start();
        } catch (IOException ex) {
            Common.logger.log(Level.SEVERE, null, ex);
        }
    }
}
