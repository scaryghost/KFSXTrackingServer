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
        def time= new Time(packet.getData(MatchPacket.keyTime).toInteger())
        
        if (levels[levelName] == null) {
            levels[levelName]= new Level(levelName)
        }
        if (difficulties[[difficultyName, length]] == null) {
            difficulties[[difficultyName, length]]= new Difficulty(difficultyName, length, wave)
        }
        
        levels[levelName].addTime(time)
        difficulties[[difficultyName, length]].addTime(time)
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

