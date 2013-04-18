import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder

public class IndexXml implements Resource {
    public String generatePage(DataReader reader, Map<String, String> queries) {
        def writer= new StringWriter()
        def xmlBuilder= new MarkupBuilder(writer)

        xmlBuilder.mkp.pi("xml-stylesheet":[type:"text/xsl",href:"http/xsl/index.xsl"])
        xmlBuilder.kfstatsx() {
            'stats'(category:"totals") {
                WebCommon.generateSummary(reader).each {attr ->
                    'entry'(attr)
                }
            }
            
            'stats'(category:"difficulties") {
                def wins= 0, losses= 0, time= 0
                reader.getDifficulties().each {row ->
                    def attr= [:]
                    wins+= row.wins
                    losses+= row.losses
                    time+= row.time
                    
                    attr["name"]= row.name
                    attr["length"]= row.length
                    attr["wins"]= row.wins
                    attr["losses"]= row.losses
                    attr["wave"]= String.format("%.2f",row.wave / (row.wins + row.losses))
                    attr["rawtime"]= row.time
                    'entry'(attr)
                }
                'total'(name: "Total", length: "", wins: wins, losses:losses, 
                    wave: "", time:Time.secToStr(time))
            }
            'stats'(category:"levels") {
                def wins= 0, losses= 0, time= 0
                reader.getLevels().each {row ->
                    def attr= [:]
                    
                    wins+= row.wins
                    losses+= row.losses
                    //Accidentally didn't set the column type for time in levels table
                    time+= row.time.toInteger()
                    attr["name"]= row.name
                    attr["wins"]= row.wins
                    attr["losses"]= row.losses
                    attr["time"]= Time.secToStr(row.time.toInteger())
                    attr["rawtime"]= row.time
                    'entry'(attr)
                }
                'total'(name: "Total", wins: wins, losses:losses, time:Time.secToStr(time))
            }
            'stats'(category:"deaths") {
                reader.getDeaths().each {row ->
                    def attr= [:]
                    attr["name"]= row.name
                    attr["value"]= row.count
                    'entry'(attr)
                }
            }
            reader.getAggregateCategories().each {category ->
                xmlBuilder.'stats'(category: category) {
                    reader.getAggregateData(category).each {row2 ->
                        def key, val
                        def attrs= [:]
                        attrs["name"]= row2.stat
                        attrs["value"]= row2.value

                        if (category == "perks") {
                            attrs["hint"]= Time.secToStr(attrs["value"])
                        } else if (attrs["name"].toLowerCase().contains("time")) {
                            attrs["value"]= Time.secToStr(attrs["value"])
                        }
                        'entry'(attrs)
                    }
                }
            }
        }
        return writer
    }
}
