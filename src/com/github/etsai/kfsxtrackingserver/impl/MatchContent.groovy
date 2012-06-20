/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.Content
import com.github.etsai.kfsxtrackingserver.Packet
import com.github.etsai.kfsxtrackingserver.Time

/**
 *
 * @author eric
 */
public class MatchContent implements Content {
    public class Level {
        public static final def keyName= "name"
        public static final def keyWins= "wins"
        public static final def keyLosses= "losses"
        public static final def keyTime= "time"
        
        protected def data
        
        public Level(def name) {
            data= [:]
            data[keyName]= name
            data[keyWins]= 0
            data[keyLosses]= 0
            data[keyTime]= new Time(0)
        }
        
        public void addWin() {
            data[keyWins]++
        }
        
        public void addLosses() {
            data[keyLosses]++
        }
        
        public void addTime(def time) {
            data[keyTime].add(time)
        }
        public Iterable<String> getKeys() {
            return data.keySet()
        }
        public def getData(String key) {
            return data[key]
        }
    }
    public class Difficulty extends Level {
        public static final def keyLength= "length"
        public static final def keyWave= "wave"
        
        public Difficulty(def name, def length, def wave) {
            super(name)
            data[keyLength]= length
            data[keyWave]= wave
        }
        
        public void addWave(def wave) {
            data[keyWave]+= wave
        }
    }

    private def difficulties
    private def levels
    private def deaths
    
    public MatchContent() {
        difficulties= [:]
        levels= [:]
        deaths= [:]
    }
    public boolean load() {
        throw new UnsupportedOperationException("Not yet implemented!")
    }
    public boolean save() {
        throw new UnsupportedOperationException("Not yet implemented!")
    }
    public void accumulate(Packet packet) {
        def levelName= packet.getData(MatchPacket.keyMap)
        def difficultyName= packet.getData(MatchPacket.Difficulty)
        def length= packet.getData(MatchPacket.Length)
        def wave= packet.getData(MatchPacket.Wave).toInteger()
        def time= packet.getData(MatchPacket.keyTime).toInteger()
        
        if (levels[levelName] == null) {
            levels[levelName]= new Level(levelName)
        }
        if (difficulties[[difficultyName, length]] == null) {
            difficulties[[difficultyName, length]]= new Difficulty(difficultyName, length, wave)
        }
        
        levels[levelName].addTime(new Time(time))
        difficulties[[difficultyName, length]].addWave(wave)
        if (packet.getData(MatchPacket.keyResult) == "1") {
            levels[levelName].addLosses()
            difficulties[[difficultyName, length]].addLosses()
        } else {
            levels[levelName].addWins()
            difficulties[[difficultyName, length]].addWins()
        }
        
        packet.getData(MatchPacket.keyDeaths).each {source, count ->
            if (deaths[source] == null) {
                deaths[source]= count.toInteger()
            } else {
                deaths[source]+= count.toInteger()
            }
        }
    }
}

