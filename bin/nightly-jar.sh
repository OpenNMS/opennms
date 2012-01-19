#!/bin/bash -e

MYDIR=`dirname $0`
BINDIR=`cd "$MYDIR"; pwd`
TOPDIR=`cd "$BINDIR"/..; pwd`

BUILDTOOL=`which buildtool.pl 2>/dev/null`
if [ $? != 0 ]; then
	echo 'Unable to locate buildtool.pl!'
	exit 1
fi

UPDATE_REPO=`which update-sourceforge-repo.pl 2>/dev/null`
if [ $? != 0 ]; then
	echo 'Unable to locate update-sourceforge-repo.pl!'
	exit 1
fi

if [ ! -x "${TOPDIR}/../make-installer.sh" ]; then
	echo "$TOPDIR/../make-installer.sh does not exist, not sure what to do"
	exit 1
fi

TIMESTAMP=`$BUILDTOOL nightly-jar get_stamp`
REVISION=`$BUILDTOOL nightly-jar get_revision`

# make sure things are cleaned up
git clean -fdx
git reset --hard HEAD
rm -rf "${HOME}"/.m2/repository/org/opennms

RELEASE=`cat "${TOPDIR}"/.nightly | grep -E '^repo:' | awk '{ print $2 }'`

cd "${TOPDIR}/.."
./make-installer.sh -a -m "${TIMESTAMP}" -u "${REVISION}"

# copy the source to SourceForge
echo $UPDATE_REPO "${RELEASE}" standalone-opennms-installer*${TIMESTAMP}.${REVISION}.zip
$UPDATE_REPO "${RELEASE}" standalone-opennms-installer*${TIMESTAMP}.${REVISION}.zip

$BUILDTOOL nightly-jar save
