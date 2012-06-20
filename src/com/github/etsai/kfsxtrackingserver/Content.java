/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

/**
 * Interface for storing and retrieving stats content
 * @author eric
 */
public interface Content {
    public boolean load();
    public boolean save();
    public void accumulate(Packet packet);
}
