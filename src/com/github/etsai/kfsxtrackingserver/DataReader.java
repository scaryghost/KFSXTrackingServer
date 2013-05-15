/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import java.util.List;
import java.util.Map;

/**
 *
 * @author etsai
 */
public interface DataReader {
    public enum Order {
        ASC,
        DESC,
        NONE
    }
    public List<Map<Object, Object>> getDifficulties();
    public List<Map<Object, Object>> getDifficultyData(String diffName, String length);
    public List<Map<Object, Object>> getLevels();
    public List<Map<Object, Object>> getLevelData(String levelName);
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
    public List<Map<Object, Object>> getWaveData(String diffName, String diffLength, String category);
    public List<Map<Object, Object>> getWaveData(String levelName, String diffName, String diffLength, String category);
    public List<String> getWaveDataCategories();
}
