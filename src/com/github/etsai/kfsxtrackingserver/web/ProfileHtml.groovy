package com.github.etsai.kfsxtrackingserver.web

import static com.github.etsai.kfsxtrackingserver.Common.sql
import groovy.xml.MarkupBuilder

public class ProfileHtml {
    public static def jsFiles= ['https://www.google.com/jsapi', 'http/js/jquery-1.8.2.js', 'http/js/tablequerywrapper.js', 'http/js/kfstatsx2.js']
    public static def stylesheets= ['http://fonts.googleapis.com/css?family=Vollkorn', 'http/css/kfstatsx2.css']

    private static def generateJs(def type, def name, def visualClass, def options, def steamid64) {
        def js

        switch(name) {
            case "profile":
                js= """
        google.load('visualization', '1', {packages:['$type']});
        google.setOnLoadCallback(drawTable);

        function drawTable() {
            var jsonData = \$.ajax({
                url: "data.html?table=$name&steamid64=$steamid64",
                dataType:"text",
                async: false
            }).responseText;
            document.getElementById('${name}_div').innerHTML= jsonData;
        }
    """
                break
            case "sessions":
                js= """
        google.load('visualization', '1', {'packages' : ['table']});
        google.setOnLoadCallback(init);

        var dataSourceUrl = 'sessionsjson.html?steamid64=$steamid64';
        var query, options, container;

        function init() {
            query = new google.visualization.Query(dataSourceUrl);
            container = document.getElementById("${name}_div");
            options = {'pageSize': 25};
            sendAndDraw();
        }

        function sendAndDraw() {
            query.abort();
            var tableQueryWrapper = new TableQueryWrapper(query, container, options);
            tableQueryWrapper.sendAndDraw();
        }

        function setOption(prop, value) {
            options[prop] = value;
            sendAndDraw();
        }
    """
                break
            default:
                js= """
        google.load('visualization', '1', {packages:['$type']});
        google.setOnLoadCallback(drawTable);

        function drawTable() {
            var jsonData = \$.ajax({
                url: "data.json?table=$name&steamid64=$steamid64",
                dataType:"json",
                async: false
            }).responseText;
            var data= new google.visualization.DataTable(jsonData);
            var table= new google.visualization.$visualClass(document.getElementById('${name}_div'));
            table.draw(data, $options);
        }
    """
                break
        }
        return js
    }

    public static String fillBody(def queries) {
        def steamid64= queries["steamid64"]
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)
        def generateNav= {
            def base= ["profile"]
            sql.eachRow('SELECT category FROM aggregate GROUP BY category') {row ->
                base << row.category
            }
            base << "sessions"
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
                    if (name == "perks") {
                        script(type:'text/javascript') {
                            mkp.yieldUnescaped(generateJs('corechart', name, 'PieChart', "{title: '$name', is3D: true}", steamid64))
                        }
                    } else if (name == "weapons" || name == "kills" || name == "deaths") {
                        script(type:'text/javascript') {
                            mkp.yieldUnescaped(generateJs('corechart', name, 'BarChart', "{vAxis: {title: '$name', titleTextStyle: {color: 'red'}, textStyle: {fontSize: 12}}, chartArea: {top: 0, height: '95%'}}", steamid64))
                        }
                    } else {
                        script(type:'text/javascript') {
                            mkp.yieldUnescaped(generateJs('table', name, 'Table', '{allowHtml: true}', steamid64))
                        }
                    }
                }

                stylesheets.each {filename ->
                    link(href: filename, rel:'stylesheet', type:'text/css')
                }
            }
            body() {
                div(id:'wrap') {
                    div(id: 'nav') {
                        h1("Navigation")
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
                    div(id:'content') {
                        div(class:'contentbox-wrapper') {
                            nav.each {item ->
                                if (item == nav.last()) {
                                    div(id:item + '_div_outer', class:'contentbox') {
                                        form(action:'', 'Number of rows to show:') {
                                            select(onchange:'setOption("pageSize", parseInt(this.value, 10))') {
                                                option(selected:"selected", value:'25', '25')
                                                option(value:'50', '50')
                                                option(value:'100', '100')
                                                option(value:'250', '250')
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
        return writer
    }
}
