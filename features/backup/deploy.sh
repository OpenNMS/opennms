#!/bin/sh
mvn clean install -DskipTests=true
cp api/target/org.opennms.features.backup.api-29.0.0-SNAPSHOT.jar minion/target/org.opennms.features.backup.minion-29.0.0-SNAPSHOT.jar service/target/org.opennms.features.backup.service-29.0.0-SNAPSHOT.jar "$OPENNMS_HOME/lib/"
