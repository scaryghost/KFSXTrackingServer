import com.github.etsai.kfsxtrackingserver.DataReader
import com.github.etsai.kfsxtrackingserver.web.Resource

public class IndexHtml implements Resource {
    public String generatePage(DataReader reader, Map<String, String> queries) {
        def nav= ["totals", "difficulties", "levels", "deaths"].plus(reader.getAggregateCategories().collect{it.category}) << "records"
        return WebCommon.generateHtml(nav, null)
        
    }
}
