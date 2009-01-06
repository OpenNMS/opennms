@echo off
set MAVEN_OPTS=-Xmx512m
maven\bin\mvn -Dmaven.test.skip.exec=true %*