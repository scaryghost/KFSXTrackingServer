@ECHO OFF

set argc=0
for %%x in (%*) do Set /A argc+=1

if %argc% LSS 2 (
    @ECHO ON
    echo "Error - Not enough arguments!"
    echo "usage: convert.bat [srcdb] [destdb]
    exit 1
)

set cp=KFSXTrackingServer.jar
set main=com.github.etsai.kfsxtrackingserver.convert.Main

java -cp %cp% %main% %1 %2
