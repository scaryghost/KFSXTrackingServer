package com.github.etsai.kfsxtrackingserver.web;

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.logging.Level
import java.util.TimeZone

public abstract class Page {
    private static def methods= ["GET", "HEAD"]
    private static def returnCodes= [200: "OK", 400: "Bad Request", 403: "Forbidden", 404: "Not Found", 500: "Internal Server Error", 501: "Not Implemented"]
    private static def extensions= ["html":"text/html", "xml":"application/xml", "xsl":"application/xslt+xml", "css":"text/css", 
        "js":"text/javascript", "json":"application/json", "ico":"image/vdn.microsoft.icon"]

    public static String generate(OutputStream output, String[] request) {
        def writer= new StringWriter()
        def xml= new MarkupBuilder(writer)
        
        def code= 200, body
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
            if(!methods.contains(request[0])) {
                code= 501
                body= "${code} ${returnCodes[code]}"
            } else {
                if (extension == "xsl" || extension == "css" || extension == "js" || extension == "ico") {
                    body= new File(filename)
                } else {
                    xml.mkp.xmlDeclaration(version:'1.0')
                    switch (filename) {
                        case "index.xml":
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/index.xsl"])
                            Index.fillBody(xml)
                            body= writer.toString()
                            break
                        case "":
                            extension= "html"
                        case "index.html":
                            def nav= ["totals", "difficulties", "levels", "deaths"].plus(WebCommon.getCategories()) << "records"
                            body= WebCommon.generateHtml(nav, null)
                            break
                        case "records.xml":
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/records.xsl"])
                            Records.fillBody(xml, queries)
                            body= writer.toString()
                            break
                        case "profile.xml":
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/profile.xsl"])
                            Profile.fillBody(xml, queries)
                            body= writer.toString()
                            break
                        case "sessions.xml":
                            xml.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/sessions.xsl"])
                            Sessions.fillBody(xml, queries)
                            body= writer.toString()
                            break
                        case "data.json":
                            body= DataJson.fillBody(queries)
                            break
                        case "data.html":
                            body= DataHtml.fillBody(queries)
                            break
                        case "profile.html":
                            def nav= ["profile"].plus(WebCommon.getCategories()) << "sessions"
                            body= WebCommon.generateHtml(nav, queries["steamid64"])
                            break
                        default:
                            code= 404
                            body= "${code} ${returnCodes[code]}"
                            extension= "html"
                            break
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
            Common.logger.log(Level.SEVERE, "Error generating webpage", ex);
        }
        
        def httpFormat= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
        httpFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        def date= httpFormat.format(Calendar.getInstance().getTime())
        
        def content= extensions[extension]
        def header= ["HTTP/1.1 ${code} ${returnCodes[code]}", "Connection: close", "Date: ${date}", "Content-Type: ${content}", "Content-Length: ${body.size()}", "\n"].join("\n")
        
        Common.logger.finest("HTTP Response: ${header}")
        output.write(header.getBytes())
        if (request[0] != "HEAD")
            output.write(body.getBytes())
    }
}
