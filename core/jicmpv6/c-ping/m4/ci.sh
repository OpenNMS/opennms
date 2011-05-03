#!/bin/bash -v

usage() {
	echo "usage: $0 <bittedness> <SVN revision> <build number>"
	echo ""
}

die() {
	usage
	echo "failed: $@"
	exit 1
}

BITS="$1"; shift
REVISION="$1"; shift
BUILDNUM="$1"; shift

[ -n "$BITS"     ] || die "you must specify bittedness"
[ -n "$REVISION" ] || die "you must specify the SVN revision"
[ -n "$BUILDNUM" ] || die "you must specify the build number"

RPM_ARCH=""
RPM_ARGS=""
HOST_ARGS=""

OS=`uname -s | tr 'A-Z' 'a-z'`
MACHINE=`uname -m`

if [ "$OS" = "linux" ]; then
	if [ "$BITS" = "64" ]; then
		# HOST_ARGS="--host=x86_64-$OS"
		HOST_ARGS="--target=x86_64-$OS"
		RPM_ARCH="--with-rpm-arch=x86_64"
	else
		# HOST_ARGS="--host=i386-$OS"
		HOST_ARGS="--target=i386-$OS"
		RPM_ARCH="--with-rpm-arch=i386"
	fi
fi

sh m4/autogen.sh || die "failed to autogen"
make distclean || :
./configure --prefix=/usr --with-java="${JAVA_HOME}" --with-jvm-arch=$BITS "$RPM_ARCH" "$RPM_ARGS" "$HOST_ARGS" || die "failed to configure"
make dist || die "unable to make dist"
if [ -x /bin/rpm ]; then
	make rpm RELEASE="0.${REVISION}.${BUILDNUM}" || die "failed to make an RPM"
else
	make || die "failed to run make"
	make install DESTDIR=`pwd`/dist || die "failed to run make install"
	rm -rf dist
fi
