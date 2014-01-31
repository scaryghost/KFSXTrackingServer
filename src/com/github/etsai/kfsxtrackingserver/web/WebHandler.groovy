package com.github.etsai.kfsxtrackingserver.web;

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.kfsxtrackingserver.ReaderWrapper
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
    private static def webpagesInfo= "webpages.xml"
    private static def mimeTypes= ["html":"text/html", "xml":"application/xml", "xsl":"application/xslt+xml", "css":"text/css", 
        "js":"text/javascript", "json":"application/json", "ico":"image/vdn.microsoft.icon"]
    
    private final def connPool, httpRootDir, readerClass, scripts
    private def lastModified, resources, webpagesFile, httpClasspath, gcl
    
    public WebHandler(int port, Path httpRootDir, ConnectionPool connPool, Class<?> readerClass){
        super(port)
        this.httpRootDir= httpRootDir.toRealPath()
        this.connPool= connPool
        this.readerClass= readerClass

        scripts= [:]
        webpagesFile= httpRootDir.resolve(webpagesInfo).toFile()
        setupResources()
        Common.logger.log(Level.CONFIG, "Listening for http requests on port: $port")
    }

    private void setupResources() {
        System.err.println "Setting up resources"
        lastModified= webpagesFile.lastModified()
        def webpagesXmlRoot= new XmlSlurper().parse(webpagesFile)
        httpClasspath= httpRootDir.resolve(webpagesXmlRoot.@classpath.toString())

        gcl= new GroovyClassLoader();
        gcl.addClasspath(httpClasspath.toString());
        
        resources= [:]
        webpagesXmlRoot.page.each {
            def scriptFile= httpClasspath.resolve(it.@script.toString()).toFile()
            def webpage= it.@name.toString()
            
            resources[webpage]= scriptFile
            if (!scripts.containsKey(scriptFile)) {
                Common.logger.log(Level.INFO, "Parsing file: `$scriptFile`")
                scripts[scriptFile]= [lastmodified: scriptFile.lastModified(), 
                        clazz: gcl.parseClass(scriptFile)]
            } else {
                if (scripts[scriptFile].lastmodified != scriptFile.lastModified()) {
                    Common.logger.log(Level.INFO, "Reparsing file: `$scriptFile`")
                    scripts[scriptFile].lastmodified= scriptFile.lastModified()
                    scripts[scriptFile].clazz= gcl.parseClass(scriptFile)
                }
            }
        }
    }
    
    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        def filename= uri.substring(1)
        def extension= filename.isEmpty() ? "html" : filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        def status= Status.OK
        def msg, conn
        
        try {
            if (lastModified != webpagesFile.lastModified()) {
                setupResources()
            }
            def resource= resources[filename]
            if (resource == null) {
                try {
                    def filePath= httpRootDir.resolve(filename)
                    System.err.println "Full path: $filePath"
                    if (filePath.toRealPath().startsWith(httpRootDir)) {
                        msg= new FileInputStream(filePath.toFile())
                    } else {
                        status= Status.FORBIDDEN
                        msg= status.getDescription()
                        extension= "html"
                    }
                } catch (NoSuchFileException ex) {
                    Common.logger.log(Level.SEVERE, "File: $filename does not exist", ex);
                    status= Status.NOT_FOUND
                    msg= status.getDescription()
                    extension= "html"
                }
            } else {
                conn= connPool.getConnection()
                def script= scripts[resource]
                if (script.lastmodified != resource.lastModified()) {
                    Common.logger.log(Level.INFO, "Reparsing file: `${resource}`")
                    script.lastmodified= resource.lastModified()
                    script.clazz= gcl.parseClass(resource)
                }
                def webResource= (Resource)script.clazz.newInstance()
                webResource.setQueries(parms)
                webResource.setDataReader(new ReaderWrapper(readerClass, conn))
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
