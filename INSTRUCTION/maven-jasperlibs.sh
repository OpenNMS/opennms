#!/bin/sh
# Skript adds necessary libs to Maven-Repository

LIBS=lib


# JasperServer 2.1.0
#mvn install:install-file -DgroupId=jasperserver-common-ws -DartifactId=jasperserver-common-ws -Dversion=2.1.0 -Dpackaging=jar -Dfile=${LIBS}/jasperserver-common-ws/jasperserver-common-ws/2.1.0/jasperserver-common-ws-2.1.0.jar
#mvn install:install-file -DgroupId=axis -DartifactId=axis -Dversion=1.4patched -Dpackaging=jar -Dfile=${LIBS}/axis/axis/1.4patched/axis-1.4patched.jar
#mvn install:install-file -DgroupId=jaxrpc -DartifactId=jaxrpc -Dversion=1.0 -Dpackaging=jar -Dfile=${LIBS}/jaxrpc/jaxrpc/1.0/jaxrpc-1.0.jar
#mvn install:install-file -DgroupId=saaj-api -DartifactId=saaj-api -Dversion=1.3 -Dpackaging=jar -Dfile=${LIBS}/saaj-api/saaj-api/1.3/saaj-api-1.3.jar
#mvn install:install-file -DgroupId=jasperserver-ireport-plugin -DartifactId=jasperserver-ireport-plugin -Dversion=2.1.0 -Dpackaging=jar -Dfile=${LIBS}/jasperserver-ireport-plugin/jasperserver-ireport-plugin/2.1.0/jasperserver-ireport-plugin-2.1.0.jar

# JasperServer 3.0.0
mvn install:install-file -DgroupId=jasperserver-common-ws -DartifactId=jasperserver-common-ws -Dversion=3.0.0 -Dpackaging=jar -Dfile=${LIBS}/jasperserver-common-ws/jasperserver-common-ws/3.0.0/jasperserver-common-ws-3.0.0.jar
mvn install:install-file -DgroupId=jasperserver-ireport-plugin -DartifactId=jasperserver-ireport-plugin -Dversion=3.0.0 -Dpackaging=jar -Dfile=${LIBS}/jasperserver-ireport-plugin/jasperserver-ireport-plugin/3.0.0/jasperserver-ireport-plugin-3.0.0.jar
mvn install:install-file -DgroupId=axis -DartifactId=axis -Dversion=1.4patched -Dpackaging=jar -Dfile=${LIBS}/axis/axis/1.4patched/axis-1.4patched.jar
mvn install:install-file -DgroupId=jaxrpc -DartifactId=jaxrpc -Dversion=1.0 -Dpackaging=jar -Dfile=${LIBS}/jaxrpc/jaxrpc/1.0/jaxrpc-1.0.jar
mvn install:install-file -DgroupId=saaj-api -DartifactId=saaj-api -Dversion=1.3 -Dpackaging=jar -Dfile=${LIBS}/saaj-api/saaj-api/1.3/saaj-api-1.3.jar
