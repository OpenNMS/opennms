#!/usr/bin/env bash
set -e

export OPTS_MAVEN="-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false"
export OPTS_SKIP_TESTS="-DskipITs=true -Dmaven.test.skip.exec=true -DskipTests=true"
export OPTS_SKIP_TARBALL="-Dbuild.skip.tarball=true"
export OPTS_ASSEMBLIES="-Passemblies"
export OPTS_PROFILES="-Prun-expensive-tasks"

OPTS_ENABLE_SNAPSHOTS=""
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

if [ "${OPENNMS_REUSE_ASSEMBLY:-0}" = "1" ]; then
	EXISTING_TARBALL="$(ls -1 "${TOPDIR}"/opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz 2>/dev/null | head -n 1 || :)"
	if [ -n "$EXISTING_TARBALL" ]; then
		echo "=== Reusing pre-built minion assembly: $EXISTING_TARBALL ==="
		exit 0
	fi
	echo "OPENNMS_REUSE_ASSEMBLY=1 but no pre-built minion tarball found under opennms-assemblies/minion/target; building it now."
fi

# shellcheck disable=SC1091
source "${TOPDIR}/bin/pkg-common.sh"
OPA_VERSION="$(opa_version)"
echo "OPA VERSION: ${OPA_VERSION}"
if [ -z "$OPA_VERSION" ]; then
	echo "ERROR: opa_version() returned an empty string; refusing to stamp debian/control" >&2
	exit 1
fi
sed -i "s/OPA_VERSION/${OPA_VERSION}/g" "${TOPDIR}/opennms-assemblies/minion/src/main/filtered/debian/control"
if grep -q 'OPA_VERSION' "${TOPDIR}/opennms-assemblies/minion/src/main/filtered/debian/control"; then
	echo "ERROR: OPA_VERSION placeholder still present in debian/control after substitution" >&2
	exit 1
fi

# always build the root POM, just to be sure inherited properties/plugin/dependencies are right
echo "=== Building root POM ==="
"${TOPDIR}/compile.pl" \
	$OPTS_SKIP_TESTS \
	$OPTS_SKIP_TARBALL \
	$OPTS_ENABLE_SNAPSHOTS \
	$OPTS_PRODUCTION \
	--projects org.opennms:opennms \
	--builder smart \
	--threads ${CCI_MAXCPU:-2} \
	install

COMPILE="./compile.pl"

echo ""
PROJECTS=""
if [ $SKIP_COMPILE -eq 1 ]; then
	echo "=== Compiling Assemblies ==="
	OPTS_PROFILES="${OPTS_PROFILES} -PskipCompile"
	COMPILE="./assemble.pl"
else
	echo "=== Compiling Projects + Assemblies ==="
fi

echo ""
"${COMPILE}" \
	$OPTS_MAVEN \
	$OPTS_SKIP_TESTS \
	$OPTS_SKIP_TARBALL \
	$OPTS_ENABLE_SNAPSHOTS \
	$OPTS_PROFILES \
	$OPTS_PRODUCTION \
	$OPTS_ASSEMBLIES \
	-DvaadinJavaMaxMemory=${CCI_VAADINJAVAMAXMEM:-1g} \
	-DmaxCpus=${CCI_MAXCPU:-2} \
	--projects "org.opennms.assemblies:org.opennms.assemblies.minion" \
	--also-make \
	--builder smart \
	--threads ${CCI_MAXCPU:-2} \
	install

echo "=== Finished ==="
echo "Your tarball is in:" opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz
