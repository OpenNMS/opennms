#!/bin/sh
dropdb -U postgres opennms
pushd ~/git/opennms/core/schema
mvn clean package -DskipTests=true
cp target/org.opennms.core.schema-25.0.0-SNAPSHOT-liquibase.jar ~/git/opennms/target/opennms/lib/
pushd ~/git/opennms/target/opennms
sudo ./bin/install -dis
