/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.*
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder

/**
 *
 * @author etsai
 */
public class ProfileXml implements Resource {
    public static final KEY_STEAMID64= "steamid64"
    
    public String generatePage(DataReader reader, Map<String, String> queries) {
        def queryValues= Queries.parseQuery(queries)
        def steamid64= queryValues[Queries.steamid64]
        def profileAttr= reader.getRecord(steamid64)
        
        def writer= new StringWriter()
        def xmlBuilder= new MarkupBuilder(writer)
        xmlBuilder.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/profile.xsl"])
        xmlBuilder.kfstatsx() {
            if (profileAttr == null) {
                'error'("No stats available for steamdID64: ${steamid64}")
            } else {
                profileAttr.putAll(reader.getSteamIDInfo(steamid64))
                'profile'(profileAttr) {
                    reader.getAggregateCategories().each {category ->
                        'stats'(category: category) {
                            reader.getAggregateData(category, steamid64).each {row ->
                                def attr= [:]
                                attr["name"]= row.stat
                                attr["value"]= row.value

                                if (category == "perks") {
                                    attr["hint"]= Time.secToStr(attr["value"])
                                } else if (attr["name"].toLowerCase().contains("time")) {
                                    attr["value"]= Time.secToStr(attr["value"])
                                }                
                                xmlBuilder.'entry'(attr)
                            }
                        }
                    }
                }
            }
        }
        
        return writer
    }
}

