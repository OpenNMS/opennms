#!/usr/bin/env bash

if curl http://localhost:8181/minion/rest/health/probe | grep --quiet "Everything is awesome"; then 
  exit 0;
else
  exit 1;
fi
