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
        cli.stopAtNonOption= false
        cli.propertyfile(args:1, argName:'file', 'Reads in server properties from the file')
        cli.version('Prints the version and exit')
        cli.help('Prints this message')
        options= cli.parse(args)
        
        if (options == null) {
            System.exit(1)
        }
        if (options.version) {
            println "KFSXTrackingServer version 1.0"
            println "Match protocol version: ${MatchPacket.packetVersion}"
            println "Player protocol version: ${PlayerPacket.packetVersion}"
            System.exit(0)
        }
        if (options.help) {
            cli.usage()
            System.exit(0)
        }
    }
    
    public String getPropertiesFilename() {
        return options.propertyfile
    }
}

