KFSXTrackingServer
==================

## Updating From Version 1.0
If you have just started running a tracking server, this section is irrelevant and you can skip ahead 
to "Starting The Server".  Otherwise, keep reading.

### Converting the Database
The structure of the SQLite database has changed from v1.0.  To preserve current data, the jar file 
will also convert and migrate the data from the v1.0 db to the v2.0 db.  A script has been provided 
to execute the jar in conversion mode.  From the base directory, execute the convert script, providing 
the paths to the old and new databases.

Example:  
    # use .bat on Windows  
    share/bin/convert.sh ${path to v1.0 db} share/etc/kfsxdb.sqlite  
    
### Server Properties
For v2.0, the following properties have been removed:
* steam.polling.period
* db.write.period

while the following properties have been added:
* num.threads (must be at least 4)

Also, the database by default now resides in the "share/etc" folder.  Remember to update your current 
server.properties file before replacing the default one, also located in "share/etc".  

## Starting The Server
Before starting the server, make sure you look at the properties file: share/etc/server.properties.  
Configure the values to your preferred settings.  Once the properties file has been configured, run 
the startserver script from the base directory.

Example:  
    share/bin/startserver.sh (or .bat for Windows)  

## Release Notes:
https://github.com/scaryghost/KFSXTrackingServer/wiki/Release-KFSXTrackingServer-2.0
