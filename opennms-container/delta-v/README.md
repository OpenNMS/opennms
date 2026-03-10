# OpenNMS Delta-V

**Composable, containerized deployment of OpenNMS Horizon.**

Delta-V decomposes the monolithic OpenNMS into 17 independently scalable services connected by Kafka and PostgreSQL. Each daemon runs in its own Karaf container, communicating via a shared event bus. There is no core container — schema migration is handled by a one-shot db-init container, and the webapp serves only the Web UI and REST API.

```
                    ┌──────────────────────────────────────────────────┐
                    │                   Kafka (KRaft)                  │
                    │    opennms-fault-events / opennms-ipc-events     │
                    └──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬─────┘
                       │  │  │  │  │  │  │  │  │  │  │  │  │  │
  ┌─────────┐     ┌────┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴────┐
  │Postgres │◄────┤ webapp │ alarmd │ pollerd │ provisiond │ ...     │
  │         │     │ Jetty  │ Alarms │ Polling │ Provision  │         │
  └─────────┘     └─────────────────────────────────────────────────┘
```

## Services

| Service          | Image            | Purpose                                       | Host Port |
|------------------|------------------|-----------------------------------------------|-----------|
| postgres         | postgres:15      | Shared database                               | 5432      |
| kafka            | apache/kafka     | Event bus (KRaft mode)                        | —         |
| db-init          | opennms/horizon  | One-shot schema migration (exits after init)  | —         |
| webapp           | opennms/horizon  | JettyServer — Web UI, REST API, Provisiond    | 8980      |
| minion           | opennms/minion   | Distributed data collection agent             | —         |
| alarmd           | opennms/daemon   | Alarm processing (Kafka consumer)             | 8201      |
| pollerd          | opennms/daemon   | Service polling                               | 8103      |
| collectd         | opennms/daemon   | Data collection                               | 8104      |
| rtcd             | opennms/daemon   | Real-time console data                        | —         |
| notifd           | opennms/daemon   | Notifications                                 | —         |
| discovery        | opennms/daemon   | Network discovery                             | —         |
| trapd            | opennms/daemon   | SNMP trap reception                           | 1162/udp  |
| syslogd          | opennms/daemon   | Syslog reception                              | 10514/udp |
| ticketer         | opennms/daemon   | Trouble ticket integration                    | —         |
| eventtranslator  | opennms/daemon   | Event transformation rules                    | —         |
| enlinkd          | opennms/daemon   | Link discovery (CDP, LLDP, OSPF, IS-IS, Bridge) | —      |
| scriptd          | opennms/daemon   | Event-driven scripting                        | —         |

## Quick Start

### Prerequisites

- Docker Engine 24+ with Compose v2
- Java 17 (for building from source)
- 8 GB RAM allocated to Docker (16 GB recommended for full deployment)

### Build from Source

```bash
# Clone the repository
git clone https://github.com/pbrane/delta-v.git
cd delta-v

# Full build: compile + assemble + Docker images
opennms-container/delta-v/build.sh

# Or build just the images (if Maven artifacts exist)
opennms-container/delta-v/build.sh images
```

### Deploy

```bash
cd opennms-container/delta-v

# Start with a profile
./deploy.sh up lite       # Essential daemons
./deploy.sh up passive    # Lite + trapd/syslogd/eventtranslator
./deploy.sh up full       # All 17 services

# Check status
./deploy.sh status

# Verify deployment
./deploy.sh test
```

Web UI: **http://localhost:8980/opennms** (admin / admin)

### Manage

```bash
# View logs
./deploy.sh logs              # All services
./deploy.sh logs alarmd       # Single service

# Karaf shell access
./deploy.sh shell webapp      # SSH to webapp Karaf

# Stop (preserve data)
./deploy.sh down

# Reset (destroy all data)
./deploy.sh reset
```

## Build Script Reference

```bash
./build.sh              # Full build (compile + assemble + images)
./build.sh compile      # Maven compile only
./build.sh assemble     # Build distribution tarballs
./build.sh images       # Build Docker images (requires prior assembly)
./build.sh overlay      # Prepare webapp overlay
./build.sh push         # Build and push to registry
./build.sh clean        # Remove Docker volumes

# Push to custom registry
DOCKER_ORG=pbranestrategy ./build.sh push
```

## Architecture

Delta-V replaces the monolithic OpenNMS runtime with a composable service mesh:

- **db-init** runs schema migration (Liquibase) and exits — no persistent core container
- **Webapp** runs JettyServer (Web UI + REST API) and Provisiond (node management)
- **Alarmd** consumes events from Kafka and processes them into alarms in PostgreSQL
- **Daemon containers** (pollerd, collectd, etc.) each run a single daemon in a lightweight Karaf instance
- **Minion** handles distributed data collection (SNMP, ICMP) via Kafka IPC

All services communicate via two Kafka topics: `opennms-fault-events` (alarm-bearing events) and `opennms-ipc-events` (daemon-to-daemon coordination). Each service generates globally unique event IDs using TSID (Time-Sorted IDs) with a unique node-id per JVM.

### Event Flow

```
Daemon → KafkaEventForwarder → Kafka → KafkaEventSubscriptionService → Alarmd
                                  ↓
                    Other daemons subscribe to relevant events
```

Events bypass the traditional `events` database table entirely. They flow through Kafka in real-time, and Alarmd processes them directly into alarms.

## Memory Requirements

Running all 17 services requires significant memory. If Docker Desktop runs out of memory (exit code 137), use deployment profiles:

| Profile | Services | Approx. Memory |
|---------|----------|-----------------|
| lite    | ~10      | ~8 GB           |
| passive | ~13      | ~10 GB          |
| full    | 17       | ~12 GB          |

## Troubleshooting

**Images not found:** Run `./build.sh` to build all images. Verify with `docker images | grep opennms`.

**OOM kills (exit 137):** Increase Docker Desktop memory or use `./deploy.sh up lite`.

**Service won't start:** Check logs: `./deploy.sh logs <service>`. Most issues are Karaf feature resolution failures — read OSGi error messages backwards from "Unable to resolve root".

**Database connection errors:** Ensure postgres is healthy before other services start. The compose healthchecks handle this, but initial schema creation takes time.

**Stale data after rebuild:** Run `./deploy.sh reset` to remove all volumes, then `./deploy.sh up`.

## License

AGPL v3 — see [LICENSE.md](../../LICENSE.md)
