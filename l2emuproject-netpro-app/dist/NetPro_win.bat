REM You may fine tune these parameters at will.
REM Please check the unix executable for more optimal parameters. These should ensure high compatibility.

@start /ABOVENORMAL javaw.exe -Dnet.l2emuproject/allow_elevation -server -Djava.net.preferIPv4Stack=true -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -jar l2emuproject-netpro-app-2.0-SNAPSHOT.jar
