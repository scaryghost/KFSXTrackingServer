package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.sql
import groovy.xml.MarkupBuilder

public class IndexHtml {
    public static def jsFiles= ['http/js/jquery-1.8.2.js', 'http/js/kfstatsx2.js', 'https://www.google.com/jsapi?autoload={"modules":[{"name":"visualization","version":"1"}]}']
    public static def stylesheets= ['http://fonts.googleapis.com/css?family=Vollkorn', 'http/css/kfstatsx2.css']

    private static def generateJs(def name, def visualClass, def options) {
        def js

        switch(name) {
            case "totals":
                js= """
        google.setOnLoadCallback(drawVisualization);

        function drawVisualization() {
            var jsonData = \$.ajax({
                url: "data.html?table=$name",
                dataType:"text",
                async: false
            }).responseText;
            document.getElementById('${name}_div').innerHTML= jsonData;
        }
    """
                break
            case "records":
                js= """
        var page= 0, pageSize= 25, column, order, filled;
        var data, chart;

        google.setOnLoadCallback(drawVisualization);
        function drawVisualization() {
            data= new google.visualization.DataTable(\$.ajax({url: "records.json?tq=0,25", dataType:"json", async: false}).responseText);
            chart= new google.visualization.ChartWrapper({'chartType': 'Table', 'containerId': 'records_div', 
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

                    data= new google.visualization.DataTable(\$.ajax({url: "records.json?tq=" + page + "," + pageSize, dataType:"json", async: false}).responseText);
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
                    data= new google.visualization.DataTable(\$.ajax({url: "records.json?tq=" + page + "," + pageSize + "," + column + "," + order, dataType:"json", async: false}).responseText);
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
            data= new google.visualization.DataTable(\$.ajax({url: "records.json?tq=" + page + "," + pageSize.toString(), dataType:"json", async: false}).responseText);
            chart.setDataTable(data);
            chart.setOption('pageSize', pageSize);
            chart.draw();
        }
    """
                break
            default:
                js= """
        google.setOnLoadCallback(drawVisualization);

        function drawVisualization() {
            var data= new google.visualization.DataTable(\$.ajax({url: "data.json?table=$name", dataType:"json", async: false}).responseText);
            var chart= new google.visualization.ChartWrapper({'chartType': '$visualClass', 'containerId': '${name}_div', 'options': $options});
            chart.setDataTable(data);
            chart.draw();
        }
    """
                break
        }
        return js
    }

    public static String fillBody() {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)
        def generateNav= {
            def base= ["totals", "difficulties", "levels", "deaths"]
            sql.eachRow('SELECT category FROM aggregate GROUP BY category') {row ->
                base << row.category
            }
            base << "records"
            return base
        }
        def nav= generateNav()

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
                        js= generateJs(name, 'PieChart', "{title: '$name', is3D: true}")
                    } else if (name == "weapons" || name == "kills" || name == "deaths") {
                        js= generateJs(name, 'BarChart', "{vAxis: {title: '$name', titleTextStyle: {color: 'red'}, textStyle: {fontSize: 12}}, chartArea: {height: '90%'}}")
                    } else {
                        js= generateJs(name, 'Table', '{allowHtml: true}')
                    }
                    script(type:'text/javascript') {
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
                                    if (item == nav.last()) {
                                        option(value:"#" + item + "_div_outer", item)
                                    } else {
                                        option(value:"#" + item + "_div", item)
                                    }
                                }
                            }
                        }
                    }
                    div(id:'content') {
                        div(class:'contentbox-wrapper') {
                            nav.each {item ->
                                if (item == nav.last()) {
                                    div(id:item + '_div_outer', class:'contentbox') {
                                        form(action:'', 'Number of rows to show:') {
                                            select(onchange:'updatePageSize(parseInt(this.value, 10))') {
                                                option(selected:"selected", value:'25', '25')
                                                option(value:'50', '50')
                                                option(value:'100', '100')
                                                option(value:'250', '250')
                                            }
                                        }
                                        form(action:'profile.xml', method:'get', style:'text-align:left') {
                                            input(type:'text', name:'steamid64')
                                            input(type:'submit', value:'Search Player')
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
        return writer
    }
}
