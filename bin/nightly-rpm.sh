#!/bin/bash -e

MYDIR=`dirname $0`
BINDIR=`cd "$MYDIR"; pwd`
TOPDIR=`cd "$BINDIR"/..; pwd`

cd "$TOPDIR"

if [ -z "$YUMDIR" ]; then
	YUMDIR="/var/www/sites/opennms.org/site/yum"
fi

if [ ! -d "$YUMDIR" ]; then
	echo "YUM repository at $YUMDIR does not exist!"
	exit 1
fi

TIMESTAMP=`bin/buildtool.sh nightly-rpm get_stamp`
REVISION=`bin/buildtool.sh nightly-rpm get_revision`

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
./bin/update-sourceforge-repo.pl "$RELEASE" target/rpm/SOURCES/opennms-source*.tar.gz

# update the $RELEASE repo, and sync it to anything later in the hierarchy
# ./bin/update-yum-repo.pl [-g gpg_id] -s "$PASSWORD" "$RELEASE" "common" "opennms" target/rpms/RPMS/noarch/*.rpm
./bin/update-yum-repo.pl -s "$PASSWORD" "$YUMDIR" "$RELEASE" "common" "opennms" target/rpm/RPMS/noarch/*.rpm

./bin/generate-yum-repo-html.pl "$YUMDIR"

bin/buildtool.sh nightly-rpm save
