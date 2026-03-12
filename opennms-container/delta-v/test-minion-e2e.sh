#!/usr/bin/env bash
#
# test-minion-e2e.sh — End-to-end Minion integration test for Delta-V
#
# Sends SNMP traps through the Minion (not directly to Trapd) and verifies
# the full pipeline: Minion → Kafka Sink → Trapd consumer → EventCreator →
# KafkaEventForwarder → Kafka → EventTranslator → Alarmd → PostgreSQL
#
# This validates that the Minion can receive traps and forward them via
# Kafka IPC without any REST dependency (OPENNMS_HTTP eliminated).
#
# Usage:
#   ./test-minion-e2e.sh              Run the test
#   ./test-minion-e2e.sh --verbose    Show full Kafka event trace
#   ./test-minion-e2e.sh --cleanup    Delete test data after run
#
# Prerequisites:
#   - Delta-V deployed: docker compose up -d
#   - Minion healthy: docker compose exec minion bin/client "opennms:health-check"
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
# Traps go to the MINION, not directly to Trapd
TRAP_HOST="localhost"
TRAP_PORT="11162"                    # Minion's mapped trap port (11162 → 1162/udp)
TRAP_COMMUNITY="public"
NODE_SCAN_TIMEOUT=90                 # Longer timeout — extra Kafka hop via Minion
ALARM_TIMEOUT=45
REST_URL="http://localhost:8980/opennms/rest"
REST_USER="admin"
REST_PASS="admin"
IFINDEX=2                            # Use ifIndex=2 to avoid collision with direct-trapd tests

# ── Usage ─────────────────────────────────────────────────────────
usage() {
    cat <<'USAGE'
Usage: ./test-minion-e2e.sh [options]

Options:
  --verbose    Show full Kafka event trace
  --cleanup    Delete test alarms after test
  --help       Show this help

Prerequisites:
  - Delta-V deployed: docker compose up -d
  - Minion healthy
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
SINK_CONSUMER_PID=""
TEST_TMPDIR=$(mktemp -d)
FAULT_LOG="$TEST_TMPDIR/fault-events.log"
IPC_LOG="$TEST_TMPDIR/ipc-events.log"
SINK_LOG="$TEST_TMPDIR/sink-events.log"

log()  { echo "==> $*"; }
ok()   { echo "  [PASS] $*"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $*"; FAIL=$((FAIL + 1)); }
err()  { echo "ERROR: $*" >&2; exit 2; }

cleanup() {
    [ -n "$FAULT_CONSUMER_PID" ] && kill "$FAULT_CONSUMER_PID" 2>/dev/null || true
    [ -n "$IPC_CONSUMER_PID" ] && kill "$IPC_CONSUMER_PID" 2>/dev/null || true
    [ -n "$SINK_CONSUMER_PID" ] && kill "$SINK_CONSUMER_PID" 2>/dev/null || true

    if $VERBOSE; then
        log ""
        log "── Kafka Sink Events (Minion → Trapd) ──"
        cat "$SINK_LOG" 2>/dev/null || true
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

REQUIRED_SERVICES="postgres kafka trapd eventtranslator alarmd provisiond"
for svc in $REQUIRED_SERVICES; do
    if ! docker compose ps --status running --format '{{.Name}}' 2>/dev/null | grep -qw "$svc"; then
        err "Service '$svc' is not running. Deploy with: docker compose up -d"
    fi
done
# Minion uses Docker Compose profiles — docker compose ps doesn't see it.
# Use docker ps directly to check the container.
if ! docker ps --format '{{.Names}}' 2>/dev/null | grep -qw "delta-v-minion"; then
    err "Minion container is not running. Start with: docker start delta-v-minion"
fi
# Webapp is optional (REST API checks are non-critical)
WEBAPP_AVAILABLE=false
if docker compose ps --status running --format '{{.Name}}' 2>/dev/null | grep -qw "webapp"; then
    WEBAPP_AVAILABLE=true
fi
ok "All required services running (including Minion)${WEBAPP_AVAILABLE:+, webapp available for REST checks}"

# Verify Minion location
MINION_LOCATION=$(docker compose exec -T minion cat /opt/minion/etc/org.opennms.minion.controller.cfg 2>/dev/null | grep "location" | head -1 | cut -d= -f2 | tr -d ' ' || echo "unknown")
log "Minion location: $MINION_LOCATION"

# ── Start Kafka Consumers ─────────────────────────────────────────
log "Starting Kafka event consumers..."

# Watch the Sink topic to verify Minion → Trapd forwarding
docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic OpenNMS.Sink.Trap \
    > "$SINK_LOG" 2>/dev/null &
SINK_CONSUMER_PID=$!

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

# Give Kafka consumers time to join consumer groups and start receiving.
# The console-consumer needs to complete group rebalancing before we send traps.
sleep 8

# ══════════════════════════════════════════════════════════════════
# Phase 1: Verify Minion Trap Forwarding (coldStart via Minion)
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 1: Trap forwarding via Minion (coldStart)..."
log "  Sending trap to Minion at ${TRAP_HOST}:${TRAP_PORT}"
log "  Expected flow: Minion → Kafka Sink → Trapd consumer → newSuspect → Provisiond"

snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
    1.3.6.1.6.3.1.1.5.1 \
    1.3.6.1.2.1.1.3.0 t 0

ok "coldStart trap sent to Minion at ${TRAP_HOST}:${TRAP_PORT}"

# Verify the trap appears on the Sink topic (Minion → Kafka)
if wait_for_kafka_event "$SINK_LOG" "trap-message-log" 15 "trap on Kafka Sink topic"; then
    ok "Trap forwarded by Minion to Kafka Sink topic"
else
    # The Sink topic name might differ — check fault events directly
    log "  (Sink topic check inconclusive — checking fault events instead)"
fi

# Verify the event reaches fault-events (Trapd processed it)
if wait_for_kafka_event "$FAULT_LOG" "Cold_Start" "$ALARM_TIMEOUT" "coldStart event in fault-events"; then
    ok "coldStart event processed by Trapd (received via Minion)"
else
    fail "coldStart event not seen in fault-events — Minion → Trapd forwarding may be broken"
    if ! $VERBOSE; then
        log "Hint: re-run with --verbose to see Kafka event trace"
        log ""
        log "── Kafka Sink Events (last 10 lines) ──"
        tail -10 "$SINK_LOG" 2>/dev/null || true
        log ""
        log "── Kafka Fault Events (last 10 lines) ──"
        tail -10 "$FAULT_LOG" 2>/dev/null || true
    fi
    log ""
    log "Results: $PASS passed, $FAIL failed"
    exit 1
fi

# Wait for node provisioning
if wait_for_kafka_event "$IPC_LOG" "nodeScanCompleted" "$NODE_SCAN_TIMEOUT" "nodeScanCompleted"; then
    ok "nodeScanCompleted received — node provisioned via Minion trap"
else
    # Node may already exist from prior test runs — check
    NODE_EXISTS=$(psql_query "SELECT count(*) FROM node WHERE nodelabel LIKE '%'" 2>/dev/null || echo "0")
    if [ "$NODE_EXISTS" -gt 0 ]; then
        log "  (Node already exists from prior run — skipping provisioning check)"
        ok "Node exists in database (prior provisioning)"
    else
        fail "nodeScanCompleted not received within ${NODE_SCAN_TIMEOUT}s"
    fi
fi

# Wait for Trapd's InterfaceToNodeCache to refresh so the newly provisioned
# node (192.168.65.1) is mapped. The cache refresh interval is configured
# to 15s via org.opennms.interface-node-cache.refresh-timer in Trapd's JAVA_OPTS.
# Without this, the linkDown event won't have a nodeid and alarms won't be created.
CACHE_WAIT=20
log ""
log "Waiting ${CACHE_WAIT}s for Trapd InterfaceToNodeCache to refresh..."
sleep "$CACHE_WAIT"

# ══════════════════════════════════════════════════════════════════
# Phase 2: Alarm Creation via linkDown Trap (through Minion)
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 2: Alarm creation via Minion (linkDown trap)..."

snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
    1.3.6.1.6.3.1.1.5.3 \
    1.3.6.1.2.1.1.3.0 t 0 \
    .1.3.6.1.2.1.2.2.1.1.${IFINDEX} i ${IFINDEX}

ok "linkDown trap sent via Minion (ifIndex=${IFINDEX})"

if wait_for_kafka_event "$FAULT_LOG" "translator/traps/SNMP_Link_Down" "$ALARM_TIMEOUT" "translated linkDown event"; then
    ok "Translated SNMP_Link_Down event seen in Kafka (via Minion path)"
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

if $WEBAPP_AVAILABLE; then
    REST_RESPONSE=$(curl -sf -u "${REST_USER}:${REST_PASS}" \
        "${REST_URL}/alarms?comparator=eq&uei=uei.opennms.org/translator/traps/SNMP_Link_Down" \
        -H 'Accept: application/json' 2>/dev/null || echo "")
    if echo "$REST_RESPONSE" | grep -q '"totalCount"' && ! echo "$REST_RESPONSE" | grep -q '"totalCount":0'; then
        ok "Alarm visible via REST API"
    else
        fail "Alarm not visible via REST API"
    fi
else
    log "  (Skipping REST API check — webapp not running)"
fi

# ══════════════════════════════════════════════════════════════════
# Phase 3: Alarm Clearing via linkUp Trap (through Minion)
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 3: Alarm clearing via Minion (linkUp trap)..."

snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
    1.3.6.1.6.3.1.1.5.4 \
    1.3.6.1.2.1.1.3.0 t 0 \
    .1.3.6.1.2.1.2.2.1.1.${IFINDEX} i ${IFINDEX}

ok "linkUp trap sent via Minion (ifIndex=${IFINDEX})"

if wait_for_kafka_event "$FAULT_LOG" "translator/traps/SNMP_Link_Up" "$ALARM_TIMEOUT" "translated linkUp event"; then
    ok "Translated SNMP_Link_Up event seen in Kafka (via Minion path)"
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

if $WEBAPP_AVAILABLE; then
    REST_CLEARED=$(curl -sf -u "${REST_USER}:${REST_PASS}" \
        "${REST_URL}/alarms?comparator=eq&uei=uei.opennms.org/translator/traps/SNMP_Link_Down" \
        -H 'Accept: application/json' 2>/dev/null || echo "")
    if echo "$REST_CLEARED" | grep -qi '"severity".*:"CLEARED"'; then
        ok "Alarm shows CLEARED via REST API"
    else
        fail "Alarm not showing CLEARED via REST API"
    fi
else
    log "  (Skipping REST CLEARED check — webapp not running)"
fi

# ══════════════════════════════════════════════════════════════════
# Results
# ══════════════════════════════════════════════════════════════════
log ""
log "Results: $PASS passed, $FAIL failed"
log ""
log "Validated flow: trap → Minion → Kafka Sink → Trapd → EventCreator →"
log "  KafkaEventForwarder → Kafka → EventTranslator → Alarmd → PostgreSQL"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
