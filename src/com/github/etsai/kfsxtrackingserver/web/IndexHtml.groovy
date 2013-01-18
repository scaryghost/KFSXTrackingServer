package com.github.etsai.kfsxtrackingserver.web

import groovy.xml.MarkupBuilder

public class IndexHtml {
    public static def nav= ["Summary", "Levels", "Difficulty", "Deaths", "Kills", "Perks", "Weapons", "Records"]
    public static def jsFiles= ['https://www.google.com/jsapi', 'http/js/jquery-1.8.2.js', 'http/js/tablequerywrapper.js', 'http/js/kfstatsx2.js']
    public static def stylesheets= ['http://fonts.googleapis.com/css?family=Vollkorn', 'http/css/kfstatsx2.css']
    public static def recordsJs= """
        google.load('visualization', '1', {'packages' : ['table']});
        google.setOnLoadCallback(init);

        var dataSourceUrl = 'recordsjson.html';
        var query, options, container;

        function init() {
            query = new google.visualization.Query(dataSourceUrl);
            container = document.getElementById("Records_div");
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

    public static String fillBody() {
        def writer= new StringWriter()
        def htmlBuilder= new MarkupBuilder(writer)

        htmlBuilder.html() {
            head() {
                meta('http-equiv':'content-type', content:'text/html; charset=utf-8')
                title("KFStatsX")
    
                jsFiles.each {filename ->
                    script(type:'text/javascript', src:filename, '')
                }
                script(type:'text/javascript', recordsJs)

                stylesheets.each {filename ->
                    link(href: filename, rel:'stylesheet', type:'text/css')
                }
            }
            body() {
                div(id:'wrap') {
                    div(id: 'nav') {
                        ul() {
                            nav.each {item ->
                                li() {
                                    a(href:"#", onclick:'goto("#'+item+'_div", this); return false', item)
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