#!/bin/sh

LANG=C
LC_ALL=C
PATH=$PWD/maven/bin:$PATH
export LANG LC_ALL PATH

./build.sh -Dtagging -Ptog-sign -Darguments='-Dtagging -Droot.dir=${basedir} -Dmaven.test.skip.exec=true -DexcludePackageNames=org.opennms.netmgt.config.notificationCommands' "$@" release:perform
