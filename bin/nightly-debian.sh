#!/bin/bash -e

MYDIR=`dirname $0`
BINDIR=`cd "$MYDIR"; pwd`
TOPDIR=`cd "$BINDIR"/..; pwd`

export PATH="/usr/local/bin:$PATH"

cd "$TOPDIR"

if [ -z "$APTDIR" ]; then
	APTDIR="/var/ftp/pub/releases/opennms/debian"
fi

if [ ! -d "$APTDIR" ]; then
	echo "APT repository at $APTDIR does not exist!"
	exit 1
fi

BUILDTOOL=`which buildtool.pl 2>/dev/null`
if [ $? != 0 ]; then
	echo 'Unable to locate buildtool.pl!'
	exit 1
fi

UPDATE_REPO=`which update-apt-repo.pl 2>/dev/null`
if [ $? != 0 ]; then
	echo 'Unable to locate update-apt-repo.pl!'
	exit 1
fi

TIMESTAMP=`$BUILDTOOL nightly-debian get_stamp`
REVISION=`$BUILDTOOL nightly-debian get_revision`

PASSWORD=""
if [ -e "${HOME}/.signingpass" ]; then
	PASSWORD=`cat "${HOME}"/.signingpass`
else
	echo "WARNING: $HOME/.signingpass does not exist, new packages and repository files will not get signed!" >&2
fi
if [ ! -e "$TOPDIR/.nightly" ]; then
	echo "ERROR: $TOPDIR/.nightly does not exist. This will fail!" >&2
	exit 1
fi

# make sure things are cleaned up
git clean -fdx
git reset --hard HEAD
rm -rf "${HOME}"/.m2/repository/org/opennms
rm -rf "${TOPDIR}"/../*opennms*.{changes,deb,dsc,tar.gz}

VERSION=`grep '<version>' pom.xml | head -n 1 | sed -e 's,^.*<version>,,' -e 's,<.version>.*$,,' | cut -d. -f1-2`
RELEASE=`cat "$TOPDIR"/.nightly | grep -E '^repo:' | awk '{ print $2 }'`

# create the package 
./makedeb.sh -a -s "$PASSWORD" -m "$TIMESTAMP" -u "$REVISION"

# update the $RELEASE repo, and sync it to anything later in the hierarchy
$UPDATE_REPO -s "$PASSWORD" "$APTDIR" "nightly-${VERSION}" "${TOPDIR}"/../*opennms*_${VERSION}*.deb
find ../*opennms*.{changes,deb,dsc,tar.gz} -maxdepth 0 -type f -exec rm -rf {} \;

$BUILDTOOL nightly-debian save
