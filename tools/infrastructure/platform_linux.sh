#!/bin/bash

VERSION_BUILD_PLATFORM_LINUX='1.1.1.1'
PACKAGES="$PACKAGES PLATFORM_LINUX"

if [ `uname` = 'Linux' ]; then
	PLATFORM="linux" export PLATFORM

	# compile.platform = the arch-specific path for headers in the JDK
	DEFINES="$DEFINES -Dcompile.otherlibs= -Dcompile.platform=linux -Dcompile.soext=so -Dcompile.jniext=so"
	DEFINES="$DEFINES -Dcompile.ld.static=-Bstatic -Dcompile.ld.dynamic=-Bdynamic -Dcompile.ld.shared=-shared"
	DEFINES="$DEFINES -Dcompile.platform.define=__LINUX__"

	ME=`echo "$0" | sed -e 's#.*/##'`

	# workaround for RedHat 7.0 and 1.3.0_02+ JVMs
	if [ -e /etc/mandrake-release ] ; then
		# nothing needed for mandrake?
		:

        elif [ -e /etc/fedora-release ] ; then
                relnum=`cat /etc/fedora-release | awk '{print $4}'`
 
                major=`echo $relnum | cut -d'.' -f1`
                minor=`echo $relnum | cut -d'.' -f2`
 
                # Use this, if needed
 
                unset relnum
                unset major
                unset minor

	elif [ -e /etc/redhat-release ] ; then

		fedtest=`cat /etc/redhat-release | awk '{print $1}'`

		if [ $fedtest != 'Fedora' ] && [ $fedtest != 'CentOS' ] ; then

			relnum=`cat /etc/redhat-release | awk '{print $5}'`
			major=`echo $relnum | cut -d'.' -f1`
			minor=`echo $relnum | cut -d'.' -f2`

			if [ $major -eq 7 ]; then
				LD_ASSUME_KERNEL="2.2.5" export LD_ASSUME_KERNEL
				ulimit -s 2048 >/dev/null 2>&1
			fi

			unset relnum
			unset major
			unset minor

		fi
		
		unset fedtest
	fi
fi
