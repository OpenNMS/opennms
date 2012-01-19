#!/bin/bash -e

MYDIR=`dirname $0`
BINDIR=`cd "$MYDIR"; pwd`
TOPDIR=`cd "$BINDIR"/..; pwd`

cd "$TOPDIR"

export PATH="/usr/local/bin:$PATH"

if [ -z "$YUMDIR" ]; then
	YUMDIR="/var/www/sites/opennms.org/site/yum"
fi

if [ ! -d "$YUMDIR" ]; then
	echo "YUM repository at $YUMDIR does not exist!"
	exit 1
fi

BUILDTOOL=`which buildtool.pl 2>/dev/null`
if [ $? != 0 ]; then
	echo 'Unable to locate buildtool.pl!'
	exit 1
fi

UPDATE_SF_REPO=`which update-sourceforge-repo.pl 2>/dev/null`
if [ $? != 0 ]; then
	echo 'Unable to locate update-sourceforge-repo.pl!'
	exit 1
fi

UPDATE_REPO=`which update-yum-repo.pl 2>/dev/null`
if [ $? != 0 ]; then
	echo 'Unable to locate update-yum-repo.pl!'
	exit 1
fi

GENERATE=`which generate-yum-repo-html.pl 2>/dev/null`
if [ $? != 0 ]; then
	echo 'Unable to locate generate-yum-repo-html.pl!'
	exit 1
fi

TIMESTAMP=`$BUILDTOOL nightly-rpm get_stamp`
REVISION=`$BUILDTOOL nightly-rpm get_revision`

PASSWORD=""
if [ -e "${HOME}/.signingpass" ]; then
	PASSWORD=`cat "${HOME}"/.signingpass`
else
	echo "WARNING: $HOME/.signingpass does not exist, new RPMs and repository files will not get signed!" >&2
fi
if [ ! -e "$TOPDIR/.nightly" ]; then
	echo "ERROR: $TOPDIR/.nightly does not exist. This will fail!" >&2
	exit 1
fi

# make sure things are cleaned up
git clean -fdx
git reset --hard HEAD
rm -rf "${HOME}"/.m2/repository/org/opennms

RELEASE=`cat "$TOPDIR"/.nightly | grep -E '^repo:' | awk '{ print $2 }'`

# create the RPM
./makerpm.sh -a -s "$PASSWORD" -m "$TIMESTAMP" -u "$REVISION"

# copy the source to SourceForge
echo $UPDATE_SF_REPO "$RELEASE" target/rpm/SOURCES/opennms-source*.tar.gz
$UPDATE_SF_REPO "$RELEASE" target/rpm/SOURCES/opennms-source*.tar.gz

# update the $RELEASE repo, and sync it to anything later in the hierarchy
# ./bin/update-yum-repo.pl [-g gpg_id] -s "$PASSWORD" "$RELEASE" "common" "opennms" target/rpms/RPMS/noarch/*.rpm
$UPDATE_REPO -s "$PASSWORD" "$YUMDIR" "$RELEASE" "common" "opennms" target/rpm/RPMS/noarch/*.rpm

$GENERATE "$YUMDIR"

$BUILDTOOL nightly-rpm save
