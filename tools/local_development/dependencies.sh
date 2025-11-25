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

command -v python >/dev/null 2>&1 || {
    echo "Python is not installed. Please install python." >&2
    exit 1
}

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

echo ""
echo "Project root: $ROOT"
echo "Product name: $PRODUCT_NAME"
echo "Project version: $OPENNMS_VERSION"
echo ""
echo "Maven version: $MAVEN_VERSION"
echo "Maven Java version: $MAVEN_JAVA_VERSION (Java major version: $JAVA_MAJOR_VERSION)"
echo "RRDtool version: $RRDTOOL_VERSION"


detect_jdk_version_required(){
    # Can't use grep since MacOS doesn't support -P option :(
    REQUIRED_VERSION=$(sed -n 's:.*<source>\(.*\)</source>.*:\1:p' "$ROOT_POM")
    if [ -z "$REQUIRED_VERSION" ]; then
        # try to get target version if source is not found
        REQUIRED_VERSION=$(sed -n 's:.*<target>\(.*\)</target>.*:\1:p' "$ROOT_POM")
    fi
}

detect_jdk_version_required
echo "Required JDK version: $REQUIRED_VERSION"

if [[ "$JAVA_MAJOR_VERSION" -lt "$REQUIRED_VERSION" ]]; then
        echo "Installed JDK version ($MAVEN_JAVA_VERSION) is less than required version ($REQUIRED_VERSION)." >&2
    else
        echo "Installed JDK version ($MAVEN_JAVA_VERSION) meets the required version ($REQUIRED_VERSION)." >&2
fi

exit 1


# ----------------------------------------------------------------------
# 2. Install dependencies
# ----------------------------------------------------------------------

install_jrrd2(){

}

install_icmp_libs(){

}

install_postgresql(){

}




