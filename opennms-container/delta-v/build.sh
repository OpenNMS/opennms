#!/usr/bin/env bash
#
# build.sh — Build all Docker images for OpenNMS Delta-V
#
# Usage:
#   ./build.sh              Build everything (compile + assemble + images + deltav)
#   ./build.sh images       Build base Docker images only (skip Maven)
#   ./build.sh deltav       Build Delta-V layered images only (requires base images)
#   ./build.sh compile      Compile only (skip assembly and images)
#   ./build.sh push         Build and push images to registry
#
# Environment:
#   DOCKER_REGISTRY   Docker registry (default: docker.io)
#   DOCKER_ORG        Docker org/user (default: opennms)
#   SKIP_TESTS        Set to "false" to run tests (default: true)
#   JAVA_HOME         JDK 21 path (auto-detected if unset)
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

    # Verify Java 21
    if [ -z "${JAVA_HOME:-}" ]; then
        if [ -d "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home" ]; then
            export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
        fi
    fi
    if [ -z "${JAVA_HOME:-}" ]; then
        err "JAVA_HOME not set and temurin-21 not found. Set JAVA_HOME to a JDK 21 installation."
    fi
    java_version=$("${JAVA_HOME}/bin/java" -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\)\..*/\1/')
    [ "$java_version" = "21" ] || err "Java 21 required (JAVA_HOME=$JAVA_HOME reports: $java_version)"
    export PATH="${JAVA_HOME}/bin:${PATH}"

    # Ensure Docker buildx uses the "default" builder instance.
    # Docker Desktop sets the active builder to "desktop-linux", which the
    # Makefile in opennms-container/core and sentinel rejects.
    local current_buildx
    current_buildx=$(docker buildx inspect 2>/dev/null | head -1 | sed 's/^Name: *//')
    if [ "$current_buildx" != "default" ]; then
        log "Switching Docker buildx from '$current_buildx' to 'default'..."
        docker context use default 2>/dev/null || true
        docker buildx use default 2>/dev/null || true
    fi
}

do_compile() {
    log "Compiling OpenNMS (version $VERSION)..."
    local test_flag=""
    [ "$SKIP_TESTS" = "true" ] && test_flag="-DskipTests"
    cd "$REPO_ROOT"
    ./compile.pl $test_flag
}

do_assemble() {
    log "Assembling Horizon distribution (webapp — skipped, use Java 17 if needed)..."
    # NOTE: assemble.pl builds opennms-full-assembly which is the webapp container.
    # The webapp stays on Java 17 and is out of scope for the Java 21 daemon upgrade.
    # Build it separately with Java 17 if needed: JAVA_HOME=<jdk17> ./assemble.pl -p dir -DskipTests
    # cd "$REPO_ROOT"
    # ./assemble.pl -Dopennms.home=/opt/opennms -DskipTests -p dir

    log "Building Karaf container modules (shared + karaf + features)..."
    cd "$REPO_ROOT"
    ./maven/bin/mvn -DskipTests -pl container/shared,container/karaf,container/features clean install

    log "Building Sentinel and Minion features modules..."
    cd "$REPO_ROOT"
    ./maven/bin/mvn -DskipTests -pl features/container/sentinel,features/container/minion clean install

    log "Building Sentinel assembly..."
    cd "$REPO_ROOT/opennms-assemblies/sentinel"
    ../../maven/bin/mvn -DskipTests clean install

    log "Building Minion assembly..."
    cd "$REPO_ROOT/opennms-assemblies/minion"
    ../../maven/bin/mvn -DskipTests clean install

    log "Building Daemon assembly..."
    cd "$REPO_ROOT/opennms-assemblies/daemon"
    ../../maven/bin/mvn -DskipTests clean install

    log "Building Alarmd assembly..."
    cd "$REPO_ROOT/opennms-assemblies/alarmd"
    ../../maven/bin/mvn -DskipTests clean install
}

do_db_init_image() {
    log "Building db-init image (opennms/db-init:$VERSION)..."
    cd "$REPO_ROOT"
    ./maven/bin/mvn -f core/db-init/pom.xml -DskipTests package
    cd "$REPO_ROOT/core/db-init"
    docker build -t "opennms/db-init:$VERSION" -t "opennms/db-init:latest" .
}

do_images() {
    local make_args="DOCKER_REGISTRY=$DOCKER_REGISTRY DOCKER_ORG=$DOCKER_ORG"
    [ "${1:-}" = "push" ] && make_args="$make_args DOCKER_FLAGS=--push"

    # NOTE: Horizon image (webapp) requires full assembly built with Java 17.
    # Build separately if needed: JAVA_HOME=<jdk17> ./assemble.pl -p dir && make -C opennms-container/core image
    if [ -f "$REPO_ROOT/opennms-full-assembly/target/opennms-full-assembly-$VERSION-core.tar.gz" ]; then
        log "Building Horizon image (opennms/horizon:$VERSION)..."
        cd "$REPO_ROOT/opennms-container/core"
        make image $make_args
    else
        log "Skipping Horizon image (no full assembly found — build with Java 17 if needed)"
    fi

    # The sentinel Makefile tags as opennms/sentinel, but the Delta-V
    # docker-compose expects opennms/daemon. Build then re-tag.
    log "Building Daemon image (opennms/daemon:$VERSION)..."
    cd "$REPO_ROOT/opennms-container/sentinel"
    make image $make_args
    docker image tag "opennms/sentinel:$VERSION" "opennms/daemon:$VERSION"
    docker image tag "opennms/sentinel:$VERSION" "opennms/daemon:latest"

    # Build Minion base image
    log "Building Minion image (opennms/minion:$VERSION)..."
    cd "$REPO_ROOT/opennms-container/minion"
    make image $make_args

    do_db_init_image

    log "Docker images built:"
    docker images --format "  {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep -E "(horizon|daemon|sentinel|minion|db-init)" | head -20
}

do_stage_daemon_jars() {
    log "Staging daemon JARs for Delta-V image..."
    local staging="$SCRIPT_DIR/staging/daemon"
    rm -rf "$staging"
    mkdir -p "$staging"

    # Common JARs (all daemon containers)
    # NOTE: features.xml is copied directly from webapp-overlay/ in Dockerfile.daemon,
    # not staged here. The patched features.xml must come from the image extraction,
    # not from container/features/target/classes/features.xml (which is only one input).
    local pairs=(
        "core/event-forwarder-kafka/target/org.opennms.core.event-forwarder-kafka-$VERSION.jar:event-forwarder-kafka.jar"
        "features/events/daemon/target/org.opennms.features.events.daemon-$VERSION.jar:events.daemon.jar"
        # Daemon-loader JARs
        "core/daemon-loader-trapd/target/org.opennms.core.daemon-loader-trapd-$VERSION.jar:daemon-loader-trapd.jar"
        "core/daemon-loader-syslogd/target/org.opennms.core.daemon-loader-syslogd-$VERSION.jar:daemon-loader-syslogd.jar"
        "core/daemon-loader-provisiond/target/org.opennms.core.daemon-loader-provisiond-$VERSION.jar:daemon-loader-provisiond.jar"
        "core/daemon-loader-bsmd/target/org.opennms.core.daemon-loader-bsmd-$VERSION.jar:daemon-loader-bsmd.jar"
        "core/daemon-loader-perspectivepoller/target/org.opennms.core.daemon-loader-perspectivepoller-$VERSION.jar:daemon-loader-perspectivepoller.jar"
        "core/daemon-loader-alarmd/target/org.opennms.core.daemon-loader-alarmd-$VERSION.jar:daemon-loader-alarmd.jar"
        "core/daemon-loader-telemetryd/target/daemon-loader-telemetryd-$VERSION.jar:daemon-loader-telemetryd.jar"
        # Special JARs (EventTranslator split-package fix, Alarmd)
        "opennms-config/target/opennms-config-$VERSION.jar:opennms-config.jar"
        "opennms-util/target/opennms-util-$VERSION.jar:opennms-util.jar"
        "opennms-alarms/daemon/target/opennms-alarmd-$VERSION.jar:opennms-alarmd.jar"
    )

    local missing=0
    for pair in "${pairs[@]}"; do
        local src="${pair%%:*}"
        local dst="${pair##*:}"
        if [ -f "$REPO_ROOT/$src" ]; then
            cp "$REPO_ROOT/$src" "$staging/$dst"
        else
            log "WARNING: $src not found — run './build.sh compile' first"
            missing=$((missing + 1))
        fi
    done

    log "Staged $(ls "$staging" | wc -l | tr -d ' ') files ($missing missing)"
    if [ "$missing" -gt 3 ]; then
        err "Too many missing JARs ($missing) — run './build.sh compile' first"
    fi
}

do_deltav_images() {
    log "Building Delta-V layered images..."

    do_stage_daemon_jars

    # Daemon image (all 15 daemon services share one image)
    log "Building opennms/daemon-deltav:$VERSION..."
    cd "$SCRIPT_DIR"
    docker build \
        --build-arg "VERSION=$VERSION" \
        -f Dockerfile.daemon \
        -t "opennms/daemon-deltav:$VERSION" \
        -t "opennms/daemon-deltav:latest" \
        .

    # Minion image
    log "Building opennms/minion-deltav:$VERSION..."
    docker build \
        --build-arg "VERSION=$VERSION" \
        -f Dockerfile.minion \
        -t "opennms/minion-deltav:$VERSION" \
        -t "opennms/minion-deltav:latest" \
        .

    # Webapp image (requires Horizon base image built with Java 17)
    if docker image inspect "opennms/horizon:$VERSION" >/dev/null 2>&1; then
        log "Building opennms/horizon-deltav:$VERSION..."
        docker build \
            --build-arg "VERSION=$VERSION" \
            -f Dockerfile.webapp \
            -t "opennms/horizon-deltav:$VERSION" \
            -t "opennms/horizon-deltav:latest" \
            .
    else
        log "Skipping horizon-deltav (no opennms/horizon:$VERSION base image)"
    fi

    # Clean up staging
    rm -rf "$SCRIPT_DIR/staging"

    log "Delta-V images built:"
    docker images --format "  {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep -E "deltav" | head -10
}

do_webapp_overlay() {
    log "Preparing webapp overlay..."
    local overlay_dir="$SCRIPT_DIR/webapp-jetty-webinf-overlay"
    mkdir -p "$overlay_dir/lib" "$overlay_dir/menu"

    # Copy updated webapp JARs.
    # opennms-webapp produces a WAR — the JAR is inside the exploded WAR.
    local webapp_jar="$REPO_ROOT/opennms-webapp/target/opennms-webapp-$VERSION/WEB-INF/lib/opennms-webapp-$VERSION.jar"
    local rest_jar="$REPO_ROOT/opennms-webapp-rest/target/opennms-webapp-rest-$VERSION.jar"
    if [ -f "$webapp_jar" ]; then
        cp "$webapp_jar" "$overlay_dir/lib/"
    else
        log "WARNING: webapp JAR not found at $webapp_jar — run './build.sh compile' first"
    fi
    [ -f "$rest_jar" ] && cp "$rest_jar" "$overlay_dir/lib/"

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
  (none)    Full build: compile + assemble + images + deltav
  compile   Compile only (Maven)
  assemble  Assemble distributions (Horizon + Daemon + Alarmd)
  images    Build base Docker images only (requires prior assembly)
  deltav    Build Delta-V layered images (stages JARs into derived images)
  overlay   Prepare webapp overlay files
  push      Build and push images to registry
  clean     Remove named Docker volumes (fresh start)
  help      Show this help

Environment variables:
  DOCKER_REGISTRY   Registry (default: docker.io)
  DOCKER_ORG        Organization (default: opennms)
  SKIP_TESTS        Skip tests (default: true)
  JAVA_HOME         JDK 21 path

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
            do_deltav_images
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
        deltav)
            do_deltav_images
            ;;
        overlay)
            do_webapp_overlay
            ;;
        push)
            do_compile
            do_assemble
            do_webapp_overlay
            do_images push
            do_deltav_images
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
