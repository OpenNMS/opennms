#!/bin/bash -e

MYDIR=`dirname $0`
TOPDIR=`cd $MYDIR; pwd`

WORKDIR="$TOPDIR/target/rpm"

export PATH="$TOPDIR/maven/bin:$JAVA_HOME/bin:$PATH"

cd "$TOPDIR"

#opennms-core-1.9.9-0.<datestamp>.rpm
#opennms-core-<pom-version>-<release-major>.<release-minor>.<release-micro>

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
    tell "\t-a : assembly only (skip the compile step)"
    tell "\t-s <password> : sign the rpm using this password for the gpg key"
    tell "\t-g <gpg_id> : signing using this gpg_id (default: opennms@opennms.org)"
    tell "\t-M <major> : default 0 (0 means a snapshot release)"
    tell "\t-m <minor> : default <datestamp> (ignored unless major is 0)"
    tell "\t-u <micro> : default 1 (ignore unless major is 0)"
    exit 1
}

function calcMinor()
{
    if use_git; then
	git log --pretty='format:%cd' --date=short -1 | head -n 1 | sed -e 's,^Date: *,,' -e 's,-,,g'
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
        echo "  http://opennms.git.sourceforge.net/git/gitweb.cgi?p=opennms/opennms;a=shortlog;h=$(commit)"
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
	for dir in /usr/java/jdk1.{5,6,7,8,9}*; do
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


function main()
{

    ASSEMBLY_ONLY=false
    SIGN=false
    SIGN_PASSWORD=
    SIGN_ID=opennms@opennms.org
    BUILD_RPM=true

    RELEASE_MAJOR=0
    local RELEASE_MINOR="$(calcMinor)"
    local RELEASE_MICRO=1


    while getopts ahrs:g:M:m:u: OPT; do
	case $OPT in
	    a)  ASSEMBLY_ONLY=true
		;;
	    s)  SIGN=true
		SIGN_PASSWORD="$OPTARG"
		;;
            r)  BUILD_RPM=false
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

    if $BUILD_RPM; then
        echo "==== Building OpenNMS RPMs ===="
        echo
        echo "Version: " $VERSION
        echo "Release: " $RELEASE
        echo

        echo "=== Creating Working Directories ==="
        run install -d -m 755 "$WORKDIR/tmp/opennms-$VERSION-$RELEASE"
        run install -d -m 755 "$WORKDIR"/{BUILD,RPMS/{i386,i686,noarch},SOURCES,SPECS,SRPMS}

        echo "=== Copying Source to Source Directory ==="
        run rsync -aqr --exclude=.git --exclude=.svn --exclude=target --delete --delete-excluded "$TOPDIR/" "$WORKDIR/tmp/opennms-$VERSION-$RELEASE/"

        echo "=== Creating a tar.gz archive of the Source in /usr/src/redhat/SOURCES ==="
        run tar zcf "$WORKDIR/SOURCES/opennms-source-$VERSION-$RELEASE.tar.gz" -C "$WORKDIR/tmp" "opennms-$VERSION-$RELEASE"
        run tar zcf "$WORKDIR/SOURCES/centric-troubleticketer.tar.gz" -C "$WORKDIR/tmp/opennms-$VERSION-$RELEASE/opennms-tools" "centric-troubleticketer"

        echo "=== Building RPMs ==="
        for spec in tools/packages/opennms/opennms.spec opennms-tools/centric-troubleticketer/src/main/rpm/opennms-plugin-ticketer-centric.spec
        do
    	run rpmbuild -bb \
    	    --define "skip_compile $(skipCompile)" \
    	    --define "extrainfo $EXTRA_INFO" \
    	    --define "extrainfo2 $EXTRA_INFO2" \
    	    --define "_topdir $WORKDIR" \
    	    --define "_tmppath $WORKDIR/tmp" \
    	    --define "version $VERSION" \
    	    --define "releasenumber $RELEASE" \
    	    $spec
        done
    fi

    if $SIGN; then

	RPMS=$(echo "$WORKDIR"/RPMS/noarch/*.rpm)
	#run rpm --define "_signature gpg" --define "_gpg_name $SIGN_ID" --resign "$RPMS"

	run expect -c "set timeout -1; spawn rpm --define \"_signature gpg\" --define \"_gpg_name $SIGN_ID\" --resign $RPMS; match_max 100000; expect \"Enter pass phrase: \"; send -- \"${SIGN_PASSWORD}\r\"; expect eof" || \
	    die "RPM signing failed for $(branch)"

    fi

    echo "==== OpenNMS RPM Build Finished ===="

    echo ""
    echo "Your completed RPMs are in the $WORKDIR/RPMS/noarch directory."
}

main "$@"
