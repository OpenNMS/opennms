#!/usr/bin/env bash
#
# test-passive-e2e.sh — End-to-end passive service monitoring test for Delta-V
#
# Validates the full passive status pipeline through Minion:
#
#   Phase 0: Requisition import provisions "The Internet" node with 3 passive
#            services (GoogleCloud, Azure, AWS). coldStart trap validates the
#            Minion trap pipeline.
#   Phase 1: syslog "AWS Down" → Minion → Syslogd → EventTranslator →
#            passiveServiceStatus → PassiveStatusKeeper → Twin API → Minion →
#            PassiveServiceMonitor → Outage + Alarm in PostgreSQL
#   Phase 2: syslog "AWS Up" → same pipeline → Outage closed + Alarm cleared
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
FOREIGN_SOURCE="cloud-services"
PROVISION_TIMEOUT=120
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

wait_for_healthy() {
    local container="$1"
    local elapsed=0
    while [ $elapsed -lt 60 ]; do
        HEALTH=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "unknown")
        if [ "$HEALTH" = "healthy" ]; then return 0; fi
        sleep 5
        elapsed=$((elapsed + 5))
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
    psql_query "INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (30, 'cloud.status.events', 'Cloud service status events (passive monitoring)', 'cloud', 30, true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'test-passive-e2e')"
fi

SVCDOWN_EXISTS=$(psql_query "SELECT count(*) FROM eventconf_events WHERE uei = 'uei.opennms.org/syslogd/cloud/serviceDown'")
if [ "${SVCDOWN_EXISTS:-0}" -eq 0 ]; then
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

# ── Discover Docker Host Gateway IP ──────────────────────────────
# The gateway IP is the source address Minion sees for syslog/trap packets
# from the host. We need this for the requisition and syslog hostname.
# Parse gateway from /proc/net/route (works in minimal containers without iproute2)
GW_HEX=$(docker compose exec -T minion cat /proc/net/route 2>/dev/null | awk '$2=="00000000" {print $3}')
if [ -n "$GW_HEX" ]; then
    NODE_IP=$(printf "%d.%d.%d.%d" "0x${GW_HEX:6:2}" "0x${GW_HEX:4:2}" "0x${GW_HEX:2:2}" "0x${GW_HEX:0:2}")
fi
if [ -z "$NODE_IP" ]; then
    # Fallback: check if a node already exists from prior run
    NODE_IP=$(psql_query "SELECT ip.ipaddr FROM ipinterface ip JOIN node n ON ip.nodeid = n.nodeid LIMIT 1")
fi
if [ -z "$NODE_IP" ]; then
    err "Could not determine Docker host gateway IP"
fi
log "Docker host gateway IP: ${NODE_IP}"

# ══════════════════════════════════════════════════════════════════
# Phase 0: Provision "The Internet" via Requisition Import
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 0: Provisioning '${NODE_LABEL}' via requisition import..."

# Check if node already exists with correct services from prior run
EXISTING_SVC=$(psql_query "SELECT count(*) FROM ifservices s JOIN service svc ON s.serviceid = svc.serviceid JOIN ipinterface ip ON s.ipinterfaceid = ip.id JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}' AND svc.servicename IN ('GoogleCloud', 'Azure', 'AWS')")
if [ "${EXISTING_SVC:-0}" -ge 3 ]; then
    log "  Node '${NODE_LABEL}' with 3 passive services already exists"
    ok "Node provisioned (prior run)"
else
    # Write the foreign source (no detectors — services declared in requisition)
    cat > "$TEST_TMPDIR/cloud-services-fs.xml" <<FSEOF
<?xml version="1.0" encoding="UTF-8"?>
<foreign-source xmlns="http://xmlns.opennms.org/xsd/config/foreign-source" name="${FOREIGN_SOURCE}">
    <scan-interval>1d</scan-interval>
    <detectors/>
    <policies/>
</foreign-source>
FSEOF

    # Write the requisition with the dynamic node IP and explicit services
    cat > "$TEST_TMPDIR/cloud-services-req.xml" <<REQEOF
<?xml version="1.0" encoding="UTF-8"?>
<model-import xmlns="http://xmlns.opennms.org/xsd/config/model-import"
              foreign-source="${FOREIGN_SOURCE}"
              date-stamp="$(date -u +%Y-%m-%dT%H:%M:%S.000Z)">
    <node foreign-id="the-internet" node-label="${NODE_LABEL}">
        <interface ip-addr="${NODE_IP}" status="1" managed="true" snmp-primary="N">
            <monitored-service service-name="GoogleCloud"/>
            <monitored-service service-name="Azure"/>
            <monitored-service service-name="AWS"/>
        </interface>
    </node>
</model-import>
REQEOF

    log "  Requisition: ${NODE_LABEL} at ${NODE_IP} with GoogleCloud, Azure, AWS"

    # Write files to the HOST overlay directory (mounted read-only into the container).
    # On restart, the container entrypoint copies from overlay → /opt/sentinel/etc/.
    mkdir -p provisiond-overlay/etc/foreign-sources provisiond-overlay/etc/imports
    cp "$TEST_TMPDIR/cloud-services-fs.xml" "provisiond-overlay/etc/foreign-sources/${FOREIGN_SOURCE}.xml"
    cp "$TEST_TMPDIR/cloud-services-req.xml" "provisiond-overlay/etc/imports/${FOREIGN_SOURCE}.xml"

    # Add a requisition-def so Provisiond auto-imports the requisition on startup
    cat > provisiond-overlay/etc/provisiond-configuration.xml <<PROVEOF
<?xml version="1.0" encoding="UTF-8"?>
<provisiond-configuration xmlns="http://xmlns.opennms.org/xsd/config/provisiond-configuration"
  foreign-source-dir="/opt/sentinel/etc/foreign-sources"
  requistion-dir="/opt/sentinel/etc/imports"
  importThreads="4" scanThreads="4" rescanThreads="4" writeThreads="4" >
  <requisition-def import-name="${FOREIGN_SOURCE}"
                   import-url-resource="file:///opt/sentinel/etc/imports/${FOREIGN_SOURCE}.xml">
    <cron-schedule>0 0 0 * * ? 2099</cron-schedule>
  </requisition-def>
</provisiond-configuration>
PROVEOF
    ok "Requisition, foreign source, and provisiond config written to host overlay"

    # Restart Provisiond to trigger the import
    log "  Restarting Provisiond to import requisition..."
    docker compose restart provisiond
    wait_for_healthy delta-v-provisiond || err "Provisiond not healthy after restart"
    ok "Provisiond restarted"

    # Wait for node to appear in database
    if wait_for_db "SELECT count(*) FROM node WHERE nodelabel = '${NODE_LABEL}'" "$PROVISION_TIMEOUT" "node '${NODE_LABEL}' in database"; then
        ok "Node '${NODE_LABEL}' provisioned"
    else
        fail "Node '${NODE_LABEL}' not found after ${PROVISION_TIMEOUT}s"
        log ""
        log "Results: $PASS passed, $FAIL failed"
        exit 1
    fi

    # Verify services
    SVC_COUNT=$(psql_query "SELECT count(*) FROM ifservices s JOIN service svc ON s.serviceid = svc.serviceid JOIN ipinterface ip ON s.ipinterfaceid = ip.id JOIN node n ON ip.nodeid = n.nodeid WHERE n.nodelabel = '${NODE_LABEL}' AND svc.servicename IN ('GoogleCloud', 'Azure', 'AWS')")
    if [ "${SVC_COUNT:-0}" -ge 3 ]; then
        ok "All 3 passive services provisioned (GoogleCloud, Azure, AWS)"
    else
        fail "Expected 3 services, found ${SVC_COUNT:-0}"
    fi
fi

# Restart daemons to pick up the new node:
# - Syslogd: InterfaceToNodeCache needs the new node IP
# - EventTranslator: needs cloud→passiveServiceStatus translation specs
# - Pollerd: needs to schedule polls for the new passive services
log ""
log "Restarting syslogd, eventtranslator, pollerd for new node..."
docker compose restart syslogd eventtranslator pollerd

wait_for_healthy delta-v-syslogd && ok "Syslogd healthy" || fail "Syslogd not healthy"
wait_for_healthy delta-v-eventtranslator && ok "EventTranslator healthy" || fail "EventTranslator not healthy"
wait_for_healthy delta-v-pollerd && ok "Pollerd healthy" || fail "Pollerd not healthy"

# Give Pollerd time to schedule polls and Twin API to sync
log "Waiting 15s for Pollerd service discovery and Twin API sync..."
sleep 15

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

# ── Validate Minion Trap Pipeline (coldStart) ─────────────────────
log ""
log "Validating Minion trap pipeline (coldStart trap)..."

snmptrap -v 2c -c "$TRAP_COMMUNITY" "${TRAP_HOST}:${TRAP_PORT}" '' \
    1.3.6.1.6.3.1.1.5.1 \
    1.3.6.1.2.1.1.3.0 t 0

ok "coldStart trap sent to Minion at ${TRAP_HOST}:${TRAP_PORT}"

if wait_for_kafka_event "$FAULT_LOG" "Cold_Start" 30 "coldStart event in fault-events"; then
    ok "coldStart event processed by Trapd (Minion pipeline validated)"
else
    fail "coldStart event not seen in fault-events"
fi

# ══════════════════════════════════════════════════════════════════
# Phase 1: AWS Down — Syslog → EventTranslator → Alarm + Outage
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 1: AWS service down..."
log "  Sending: CLOUD-STATUS: Service AWS is Down (host=${NODE_IP})"

send_syslog 129 "$NODE_IP" "CLOUD-STATUS: Service AWS is Down"
ok "Syslog AWS Down sent to Minion"

if wait_for_kafka_event "$FAULT_LOG" "syslogd/cloud/serviceDown" "$ALARM_TIMEOUT" "cloud/serviceDown event"; then
    ok "cloud/serviceDown event seen in Kafka"
else
    fail "cloud/serviceDown not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

if wait_for_kafka_event "$FAULT_LOG" "passiveServiceStatus" "$ALARM_TIMEOUT" "passiveServiceStatus event"; then
    ok "passiveServiceStatus event seen (EventTranslator working)"
else
    fail "passiveServiceStatus not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

if wait_for_db "SELECT count(*) FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND alarmtype = 1" "$ALARM_TIMEOUT" "serviceDown alarm"; then
    ALARM_ROW=$(psql_query "SELECT alarmid, severity, reductionkey FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND alarmtype = 1 LIMIT 1")
    ok "Alarm created: $ALARM_ROW"
else
    fail "No serviceDown alarm in PostgreSQL"
fi

if wait_for_db "SELECT count(*) FROM outages o JOIN ifservices s ON o.ifserviceid = s.id JOIN service svc ON s.serviceid = svc.serviceid WHERE svc.servicename = 'AWS' AND o.ifregainedservice IS NULL" "$OUTAGE_TIMEOUT" "AWS outage"; then
    ok "Outage created for AWS service"
else
    fail "No AWS outage within ${OUTAGE_TIMEOUT}s"
fi

# ══════════════════════════════════════════════════════════════════
# Phase 2: AWS Up — Syslog → Alarm Cleared + Outage Closed
# ══════════════════════════════════════════════════════════════════
log ""
log "Phase 2: AWS service up..."

send_syslog 134 "$NODE_IP" "CLOUD-STATUS: Service AWS is Up"
ok "Syslog AWS Up sent to Minion"

if wait_for_kafka_event "$FAULT_LOG" "syslogd/cloud/serviceUp" "$ALARM_TIMEOUT" "cloud/serviceUp event"; then
    ok "cloud/serviceUp event seen in Kafka"
else
    fail "cloud/serviceUp not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

if wait_for_db "SELECT count(*) FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' AND severity = 2" 90 "alarm CLEARED"; then
    ok "Alarm CLEARED in PostgreSQL"
else
    STILL=$(psql_query "SELECT alarmid, severity FROM alarms WHERE eventuei = 'uei.opennms.org/syslogd/cloud/serviceDown' LIMIT 1")
    if [ -n "$STILL" ]; then
        fail "Alarm not cleared (still: $STILL)"
    else
        fail "No alarm found"
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
log "  Phase 0: Requisition import → '${NODE_LABEL}' + GoogleCloud/Azure/AWS"
log "           coldStart trap → Minion → Trapd (pipeline ✓)"
log "  Phase 1: syslog Down → EventTranslator → passiveServiceStatus → Alarm + Outage"
log "  Phase 2: syslog Up → Alarm cleared + Outage closed"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
