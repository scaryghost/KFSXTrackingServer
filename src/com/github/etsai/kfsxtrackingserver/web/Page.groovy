package com.github.etsai.kfsxtrackingserver.web;

import com.github.etsai.kfsxtrackingserver.Common
import java.util.logging.Level
import java.io.DataOutputStream
import groovy.xml.MarkupBuilder

public abstract class Page {
    private static def pages= ["","index.xml", "records.xml", "profile.xml"].collect {"/${it}" as String}
    private static def methods= ["GET", "HEAD"]
    private static def returnCodes= [200: "OK", 400: "Bad Request", 403: "Forbidden", 
        404: "Not Found", 500: "Internal Server Error", 501: "Not Implemented"]
    private static def extensions= ["html":"Content-Type: text/html", "xml":"Content-Type: application/xml" ,
        "xsl":"Content-Type: application/xslt+xml","css":"Content-Type: text/css","":""]

    public static String generate(DataOutputStream output, String[] request) {
        def writer= new StringWriter()
        def xml= new MarkupBuilder(writer)
        
        def code= 200, body
        def uri= URI.create(request[1])
        def filename= uri.getPath()
        def extension= filename.substring(filename.lastIndexOf(".")+1, filename.length());
        def queries= [:]
        
        if (uri.getQuery() != null) {
            uri.getQuery().tokenize("&").each {token ->
                def keyVal= token.split("=")
                queries[keyVal[0]]= keyVal[1]
            }
        }
        
        try {
            if(!methods.contains(request[0])) {
                code= 501
                body= "${code} ${returnCodes[code]}"
            } else {
                if(extension == "xsl" || extension == "css" || extension == "js"){
                    body= new File("./dist/${filename}").readLines().join("\n")
                } else if (!pages.contains(filename)) {
                    code= 404
                    body= "${code} ${returnCodes[code]}"
                    extension= "html"
                } else {
                    switch (filename) {
                        case "/":
                        case "/index.xml":
                            xml.mkp.xmlDeclaration(version:'1.0')
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/index.xsl"])
                            new Index().fillBody(xml)
                            break
                        case "/records.xml":
                            xml.mkp.xmlDeclaration(version:'1.0')
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/records.xsl"])
                            new Records(queries).fillBody(xml)
                            break
                        case "/profile.xml":
                            xml.mkp.xmlDeclaration(version:'1.0')
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/profile.xsl"])
                            new Profile(queries).fillBody(xml)
                            break
                    }
                    
                    body= writer.toString()
                }              
            }
        } catch (Exception ex) {
            def sw= new StringWriter()
            def pw= new PrintWriter(sw)

            extension= "html"
            code= 500
            body= "<pre>${code} ${returnCodes[code]}\n\n"
            body+= sw.toString() + "</pre>"
            Common.logger.log(Level.SEVERE, "Error generating webpage", ex);
        }
        
        def content= extensions[extension]
        def header= "HTTP/1.0 ${code} ${returnCodes[code]}\nConnection: close\nServer KFStats\n${content}\n\n"
        
        output.writeBytes(header)
        if (request != "HEAD")
            output.writeBytes(body)
    }

    public abstract String fillBody(def xmlBuilder);
}
