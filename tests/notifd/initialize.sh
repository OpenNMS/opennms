#!/bin/bash

psql -U opennms opennms < clean.sql

cp *.xml /opt/OpenNMS/etc
