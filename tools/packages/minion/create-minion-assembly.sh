#!/bin/sh -e

export OPTS_MAVEN="-Daether.connector.basic.threads=1 -Daether.connector.resumeDownloads=false"
export OPTS_SKIP_TESTS="-DskipITs=true -Dmaven.test.skip.exec=true"
export OPTS_SKIP_TARBALL="-Dbuild.skip.tarball=true"
export OPTS_ASSEMBLIES="-Passemblies"
export OPTS_PROFILES="-Prun-expensive-tasks"
export COMPILE_PROJECTS="org.opennms.features.minion.container:karaf,org.opennms.features.minion:core-repository,org.opennms.features.minion:repository,org.opennms.features.minion:container-parent,org.opennms.features.minion:core-parent,org.opennms.features.minion:org.opennms.features.minion.heartbeat,org.opennms.features.minion:repository,org.opennms.features.minion:shell"
export ASSEMBLY_PROJECTS=":org.opennms.assemblies.minion"

OPTS_ENABLE_SNAPSHOTS=""
OPTS_UPDATE_POLICY=""

SKIP_COMPILE=0

printHelp() {
	echo "usage: $0 [-h] [-s] [-c]"
	echo ""
	echo "	-h    this help"
	echo "	-s    enable snapshot downloads"
	echo "	-c    skip compilation"
}

while getopts "hs" OPT
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
echo "=== Building root POM ==="
./compile.pl -N $OPTS_SKIP_TESTS $OPTS_SKIP_TARBALL $OPTS_ENABLE_SNAPSHOTS $OPTS_UPDATE_POLICY install

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
