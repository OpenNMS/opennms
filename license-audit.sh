#!/bin/sh
export MAVEN_OPTS="-Xms2g -Xmx16g"
mvn -o generate-resources -Pcreate-license-list
