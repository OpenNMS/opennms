#!/bin/sh

LANG=C
LC_ALL=C
PATH=$PWD/maven/bin:$PATH
export LANG LC_ALL PATH

./compile.pl -Dtagging -DautoVersionSubmodules=true "$@" release:prepare
