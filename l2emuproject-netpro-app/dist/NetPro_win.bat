REM You may fine tune these parameters at will.
REM Please check the unix executable for more optimal parameters. These should ensure high compatibility.

@start /ABOVENORMAL javaw.exe -Djava.net.preferIPv4Stack=true -Xmn64m -Xms512m -Xmx512m -Dnet.l2emuproject/allow_elevation -XX:+UseConcMarkSweepGC -jar l2emuproject-netpro-app-1.5.jar
