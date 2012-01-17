#!/bin/bash -e

MYDIR=`dirname $0`
BINDIR=`cd "$MYDIR"; pwd`
TOPDIR=`cd "$BINDIR"/..; pwd`

if [ ! -x "${TOPDIR}/../make-installer.sh" ]; then
	echo "$TOPDIR/../make-installer.sh does not exist, not sure what to do"
	exit 1
fi

TIMESTAMP=`"${TOPDIR}"/bin/buildtool.sh nightly-jar get_stamp`
REVISION=`"${TOPDIR}"/bin/buildtool.sh nightly-jar get_revision`

# make sure things are cleaned up
git clean -fdx
git reset --hard HEAD
rm -rf "${HOME}"/.m2/repository/org/opennms

RELEASE=`cat "${TOPDIR}"/.nightly | grep -E '^repo:' | awk '{ print $2 }'`

cd "${TOPDIR}/.."
./make-installer.sh -a -m "${TIMESTAMP}" -u "${REVISION}"

# copy the source to SourceForge
"${TOPDIR}"/bin/update-sourceforge-repo.pl "${RELEASE}" standalone-opennms-installer*${TIMESTAMP}.${REVISION}.zip

"${TOPDIR}"/bin/buildtool.sh nightly-jar save
