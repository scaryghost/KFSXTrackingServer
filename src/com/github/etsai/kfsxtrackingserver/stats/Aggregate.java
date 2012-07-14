/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver.stats;

/**
 *
 * @author etsai
 */
public class Aggregate extends Death {
    private final String category;
    
    public Aggregate(int id, String stat, String category) {
        super(id, stat);
        this.category= category;
    }
    
    public String getCategory() {
        return category;
    }
    
}
