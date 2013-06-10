/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.web;

import com.github.etsai.kfsxtrackingserver.DataReader;
import java.util.Map;

/**
 * Abstract class that generates the data for the resource requested in the HTTP header
 * @author etsai
 */
public abstract class Resource {
    /** 
     * The queries given in the request, split on the '=' character.  For example,
     * the query a=b becomes an entry in the map, with key 'a' and value 'b'.
     */
    protected Map<String, String> queries;
    /** Variable that provides read access to the statistics */
    protected DataReader reader;

    /**
     * Set the reader variable
     * @param   reader      The reader object to use
     */
    public void setDataReader(DataReader reader) {
        this.reader= reader;
    }
    /**
     * Set the queries variable
     * @param   queries     The parsed query field from the HTTP request as a map
     */
    public void setQueries(Map<String, String> queries) {
        this.queries= queries;
    }
    /**
     * Generate the web page for the requested resource
     * @return  The web page
     */
    public abstract String generatePage();
}
