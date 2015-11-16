REM You may fine tune these parameters at will.

@start /ABOVENORMAL javaw.exe -server -Djava.net.preferIPv4Stack=true -Xmn256m -Xms2g -Xmx2g -XX:-UseLargePages -Dnet.l2emuproject/allow_elevation -XX:+UseConcMarkSweepGC -XX:+AggressiveOpts -XX:+TieredCompilation -jar l2emuproject-netpro-app-1.3.jar
