# OpenNMS Delta-V

**Delta-V** is a microservice decomposition of [OpenNMS Horizon][], transforming the monolithic Java application into 19+ independently deployable Kafka-connected containers.

> For the original OpenNMS Horizon project description, see [OPENNMS.md](OPENNMS.md).

## Plan Status Dashboard

### Complete (29 docs)

| Date | Plan | Key Achievement |
|------|------|-----------------|
| 03-02 | EventBus Redesign (design + impl + phase2) | Kafka-backed fault events, TSID generation, Alarmd extraction |
| 03-05 | KafkaEventForwarder (design + impl + OSGi) | Per-daemon event enrichment + Kafka publish, no centralized Eventd |
| 03-05 | Karaf-Only Daemon Assembly | Daemon-loader bundle pattern established |
| 03-07 | Strike Fighter Completion (design + impl) | **18/18 tasks**, 4 dead daemons deleted, 8 daemons extracted |
| 03-08 | Feature Removal (design + impl) | Tl1d, Charts, Device Config Backup, Database Reports/Jasper all deleted |
| 03-08 | Enlinkd & Scriptd Extraction | Both running as standalone containers |
| 03-10 | E2E Integration Test | `test-e2e.sh` — 11 tests, 3 phases all passing |
| 03-10 | Project Status Analysis | Snapshot: 100% Strike Fighter, 100% Phase A |
| 03-11 | Db-Init Extraction | Spring Boot 4.0.3 app, 312 MB image (vs 35.6 GB Horizon) |
| 03-12 | Minion E2E Pipeline Report | 13/13 tests passing, 3 race-condition bugs fixed |
| 03-12 | Minion-Mandatory RPC Migration | **All 6 daemons migrated** to real KafkaRpcClientFactory (PR #17) |
| 03-12 | PerspectivePollerd Cleanup | Standalone container running healthy (TSID=7, PR #15) |
| 03-13 | Minion-Only Listeners (design + impl) | Eventd/DHCP deleted, Syslogd KafkaSinkBridge, Telemetryd container (TSID=18) |
| 03-14 | Java 21 Runtime Upgrade (design + impl) | Karaf 4.4.9, Felix 7.0.5, OSGi R8, Pax Web 8.0 — all daemons + Minion on JRE 21 |
| 03-14 | Webapp Elimination from Test Pipeline | E2E tests use SQL-only verification, webapp removed from docker-compose |
| 03-11 | EventDao/Notifd/Minion REST Elimination | EventDao/OnmsEvent deleted, Notifd eliminated, Minion REST replaced with Twin API, SNMPv3 Twin publisher |

### Superseded (2 docs)

| Date | Plan | Superseded By |
|------|------|---------------|
| 03-02 | EventBus Follow-ups | Later phases (Vacuumd deleted, not migrated) |
| 03-07 | Strike Fighter Design | Exceeded — 17 services achieved vs. 15 planned |

### In Progress / Partial (1 doc)

| Date | Plan | Remaining Work |
|------|------|----------------|
| 03-09 | Microservice Event Architecture | Two-topic Kafka design working; full IPC flow documented |

### Deferred (2 docs)

| Date | Plan | Reason |
|------|------|--------|
| 03-09 | Minion-Mandatory Architecture | Non-distributable monitors, collector delegation gaps |
| 03-12 | Next-session prompts (×2) | Handoff docs for future sessions |

### Architectural Milestones Achieved

1. **Events table eliminated** — events never touch PostgreSQL
2. **ActiveMQ eliminated** — all IPC via Kafka
3. **Core container eliminated** — replaced by lightweight `db-init` Spring Boot app
4. **19 standalone daemon containers** running on `opennms/daemon` image
5. **Minion RPC mandatory** — all 6 polling/collection daemons use real Kafka RPC
6. **End-to-end validated** — both direct (11 tests) and Minion (13 tests) pipelines passing
7. **Legacy features removed** — Tl1d, Charts, Device Config Backup, Database Reports/Jasper, DHCP monitor all deleted
8. **Minion-only network ingress** — Eventd listeners deleted, Syslogd/Telemetryd consume via KafkaSinkBridge from Minion
9. **Java 21 runtime** — all daemon + Minion containers run JRE 21 with Karaf 4.4.9, Felix 7.0.5, OSGi R8, Pax Web 8.0
10. **Webapp eliminated from test pipeline** — E2E tests verify via PostgreSQL directly, no 43GB Horizon image needed

### Remaining Work

All planned feature work is complete or deferred by design. The deferred Minion-Mandatory Architecture requires prerequisite work on non-distributable ServiceMonitors and collector delegation.

All plan documents are in [`docs/plans/`](docs/plans/).

---

## What Is Delta-V?

OpenNMS Horizon is an enterprise-grade open-source network monitoring platform. Delta-V restructures it from a single 35.6 GB monolith into lean, focused microservices:

- **Each daemon runs in its own container** — independent scaling, isolation, and restartability
- **Kafka-only event transport** — no ActiveMQ, no shared event bus
- **Events never touch PostgreSQL** — only alarms are persisted to the database
- **2 Docker images** serve all daemon roles — `opennms/daemon` (all 17 daemon types, JRE 21), `opennms/minion` (distributed collection, JRE 21)
- **One-shot database initialization** — `opennms/db-init` (312 MB) replaces the Core container for schema setup

## Architecture

```
Minion → Kafka Sink → Trapd/Syslogd
                          ↓
            KafkaEventForwarder → opennms-fault-events (Kafka)
                                        ↓
                        ┌───────────────┼───────────────┐
                        ↓               ↓               ↓
                    Alarmd      EventTranslator    All Daemons
                   (→ PostgreSQL)  (→ translate     (subscribe to
                                    → re-publish)   relevant events)
                                        ↓
                              opennms-ipc-events (Kafka)
                                        ↓
                              Provisiond, Discovery, etc.
```

### Services

| Service | Image | TSID | Purpose |
|---------|-------|------|---------|
| alarmd | opennms/daemon | 2 | Kafka → alarm creation/reduction → PostgreSQL |
| pollerd | opennms/daemon | 4 | Service availability polling |
| collectd | opennms/daemon | 5 | Performance data collection |
| rtcd | opennms/daemon | 6 | Response time collection daemon |
| perspectivepollerd | opennms/daemon | 7 | Perspective (remote location) polling |
| discovery | opennms/daemon | 9 | Network discovery |
| trapd | opennms/daemon | 10 | SNMP trap reception (via Minion Kafka Sink) |
| syslogd | opennms/daemon | 11 | Syslog reception (via Minion Kafka Sink) |
| ticketer | opennms/daemon | 12 | Trouble ticket integration |
| eventtranslator | opennms/daemon | 13 | Event translation rules |
| enlinkd | opennms/daemon | 14 | Enhanced link discovery |
| scriptd | opennms/daemon | 15 | Script-based event automation |
| provisiond | opennms/daemon | 16 | Node provisioning and scanning |
| bsmd | opennms/daemon | 17 | Business service monitoring |
| telemetryd | opennms/daemon | 18 | Telemetry/flow reception (via Minion Kafka Sink) |
| minion | opennms/minion | — | Distributed data collection agent |
| db-init | opennms/db-init | — | One-shot Liquibase schema migration |
| postgres | postgres:16 | — | PostgreSQL database (alarms only) |
| kafka | kafka | — | Event transport backbone |

### Kafka Topics

| Topic | Purpose |
|-------|---------|
| `opennms-fault-events` | Alarm-bearing events (traps, syslog, translated events with alarm-data) |
| `opennms-ipc-events` | Daemon-to-daemon internal events (newSuspect, nodeScanCompleted, reloadDaemonConfig) |
| `OpenNMS.Sink.Trap` | Minion → Trapd raw trap forwarding |
| `OpenNMS.Sink.Syslog` | Minion → Syslogd raw syslog forwarding |
| `OpenNMS.Sink.Telemetry-*` | Minion → Telemetryd per-protocol flow forwarding |

## Quick Start

```bash
cd opennms-container/delta-v

# Start core infrastructure + all daemons
COMPOSE_PROFILES=full docker compose up -d

# Start minimal set (alarmd, pollerd, trapd, provisiond)
COMPOSE_PROFILES=lite docker compose up -d

# Check service health
docker compose ps

# Run all E2E tests (no webapp required — SQL-only verification)
./test-e2e.sh          # 11 tests: trap → provision → alarm lifecycle
./test-minion-e2e.sh   # 13 tests: Minion → Kafka Sink → alarm lifecycle
./test-syslog-e2e.sh   # 15 tests: syslog → Minion → Cisco alarm lifecycle
```

### Prerequisites

- **JDK 21** (daemon/minion build and runtime)
- Docker Desktop with **16 GB memory** (17+ JVM containers)
- `snmptrap` (net-snmp) for E2E tests

## Building

Requires **JDK 21** (`jenv`, `JAVA_HOME`, or temurin-21 auto-detected).

```bash
cd opennms-container/delta-v

# Full build: compile → assemble → images → deltav
./build.sh

# Or individual steps:
./build.sh compile    # Maven compile with JDK 21
./build.sh assemble   # Build Karaf assemblies (sentinel, minion, daemon, alarmd)
./build.sh images     # Build base Docker images (sentinel, minion, db-init)
./build.sh deltav     # Build Delta-V layered images (daemon-deltav, minion-deltav)
```

See [BUILD.md](BUILD.md) for detailed build instructions.

## Key Design Decisions

1. **No events table** — Events flow exclusively via Kafka. Only alarms are persisted to PostgreSQL by Alarmd. The `events`, `event_parameters`, `notifications`, and `usersnotified` tables are eliminated.

2. **No ActiveMQ** — All cross-container communication uses Kafka topics. The AMQ hub-and-spoke transport is fully removed.

3. **Each daemon is self-contained** — Every daemon container has its own `EventWriter`, `EventListener`, `EventExpander`, and `KafkaEventForwarder`. No dependency on Eventd or a central event bus.

4. **Producer-side event enrichment** — Each daemon's `KafkaEventForwarder` loads 157 event definitions from the database via `EventConfInitializer` and applies severity + alarm-data to events before publishing to Kafka.

5. **Minion communicates via Kafka only** — No REST dependency. SNMPv3 user config distributed via Twin API. Traps, syslog, and telemetry forwarded via Kafka Sink topics.

6. **Minion is sole network ingress** — No daemon container binds external UDP/TCP monitoring ports. All protocol data (traps, syslog, flows) enters via Minion → Kafka Sink → KafkaSinkBridge → daemon container.

## Project Status

See [DELTA-V_Status.md](DELTA-V_Status.md) for detailed progress tracking.

**Current state:** 19 services running (Telemetryd TSID=18 added), Minion is sole network ingress. Eventd listeners and DHCP monitor deleted. Syslogd and Telemetryd consume via KafkaSinkBridge from Minion.

## Documentation

| Document | Description |
|----------|-------------|
| [OPENNMS.md](OPENNMS.md) | Original OpenNMS Horizon project description |
| [DELTA-V_Status.md](DELTA-V_Status.md) | Detailed status of all Delta-V work |
| [BUILD.md](BUILD.md) | Build instructions |
| [CLAUDE.md](CLAUDE.md) | AI assistant project context |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution guidelines |

Design documents are in `docs/plans/`.

## License

This project is licensed under the [GNU Affero General Public License v3](LICENSE.md).

[OpenNMS Horizon]: http://www.opennms.com/
