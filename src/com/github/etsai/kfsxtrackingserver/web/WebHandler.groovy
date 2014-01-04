package com.github.etsai.kfsxtrackingserver.web;

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.kfsxtrackingserver.Reader
import com.github.etsai.utils.sql.ConnectionPool
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.Response.Status
import java.io.FileInputStream
import java.lang.reflect.Constructor
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.NoSuchFileException
import java.sql.Connection
import java.util.logging.Level

public class WebHandler extends NanoHTTPD {
    private static def webpages= "webpages.xml"
    private static def mimeTypes= ["html":"text/html", "xml":"application/xml", "xsl":"application/xslt+xml", "css":"text/css", 
        "js":"text/javascript", "json":"application/json", "ico":"image/vdn.microsoft.icon"]
    
    final def connPool, httpRootDir, readerClass
    
    public WebHandler(int port, Path httpRootDir, ConnectionPool connPool, Class<?> readerClass){
        super(port)
        this.httpRootDir= httpRootDir
        this.connPool= connPool
        this.readerClass= readerClass

        Common.logger.log(Level.CONFIG, "Listening for http requests on port: $port")
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        def filename= uri.substring(1)
        def extension= filename.isEmpty() ? "html" : filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        def status= Status.OK
        def msg, conn
        
        try {
            def xmlRoot= new XmlSlurper().parse(httpRootDir.resolve(webpages).toFile())
            def resources= [:]
            def root= httpRootDir.resolve(xmlRoot.@classpath.toString())

            xmlRoot.page.each {
                resources[it.@name.toString()]= root.resolve(it.@script.toString())
            }
            if (resources[filename] == null) {
                try {
                    def filePath= Paths.get(filename)
                    if (filePath.toRealPath().startsWith(httpRootDir.toRealPath())) {
                        msg= new FileInputStream(filePath.toFile())
                    } else {
                        status= Status.FORBIDDEN
                        msg= status.getDescription()
                        extension= "html"
                    }
                } catch (NoSuchFileException ex) {
                    Common.logger.log(Level.SEVERE, "File: $filename oes not exist", ex);
                    status= Status.NOT_FOUND
                    msg= status.getDescription()
                    extension= "html"
                }
            } else {
                conn= connPool.getConnection()
                
                def gcl= new GroovyClassLoader();
                gcl.addClasspath(root.toString());
            
                def webResource= (Resource)gcl.parseClass(resources[filename].toFile()).newInstance()
                webResource.setQueries(parms)
                webResource.setDataReader(new Reader(readerClass, conn))
                msg= webResource.generatePage()
                
                if (parms.xml != null) {
                    extension= "xml"
                }
            }
        } catch (Exception ex) {
            def sw= new StringWriter()
            def pw= new PrintWriter(sw)

            extension= "html"
            status= Status.INTERNAL_ERROR
            ex.printStackTrace(pw)
            msg= "<pre>${status.getDescription()}\n\n${sw.toString()}</pre>"
            Common.logger.log(Level.SEVERE, "Error generating webpage", ex)
        } finally {
            connPool.release(conn)
        }
        
        return new Response(status, mimeTypes[extension], msg)
    }
}
