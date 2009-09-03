#!/bin/bash

PROG_PATH=`dirname "$0"`
WHAT_HOME=`cd "$PROG_PATH/.."; pwd`

cat>runner.args<<EOF
--downloadFeedback=false
--log=NONE
--vmOptions=-Dbundles.configuration.location=../../conf -Dsms.modemConfig.home=$WHAT_HOME
--platform=equinox
--repositories=file:../equinox
scan-dir:../lib
EOF

exec java -jar pax-runner-0.17.0.jar
