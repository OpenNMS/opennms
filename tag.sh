#!/bin/sh

LANG=C
LC_ALL=C
PATH=$PWD/maven-2.0.4/bin:$PATH
export LANG LC_ALL PATH

./build.sh -Dtagging -DautoVersionSubmodules=true release:prepare 

