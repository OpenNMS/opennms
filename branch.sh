#!/bin/sh

LANG=C
LC_ALL=C
PATH=$PWD/maven/bin:$PATH
export LANG LC_ALL PATH

./build.sh -Dtagging -DautoVersionSubmodules=true -DupdateBranchVersions=true -DupdateWorkingCopyVersions=false "$@" release:branch
