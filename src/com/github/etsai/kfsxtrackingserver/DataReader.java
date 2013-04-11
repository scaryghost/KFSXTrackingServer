/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import java.util.Map;

/**
 *
 * @author eric
 */
public interface DataReader {
    public enum Order {
        ASC,
        DESC,
        NONE
    }
    public Iterable<Map<Object, Object>> getDifficulties();
    public Iterable<Map<Object, Object>> getLevels();
    public Iterable<Map<Object, Object>> getDeaths();
    public Iterable<Map<Object, Object>> getRecords(String group, Order order, int start, int end);
    public Iterable<Map<Object, Object>> getRecords();
    public Iterable<Map<Object, Object>> getSessions(String steamID64, String group, Order order, int start, int end);
    public Iterable<Map<Object, Object>> getSessions(String steamID64);
    public Iterable<String> getAggregateCategories();
    public Iterable<Map<Object, Object>> getAggregateData(String category);
    public Iterable<Map<Object, Object>> getAggregateData(String category, String steamID64);
    public Map<Object, Object> getSteamIDInfo(String steamID64);
}
