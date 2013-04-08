/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.web.*
import com.github.etsai.utils.Time
import groovy.sql.Sql
import groovy.xml.MarkupBuilder

/**
 *
 * @author etsai
 */
public class ProfileXml implements Resource {
    public static final KEY_STEAMID64= "steamid64"
    
    public String generatePage(Sql sql, Map<String, String> queries) {
        def steamid64= queries[KEY_STEAMID64]
        def profileAttr= null
        def steamIdInfo= SteamIDInfo.getSteamIDInfo(steamid64)
        
        sql.eachRow("SELECT * FROM records where steamid64=?", [steamid64]) {row ->
            profileAttr= [:]
            profileAttr["steamid64"]= steamid64
            profileAttr["name"]= steamIdInfo.name
            profileAttr["avatar"]= steamIdInfo.avatar
            profileAttr["wins"]= row.wins
            profileAttr["losses"]= row.losses
            profileAttr["disconnects"]= row.disconnects
        }
        
        def writer= new StringWriter()
        def xmlBuilder= new MarkupBuilder(writer)
        xmlBuilder.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/profile.xsl"])
        xmlBuilder.kfstatsx() {
            if (profileAttr == null) {
                'error'("No stats available for steamdID64: ${steamid64}")
            } else {
                'profile'(profileAttr) {
                    sql.eachRow("SELECT category FROM player where steamid64=? group by category", [steamid64]) {row1 ->
                        'stats'(category: row1.category) {
                            sql.eachRow("SELECT * FROM player where steamid64=? AND category=?", [steamid64, row1[0]]) {row ->
                                def attr= [:]
                                attr["name"]= row.stat
                                attr["value"]= row.value

                                if (row1.category == "perks") {
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

