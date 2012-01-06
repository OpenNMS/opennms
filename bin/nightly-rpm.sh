#!/bin/bash -e

MYDIR=`dirname $0`
TOPDIR=`cd $MYDIR; pwd`

cd "$TOPDIR"/..

if [ -z "$YUMDIR" ]; then
	YUMDIR="/opt/yum"
fi

if [ ! -d "$YUMDIR" ]; then
	echo "YUM repository at $YUMDIR does not exist!"
	exit 1
fi

TIMESTAMP=`bin/buildtool.sh get_stamp`
REVISION=`bin/buildtool.sh get_revision`

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

RELEASE=`cat "$TOPDIR"/.nightly | grep -E '^repo:' | awk '{ print $2 }'`

# create the RPM
./makerpm.sh -a -s "$PASSWORD" -m "$TIMESTAMP" -u "$REVISION"

# update the $RELEASE repo, and sync it to anything later in the hierarchy
# ./bin/update-repo.pl [-g gpg_id] -s "$PASSWORD" "$RELEASE" "common" "opennms" target/rpms/RPMS/noarch/*.rpm
./bin/update-repo.pl -s "$PASSWORD" "$YUMDIR" "$RELEASE" "common" "opennms" target/rpm/RPMS/noarch/*.rpm

./bin/generate-repo-html.pl "$YUMDIR"

bin/buildtool.sh save
