#!/usr/bin/env bash
# ----------------------------------------------------------------------
# Setup dependencies for local development
# ----------------------------------------------------------------------
set -euo pipefail          # Fail fast & catch unset vars

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Detect JDK Version we need from root pom.xml
detect_jdk_version_required(){
    local pom="${ROOT_POM:-$ROOT/pom.xml}"
    # The build runs only on the JDK in the enforcer range. Use its lower bound, for example 25 from [25,26).
    REQUIRED_VERSION=$(sed -n '/<requireJavaVersion>/,/<\/requireJavaVersion>/s:.*<version>\[\([0-9]*\),.*:\1:p' "$pom" | head -n 1)
    if [ -z "$REQUIRED_VERSION" ]; then
        # Can't use grep since MacOS doesn't support -P option :(
        REQUIRED_VERSION=$(sed -n 's:.*<source>\(.*\)</source>.*:\1:p' "$pom")
    fi
    if [ -z "$REQUIRED_VERSION" ]; then
        # try to get target version if source is not found
        REQUIRED_VERSION=$(sed -n 's:.*<target>\(.*\)</target>.*:\1:p' "$pom")
    fi
}

# Print the major version of the JDK in the given directory
java_major_version(){
    "$1/bin/java" -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -n 1
}

# Set JAVA_HOME to a JDK with the version that the build needs
select_jdk(){
    detect_jdk_version_required
    local candidate
    for candidate in "${JAVA_HOME:-}" \
                     "$(/usr/libexec/java_home -v "$REQUIRED_VERSION" 2>/dev/null)" \
                     "$(brew --prefix 2>/dev/null)/opt/openjdk@$REQUIRED_VERSION/libexec/openjdk.jdk/Contents/Home" \
                     /usr/lib/jvm/java-"$REQUIRED_VERSION"-* /usr/lib/jvm/jdk-"$REQUIRED_VERSION"* /usr/lib/jvm/temurin-"$REQUIRED_VERSION"*; do
        if [[ -x "$candidate/bin/java" && "$(java_major_version "$candidate")" == "$REQUIRED_VERSION" ]]; then
            export JAVA_HOME="$candidate"
            export PATH="$JAVA_HOME/bin:$PATH"
            echo "Using JDK $REQUIRED_VERSION: $JAVA_HOME"
            return 0
        fi
    done
    echo "No JDK $REQUIRED_VERSION found. Install one, or set JAVA_HOME to it." >&2
    return 1
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

# Function to detect jrrd2 locations
detect_jrrd2_location(){
export JRRD_JAR=${JRRD_JAR:-$( \
  for path in \
    "$ROOT"/built/jrrd2-*.jar \
    "$ROOT"/built/jrrd2*.jar \
    ./java/jrrd2.jar \
    /usr/share/java/jrrd2.jar \
    /usr/local/lib/jrrd2.jar
  do
    # Handle wildcards safely
    if compgen -G "$path" > /dev/null; then
      ls -1 -- "$path" | sort -V | head -n1
      break
    fi
  done
)}

export JRRD_LIB=${JRRD_LIB:-$( \
  for path in \
    "$ROOT"/built/lib/libjrrd2.so \
    ./lib/libjrrd2.so \
    /usr/lib64/libjrrd2.so \
    /usr/local/lib/libjrrd2.dylib
  do
    if [[ -f "$path" ]]; then
      echo "$path"
      break
    fi
  done
)}

#  Check that they exist
if [[ ! -f "$JRRD_JAR" ]]; then
  echo "Warning: jrrd2.jar not found." >&2
else
  echo "Found jrrd2.jar at $JRRD_JAR"
fi

if [[ ! -f "$JRRD_LIB" ]]; then
  echo "Warning: libjrrd2 not found " >&2
else
  echo "Found libjrrd2 at $JRRD_LIB"
fi
}

