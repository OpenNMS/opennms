#!/bin/bash -e

MYDIR=`dirname $0`
TOPDIR=`cd $MYDIR; pwd`

cd "$TOPDIR"

#opennms-core_1.9.9-0.<datestamp>_all.deb
#opennms-core_<pom-version>-<release-major>.<release-minor>.<release-micro>_all.deb

function exists() {
    which "$1" >/dev/null 2>&1
}

function use_git() {
    exists git && test -d "${TOPDIR}/.git"
}

function run()
{
    if exists $1; then
        "$@"
    else
        die "Command not found: $1"
    fi
}    

function die()
{
    echo "$@" 1>&2
    exit 1
}

function tell()
{
    echo -e "$@" 1>&2
}

function usage()
{
    tell "makedeb [-h] [-a] [-s <password>] [-g <gpg-id>] [-M <major>] [-m <minor>] [-u <micro>]"
    tell "\t-h : print this help"
    tell "\t-a : assembly-only (skip the compile step)"
    tell "\t-d : disable downloading snapshots when doing an assembly-only build"
    tell "\t-s <password> : sign the deb using this password for the gpg key"
    tell "\t-g <gpg_id> : signing using this gpg_id (default: opennms@opennms.org)"
    tell "\t-M <major> : default 0 (0 means a snapshot release)"
    tell "\t-m <minor> : default <datestamp> (ignored unless major is 0)"
    tell "\t-u <micro> : default 1 (ignore unless major is 0)"
    exit 1
}

function calcMinor()
{
    if use_git; then
        git log --pretty='format:%cd' --date=short | sort -u -r | head -n 1 | sed -e 's,^Date: *,,' -e 's,-,,g'
    else
        date '+%Y%m%d'
    fi
}

function branch()
{
    if use_git; then
        run git branch | grep -E '^\*' | awk '{ print $2 }'
    else
        echo "source"
    fi
}

function commit()
{
    if use_git; then
        run git log -1 | grep -E '^commit' | cut -d' ' -f2
    else
        echo ""
    fi
}

function extraInfo()
{
    if use_git; then
        if [ "$RELEASE_MAJOR" = "0" ] ; then
            echo "This is an OpenNMS build from the $(branch) branch.  For a complete log, see:"
        else
            echo "This is an OpenNMS build from Git.  For a complete log, see:"
        fi
    else
        echo "This is an OpenNMS build from source."
    fi
}

function extraInfo2()
{
    if use_git; then
        echo "  https://github.com/OpenNMS/opennms/commit/$(commit)"
    else
        echo ""
    fi
}

function version()
{
    grep '<version>' pom.xml | \
        sed -e 's,^[^>]*>,,' -e 's,<.*$,,' -e 's,-[^-]*-SNAPSHOT$,,' -e 's,-SNAPSHOT$,,' -e 's,-testing$,,' -e 's,-,.,g' | \
        head -n 1
}

function setJavaHome()
{
    if [ -z "$JAVA_HOME" ]; then
        # hehe
        for dir in /usr/lib/jvm/java-{1.5.0,6,7,8,9}-sun; do
            if [ -x "$dir/bin/java" ]; then
                export JAVA_HOME="$dir"
                break
            fi
        done
    fi

    if [ -z $JAVA_HOME ]; then
        die "*** JAVA_HOME must be set ***"
    fi
}

function skipCompile()
{
    if $ASSEMBLY_ONLY; then echo 1; else echo 0; fi
}

function enableSnapshots()
{
    if $ENABLE_SNAPSHOTS; then echo 1; else echo 0; fi
}


function main()
{

    ASSEMBLY_ONLY=false
    ENABLE_SNAPSHOTS=true
    SIGN=false
    SIGN_PASSWORD=
    SIGN_ID=opennms@opennms.org
    BUILD_DEB=true

    RELEASE_MAJOR=0
    local RELEASE_MINOR="$(calcMinor)"
    local RELEASE_MICRO=1


    while getopts adhrs:g:M:m:u: OPT; do
        case $OPT in
            a)  ASSEMBLY_ONLY=true
                ;;
            d)  ENABLE_SNAPSHOTS=false
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
            *)  usage
                ;;
        esac
    done

    RELEASE=$RELEASE_MAJOR
    if [ "$RELEASE_MAJOR" = 0 ] ; then
        RELEASE=${RELEASE_MAJOR}.${RELEASE_MINOR}.${RELEASE_MICRO}
    fi

    EXTRA_INFO=$(extraInfo)
    EXTRA_INFO2=$(extraInfo2)
    VERSION=$(version)

    setJavaHome
    export PATH="$TOPDIR/maven/bin:$JAVA_HOME/bin:$PATH"

    if $BUILD_DEB; then
        echo "==== Building OpenNMS Debian Packages ===="
        echo
        echo "Version: " $VERSION
        echo "Release: " $RELEASE
        echo

        dch -b -v "$VERSION-$RELEASE" "${EXTRA_INFO}${EXTRA_INFO2}" || die "failed to update debian/changelog"

        # prime the local ~/.m2/repository
        if [ -d core/build ]; then
            nice ./compile.pl -N install || die "unable to build top-level POM"
            pushd core
                nice ../compile.pl -N install || die "unable to build core POM"
            popd
            pushd core/build
                nice ../../compile.pl install || die "unable to build build tools"
            popd
        fi

        if [ -f "${HOME}/.m2/settings.xml" ]; then
            export OPENNMS_SETTINGS_XML="${HOME}/.m2/settings.xml"
        fi
        export OPENNMS_SKIP_COMPILE=$(skipCompile)
        export OPENNMS_ENABLE_SNAPSHOTS=$(enableSnapshots)

        dpkg-buildpackage -p/bin/true -us -uc
    fi

    if $SIGN; then

        DEBS=$(echo "$TOPDIR"/../*.deb)
        which dpkg-sig >/dev/null 2>&1 || die "unable to locate dpkg-sig"

        for DEB in $(echo "$TOPDIR"/../*.deb); do
            run expect -c "set timeout -1; spawn dpkg-sig --sign builder -k \"$SIGN_ID\" \"$DEB\"; match_max 100000; expect \"Enter passphrase: \"; send -- \"${SIGN_PASSWORD}\r\"; expect eof" || \
            die "Debian package signing of $DEB failed for $(branch)"
        done

    fi

    echo "==== OpenNMS Debian Build Finished ===="

    echo ""
    echo "Your completed Debian packages are in the $TOPDIR/.. directory."
}

main "$@"
