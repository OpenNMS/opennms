#!/usr/bin/env bash
set -e

MYDIR=`dirname $0`
TOPDIR=`cd $MYDIR; pwd`

WORKDIR="$TOPDIR/target/rpm"
BRANCH=""
COMMIT=""

JAVA_HOME=`"$TOPDIR/bin/javahome.pl"`

export PATH="$TOPDIR/maven/bin:$JAVA_HOME/bin:$PATH"

cd "$TOPDIR"

BINARIES="expect rpmbuild rsync"

# shellcheck disable=SC1091
source "$TOPDIR/bin/pkg-common.sh"

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
    tell "\t-b <branch> : the name of the branch"
    tell "\t-c <commit> : the commit revision hash from git"
    tell "\t-S <specfile> : the path to the rpm specification file"
    tell "\t-M <major> : default 0 (0 means a snapshot release)"
    tell "\t-m <minor> : default <datestamp> (ignored unless major is 0)"
    tell "\t-u <micro> : default 1 (ignore unless major is 0)"
    exit 1
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


    while getopts adhrs:g:n:x:M:m:u:b:c:S: OPT; do
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
            b)  BRANCH="$OPTARG"
                ;;
            c)  COMMIT="$OPTARG"
                ;;
            S)  SPECS="$OPTARG"
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
    OPA_VERSION=$(opa_version)
    

    if $BUILD_RPM; then
        if [ "$SPECS" == "" ]; then
            SPECS="tools/packages/opennms/opennms.spec tools/packages/minion/minion.spec tools/packages/sentinel/sentinel.spec"
        else
            for spec in $SPECS
            do
                if [ ! -f "$spec" ]; then
                    die "Spec not found: $spec"
                fi
            done
        fi

        echo "==== Building OpenNMS RPMs ===="
        echo
        echo "Version: " $VERSION
        echo "Release: " $RELEASE
        echo "Specs  : " $SPECS
        echo "OPA VERSION: " $OPA_VERSION
        echo

        echo "=== Creating Working Directories ==="
        run install -d -m 755 "$WORKDIR/tmp/$PACKAGE_NAME-$VERSION-$RELEASE"
        run install -d -m 755 "$WORKDIR"/{BUILD,RPMS/{i386,i686,noarch},SOURCES,SPECS,SRPMS}

        echo "=== Copying Source to Source Directory ==="
        run rsync -aqr --exclude=.git --exclude=.svn --exclude=target --delete --delete-excluded "$TOPDIR/" "$WORKDIR/tmp/$PACKAGE_NAME-$VERSION-$RELEASE/"
        if $ASSEMBLY_ONLY; then
            # Include any existing target/ directory from the core/web-assets project so that webpack does not need to run again
            run rsync -aqr --delete --delete-excluded "$TOPDIR/core/web-assets/" "$WORKDIR/tmp/$PACKAGE_NAME-$VERSION-$RELEASE/core/web-assets/"
        fi

        echo "=== Creating a tar.gz Archive of the Source in $WORKDIR/tmp/$PACKAGE_NAME-$VERSION-$RELEASE ==="
        run tar zcf "$WORKDIR/SOURCES/${PACKAGE_NAME}-source-$VERSION-$RELEASE.tar.gz" -C "$WORKDIR/tmp" "${PACKAGE_NAME}-$VERSION-$RELEASE"

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
                --define "opa_version $OPA_VERSION" \
                $spec || die "failed to build $spec"
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
