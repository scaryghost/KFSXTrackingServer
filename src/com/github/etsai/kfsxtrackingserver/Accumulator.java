/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

/**
 * Interface for accumulating and reordering packets
 * @author eric
 */
public interface Accumulator {
    public void addPacket(Packet packet);
}
