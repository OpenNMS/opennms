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


INSTALL_POSTGRESQL=${INSTALL_POSTGRESQL:-"no"}

ENABLE_JRRD2=${ENABLE_JRRD2:-"no"}
# INSTALL_JICMP=${INSTALL_JICMP:-"no"}
# INSTALL_JICMP6=${INSTALL_JICMP6:-"no"}

# run dependency setup
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

detect_jrrd2_location

# ------------------------------------------------------
# PostgreSQL 
# ------------------------------------------------------
start_postgres_docker(){
    if [[ "$(docker ps -q -f name=opennms-postgres)" ]]; then
        echo "PostgreSQL Docker container is already running."
        return
    fi

    echo "Starting PostgreSQL Docker container..."
    cd "$ROOT"/tools/local_development/postgres || exit 1
    docker-compose up -d
    cd - || exit 1
    
    # Check if postgres is ready
    until docker exec opennms-postgres pg_isready -U postgres; do
        echo "Waiting for PostgreSQL to be ready..."
        sleep 2
    done
    echo "PostgreSQL is ready."
    export POSTGRES_PASSWORD=postgres                                                                                                                                                                               
}

# setup postgres
setup_postgres(){
if [[ "$INSTALL_POSTGRESQL" == "yes" ]]; then
    start_postgres_docker
else
    echo "INSTALL_POSTGRESQL is set to 'no'. Skipping"
    return 
fi
}   

# Do we have postgres installed?
check_postgres(){
    if command -v pg_isready >/dev/null; then
        if ! pg_isready -q; then
            echo "PostgreSQL not ready - attempting to start Docker container"
            setup_postgres
        else
            echo "PostgreSQL is already ready"
        fi
    else
        # Fallback – try to connect directly
        if ! nc -z localhost 5432; then
            echo "PostgreSQL not reachable - attempting to start Docker container"
            setup_postgres
        else
            echo "PostgreSQL is already reachable"
        fi
    fi
}

check_postgres


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

