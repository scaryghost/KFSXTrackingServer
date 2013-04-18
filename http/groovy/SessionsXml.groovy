/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.xml.MarkupBuilder

/**
 * Generates the xml for a player's session history
 * @author etsai
 */
public class SessionsXml implements Resource {
    public String generatePage(DataReader reader, Map<String, String> queries) {
        def queryValues= Queries.parseQuery(queries)

        def writer= new StringWriter()
        def xmlBuilder= new MarkupBuilder(writer)
        xmlBuilder.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/sessions.xsl"])
        xmlBuilder.kfstatsx() {
            def attrs= [category: "sessions", steamid64: queryValues[Queries.steamid64], page: queryValues[Queries.page], rows: queryValues[Queries.rows], 
                query: "&group=${queryValues[Queries.group]}&order=${queryValues[Queries.order]}"]

            ["level", "difficulty", "length", "result", "wave", "timestamp"].each {col ->
                if (col == queryValues[Queries.group] && queryValues[Queries.order] == "desc") {
                    attrs[col]= "asc"
                } else {
                    attrs[col]= "desc"
                }
            }
            'stats'(attrs) {
                WebCommon.partialQuery(reader, queryValues, false).each {row ->
                    row.remove("steamid64")
                    'entry'(row)
                }
            }
        }

        return writer
    }
}

