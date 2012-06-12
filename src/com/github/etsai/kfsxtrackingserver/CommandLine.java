/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver;

/**
 * Parses the command line options
 * @author etsai
 */
public abstract class CommandLine {
    public static CommandLine parse(String[] args) {
        return new com.github.etsai.kfsxtrackingserver.impl.CommandLineImpl(args);
    }
 
    public abstract String getPropertiesFilename();
}
