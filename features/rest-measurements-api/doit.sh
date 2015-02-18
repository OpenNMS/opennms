#!/bin/sh
../../compile.pl
sudo cp ./target/org.opennms.features.rest-measurements-api-16.0.0-SNAPSHOT.jar ~/git/opennms/target/opennms/lib/org.opennms.features.rest-measurements-api-16.0.0-SNAPSHOT.jar
sudo $OPENNMS_HOME/bin/opennms restart
