#!/usr/bin/env bash
set -e

MYDIR=`dirname $0`
TOPDIR=`cd $MYDIR; pwd`
BRANCH=""
COMMIT=""
TRUE_BIN="$(which true)"

cd "$TOPDIR"

JAVA_HOME=`"$TOPDIR/bin/javahome.pl"`

BINARIES="dch dh dh_systemd_enable dpkg-sig dpkg-buildpackage expect po2debconf"

# shellcheck disable=SC1091
source "$TOPDIR/bin/pkg-common.sh"

function usage()
{
    tell "makedeb [-h] [-a] [-s <password>] [-g <gpg-id>] [-M <major>] [-m <minor>] [-u <micro>] [package]"
    tell "\t-h : print this help"
    tell "\t-a : assembly-only (skip the compile step)"
    tell "\t-d : disable downloading snapshots when doing an assembly-only build"
    tell "\t-n : no changelog (disable auto-generation of a changelog entry)"
    tell "\t-s <password> : sign the deb using this password for the gpg key"
    tell "\t-g <gpg_id> : signing using this gpg_id (default: opennms@opennms.org)"
    tell "\t-b <branch> : the name of the branch"
    tell "\t-c <commit> : the commit revision hash from git"
    tell "\t-M <major> : default 0 (0 means a snapshot release)"
    tell "\t-m <minor> : default <datestamp> (ignored unless major is 0)"
    tell "\t-u <micro> : default 1 (ignore unless major is 0)"
    exit 1
}

for BIN in $BINARIES; do
    EXECUTABLE=`which $BIN 2>/dev/null || :`
    if [ -z "$EXECUTABLE" ] || [ ! -x "$EXECUTABLE" ]; then
        echo "ERROR: $BIN not found"
        echo "       try 'sudo apt install debhelper devscripts dpkg-dev dpkg-sig expect po-debconf'"
        exit 1
    fi
done

ASSEMBLY_ONLY=false
ENABLE_SNAPSHOTS=true
DO_CHANGELOG=true
SIGN=false
SIGN_PASSWORD=
SIGN_ID=opennms@opennms.org
BUILD_DEB=true

RELEASE_MAJOR=0
RELEASE_MINOR="$(calcMinor)"
RELEASE_MICRO=1


while getopts adhnrs:g:M:m:u:b:c: OPT; do
    case $OPT in
        a)  ASSEMBLY_ONLY=true
            ;;
        d)  ENABLE_SNAPSHOTS=false
            ;;
        n)  DO_CHANGELOG=false
            ;;
        s)  SIGN=true
            SIGN_PASSWORD="$OPTARG"
            ;;
        r)  BUILD_DEB=false
            ;;
        g)  SIGN_ID="$OPTARG"
            ;;
        M)  RELEASE_MAJOR="$OPTARG"
            ;;
        m)  RELEASE_MINOR="$OPTARG"
            ;;
        u)  RELEASE_MICRO="$OPTARG"
            ;;
        b)  BRANCH="$OPTARG"
            ;;
        c)  COMMIT="$OPTARG"
            ;;
        *)  usage
            ;;
    esac
done
shift "$((OPTIND - 1))"

PACKAGE_NAME="$1"

RELEASE=$RELEASE_MAJOR
if [ "$RELEASE_MAJOR" = 0 ] ; then
    RELEASE=${RELEASE_MAJOR}.${RELEASE_MINOR}.${RELEASE_MICRO}
fi

EXTRA_INFO=$(extraInfo)
EXTRA_INFO2=$(extraInfo2)
VERSION=$(version)
OPA_VERSION=$(opa_version)

export PATH="$TOPDIR/maven/bin:$JAVA_HOME/bin:$PATH"

export OPENNMS_SKIP_COMPILE=$(skipCompile)
export OPENNMS_ENABLE_SNAPSHOTS=$(enableSnapshots)

function build_opennms()
{
    echo "==== Building Debian OpenNMS ===="
    echo
    echo "Version: " $VERSION
    echo "Release: " $RELEASE
    echo "OPA VERSION: " $OPA_VERSION
    echo
    sed -i "s/OPA_VERSION/$OPA_VERSION/g" debian/control

    if $DO_CHANGELOG; then
        echo "- adding auto-generated changelog entry"
        dch -b -v "$VERSION-$RELEASE" "${EXTRA_INFO}${EXTRA_INFO2}" || die "failed to update debian/changelog"
    fi

    if [ -f "${HOME}/.m2/settings.xml" ]; then
        export OPENNMS_SETTINGS_XML="${HOME}/.m2/settings.xml"
    fi

    ./compile.pl -N install

    dpkg-buildpackage "-p${TRUE_BIN}" -us -uc -Zgzip
}

function build_minion()
{
    echo "==== Building Debian Minion ===="
    echo
    echo "Version: " $VERSION
    echo "Release: " $RELEASE
    echo "OPA VERSION: " $OPA_VERSION
    echo
    sed -i "s/OPA_VERSION/$OPA_VERSION/g" $TOPDIR/opennms-assemblies/minion/src/main/filtered/debian/control
    local _extra_args=()
    if [ "$OPENNMS_ENABLE_SNAPSHOTS" = "1" ]; then
        _extra_args+=("-s")
    fi
    if [ "$OPENNMS_SKIP_COMPILE" = "1" ]; then
        _extra_args+=("-c")
    fi

    tools/packages/minion/create-minion-assembly.sh "${_extra_args[@]}"

    MINION_TARBALL="$(ls -1 $TOPDIR/opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz 2>/dev/null || :)"
    if [ -z "$MINION_TARBALL" ]; then
        MINION_TARBALL="$(ls -1 "~/.m2/repository/org/opennms/assemblies/org.opennms.assemblies.minion/${VERSION}"/org.opennms.assemblies.minion-*-minion.tar.gz 2>/dev/null || :)"
    fi

    mkdir -p target
    pushd target >/dev/null 2>&1
        cp "$MINION_TARBALL" "opennms-minion_${VERSION}.orig.tar.gz"
        tar -xzf "opennms-minion_${VERSION}.orig.tar.gz" || die "could not unpack opennms-minion tarball"
        DIRNAME=$(echo "$MINION_TARBALL" | sed -e 's,^.*org.opennms.assemblies.,,' -e 's,-minion.tar.gz,,')
        mv "${DIRNAME}" "opennms-minion-${VERSION}"
        pushd "opennms-minion-${VERSION}" >/dev/null 2>&1
            if $DO_CHANGELOG; then
                echo "- adding auto-generated changelog entry"
                dch -b -v "${VERSION}-${RELEASE}" "${EXTRA_INFO}${EXTRA_INFO2}" || die "failed to update minion debian/changelog"
            fi
            dpkg-buildpackage "-p${TRUE_BIN}" -us -uc -Zgzip
        popd >/dev/null 2>&1

        # move the build artifacts to the root
        mv *.deb *.orig.tar.gz *.changes *.dsc "$TOPDIR/.."
    popd >/dev/null 2>&1
}

function build_sentinel() {
    echo "==== Building Debian OpenNMS ===="
    echo
    echo "Version: " $VERSION
    echo "Release: " $RELEASE
    echo "OPA VERSION: " $OPA_VERSION
    echo
    sed -i "s/OPA_VERSION/$OPA_VERSION/g" $TOPDIR/opennms-assemblies/sentinel/src/main/filtered/debian/control

    local _extra_args=()
    if [ "$OPENNMS_ENABLE_SNAPSHOTS" = "1" ]; then
        _extra_args+=("-s")
    fi
    if [ "$OPENNMS_SKIP_COMPILE" = "1" ]; then
        _extra_args+=("-c")
    fi

    tools/packages/sentinel/create-sentinel-assembly.sh "${_extra_args[@]}"

    SENTINEL_TARBALL="$(ls -1 $TOPDIR/opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-*-sentinel.tar.gz 2>/dev/null || :)"
    if [ -z "$SENTINEL_TARBALL" ]; then
        SENTINEL_TARBALL="$(ls -1 "~/.m2/repository/org/opennms/assemblies/org.opennms.assemblies.sentinel/${VERSION}"/org.opennms.assemblies.sentinel-*-sentinel.tar.gz 2>/dev/null || :)"
    fi

    mkdir -p target
    pushd target >/dev/null 2>&1
        cp "$SENTINEL_TARBALL" "opennms-sentinel_${VERSION}.orig.tar.gz"
        tar -xzf "opennms-sentinel_${VERSION}.orig.tar.gz" || die "could not unpack opennms-sentinel tarball"
        DIRNAME=$(echo "$SENTINEL_TARBALL" | sed -e 's,^.*org.opennms.assemblies.,,' -e 's,-sentinel.tar.gz,,')
        mv "${DIRNAME}" "opennms-sentinel-${VERSION}"
        pushd "opennms-sentinel-${VERSION}" >/dev/null 2>&1
            dch -b -v "${VERSION}-${RELEASE}" "${EXTRA_INFO}${EXTRA_INFO2}" || die "failed to update sentinel debian/changelog"
            dpkg-buildpackage "-p${TRUE_BIN}" -us -uc -Zgzip
        popd >/dev/null 2>&1

        # move the build artifacts to the root
        mv *.deb *.orig.tar.gz *.changes *.dsc "$TOPDIR/.."
    popd >/dev/null 2>&1
}

if $BUILD_DEB; then

    if [ "$PACKAGE_NAME" = "opennms" ] || [ -z "$PACKAGE_NAME" ]; then
        build_opennms
    fi
    if [ "$PACKAGE_NAME" = "minion" ] || [ -z "$PACKAGE_NAME" ]; then
        build_minion
    fi
    if [ "$PACKAGE_NAME" = "sentinel" ] || [ -z "$PACKAGE_NAME" ]; then
        build_sentinel
    fi

fi

if $SIGN; then

    DEBS=$(echo "$TOPDIR"/../*.deb)
    which dpkg-sig >/dev/null 2>&1 || die "unable to locate dpkg-sig"

    for DEB in $(echo "$TOPDIR"/../*.deb); do
        run expect -c "set timeout -1; spawn dpkg-sig --sign builder -k \"$SIGN_ID\" \"$DEB\"; match_max 100000; expect \"Enter passphrase: \"; send -- \"${SIGN_PASSWORD}\r\"; expect eof" 2>/dev/null || \
        die "Debian package signing of $DEB failed for $(branch)"
    done

fi

echo "==== OpenNMS Debian Build Finished ===="

echo ""
echo "Your completed Debian packages are in the $TOPDIR/.. directory."
