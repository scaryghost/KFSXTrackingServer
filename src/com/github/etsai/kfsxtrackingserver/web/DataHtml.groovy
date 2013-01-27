/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common


/**
 * Generates the html data for the page data.html
 * @author etsai
 */
public class DataHtml {
    private static def colStyle= "text-align:center"
    
    public static String fillBody(def queries) {
        def data= ""
        
        switch(queries["table"]) {
            case "totals":
                data= "<center><table width='630' cellspacing='6' cellpadding='0'><tbody>"
                WebCommon.generateSummary().each {attr ->
                    data+= "<tr><td>${attr['name']}</td><td>${attr['value']}</td></tr>"
                    }
                data+= "</tbody></table></center>"
            break
        
        }
        return data
    }
}

