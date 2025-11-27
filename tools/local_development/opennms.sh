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
    echo "  --help                   Show this help message"
    echo "  --enable-jrrd2          Enable jrrd2 library,from prebuilt binaries"
    exit 1
}

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

    echo "Cleaning previous build artifacts..."
    rm -rf ./target
    rm -rf ./features/minion/container/karaf/target

    echo "Compiling & assembling (skip tests)..."
    ./clean.pl && ./compile.pl -DskipTests=true && ./assemble.pl -DskipTests=true
fi

env 

echo "Compiling & assembling (skip tests)..."
./clean.pl && ./compile.pl -DskipTests=true && ./assemble.pl -DskipTests=true

echo "Preparing symlink for OpenNMS release $RELEASE"
mkdir -p "./target/opennms-$RELEASE"
ln -s "$ROOT/target/opennms-$RELEASE" "$ROOT/target/opennms"
tar -zxvf "./target/opennms-$RELEASE.tar.gz" -C "$ROOT/target/opennms-$RELEASE"

# Set runtime user
echo "RUNAS=$(id -u -n)" > "$ROOT/target/opennms/etc/opennms.conf"

# If jrrd2 is installed, setup config
if [[ "$ENABLE_JRRD2" == "yes" ]]; then 
    echo "
    org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy
    org.opennms.rrd.interfaceJar=$JRRD_JAR
    opennms.library.jrrd2=$JRRD_LIB
    org.opennms.web.graphs.engine=rrdtool
    rrd.binary=/usr/bin/rrdtool
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

