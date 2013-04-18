/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.*
import groovy.xml.MarkupBuilder

/**
 * Generates the html data for the page data.html
 * @author etsai
 */
public class DataHtml implements Resource {
    private static def colStyle= "text-align:center"
    private static def tableAttr= [class: "content-table"]
    
    public String generatePage(DataReader reader, Map<String, String> queries) {
        def writer= new StringWriter()
        def xml= new MarkupBuilder(writer)
        def queryValues= Queries.parseQuery(queries)
        
        switch(queryValues[Queries.table]) {
            case "totals":
                xml.center() {
                    table(tableAttr) {
                        tbody() {
                            WebCommon.generateSummary(reader).each {attr ->
                                tr() {
                                    td(attr['name'])
                                    td(attr['value'])
                                }
                            }
                        }
                    }
                }
                break
            case "profile":
                def steamid64= queryValues[Queries.steamid64]
                def row= reader.getRecord(steamid64);

                if (row == null) {
                    xml.center("No records found for SteamID64: ") {
                        a(href: "http://steamcommunity.com/profiles/" + steamid64, steamid64)
                    }
                } else {
                    def steamIdInfo= reader.getSteamIDInfo(steamid64)

                    xml.center() {
                        table(tableAttr) {
                            tbody() {
                                tr() {
                                    td("Name")
                                    td(steamIdInfo.name)
                                }
                                tr() {
                                    td("Wins")
                                    td(row.wins)
                                    td(rowspan: "4") {
                                        img(src: steamIdInfo.avatar)
                                    }
                                }
                                tr() {
                                    td("Losses")
                                    td(row.losses)
                                }
                                tr() {
                                    td("Disconnects")
                                    td(row.disconnects)
                                }
                                tr() {
                                    td("Steam Community")
                                    td() {
                                        a(target: "_blank", href: "http://steamcommunity.com/profiles/" + steamid64, steamid64)
                                    }
                                }
                            }
                        }
                    }
                }
                break
        }
        return writer
    }
}

