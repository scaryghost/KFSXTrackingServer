package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.kfsxtrackingserver.impl.SQLiteReader
import com.github.etsai.utils.Time
import groovy.sql.Sql
import java.io.BufferedReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.logging.Level
import java.util.TimeZone
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.NoSuchFileException

public class HTTPHandler implements Runnable {
    private static def webpages= "webpages.xml"
    private static def methods= ["GET", "HEAD"]
    private static def returnCodes= [200: "OK", 400: "Bad Request", 403: "Forbidden", 404: "Not Found", 500: "Internal Server Error", 501: "Not Implemented"]
    private static def extensions= ["html":"text/html", "xml":"application/xml", "xsl":"application/xslt+xml", "css":"text/css", 
        "js":"text/javascript", "json":"application/json", "ico":"image/vdn.microsoft.icon"]

    private final def input, output, httpRootDir, id
    
    public HTTPHandler(BufferedReader input, OutputStream output, Path httpRootDir, int id) {
        this.input= input
        this.output= output
        this.httpRootDir= httpRootDir
        this.id= id
    }
    
    @Override
    public void run() {
        def line= input.readLine()
        def request= line.tokenize(" ")
        Common.logger.info("HTTP request ($id): $line")
        
        def code= 200, body, conn
        def uri= URI.create(request[1])
        def filename= uri.getPath().substring(1)
        def extension= filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        
        def queries= [:]
        if (uri.getQuery() != null) {
            uri.getQuery().tokenize("&").each {token ->
                def keyVal= token.split("=")
                queries[keyVal[0]]= keyVal[1]
            }
        }
         
        try {
            def xmlRoot= new XmlSlurper().parse(httpRootDir.resolve(webpages).toFile())
            def resources= [:]
            def root= httpRootDir.resolve(xmlRoot.@classpath.toString())

            xmlRoot.page.each {
                resources[it.@name.toString()]= root.resolve(it.@script.toString())
            }
            
            if(!methods.contains(request[0])) {
                code= 501
                body= "${code} ${returnCodes[code]}"
            } else {
                if (resources[filename] == null) {
                    try {
                        def filePath= Paths.get(filename)
                        if (filePath.toRealPath().startsWith(httpRootDir.toRealPath())) {
                            body= filePath.toFile()
                        } else {
                            code= 403
                            body= "${code} ${returnCodes[code]}"
                            extension= "html"
                        }
                    } catch (NoSuchFileException ex) {
                        Common.logger.log(Level.SEVERE, "File: $filename does not exist", ex);
                        code= 404
                        body= "${code} ${returnCodes[code]}"
                        extension= "html"
                    }
                } else {
                    def gcl= new GroovyClassLoader()
                    gcl.addClasspath(root.toString())
                    def clazz = gcl.parseClass(resources[filename].toFile())
                    def aScript = (Resource)clazz.newInstance();
                    
                    conn= Common.connPool.getConnection()
                    aScript.setQueries(queries)
                    aScript.setDataReader(new SQLiteReader(conn))
                    body= aScript.generatePage()

                    if (queries.xml != null) {
                        extension= "xml"
                    }
                }              
            }
        } catch (Exception ex) {
            def sw= new StringWriter()
            def pw= new PrintWriter(sw)

            extension= "html"
            code= 500
            ex.printStackTrace(pw)
            body= "<pre>${code} ${returnCodes[code]}\n\n${sw.toString()}</pre>"
            Common.logger.log(Level.SEVERE, "Error generating webpage", ex)
        } finally {
            Common.connPool.release(conn)
        }
        
        def httpFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
        httpFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        def date= httpFormat.format(Calendar.getInstance().getTime())
        
        def content= extensions[extension]
        def header= ["HTTP/1.1 ${code} ${returnCodes[code]}", "Connection: close", "Date: ${date}", "Content-Type: ${content}", 
                "Content-Length: ${body.size()}", "\n"].join("\n")
        
        Common.logger.info("HTTP Response ($id): ${header}")
        output.write(header.getBytes())
        if (request[0] != "HEAD")
            output.write(body.getBytes())
    }
}
