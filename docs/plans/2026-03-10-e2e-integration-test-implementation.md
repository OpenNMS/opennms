# Delta-V E2E Integration Test Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Create a shell script that sends SNMP traps to verify the full trap-to-alarm pipeline across Delta-V microservice containers.

**Architecture:** Shell script sends coldStart/linkDown/linkUp traps via `snmptrap`, monitors Kafka topics via background `kafka-console-consumer.sh` for event flow diagnostics, and verifies alarm creation/clearing in both PostgreSQL (direct query) and REST API (webapp).

**Tech Stack:** Bash, snmptrap (net-snmp), Docker Compose exec (Kafka tools, psql), curl (REST API).

**Design Doc:** `docs/plans/2026-03-10-e2e-integration-test-design.md`

---

## Task 1: Add eventtranslator and provisiond to passive profile

The `passive` Docker Compose profile currently has trapd, syslogd, and alarmd. Without Provisiond, traps from unknown IPs cannot create nodes. Without EventTranslator, raw `SNMP_Link_Down` events lack alarm-data (it's commented out in the raw eventconf definition — only the translator-enriched version has active alarm-data).

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml`

**Step 1: Add `passive` to eventtranslator profiles**

In `opennms-container/delta-v/docker-compose.yml`, find the `eventtranslator:` service definition. Its `profiles:` line currently reads:

```yaml
    profiles: [full]
```

Change it to:

```yaml
    profiles: [passive, full]
```

**Step 2: Add `passive` to provisiond profiles**

Find the `provisiond:` service definition. Its `profiles:` line currently reads:

```yaml
    profiles: [lite, full]
```

Change it to:

```yaml
    profiles: [lite, passive, full]
```

**Step 3: Update deploy.sh usage text**

In `opennms-container/delta-v/deploy.sh`, find the `usage()` function. Update the `passive` profile description from:

```
  passive   + trap/syslog receivers (alarmd, trapd, syslogd)
```

to:

```
  passive   + trap/syslog receivers (alarmd, trapd, syslogd, eventtranslator, provisiond)
```

**Step 4: Verify**

Run: `grep -A1 'profiles:' opennms-container/delta-v/docker-compose.yml | grep -B1 passive`

Expected: eventtranslator and provisiond now appear in passive profile.

**Step 5: Commit**

```bash
git add opennms-container/delta-v/docker-compose.yml opennms-container/delta-v/deploy.sh
git commit -m "feat: add eventtranslator and provisiond to passive profile

The passive profile needs EventTranslator (raw SNMP_Link_Down has no
alarm-data — only the translator-enriched version does) and Provisiond
(traps from unknown IPs need node provisioning for alarm reduction keys)."
```

---

## Task 2: Create test-e2e.sh script

This is the main integration test script. It exercises the full trap→event→alarm pipeline.

**Files:**
- Create: `opennms-container/delta-v/test-e2e.sh`

**Key technical details the implementer needs:**

### SNMP Trap Commands

The test sends three SNMPv2c traps to `localhost:1162` (mapped to Trapd container port 10162):

**coldStart** (triggers newSuspect → Provisiond node scan):
```bash
snmptrap -v 2c -c public localhost:1162 '' \
    1.3.6.1.6.3.1.1.5.1 \
    1.3.6.1.2.1.1.3.0 t 0
```

**linkDown** (with ifIndex=1 varbind, triggers alarm creation after EventTranslator enrichment):
```bash
snmptrap -v 2c -c public localhost:1162 '' \
    1.3.6.1.6.3.1.1.5.3 \
    1.3.6.1.2.1.1.3.0 t 0 \
    .1.3.6.1.2.1.2.2.1.1.1 i 1
```

**linkUp** (with ifIndex=1 varbind, clears the linkDown alarm):
```bash
snmptrap -v 2c -c public localhost:1162 '' \
    1.3.6.1.6.3.1.1.5.4 \
    1.3.6.1.2.1.1.3.0 t 0 \
    .1.3.6.1.2.1.2.2.1.1.1 i 1
```

### Kafka Event Flow

The script starts a background `kafka-console-consumer.sh` on BOTH topics (comma-separated won't work — need two consumers):
- `opennms-fault-events` — trap events, translated events, alarm-bearing events
- `opennms-ipc-events` — internal events including `nodeScanCompleted`

Start consumers BEFORE sending traps, capture output to temp files:

```bash
docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic opennms-fault-events \
    --from-beginning &
```

**Gate event**: Wait for `nodeScanCompleted` UEI in the IPC topic consumer output:
- UEI: `uei.opennms.org/internal/provisiond/nodeScanCompleted`
- Timeout: 60 seconds
- Poll the temp file with `grep` in a loop

### Alarm Verification

**Translator UEIs** (these are the alarm-bearing events, NOT the raw trap UEIs):
- linkDown alarm: `uei.opennms.org/translator/traps/SNMP_Link_Down`
- linkUp clear: `uei.opennms.org/translator/traps/SNMP_Link_Up`

**Alarm-data from eventconf:**
- linkDown: `alarm-type="1"`, `reduction-key="%uei%:%dpname%:%nodeid%:%parm[#1]%"`
- linkUp: `alarm-type="2"`, `clear-key="uei.opennms.org/translator/traps/SNMP_Link_Down:%dpname%:%nodeid%:%parm[#1]%"`

**PostgreSQL query** (alarm exists after linkDown):
```bash
docker compose exec -T -e PGPASSWORD=opennms postgres \
    psql -U opennms -d opennms -t -A \
    -c "SELECT alarmid, eventuei, severity, alarmtype, reductionkey FROM alarms WHERE eventuei LIKE '%translator/traps/SNMP_Link_Down%'"
```

Expected: One row with `alarmtype=1` (PROBLEM).

**PostgreSQL query** (alarm cleared after linkUp):
```bash
docker compose exec -T -e PGPASSWORD=opennms postgres \
    psql -U opennms -d opennms -t -A \
    -c "SELECT alarmid, eventuei, severity, alarmtype, reductionkey FROM alarms WHERE eventuei LIKE '%translator/traps/SNMP_Link_Down%' AND severity = 3"
```

Expected: The same alarm now has `severity=3` (CLEARED, from OnmsSeverity enum: INDETERMINATE=1, CLEARED=2... wait — check the enum).

**OnmsSeverity enum values** (from `OnmsSeverity.java`):
- INDETERMINATE=1, CLEARED=2, NORMAL=3, WARNING=4, MINOR=5, MAJOR=6, CRITICAL=7

So after clearing: `severity=2` (CLEARED).

**REST API query** (alarm exists after linkDown):
```bash
curl -sf -u admin:admin \
    'http://localhost:8980/opennms/rest/alarms?comparator=eq&uei=uei.opennms.org/translator/traps/SNMP_Link_Down' \
    -H 'Accept: application/json'
```

Expected: JSON with `totalCount >= 1` and alarm with `type=1`.

**REST API query** (alarm cleared after linkUp):

Same URL, check that the alarm's severity is now `CLEARED`.

### Cleanup (--cleanup flag)

Delete test data from PostgreSQL:
```bash
docker compose exec -T -e PGPASSWORD=opennms postgres \
    psql -U opennms -d opennms \
    -c "DELETE FROM alarms WHERE eventuei LIKE '%translator/traps/SNMP_Link%'"
```

### Required Services Check

Before running tests, verify these services are running:
```bash
for svc in postgres kafka trapd eventtranslator alarmd provisiond webapp; do
    docker compose ps --status running --format '{{.Name}}' | grep -q "$svc" || fail
done
```

### Step 1: Create the script

Create `opennms-container/delta-v/test-e2e.sh` with the following structure:

```bash
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
NODE_SCAN_TIMEOUT=60        # seconds to wait for nodeScanCompleted
ALARM_TIMEOUT=30            # seconds to wait for alarm creation/clearing
REST_URL="http://localhost:8980/opennms/rest"
REST_USER="admin"
REST_PASS="admin"
IFINDEX=1                   # interface index for linkDown/linkUp traps

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
TMPDIR=$(mktemp -d)
FAULT_LOG="$TMPDIR/fault-events.log"
IPC_LOG="$TMPDIR/ipc-events.log"

log()  { echo "==> $*"; }
ok()   { echo "  [PASS] $*"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $*"; FAIL=$((FAIL + 1)); }
err()  { echo "ERROR: $*" >&2; exit 2; }

cleanup() {
    # Kill background consumers
    kill "$FAULT_CONSUMER_PID" 2>/dev/null || true
    kill "$IPC_CONSUMER_PID" 2>/dev/null || true

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

    rm -rf "$TMPDIR"
}
trap cleanup EXIT

usage() {
    cat <<'USAGE'
Usage: ./test-e2e.sh [options]

Options:
  --verbose    Show full Kafka event trace
  --cleanup    Delete test alarms/node after test
  --help       Show this help

Prerequisites:
  - Deploy with: ./deploy.sh up passive
  - snmptrap must be installed (net-snmp)
USAGE
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

command -v snmptrap >/dev/null 2>&1 || err "snmptrap not found. Install net-snmp."

REQUIRED_SERVICES="postgres kafka trapd eventtranslator alarmd provisiond webapp"
for svc in $REQUIRED_SERVICES; do
    if ! docker compose ps --status running --format '{{.Name}}' 2>/dev/null | grep -q "$svc"; then
        err "Service '$svc' is not running. Deploy with: ./deploy.sh up passive"
    fi
done
ok "All required services running"

# ── Start Kafka Consumers ─────────────────────────────────────────
log "Starting Kafka event consumers..."

docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic opennms-fault-events \
    --from-latest \
    > "$FAULT_LOG" 2>/dev/null &
FAULT_CONSUMER_PID=$!

docker compose exec -T kafka /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 \
    --topic opennms-ipc-events \
    --from-latest \
    > "$IPC_LOG" 2>/dev/null &
IPC_CONSUMER_PID=$!

# Give consumers time to subscribe
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

# Wait for translated event in Kafka
if wait_for_kafka_event "$FAULT_LOG" "translator/traps/SNMP_Link_Down" "$ALARM_TIMEOUT" "translated linkDown event"; then
    ok "Translated SNMP_Link_Down event seen in Kafka"
else
    fail "Translated SNMP_Link_Down not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

# Wait a moment for Alarmd to process
sleep 5

# Verify alarm in PostgreSQL
ALARM_ROW=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/translator/traps/SNMP_Link_Down' AND alarmtype = 1 LIMIT 1")
if [ -n "$ALARM_ROW" ]; then
    ok "Alarm created in PostgreSQL: $ALARM_ROW"
else
    fail "No linkDown alarm found in PostgreSQL"
fi

# Verify alarm via REST API
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

# Wait for translated event in Kafka
if wait_for_kafka_event "$FAULT_LOG" "translator/traps/SNMP_Link_Up" "$ALARM_TIMEOUT" "translated linkUp event"; then
    ok "Translated SNMP_Link_Up event seen in Kafka"
else
    fail "Translated SNMP_Link_Up not seen in Kafka within ${ALARM_TIMEOUT}s"
fi

# Wait a moment for Alarmd to process
sleep 5

# Verify alarm cleared in PostgreSQL (severity=2 is CLEARED)
CLEARED_ROW=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/translator/traps/SNMP_Link_Down' AND severity = 2 LIMIT 1")
if [ -n "$CLEARED_ROW" ]; then
    ok "Alarm cleared in PostgreSQL: $CLEARED_ROW"
else
    # Check if alarm still exists but not cleared
    STILL_ACTIVE=$(psql_query "SELECT alarmid, severity, alarmtype FROM alarms WHERE eventuei = 'uei.opennms.org/translator/traps/SNMP_Link_Down' LIMIT 1")
    if [ -n "$STILL_ACTIVE" ]; then
        fail "Alarm exists but NOT cleared (still: $STILL_ACTIVE)"
    else
        fail "No linkDown alarm found in PostgreSQL at all"
    fi
fi

# Verify alarm cleared via REST API
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
```

Make the script executable:

```bash
chmod +x opennms-container/delta-v/test-e2e.sh
```

**Step 2: Verify syntax**

Run: `bash -n opennms-container/delta-v/test-e2e.sh`

Expected: No output (no syntax errors).

**Step 3: Commit**

```bash
git add opennms-container/delta-v/test-e2e.sh
git commit -m "feat: add e2e integration test for trap-to-alarm pipeline

Sends coldStart → linkDown → linkUp SNMP traps and verifies:
- Node provisioned via nodeScanCompleted on Kafka IPC topic
- Alarm created in PostgreSQL and REST API after linkDown
- Alarm cleared in PostgreSQL and REST API after linkUp

Usage: ./test-e2e.sh [--verbose] [--cleanup]
Requires: ./deploy.sh up passive"
```

---

## Task 3: Wire test-e2e into deploy.sh

Add a `test-e2e` command to `deploy.sh` so users can run `./deploy.sh test-e2e` alongside the existing `./deploy.sh test` health check.

**Files:**
- Modify: `opennms-container/delta-v/deploy.sh`

**Step 1: Add test-e2e case to main()**

In `opennms-container/delta-v/deploy.sh`, find the `main()` function's `case` statement. Add a new case for `test-e2e`:

After the line:
```bash
        test)    do_test ;;
```

Add:
```bash
        test-e2e) shift; "$SCRIPT_DIR/test-e2e.sh" "$@" ;;
```

**Step 2: Update usage() text**

In the `usage()` function, after the `test` command entry:
```
  test            Run deployment verification tests
```

Add:
```
  test-e2e        Run end-to-end trap-to-alarm integration test
```

And in the Examples section, add:
```
  ./deploy.sh test-e2e             # Full trap-to-alarm integration test
  ./deploy.sh test-e2e --verbose   # With Kafka event trace
```

**Step 3: Verify**

Run: `./deploy.sh help`

Expected: `test-e2e` appears in the command list and examples.

**Step 4: Commit**

```bash
git add opennms-container/delta-v/deploy.sh
git commit -m "feat: wire test-e2e command into deploy.sh

./deploy.sh test-e2e [--verbose] [--cleanup] runs the full
trap-to-alarm integration test against a passive deployment."
```
