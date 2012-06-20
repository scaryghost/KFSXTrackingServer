/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

/**
 *
 * @author eric
 */
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

