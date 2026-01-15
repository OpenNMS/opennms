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

if [[ "$DISABLE_JRRD2" != "true" ]]; then
   # if $JAVA_HOME is not set, ask user to setup JAVA_HOME
   if [[ -z "${JAVA_HOME:-}" ]]; then
     echo "JAVA_HOME is not set. Detecting required JDK version from pom.xml..."
     detect_jdk_version_required
     echo "Detected required JDK version: $REQUIRED_VERSION"
     echo "Please set JAVA_HOME to a JDK $REQUIRED_VERSION installation and re-run this script."
     exit 1
   fi

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

if [[ "$RUN_OPENNMS_FOREGROUND" == "yes" ]]; then
  echo "Starting OpenNMS (foreground)..."
  "$ROOT/target/opennms/bin/opennms" -f -t start
else
  echo "Starting OpenNMS (background)..."
  "$ROOT/target/opennms/bin/opennms" -vt start
fi