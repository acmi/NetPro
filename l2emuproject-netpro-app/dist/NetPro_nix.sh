#!/bin/sh
# You may fine tune these parameters at will.

java -Dnet.l2emuproject/allow_elevation -server -Djava.net.preferIPv4Stack=true -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -jar l2emuproject-netpro-app-2.0-SNAPSHOT.jar &
