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
INSTALL_POSTGRESQL=${INSTALL_POSTGRESQL:-"no"}

INSTALL_JRRD2=${INSTALL_JRRD2:-"no"}
# INSTALL_JICMP=${INSTALL_JICMP:-"no"}
# INSTALL_JICMP6=${INSTALL_JICMP6:-"no"}

ROOT="$(pwd)"

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
            echo "PostgreSQL not ready – attempting to start Docker container"
            setup_postgres
        else
            echo "PostgreSQL is already ready"
        fi
    else
        # Fallback – try to connect directly
        if ! nc -z localhost 5432; then
            echo "PostgreSQL not reachable – attempting to start Docker container"
            setup_postgres
        else
            echo "PostgreSQL is already reachable"
        fi
    fi
}

check_postgres


