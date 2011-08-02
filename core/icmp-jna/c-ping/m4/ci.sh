#!/bin/bash -v
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

usage() {
	echo "usage: $0 <bittedness> <SVN revision> <build number>"
	echo ""
}

die() {
	usage
	echo "failed: $@"
	exit 1
}

BITS="$1"; shift
REVISION="$1"; shift
BUILDNUM="$1"; shift

[ -n "$BITS"     ] || die "you must specify bittedness"
[ -n "$REVISION" ] || die "you must specify the SVN revision"
[ -n "$BUILDNUM" ] || die "you must specify the build number"

RPM_ARCH=""
RPM_ARGS=""
HOST_ARGS=""

OS=`uname -s | tr 'A-Z' 'a-z'`
MACHINE=`uname -m`

if [ "$OS" = "linux" ]; then
	if [ "$BITS" = "64" ]; then
		# HOST_ARGS="--host=x86_64-$OS"
		HOST_ARGS="--target=x86_64-$OS"
		RPM_ARCH="--with-rpm-arch=x86_64"
	else
		# HOST_ARGS="--host=i386-$OS"
		HOST_ARGS="--target=i386-$OS"
		RPM_ARCH="--with-rpm-arch=i386"
	fi
fi

sh m4/autogen.sh || die "failed to autogen"
make distclean || :
./configure --prefix=/usr --with-java="${JAVA_HOME}" --with-jvm-arch=$BITS "$RPM_ARCH" "$RPM_ARGS" "$HOST_ARGS" || die "failed to configure"
make dist || die "unable to make dist"
if [ -x /bin/rpm ]; then
	make rpm RELEASE="0.${REVISION}.${BUILDNUM}" || die "failed to make an RPM"
else
	make || die "failed to run make"
	make install DESTDIR=`pwd`/dist || die "failed to run make install"
	rm -rf dist
fi
