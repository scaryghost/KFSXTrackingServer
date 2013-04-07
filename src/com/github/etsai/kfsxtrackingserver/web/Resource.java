/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.web;

import java.util.Map;

/**
 *
 * @author eric
 */
public interface Resource {
    String generatePage(Map<Queries, String> queries);
}
