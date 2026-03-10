# Delta-V End-to-End Integration Test Design

## Goal

A shell script that exercises the full trap-to-alarm pipeline across multiple
microservice containers, verifying event flow via Kafka and alarm persistence
via PostgreSQL and REST API.

## Profile Changes

Add **eventtranslator** and **provisiond** to the Docker Compose `passive`
profile. Without Provisiond, traps cannot create nodes, and without
EventTranslator, raw SNMP_Link_Down traps lack alarm-data. The updated
`passive` profile becomes a real integration-testable deployment.

**Updated passive profile services:**
postgres, kafka, db-init, webapp, alarmd, trapd, syslogd, eventtranslator,
provisiond

## Test Flow

```
Phase 1 — Node Provisioning (coldStart trap)

  snmptrap coldStart → localhost:1162/udp
    → Trapd (newSuspect-on-trap=true)
    → EventCreator → Kafka opennms-fault-events
    → Provisiond consumes newSuspect → node scan
    → nodeScanCompleted event on Kafka opennms-ipc-events

Phase 2 — Alarm Creation (linkDown trap)

  snmptrap linkDown → localhost:1162/udp
    → Trapd → EventCreator → Kafka opennms-fault-events
    → EventTranslator → enriched SNMP_Link_Down (+alarm-data)
    → KafkaEventForwarder → Kafka opennms-fault-events
    → Alarmd → INSERT alarm into PostgreSQL

Phase 3 — Alarm Clearing (linkUp trap)

  snmptrap linkUp → localhost:1162/udp
    → same pipeline
    → Alarmd → CLEAR alarm in PostgreSQL
```

## Verification Points

### Kafka Event Stream (diagnostic)

A background `kafka-console-consumer.sh` tails both topics
(`opennms-fault-events` and `opennms-ipc-events`) for the duration of the
test. All events are captured to a temp file. Used to:

- Detect `nodeScanCompleted` as the gate before sending linkDown/linkUp traps
- Provide full event trace on failure or with `--verbose` flag

### PostgreSQL (ground truth)

- After linkDown: alarm row exists with matching UEI and alarm type
- After linkUp: alarm is cleared (severity = CLEARED or counter updated)

### REST API (webapp verification)

- After linkDown: `GET /opennms/rest/alarms?uei=...` returns the alarm
- After linkUp: same endpoint shows cleared state

## Script Design

| Aspect | Detail |
|--------|--------|
| Location | `opennms-container/delta-v/test-e2e.sh` |
| Prerequisites | Checks 7 services: postgres, kafka, trapd, eventtranslator, alarmd, provisiond, webapp |
| `--verbose` | Show full Kafka event trace during test |
| `--cleanup` | Delete test alarms and node from PostgreSQL after test |
| Timeouts | 60s for node scan (nodeScanCompleted), 30s for alarm creation, 30s for alarm clearing |
| Exit codes | 0 = all pass, 1 = test failure, 2 = prerequisite failure |

## Tools Used

- `snmptrap` (host, `/usr/bin/snmptrap`) — send SNMP traps to Trapd
- `docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh` — tail Kafka topics
- `docker compose exec postgres psql` — query alarms table
- `curl` — query webapp REST API

## Architecture Decisions

### Why shell script (not Java/Testcontainers)

The original test was manual CLI commands. `snmptrap` is already on the host,
`deploy.sh` establishes the shell script pattern, and spinning up 17
containers via Testcontainers would be very heavy and slow.

### Why Kafka consumer for gating (not REST polling)

Watching the Kafka topic gives visibility into the entire event flow. When
something breaks, you see exactly where events stop flowing. REST polling only
shows the end result and provides no diagnostic value.

### Why both PostgreSQL and REST verification

PostgreSQL is ground truth — alarms exist in the database. REST verification
proves the webapp query layer works too. Testing both catches issues in either
the persistence path or the API layer.
