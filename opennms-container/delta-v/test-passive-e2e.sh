#!/usr/bin/env bash
#
# test-passive-e2e.sh — End-to-end passive service monitoring test for Delta-V
#
# Validates the full passive status pipeline through Minion:
#
#   Phase 0: coldStart trap → Minion → Trapd → newSuspect → Provisiond (pipeline validation)
#            Then SQL-based provisioning adds passive services + sets node label
#   Phase 1: syslog "AWS Down" → Minion → Syslogd → EventTranslator →
#            passiveServiceStatus → Outage + Alarm in PostgreSQL
#   Phase 2: syslog "AWS Up" → same pipeline → Outage closed + Alarm cleared
#
# Usage:
#   ./test-passive-e2e.sh              Run the test
#   ./test-passive-e2e.sh --verbose    Show full Kafka event trace
#   ./test-passive-e2e.sh --cleanup    Delete test data after run
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# ── Configuration ──────────────────────────────────────────────────
TRAP_HOST="localhost"
TRAP_PORT="11162"
TRAP_COMMUNITY="public"
SYSLOG_HOST="localhost"
SYSLOG_PORT="1514"
NODE_LABEL="The Internet"
NODE_SCAN_TIMEOUT=120
CACHE_WAIT=20
ALARM_TIMEOUT=60
OUTAGE_TIMEOUT=120

# ── Parse flags ────────────────────────────────────────────────────
VERBOSE=false
CLEANUP=false
for arg in "$@"; do
    case "$arg" in
        --verbose) VERBOSE=true ;;
        --cleanup) CLEANUP=true ;;
        --help|-h) echo "Usage: $0 [--verbose] [--cleanup]"; exit 0 ;;
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
        psql_query "DELETE FROM alarms WHERE eventuei LIKE '%syslogd/cloud/%'" 2>/dev/null || true
    fi

    docker compose exec -T kafka pkill -f 'kafka-console-consumer' 2>/dev/null || true
    rm -rf "$TEST_TMPDIR"
}
trap cleanup EXIT

send_syslog() {
    local pri="$1"
    local syslog_host="$2"
    local msg="$3"
    local timestamp
    timestamp=$(date '+%b %d %H:%M:%S')
    # Use the node IP as the syslog hostname so Syslogd's InterfaceToNodeCache
    # can resolve it to a node (critical for getting the correct nodeid in events)
    echo "<${pri}>${timestamp} ${syslog_host} ${msg}" | nc -u -w1 "$SYSLOG_HOST" "$SYSLOG_PORT"
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

wait_for_db() {
    local query="$1"
    local timeout="$2"
    local description="$3"
    local elapsed=0

    log "Waiting for $description (timeout: ${timeout}s)..."
    while [ $elapsed -lt "$timeout" ]; do
        local result
        result=$(psql_query "$query" 2>/dev/null || echo "")
        if [ -n "$result" ] && [ "$result" != "0" ]; then
            return 0
        fi
        sleep 3
        elapsed=$((elapsed + 3))
    done
    return 1
}

# ── Prerequisite Checks ───────────────────────────────────────────
log "Checking prerequisites..."

command -v snmptrap >/dev/null 2>&1 || err "snmptrap not found. Install net-snmp."
command -v nc >/dev/null 2>&1 || err "nc (netcat) not found."

REQUIRED_SERVICES="postgres kafka trapd syslogd eventtranslator alarmd provisiond pollerd"
for svc in $REQUIRED_SERVICES; do
    if ! docker compose ps --status running --format '{{.Name}}' 2>/dev/null | grep -qw "$svc"; then
        err "Service '$svc' is not running. Deploy with: docker compose up -d"
    fi
done
if ! docker ps --format '{{.Names}}' 2>/dev/null | grep -qw "delta-v-minion"; then
    err "Minion container is not running."
fi
ok "All required services running (including Minion)"

# ── Ensure cloud syslog event definitions exist in eventconf DB ──
log "Ensuring cloud status event definitions exist in eventconf DB..."

CLOUD_SOURCE_EXISTS=$(psql_query "SELECT count(*) FROM eventconf_sources WHERE name = 'cloud.status.events'")
if [ "${CLOUD_SOURCE_EXISTS:-0}" -eq 0 ]; then
    log "  Inserting cloud.status.events source..."
    psql_query "INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (30, 'cloud.status.events', 'Cloud service status events (passive monitoring)', 'cloud', 30, true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test-passive-e2e')"
fi

SVCDOWN_EXISTS=$(psql_query "SELECT count(*) FROM eventconf_events WHERE uei = 'uei.opennms.org/syslogd/cloud/serviceDown'")
if [ "${SVCDOWN_EXISTS:-0}" -eq 0 ]; then
    log "  Inserting cloud/serviceDown event definition..."
    psql_query "INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, created_time, last_modified, modified_by) VALUES (200, 30, 'uei.opennms.org/syslogd/cloud/serviceDown', 'Cloud Service Down', 'Cloud service down via syslog', true, '<event xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">
   <uei>uei.opennms.org/syslogd/cloud/serviceDown</uei>
   <event-label>Cloud Service Down</event-label>
   <descr>Cloud service %parm[cloudService]% is down</descr>
   <logmsg dest=\"logndisplay\">Cloud service %parm[cloudService]% is down</logmsg>
   <severity>Minor</severity>
   <alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%:%parm[cloudService]%\" alarm-type=\"1\" auto-clean=\"false\">
      <update-field field-name=\"severity\" update-on-reduction=\"true\"/>
   </alarm-data>
</event>', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test-passive-e2e')"
fi

SVCUP_EXISTS=$(psql_query "SELECT count(*) FROM eventconf_events WHERE uei = 'uei.opennms.org/syslogd/cloud/serviceUp'")
if [ "${SVCUP_EXISTS:-0}" -eq 0 ]; then
    log "  Inserting cloud/serviceUp event definition..."
    psql_query "INSERT INTO eventconf_events(id, source_id, uei, event_label, description, enabled, xml_content, created_time, last_modified, modified_by) VALUES (201, 30, 'uei.opennms.org/syslogd/cloud/serviceUp', 'Cloud Service Up', 'Cloud service up via syslog', true, '<event xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">
   <uei>uei.opennms.org/syslogd/cloud/serviceUp</uei>
   <event-label>Cloud Service Up</event-label>
   <descr>Cloud service %parm[cloudService]% is up</descr>
   <logmsg dest=\"logndisplay\">Cloud service %parm[cloudService]% is up</logmsg>
   <severity>Normal</severity>
   <alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%:%parm[cloudService]%\" alarm-type=\"2\" clear-key=\"uei.opennms.org/syslogd/cloud/serviceDown:%dpname%:%nodeid%:%parm[cloudService]%\"/>
</event>', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test-passive-e2e')"
fi

ok "Cloud status event definitions present in DB"

# Restart syslogd to pick up new eventconf definitions
log "Restarting syslogd to load new event definitions..."
docker compose restart syslogd
SYSLOGD_WAIT=0
while [ $SYSLOGD_WAIT -lt 60 ]; do
    HEALTH=$(docker inspect --format='{{.State.Health.Status}}' delta-v-syslogd 2>/dev/null || echo "unknown")
    if [ "$HEALTH" = "healthy" ]; then break; fi
    sleep 5
    SYSLOGD_WAIT=$((SYSLOGD_WAIT + 5))
done
[ $SYSLOGD_WAIT -ge 60 ] && err "Syslogd did not become healthy within 60s"
ok "Syslogd restarted and healthy"

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

sleep 8

# ══════════════════════════════════════════════════════════════════
# Phase 0a: Validate Minion Trap Pipeline (coldStart trap)
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 0a: Validate Minion trap pipeline (coldStart trap)..."

# Check if a node already exists (from prior test runs)
EXISTING_NODE=$(psql_query "SELECT nodeid FROM node ORDER BY nodeid DESC LIMIT 1")
if [ -n "$EXISTING_NODE" ]; then
    log "  Node already exists (id=${EXISTING_NODE}) — sending coldStart to validate pipeline only"
fi

snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
    1.3.6.1.6.3.1.1.5.1 \
    1.3.6.1.2.1.1.3.0 t 0

ok "coldStart trap sent to Minion at ${TRAP_HOST}:${TRAP_PORT}"

if wait_for_kafka_event "$FAULT_LOG" "Cold_Start" 30 "coldStart event in fault-events"; then
    ok "coldStart event processed by Trapd (via Minion pipeline)"
else
    fail "coldStart event not seen in fault-events"
fi

if [ -z "$EXISTING_NODE" ]; then
    # First run — wait for Provisiond to create the node
    if wait_for_kafka_event "$IPC_LOG" "nodeScanCompleted" "$NODE_SCAN_TIMEOUT" "nodeScanCompleted"; then
        ok "nodeScanCompleted — Provisiond created node from coldStart trap"
    else
        fail "nodeScanCompleted not received within ${NODE_SCAN_TIMEOUT}s"
        log ""
        log "Results: $PASS passed, $FAIL failed"
        exit 1
    fi
else
    ok "Node exists from prior run — skipping provisioning wait"
fi

# Discover the node's IP (Docker host gateway, varies by platform)
NODE_IP=$(psql_query "SELECT ip.ipaddr FROM ipinterface ip JOIN node n ON ip.nodeid = n.nodeid ORDER BY n.nodeid DESC LIMIT 1")
NODE_ID=$(psql_query "SELECT nodeid FROM node ORDER BY nodeid DESC LIMIT 1")
log "  Node: id=${NODE_ID}, ip=${NODE_IP}"

# ══════════════════════════════════════════════════════════════════
# Phase 0b: Add Passive Services + Set Node Label via SQL
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 0b: Adding passive services and setting node label via SQL..."

# Set node label to "The Internet"
psql_query "UPDATE node SET nodelabel = '${NODE_LABEL}' WHERE nodeid = ${NODE_ID}"
ok "Node label set to '${NODE_LABEL}'"

# Create service name records
for SVC in GoogleCloud Azure AWS; do
    psql_query "INSERT INTO service (serviceid, servicename) SELECT nextval('servicenxtid'), '${SVC}' WHERE NOT EXISTS (SELECT 1 FROM service WHERE servicename = '${SVC}')"
done
ok "Service name records created (GoogleCloud, Azure, AWS)"

# Get the interface ID for this node
IFACE_ID=$(psql_query "SELECT id FROM ipinterface WHERE nodeid = ${NODE_ID} LIMIT 1")

# Link passive services to the interface
for SVC in GoogleCloud Azure AWS; do
    SVC_ID=$(psql_query "SELECT serviceid FROM service WHERE servicename = '${SVC}'")
    ALREADY=$(psql_query "SELECT count(*) FROM ifservices WHERE ipinterfaceid = ${IFACE_ID} AND serviceid = ${SVC_ID}")
    if [ "${ALREADY:-0}" -eq 0 ]; then
        psql_query "INSERT INTO ifservices (id, ipinterfaceid, serviceid, status) VALUES (nextval('opennmsnxtid'), ${IFACE_ID}, ${SVC_ID}, 'A')"
    fi
done
ok "Passive services linked to interface ${NODE_IP}"

# Verify
SVC_COUNT=$(psql_query "SELECT count(*) FROM ifservices s JOIN service svc ON s.serviceid = svc.serviceid WHERE s.ipinterfaceid = ${IFACE_ID} AND svc.servicename IN ('GoogleCloud', 'Azure', 'AWS')")
if [ "${SVC_COUNT:-0}" -ge 3 ]; then
    ok "Verified: 3 passive services on node '${NODE_LABEL}' at ${NODE_IP}"
else
    fail "Expected 3 services, found ${SVC_COUNT:-0}"
fi

# Restart syslogd to refresh InterfaceToNodeCache with the new node
log ""
log "Restarting syslogd to refresh InterfaceToNodeCache..."
docker compose restart syslogd
SYSLOGD_WAIT=0
while [ $SYSLOGD_WAIT -lt 60 ]; do
    HEALTH=$(docker inspect --format='{{.State.Health.Status}}' delta-v-syslogd 2>/dev/null || echo "unknown")
    if [ "$HEALTH" = "healthy" ]; then break; fi
    sleep 5
    SYSLOGD_WAIT=$((SYSLOGD_WAIT + 5))
done
ok "Syslogd restarted with updated node cache"

sleep 5

# ══════════════════════════════════════════════════════════════════
# Phase 1: AWS Down — Syslog → EventTranslator → Alarm + Outage
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 1: AWS service down (syslog → passiveServiceStatus → outage + alarm)..."
log "  Sending: CLOUD-STATUS: Service AWS is Down (host=${NODE_IP})"

# PRI 129 = local0.alert — use NODE_IP as syslog hostname so Syslogd resolves it
send_syslog 129 "$NODE_IP" "CLOUD-STATUS: Service AWS is Down"

ok "Syslog AWS Down sent to Minion (source host=${NODE_IP})"

if wait_for_kafka_event "$FAULT_LOG" "syslogd/cloud/serviceDown" "$ALARM_TIMEOUT" "cloud/serviceDown event"; then
    ok "cloud/serviceDown event seen in Kafka"
else
    fail "cloud/serviceDown event not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

if wait_for_kafka_event "$FAULT_LOG" "passiveServiceStatus" "$ALARM_TIMEOUT" "passiveServiceStatus event"; then
    ok "passiveServiceStatus event seen in Kafka (EventTranslator working)"
else
    fail "passiveServiceStatus event not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

if wait_for_db "SELECT count(*) FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND alarmtype = 1" "$ALARM_TIMEOUT" "serviceDown alarm"; then
    ALARM_ROW=$(psql_query "SELECT alarmid, severity, reductionkey FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND alarmtype = 1 LIMIT 1")
    ok "Alarm created in PostgreSQL: $ALARM_ROW"
else
    fail "No cloud/serviceDown alarm found in PostgreSQL"
fi

if wait_for_db "SELECT count(*) FROM outages o JOIN ifservices s ON o.ifserviceid = s.id JOIN service svc ON s.serviceid = svc.serviceid WHERE svc.servicename = 'AWS' AND o.ifregainedservice IS NULL" "$OUTAGE_TIMEOUT" "AWS outage"; then
    ok "Outage created for AWS service"
else
    fail "No AWS outage found within ${OUTAGE_TIMEOUT}s"
fi

# ══════════════════════════════════════════════════════════════════
# Phase 2: AWS Up — Syslog → Alarm Cleared + Outage Closed
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 2: AWS service up (syslog → outage closed + alarm cleared)..."

# PRI 134 = local0.info
send_syslog 134 "$NODE_IP" "CLOUD-STATUS: Service AWS is Up"

ok "Syslog AWS Up sent to Minion"

if wait_for_kafka_event "$FAULT_LOG" "syslogd/cloud/serviceUp" "$ALARM_TIMEOUT" "cloud/serviceUp event"; then
    ok "cloud/serviceUp event seen in Kafka"
else
    fail "cloud/serviceUp event not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

CLEARED_TIMEOUT=90
if wait_for_db "SELECT count(*) FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND severity = 2" "$CLEARED_TIMEOUT" "alarm CLEARED"; then
    ok "Alarm CLEARED in PostgreSQL"
else
    STILL=$(psql_query "SELECT alarmid, severity FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' LIMIT 1")
    if [ -n "$STILL" ]; then
        fail "Alarm exists but NOT cleared (still: $STILL)"
    else
        fail "No alarm found in PostgreSQL"
    fi
fi

if wait_for_db "SELECT count(*) FROM outages o JOIN ifservices s ON o.ifserviceid = s.id JOIN service svc ON s.serviceid = svc.serviceid WHERE svc.servicename = 'AWS' AND o.ifregainedservice IS NOT NULL" "$OUTAGE_TIMEOUT" "AWS outage closed"; then
    ok "Outage CLOSED for AWS service"
else
    fail "AWS outage not closed within ${OUTAGE_TIMEOUT}s"
fi

# ══════════════════════════════════════════════════════════════════
# Results
# ══════════════════════════════════════════════════════════════════
log ""
log "Results: $PASS passed, $FAIL failed"
log ""
log "Validated:"
log "  Phase 0a: coldStart trap → Minion → Trapd → Provisiond (pipeline ✓)"
log "  Phase 0b: SQL provisioning → '${NODE_LABEL}' + GoogleCloud/Azure/AWS"
log "  Phase 1:  syslog Down → EventTranslator → passiveServiceStatus → Alarm + Outage"
log "  Phase 2:  syslog Up → Alarm cleared + Outage closed"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
