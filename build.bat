@echo off
set MAVEN_OPTS=-XX:MaxPermSize=256m -Xmx512m
maven\bin\mvn -Dmaven.test.skip.exec=true %*
