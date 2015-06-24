#!/bin/sh

#############################################################
# This file is part of the OpenNMS(R) Application.
#
# OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
# OpenNMS(R) is a derivative work, containing both original code, included code and modified
# code that was published under the GNU General Public License. Copyrights for modified
# and included code are below.
#
# OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
#
# Modifications:
# 
# Created: September 3, 2009
#
# Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
#
# For more information contact:
#      OpenNMS Licensing       <license@opennms.org>
#      http://www.opennms.org/
#      http://www.opennms.com/
#
#############################################################


OPENNMS_HOME="${install.dir}"
SEND_EVENT="${OPENNMS_HOME}/bin/send-event.pl"
EVENT_UEI=uei.opennms.org/internal/configureSNMP

makeIpRange() {
	echo $1 | grep -- '-' 1>/dev/null 2>/dev/null

	if [ $? = 0 ]; then
		firstip=`echo $1 | awk -F- '{ print $1 }'`
		lastip=`echo $1 | awk -F- '{ print $2 }'`
		ipparams="-p 'firstIPAddress $firstip' -p 'lastIPAddress $lastip'"
	else
		ipparams="-p 'firstIPAddress $1'"
	fi
	shift
}

usage() {
	echo "Usage:"
	echo "$0 -v [1|2c|3] \\"
	echo "  -c [communityString] -t [timeout] -r [retries] [ipaddr|firstaddr-lastaddr]..."
	echo
	echo "NB timeout is in milliseconds"
}

# Execution starts here
args=`getopt v:c:t:r:n $*`

if [ $? != 0 ]; then
	usage
	exit 2
fi

set -- $args
for i
do
	case "$i"
	in
		-v)
			version=$2
			shift
			shift
			;;
		-c)
			community=$2
			shift
			shift
			;;
		-t)
			timeout=$2
			shift
			shift
			;;
		-r)
			retry=$2
			shift
			shift
			;;
		-n)
			dryrun=1
			shift
			;;
	esac
done

if [ x"$version" = x ]; then
	echo "Version is required"
	usage
	exit 2
fi
if [ x"$community" = x ]; then
	echo "Community is required"
	usage
	exit 2
fi
if [ x"$timeout" = x ]; then
	echo "Timeout is required"
	usage
	exit 2
fi
if [ x"$retry" = x ]; then
	echo "Retry is required"
	usage
	exit 2
fi

case "$version"
in
	v1|1)
		fullversion=v1
		;;
	v2c|v2|2c|2)
		fullversion=v2c
		;;
	v3|3)
		fullversion=v3
		;;
	*)
		echo "Invalid SNMP version '$version'"
		usage
		exit 2
		;;
esac


shift  # eat the --

# Now do the commands
for ipaddr
do
	makeIpRange $ipaddr
	cmd="$SEND_EVENT -p 'version $fullversion' -p 'communityString $community' -p 'timeout $timeout' -p 'retryCount $retry' $ipparams $EVENT_UEI"
	if [ x"$dryrun" != x ]; then
		echo $cmd
	else
		sh -c "$cmd"
	fi
done
