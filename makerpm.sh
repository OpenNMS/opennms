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

EXTRA_INFO=""
EXTRA_INFO2=""

GIT=`which git 2>/dev/null`
if [ -n "$GIT" ] && [ -x "$GIT" ]; then
	BRANCH=`git branch | grep -E '^\*' | awk '{ print $2 }'`
	COMMIT=`git log -1 | grep -E '^commit' | cut -d' ' -f2`
	if [ $RELEASE_MAJOR = 0 ]; then
		EXTRA_INFO="This is an OpenNMS build from the $BRANCH branch.  For a complete log, see:"
	else
		EXTRA_INFO="This is an OpenNMS build from Git.  For a complete log, see:"
	fi
	EXTRA_INFO2="  http://opennms.git.sourceforge.net/git/gitweb.cgi?p=opennms/opennms;a=shortlog;h=$COMMIT"
fi

if [ $RELEASE_MAJOR = 0 ]; then
	RELEASE_MINOR=`date '+%Y%m%d'`
	RELEASE=$RELEASE_MAJOR.$RELEASE_MINOR.1
else
	RELEASE=$RELEASE_MAJOR
fi

VERSION=`grep '<version>' pom.xml | sed -e 's,^[^>]*>,,' -e 's,<.*$,,' -e 's,-[^-]*-SNAPSHOT$,,' -e 's,-SNAPSHOT$,,' -e 's,-testing$,,' -e 's,-,.,g' | head -n 1`

if [ -z "$JAVA_HOME" ]; then
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

if [ -z "$OPENNMS_SKIP_COMPILE" ]; then
	OPENNMS_SKIP_COMPILE=0
fi

if [ -z "$SKIP_SETUP" ]; then
	if [ -z "$SKIP_CLEAN" ]; then
		echo "=== Clean Up ==="
		./compile.pl clean
		./assemble.pl clean
	fi

	echo "=== Creating Working Directories ==="
	install -d -m 755 "$WORKDIR/tmp/opennms-$VERSION-$RELEASE/source"
	install -d -m 755 "$WORKDIR"/{BUILD,RPMS/{i386,i686,noarch},SOURCES,SPECS,SRPMS}

	echo "=== Copying Source to Source Directory ==="
	$RSYNC -aqr --exclude=.git --exclude=.svn --exclude=target --delete --delete-excluded "$TOPDIR/" "$WORKDIR/tmp/opennms-$VERSION-$RELEASE/source/"

	echo "=== Creating a tar.gz archive of the Source in /usr/src/redhat/SOURCES ==="

	$TAR zcf "$WORKDIR/SOURCES/opennms-source-$VERSION-$RELEASE.tar.gz" -C "$WORKDIR/tmp" "opennms-$VERSION-$RELEASE"
	$TAR zcf "$WORKDIR/SOURCES/centric-troubleticketer.tar.gz" -C "$WORKDIR/tmp/opennms-$VERSION-$RELEASE/source/opennms-tools" "centric-troubleticketer"
fi

if [ -z "$SKIP_RPMBUILD" ]; then
	echo "=== Building RPMs ==="

	rpmbuild -bb --define "skip_compile $OPENNMS_SKIP_COMPILE" --define "extrainfo $EXTRA_INFO" --define "extrainfo2 $EXTRA_INFO2" --define "_topdir $WORKDIR" --define "_tmppath $WORKDIR/tmp" --define "version $VERSION" --define "releasenumber $RELEASE" tools/packages/opennms/opennms.spec
	rpmbuild -bb --define "skip_compile $OPENNMS_SKIP_COMPILE" --define "extrainfo $EXTRA_INFO" --define "extrainfo2 $EXTRA_INFO2" --define "_topdir $WORKDIR" --define "_tmppath $WORKDIR/tmp" --define "version $VERSION" --define "releasenumber $RELEASE" opennms-tools/centric-troubleticketer/src/main/rpm/opennms-plugin-ticketer-centric.spec

	if [ -n "$GPG" ]; then
		if [ `$GPG $GPGOPTS --list-keys opennms@opennms.org 2>/dev/null | grep -c '^sub'` -gt 0 ]; then
			rpm --define "_signature gpg" --define "_gpg_name opennms@opennms.org" --resign "$WORKDIR"/RPMS/noarch/*.rpm
		fi
	fi

	echo "==== OpenNMS RPM Build Finished ===="
fi

echo ""
echo "Your completed RPMs are in the $WORKDIR/RPMS/noarch directory."
