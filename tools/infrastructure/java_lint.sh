#!/bin/bash

VERSION_BUILD_JAVA_LINT='1.3'
PACKAGES="$PACKAGES JAVA_LINT"
MINIMUM_JAVA=1.4.0

################################# NOTE #################################
# java_lint () is run when this library is loaded
########################################################################

for dir in $OPENNMS_HOME/lib/scripts $PREFIX/tools/infrastructure; do
	if [ -f $dir/version_compare.sh ]; then
		. $dir/version_compare.sh
	fi
done

# java_lint ()
#   input : N/A
#   output: Checks JAVA_HOME (if it exists) and makes
#	   sure the JDK is OK, and finds it if it's not.

java_lint () {

	if [ "$STATUS_ONLY" = "yes" ]; then
		return 1;
	fi

	# we want to force 1.4 on MacOSX if it exists, even if
	# JAVA_HOME is set since /Library/Java/Home is still 1.3

	for jdk in /System/Library/Frameworks/JavaVM.framework/Versions/1.4*/Home; do
		if [ -d "$jdk" ] && [ -d "$jdk/bin" ]; then
			export JAVA_HOME="$jdk";
			break;
		fi
	done

	if [ -z "$JAVA_HOME" ]; then

		# prefer the Apple JDK, then the newest 1.4 sun JDK, then whatever's left
		JAVADIR=`ls -1 /usr/java 2>/dev/null | grep 1.4 | sort | tail -1`
		for jdk in /System/Library/Frameworks/JavaVM.framework/Versions/1.4*/Home /Library/Java/Home $JAVADIR /usr/java/jdk1.4* /usr/java/j2sdk1.4*; do
			if [ -x "$jdk/bin/java" ]; then
				export JAVA_HOME="$jdk";
				break
			fi
		done

	fi

	if [ -z "$JAVA_HOME" ]; then

		JAVA_PATH=`which java 2>&1 | grep -v "no java"`
		if [ ! -z "$JAVA_PATH" ]; then
			JAVA_PATH=`echo $JAVA_PATH | sed -e 's#/bin/java##'`
			if [ "$?" -eq "0" ]; then
				export JAVA_HOME="$JAVA_PATH"
			fi
		fi
	fi
 
	if [ -f "$JAVA_HOME/lib/tools.jar" ] || [ "`uname`" = "Darwin" ]; then
		if [ -z "$CLASSPATH" ] ; then
			CLASSPATH="$JAVA_HOME/lib/tools.jar" export CLASSPATH
		else
			CLASSPATH="$CLASSPATH:$JAVA_HOME/lib/tools.jar" export CLASSPATH
			fi

		else

			cat <<END
ERROR: Either your JDK is too old, or I was unable to find
echo lib/tools.jar in your JAVA_HOME directory.  Please make
sure you have a Java2-compliant compiler and JVM.  JDK
versions earlier than 1.3 may work with OpenNMS on some
platforms, but are not supported.
END
			exit 2
		fi

	if ! check_java_version $JAVA_HOME; then
		cat <<END
ERROR: Your JDK does not meet the minimum version of ${MINIMUM_JAVA}.
Please upgrade your JDK and set \$JAVA_HOME to the location
of the root of the JDK directory.

END
		exit 3
	else
		# Since the JDK is a valid revision
		# check to see if it is a HotSpot VM
		# If it's hotspot then we can use the
		# -server flag
		is_hotspot_vm $JAVA_HOME
	fi

	if [ -z "$JAVA_HOME" ]; then

		cat <<END
I was unable to locate a JDK to use for startup.  Please
add the location of your 'java' executable to your path, or
set the JAVA_HOME environment variable to the top-level
directory of your JDK (i.e., if you installed the recommended
IBM JDK from RPMs, it is put in "/opt/IBMJava2-13".

END
		exit 4

	fi

	export JAVA_HOME

	return
}

# check_java_version ()
#   input : a path to check (java_home)
#   output: returns true if JDK is OK, false if not

check_java_version () {
	[ -z "$1" ] && return 1

	if [ -x "$1/bin/java" ]; then
		if $1/bin/java -version 2>&1 | grep "cannot open shared object file" >/dev/null 2>&1; then
			cat <<END_SOERROR

Error!  You have the JDK 1.4.0 installed but you are
missing a required dependency.  Please make sure you
install the "compat-libstdc++" RPM from your CD, or,
if you used the web installer, run:

	apt-get install compat-libstdc++

END_SOERROR
			exit 1
		fi
		JAVA_VERSION=`$1/bin/java -version 2>&1 | grep "java version" | sed -e 's#^[^"]*"##' | sed -e 's#".*$##'`
		[ "$VERBOSE" = "1" ] && echo "JAVA_VERSION=$JAVA_VERSION"
		check_version $JAVA_VERSION $MINIMUM_JAVA
		return $?
	fi

}

# is_hotspot_vm()
#  input: a path to check (java_home)
#  output: returns true if the JRE is a HotSpot VM.

is_hotspot_vm () {
	_rc=1
	HOTSPOT=false
	if [ -n "$1" -a -x "$1/bin/java" ] ; then
		if $1/bin/java -version 2>&1 | grep -i HotSpot >/dev/null 2>&1; then
			if [ "`uname`" != "Darwin" ]; then
				HOTSPOT=true
				_rc=0
			fi
		fi
	fi
	return $_rc
}

java_lint
