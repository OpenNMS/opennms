#!/usr/bin/env bash

if ssh -o StrictHostKeyChecking=no -p 8201 localhost health:check | grep --quiet "Everything is awesome"; then 
  exit 0;
else
  exit 1;
fi
