#!/bin/bash

PROG_PATH=`dirname "$0"`
SMS_REFLECTOR_HOME=`cd "$PROG_PATH/.."; pwd`

cat>runner.args<<EOF
--downloadFeedback=false
--log=NONE
--vmOptions=-Dbundles.configuration.location=$SMS_REFLECTOR_HOME/conf -Dsms.modemConfig.home=$SMS_REFLECTOR_HOME -Dsms.modemConfig.url=file://$SMS_REFLECTOR_HOME/modemConfig.properties
--platform=equinox
--repositories=file:../equinox
scan-dir:../lib
EOF

exec java -jar pax-runner-0.17.0.jar
