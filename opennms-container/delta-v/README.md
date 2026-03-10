# OpenNMS Delta-V

**Composable, containerized deployment of OpenNMS Horizon.**

Delta-V decomposes the monolithic OpenNMS into 15 independently scalable services connected by Kafka and PostgreSQL. Each daemon runs in its own Karaf container, communicating via a shared event bus.

```
                    ┌──────────────────────────────────────────────────┐
                    │                   Kafka (KRaft)                  │
                    │              opennms-fault-events topic          │
                    └──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬────────────┘
                       │  │  │  │  │  │  │  │  │  │  │  │
  ┌─────────┐     ┌────┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴────┐
  │Postgres │◄────┤  core  │ webapp │ alarmd │ pollerd │ ...   │
  │         │     │ Eventd │ Jetty  │ Alarms │ Polling │       │
  └─────────┘     └───────────────────────────────────────────┘
```

## Services

| Service          | Image            | Purpose                                       | Host Port |
|------------------|------------------|-----------------------------------------------|-----------|
| postgres         | postgres:15      | Shared database                               | 5432      |
| kafka            | apache/kafka     | Event bus (KRaft mode)                        | —         |
| core             | opennms/horizon  | Eventd, Provisiond, Enlinkd, Telemetryd, Bsmd | 8101      |
| webapp           | opennms/horizon  | JettyServer — Web UI and REST API             | 8980      |
| alarmd           | opennms/alarmd   | Alarm processing (Kafka consumer)             | 8201      |
| pollerd          | opennms/daemon   | Service polling                               | 8103      |
| collectd         | opennms/daemon   | Data collection                               | 8104      |
| rtcd             | opennms/daemon   | Real-time console data                        | —         |
| notifd           | opennms/daemon   | Notifications                                 | —         |
| discovery        | opennms/daemon   | Network discovery                             | —         |
| trapd            | opennms/daemon   | SNMP trap reception                           | 1162/udp  |
| syslogd          | opennms/daemon   | Syslog reception                              | 10514/udp |
| ticketer         | opennms/daemon   | Trouble ticket integration                    | —         |
| eventtranslator  | opennms/daemon   | Event transformation rules                    | —         |

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

# Start all 15 services
./deploy.sh up

# Or start a smaller subset
./deploy.sh up lite    # 10 services (no trapd/syslogd/ticketer/eventtranslator)
./deploy.sh up core    # 4 services (postgres + kafka + core + webapp)

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
./deploy.sh shell core        # SSH to core Karaf
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

- **Core** runs Eventd (event ingestion), Provisiond (node management), Enlinkd, Telemetryd, Bsmd, Correlator, and Scriptd
- **Webapp** runs only JettyServer — serves the Web UI and REST API
- **Alarmd** consumes events from Kafka and processes them into alarms in PostgreSQL
- **Daemon containers** (pollerd, collectd, etc.) each run a single daemon in a lightweight Karaf instance

All services communicate via Kafka's `opennms-fault-events` topic. Each service generates globally unique event IDs using TSID (Time-Sorted IDs) with a unique node-id per JVM.

### Event Flow

```
Daemon → KafkaEventForwarder → Kafka → KafkaEventSubscriptionService → Alarmd
                                  ↓
                    Other daemons subscribe to relevant events
```

Events bypass the traditional `events` database table entirely. They flow through Kafka in real-time, and Alarmd processes them directly into alarms.

## Memory Requirements

Running all 15 services requires significant memory. If Docker Desktop runs out of memory (exit code 137), use deployment profiles:

| Profile | Services | Approx. Memory |
|---------|----------|-----------------|
| core    | 4        | ~4 GB           |
| lite    | 10       | ~8 GB           |
| full    | 15       | ~12 GB          |

## Troubleshooting

**Images not found:** Run `./build.sh` to build all images. Verify with `docker images | grep opennms`.

**OOM kills (exit 137):** Increase Docker Desktop memory or use `./deploy.sh up lite`.

**Service won't start:** Check logs: `./deploy.sh logs <service>`. Most issues are Karaf feature resolution failures — read OSGi error messages backwards from "Unable to resolve root".

**Database connection errors:** Ensure postgres is healthy before other services start. The compose healthchecks handle this, but initial schema creation takes time.

**Stale data after rebuild:** Run `./deploy.sh reset` to remove all volumes, then `./deploy.sh up`.

## License

AGPL v3 — see [LICENSE.md](../../LICENSE.md)
