/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.web;

import com.github.etsai.kfsxtrackingserver.DataReader;
import java.util.Map;

/**
 *
 * @author eric
 */
public interface Resource {
    public String generatePage(DataReader reader, Map<String, String> queries);
}
