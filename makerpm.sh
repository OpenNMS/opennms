#!/bin/bash

###
# First stab at a script to build RPMs
#
# May need to run as root as it must write to
# /usr/src/redhat
#
###

# TODO: Pass these as parameters
RELEASE_MAJOR=0

if [ $RELEASE_MAJOR -eq 0 ]; then
	# Set the SVN checkout version if SVN is on the box, otherwise use the date as default
	RELEASE_MINOR=`date +%Y%m%d`
	if which svn > /dev/null; then
	        SVN_DUMMY=`svn info $(dirname "$0") | grep -i revision | awk '{print $2}'`
	        if [ "$SVN_DUMMY" ]; then RELEASE_MINOR=$SVN_DUMMY; fi
	fi
	RELEASE=$RELEASE_MAJOR.$RELEASE_MINOR
else
	RELEASE=$RELEASE_MAJOR
fi

VERSION=1.3.5
PLATFORM=linux-centos-4

if [ -z $JAVA_HOME ]
then
echo "*** JAVA_HOME must be set ***"
exit
fi

echo "==== Building OpenNMS RPMs ===="
echo
echo "Version: " $VERSION
echo "Release: " $RELEASE
echo "Platform: " $PLATFORM
echo

echo "=== Build Clean ==="

./build.sh clean

if [ -d /tmp/opennms-$VERSION-$RELEASE/source ]
then
echo "=== Removing existing source directory ==="
rm -rf /tmp/opennms-$VERSION-$RELEASE/source
fi

echo "=== Create Source Directory ==="

mkdir -p /tmp/opennms-$VERSION-$RELEASE/source

echo "=== Copying Source to Source Directory ==="

cp -r * /tmp/opennms-$VERSION-$RELEASE/source/

echo "=== Removing .svn directories from the Source ==="

find /tmp/opennms-$VERSION-$RELEASE/source -name ".svn" -exec rm \-rf {} \;

echo "=== Creating a tar.gz archive of the Source in /usr/src/redhat/SOURCES ==="

tar zcvf /usr/src/redhat/SOURCES/opennms-source-$VERSION-$RELEASE.tar.gz -C /tmp opennms-$VERSION-$RELEASE

echo "=== Building RPMs ==="

rpmbuild -ba --define "platform $PLATFORM" --define "version $VERSION" --define "releasenumber $RELEASE" tools/packages/opennms/opennms.spec.in 

echo "=== RPM Build Complete ==="

echo "==== OpenNMS RPM Build Finished ===="
echo
echo "There should be three OpenNMS RPM files in /usr/src/redhat/RPMS/<arch>"
echo




