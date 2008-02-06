#!/bin/bash

#
# Wrapper script for Java MIB parser.
# David Hustace
# Copyright (c) The OpenNMS Group
# Friday, February 13, 2004

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
# This script sets up the java call to the Java MIB
# parser written by John Rodriguez.
#

#
# A few needed globals.
#
JP=""
num_args="$#"
all_args="$*"
searchdirs="/usr /opt /Library"

#
# Needed functions.
#

#
# Function: usage()
# This function checks to see if at least
# one argument was passed to the script.
#
usage()
{
	if [ "$num_args" -lt "1" ]
	then
		echo "    Missing arguments..."
		echo "    Usage: `basename $0` <MIB File 1> [<MIB file 2>...]"
		echo "    Example: `basename $0` RFC-1213.my"
		echo ""
		return 1
	fi
}

#
# Function: argTest()
# This function checks to see if the arguments passed
# are names of files readable by the user.  No
# attempt is made to verify that the file is actually
# a valid MIB.
#
argTest()
{
	for mib in $all_args
	do
		if [ ! -r $mib ]
		then
			echo "    $mib not readable"
			return 1
		fi
	done
}

#
# Function: javaCheck()
# Requires: variable $JP (java path)
# This function verifies the version of the java
# command found in the javaPath() and the
# javaFind() functions.
#
javaCheck()
{
	echo "    Checking Java version for 1.4+..."
	ret=`$JP -version 2>&1`
	echo "    Version is: $ret"
	echo $ret | grep '1\.[456789]\.' 1>/dev/null 2>/dev/null 
	if [ $? = 0 ]
	then
		return 0
	else
		return 1
	fi
}

#
# Function: javaPaths()
# This function attempts to find a path
# to a java executable.  It searchs for
# files (hopefully directories) that match
# the regex "^j2*" or "^java$" and appends "/bin/java".
# It then tests to see if this there is an
# executable file by this name.  This is not
# foolproof but should get us close.
#
javaPaths() {
	for searchdir in $searchdirs; do
		if [ ! -d "$searchdir" ]; then
			continue;
		fi

		# We search "j2*" for the Sun-supplied Java packages ("j2sdk"),
		# "java" for the Java installation shipped with SuSE, and "Home"
		# to catch /Library/Java/Home on Mac OS X.
		jdirs="`find \"$searchdir\" -maxdepth 2 \\( -name \"j2*\" -o -name \"jdk*\" -o -name \"java\" -o -name \"Home\" \\) -print`"
		for jdir in $searchdir/java/default $searchdir/java/latest $jdirs; do
			if [ -x $jdir/bin/java ]; then
				JP=$jdir/bin/java
				javaCheck && return 0
			fi
		done
	done
	return 1
}

#
# Function: javaFind()
# This function first checks the current
# path for a java executable then then
# searches for one by calling javaPaths().
#
javaFind()
{
	#
	# Do we have java in our path
	#
	echo "    Looking for a good java..."
	saveJP=$JP
	JP=`which java`
	if [ "$?" -eq "1" ]
	then
		#
		# No java in our path
		# Look for it
		#
		javaPaths
		if [ "$?" = "0" ]
		then
			echo "    Found a good java..."
			return 0
		else
			JP=$saveJP
			return 1
		fi
	else
		echo "    Using java in user's path..."
		javaCheck
		if [ "$?" -eq "0" ]
		then
			return 0
		else
			return 1
		fi
		return 0
	fi
}

#
# Function: javaHome()
# This function sets the JAVA_HOME
# if it is not currently set.
#
# This may still need some work for
# the java in the path may not be the same JAVA_HOME
# that we find.
#
javaHome()
{
	echo "    Checking for JAVA_HOME..."
	if [ -z "$JAVA_HOME" ]
	then
		echo "    JAVA_HOME not set, trying to find it..."
		
		if [ $JP != "/usr/bin/java" ]
		then
			JAVA_HOME=`echo $JP | awk -F "/" '{ print substr($0,1,length($0) - length("/bin/java")) }'`
			return 0
		else
			echo "    java path is in /usr/bin, looking for actual HOME..."
			saveJP=$JP
			javaPaths
			if [ "$?" -eq "0" ]
			then
				JAVA_HOME=`echo $JP | awk -F "/" '{ print substr($0,1,length($0) - length("/bin/java")) }'`
				return 0
			else
				JP=$saveJP
				return 1
			fi
		fi
	else
		echo "    JAVA_HOME is already set."
	fi
}

#
# Main
#

#
# Set the path for the parser applicaiton.  This script
# must be in the same directory as the JAR file.
#
parserPath=`echo $0 | awk -F "/" '{ print substr($0,1,length($0) - length("/parseMib.sh")) }'`
if [ ! -r $parserPath/mibparser.jar ]
then
	echo "    JAR file: mibparser.jar not located in same directory as this script."
	exit 1
fi

#
# Usage
#
usage
if [ "$?" -eq "1" ]
then
	exit 1
fi

#
# Verify args.
# Args are MIB files to be converted.
# This test is only to see if they are readable by
# the user.
#
argTest
if [ "$?" -eq "1" ]
then
	exit 1
fi

#
# Find a good java.
# This call sets the JP (Java Path) varible
# to point to a java version that is >= 1.4.
#
javaFind
if [ "$?" -eq "1" ]
then
    echo Failed to find a Java 1.4 or greater JRE.  Exiting.
	exit 1
fi

#
# Set the JAVA_HOME.
# Check to see if JAVA_HOME is set and if it
# isn't set it.
#
javaHome
if [ "$?" -eq "1" ]
then
	echo "    Could not set JAVA_HOME."
	exit 1
else
	echo "    JAVA_HOME set to: $JAVA_HOME."
	export JAVA_HOME
fi

#
# Convert the MIB using the Java class.
#
echo "    Calling parser..."
echo ""
$JP -classpath $parserPath/mibparser.jar ParseMib $all_args
