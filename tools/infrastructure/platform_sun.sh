#!/bin/bash

VERSION_BUILD_PLATFORM_SUN='1.1.1.1'
PACKAGES="$PACKAGES PLATFORM_SUN"

if [ `uname` = 'SunOS' ]; then
	PATH="/usr/local/bin:/usr/xpg4/bin:/usr/ccs/bin:$PATH" export PATH
	LD_LIBRARY_PATH="/usr/local/pgsql:/usr/local/rrdtool-1.0.33:$LD_LIBRARY_PATH" export LD_LIBRARY_PATH
	PLATFORM=sun export PLATFORM
	# compile.platform = the arch-specific path for headers in the JDK
	DEFINES="$DEFINES -Dcompile.otherlibs=/lib/libc.so -Dcompile.platform=solaris -Dcompile.soext=so"
	DEFINES="$DEFINES -Dcompile.jniext=so -Dcompile.ld.static=-Bstatic -Dcompile.ld.dynamic=-Bdynamic"
	DEFINES="$DEFINES -Dcompile.ld.shared=-G -Dcompile.platform.define=__SOLARIS__"
fi
