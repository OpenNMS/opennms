#!/usr/bin/env bash
# ----------------------------------------------------------------------
# Setup dependencies for local development
# ----------------------------------------------------------------------
set -euo pipefail          # Fail fast & catch unset vars

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

# ----------------------------------------------------------------------
# Argument parsing
# ----------------------------------------------------------------------

usage(){
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  --help                  Show this help message"
    echo "  --check-dependencies    Check if required dependencies are installed (default action)"
    echo "  --install-postgresql    Install and setup PostgreSQL using Docker"
    echo "  --install-jrrd2         Install jrrd2 library,from prebuilt binaries"
    echo "  --install-jrrd2-from-source  Compile and install jrrd2 from source code"
    # echo "  --install-jicmp         Install jicmp library"
    # echo "  --install-jicmp6        Install jicmp6 library"
    exit 1
}

if [[ $# -eq 0 ]]; then
    # usage
    CHECK_DEPENDENCIES="yes"
fi

INSTALL_POSTGRESQL="no"
INSTALL_JRRD2="no" # install prebuilt jrrd2
INSTALL_JRRD2_FROM_SOURCE="no"
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
        --install-jrrd2 )
            INSTALL_JRRD2="yes"
            shift
            ;;
        --install-jrrd2-from-source)
            INSTALL_JRRD2="no"
            INSTALL_JRRD2_FROM_SOURCE="yes"
            shift
            ;;
        --all)
            INSTALL_POSTGRESQL="yes"
            INSTALL_JRRD2_FROM_SOURCE="yes"
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




start_postgres_docker(){
    if [[ "$(docker ps -q -f name=opennms-postgres)" ]]; then
        echo "PostgreSQL Docker container is already running."
        return
    fi

    echo "Starting PostgreSQL Docker container..."
    cd "$ROOT"/tools/local_development/postgres || exit 1
    docker compose up -d
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
if [[ "$INSTALL_POSTGRESQL" == "yes" || "$POSTGRES_VERSION" == "unknown"  ]]; then
    start_postgres_docker
else
    echo "INSTALL_POSTGRESQL is set to 'no'. Skipping"
    return 
fi
}   

install_jrrd2_from_source(){
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

BASE_URL="https://debian.opennms.org/dists/stable/main"
BASE_ARM64="$BASE_URL/binary-arm64/"
BASE_AMD64="$BASE_URL/binary-amd64/"

get_latest_deb() {
  local base_url=$1
  # Fetch directory listing, filter jrrd, sort by version, pick last
  latest=$(curl -s "$base_url" | grep -o 'jrrd2[^"]*\.deb' | sort -V | tail -n 1)
  echo "$base_url$latest"
}

install_jrrd2_prebuilt(){
    echo "Installing prebuilt jrrd2..."

    # create a simple folder for compiling jrrd2 if not present
    if [[ ! -d "$ROOT/built_dependencies" ]]; then
      mkdir -p "$ROOT/built_dependencies"
      mkdir -p "$ROOT/built_dependencies/lib"
      
    else
      rm -rf "$ROOT/built_dependencies"
      mkdir -p "$ROOT/built_dependencies"
      mkdir -p "$ROOT/built_dependencies/lib"
    fi

    cd "$ROOT/built_dependencies" || exit 1
    mkdir tmp
    cd tmp || exit 1

    if [[ "$OS_NAME" == "Linux" ]]; then
      ARCH=$(uname -m)
      if [[ "$ARCH" == "x86_64" ]]; then
        DEB_URL=$(get_latest_deb "$BASE_AMD64")
      else
        echo "Unsupported architecture: $ARCH"
        exit 1
      fi
      echo "Downloading jrrd2 from $DEB_URL"
    fi

    if [[ "$OS_NAME" == "macOS" ]]; then
      echo "macOS detected. Using prebuilt binaries from arm64 package."
      DEB_URL=$(get_latest_deb "$BASE_ARM64")
      echo "Downloading jrrd2 from $DEB_URL"
    fi

    if [[ ! -z "$DEB_URL" ]]; then
      curl -s -LO "$DEB_URL"
      DEB_FILE=$(basename "$DEB_URL")
    fi


    if [[ -f "$DEB_FILE" ]]; then  
      ar vx "$DEB_FILE" 2>/dev/null 1>&2 || true
      tar -xvf data.tar.gz 2>/dev/null 1>&2 || true
    fi

    mv usr/share/java/jrrd2.jar $ROOT/built_dependencies/
    mv usr/lib/jni/libjrrd2.so $ROOT/built_dependencies/lib

    cd .. || exit 1

    rm -rf tmp

    if [[ "$OS_NAME" == "macOS" ]]; then
       ln -s $ROOT/built_dependencies/lib/libjrrd2.so $ROOT/built_dependencies/lib/libjrrd2.dylib
    fi

    JRRD_JAR="$ROOT/built_dependencies/jrrd2.jar"
    JRRD_LIB="$ROOT/built_dependencies/lib/libjrrd2.so"

   # echo "Successfully installed prebuilt jrrd2: $JRRD_JAR, $JRRD_LIB"
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
    install_jrrd2_prebuilt
fi

if [[ "$INSTALL_JRRD2_FROM_SOURCE" == "yes" ]]; then
    install_jrrd2_from_source
fi
# ----------------------------------------------------------------------
# 2. Install dependencies
# ----------------------------------------------------------------------


exit 1

install_icmp_libs(){

}

install_postgresql(){

}




