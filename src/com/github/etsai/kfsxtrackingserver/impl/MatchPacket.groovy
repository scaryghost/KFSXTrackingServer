/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Packet

/**
 * Represents a match message
 * @author etsai
 */
public class MatchPacket extends Packet {
    public static String PROTOCOL= "kfstatsx-match"
    public static Integer VERSION= 1
    
    public enum Result {
        WIN, LOSS
    }
    
    private final def levelName
    private final def difficulty
    private final def length
    private final def elapsedTime
    private final def result
    private final def wave
    
    public MatchPacket(def parts) {
        super(parts[7])
        
        levelName= parts[1].toLowerCase()
        difficulty= parts[2]
        length= parts[3]
        elapsedTime= parts[4].toInteger()
        wave= parts[6].toInteger()
        
        switch (parts[5]) {
            case "1":
                result= Result.LOSS
                break
            case "2":
                result= Result.WIN
                break
            default:
                throw new RuntimeException("Unrecognized result value: ${parts[5]}")
        }
    }
    
    public String getLevelName() {
        return levelName
    }
    
    public String getDifficulty() {
        return difficulty
    }
    
    public String getLength() {
        return length
    }
    
    public int getElapsedTime() {
        return elapsedTime
    }
    
    public Result getResult() {
        return result
    }
    
    public int getWave() {
        return wave
    }

    @Override
    public String toString() {
        return [levelName, difficulty, length, result, wave, elapsedTime]
    }
}