#!/bin/sh

LANG=C
LC_ALL=C
export LANG LC_ALL

./build.sh -Dtagging -DautoVersionSubmodules=true release:prepare 

