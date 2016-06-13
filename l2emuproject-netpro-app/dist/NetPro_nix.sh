#!/bin/sh
# You may fine tune these parameters at will.

java -server -Djava.net.preferIPv4Stack=true -Xmn256m -Xms2g -Xmx2g -XX:+UseLargePages -Dnet.l2emuproject/allow_elevation -XX:+UseConcMarkSweepGC -XX:+AggressiveOpts -XX:+TieredCompilation -Djava.net.preferIPv4Stack=true -jar l2emuproject-netpro-app-1.5-SNAPSHOT.jar &
