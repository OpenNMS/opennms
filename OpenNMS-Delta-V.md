# OpenNMS Delta-V

**Delta-V** is a microservice decomposition of [OpenNMS Horizon][], transforming the monolithic Java application into 17+ independently deployable Kafka-connected containers.

> For the original OpenNMS Horizon project description, see [OPENNMS.md](OPENNMS.md).

## What Is Delta-V?

OpenNMS Horizon is an enterprise-grade open-source network monitoring platform. Delta-V restructures it from a single 35.6 GB monolith into lean, focused microservices:

- **Each daemon runs in its own container** — independent scaling, isolation, and restartability
- **Kafka-only event transport** — no ActiveMQ, no shared event bus
- **Events never touch PostgreSQL** — only alarms are persisted to the database
- **3 Docker images** serve all roles — `opennms/horizon` (webapp), `opennms/daemon` (all 14 daemon types), `opennms/minion` (distributed collection)
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
| webapp | opennms/horizon | 3 | Web UI, REST API, Karaf console |
| alarmd | opennms/daemon | 2 | Kafka → alarm creation/reduction → PostgreSQL |
| pollerd | opennms/daemon | 4 | Service availability polling |
| collectd | opennms/daemon | 5 | Performance data collection |
| rtcd | opennms/daemon | 6 | Response time collection daemon |
| discovery | opennms/daemon | 9 | Network discovery |
| trapd | opennms/daemon | 10 | SNMP trap reception (UDP 1162) |
| syslogd | opennms/daemon | 11 | Syslog reception (UDP 10514) |
| ticketer | opennms/daemon | 12 | Trouble ticket integration |
| eventtranslator | opennms/daemon | 13 | Event translation rules |
| enlinkd | opennms/daemon | 14 | Enhanced link discovery |
| scriptd | opennms/daemon | 15 | Script-based event automation |
| provisiond | opennms/daemon | 16 | Node provisioning and scanning |
| bsmd | opennms/daemon | 17 | Business service monitoring |
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

## Quick Start

```bash
cd opennms-container/delta-v

# Start core infrastructure + all daemons
COMPOSE_PROFILES=full docker compose up -d

# Start minimal set (webapp, alarmd, pollerd, trapd, provisiond)
COMPOSE_PROFILES=lite docker compose up -d

# Check service health
docker compose ps

# Run end-to-end integration test
./test-e2e.sh

# Run Minion end-to-end test
./test-minion-e2e.sh
```

### Prerequisites

- Docker Desktop with **16 GB memory** (17+ JVM containers)
- `snmptrap` (net-snmp) for E2E tests

## Building

```bash
# Full compile (skip tests)
./compile.pl -DskipTests

# Build daemon assembly
cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install

# Build Docker images
cd opennms-container/delta-v && ./build.sh
```

See [BUILD.md](BUILD.md) for detailed build instructions.

## Key Design Decisions

1. **No events table** — Events flow exclusively via Kafka. Only alarms are persisted to PostgreSQL by Alarmd. The `events`, `event_parameters`, `notifications`, and `usersnotified` tables are eliminated.

2. **No ActiveMQ** — All cross-container communication uses Kafka topics. The AMQ hub-and-spoke transport is fully removed.

3. **Each daemon is self-contained** — Every daemon container has its own `EventWriter`, `EventListener`, `EventExpander`, and `KafkaEventForwarder`. No dependency on Eventd or a central event bus.

4. **Producer-side event enrichment** — Each daemon's `KafkaEventForwarder` loads 157 event definitions from the database via `EventConfInitializer` and applies severity + alarm-data to events before publishing to Kafka.

5. **Minion communicates via Kafka only** — No REST dependency. SNMPv3 user config distributed via Twin API. Traps forwarded via Kafka Sink topic.

## Project Status

See [DELTA-V_Status.md](DELTA-V_Status.md) for detailed progress tracking.

**Current state:** 17 services running, all E2E tests passing (direct + Minion paths). PerspectivePollerd extraction (service #18) is designed and pending implementation.

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
