#!/bin/sh

VERSION_BUILD_PLATFORM_SUN='$Revision$'
PACKAGES="$PACKAGES PLATFORM_DARWIN"

if [ `uname` = 'Darwin' ]; then
	PATH="/usr/local/bin:/sw/bin:$PATH" export PATH
	DYLD_LIBRARY_PATH="/sw/lib:/usr/local/rrdtool-1.0.33/lib:$DYLD_LIBRARY_PATH" export DYLD_LIBRARY_PATH
	PLATFORM=darwin export PLATFORM
	# compile.javainc = the arch-specific path for headers in the JDK
	DEFINES="$DEFINES -Dcompile.otherlibs=/usr/lib/libc.dylib -Dcompile.platform=darwin -Dcompile.soext=so"
	DEFINES="$DEFINES -Dcompile.jniext=jnilib -Dcompile.ld.static=-static -Dcompile.ld.dynamic=-dynamic"
	DEFINES="$DEFINES -Dcompile.ld.shared=-bundle -Dcompile.platform.define=__DARWIN__"
fi
