/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package scaryghost.kfsxtrackingserver.convert;

import groovy.sql.Sql;
import java.sql.SQLException;

/**
 * Converts data from the Version 1.0 database to Version 2.0 database
 * @author etsai
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Sql src= Sql.newInstance(String.format("jdbc:sqlite:%s", args[0]));
        Sql dest= Sql.newInstance(String.format("jdbc:sqlite:%s", args[1]));
        
        long start= System.currentTimeMillis();
        System.out.println("Reading data from db: " + args[0]);
        System.out.println("Converting data to db: " + args[1]);
        DBEditor.convert(src, dest);
        long end= System.currentTimeMillis();
        System.out.println(String.format("Done: %1$.2f seconds", (end - start)/(double)1000));
    }
}
