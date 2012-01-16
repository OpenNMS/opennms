#!/bin/bash -e

MYDIR=`dirname $0`
BINDIR=`cd "$MYDIR"; pwd`
TOPDIR=`cd "$BINDIR"/..; pwd`

cd "$TOPDIR"

if [ -z "$APTDIR" ]; then
	APTDIR="/var/ftp/pub/releases/opennms/debian"
fi

if [ ! -d "$APTDIR" ]; then
	echo "APT repository at $APTDIR does not exist!"
	exit 1
fi

TIMESTAMP=`bin/buildtool.sh get_stamp`
REVISION=`bin/buildtool.sh get_revision`

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

RELEASE=`cat "$TOPDIR"/.nightly | grep -E '^repo:' | awk '{ print $2 }'`

# create the package 
./makedeb.sh -a -s "$PASSWORD" -m "$TIMESTAMP" -u "$REVISION"

# update the $RELEASE repo, and sync it to anything later in the hierarchy
./bin/update-apt-repo.pl -s "$PASSWORD" "$APTDIR" "$RELEASE" ../*.${TIMESTAMP}.${REVISION}_all.deb

bin/buildtool.sh save
