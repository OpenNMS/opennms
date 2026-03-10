#!/usr/bin/env bash
#
# test-e2e.sh — End-to-end integration test for Delta-V
#
# Sends SNMP traps through the full microservice pipeline and verifies
# alarm creation/clearing in PostgreSQL and REST API.
#
# Usage:
#   ./test-e2e.sh              Run the test
#   ./test-e2e.sh --verbose    Show full Kafka event trace
#   ./test-e2e.sh --cleanup    Delete test data after run
#
# Prerequisites:
#   - Delta-V deployed with passive profile: ./deploy.sh up passive
#   - snmptrap available on host (net-snmp)
#
# Exit codes:
#   0 = all tests passed
#   1 = test failure
#   2 = prerequisite failure
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ── Configuration ──────────────────────────────────────────────────
TRAP_HOST="localhost"
TRAP_PORT="1162"
TRAP_COMMUNITY="public"
NODE_SCAN_TIMEOUT=60
ALARM_TIMEOUT=30
REST_URL="http://localhost:8980/opennms/rest"
REST_USER="admin"
REST_PASS="admin"
IFINDEX=1

# ── Usage ─────────────────────────────────────────────────────────
usage() {
    cat <<'USAGE'
Usage: ./test-e2e.sh [options]

Options:
  --verbose    Show full Kafka event trace
  --cleanup    Delete test alarms after test
  --help       Show this help

Prerequisites:
  - Deploy with: ./deploy.sh up passive
  - snmptrap must be installed (net-snmp)
USAGE
}

# ── Parse flags ────────────────────────────────────────────────────
VERBOSE=false
CLEANUP=false
for arg in "$@"; do
    case "$arg" in
        --verbose) VERBOSE=true ;;
        --cleanup) CLEANUP=true ;;
        --help|-h) usage; exit 0 ;;
    esac
done

# ── Helpers ────────────────────────────────────────────────────────
PASS=0
FAIL=0
FAULT_CONSUMER_PID=""
IPC_CONSUMER_PID=""
TEST_TMPDIR=$(mktemp -d)
FAULT_LOG="$TEST_TMPDIR/fault-events.log"
IPC_LOG="$TEST_TMPDIR/ipc-events.log"

log()  { echo "==> $*"; }
ok()   { echo "  [PASS] $*"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $*"; FAIL=$((FAIL + 1)); }
err()  { echo "ERROR: $*" >&2; exit 2; }

cleanup() {
    [ -n "$FAULT_CONSUMER_PID" ] && kill "$FAULT_CONSUMER_PID" 2>/dev/null || true
    [ -n "$IPC_CONSUMER_PID" ] && kill "$IPC_CONSUMER_PID" 2>/dev/null || true

    if $VERBOSE; then
        log ""
        log "── Kafka Fault Events ──"
        cat "$FAULT_LOG" 2>/dev/null || true
        log ""
        log "── Kafka IPC Events ──"
        cat "$IPC_LOG" 2>/dev/null || true
    fi

    if $CLEANUP; then
        log "Cleaning up test data..."
        docker compose exec -T -e PGPASSWORD=opennms postgres \
            psql -U opennms -d opennms -q \
            -c "DELETE FROM alarms WHERE eventuei LIKE '%translator/traps/SNMP_Link%'" \
            2>/dev/null || true
    fi

    docker compose exec -T kafka pkill -f 'kafka-console-consumer' 2>/dev/null || true
    rm -rf "$TEST_TMPDIR"
}
trap cleanup EXIT

wait_for_kafka_event() {
    local log_file="$1"
    local pattern="$2"
    local timeout="$3"
    local description="$4"
    local elapsed=0

    log "Waiting for $description (timeout: ${timeout}s)..."
    while [ $elapsed -lt "$timeout" ]; do
        if grep -q "$pattern" "$log_file" 2>/dev/null; then
            return 0
        fi
        sleep 2
        elapsed=$((elapsed + 2))
    done
    return 1
}

psql_query() {
    docker compose exec -T -e PGPASSWORD=opennms postgres \
        psql -U opennms -d opennms -t -A -c "$1" 2>/dev/null
}

# ── Prerequisite Checks ───────────────────────────────────────────
log "Checking prerequisites..."

command -v snmptrap >/dev/null 2>&1 || err "snmptrap not found. Install net-snmp."

REQUIRED_SERVICES="postgres kafka trapd eventtranslator alarmd provisiond webapp"
for svc in $REQUIRED_SERVICES; do
    if ! docker compose ps --status running --format '{{.Name}}' 2>/dev/null | grep -qw "$svc"; then
        err "Service '$svc' is not running. Deploy with: ./deploy.sh up passive"
    fi
done
ok "All required services running"

# ── Start Kafka Consumers ─────────────────────────────────────────
log "Starting Kafka event consumers..."

docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic opennms-fault-events \
    > "$FAULT_LOG" 2>/dev/null &
FAULT_CONSUMER_PID=$!

docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic opennms-ipc-events \
    > "$IPC_LOG" 2>/dev/null &
IPC_CONSUMER_PID=$!

sleep 3

# ══════════════════════════════════════════════════════════════════
# Phase 1: Node Provisioning via coldStart Trap
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 1: Node provisioning (coldStart trap)..."

snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
    1.3.6.1.6.3.1.1.5.1 \
    1.3.6.1.2.1.1.3.0 t 0

ok "coldStart trap sent to ${TRAP_HOST}:${TRAP_PORT}"

if wait_for_kafka_event "$IPC_LOG" "nodeScanCompleted" "$NODE_SCAN_TIMEOUT" "nodeScanCompleted"; then
    ok "nodeScanCompleted received — node provisioned"
else
    fail "nodeScanCompleted not received within ${NODE_SCAN_TIMEOUT}s"
    if ! $VERBOSE; then
        log "Hint: re-run with --verbose to see Kafka event trace"
        log ""
        log "── Kafka Fault Events (last 20 lines) ──"
        tail -20 "$FAULT_LOG" 2>/dev/null || true
        log ""
        log "── Kafka IPC Events (last 20 lines) ──"
        tail -20 "$IPC_LOG" 2>/dev/null || true
    fi
    log ""
    log "Results: $PASS passed, $FAIL failed"
    exit 1
fi

# ══════════════════════════════════════════════════════════════════
# Phase 2: Alarm Creation via linkDown Trap
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 2: Alarm creation (linkDown trap)..."

snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
    1.3.6.1.6.3.1.1.5.3 \
    1.3.6.1.2.1.1.3.0 t 0 \
    .1.3.6.1.2.1.2.2.1.1.${IFINDEX} i ${IFINDEX}

ok "linkDown trap sent (ifIndex=${IFINDEX})"

if wait_for_kafka_event "$FAULT_LOG" "translator/traps/SNMP_Link_Down" "$ALARM_TIMEOUT" "translated linkDown event"; then
    ok "Translated SNMP_Link_Down event seen in Kafka"
else
    fail "Translated SNMP_Link_Down not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

sleep 5

ALARM_ROW=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/translator/traps/SNMP_Link_Down' AND alarmtype = 1 LIMIT 1")
if [ -n "$ALARM_ROW" ]; then
    ok "Alarm created in PostgreSQL: $ALARM_ROW"
else
    fail "No linkDown alarm found in PostgreSQL"
fi

REST_RESPONSE=$(curl -sf -u "${REST_USER}:${REST_PASS}" \
    "${REST_URL}/alarms?comparator=eq&uei=uei.opennms.org/translator/traps/SNMP_Link_Down" \
    -H 'Accept: application/json' 2>/dev/null || echo "")
if echo "$REST_RESPONSE" | grep -q '"totalCount"' && ! echo "$REST_RESPONSE" | grep -q '"totalCount":0'; then
    ok "Alarm visible via REST API"
else
    fail "Alarm not visible via REST API"
fi

# ══════════════════════════════════════════════════════════════════
# Phase 3: Alarm Clearing via linkUp Trap
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 3: Alarm clearing (linkUp trap)..."

snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
    1.3.6.1.6.3.1.1.5.4 \
    1.3.6.1.2.1.1.3.0 t 0 \
    .1.3.6.1.2.1.2.2.1.1.${IFINDEX} i ${IFINDEX}

ok "linkUp trap sent (ifIndex=${IFINDEX})"

if wait_for_kafka_event "$FAULT_LOG" "translator/traps/SNMP_Link_Up" "$ALARM_TIMEOUT" "translated linkUp event"; then
    ok "Translated SNMP_Link_Up event seen in Kafka"
else
    fail "Translated SNMP_Link_Up not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

sleep 5

CLEARED_ROW=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/translator/traps/SNMP_Link_Down' AND severity = 2 LIMIT 1")
if [ -n "$CLEARED_ROW" ]; then
    ok "Alarm cleared in PostgreSQL: $CLEARED_ROW"
else
    STILL_ACTIVE=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/translator/traps/SNMP_Link_Down' LIMIT 1")
    if [ -n "$STILL_ACTIVE" ]; then
        fail "Alarm exists but NOT cleared (still: $STILL_ACTIVE)"
    else
        fail "No linkDown alarm found in PostgreSQL at all"
    fi
fi

REST_CLEARED=$(curl -sf -u "${REST_USER}:${REST_PASS}" \
    "${REST_URL}/alarms?comparator=eq&uei=uei.opennms.org/translator/traps/SNMP_Link_Down" \
    -H 'Accept: application/json' 2>/dev/null || echo "")
if echo "$REST_CLEARED" | grep -qi '"severity".*:"CLEARED"'; then
    ok "Alarm shows CLEARED via REST API"
else
    fail "Alarm not showing CLEARED via REST API"
fi

# ══════════════════════════════════════════════════════════════════
# Results
# ══════════════════════════════════════════════════════════════════
log ""
log "Results: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
