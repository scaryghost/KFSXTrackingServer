/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

import java.util.List;
import java.util.Map;

/**
 * Interfaces with the statistical data, providing the user with read access to the underlying storage scheme
 * @author etsai
 */
public interface DataReader {
    /**
     */
    public enum Order {
        /** Sort in ascending order */
        ASC,
        /** Sort in descending order */
        DESC,
        /** Do not sort */
        NONE
    }
    /**
     * Get the difficulty information for each difficulty setting.  The keys for the map are: name, length, wins, losses, waveaccum, and time.
     * @return  List of a map of attributes for each difficulty
     */
    public List<Map<Object, Object>> getDifficulties();
    /**
     * Get the level breakdown for the given difficulty setting.  The keys for the map are: name, wins, losses, waveaccum, and time
     * @param   diffName    Difficulty name
     * @param   length      Game length
     * @return  List of statistics for each map played on the given difficulty setting
     */
    public List<Map<Object, Object>> getDifficultyData(String diffName, String length);
    /**
     * Get the totals for each played level, across all difficulty settings.  The keys for the map are: name, wins, losses, and time
     * @return  List of totals for all levels
     */
    public List<Map<Object, Object>> getLevels();
    /**
     * Get the difficulty breakdown for specific level.  The keys for the map are: name, length, wins, losses, waveaccum, and time
     * @param   levelName   Name of the level to lookup
     * @return  List of difficulty breakdowns for all levels played
     */
    public List<Map<Object, Object>> getLevelData(String levelName);
    /**
     * Get the number of player records in the database
     * @return  Number of players in the database
     */
    public Integer getNumRecords();
    /**
     * Get the record for the given player.  The keys for the map are: name, avatar, wins, losses, disconnects, finale_played, finale_survived, time_connected.
     * If the steamID64 is invalid, null is returned.
     * @param   steamID64   SteamID64 of the desired player.
     * @return  Map of attributes for the given player, null if invalid steamID64 is given
     */
    public Map<Object, Object> getRecord(String steamID64);
    /**
     * Get a subset of all the player records, sorted by a specific category in a given order.  The keys for the map are: name, avatar, wins, losses, disconnects, 
     * finale_played, finale_survived, time_connected.
     * @param   group   The group to sort on.  If order is NONE, this parameter is ignored
     * @param   order   Order to sort the group if desired
     * @param   start   The first row to return in the given ordering
     * @param   end     The last row to return in the given ordering
     * @return  Ordered list of player records, limitted by a start and end.
     */
    public List<Map<Object, Object>> getRecords(String group, Order order, int start, int end);
    /**
     * Get all stored player records.  The keys for the map are: name, avatar, wins, losses, disconnects, finale_played, finale_survived, time_connected.
     * @return  All player records
     */
    public List<Map<Object, Object>> getRecords();
    /**
     * Get a subset of the match history for the specific player.  The list can be ordered based on a grouping.  The keys for the map are:
     * result, wave, duration, timestamp, difficulty, length, and level.  
     * @param   steamID64   SteamID64 of the player to lookup
     * @param   group   The group to sort on.  If order is NONE, this parameter is ignored
     * @param   order   Order to sort the group if desired
     * @param   start   The first row to return in the given ordering
     * @param   end     The last row to return in the given ordering
     * @return  Ordered list of the match history for a player.
     */
    public List<Map<Object, Object>> getSessions(String steamID64, String group, Order order, int start, int end);
    /**
     * Get all games in a player's match history.  The keys for the map are:result, wave, duration, timestamp, difficulty, length, and level.
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  All games played by the player
     */
    public List<Map<Object, Object>> getSessions(String steamID64);
    /**
     * Get all stat categories that have an aggregate sum over all players
     * @return  Stat categories for aggregate stats
     */
    public List<String> getAggregateCategories();
    /**
     * Get the aggregate statistics for a specific category.  Map keys are: stat and value
     * @param   category    Aggregate category to lookup
     * @return  List of all statistics for a category
     */
    public List<Map<Object, Object>> getAggregateData(String category);
    /**
     * Get the aggregate statistics for a specific category and player.  Map keys are: stat and value
     * @param   category    Aggregate category to lookup
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  List of all statistics for a category and player
     */
    public List<Map<Object, Object>> getAggregateData(String category, String steamID64);
    /**
     * Get the steam community info for a player.  Map keys are: name, avatar
     * @param   steamID64   SteamID64 of the player to lookup
     * @return  Steam community info for a player
     */
    public Map<Object, Object> getSteamIDInfo(String steamID64);
    /**
     * Get categories for statistics that support wave by wave analytics
     * @return  List of support statistics that have wave by wave numbers
     */
    public List<String> getWaveDataCategories();
    /**
     * Get detailed wave by wave numbers for a given difficulty setting.  Map keys are: wave, stat, and value
     * @param   diffName    Difficulty name
     * @param   length      Game length
     * @param   category    Category of statistics to retrieve
     * @return  List of wave by wave statistics
     */
    public List<Map<Object, Object>> getWaveData(String diffName, String diffLength, String category);
    /**
     * Get detailed wave by wave numbers for a given difficulty setting and map.  Map keys are: wave, stat, and value
     * @param   levelName   Name of the level
     * @param   diffName    Difficulty name
     * @param   length      Game length
     * @param   category    Category of statistics to retrieve
     * @return  List of wave by wave statistics
     */
    public List<Map<Object, Object>> getWaveData(String levelName, String diffName, String diffLength, String category);
}
