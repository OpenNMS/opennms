#!/usr/bin/env bash
# ----------------------------------------------------------------------
# Setup dependencies for local development
# ----------------------------------------------------------------------
set -euo pipefail          # Fail fast & catch unset vars

# ----------------------------------------------------------------------
# Argument parsing
# ----------------------------------------------------------------------

usage(){
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  --help                   Show this help message"
    echo "  --check-dependencies    Check if required dependencies are installed (default action)"
    echo "  --install-postgresql     Install and setup PostgreSQL using Docker"
    echo "  --install-jrrd2         Install jrrd2 library"
    # echo "  --install-jicmp         Install jicmp library"
    # echo "  --install-jicmp6        Install jicmp6 library"
    exit 1
}

if [[ $# -eq 0 ]]; then
    # usage
    CHECK_DEPENDENCIES="yes"
fi

INSTALL_POSTGRESQL="no"
INSTALL_JRRD2="no"
# INSTALL_JICMP="no"
# INSTALL_JICMP6="no"

while [[ $# -gt 0 ]]; do
    case $1 in
        --help)
            usage
            ;;
        --check-dependencies)
            CHECK_DEPENDENCIES="yes"
            shift
            ;;
        --install-postgresql)
            INSTALL_POSTGRESQL="yes"
            shift
            ;;
        --install-jrrd2)
            INSTALL_JRRD2="yes"
            shift
            ;;
        --all)
            INSTALL_POSTGRESQL="yes"
            INSTALL_JRRD2="yes"
            # INSTALL_JICMP="yes"
            # INSTALL_JICMP6="yes"
            shift
            ;;

        # --install-jicmp)
        #     INSTALL_JICMP="yes"
        #     shift
        #     ;;
        # --install-jicmp6)
        #     INSTALL_JICMP6="yes"
        #     shift
        #     ;;
        *)
            echo "Unknown option: $1"
            usage
            ;;
    esac
done




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
# Detect required tools
# ------------------------------------------------------

command -v java >/dev/null 2>&1 || {
    echo "Java is not installed. Please install JDK 11 or higher." >&2
    exit 1
}

command -v mvn >/dev/null 2>&1 || {
    echo "Maven is not installed. Please install Maven 3.6 or higher." >&2
    exit 1
}

command -v docker >/dev/null 2>&1 || {
    echo "Docker is not installed. Please install Docker." >&2
    exit 1
}

command -v git >/dev/null 2>&1 || {
    echo "Git is not installed. Please install Git." >&2
    exit 1
}

command -v curl >/dev/null 2>&1 || {
    echo "Curl is not installed. Please install Curl." >&2
    exit 1
}

command -v perl >/dev/null 2>&1 || {
    echo "Perl is not installed. Please install perl." >&2
    exit 1
}

if command -v python3 >/dev/null 2>&1; then
    PYTHON=python3
elif command -v python >/dev/null 2>&1; then
    PYTHON=python
else
    echo "Python is not installed. Please install python3." >&2
    exit 1
fi

command -v rrdtool >/dev/null 2>&1 || {
    echo "rrdtool is not installed. Please install rrdtool." >&2
    if [[ "$OS_NAME" == "Linux" ]]; then
      echo "On Debian/Ubuntu you can install it via: sudo apt-get install rrdtool" >&2
    elif [[ "$OS_NAME" == "macOS" ]]; then
      echo "On macOS you can install it via: brew install rrdtool" >&2
    fi
    exit 1
}

# ----------------------------------------------------------------------
# Resolve variables
# ----------------------------------------------------------------------
ROOT="$(pwd)"
ROOT_POM="$ROOT/pom.xml"

PRODUCT_NAME=$(sed -n 's:.*<product.name>\(.*\)</product.name>.*:\1:p' "$ROOT_POM" | head -n 1)
OPENNMS_VERSION="$(.circleci/scripts/pom2version.sh pom.xml)"

MAVEN_VERSION=$(mvn -v | awk '/Apache Maven/ {print $3}')
MAVEN_JAVA_VERSION=$(mvn -v | awk '/Java version/ {print $3}'| tr -d ',')
JAVA_MAJOR_VERSION=$(echo "$MAVEN_JAVA_VERSION" | awk -F. '{print $1}')

RRDTOOL_VERSION=$(rrdtool --version | head -n1 | awk '{print $2}')

POSTGRES_VERSION=""

echo ""
echo "Project root: $ROOT"
echo "Product name: $PRODUCT_NAME"
echo "Project version: $OPENNMS_VERSION"


detect_jdk_version_required(){
    # Can't use grep since MacOS doesn't support -P option :(
    REQUIRED_VERSION=$(sed -n 's:.*<source>\(.*\)</source>.*:\1:p' "$ROOT_POM")
    if [ -z "$REQUIRED_VERSION" ]; then
        # try to get target version if source is not found
        REQUIRED_VERSION=$(sed -n 's:.*<target>\(.*\)</target>.*:\1:p' "$ROOT_POM")
    fi
}

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
    export POSTGRES_PASSWORD=postgres
    POSTGRES_VERSION=$(docker exec opennms-postgres psql --version | awk '{print $3}')                                                                                                                                                                           
    echo "PostgreSQL: $POSTGRES_VERSION"

}

setup_postgres(){
if [[ "$INSTALL_POSTGRESQL" == "yes" ]]; then
    start_postgres_docker
else
    echo "INSTALL_POSTGRESQL is set to 'no'. Skipping"
    return 
fi
}   


detect_postgres_installed(){
    if command -v pg_isready >/dev/null; then
        if ! pg_isready -q; then
            echo "PostgreSQL not ready - attempting to start Docker container"
            setup_postgres
        else
            echo "PostgreSQL is already ready"
            POSTGRES_VERSION=$(psql --version | awk '{print $3}')
        fi
    else
        # Fallback – try to connect directly
        if ! nc -z localhost 5432; then
            echo "PostgreSQL not reachable - attempting to start Docker container"
            setup_postgres
        else
            echo "PostgreSQL is already reachable"
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
JRRD_JAR=${JRRD_JAR:-$( \
  for path in \
    "$ROOT"/built_dependencies/jrrd2-*.jar \
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

JRRD_LIB=${JRRD_LIB:-$( \
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


detect_jrrd2_location2(){
    if [[ -f "$ROOT/built_dependencies/" && -f "$ROOT/built_dependencies/libjrrd2.so" ]]; then
        JRRD_JAR="$ROOT/built_dependencies/jrrd2.jar"
        JRRD_LIB="$ROOT/built_dependencies/libjrrd2.so"

        echo "Found jrrd2 in built_dependencies: $JRRD_JAR, $JRRD_LIB"
        return
    fi

    echo "Could not find jrrd2 in built_dependencies."

}


install_jrrd2(){
    echo "Compiling jrrd2 from source..."
    # create a simple folder for compiling jrrd2 if not present
    if [[ ! -d "$ROOT/built_dependencies" ]]; then
      mkdir -p "$ROOT/built_dependencies"
    else
      rm -rf "$ROOT/built_dependencies"
      mkdir -p "$ROOT/built_dependencies"
    fi
    
    if [[ -d "$ROOT/jrrd2" ]]; then
        rm -rf "$ROOT/jrrd2"
    fi

    echo "Attempting to build jrrd2 locally..."
    git clone https://github.com/OpenNMS/jrrd2.git
    cd jrrd2
    make
    
    JRRD_VERSION="$("$ROOT"/.circleci/scripts/pom2version.sh "$ROOT"/jrrd2/java/pom.xml)"
    
    JRRD_JAR=$(find "$ROOT/jrrd2" -name "jrrd2-*-$JRRD_VERSION.jar" | head -n 1)
    JRRD_LIB=$(find "$ROOT/jrrd2" -name "libjrrd2.so" | head -n 1)
    
    if [[ ! -d "$ROOT/built_dependencies/lib" ]]; then
      mkdir -p "$ROOT/built_dependencies/lib"
    fi
    
    cp $JRRD_LIB $ROOT/built_dependencies/lib/
    cp $JRRD_JAR $ROOT/built_dependencies/
    if [[ "$OS_NAME" == "macOS" ]]; then
       ln -s $ROOT/built_dependencies/lib/libjrrd2.so $ROOT/built_dependencies/lib/libjrrd2.dylib
    fi
    ln -s $ROOT/build_dependencies/jrrd2-*-$JRRD_VERSION.jar $ROOT/built_dependencies/jrrd2.jar
    
    JRRD_JAR="$ROOT/built_dependencies/jrrd2.jar"
    JRRD_LIB="$ROOT/built_dependencies/lib/$(basename $JRRD_LIB)"  
    cd ..
    echo "Successfully built jrrd2 locally: $JRRD_JAR, $JRRD_LIB"

    rm -rf jrrd2

}


# ----------------------------------------------------------------------
# Check dependencies
# ----------------------------------------------------------------------
if [[ "${CHECK_DEPENDENCIES:-}" == "yes" ]]; then
    echo ""
    echo "Maven version: $MAVEN_VERSION"
    echo "Maven Java version: $MAVEN_JAVA_VERSION (Java major version: $JAVA_MAJOR_VERSION)"
    echo "RRDtool version: $RRDTOOL_VERSION"

    detect_jdk_version_required
    echo "Required JDK version: $REQUIRED_VERSION"

    if [[ "$JAVA_MAJOR_VERSION" -lt "$REQUIRED_VERSION" ]]; then
            echo "Installed JDK version ($MAVEN_JAVA_VERSION) is less than required version ($REQUIRED_VERSION)." >&2
        else
            echo "Installed JDK version ($MAVEN_JAVA_VERSION) meets the required version ($REQUIRED_VERSION)." >&2
    fi

    detect_postgres_installed
    if [[ -n "$POSTGRES_VERSION" ]]; then
        echo "PostgreSQL version: $POSTGRES_VERSION"
    else
        echo "PostgreSQL is not installed or not reachable."
    fi


    detect_jrrd2_location

    exit 0
fi

if [[ "$INSTALL_POSTGRESQL" == "yes" ]]; then
    setup_postgres
fi

if [[ "$INSTALL_JRRD2" == "yes" ]]; then
    install_jrrd2
fi
# ----------------------------------------------------------------------
# 2. Install dependencies
# ----------------------------------------------------------------------


exit 1

install_icmp_libs(){

}

install_postgresql(){

}




