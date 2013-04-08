/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.web.Resource
import groovy.sql.Sql
import groovy.xml.MarkupBuilder

/**
 * Generates the records.xml page
 * @author etsai
 */
public class RecordsXml implements Resource {
    public String generatePage(Sql sql, Map<String, String> queries) {
        def writer= new StringWriter()
        def xmlBuilder= new MarkupBuilder(writer)
        def queryValues= Queries.parseQuery(queries)

        xmlBuilder.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/records.xsl"])
        xmlBuilder.kfstatsx() {
            def attrs= [page: queryValues[Queries.page], rows: queryValues[Queries.rows], query: "&group=${queryValues[Queries.group]}&order=${queryValues[Queries.order]}"]
            def pos= queryValues[Queries.page].toInteger() * queryValues[Queries.rows].toInteger()

            ["name", "wins", "losses", "disconnects"].each {col ->
                if (col == queryValues[Queries.group] && queryValues[Queries.order] == "desc") {
                    attrs[col]= "asc"
                } else {
                    attrs[col]= "desc"
                }
            }
            
            xmlBuilder.'records'(attrs) {
                WebCommon.partialQuery(sql, queryValues, 
                        "SELECT r.steamid64,r.wins,r.losses,r.disconnects,s.name FROM records r INNER JOIN steaminfo s ON r.steamid64=s.steamid64 ", [], {row ->
                    def result= row.toRowResult()
                    result["pos"]= pos + 1

                    xmlBuilder.'record'(result)
                    pos++
                })
            }
        }
        return writer
    }
}

