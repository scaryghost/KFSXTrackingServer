package com.github.etsai.kfsxtrackingserver.web;

public abstract class Page {
    private static def pages= ["index.xml", "playerinfo.xml", "players.xml"].collect {"/${it}"}
    private static def methods= ["GET", "HEAD"]
    private static def returnCodes= [200: "OK", 400: "Bad Request", 403: "Forbidden", 
        404: "Not Found", 500: "Internal Server Error", 501: "Not Implemented"]
    private static def extensions= ["html":"Content-Type: text/html", "xml":"Content-Type: application/xml" ,
        "xsl":"Content-Type: application/xslt+xml","css":"Content-Type: text/css","":""]

    public static String generate(String filename) {
        def extension= filename.substring(filename.lastIndexOf(".")+1, filename.length());

        if (!pages.contains(filename)) {
            
        }
/*
            def header= "HTTP/1.0 "
            

            try {
                request= input.readLine().tokenize(" ")
                String client= String.format("%s:%s", socket.getInetAddress().getHostAddress(), socket.getPort());
                logger.info("${client}-${request}")
                
                def fileSplit= request[1].tokenize("?=")
                def filepath= fileSplit[0] == "/" ? "/index.xml" : fileSplit[0]
                def mid= filepath.lastIndexOf(".")
                
                extension= filepath.substring(mid+1, filepath.length())
                if(!methods.contains(request[0])) {
                    code= 501
                    body= "${code} ${returnCodes[code]}"
                } else {
                    if(filepath == "/web/Weapons.xml" || extension == "xsl" || extension == "css"){
                        body= new File("."+filepath).readLines().join("\n")
                    }
                    else if (!pages.contains(filepath)) {
                        code= 404
                        body= "${code} ${returnCodes[code]}"
                        extension= "html"
                    } else {
                        body= pageActions[filepath](fileSplit)
                    }
                    
                }
            } catch (Exception ex) {
                def sw= new StringWriter()
                def pw= new PrintWriter(sw)
                
                extension= "html"
                code= 500
                body= "<pre>"
                body+= "${code} ${returnCodes[code]}\n\n"
                ex.printStackTrace(pw)
                body+= sw.toString() + "</pre>"
                logger.log(Level.SEVERE, "Error generating webpage", ex);
            }
            
            def content= extensions[extension]
            header+= "${code} ${returnCodes[code]}\r\nConnection: close\r\nServer KFStats\r\n${content}\r\n\r\n"
*/
    }

    public abstract String fillBody(def xmlBuilder);
}
