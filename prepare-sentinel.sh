#!/bin/bash

SOURCE_ROOT=/Users/mvrueden/dev/opennms/NMS-10539
OPENNMS_HOME="${SOURCE_ROOT}/target/opennms-24.0.0-SNAPSHOT"
SENTINEL_HOME="${SOURCE_ROOT}/features/container/sentinel/target/sentinel-karaf-1"

cp "${OPENNMS_HOME}"/etc/*.xml "${SENTINEL_HOME}/etc"
cp -R "${OPENNMS_HOME}/etc/events" "${SENTINEL_HOME}/etc"
cp -R "${OPENNMS_HOME}/etc/datacollection" "${SENTINEL_HOME}/etc"
cp -R "${OPENNMS_HOME}/etc/syslog" "${SENTINEL_HOME}/etc"
cp -R "${OPENNMS_HOME}/etc/resource-types.d" "${SENTINEL_HOME}/etc"
# TODO MVR where should this live?
cp -R "${SOURCE_ROOT}/opennms-webapp/src/main/webapp/svg" "${SENTINEL_HOME}/etc"
