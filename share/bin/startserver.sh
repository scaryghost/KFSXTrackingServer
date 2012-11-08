#!/usr/bin/env sh

# This script is an example of how to start the server

propertyfile=setup/properties/server.properties
jar=KFSXTrackingServer.jar

exec java -jar $jar -propertyfile $propertyfile
