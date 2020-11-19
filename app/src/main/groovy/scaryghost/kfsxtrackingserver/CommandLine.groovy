package scaryghost.kfsxtrackingserver

import groovy.cli.picocli.CliBuilder

/**
 * Processes command line arguments
 * @author etsai
 */
public class CommandLine {
    private def options;
    
    public CommandLine (String[] args) {
        def cli= new CliBuilder(usage:"cli.groovy")
        cli.stopAtNonOption= false
        cli.propertyfile(args:1, argName:'file', required:true, 'Reads in server properties from the file')
        cli.version('Prints version information and exits')
        cli.help('Prints this message')
        cli.r(longOpt: 'refactor', args:1, argName:'group', 'Refactors the stats in the specified group, then exit')
        cli._(longOpt: 'refactor-info', args: 1, argName:'info', 'Extra information that may be needed to refactor the data')
        options= cli.parse(args)
        
        if (options == null) {
            System.exit(1)
        }
        if (options.version) {
            println "KFSXTrackingServer - Version ${Version.gitTag}"
            println "Match protocol version: ${PacketParser.MatchPacket.VERSION}"
            println "Player protocol version: ${PacketParser.PlayerPacket.VERSION}"
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

    public String getRefactorGroup() {
        return options.r ? options.r : null
    }

    public String getRefactorInfo() {
        return options.'refactor-info'
    }
}

