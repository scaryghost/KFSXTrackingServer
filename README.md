KFSXTrackingServer
==================
## Updating From 2.x
If you have just started running a tracking server, this section is irrelevant and you can skip ahead to 
"Starting The Server".  Otherwise, keep reading.

### Migrating Data
The structure of the SQLite database has changed from v2.x.  To preserve current data, the jar file will also convert 
and migrate the data from the v2.x db to the v3.0 db.  A script has been provided to execute the jar in conversion 
mode.  From the base directory, execute the convert script, providing the paths to the old and new databases.

Example:  

    # use .bat on Windows  
    share/bin/convert.sh ${path to v2.x db} share/etc/kfsxdb.sqlite3  
    
Please note that the extension to the db has been changed to sqlite3.  This was done to signify that the database was 
created using sqlite3.

If you had modified the css file to customize the web content, you will need to copy that file to the new directory.

### Server Properties
For v3.0, the following properties have been added:  
* http.root.dir  
* num.db.conn
* db.url
* db.driver
* db.reader.script
* db.writer.script
* db.user
* db.password
 
Meanwhile, the following properties have been removed:  
* num.threads 
* db.name

The new db.url property functions almost exactly like the previous db.name property with the minor difference being a 
full JDBC url must be given for the database instead of a simple path.  By default, the application uses SQLite so the 
url will now be:

    jdbc:sqlite:share/etc/kfsxdb.sqlite3

The change was made to allow users to create their own database for storing the statistics, rather than depend solely 
on SQLite.

## Starting The Server
Before starting the server, make sure you look at the properties file: share/etc/server.properties.  
Configure the values to your preferred settings.  Once the properties file has been configured, run 
the startserver script from the base directory.

Example:  

    share/bin/startserver.sh (or .bat for Windows)  

For convenience, you can copy the scripts in the bin folder to the root directory

## Custom Database
If the default SQLite database is not to your liking, you may choose to setup a db of your choosing for the 
application.  You will need to create a custom data writer so the application knows how to store the data in your db.  
The project wiki has more information on creating a the necessary files for custom database support.

https://github.com/scaryghost/KFSXTrackingServer/wiki/Custom-Database

### Start Up
If your custom database uses another driver, such as PostgreSQL, you will need to modify the startup line to point to 
your databases' driver.  The server should instead be started as follows:
    
    java -cp libs:${your JBDC driver} com.github.etsai.kfsxtrackingserver.Main -propertyfile ${your property file}
    

This roundabout method is required since the jar's manifest file only knows about the SQLite driver.

## Web Server
Starting from v3.0 and on, the web portion of the application has been separated from the jar file.  This gives end 
users greater flexibility in customizing the web pages.  The project wiki has more information:

https://github.com/scaryghost/KFSXTrackingServer/wiki/Creating-Web-Content
## Release Notes:
https://github.com/scaryghost/KFSXTrackingServer/wiki/Release-KFSXTrackingServer-3.0
