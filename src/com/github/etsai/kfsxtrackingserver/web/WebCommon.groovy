package com.github.etsai.kfsxtrackingserver.web

import com.github.etsai.kfsxtrackingserver.Common
import com.github.etsai.utils.Time
import groovy.xml.MarkupBuilder

public class WebCommon {
    public static def jsFiles= ['http/js/jquery-1.8.2.js', 'http/js/kfstatsx2.js', 'https://www.google.com/jsapi?autoload={"modules":[{"name":"visualization","version":"1"}]}']
    public static def stylesheets= ['http://fonts.googleapis.com/css?family=Vollkorn', 'http/css/kfstatsx2.css']

    public static def getCategories() {
        def categories= []
        Common.sql.eachRow('SELECT category FROM aggregate GROUP BY category') {row ->
            categories << row.category
        }
        return categories
    }

    public static def generateSummary() {
        def games= 0, playTime= 0, playerCount
        Common.sql.eachRow('select * from difficulties') {row ->
                games+= row.wins + row.losses
                playTime+= row.time
        }
        Common.sql.eachRow('SELECT count(*) FROM records') {row ->
            playerCount= row[0]
        }
        return [["Games", games], ["Play Time", Time.secToStr(playTime)], ["Player Count", playerCount]].collect {
            [name: it[0], value: it[1]]
        }
    }
    
    public static void adjustStartEnd(def sql, def psValues, def start, def end) {
        Common.sql.eachRow(sql, psValues) {row ->
            if (row[0] == 0) {
                start= 0
                end= 0
            } else {
                if (start >= row[0]) {
                    start= [0, row[0] - (row[0] % rows)].max()
                    page= (row[0] / rows).toInteger()
                }
                if (end >= row[0]) end= row[0]
            }
        }
    }

    private static def generateJs(def name, def visualClass, def options, def steamid64) {
        def js
        def queries= ["table=$name"]

        switch(name) {
            case "profile":
                queries << "steamid64=$steamid64"
            case "totals":
                js= """
        google.setOnLoadCallback(drawVisualization);

        function drawVisualization() {
            var jsonData = \$.ajax({
                url: "data.html?${queries.join('&')}",
                dataType:"text",
                async: false
            }).responseText;
            document.getElementById('${name}_div').innerHTML= jsonData;
        }
    """
                break
            case "sessions":
                queries << "steamid64=$steamid64"
            case "records":
                js= """
        var page= 0, pageSize= 25, column, order, filled;
        var data, chart;

        google.setOnLoadCallback(drawVisualization);
        function drawVisualization() {
            data= new google.visualization.DataTable(\$.ajax({url: "data.json?${queries.join('&')}&tq=0,25", dataType:"json", async: false}).responseText);
            chart= new google.visualization.ChartWrapper({'chartType': 'Table', 'containerId': '${name}_div', 
                'options': {
                    'page': 'event',
                    'sort': 'event',
                    'pageSize': pageSize,
                    'pagingButtonsConfiguration': 'both',
                    'showRowNumber': true,
                    'allowHtml': true
                }
            });

            google.visualization.events.addListener(chart, 'ready', onReady);
            chart.setDataTable(data);
            chart.draw();

            function onReady() {
                google.visualization.events.addListener(chart.getChart(), 'page', function(properties) {
                    page+= parseInt(properties['page'], 10);
                    if (page < 0) {
                        page= 0;
                    }

                    data= new google.visualization.DataTable(\$.ajax({url: "data.json?${queries.join('&')}&tq=" + page + "," + pageSize, dataType:"json", async: false}).responseText);
                    if (data.getNumberOfRows() == 0 || (!filled && data.getNumberOfRows() != pageSize)) {
                        page--;
                    } else {
                        chart.setOption('firstRowNumber', pageSize * page + 1);
                        chart.setDataTable(data);
                        chart.draw();
                        filled= data.getNumberOfRows() == pageSize;
                    }
                });
                google.visualization.events.addListener(chart.getChart(), 'sort', function(properties) {
                    order= properties["ascending"] ? "asc" : "desc";
                    column= properties["column"];
                    data= new google.visualization.DataTable(\$.ajax({url: "data.json?${queries.join('&')}&tq=" + page + "," + pageSize + "," + column + "," + order, dataType:"json", async: false}).responseText);
                    filled= data.getNumberOfRows() == pageSize;
                    chart.setOption('sortColumn', column);
                    chart.setOption('sortAscending', properties["ascending"]);
                    chart.setDataTable(data);
                    chart.draw();
                });
            }
        }

        function updatePageSize(newSize) {
            pageSize= newSize;
            data= new google.visualization.DataTable(\$.ajax({url: "data.json?${queries.join('&')}&tq=" + page + "," + pageSize.toString(), dataType:"json", async: false}).responseText);
            chart.setDataTable(data);
            chart.setOption('pageSize', pageSize);
            chart.draw();
        }
    """
                break
            case "weapons":
            case "kills":
            case "deaths":
                if (steamid64 != null) {
                    queries << "steamid64=$steamid64"
                }
                js= """
        google.setOnLoadCallback(drawVisualization);

        function drawVisualization() {
            var data= new google.visualization.DataTable(\$.ajax({url: "data.json?${queries.join('&')}", dataType:"json", async: false}).responseText);
            var chart= new google.visualization.ChartWrapper({'chartType': 'BarChart', 'containerId': '${name}_div', 'options': {
                'legend': {position: 'none'},
                'chartArea': {height: '80%'},
                'vAxis': {textStyle: {fontSize: 15}}
            }});
            chart.setDataTable(data);
            var numRows = chart.getDataTable().getNumberOfRows();
            var expectedHeight = numRows * 25;
            // Update the chart options and redraw just it
            chart.setOption('title', '$name');
            chart.setOption('height', expectedHeight);
            chart.setOption('width', document.getElementById('${name}_div').offsetWidth * 0.985);
            chart.draw();
        }
    """
                break
            default:
                if (steamid64 != null) {
                    queries << "steamid64=$steamid64"
                }
                js= """
        google.setOnLoadCallback(drawVisualization);

        function drawVisualization() {
            var data= new google.visualization.DataTable(\$.ajax({url: "data.json?${queries.join('&')}", dataType:"json", async: false}).responseText);
            var chart= new google.visualization.ChartWrapper({'chartType': '$visualClass', 'containerId': '${name}_div', 'options': $options});
            chart.setDataTable(data);
            chart.setOption('height', document.getElementById('${name}_div').offsetHeight * 0.975);
            chart.setOption('width', document.getElementById('${name}_div').offsetWidth * 0.985);
            chart.draw();
        }
    """
                break
        }
        return js
    }

    public static def generateHtml(def nav, def steamid64) {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)

        htmlBuilder.html() {
            head() {
                meta('http-equiv':'content-type', content:'text/html; charset=utf-8')
                title("KFStatsX")
    
                jsFiles.each {filename ->
                    script(type:'text/javascript', src:filename, '')
                }
                nav.each {name ->
                    def js
                    
                    if (name == "perks") {
                        js= WebCommon.generateJs(name, 'PieChart', "{is3D: true}", steamid64)
                    } else {
                        js= WebCommon.generateJs(name, 'Table', '{allowHtml: true}', steamid64)
                    }
                    script(type: 'text/javascript') {
                        mkp.yieldUnescaped(js)
                    }
                }

                stylesheets.each {filename ->
                    link(href: filename, rel:'stylesheet', type:'text/css')
                }
                link(rel:'icon', type:'image/vnd.microsoft.icon', href: 'http/ico/favicon.ico')
            }
            body() {
                div(id:'wrap') {
                    div(id: 'nav') {
                        h3("Navigation") {
                            select(onchange:'goto(this.options[this.selectedIndex].value, this); return false') {
                                nav.each {item ->
                                    def attr= [value: "#${item}_div"]
                                    if (item == nav.first()) {
                                        attr["selected"]= "selected"
                                    } else if (item == nav.last()) {
                                        attr["value"]+= "_outer"
                                    }
                                    option(attr, item)
                                }
                            }
                        }
                    }
                    div(id:'content') {
                        div(class:'contentbox-wrapper') {
                            nav.each {item ->
                                if (item == nav.last()) {
                                    div(id:item + '_div_outer', class:'contentbox') {
                                        form(action:'', 'Number of rows:') {
                                            select(onchange:'updatePageSize(parseInt(this.value, 10))') {
                                                option(selected:"selected", value:'25', '25')
                                                option(value:'50', '50')
                                                option(value:'100', '100')
                                                option(value:'250', '250')
                                            }
                                        }
                                        if (steamid64 == null) {
                                            form(action:'profile.html', method:'get', style:'text-align:left') {
                                                mkp.yieldUnescaped("Enter player's <a href='http://steamidconverter.com/' target='_blank'>steamID64: </a>")
                                                input(type:'text', name:'steamid64')
                                                input(type:'submit', value:'Search Player')
                                            }
                                        }
                                        div(id: item + '_div', '')
                                    }
                                } else {
                                    div(id: item + '_div', class:'contentbox', '')
                                }
                            }
                        }
                    }
                }
            }
        }
        return writer.toString()
    }
}
