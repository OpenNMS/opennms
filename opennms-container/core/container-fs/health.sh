#!/usr/bin/env bash

if curl -sSF http://localhost:8980/opennms/rest/health/probe | grep --quiet "Everything is awesome"; then 
  exit 0;
else
  exit 1;
fi
