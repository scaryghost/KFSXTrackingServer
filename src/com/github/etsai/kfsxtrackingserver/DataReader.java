/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import java.util.List;
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
    public List<Map<Object, Object>> getDifficulties();
    public List<Map<Object, Object>> getLevels();
    public List<Map<Object, Object>> getDeaths();
    public Integer getNumRecords();
    public Map<Object, Object> getRecord(String steamID64);
    public List<Map<Object, Object>> getRecords(String group, Order order, int start, int end);
    public List<Map<Object, Object>> getRecords();
    public List<Map<Object, Object>> getSessions(String steamID64, String group, Order order, int start, int end);
    public List<Map<Object, Object>> getSessions(String steamID64);
    public List<String> getAggregateCategories();
    public List<Map<Object, Object>> getAggregateData(String category);
    public List<Map<Object, Object>> getAggregateData(String category, String steamID64);
    public Map<Object, Object> getSteamIDInfo(String steamID64);
}
