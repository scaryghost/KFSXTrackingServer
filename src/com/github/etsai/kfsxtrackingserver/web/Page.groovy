package com.github.etsai.kfsxtrackingserver.web;

import com.github.etsai.kfsxtrackingserver.Common
import java.util.logging.Level
import java.io.DataOutputStream
import groovy.xml.MarkupBuilder

public abstract class Page {
    private static def pages= ["index.xml", "records.xml", "profile.xml"].collect {"/${it}" as String}
    private static def methods= ["GET", "HEAD"]
    private static def returnCodes= [200: "OK", 400: "Bad Request", 403: "Forbidden", 
        404: "Not Found", 500: "Internal Server Error", 501: "Not Implemented"]
    private static def extensions= ["html":"Content-Type: text/html", "xml":"Content-Type: application/xml" ,
        "xsl":"Content-Type: application/xslt+xml","css":"Content-Type: text/css","":""]

    public static String generate(DataOutputStream output, String[] request) {
        def writer= new StringWriter()
        def xml= new MarkupBuilder(writer)
        
        def code= 200, body
        def fileSplit= request[1].tokenize("\\?=");
        def filename= fileSplit[0] == "/" ? "/index.xml" : fileSplit[0]
        def extension= filename.substring(filename.lastIndexOf(".")+1, filename.length());
        
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
                            xml.mkp.xmlDeclaration(version:'1.0')
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/index.xsl"])
                            new Index().fillBody(xml)
                            break
                        case "/index.xml":
                            xml.mkp.xmlDeclaration(version:'1.0')
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/index.xsl"])
                            new Index().fillBody(xml)
                            break
                        case "/records.xml":
                            xml.mkp.xmlDeclaration(version:'1.0')
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/records.xsl"])
                            new Records().fillBody(xml)
                            break
                        case "/profile.xml":
                            xml.mkp.xmlDeclaration(version:'1.0')
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/profile.xsl"])
                            new Profile(fileSplit[2]).fillBody(xml)
                            break
                    }
                    
                    body= writer.toString()
                    //body= pageActions[filepath](fileSplit)
                }              
            }
        } catch (Exception ex) {
            def sw= new StringWriter()
            def pw= new PrintWriter(sw)

            extension= "html"
            code= 500
            body= "<pre>${code} ${returnCodes[code]}\n\n"
            ex.printStackTrace(pw)
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
