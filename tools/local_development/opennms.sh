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
    echo "  --enable-jrrd2          Enable jrrd2 library,from prebuilt binaries"
    echo "  --skip-cleanup          Skip cleanup of previous build artifacts"
    echo "  --enable-tests          Enable running tests during build"
    exit 1
}

# Default options
ENABLE_TESTS="no"
SKIP_CLEANUP="no"
ENABLE_JRRD2=${ENABLE_JRRD2:-"no"}
# INSTALL_JICMP=${INSTALL_JICMP:-"no"}
# INSTALL_JICMP6=${INSTALL_JICMP6:-"no"}

while [[ $# -gt 0 ]]; do
    case $1 in
        --help)
            usage
            ;;
        --enable-jrrd2 )
            ENABLE_JRRD2="yes"
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
        --all)
            ENABLE_JRRD2="yes"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done


# run dependency setup
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

if [[ "$ENABLE_JRRD2" == "yes" ]]; then
   detect_jrrd2_location
fi


detect_postgres_installed

if [[ ${POSTGRES_VERSION:-} == "unknown" ]]; then
    echo "PostgreSQL not detected. "
    exit 1
fi

# ------------------------------------------------------
# Build OpenNMS
# ------------------------------------------------------

if [[ -f "$ROOT/target/opennms/bin/opennms" ]]; then
    echo "OpenNMS already built. Lets stop existing."
    ./target/opennms/bin/opennms stop || true

    if [[ "$SKIP_CLEANUP" == "yes" ]]; then
        echo "Skipping cleanup of previous build artifacts."
    else
        echo "Cleaning previous build artifacts..."
        rm -rf ./target
        rm -rf ./features/minion/container/karaf/target
        ./clean.pl
    fi

fi

echo "Checking ulimit..."
ULIMIT_OUTPUT=$(ulimit -n || true)
echo "Current ulimit -n: $ULIMIT_OUTPUT"
if [[ "$ULIMIT_OUTPUT" == "unlimited" || "$ULIMIT_OUTPUT" -gt 20000 ]]; then
    echo "ulimit is sufficient."
else
    echo "Setting ulimit to 20000"
    ulimit -n 20000 || echo "Failed to set ulimit. You may need to run this script with elevated permissions."
fi

echo "Compiling & assembling (skip tests)..."
if [[ "$SKIP_CLEANUP" == "yes" ]]; then
    echo "Skipping cleanup of previous build artifacts."
else
    echo "Cleaning previous build artifacts..."
    ./clean.pl
fi

if [[ "$ENABLE_TESTS" == "yes" ]]; then
    echo "Compiling & assembling (with tests)..."
    ./compile.pl && ./assemble.pl
else
    echo "Compiling & assembling (skip tests)..."
    ./compile.pl -DskipTests=true && ./assemble.pl -DskipTests=true
fi

echo "Preparing symlink for OpenNMS release $RELEASE"
mkdir -p "./target/opennms-$RELEASE"
ln -s "$ROOT/target/opennms-$RELEASE" "$ROOT/target/opennms"
tar -zxvf "./target/opennms-$RELEASE.tar.gz" -C "$ROOT/target/opennms-$RELEASE"

# Set runtime user
echo "RUNAS=$(id -u -n)" > "$ROOT/target/opennms/etc/opennms.conf"

# If jrrd2 is installed, setup config
if [[ "$ENABLE_JRRD2" == "yes" ]]; then 
    # Figureout where rrdtool is installed
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

echo "Starting OpenNMS..."
"$ROOT/target/opennms/bin/opennms" -t start

