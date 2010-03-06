@echo off
set MAVEN_OPTS=-XX:PermSize=64M -XX:MaxPermSize=256M -Xmx1G
maven\bin\mvn -Dmaven.test.skip.exec=true %*
