# OpenNMS Delta-V

## Foreword

OpenNMS began in the year 2000 as an audacious idea: an enterprise-grade network monitoring platform, built entirely in the open, that could stand alongside the commercial giants of its era. Twenty-six years later, it is one of the longest-running open-source infrastructure projects in existence. That longevity is not an accident. It is the direct result of a community and a developer culture that refused to let the platform calcify.

Over the course of those twenty-six years, OpenNMS has absorbed and adapted to every major shift in the infrastructure landscape. It moved from RRDtool to Cassandra-backed time-series storage. It embraced OSGi and Apache Karaf when modularity became essential. It adopted Apache Kafka for distributed messaging, Newts for high-performance metrics, and Sentinel for horizontally-scaled event processing. It traded JSP page flows for a modern Vue 3 single-page application. At each inflection point, the community and contributors made the hard choice to modernize rather than maintain, to invest in the future rather than protect the past.

To every contributor who has written a line of code, filed a bug, answered a question on the mailing list, or deployed OpenNMS in a network operations center anywhere in the world: this architecture exists because of you. The platform you built and sustained for over two decades is the foundation on which OpenNMS Delta-V stands.

And yet, of all the design decisions made in those early years, one stands above the rest as the reason this transformation is possible at all: the event-driven architecture. From its very first release, OpenNMS was built around the principle that every meaningful state change in a monitored network should be expressed as an event. Nodes discovered, services gained and lost, thresholds crossed, traps received, alarms raised and cleared --- every action in the system flows through a unified event pipeline. This design was unusual in the industry in 2000. It remains unusual today. Most monitoring platforms bolt on event handling as an afterthought; OpenNMS made it the central nervous system.

It is precisely this event-driven foundation that made OpenNMS Delta-V possible. Because every daemon in the monolith already communicated through events rather than direct method calls, decomposing the system into independent containers required changing only the *transport* --- from an in-process event bus to Apache Kafka --- not the *semantics*. The daemons did not need to be rewritten. They needed to be *released*: freed from the single JVM, given their own containers, and connected by the same event vocabulary they had always spoken.

OpenNMS Delta-V is the realization of a possibility that was planted in the architecture twenty-six years ago.

---

## Executive Summary

OpenNMS Delta-V is a containerized, composable deployment architecture for OpenNMS Horizon. It decomposes the traditional single-JVM monolith into independent, purpose-built containers --- one per daemon --- connected by Apache Kafka event streams and backed by a shared PostgreSQL database.

The result is an OpenNMS deployment that operators can tailor to their organization's specific requirements. Services can be scaled independently, upgraded without full-platform downtime, and omitted entirely when not needed. A small deployment might run five containers. A large enterprise deployment might run fifteen or more, with multiple replicas of high-throughput services like Trapd and Syslogd.

Delta-V achieves this without rewriting the daemons themselves. The same battle-tested Pollerd, Collectd, Alarmd, and Notifd code that has run inside the monolith for years now runs in its own lightweight Karaf container, communicating with the rest of the platform through Kafka. The event-driven contract between daemons is preserved. What changes is the deployment topology, not the monitoring logic.

### Key outcomes

- **Composable**: Deploy only the daemons your organization needs. Each service is an independent container with its own lifecycle.
- **Scalable**: High-volume services (trap reception, syslog ingestion, polling, data collection) can be replicated horizontally without duplicating the entire platform.
- **Resilient**: A failure in one daemon container does not bring down the platform. Kafka provides durable, replayable event delivery between services.
- **Sustainable**: Smaller, focused containers consume less memory and start faster than the monolith. Resource allocation maps directly to workload.
- **Compatible**: The web interface, REST APIs, and all existing integrations continue to function. Delta-V is a deployment change, not a product change.

---

## Architecture Overview

### From Monolith to Delta-V

The traditional OpenNMS Horizon deployment runs all daemons within a single JVM process. Eventd receives and enriches events, then broadcasts them to all registered listeners over an in-process event bus. Every daemon --- Pollerd, Collectd, Alarmd, Notifd, and the rest --- subscribes to the events it cares about and acts accordingly. This model is simple, fast, and has served the platform well for over two decades.

Delta-V preserves this event-driven model but replaces the in-process bus with Apache Kafka. Each daemon runs in its own container and publishes events to a shared Kafka topic. Each daemon subscribes to the same topic and filters for the events it needs. The semantics are identical. The transport is distributed.

```
                     Traditional (Monolith)

  ┌─────────────────────────────────────────────────────┐
  │                   Single JVM                        │
  │                                                     │
  │  Eventd ──→ In-Process Event Bus ──→ All Daemons    │
  │                                                     │
  │  Pollerd  Collectd  Alarmd  Notifd  Trapd  ...      │
  └─────────────────────────────────────────────────────┘
                          │
                     PostgreSQL


                      Delta-V (Composable)

  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
  │  Pollerd │  │ Collectd │  │  Alarmd  │  │  Notifd  │
  │ Container│  │ Container│  │ Container│  │ Container│
  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘
       │              │              │              │
       └──────────────┴──────┬───────┴──────────────┘
                             │
                      Apache Kafka
                   (Event Bus / Streams)
                             │
       ┌─────────────────────┼─────────────────────┐
       │                     │                      │
  ┌────┴─────┐         ┌────┴─────┐          ┌─────┴────┐
  │   Core   │         │  Webapp  │          │  Trapd   │
  │ Container│         │ Container│          │ Container│
  └────┬─────┘         └────┬─────┘          └──────────┘
       │                     │
       └──────────┬──────────┘
                  │
             PostgreSQL
```

### Service Catalog

Delta-V decomposes OpenNMS Horizon into the following independently deployable services:

| Service | Role | Container Image |
|---------|------|-----------------|
| **PostgreSQL** | Relational data store for nodes, alarms, notifications, outages, assets | `postgres:15` |
| **Kafka** | Distributed event bus connecting all daemon containers | `apache/kafka` |
| **Core** | Eventd, Provisiond, Enlinkd, Telemetryd, Bsmd, Correlator, Scriptd | `opennms/horizon` |
| **Webapp** | Web UI (Vue 3 SPA + legacy JSPs), REST API, JettyServer | `opennms/horizon` |
| **Alarmd** | Kafka event consumer, alarm creation, reduction, and lifecycle | `opennms/alarmd` |
| **Pollerd** | Service availability polling | `opennms/daemon` |
| **Collectd** | Performance data collection | `opennms/daemon` |
| **Rtcd** | Response time calculator | `opennms/daemon` |
| **PassiveStatusd** | Passive status monitoring | `opennms/daemon` |
| **Notifd** | Notification delivery (email, XMPP, scripts) | `opennms/daemon` |
| **Discovery** | Network discovery and node scanning | `opennms/daemon` |
| **Trapd** | SNMP trap reception (UDP 1162) | `opennms/daemon` |
| **Syslogd** | Syslog message reception (UDP 10514) | `opennms/daemon` |
| **Ticketer** | Trouble ticket integration | `opennms/daemon` |
| **EventTranslator** | Event translation and enrichment rules | `opennms/daemon` |

An organization that does not use SNMP traps simply omits the Trapd container. An organization that needs high-throughput syslog ingestion runs multiple Syslogd replicas. The platform adapts to the workload rather than imposing a fixed footprint.

### Event Flow

Events are the lingua franca of OpenNMS Delta-V. Every state change --- a service going down, a trap arriving, a threshold being crossed --- is expressed as an event and published to Kafka.

**Daemon containers** enrich and publish events locally:

```
Daemon (Pollerd, Trapd, Syslogd, etc.)
  └─→ KafkaEventForwarder
        ├─→ EventExpander    (enrich from eventconf.xml)
        ├─→ TsidAssigner     (assign time-sorted unique ID)
        └─→ Kafka producer → opennms-fault-events topic
```

**Core container** routes events based on classification:

```
Core (Provisiond, Enlinkd, Telemetryd, etc.)
  └─→ EventRouter
        ├─→ FAULT events  → Kafka (opennms-fault-events)
        ├─→ IPC events    → ActiveMQ (internal coordination)
        └─→ DUAL events   → both Kafka and ActiveMQ
```

**Consumers** read from Kafka and act independently:

```
opennms-fault-events topic
  ├─→ Alarmd           (creates and reduces alarms)
  ├─→ Notifd           (sends notifications)
  ├─→ Pollerd/Collectd (schedule updates)
  ├─→ EventTranslator  (event enrichment rules)
  └─→ All other daemon consumers (fanout via consumer groups)
```

Each consumer operates in its own Kafka consumer group, ensuring that every event is delivered to every interested service exactly once. Kafka's durability guarantees mean that if a daemon container restarts, it resumes processing from where it left off with no event loss.

### TSID: Distributed Event Identity

In the monolithic architecture, events received sequential integer IDs from a PostgreSQL sequence. In a distributed system, multiple containers produce events concurrently, making a centralized sequence impractical.

Delta-V replaces sequential database IDs with **Time-Sorted IDs (TSIDs)** --- 64-bit identifiers composed of a millisecond timestamp and a node identifier. Each container is assigned a unique node ID at startup, ensuring that TSIDs are globally unique without coordination. TSIDs are sortable by time, making them suitable for ordering and correlation.

| Container | TSID Node ID |
|-----------|-------------|
| Core | 1 |
| Alarmd | 2 |
| Webapp | 3 |
| Pollerd | 4 |
| Collectd | 5 |
| Rtcd | 6 |
| PassiveStatusd | 7 |
| Notifd | 8 |
| Discovery | 9 |
| Trapd | 10 |
| Syslogd | 11 |
| Ticketer | 12 |
| EventTranslator | 13 |

### Events Without a Database Table

One of Delta-V'smost significant architectural changes is the elimination of the PostgreSQL `events` table. In the monolith, every event was written to this table by `HibernateEventWriter`, creating a persistent record that other tables (alarms, notifications, outages) referenced via foreign keys.

At scale, the events table became the platform's primary performance bottleneck. Networks generating tens of thousands of events per second overwhelmed PostgreSQL's write capacity, causing cascading slowdowns across the entire system.

Delta-V removes this bottleneck entirely. Events flow through Kafka and are never written to a relational database. The data that downstream tables formerly obtained through foreign key joins to the events table --- severity, UEI, timestamp, source --- is now denormalized directly onto the consuming tables. Alarms carry their own event data. Notifications carry their own event data. The events table is gone.

This is not a compromise. Kafka provides durable, replayable, high-throughput event storage that PostgreSQL was never designed to deliver. The events are not lost --- they are in Kafka, available to any consumer that needs them, for as long as the retention policy allows.

### Daemon Loader Pattern

Each daemon is extracted from the monolith using a consistent pattern called the **daemon loader**. A thin OSGi bundle (`core/daemon-loader-<name>/`) contains:

- A flat Spring XML context that declares the daemon's dependencies as OSGi service references
- A `DaemonLifecycleManager` that calls `init()`, `start()`, and `stop()` on the daemon bean
- A Karaf feature definition that bundles the daemon loader with its runtime dependencies

The daemon's own code is unchanged. The loader simply wires it into a standalone Karaf container the same way the monolith's Spring context would, but with OSGi service references replacing direct bean injection.

### Composability in Practice

Delta-V'scomposability is not theoretical. Consider three real deployment scenarios:

**Small office (5 containers)**: PostgreSQL, Kafka, Core, Webapp, Pollerd. Monitoring is limited to service polling. No traps, no syslog, no data collection. Memory footprint: ~4 GB.

**Mid-size enterprise (10 containers)**: Add Collectd, Alarmd, Notifd, Trapd, Discovery. Full monitoring with alarms, notifications, and SNMP trap handling. Memory footprint: ~8 GB.

**Large-scale operations (15+ containers)**: All services deployed. Multiple Trapd and Syslogd replicas behind a UDP load balancer for high-throughput ingestion. Pollerd and Collectd scaled to cover thousands of nodes. Memory footprint: scales with replica count.

In every case, the same container images are used. The difference is which containers are present and how many replicas of each are running.

---

## Getting Started

### Prerequisites

- Docker Engine 24+ and Docker Compose v2
- At minimum 4 GB of memory allocated to Docker (8 GB recommended for full deployment)

### Minimal Deployment

```bash
cd opennms-container/delta
docker compose up -d postgres kafka core webapp pollerd
```

### Full Deployment

```bash
cd opennms-container/delta
docker compose up -d
```

### Accessing the Platform

- **Web UI**: http://localhost:8980/opennms (default credentials: admin/admin)
- **REST API**: http://localhost:8980/opennms/rest/

### Verifying Health

```bash
docker compose ps
```

All services should report `healthy` status. Daemon containers expose a health probe at `/sentinel/rest/health/probe` on port 8181.

---

## Summary

OpenNMS Delta-V is the same OpenNMS Horizon that has monitored networks for twenty-six years, decomposed into containers and connected by Kafka event streams. It is not a rewrite. It is a restructuring --- made possible by the event-driven architecture that was there from the beginning.

The monolith served its era well. Delta-V serves the era that followed.
