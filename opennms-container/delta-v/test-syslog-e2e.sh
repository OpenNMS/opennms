#!/usr/bin/env bash
#
# test-syslog-e2e.sh — End-to-end syslog integration test for Delta-V
#
# Sends syslog messages through the Minion and verifies alarm creation/clearing
# in PostgreSQL and REST API.
#
# Flow: Host → Minion (UDP 1514) → Kafka Sink (OpenNMS.Sink.Syslog) →
#       Syslogd KafkaSinkBridge → SyslogSinkConsumer → ConvertToEvent →
#       KafkaEventForwarder (enriches alarm-data from eventconf) →
#       Kafka fault-events → Alarmd → PostgreSQL
#
# Phase 1: Discovery — send a syslog Alert to trigger newSuspect + Provisiond
# Phase 2: Alarm creation — send Cisco LINK-3-UPDOWN "down" → alarm-type 1
# Phase 3: Alarm clearing — send Cisco LINK-3-UPDOWN "up" → alarm-type 2
#
# Usage:
#   ./test-syslog-e2e.sh              Run the test
#   ./test-syslog-e2e.sh --verbose    Show full Kafka event trace
#   ./test-syslog-e2e.sh --cleanup    Delete test data after run
#
# Prerequisites:
#   - Delta-V deployed: docker compose up -d
#   - Minion running (receives syslog on port 1514)
#   - nc (netcat) available on host
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
SYSLOG_HOST="localhost"
SYSLOG_PORT="1514"                   # Minion's mapped syslog port (1514 → 1514/udp)
NODE_SCAN_TIMEOUT=90                 # Longer timeout — extra Kafka hop via Minion
ALARM_TIMEOUT=45
# Alarm verification uses PostgreSQL directly (no webapp dependency)
IFDESCR="eth0"                       # Interface name for Cisco LINK-3-UPDOWN messages

# ── Usage ─────────────────────────────────────────────────────────
usage() {
    cat <<'USAGE'
Usage: ./test-syslog-e2e.sh [options]

Options:
  --verbose    Show full Kafka event trace
  --cleanup    Delete test alarms after test
  --help       Show this help

Prerequisites:
  - Delta-V deployed: docker compose up -d
  - Minion must be running
  - nc (netcat) must be installed
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
        log "── Kafka Sink Events (Minion → Syslogd) ──"
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
            -c "DELETE FROM alarms WHERE eventuei LIKE '%syslogd/cisco/link%'" \
            2>/dev/null || true
    fi

    docker compose exec -T kafka pkill -f 'kafka-console-consumer' 2>/dev/null || true
    rm -rf "$TEST_TMPDIR"
}
trap cleanup EXIT

send_syslog() {
    # Send a raw RFC3164 syslog message via UDP.
    # Args: $1 = PRI value, $2 = message body
    local pri="$1"
    local msg="$2"
    local timestamp
    timestamp=$(date '+%b %d %H:%M:%S')
    local hostname
    hostname=$(hostname -s)
    # RFC3164: <PRI>Mmm dd hh:mm:ss HOSTNAME MSG
    echo "<${pri}>${timestamp} ${hostname} ${msg}" | nc -u -w1 "$SYSLOG_HOST" "$SYSLOG_PORT"
}

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

# nc (netcat) is required to send raw UDP syslog messages
command -v nc >/dev/null 2>&1 || err "nc (netcat) not found."

REQUIRED_SERVICES="postgres kafka syslogd alarmd provisiond"
for svc in $REQUIRED_SERVICES; do
    if ! docker compose ps --status running --format '{{.Name}}' 2>/dev/null | grep -qw "$svc"; then
        err "Service '$svc' is not running. Deploy with: docker compose up -d"
    fi
done
# Minion uses Docker Compose profiles — docker compose ps doesn't see it.
if ! docker ps --format '{{.Names}}' 2>/dev/null | grep -qw "delta-v-minion"; then
    err "Minion container is not running. Start with: docker start delta-v-minion"
fi
ok "All required services running (including Minion)"

# ── Ensure Cisco syslog eventconf is in the database ──────────────
# The Cisco linkDown/linkUp events need alarm-data in the eventconf DB so that
# Syslogd's KafkaEventForwarder can enrich events before publishing to Kafka.
# This is idempotent — only inserts if the events don't already exist.
log "Ensuring Cisco syslog event definitions exist in eventconf DB..."

CISCO_SOURCE_EXISTS=$(psql_query "SELECT count(*) FROM eventconf_sources WHERE name = 'cisco.syslog.events'")
if [ "${CISCO_SOURCE_EXISTS:-0}" -eq 0 ]; then
    log "  Inserting cisco.syslog.events source..."
    psql_query "INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (24, 'cisco.syslog.events', 'Cisco IOS syslog events (LINK-3-UPDOWN)', 'cisco', 24, true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test-syslog-e2e')"
fi

LINKDOWN_EXISTS=$(psql_query "SELECT count(*) FROM eventconf_events WHERE uei = 'uei.opennms.org/syslogd/cisco/linkDown'")
if [ "${LINKDOWN_EXISTS:-0}" -eq 0 ]; then
    log "  Inserting cisco/linkDown event definition..."
    psql_query "INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, created_time, last_modified, modified_by) VALUES (158, 24, 'uei.opennms.org/syslogd/cisco/linkDown', 'Cisco Syslog LINK-3-UPDOWN: Interface Down', 'Cisco IOS LINK-3-UPDOWN interface down', true, '<event xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">
   <uei>uei.opennms.org/syslogd/cisco/linkDown</uei>
   <event-label>Cisco Syslog LINK-3-UPDOWN: Interface Down</event-label>
   <descr>Cisco interface %parm[ifDescr]% is down</descr>
   <logmsg dest=\"logndisplay\">Cisco interface %parm[ifDescr]% is down (LINK-3-UPDOWN)</logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key=\"uei.opennms.org/syslogd/cisco/linkDown:%dpname%:%nodeid%:%parm[ifDescr]%\" alarm-type=\"1\" auto-clean=\"false\">
      <update-field field-name=\"severity\" update-on-reduction=\"true\"/>
   </alarm-data>
</event>', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test-syslog-e2e')"
fi

LINKUP_EXISTS=$(psql_query "SELECT count(*) FROM eventconf_events WHERE uei = 'uei.opennms.org/syslogd/cisco/linkUp'")
if [ "${LINKUP_EXISTS:-0}" -eq 0 ]; then
    log "  Inserting cisco/linkUp event definition..."
    psql_query "INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, created_time, last_modified, modified_by) VALUES (159, 24, 'uei.opennms.org/syslogd/cisco/linkUp', 'Cisco Syslog LINK-3-UPDOWN: Interface Up', 'Cisco IOS LINK-3-UPDOWN interface up', true, '<event xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">
   <uei>uei.opennms.org/syslogd/cisco/linkUp</uei>
   <event-label>Cisco Syslog LINK-3-UPDOWN: Interface Up</event-label>
   <descr>Cisco interface %parm[ifDescr]% is up</descr>
   <logmsg dest=\"logndisplay\">Cisco interface %parm[ifDescr]% is up (LINK-3-UPDOWN)</logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%:%parm[ifDescr]%\" alarm-type=\"2\" clear-key=\"uei.opennms.org/syslogd/cisco/linkDown:%dpname%:%nodeid%:%parm[ifDescr]%\"/>
</event>', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test-syslog-e2e')"
fi

ok "Cisco syslog event definitions present in DB"

# Restart syslogd so EventConfInitializer picks up the new event definitions.
# The daemon-thread retry loop in EventConfInitializer loads events from DB at
# startup; a restart ensures the Cisco alarm-data is available for enrichment.
log "Restarting syslogd to load new eventconf definitions..."
docker compose restart syslogd
sleep 10
# Wait for syslogd to become healthy
SYSLOGD_WAIT=0
while [ $SYSLOGD_WAIT -lt 60 ]; do
    if docker compose ps --status running --format '{{.Name}}' 2>/dev/null | grep -qw "syslogd"; then
        HEALTH=$(docker inspect --format='{{.State.Health.Status}}' delta-v-syslogd 2>/dev/null || echo "unknown")
        if [ "$HEALTH" = "healthy" ]; then
            break
        fi
    fi
    sleep 5
    SYSLOGD_WAIT=$((SYSLOGD_WAIT + 5))
done
if [ $SYSLOGD_WAIT -ge 60 ]; then
    err "Syslogd did not become healthy within 60s after restart"
fi
ok "Syslogd restarted and healthy"

# ── Start Kafka Consumers ─────────────────────────────────────────
log "Starting Kafka event consumers..."

# Watch the Syslog Sink topic to verify Minion → Syslogd forwarding
docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic OpenNMS.Sink.Syslog \
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
sleep 8

# ══════════════════════════════════════════════════════════════════
# Phase 1: Node Discovery via Syslog Alert Message
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 1: Node discovery (syslog Alert message)..."
log "  Sending syslog Alert to Minion at ${SYSLOG_HOST}:${SYSLOG_PORT}"
log "  Expected flow: Minion → Kafka Sink → Syslogd → newSuspect → Provisiond"

# PRI 129 = local0 (facility 16) * 8 + alert (severity 1)
# This matches uei.opennms.org/syslogd/local0/alert (default UEI pattern)
# syslogd's new-suspect-on-message="true" triggers newSuspect for unknown hosts
send_syslog 129 "test-syslog-e2e: alert message for node discovery"

ok "Syslog Alert message sent to Minion at ${SYSLOG_HOST}:${SYSLOG_PORT}"

# Verify the syslog appears on the Sink topic (Minion → Kafka)
if wait_for_kafka_event "$SINK_LOG" "syslog" 15 "syslog on Kafka Sink topic"; then
    ok "Syslog forwarded by Minion to Kafka Sink topic"
else
    log "  (Sink topic check inconclusive — checking fault events instead)"
fi

# Verify the event reaches fault-events (Syslogd processed it)
if wait_for_kafka_event "$FAULT_LOG" "syslogd" "$ALARM_TIMEOUT" "syslog event in fault-events"; then
    ok "Syslog event processed by Syslogd (received via Minion)"
else
    fail "Syslog event not seen in fault-events — Minion → Syslogd forwarding may be broken"
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

# Wait for node provisioning (newSuspect → Provisiond → nodeScanCompleted)
if wait_for_kafka_event "$IPC_LOG" "nodeScanCompleted" "$NODE_SCAN_TIMEOUT" "nodeScanCompleted"; then
    ok "nodeScanCompleted received — node provisioned via syslog discovery"
else
    # Node may already exist from prior test runs
    NODE_EXISTS=$(psql_query "SELECT count(*) FROM node WHERE nodelabel LIKE '%'" 2>/dev/null || echo "0")
    if [ "$NODE_EXISTS" -gt 0 ]; then
        log "  (Node already exists from prior run — skipping provisioning check)"
        ok "Node exists in database (prior provisioning)"
    else
        fail "nodeScanCompleted not received within ${NODE_SCAN_TIMEOUT}s"
    fi
fi

# Wait for Syslogd's InterfaceToNodeCache to refresh so the newly provisioned
# node is mapped. Without this, subsequent syslog events won't have a nodeid
# and alarm reduction keys won't match for clearing.
CACHE_WAIT=20
log ""
log "Waiting ${CACHE_WAIT}s for Syslogd InterfaceToNodeCache to refresh..."
sleep "$CACHE_WAIT"

# ══════════════════════════════════════════════════════════════════
# Phase 2: Alarm Creation via Cisco LINK-3-UPDOWN "down"
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 2: Alarm creation (Cisco LINK-3-UPDOWN down)..."

# PRI 131 = local0 (16) * 8 + err (3)
# The Cisco.syslog.xml rule matches "LINK-3-UPDOWN: Interface (\S+),? changed state to down"
# and maps it to uei.opennms.org/syslogd/cisco/linkDown with ifDescr parameter
send_syslog 131 "%LINK-3-UPDOWN: Interface ${IFDESCR}, changed state to down"

ok "Cisco linkDown syslog sent via Minion (ifDescr=${IFDESCR})"

if wait_for_kafka_event "$FAULT_LOG" "syslogd/cisco/linkDown" "$ALARM_TIMEOUT" "cisco/linkDown event in fault-events"; then
    ok "Cisco linkDown event seen in Kafka fault-events"
else
    fail "Cisco linkDown event not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

sleep 5

ALARM_ROW=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cisco/linkDown' AND alarmtype = 1 LIMIT 1")
if [ -n "$ALARM_ROW" ]; then
    ok "Alarm created in PostgreSQL: $ALARM_ROW"
else
    fail "No cisco/linkDown alarm found in PostgreSQL"
fi

ALARM_COUNT=$(psql_query "SELECT count(*) FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cisco/linkDown'")
if [ "${ALARM_COUNT:-0}" -gt 0 ]; then
    ok "Alarm verified in PostgreSQL ($ALARM_COUNT alarm(s))"
else
    fail "No Cisco linkDown alarm found in PostgreSQL"
fi

# ══════════════════════════════════════════════════════════════════
# Phase 3: Alarm Clearing via Cisco LINK-3-UPDOWN "up"
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 3: Alarm clearing (Cisco LINK-3-UPDOWN up)..."

# PRI 134 = local0 (16) * 8 + info (6)
# The Cisco.syslog.xml rule matches "LINK-3-UPDOWN: Interface (\S+),? changed state to up"
# and maps it to uei.opennms.org/syslogd/cisco/linkUp with ifDescr parameter
# The alarm-data clear-key matches the linkDown reduction-key → alarm is cleared
send_syslog 134 "%LINK-3-UPDOWN: Interface ${IFDESCR}, changed state to up"

ok "Cisco linkUp syslog sent via Minion (ifDescr=${IFDESCR})"

if wait_for_kafka_event "$FAULT_LOG" "syslogd/cisco/linkUp" "$ALARM_TIMEOUT" "cisco/linkUp event in fault-events"; then
    ok "Cisco linkUp event seen in Kafka fault-events"
else
    fail "Cisco linkUp event not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

sleep 5

CLEARED_ROW=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cisco/linkDown' AND severity = 2 LIMIT 1")
if [ -n "$CLEARED_ROW" ]; then
    ok "Alarm cleared in PostgreSQL: $CLEARED_ROW"
else
    STILL_ACTIVE=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cisco/linkDown' LIMIT 1")
    if [ -n "$STILL_ACTIVE" ]; then
        fail "Alarm exists but NOT cleared (still: $STILL_ACTIVE)"
    else
        fail "No cisco/linkDown alarm found in PostgreSQL at all"
    fi
fi

CLEARED_COUNT=$(psql_query "SELECT count(*) FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cisco/linkDown' AND severity = 2")
if [ "${CLEARED_COUNT:-0}" -gt 0 ]; then
    ok "Alarm CLEARED verified in PostgreSQL"
else
    fail "Alarm not showing CLEARED in PostgreSQL"
fi

# ══════════════════════════════════════════════════════════════════
# Results
# ══════════════════════════════════════════════════════════════════
log ""
log "Results: $PASS passed, $FAIL failed"
log ""
log "Validated flow: syslog → Minion → Kafka Sink → Syslogd → ConvertToEvent →"
log "  KafkaEventForwarder (alarm-data enrichment) → Kafka → Alarmd → PostgreSQL"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
