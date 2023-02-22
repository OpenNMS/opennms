#!/usr/bin/env bash
set -e

export OPTS_MAVEN="-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false"
export OPTS_SKIP_TESTS="-DskipITs=true -Dmaven.test.skip.exec=true -DskipTests=true"
export OPTS_SKIP_TARBALL="-Dbuild.skip.tarball=true"
export OPTS_ASSEMBLIES="-Passemblies"
export OPTS_PROFILES="-Prun-expensive-tasks"

OPTS_ENABLE_SNAPSHOTS=""
OPTS_UPDATE_POLICY="-DupdatePolicy=never"
OPTS_PRODUCTION=""

TOPDIR="$(pwd)"
MYDIR="$(dirname "$0")"
MYDIR="$(cd "$MYDIR"; pwd)"

SKIP_COMPILE=0

printHelp() {
	echo "usage: $0 [-h] [-s] [-c]"
	echo ""
	echo "	-h    this help"
	echo "	-s    enable snapshot downloads"
	echo "	-c    skip compilation"
}

while getopts "chs" OPT
do
	case "$OPT" in
		h)
			printHelp
			exit 1
			;;
		s)
			OPTS_ENABLE_SNAPSHOTS="-Denable.snapshots=true"
			OPTS_UPDATE_POLICY="-DupdatePolicy=always"
			;;
		c)
			SKIP_COMPILE=1
			;;
		*)
			echo "Unknown option: $OPT"
			exit 1
			;;
	esac
done

case "${CIRCLE_BRANCH}" in
	"master"*|"release-"*|develop)
		OPTS_PRODUCTION="-Dbuild.type=production"
	;;
esac

# always build the root POM, just to be sure inherited properties/plugin/dependencies are right
echo "=== Building root POM ==="
"${TOPDIR}/compile.pl" $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY $OPTS_PRODUCTION --projects org.opennms:opennms install --builder smart --threads ${CCI_MAXCPU:-2}

COMPILE="./compile.pl"

echo ""
if [ $SKIP_COMPILE -eq 1 ]; then
	echo "=== Compiling Assemblies ==="
	OPTS_PROFILES="${OPTS_PROFILES} -PskipCompile"
	COMPILE="./assemble.pl"
else
	echo "=== Compiling Projects + Assemblies ==="
fi

echo ""
"$COMPILE" $OPTS_MAVEN $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY $OPTS_PROFILES $OPTS_PRODUCTION $OPTS_ASSEMBLIES \
	-DvaadinJavaMaxMemory=${CCI_VAADINJAVAMAXMEM:-1g} \
	-DmaxCpus=${CCI_MAXCPU:-2} \
	--projects "org.opennms.assemblies:org.opennms.assemblies.sentinel" \
	--also-make \
	install --builder smart --threads ${CCI_MAXCPU:-2}

echo "=== Finished ==="
echo "Your tarball is in:" opennms-assemblies/sentinel/target/org.opennms.assemblies.sentinel-*-sentinel.tar.gz
