#!/bin/sh
pushd ../features/gwt-snmpselect-list/ && ../../clean.pl && ../../compile.pl && cp ./target/org.opennms.features.gwt-snmpselect-list-14.0.2-SNAPSHOT.jar $OPENNMS_HOME/lib/ && popd
../clean.pl && ../compile.pl && cp -R target/opennms-webapp-14.0.2-SNAPSHOT/* ~/git/opennms/target/opennms/jetty-webapps/opennms/
sudo $OPENNMS_HOME/bin/opennms restart
