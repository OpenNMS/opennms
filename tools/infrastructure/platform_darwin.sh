#!/bin/bash

if [ `uname` = 'Darwin' ]; then
	PATH="/usr/local/bin:/sw/bin:$PATH" export PATH
	DYLD_LIBRARY_PATH="/sw/lib:/usr/local/rrdtool-1.0.33/lib:$DYLD_LIBRARY_PATH" export DYLD_LIBRARY_PATH
	PLATFORM=darwin export PLATFORM
	# compile.javainc = the arch-specific path for headers in the JDK
	DEFINES="$DEFINES -Dcompile.otherlibs=/usr/lib/libc.dylib -Dcompile.platform=darwin -Dcompile.soext=so"
	DEFINES="$DEFINES -Dcompile.jniext=jnilib -Dcompile.ld.static=-static -Dcompile.ld.dynamic=-dynamic"
	DEFINES="$DEFINES -Dcompile.ld.shared=-bundle -Dcompile.platform.define=__DARWIN__"

	for dir in /System/Library/Frameworks/JavaVM.framework/Versions/1.4*/Home; do
		if [ -z "$JAVA_FRAMEWORK" ]; then
			JAVA_FRAMEWORK="$dir"
		fi
	done
	DEFINES="$DEFINES -Djava.framework=$JAVA_FRAMEWORK"
fi
