/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.web;

import groovy.sql.Sql;
import java.util.Map;

/**
 *
 * @author eric
 */
public interface Resource {
    public String generatePage(Sql sql, Map<String, String> queries);
}
