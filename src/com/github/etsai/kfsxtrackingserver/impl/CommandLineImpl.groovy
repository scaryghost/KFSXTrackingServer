/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.etsai.kfsxtrackingserver.impl

import com.github.etsai.kfsxtrackingserver.CommandLine
import groovy.util.CliBuilder

/**
 * Groovy implementation of the CommandLine abstract class
 * @author etsai
 */
public class CommandLineImpl extends CommandLine {
    private def options;
    
    public CommandLineImpl(String[] args) {
        def cli= new CliBuilder(usage:"cli.groovy")
        cli.propertyfile(args:1, argName:'file', 'sets properties to file')
        options= cli.parse(args)

        println options.propertyfile
        
    }
    
    public String getPropertiesFilename() {
        return options.propertyfile
    }
}

