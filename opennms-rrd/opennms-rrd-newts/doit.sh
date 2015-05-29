#!/bin/sh
OPENMMS_HOME=~/git/opennms/target/opennms
rm -rf ./target
../../compile.pl
sudo cp target/opennms-rrd-newts-17.0.0-SNAPSHOT.jar $OPENNMS_HOME/lib/
sudo $OPENMMS_HOME/bin/opennms restart
