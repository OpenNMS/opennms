#!/bin/bash
#*******************************************************************************
# This file is part of the OpenNMS(R) Application.
#
# OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
# OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
#
# OpenNMS(R) is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# OpenNMS(R) is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
#     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
#
# For more information contact: 
#     OpenNMS(R) Licensing <license@opennms.org>
#     http://www.opennms.org/
#     http://www.opennms.com/
#*******************************************************************************

PROG_PATH=`dirname "$0"`
SMS_REFLECTOR_HOME=`cd "$PROG_PATH/.."; pwd`

cat>runner.args<<EOF
--downloadFeedback=false
--log=NONE
--vmOptions=-Dbundles.configuration.location=$SMS_REFLECTOR_HOME/conf -Dsms.modemConfig.home=$SMS_REFLECTOR_HOME -Dsms.modemConfig.url=file://$SMS_REFLECTOR_HOME/modemConfig.properties -Dgnu.io.SerialPorts=/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2:/dev/ttyACM3:/dev/ttyACM4:/dev/ttyACM5 -Dsmslib.serial.polling=true
--platform=equinox
--repositories=file:../equinox
scan-dir:../lib
EOF

exec java -jar pax-runner-1.1.1.jar
