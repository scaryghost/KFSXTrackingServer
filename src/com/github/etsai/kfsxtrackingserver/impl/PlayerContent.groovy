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
 * @author etsai
 */
public class PlayerContent implements Content {
    private final def id
    private def session
    private def player
    private def actions
    private def weapons
    private def kills
    private def perks
    private def packets
    private def levels
    private def difficulties
    
    public PlayerContent(def id) {
        this.id= id
        session= [:]
        session["player"]= [:]
        session["actions"]= [:]
        session["weapons"]= [:]
        session["kills"]= [:]
        session["perks"]= [:]
        session["match"]= [:]
        
        player= [:]
        actions= [:]
        weapons= [:]
        kills= [:]
        perks= [:]
        packets= []
        difficulties= [:]
        levels= [:]
    }
    public boolean load() {
        throw new UnsupportedOperationException("Not yet implemented!")
    }
    public boolean save() {
        throw new UnsupportedOperationException("Not yet implemented!")
    }
    public void accumulate(Packet packet) {
        def id= packet.getData(PlayerPacket.playerId)
        def seqnum= packet.getSeqnum()
        
        packets[seqnum]= packet
        def completed= packets[id].last().isLast() && 
                packets[id].inject(true) {acc, val -> acc && (val != null) }
        if (completed) {
            buildSession()
        }
    }
    
    private void buildSession() {
        def time
        def accum= {packet, map, group ->
            packet.getData(PlayerPacket.keyStats).each {stat, value ->
                sessions[group][stat]= value
                map[stat]+= value
                if (stat == "time connected") time= new Time(value)
            }
        }
        def groupActions= [:]
        groupActions["player"]= {packet ->
            accum(packet, player, "player")
        }
        groupActions["weapon"]= {packet ->
            accum(packet, weapons, "weapon")
        }
        groupActions["kills"]= {packet ->
            accum(packet, kills, "kills")
        }
        groupActions["perks"]= {packet ->
            accum(packet, perks, "perks")
        }
        groupActions["actions"]= {packet ->
            accum(packet, actions, "actions")
        }
        groupActions["match"]= {packet ->
            sessions["match"]= packet.getData(PlayerPacket.keyStats)
            def levelName= sessions["match"]["map"]
            def difficultyName= sessions["match"]["difficulty"]
            def length= sessions["match"]["length"]
            def wave= sessions["match"]["wave"]
            def result= sessions["match"]["result"]
            
            if (levels[levelName] == null) {
                levels[levelName]= new Level(levelName)
            }
            if (difficulties[[difficultyName, length]] == null) {
                difficulties[[difficultyName, length]]= new Difficulty(difficultyName, length, wave)
            }
            levels[levelName].addTime(time)
            difficulties[[difficultyName, length]].addTime(time)
            difficulties[[difficultyName, length]].addWave(wave)
            if (result != "2") {
                levels[levelName].addLosses()
                difficulties[[difficultyName, length]].addLosses()
            } else {
                levels[levelName].addWins()
                difficulties[[difficultyName, length]].addWins()
            }
        }
        
        packets.each {packet ->
            groupActions[packet.getData(PlayerPacket.getGroup)]
        }
    }
}

