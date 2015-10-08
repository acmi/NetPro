REM You may fine tune these parameters at will.
REM 1G is fine for (at least) 9-boxing. After all, who plays with less than a full party of chars at once nowadays?
REM You may specify a lower heap size, just keep in mind that once you set 128m (or lower), JavaFX internals may start interfering.

@start /ABOVENORMAL javaw.exe -Xmn128m -Xmx1g -Xms1g -server -XX:+TieredCompilation -XX:+AggressiveOpts -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -jar l2emuproject-netpro-app-1.3-SNAPSHOT.jar
