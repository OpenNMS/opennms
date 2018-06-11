#!/bin/bash -e

export OPTS_MAVEN="-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false"
export OPTS_SKIP_TESTS="-DskipITs=true -Dmaven.test.skip.exec=true"
export OPTS_SKIP_TARBALL="-Dbuild.skip.tarball=true"
export OPTS_ASSEMBLIES="-Passemblies"
export OPTS_PROFILES="-Prun-expensive-tasks"

OPTS_ENABLE_SNAPSHOTS=""
OPTS_UPDATE_POLICY=""

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

# always build the root POM, just to be sure inherited properties/plugin/dependencies are right
echo "=== Building checkstyle & root POM ==="
"${TOPDIR}/compile.pl" $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY --projects org.opennms:org.opennms.checkstyle,org.opennms:opennms install

get_maven_artifact() {
	xsltproc "${MYDIR}/get-id.xsl" "$1/pom.xml"
}

get_maven_artifacts() {
	pushd "$1" >/dev/null 2>&1
		find . -name pom.xml | while read -r POM; do
			DIR="$(dirname "$POM")"
			get_maven_artifact "$DIR"
		done | tr '\n' ',' | sed -e 's/,$//'
	popd >/dev/null 2>&1
}

COMPILE_PROJECTS="$(get_maven_artifacts features/minion)"
ASSEMBLY_PROJECTS="org.opennms.assemblies:org.opennms.assemblies.minion"

echo ""
PROJECTS=""
if [ $SKIP_COMPILE -eq 1 ]; then
	echo "=== Compiling Assemblies ==="
	PROJECTS="${ASSEMBLY_PROJECTS}"
else
	echo "=== Compiling Projects + Assemblies ==="
	PROJECTS="${COMPILE_PROJECTS},${ASSEMBLY_PROJECTS}"
fi

echo "Projects: ${PROJECTS}"
echo ""
./compile.pl $OPTS_MAVEN $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY $OPTS_PROFILES $OPTS_ASSEMBLIES \
	--projects "${PROJECTS}" \
	--also-make \
	install

echo "=== Finished ==="
echo "Your tarball is in:" opennms-assemblies/minion/target/org.opennms.assemblies.minion-*-minion.tar.gz
