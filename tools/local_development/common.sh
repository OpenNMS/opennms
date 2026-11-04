#!/usr/bin/env bash
# ----------------------------------------------------------------------
# Setup dependencies for local development
# ----------------------------------------------------------------------
set -euo pipefail          # Fail fast & catch unset vars

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Detect JDK Version we need from root pom.xml
detect_jdk_version_required(){
    # Can't use grep since MacOS doesn't support -P option :(
    REQUIRED_VERSION=$(sed -n 's:.*<source>\(.*\)</source>.*:\1:p' "$ROOT_POM")
    if [ -z "$REQUIRED_VERSION" ]; then
        # try to get target version if source is not found
        REQUIRED_VERSION=$(sed -n 's:.*<target>\(.*\)</target>.*:\1:p' "$ROOT_POM")
    fi
}

# Function to detect if PostgreSQL is installed and its version
detect_postgres_installed(){
    if command -v pg_isready >/dev/null; then
        if ! pg_isready -q; then
            # echo "PostgreSQL not ready"
            # setup_postgres
            POSTGRES_VERSION="unknown"
        else
            echo "PostgreSQL is already ready"
            POSTGRES_VERSION=$(psql --version | awk '{print $3}')
        fi
    else
        # Fallback – try to connect directly
        if ! nc -z localhost 5432; then
            # echo "PostgreSQL not reachable"
            # setup_postgres
            # need netcat-openbsd package on Ubuntu
            POSTGRES_VERSION="unknown"
        else
            # echo "PostgreSQL is already reachable"
            # If psql is available, get the version
            if command -v psql >/dev/null 2>&1; then
                POSTGRES_VERSION=$(psql --version | awk '{print $3}')
            else
                POSTGRES_VERSION=$(docker exec opennms-postgres psql --version | awk '{print $3}')                                                                                                                                                                           
            fi
        fi
    fi
    
}

# Function to detect the system librrd (provided by the rrdtool package)
detect_librrd_location(){
export LIBRRD=${LIBRRD:-$( \
  for path in \
    /usr/lib/x86_64-linux-gnu/librrd.so.8 \
    /usr/lib/aarch64-linux-gnu/librrd.so.8 \
    /usr/lib64/librrd.so.8 \
    /usr/lib/librrd.so.8 \
    /opt/homebrew/lib/librrd.8.dylib \
    /usr/local/lib/librrd.8.dylib
  do
    if [[ -f "$path" ]]; then
      echo "$path"
      break
    fi
  done
)}

if [[ -z "$LIBRRD" ]] || [[ ! -f "$LIBRRD" ]]; then
  echo "Warning: librrd not found. Install rrdtool (Debian/Ubuntu: apt-get install rrdtool, macOS: brew install rrdtool)." >&2
else
  echo "Found librrd at $LIBRRD"
fi
}
