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
    echo "  --disable-rrdtool       Do not configure OpenNMS to use RRDtool"
    echo "  --skip-cleanup          Skip cleanup of previous build artifacts"
    echo "  --enable-tests          Enable running tests during build"
    echo "  --run-opennms-foreground    Start OpenNMS in foreground after build"
    exit 1
}

# Default options
ENABLE_TESTS="no"
SKIP_CLEANUP="no"
DISABLE_RRDTOOL=${DISABLE_RRDTOOL:-"false"}
RUN_OPENNMS_FOREGROUND="no"

while [[ $# -gt 0 ]]; do
    case $1 in
        --help)
            usage
            ;;
        --disable-rrdtool )
            DISABLE_RRDTOOL="true"
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
            DISABLE_RRDTOOL="false"
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

if [[ "$DISABLE_RRDTOOL" != "true" ]]; then
   detect_librrd_location
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

# If librrd is installed, setup config
if [[ "$DISABLE_RRDTOOL" != "true" ]] && [[ -n "$LIBRRD" ]]; then
    # Figure out where rrdtool is installed
    RRD_TOOL_PATH=$(which rrdtool || echo "/usr/local/bin/rrdtool")
    echo "Detected rrdtool at: $RRD_TOOL_PATH"

    echo "Configuring OpenNMS to use RRDtool (librrd at $LIBRRD)..."
    echo "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy
opennms.library.rrd=$LIBRRD
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

if [[ "$RUN_OPENNMS_FOREGROUND" == "yes" ]]; then
  echo "Starting OpenNMS (foreground)..."
  "$ROOT/target/opennms/bin/opennms" -f -t start
else
  echo "Starting OpenNMS (background)..."
  "$ROOT/target/opennms/bin/opennms" -vt start
fi