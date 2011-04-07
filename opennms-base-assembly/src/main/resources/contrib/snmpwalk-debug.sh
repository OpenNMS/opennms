#!/bin/sh

# generate an snmpwalk file for debugging purposes
#
#
# This file is part of the OpenNMS(R) Application.
#
# OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
# OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
# OpenNMS Licensing       <license@opennms.org>
#     http://www.opennms.org/
#     http://www.opennms.com/

host="$1"; shift
community="$1"; shift

if [ -z "$host" ]; then
	echo "usage: $0 <host> [community [extra OID tables]]"
	echo ""
	exit 1
fi

if [ -z "$community" ]; then
	community="public"
fi

SNMPWALK=`which snmpbulkwalk 2>/dev/null`
if [ ! -x "$SNMPWALK" ]; then
	SNMPWALK=`which snmpwalk 2>/dev/null`
	if [ ! -x "$SNMPWALK" ]; then
		echo "unable to find snmpbulkwalk or snmpwalk in your path"
		exit 1
	fi
fi

for table in system interfaces ipaddr ifxtable ipaddresstable "$@"; do
	echo "walking $table..." >&2
	echo "# table $table"
	if ! $SNMPWALK -OUne -v2c -c "$community" "$host" "$table"; then
		echo "an error occurred walking host '$host' (table: $table) with community '$community'"
		exit 2
	fi
done
