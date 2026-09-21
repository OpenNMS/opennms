#!/usr/bin/env bash

if curl -sSf http://localhost:8181/sentinel/rest/health/probe | grep --quiet "Everything is awesome"; then
  exit 0;
else
  exit 1;
fi
