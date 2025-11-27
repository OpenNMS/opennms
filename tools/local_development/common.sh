
#!/usr/bin/env bash
# ----------------------------------------------------------------------
# Setup dependencies for local development
# ----------------------------------------------------------------------
set -euo pipefail          # Fail fast & catch unset vars

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"


detect_jdk_version_required(){
    # Can't use grep since MacOS doesn't support -P option :(
    REQUIRED_VERSION=$(sed -n 's:.*<source>\(.*\)</source>.*:\1:p' "$ROOT_POM")
    if [ -z "$REQUIRED_VERSION" ]; then
        # try to get target version if source is not found
        REQUIRED_VERSION=$(sed -n 's:.*<target>\(.*\)</target>.*:\1:p' "$ROOT_POM")
    fi
}

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



detect_jrrd2_location(){
export JRRD_JAR=${JRRD_JAR:-$( \
  for path in \
    "$ROOT"/built_dependencies/jrrd2-*.jar \
    "$ROOT"/built_dependencies/jrrd2*.jar \
    ./java/jrrd2.jar \
    /usr/share/java/jrrd2.jar \
    /usr/local/lib/jrrd2.jar
  do
    # Handle wildcards safely
    if compgen -G "$path" > /dev/null; then
      ls -1 $path | sort -V | head -n1
      break
    fi
  done
)}

export JRRD_LIB=${JRRD_LIB:-$( \
  for path in \
    "$ROOT"/built_dependencies/lib/libjrrd2.so \
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

