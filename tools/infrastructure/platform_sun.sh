#!/bin/bash

VERSION_BUILD_PLATFORM_SUN='1.1.1.1'
PACKAGES="$PACKAGES PLATFORM_SUN"

if [ `uname` = 'SunOS' ]; then
	PATH="/usr/local/bin:/usr/xpg4/bin:/usr/ccs/bin:$PATH" export PATH
        LD_LIBRARY_PATH="/usr/local/lib:/usr/local/ssl/lib/:/usr/local/pgsql/lib:/usr/local/opennms/lib:/usr/local/pgsql/include/server/lib:/usr/local/pgsql/include/internal/lib:$LD_LIBRARY_PATH" export LD_LIBRARY_PATH
	POSTGRES_LIB="/usr/local/pgsql/lib" export POSTGRES_LIB
        PERL5LIB="/usr/local/lib/perl5" export PERL5LIB
	PLATFORM=sun export PLATFORM
	# compile.platform = the arch-specific path for headers in the JDK
	DEFINES="$DEFINES -Dcompile.otherlibs=/lib/libc.a -Dcompile.platform=solaris -Dcompile.soext=so"
	DEFINES="$DEFINES -Dcompile.jniext=so -Dcompile.ld.static=-Bstatic -Dcompile.ld.dynamic=-Bdynamic"
	DEFINES="$DEFINES -Dcompile.ld.shared=-G -Dcompile.platform.define=__SOLARIS__"
	DEFINES="$DEFINES -Dcompile.postgresql.include=/usr/local/pgsql/include"
	DEFINES="$DEFINES -Dcompile.postgresql.lib=/usr/local/pgsql/lib"
	DEFINES="$DEFINES -Dcompile.rrdtool.include=/usr/local/rrdtool-1.0.37/include"
	DEFINES="$DEFINES -Dcompile.rrdtool.lib=/usr/local/rrdtool-1.0.37/lib"
fi
