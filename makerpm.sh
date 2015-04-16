#!/bin/bash -e

MYDIR=`dirname $0`
TOPDIR=`cd $MYDIR; pwd`

WORKDIR="$TOPDIR/target/rpm"

JAVA_HOME=`"$TOPDIR/bin/javahome.pl"`

export PATH="$TOPDIR/maven/bin:$JAVA_HOME/bin:$PATH"

cd "$TOPDIR"

BINARIES="expect rpmbuild rsync makensis"

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
    tell "makerpm [-h] [-a] [-s <password>] [-g <gpg-id>] [-M <major>] [-m <minor>] [-u <micro>]"
    tell "\t-h : print this help"
    tell "\t-a : assembly-only (skip the compile step)"
    tell "\t-d : disable downloading snapshots when doing an assembly-only build"
    tell "\t-s <password> : sign the rpm using this password for the gpg key"
    tell "\t-g <gpg_id> : signing using this gpg_id (default: opennms@opennms.org)"
    tell "\t-n <name> : the name of the package"
    tell "\t-x <description> : the description of the package"
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
    SIGN_ID="opennms@opennms.org"
    BUILD_RPM=true
    PACKAGE_NAME="opennms"
    PACKAGE_DESCRIPTION="OpenNMS"

    RELEASE_MAJOR=0
    local RELEASE_MINOR="$(calcMinor)"
    local RELEASE_MICRO=1


    while getopts adhrs:g:n:x:M:m:u: OPT; do
        case $OPT in
            a)  ASSEMBLY_ONLY=true
                ;;
            d)  ENABLE_SNAPSHOTS=false
                ;;
            s)  SIGN=true
                SIGN_PASSWORD="$OPTARG"
                ;;
            r)  BUILD_RPM=false
                ;;
            g)  SIGN_ID="$OPTARG"
                ;;
            n)  PACKAGE_NAME="$OPTARG"
                ;;
            x)  PACKAGE_DESCRIPTION="$OPTARG"
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

    if $BUILD_RPM; then
        echo "==== Building OpenNMS RPMs ===="
        echo
        echo "Version: " $VERSION
        echo "Release: " $RELEASE
        echo

        echo "=== Creating Working Directories ==="
        run install -d -m 755 "$WORKDIR/tmp/$PACKAGE_NAME-$VERSION-$RELEASE"
        run install -d -m 755 "$WORKDIR"/{BUILD,RPMS/{i386,i686,noarch},SOURCES,SPECS,SRPMS}

        echo "=== Copying Source to Source Directory ==="
        run rsync -aqr --exclude=.git --exclude=.svn --exclude=target --delete --delete-excluded "$TOPDIR/" "$WORKDIR/tmp/$PACKAGE_NAME-$VERSION-$RELEASE/"

        echo "=== Creating a tar.gz archive of the Source in /usr/src/redhat/SOURCES ==="
        SPECS="tools/packages/opennms/opennms.spec"
        run tar zcf "$WORKDIR/SOURCES/${PACKAGE_NAME}-source-$VERSION-$RELEASE.tar.gz" -C "$WORKDIR/tmp" "${PACKAGE_NAME}-$VERSION-$RELEASE"
        if [ "$PACKAGE_NAME" = "opennms" ]; then
                run tar zcf "$WORKDIR/SOURCES/centric-troubleticketer.tar.gz" -C "$WORKDIR/tmp/$PACKAGE_NAME-$VERSION-$RELEASE/opennms-tools" "centric-troubleticketer"
                SPECS="$SPECS opennms-tools/centric-troubleticketer/src/main/rpm/opennms-plugin-ticketer-centric.spec"
        fi

        echo "=== Building RPMs ==="
        for spec in $SPECS
        do
            run rpmbuild -bb \
                --define "skip_compile $(skipCompile)" \
                --define "enable_snapshots $(enableSnapshots)" \
                --define "extrainfo $EXTRA_INFO" \
                --define "extrainfo2 $EXTRA_INFO2" \
                --define "_topdir $WORKDIR" \
                --define "_tmppath $WORKDIR/tmp" \
                --define "version $VERSION" \
                --define "releasenumber $RELEASE" \
                --define "_name $PACKAGE_NAME" \
                --define "_descr $PACKAGE_DESCRIPTION" \
                $spec
        done
    fi

    if $SIGN; then

        RPMS=$(echo "$WORKDIR"/RPMS/noarch/*.rpm)
        #run rpmsign --define "_signature gpg" --define "_gpg_name $SIGN_ID" --resign "$RPMS"

        run expect -c "set timeout -1; spawn rpmsign --define \"_signature gpg\" --define \"_gpg_name $SIGN_ID\" --resign $RPMS; match_max 100000; expect \"Enter pass phrase: \"; send -- \"${SIGN_PASSWORD}\r\"; expect eof" || \
            die "RPM signing failed for $(branch)"

    fi

    echo "==== OpenNMS RPM Build Finished ===="

    echo ""
    echo "Your completed RPMs are in the $WORKDIR/RPMS/noarch directory."
}

for BIN in $BINARIES; do
        EXECUTABLE=`which $BIN 2>/dev/null || :`
        if [ -z "$EXECUTABLE" ] || [ ! -x "$EXECUTABLE" ]; then
                echo "ERROR: $BIN not found"
                exit 1
        fi
done

main "$@"
