#!/bin/bash

if [ `uname` = 'FreeBSD' ]; then
	PLATFORM="freebsd" export PLATFORM

	# compile.platform = the arch-specific path for headers in the JDK
	DEFINES="$DEFINES -Dcompile.otherlibs= -Dcompile.platform=freebsd -Dcompile.soext=so -Dcompile.jniext=so"
	DEFINES="$DEFINES -Dcompile.ld.static=-Bstatic -Dcompile.ld.dynamic=-Bdynamic -Dcompile.ld.shared=-Bshareable"
	DEFINES="$DEFINES -Dcompile.platform.define=__FreeBSD__"
fi
