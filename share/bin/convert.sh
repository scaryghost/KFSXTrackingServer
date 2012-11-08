#!/usr/bin/env sh

# This is an example of how to convert the database from version 1.0 to 2.0

if [ $# -lt "2" ] ; then
    echo "Error - Not enough arguments!"
    echo "usage: convert.sh [srcdb] [destdb]"
    exit 1
fi


cp=KFSXTrackingServer.jar
main=com.github.etsai.kfsxtrackingserver.convert.Main

exec java -cp $cp $main $1 $2
