#!/usr/bin/env bash
#
# build.sh — Build all Docker images for OpenNMS Delta-V
#
# Usage:
#   ./build.sh              Build everything (compile + assemble + images)
#   ./build.sh images       Build Docker images only (skip Maven)
#   ./build.sh compile      Compile only (skip assembly and images)
#   ./build.sh push         Build and push images to a registry
#
# Environment:
#   DOCKER_REGISTRY   Docker registry (default: docker.io)
#   DOCKER_ORG        Docker org/user (default: opennms)
#   SKIP_TESTS        Set to "false" to run tests (default: true)
#   JAVA_HOME         JDK 17 path (auto-detected if unset)
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
SKIP_TESTS="${SKIP_TESTS:-true}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-docker.io}"
DOCKER_ORG="${DOCKER_ORG:-opennms}"

# Detect version from POM
VERSION="$("$REPO_ROOT/.circleci/scripts/pom2version.sh" "$REPO_ROOT/pom.xml")"

log() { echo "==> $*"; }
err() { echo "ERROR: $*" >&2; exit 1; }

check_prereqs() {
    command -v docker >/dev/null 2>&1 || err "docker not found"
    command -v perl >/dev/null 2>&1   || err "perl not found (needed by compile.pl)"

    # Verify Java 17
    if [ -z "${JAVA_HOME:-}" ]; then
        if [ -d "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home" ]; then
            export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
        fi
    fi
    java_version=$(java -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\)\..*/\1/')
    [ "$java_version" = "17" ] || err "Java 17 required (found: $java_version)"
}

do_compile() {
    log "Compiling OpenNMS (version $VERSION)..."
    local test_flag=""
    [ "$SKIP_TESTS" = "true" ] && test_flag="-DskipTests"
    cd "$REPO_ROOT"
    ./compile.pl $test_flag
}

do_assemble() {
    log "Assembling Horizon distribution..."
    cd "$REPO_ROOT"
    ./assemble.pl -Dopennms.home=/opt/opennms -DskipTests -p dir

    log "Building container/features module..."
    cd "$REPO_ROOT"
    JAVA_HOME="${JAVA_HOME:-}" ./maven/bin/mvn -DskipTests -pl container/features install

    log "Building Sentinel features module..."
    cd "$REPO_ROOT"
    JAVA_HOME="${JAVA_HOME:-}" ./maven/bin/mvn -DskipTests -pl features/container/sentinel install

    log "Building Daemon assembly..."
    cd "$REPO_ROOT/opennms-assemblies/daemon"
    ../../maven/bin/mvn -DskipTests install

    log "Building Alarmd assembly..."
    cd "$REPO_ROOT/opennms-assemblies/alarmd"
    ../../maven/bin/mvn -DskipTests install
}

do_images() {
    local make_args="DOCKER_REGISTRY=$DOCKER_REGISTRY DOCKER_ORG=$DOCKER_ORG"
    [ "${1:-}" = "push" ] && make_args="$make_args DOCKER_FLAGS=--push"

    log "Building Horizon image (opennms/horizon:$VERSION)..."
    cd "$REPO_ROOT/opennms-container/core"
    make image $make_args

    log "Building Daemon image (opennms/daemon:$VERSION)..."
    cd "$REPO_ROOT/opennms-container/sentinel"
    make image $make_args

    log "Building Alarmd image (opennms/alarmd:$VERSION)..."
    cd "$REPO_ROOT/opennms-container/alarmd"
    make image $make_args

    log "Docker images built:"
    docker images --format "  {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep -E "(horizon|daemon|alarmd)" | head -10
}

do_webapp_overlay() {
    log "Preparing webapp overlay..."
    local overlay_dir="$SCRIPT_DIR/webapp-jetty-webinf-overlay"
    mkdir -p "$overlay_dir/lib" "$overlay_dir/menu"

    # Copy updated webapp JARs
    local webapp_jar="$REPO_ROOT/opennms-webapp/target/opennms-webapp-$VERSION.jar"
    local rest_jar="$REPO_ROOT/opennms-webapp-rest/target/opennms-webapp-rest-$VERSION.jar"
    [ -f "$webapp_jar" ] && cp "$webapp_jar" "$overlay_dir/lib/"
    [ -f "$rest_jar" ]   && cp "$rest_jar"   "$overlay_dir/lib/"

    # Copy dispatcher-servlet.xml
    local servlet_xml="$REPO_ROOT/opennms-webapp/src/main/webapp/WEB-INF/dispatcher-servlet.xml"
    [ -f "$servlet_xml" ] && cp "$servlet_xml" "$overlay_dir/"

    # Copy menu templates
    local menu_src="$REPO_ROOT/ui/src/menu/dist-menu"
    if [ -d "$menu_src" ]; then
        cp "$menu_src"/menu-template*.json "$overlay_dir/menu/" 2>/dev/null || true
    fi

    log "Webapp overlay prepared at $overlay_dir"
}

usage() {
    cat <<'USAGE'
Usage: ./build.sh [command]

Commands:
  (none)    Full build: compile + assemble + images
  compile   Compile only (Maven)
  assemble  Assemble distributions (Horizon + Daemon + Alarmd)
  images    Build Docker images only (requires prior assembly)
  overlay   Prepare webapp overlay files
  push      Build and push images to registry
  clean     Remove named Docker volumes (fresh start)
  help      Show this help

Environment variables:
  DOCKER_REGISTRY   Registry (default: docker.io)
  DOCKER_ORG        Organization (default: opennms)
  SKIP_TESTS        Skip tests (default: true)
  JAVA_HOME         JDK 17 path

Examples:
  ./build.sh                                    # Full build
  ./build.sh images                             # Rebuild images only
  DOCKER_ORG=pbranestrategy ./build.sh push     # Push to custom registry
  ./build.sh clean && docker compose up -d      # Fresh deployment
USAGE
}

do_clean() {
    log "Removing Delta-V Docker volumes..."
    cd "$SCRIPT_DIR"
    docker compose down -v 2>/dev/null || true
    log "Volumes removed. Run 'docker compose up -d' for a fresh start."
}

main() {
    check_prereqs

    case "${1:-all}" in
        all)
            do_compile
            do_assemble
            do_webapp_overlay
            do_images
            log "Build complete! Run: cd $SCRIPT_DIR && docker compose up -d"
            ;;
        compile)
            do_compile
            ;;
        assemble)
            do_assemble
            ;;
        images)
            do_images
            ;;
        overlay)
            do_webapp_overlay
            ;;
        push)
            do_compile
            do_assemble
            do_webapp_overlay
            do_images push
            ;;
        clean)
            do_clean
            ;;
        help|-h|--help)
            usage
            ;;
        *)
            err "Unknown command: $1 (run './build.sh help' for usage)"
            ;;
    esac
}

main "$@"
