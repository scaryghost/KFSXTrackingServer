/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.web

import java.util.Map;

/**
 * Valid get query keys for the web server
 * @author etsai
 */
public enum Queries {
    page,
    rows,
    steamid64,
    order,
    group

    public static defaults= [(page): 0, (rows): 25, (order): "asc", (group): "none"]
    public static def parseQuery(def queries) {
        def values= [:]
        Queries.values().each {key ->
            def keyStr= key.toString()
            values[key]= queries[keyStr] == null ? defaults[key] : queries[keyStr]
        }
    }
};
