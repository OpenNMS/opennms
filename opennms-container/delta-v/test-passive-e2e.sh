#!/usr/bin/env bash
#
# test-passive-e2e.sh — End-to-end passive service monitoring test for Delta-V
#
# Validates the full passive status pipeline through Minion:
#
#   Phase 0: coldStart trap → Minion → Trapd → newSuspect → Provisiond
#            (LoopDetector assigns GoogleCloud/Azure/AWS, ScriptPolicy sets label)
#   Phase 1: syslog "AWS Down" → Minion → Syslogd → EventTranslator →
#            passiveServiceStatus → PassiveStatusKeeper → Twin API → Minion →
#            PassiveServiceMonitor → Outage + Alarm in PostgreSQL
#   Phase 2: syslog "AWS Up" → same pipeline → Outage closed + Alarm cleared
#
# Usage:
#   ./test-passive-e2e.sh              Run the test
#   ./test-passive-e2e.sh --verbose    Show full Kafka event trace
#   ./test-passive-e2e.sh --cleanup    Delete test data after run
#
# Prerequisites:
#   - Delta-V deployed: docker compose up -d
#   - Minion running
#   - snmptrap (net-snmp) and nc (netcat) available on host
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
TRAP_PORT="11162"                    # Minion's mapped trap port
TRAP_COMMUNITY="public"
SYSLOG_HOST="localhost"
SYSLOG_PORT="1514"                   # Minion's mapped syslog port
NODE_LABEL="The Internet"
NODE_SCAN_TIMEOUT=120
CACHE_WAIT=20                        # InterfaceToNodeCache refresh
PASSIVE_STATUS_TIMEOUT=90
ALARM_TIMEOUT=60
OUTAGE_TIMEOUT=90

# ── Usage ─────────────────────────────────────────────────────────
usage() {
    cat <<'USAGE'
Usage: ./test-passive-e2e.sh [options]

Options:
  --verbose    Show full Kafka event trace
  --cleanup    Delete test alarms after test
  --help       Show this help

Prerequisites:
  - Delta-V deployed: docker compose up -d
  - Minion must be running
  - snmptrap (net-snmp) and nc (netcat) must be installed
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
        psql_query "DELETE FROM alarms WHERE eventuei LIKE '%syslogd/cloud/%'" 2>/dev/null || true
    fi

    docker compose exec -T kafka pkill -f 'kafka-console-consumer' 2>/dev/null || true
    rm -rf "$TEST_TMPDIR"
}
trap cleanup EXIT

send_syslog() {
    local pri="$1"
    local msg="$2"
    local timestamp
    timestamp=$(date '+%b %d %H:%M:%S')
    local hostname
    hostname=$(hostname -s)
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
# Phase 0: Node Provisioning via coldStart Trap through Minion
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 0: Node provisioning (coldStart trap → Minion → Provisiond)..."
log "  Trap destination: Minion at ${TRAP_HOST}:${TRAP_PORT}"
log "  Default foreign source: LoopDetector (GoogleCloud, Azure, AWS) + ScriptPolicy (label)"

# Check if node already exists from a prior run
NODE_EXISTS=$(psql_query "SELECT count(*) FROM node WHERE nodelabel = '${NODE_LABEL}'" 2>/dev/null || echo "0")
if [ "${NODE_EXISTS:-0}" -gt 0 ]; then
    log "  Node '${NODE_LABEL}' already exists from prior run"
    ok "Node exists in database (prior provisioning)"
else
    snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
        1.3.6.1.6.3.1.1.5.1 \
        1.3.6.1.2.1.1.3.0 t 0

    ok "coldStart trap sent to Minion at ${TRAP_HOST}:${TRAP_PORT}"

    # Verify coldStart event reaches fault-events (Minion → Kafka Sink → Trapd)
    if wait_for_kafka_event "$FAULT_LOG" "Cold_Start" 30 "coldStart event in fault-events"; then
        ok "coldStart event processed by Trapd (via Minion)"
    else
        fail "coldStart event not seen in fault-events"
    fi

    # Wait for Provisiond to complete the node scan
    # The scan runs LoopDetectors (adds 3 services) + ScriptPolicy (sets label)
    if wait_for_kafka_event "$IPC_LOG" "nodeScanCompleted" "$NODE_SCAN_TIMEOUT" "nodeScanCompleted"; then
        ok "nodeScanCompleted received — node provisioned via coldStart trap"
    else
        fail "nodeScanCompleted not received within ${NODE_SCAN_TIMEOUT}s"
        log ""
        log "Results: $PASS passed, $FAIL failed"
        exit 1
    fi
fi

# Verify node label was set by ScriptPolicy
ACTUAL_LABEL=$(psql_query "SELECT nodelabel FROM node ORDER BY nodeid DESC LIMIT 1")
if [ "$ACTUAL_LABEL" = "$NODE_LABEL" ]; then
    ok "Node label set to '${NODE_LABEL}' by ScriptPolicy"
else
    fail "Expected label '${NODE_LABEL}', got '${ACTUAL_LABEL}'"
fi

# Verify 3 passive services were added by LoopDetectors
SVC_COUNT=$(psql_query "SELECT count(*) FROM ifservices s JOIN service svc ON s.serviceid = svc.serviceid JOIN ipinterface ip ON s.ipinterfaceid = ip.id JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}' AND svc.servicename IN ('GoogleCloud', 'Azure', 'AWS')")
if [ "${SVC_COUNT:-0}" -ge 3 ]; then
    ok "All 3 passive services exist (GoogleCloud, Azure, AWS)"
else
    fail "Expected 3 passive services, found ${SVC_COUNT:-0}"
    psql_query "SELECT svc.servicename FROM ifservices s JOIN service svc ON s.serviceid = svc.serviceid JOIN ipinterface ip ON s.ipinterfaceid = ip.id JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}'" || true
fi

# Get the node's IP address (dynamically assigned by the Docker host gateway)
NODE_IP=$(psql_query "SELECT ip.ipaddr FROM ipinterface ip JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}' LIMIT 1")
log "  Node IP (Docker host gateway): ${NODE_IP}"

# Wait for Syslogd InterfaceToNodeCache to pick up the new node
log ""
log "Waiting ${CACHE_WAIT}s for Syslogd InterfaceToNodeCache to refresh..."
sleep "$CACHE_WAIT"

# ══════════════════════════════════════════════════════════════════
# Phase 1: AWS Down — Syslog → PassiveStatus → Outage + Alarm
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 1: AWS service down (syslog → passiveServiceStatus → outage + alarm)..."
log "  Sending: CLOUD-STATUS: Service AWS is Down"

# PRI 129 = local0 (facility 16) * 8 + alert (severity 1)
send_syslog 129 "CLOUD-STATUS: Service AWS is Down"

ok "Syslog AWS Down message sent to Minion at ${SYSLOG_HOST}:${SYSLOG_PORT}"

# Verify the cloud/serviceDown event reaches fault-events
if wait_for_kafka_event "$FAULT_LOG" "syslogd/cloud/serviceDown" "$ALARM_TIMEOUT" "cloud/serviceDown event in fault-events"; then
    ok "cloud/serviceDown event seen in Kafka"
else
    fail "cloud/serviceDown event not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

# Verify the translated passiveServiceStatus event
if wait_for_kafka_event "$FAULT_LOG" "passiveServiceStatus" "$PASSIVE_STATUS_TIMEOUT" "passiveServiceStatus event in fault-events"; then
    ok "passiveServiceStatus event seen in Kafka (EventTranslator working)"
else
    fail "passiveServiceStatus event not seen in Kafka within ${PASSIVE_STATUS_TIMEOUT}s"
fi

# Verify alarm created
if wait_for_db "SELECT count(*) FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND alarmtype = 1" "$ALARM_TIMEOUT" "cloud/serviceDown alarm in database"; then
    ALARM_ROW=$(psql_query "SELECT alarmid, severity, alarmtype, reductionkey FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND alarmtype = 1 LIMIT 1")
    ok "Alarm created in PostgreSQL: $ALARM_ROW"
else
    fail "No cloud/serviceDown alarm found in PostgreSQL"
fi

# Verify outage created for AWS service (Pollerd detects Down via PassiveServiceMonitor)
if wait_for_db "SELECT count(*) FROM outages o JOIN ifservices s ON o.ifserviceid = s.id JOIN service svc ON s.serviceid = svc.serviceid JOIN ipinterface ip ON s.ipinterfaceid = ip.id JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}' AND svc.servicename = 'AWS' AND o.ifregainedservice IS NULL" "$OUTAGE_TIMEOUT" "AWS outage in database"; then
    OUTAGE_ROW=$(psql_query "SELECT o.outageid, o.iflostservice FROM outages o JOIN ifservices s ON o.ifserviceid = s.id JOIN service svc ON s.serviceid = svc.serviceid JOIN ipinterface ip ON s.ipinterfaceid = ip.id JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}' AND svc.servicename = 'AWS' AND o.ifregainedservice IS NULL LIMIT 1")
    ok "Outage created for AWS service: $OUTAGE_ROW"
else
    fail "No outage found for AWS service within ${OUTAGE_TIMEOUT}s"
fi

# ══════════════════════════════════════════════════════════════════
# Phase 2: AWS Up — Syslog → PassiveStatus → Outage Closed + Alarm Cleared
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 2: AWS service up (syslog → passiveServiceStatus → outage closed + alarm cleared)..."
log "  Sending: CLOUD-STATUS: Service AWS is Up"

# PRI 134 = local0 (facility 16) * 8 + info (severity 6)
send_syslog 134 "CLOUD-STATUS: Service AWS is Up"

ok "Syslog AWS Up message sent to Minion"

# Verify the cloud/serviceUp event reaches fault-events
if wait_for_kafka_event "$FAULT_LOG" "syslogd/cloud/serviceUp" "$ALARM_TIMEOUT" "cloud/serviceUp event in fault-events"; then
    ok "cloud/serviceUp event seen in Kafka"
else
    fail "cloud/serviceUp event not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

# Verify alarm cleared (severity = 2 = CLEARED)
CLEARED_TIMEOUT=90
if wait_for_db "SELECT count(*) FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND severity = 2" "$CLEARED_TIMEOUT" "alarm CLEARED in database"; then
    CLEARED_ROW=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND severity = 2 LIMIT 1")
    ok "Alarm CLEARED in PostgreSQL: $CLEARED_ROW"
else
    STILL_ACTIVE=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' LIMIT 1")
    if [ -n "$STILL_ACTIVE" ]; then
        fail "Alarm exists but NOT cleared (still: $STILL_ACTIVE)"
    else
        fail "No cloud/serviceDown alarm found in PostgreSQL at all"
    fi
fi

# Verify outage closed (ifRegainedService populated)
if wait_for_db "SELECT count(*) FROM outages o JOIN ifservices s ON o.ifserviceid = s.id JOIN service svc ON s.serviceid = svc.serviceid JOIN ipinterface ip ON s.ipinterfaceid = ip.id JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}' AND svc.servicename = 'AWS' AND o.ifregainedservice IS NOT NULL" "$OUTAGE_TIMEOUT" "AWS outage closed in database"; then
    OUTAGE_CLOSED=$(psql_query "SELECT o.outageid, o.iflostservice, o.ifregainedservice FROM outages o JOIN ifservices s ON o.ifserviceid = s.id JOIN service svc ON s.serviceid = svc.serviceid JOIN ipinterface ip ON s.ipinterfaceid = ip.id JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}' AND svc.servicename = 'AWS' AND o.ifregainedservice IS NOT NULL LIMIT 1")
    ok "Outage CLOSED for AWS service: $OUTAGE_CLOSED"
else
    fail "AWS outage not closed within ${OUTAGE_TIMEOUT}s"
fi

# ══════════════════════════════════════════════════════════════════
# Results
# ══════════════════════════════════════════════════════════════════
log ""
log "Results: $PASS passed, $FAIL failed"
log ""
log "Validated flow:"
log "  Phase 0: coldStart trap → Minion → Trapd → Provisiond (LoopDetector + ScriptPolicy)"
log "  Phase 1: syslog Down → Minion → Syslogd → EventTranslator → passiveServiceStatus →"
log "           PassiveStatusKeeper → Twin API → Minion → PassiveServiceMonitor → Outage + Alarm"
log "  Phase 2: syslog Up → same pipeline → Outage closed + Alarm cleared"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
