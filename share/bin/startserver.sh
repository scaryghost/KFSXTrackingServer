#!/usr/bin/env sh

# This script is an example of how to start the server

propertyfile=share/etc/server.properties
jar=KFSXTrackingServer.jar

exec java -jar $jar -propertyfile $propertyfile
