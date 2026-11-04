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
    echo "  --help                       Show this help message"
    echo "  --check-dependencies         Check if required dependencies are installed (default action)"
    echo "  --deploy-postgresql          Deploy and setup PostgreSQL using Docker"
    echo "  --enable-rrdtool             Update OpenNMS configuration to use RRDtool (system librrd)"
    exit 1
}

if [[ $# -eq 0 ]]; then
    # usage
    CHECK_DEPENDENCIES="yes"
fi

DEPLOY_POSTGRESQL="no"
ENABLE_RRDTOOL_CONFIGURATION="no"

while [[ $# -gt 0 ]]; do
    case $1 in
        --help)
            usage
            ;;
        --check-dependencies)
            CHECK_DEPENDENCIES="yes"
            shift
            ;;
        --deploy-postgresql)
            DEPLOY_POSTGRESQL="yes"
            shift
            ;;
        --enable-rrdtool )
            ENABLE_RRDTOOL_CONFIGURATION="yes"
            shift
            ;;
        --all)
            DEPLOY_POSTGRESQL="yes"
            shift
            ;;

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

command -v javac >/dev/null 2>&1 || {
    echo "Java compiler (javac) is not installed. Please install JDK 11 or higher." >&2
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

command -v node >/dev/null 2>&1 || {
    echo "Node.js is not installed. Please install Node.js." >&2
    if [[ "$OS_NAME" == "Linux" ]]; then
      echo "On Debian/Ubuntu you can install it via: sudo apt-get install nodejs" >&2
    elif [[ "$OS_NAME" == "macOS" ]]; then
      echo "On macOS you can install it via: brew install node" >&2
    fi
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

# command -v rrdtool >/dev/null 2>&1 || {
#     echo "rrdtool is not installed. Please install rrdtool." >&2
#     if [[ "$OS_NAME" == "Linux" ]]; then
#       echo "On Debian/Ubuntu you can install it via: sudo apt-get install rrdtool" >&2
#     elif [[ "$OS_NAME" == "macOS" ]]; then
#       echo "On macOS you can install it via: brew install rrdtool" >&2
#     fi
#     exit 1
# }

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

RRDTOOL_VERSION=$( (rrdtool --version || true) | head -n1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' )

POSTGRES_VERSION=""

echo ""
echo "Project root: $ROOT"
echo "Product name: $PRODUCT_NAME"
echo "Project version: $OPENNMS_VERSION"

# Start PostgreSQL Docker container
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

# Ensure PostgreSQL is setup
setup_postgres(){
if [[ "$DEPLOY_POSTGRESQL" == "yes" || "$POSTGRES_VERSION" == "unknown"  ]]; then
    start_postgres_docker
else
    echo "DEPLOY_POSTGRESQL is set to 'no'. Skipping"
    return 
fi
}   

# ----------------------------------------------------------------------
# Check dependencies
# ----------------------------------------------------------------------
if [[ "${CHECK_DEPENDENCIES:-}" == "yes" ]]; then
    echo ""
    echo "Maven version: $MAVEN_VERSION"
    echo "Maven Java version: $MAVEN_JAVA_VERSION (Java major version: $JAVA_MAJOR_VERSION)"
    echo "Node.js version: $(node -v)"
    echo "NPM version: $(npm -v)"
    echo "Python version: $($PYTHON --version 2>&1)"
    echo "Git version: $(git --version)"
    echo "Docker version: $(docker --version)"

    if ! command -v nc >/dev/null 2>&1; then
    NC_VERSION="not installed"
    else
    NC_VERSION=$(
        strings "$(command -v nc)" |
        grep -i -E 'netcat|PROJECT' |
        grep -oE '[0-9]+(\.[0-9]+)*' |
        head -n1
    )
    NC_VERSION=${NC_VERSION:-unknown}
    fi

    echo "Netcat version: $NC_VERSION"

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

    detect_librrd_location

    exit 0
fi

if [[ "$DEPLOY_POSTGRESQL" == "yes" ]]; then
    setup_postgres
fi

if [[ "$ENABLE_RRDTOOL_CONFIGURATION" == "yes" ]]; then
   detect_librrd_location
   if [[ -n "$LIBRRD" ]]; then
       echo "Enabling RRDtool configuration in OpenNMS..."
       if [[ ! -d "$ROOT/target/opennms/etc/opennms.properties.d" ]]; then
           echo "You need to build OpenNMS at least once before enabling RRDtool configuration." >&2
           exit 1
       fi
    RRDTOOL_BINARY="$(which rrdtool 2>/dev/null || echo /usr/bin/rrdtool)"
    echo "
    org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy
    opennms.library.rrd=$LIBRRD
    org.opennms.web.graphs.engine=rrdtool
    rrd.binary=$RRDTOOL_BINARY
    " > "$ROOT/target/opennms/etc/opennms.properties.d/timeseries.properties"
   else
       echo "librrd not found. Cannot enable RRDtool configuration." >&2
       exit 1
   fi
fi