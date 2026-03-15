#!/usr/bin/env bash
#
# deploy.sh — Deploy and manage OpenNMS Delta-V
#
# Usage:
#   ./deploy.sh up [profile] Start services (profiles: lite, passive, full)
#   ./deploy.sh down        Stop all services (preserve data)
#   ./deploy.sh reset       Stop and remove all data
#   ./deploy.sh status      Show service status
#   ./deploy.sh logs [svc]  Tail logs (optionally for a specific service)
#   ./deploy.sh shell <svc> Karaf shell for a service
#   ./deploy.sh test        Verify deployment is working
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Source version
VERSION=$(cat .env | grep VERSION | cut -d= -f2)

log() { echo "==> $*"; }
err() { echo "ERROR: $*" >&2; exit 1; }

# Karaf SSH ports per service (host:container)
declare -A KARAF_PORTS=(
    [webapp]=8102
    [pollerd]=8103
    [collectd]=8104
)

do_up() {
    log "Starting Delta-V (version $VERSION)..."

    # Check images exist
    for img in "opennms/horizon:$VERSION" "opennms/daemon:$VERSION" "opennms/minion:$VERSION"; do
        docker image inspect "$img" >/dev/null 2>&1 || err "Image $img not found. Run ./build.sh first."
    done

    local profile="${1:-}"
    if [ -n "$profile" ]; then
        log "Using profile: $profile"
        COMPOSE_PROFILES="$profile" docker compose up -d
    else
        log "Starting infrastructure only (db-init + webapp + minion)"
        docker compose up -d
    fi

    log "Waiting for services to start..."
    log "Run './deploy.sh status' to check progress."
    log "Web UI: http://localhost:8980/opennms (admin/admin)"
}

do_down() {
    log "Stopping Delta-V..."
    docker compose down
}

do_reset() {
    log "Stopping Delta-V and removing all data volumes..."
    docker compose down --remove-orphans 2>/dev/null || true
    # docker compose down -v only removes volumes for active profile services.
    # Explicitly remove ALL delta-v volumes to ensure clean Karaf bundle caches.
    local stale_vols
    stale_vols=$(docker volume ls --format '{{.Name}}' | grep "^delta-v_" || true)
    if [ -n "$stale_vols" ]; then
        echo "$stale_vols" | xargs docker volume rm 2>/dev/null || true
    fi
    log "Clean slate. Run './deploy.sh up' to start fresh."
}

do_status() {
    docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
}

do_logs() {
    local service="${1:-}"
    if [ -n "$service" ]; then
        docker compose logs "$service" --tail=100 -f
    else
        docker compose logs --tail=20 -f
    fi
}

do_shell() {
    local service="${1:-}"
    [ -z "$service" ] && err "Usage: ./deploy.sh shell <service>"

    local port="${KARAF_PORTS[$service]:-}"
    if [ -z "$port" ]; then
        # Daemon containers use port 8181 internally, mapped to various host ports
        log "Connecting to $service Karaf shell..."
        docker compose exec "$service" /opt/daemon/bin/client 2>/dev/null \
            || docker compose exec "$service" /opt/sentinel/bin/client 2>/dev/null \
            || err "Could not connect to $service Karaf shell"
    else
        log "Connecting to $service Karaf shell (SSH port $port)..."
        ssh -p "$port" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null admin@localhost
    fi
}

do_test() {
    log "Testing Delta-V deployment..."
    local pass=0
    local fail=0

    # Test 1: Check services are running
    local running
    running=$(docker compose ps --status running --format "{{.Name}}" | wc -l | tr -d ' ')
    if [ "$running" -ge 3 ]; then
        log "  [PASS] $running services running"
        pass=$((pass + 1))
    else
        log "  [FAIL] Only $running services running"
        fail=$((fail + 1))
    fi

    # Test 2: Web UI accessible
    if curl -sf -o /dev/null http://localhost:8980/opennms/login.jsp 2>/dev/null; then
        log "  [PASS] Web UI accessible"
        pass=$((pass + 1))
    else
        log "  [FAIL] Web UI not accessible at http://localhost:8980/opennms"
        fail=$((fail + 1))
    fi

    # Test 3: REST API responds
    if curl -sf -u admin:admin http://localhost:8980/opennms/rest/info 2>/dev/null | grep -q "version"; then
        log "  [PASS] REST API responding"
        pass=$((pass + 1))
    else
        log "  [FAIL] REST API not responding"
        fail=$((fail + 1))
    fi

    # Test 4: Database accessible
    if docker compose exec -T -e PGPASSWORD=opennms postgres psql -U opennms -d opennms -c "SELECT 1" >/dev/null 2>&1; then
        log "  [PASS] PostgreSQL accessible"
        pass=$((pass + 1))
    else
        log "  [FAIL] PostgreSQL not accessible"
        fail=$((fail + 1))
    fi

    # Test 5: Kafka topic exists
    if docker compose exec -T kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list 2>/dev/null | grep -q "opennms"; then
        log "  [PASS] Kafka topics created"
        pass=$((pass + 1))
    else
        log "  [FAIL] Kafka topics not found"
        fail=$((fail + 1))
    fi

    # Test 6: db-init completed successfully
    local db_init_status
    db_init_status=$(docker compose ps db-init --format "{{.State}}" 2>/dev/null || echo "unknown")
    if [ "$db_init_status" = "exited" ]; then
        log "  [PASS] db-init completed (exited)"
        pass=$((pass + 1))
    else
        log "  [FAIL] db-init status: $db_init_status (expected: exited)"
        fail=$((fail + 1))
    fi

    log ""
    log "Results: $pass passed, $fail failed"
    [ "$fail" -eq 0 ] && return 0 || return 1
}

usage() {
    cat <<'USAGE'
Usage: ./deploy.sh <command> [args]

Commands:
  up [profile]    Start services (profiles: lite, passive, full)
  down            Stop services (preserve data volumes)
  reset           Stop and destroy all data (clean slate)
  status          Show service status
  logs [service]  Tail logs (all or specific service)
  shell <service> Open Karaf shell (webapp, pollerd, etc.)
  test            Run deployment verification tests
  test-e2e        Run end-to-end trap-to-alarm integration test
  help            Show this help

Profiles:
  (none)    Infrastructure only: postgres + kafka + db-init + webapp + minion
  lite      + essential daemons (alarmd, pollerd, collectd, notifd, discovery, rtcd, provisiond, bsmd)
  passive   + trap/syslog receivers (alarmd, trapd, syslogd, eventtranslator, provisiond)
  full      All 17 services

Examples:
  ./deploy.sh up                    # Infrastructure only (5 services)
  ./deploy.sh up full               # Start everything (17 services)
  ./deploy.sh up passive            # Trap/syslog receivers with alarmd
  ./deploy.sh up lite               # Webapp + essential daemons
  ./deploy.sh logs alarmd           # Tail alarmd logs
  ./deploy.sh shell webapp          # Karaf shell on webapp
  ./deploy.sh test                  # Verify deployment
  ./deploy.sh test-e2e             # Full trap-to-alarm integration test
  ./deploy.sh test-e2e --verbose   # With Kafka event trace
  ./deploy.sh reset && ./deploy.sh up  # Fresh start
USAGE
}

main() {
    case "${1:-help}" in
        up)      shift; do_up "$@" ;;
        down)    do_down ;;
        reset)   do_reset ;;
        status)  do_status ;;
        logs)    shift; do_logs "$@" ;;
        shell)   shift; do_shell "$@" ;;
        test)    do_test ;;
        test-e2e) shift; "$SCRIPT_DIR/test-e2e.sh" "$@" ;;
        help|-h|--help) usage ;;
        *)       err "Unknown command: $1 (run './deploy.sh help')" ;;
    esac
}

main "$@"
