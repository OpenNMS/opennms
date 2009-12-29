#!/bin/bash -e

MYDIR=`dirname $0`
TOPDIR=`cd $MYDIR; pwd`

cd "$TOPDIR"

if [ -n "$1" ]; then
	RELEASE_MAJOR="$1"
	shift
else
	RELEASE_MAJOR=0
fi

if [ $RELEASE_MAJOR -eq 0 ]; then
	# Set the SVN checkout version if SVN is on the box, otherwise use the date as default
	RELEASE_MINOR=`date '+%Y%m%d'`
	RELEASE=$RELEASE_MAJOR.$RELEASE_MINOR
else
	RELEASE=$RELEASE_MAJOR
fi

VERSION=`grep '<version>' pom.xml | sed -e 's,^[^>]*>,,' -e 's,<.*$,,' -e 's,-[^-]*-SNAPSHOT$,,' -e 's,-SNAPSHOT$,,' -e 's,-testing$,,' | head -n 1`

if [ -z $JAVA_HOME ]; then
	# hehe
	for dir in /usr/java/jdk1.{5,6,7,8,9}*; do
		if [ -x "$dir/bin/java" ]; then
			export JAVA_HOME="$dir"
			break
		fi
	done
fi

if [ -z $JAVA_HOME ]; then
	echo "*** JAVA_HOME must be set ***"
	exit
fi

TAR=`which gtar 2>/dev/null || which tar 2>/dev/null`
RSYNC=`which rsync 2>/dev/null`
GPG=`which gpg 2>/dev/null`
WORKDIR="$TOPDIR/target/rpm"
export PATH="$TOPDIR/maven/bin:$JAVA_HOME/bin:$PATH"

if [ -z "$TAR" ]; then
	echo "*** could not find tar ***"
	exit 1
fi

if [ -z "$RSYNC" ]; then
	echo "*** could not find rsync ***"
	exit 1
fi

echo "==== Building OpenNMS RPMs ===="
echo
echo "Version: " $VERSION
echo "Release: " $RELEASE
echo

echo "=== Clean Up ==="

if [ -z "$SKIP_SETUP" ]; then
	if [ -z "$SKIP_CLEAN" ]; then
		./build.sh clean
	fi

	echo "=== Creating Working Directories ==="
	install -d -m 755 "$WORKDIR/tmp/opennms-$VERSION-$RELEASE/source"
	install -d -m 755 "$WORKDIR"/{BUILD,RPMS/{i386,i686,noarch},SOURCES,SPECS,SRPMS}

	echo "=== Copying Source to Source Directory ==="
	$RSYNC -aqr --exclude=.svn --exclude=target --delete --delete-excluded "$TOPDIR/" "$WORKDIR/tmp/opennms-$VERSION-$RELEASE/source/"

	echo "=== Creating a tar.gz archive of the Source in /usr/src/redhat/SOURCES ==="

	$TAR zcvf "$WORKDIR/SOURCES/opennms-source-$VERSION-$RELEASE.tar.gz" -C "$WORKDIR/tmp" "opennms-$VERSION-$RELEASE"
	$TAR zcvf "$WORKDIR/SOURCES/centric-troubleticketer.tar.gz" -C "$WORKDIR/tmp/opennms-$VERSION-$RELEASE/source/opennms-tools" "centric-troubleticketer"
fi

echo "=== Building RPMs ==="

rpmbuild -bb --define "_topdir $WORKDIR" --define "_tmppath $WORKDIR/tmp" --define "version $VERSION" --define "releasenumber $RELEASE" tools/packages/opennms/opennms.spec
rpmbuild -bb --define "_topdir $WORKDIR" --define "_tmppath $WORKDIR/tmp" --define "version $VERSION" --define "releasenumber $RELEASE" opennms-tools/centric-troubleticketer/src/main/rpm/opennms-plugin-ticketer-centric.spec

if [ -n "$GPG" ]; then
	if [ `$GPG $GPGOPTS --list-keys opennms@opennms.org 2>/dev/null | grep -c '^sub'` -gt 0 ]; then
		rpm --define "_signature gpg" --define "_gpg_name opennms@opennms.org" --resign "$WORKDIR"/RPMS/noarch/*.rpm
	fi
fi

echo "==== OpenNMS RPM Build Finished ===="
echo ""
echo "Your completed RPMs are in the $WORKDIR/RPMS/noarch directory."
