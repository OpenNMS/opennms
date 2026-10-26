#!/usr/bin/env bash
# ----------------------------------------------------------------------
# Setup dependencies for local development
# ----------------------------------------------------------------------
set -euo pipefail          # Fail fast & catch unset vars

# ----------------------------------------------------------------------
# Detect OS & set OS‑specific parameters
# ----------------------------------------------------------------------
OS="$(uname -s)"
case "$OS" in
  Linux*)   OS_NAME="Linux" ;;
  Darwin*)  OS_NAME="macOS" ;;
  *)        echo "Unsupported OS: $OS" && exit 1 ;;
esac
echo "Detected OS: $OS_NAME"


# ------------------------------------------------------
# Options
# ------------------------------------------------------
ROOT="$(pwd)"

RELEASE="$(.circleci/scripts/pom2version.sh pom.xml)"


usage(){
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  --help                  Show this help message"
    echo "  --disable-jrrd2         Disable building jrrd2 library"
    echo "  --skip-cleanup          Skip cleanup of previous build artifacts"
    echo "  --enable-tests          Enable running tests during build"
    echo "  --run-opennms-foreground    Start OpenNMS in foreground after build"
    exit 1
}

# Default options
ENABLE_TESTS="no"
SKIP_CLEANUP="no"
DISABLE_JRRD2=${DISABLE_JRRD2:-"false"}
RUN_OPENNMS_FOREGROUND="no"

while [[ $# -gt 0 ]]; do
    case $1 in
        --help)
            usage
            ;;
        --disable-jrrd2 )
            DISABLE_JRRD2="true"
            shift
            ;;
        --enable-tests )
            ENABLE_TESTS="yes"
            shift
            ;;
        --skip-cleanup )
            SKIP_CLEANUP="yes"
            shift
            ;;
        --run-opennms-foreground )
            RUN_OPENNMS_FOREGROUND="yes"
            shift
            ;;
        --all)
            DISABLE_JRRD2="false"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done

# ------------------------------------------------------
# Pre-build setup
# ------------------------------------------------------

echo "Checking ulimit..."
ULIMIT_OUTPUT=$(ulimit -n || true)
echo "Current ulimit -n: $ULIMIT_OUTPUT"
if [[ "$ULIMIT_OUTPUT" == "unlimited" || "$ULIMIT_OUTPUT" -gt 20000 ]]; then
    echo "ulimit is sufficient."
else
    echo "Setting ulimit to 20000"
    ulimit -n 20000 || echo "Failed to set ulimit. You may need to run this script with elevated permissions."
fi


# run dependency setup
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

select_jdk || exit 1

if [[ "$DISABLE_JRRD2" != "true" ]]; then
   detect_jrrd2_location
   if [[ -z "$JRRD_JAR" ]] || [[ ! -f "$JRRD_JAR" ]] || [[ -z "$JRRD_LIB" ]] || [[ ! -f "$JRRD_LIB" ]]; then
     # Build JRRD2 from source then run the rest of script
     "$SCRIPT_DIR/dependencies.sh" --install-jrrd2-from-source || true
     detect_jrrd2_location  
   fi
fi

detect_postgres_installed

if [[ ${POSTGRES_VERSION:-} == "unknown" ]]; then
    echo "PostgreSQL not detected. You may deploy PostgreSQL using Docker by running:"
    echo "  $SCRIPT_DIR/dependencies.sh --deploy-postgresql"
    exit 1
fi

# ------------------------------------------------------
# Build OpenNMS
# ------------------------------------------------------

if [[ -f "$ROOT/target/opennms/bin/opennms" ]]; then
    echo "OpenNMS already built. Lets stop existing."
    ./target/opennms/bin/opennms stop || true
fi

BUILD_START=$(date +%s)

if [[ "$SKIP_CLEANUP" == "yes" ]]; then
    echo "Skipping cleanup of previous build artifacts."
else
    echo "Cleaning previous build artifacts..."
    # Faster than clean.pl, which starts Maven on every module. node_modules stays, so pnpm does not install all packages again.
    find . -name node_modules -prune -o -name target -type d -prune -exec rm -rf {} +
    rm -rf ./ui/src/main/dist ./ui/src/menu/dist-menu
fi

# "-p dir" writes the assembly to target/opennms-$RELEASE, so there is no tarball to unpack.
# Local builds do not use the source tarball or the core and optional tarballs.
ASSEMBLE_ARGS=(-p dir -Dbuild.skip.tarball=true -Dbuild.skip.coretar=true)
if [[ "$ENABLE_TESTS" == "yes" ]]; then
    echo "Compiling & assembling (with tests)..."
    ./compile.pl -Dbuild.skip.tarball=true && ./assemble.pl "${ASSEMBLE_ARGS[@]}"
else
    echo "Compiling & assembling (skip tests)..."
    # Parallel threads need a larger heap. CI runs checkstyle and the enforcer rules.
    CPUS=$(getconf _NPROCESSORS_ONLN 2>/dev/null || echo 4)
    export MAVEN_OPTS=${MAVEN_OPTS:-"-Xmx8g -XX:ReservedCodeCacheSize=1g -XX:+TieredCompilation"}
    COMPILE_ARGS=(-DskipTests=true -Dbuild.skip.tarball=true -Ddisable.checkstyle=true -Denforcer.skip=true \
        --threads "$(( CPUS < 8 ? CPUS : 8 ))" install)
    # The Karaf assemblies package bundles from features files, and Maven does not use features files
    # to order the build. In a parallel build, an assembly can start before its bundles exist.
    # Build the assemblies in a second run, when all bundles are in the local repository.
    KARAF_ASSEMBLIES="container/karaf,container/shared,features/container/minion,features/container/sentinel,features/minion/core/repository,features/minion/repository"
    ./compile.pl "${COMPILE_ARGS[@]}" --projects "!${KARAF_ASSEMBLIES//,/,!}" \
        && ./compile.pl "${COMPILE_ARGS[@]}" --projects "$KARAF_ASSEMBLIES" \
        && ./assemble.pl -DskipTests=true "${ASSEMBLE_ARGS[@]}"
fi

echo "Preparing symlink for OpenNMS release $RELEASE"
ln -sfn "$ROOT/target/opennms-$RELEASE" "$ROOT/target/opennms"

# Set runtime user
echo "RUNAS=$(id -u -n)" > "$ROOT/target/opennms/etc/opennms.conf"

# If jrrd2 is installed, setup config
if [[ "$DISABLE_JRRD2" != "true" ]]; then 
    # Figure out where rrdtool is installed
    RRD_TOOL_PATH=$(which rrdtool || echo "/usr/local/bin/rrdtool")
    echo "Detected rrdtool at: $RRD_TOOL_PATH"

    echo "Configuring OpenNMS to use jrrd2 library..."
    echo "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy
org.opennms.rrd.interfaceJar=$JRRD_JAR
opennms.library.jrrd2=$JRRD_LIB
org.opennms.web.graphs.engine=rrdtool
rrd.binary=$RRD_TOOL_PATH
" > "$ROOT/target/opennms/etc/opennms.properties.d/timeseries.properties"
fi


# Check if POSTGRES_PASSWORD is set, if not set a default value
if [[ -z "${POSTGRES_PASSWORD:-}" ]]; then
    echo "POSTGRES_PASSWORD is not set. Setting default value to 'postgres'."
    export POSTGRES_PASSWORD=postgres
fi

echo "Initialize the Java environment..."
"$ROOT/target/opennms/bin/runjava" -s

echo "Initialize the database schema..."
"$ROOT/target/opennms/bin/install" -dis

# Show the time before the start, because a foreground start does not return until OpenNMS stops.
ELAPSED=$(( $(date +%s) - BUILD_START ))
echo "Build and setup time: $(( ELAPSED / 60 ))m $(( ELAPSED % 60 ))s"

if [[ "$RUN_OPENNMS_FOREGROUND" == "yes" ]]; then
  echo "Starting OpenNMS (foreground)..."
  "$ROOT/target/opennms/bin/opennms" -f -t start
else
  echo "Starting OpenNMS (background)..."
  "$ROOT/target/opennms/bin/opennms" -vt start
fi