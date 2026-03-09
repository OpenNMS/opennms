# Minion-First Microservice Architecture Design

## Goal

Enforce all network communications through Minion, use Kafka as the single inter-service
transport (two topics: IPC and fault), and fully extract all daemons into standalone containers.

## Context

The previous design used a dual-transport model: AMQ for IPC events, Kafka for fault events.
This was complex (two transport layers, race conditions in AMQ connectivity, spring-extender
bundle ordering issues). The Minion-first approach simplifies everything:

1. **Minion handles ALL device network I/O** (traps, syslog, SNMP, ICMP, flows, telemetry)
2. **Kafka is the single transport** between all services (two topics, not two technologies)
3. **Daemon containers become pure processing nodes** — no UDP listeners, no direct SNMP

## What Changes from Previous Design

| Aspect | Previous (AMQ + Kafka) | New (Kafka-only + Minion) |
|--------|----------------------|--------------------------|
| Transport | AMQ for IPC, Kafka for faults | Kafka for everything (2 topics) |
| Trapd container | Listens UDP 162, creates events | Consumes from Minion's Sink topic |
| Syslogd container | Listens UDP 514, creates events | Consumes from Minion's Sink topic |
| Pollerd container | Sends ICMP/SNMP directly | Dispatches RPC to Minion via Kafka |
| Collectd container | Collects SNMP directly | Dispatches RPC to Minion via Kafka |
| Discovery container | Sends ICMP pings directly | Dispatches detection RPC to Minion |
| Enlinkd container | SNMP walks directly | SNMP proxy RPC to Minion |
| Telemetryd container | N/A (was on core) | Consumes from Minion's Telemetry Sink |
| Core broker | Embedded AMQ on port 61616 | No AMQ needed for inter-daemon comms |
| Inter-daemon events | AMQ Topics (OpenNMS.IPC.*) | Kafka `opennms-ipc-events` topic |

## Architecture

```
                    +-----------+
                    |  Minion   |  ← ALL device network I/O
                    | (Kafka    |    UDP 162 (traps), UDP 514 (syslog),
                    |  Sink/RPC)|    SNMP, ICMP, gNMI, NetFlow
                    +-----+-----+
                          |
                    Kafka Cluster
                    /     |     \
        Sink Topics   RPC Topics   Event Topics
        (Trap,Syslog, (Poll,Collect,(opennms-ipc-events,
         Telemetry)    Detect,SNMP)  opennms-fault-events)
              |            |              |
    +---------+----+  +----+-------+  +---+----------+
    | Trapd        |  | Pollerd    |  | Core         |
    | (SinkConsumer|  | (Scheduler |  | (Eventd,     |
    |  → events)   |  |  + RPC     |  |  Provisiond) |
    +--------------+  |  dispatch) |  +--------------+
    | Syslogd      |  +------------+  | Alarmd       |
    | (SinkConsumer|  | Collectd   |  | (fault events|
    |  → events)   |  | (Scheduler |  |  → alarms)   |
    +--------------+  |  + RPC     |  +--------------+
    | Telemetryd   |  |  dispatch) |  | Notifd       |
    | (SinkConsumer|  +------------+  | (events →    |
    |  → metrics)  |  | Discovery  |  |  notifs)     |
    +--------------+  | (RPC       |  +--------------+
                      |  dispatch) |
                      +------------+
                      | Enlinkd    |
                      | (SNMP RPC  |
                      |  dispatch) |
                      +------------+
```

## Kafka Topics

| Topic | Purpose | Producers | Consumers |
|-------|---------|-----------|-----------|
| `opennms-ipc-events` | Internal daemon-to-daemon events (newSuspect, nodeScanCompleted, reloadDaemonConfig, nodeAdded, etc.) | Any daemon | Any daemon that subscribes |
| `opennms-fault-events` | Fault events with alarm-data (traps, syslog, thresholds) | Trapd, Syslogd, Pollerd, Collectd | Alarmd, Core (Eventd), Notifd, Scriptd |
| `OpenNMS.Sink.Trap` | Raw trap data from Minion | Minion | Trapd container (TrapSinkConsumer) |
| `OpenNMS.Sink.Syslog` | Raw syslog data from Minion | Minion | Syslogd container (SyslogSinkConsumer) |
| `OpenNMS.Sink.Telemetry-*` | Flow/telemetry data from Minion | Minion | Telemetryd container |
| `OpenNMS.RPC.*` | RPC request/response for Minion | Pollerd, Collectd, Discovery, Enlinkd, Provisiond | Minion |

The first two topics (`opennms-ipc-events`, `opennms-fault-events`) are new. The `OpenNMS.Sink.*`
and `OpenNMS.RPC.*` topics already exist — they're the Minion IPC infrastructure.

## What AMQ Code Can Be Removed

With Kafka as the sole inter-daemon transport, the following become unnecessary:

1. **JmsMessageBus** (`core/messagebus-jms/`) — was bridging IPC events over AMQ Topics
2. **MessageBusEventListenerBridge** — was delivering AMQ messages to `@EventHandler` methods
3. **LocalMessageBus** — in-JVM fallback, no longer needed
4. **MessageBus API** (`core/messagebus-api/`) — entire abstraction can be removed
5. **AMQ broker URL config** (`org.opennms.core.messagebus.jms.cfg`) — per-daemon config files
6. **Core's embedded AMQ broker exposure** (port 61616 for daemon containers)

Note: Core's embedded AMQ broker still exists for legacy Camel-based IPC (Minion Camel
transport). But daemon containers no longer need to connect to it.

## EventClassifier Changes

The EventClassifier currently routes to three destinations: FAULT (Kafka), IPC (MessageBus/AMQ),
DUAL (both). With AMQ removed:

- **FAULT** → `opennms-fault-events` Kafka topic (unchanged)
- **IPC** → `opennms-ipc-events` Kafka topic (was AMQ, now Kafka)
- **DUAL** → both Kafka topics

The `KafkaEventForwarder` in daemon containers already publishes to Kafka. We add a second
Kafka producer for the IPC topic. The EventClassifier logic stays the same — only the transport
backing for IPC changes from AMQ to Kafka.

## Daemon Container Simplification

### Sink consumers (Trapd, Syslogd, Telemetryd)

These containers no longer run UDP listeners. Instead they run the Core-side Sink consumer:

- **Trapd container** runs `TrapSinkConsumer` which consumes from `OpenNMS.Sink.Trap` Kafka
  topic, converts `TrapLogDTO` → Events, and publishes fault events to `opennms-fault-events`.
- **Syslogd container** runs `SyslogSinkConsumer` similarly.
- **Telemetryd container** runs `TelemetryMessageConsumer` which processes flows/metrics.

The Sink consumer infrastructure already exists and works with Kafka. The daemon containers
just need the Kafka consumer config (bootstrap servers) and the Sink consumer beans.

### RPC dispatchers (Pollerd, Collectd, Discovery, Enlinkd)

These containers run the scheduling/orchestration logic and dispatch work to Minion via RPC:

- **Pollerd** runs the poll scheduler, calls `LocationAwarePollerClient.poll()` which sends
  RPC to Minion via Kafka, Minion executes the poll, returns result.
- **Collectd** runs the collection scheduler, same pattern via `LocationAwareCollectorClient`.
- **Discovery** triggers detection scans via `LocationAwareDetectorClient`.
- **Enlinkd** does SNMP walks via `SnmpProxyRpcModule` (SNMP proxied through Minion).

These already use `RpcClientFactory` which has Kafka transport support. The containers need
Kafka config (bootstrap servers) and the RPC client beans.

### Pure processors (Alarmd, Notifd, Provisiond)

These don't do network I/O at all:

- **Alarmd** — consumes fault events from Kafka, creates/updates alarms in PostgreSQL
- **Notifd** — consumes events, sends notifications (SMTP, etc.)
- **Provisiond** — orchestrates provisioning, dispatches detection/scan RPCs to Minion

## Minion Requirements

Minion must be configured with:
- Kafka bootstrap servers (for Sink and RPC transport)
- Monitoring location
- All listener ports (UDP 162 for traps, UDP 514 for syslog, etc.)

Minion features needed:
- `minion-trapd-listener` (Trap Sink producer)
- `minion-syslogd-listener` (Syslog Sink producer)
- `minion-telemetryd-receivers` (Telemetry Sink producer)
- `minion-poller` (Poll RPC handler)
- `minion-collection` (Collection RPC handler)
- `minion-provisiond-detectors` (Detection RPC handler)
- `minion-snmp-proxy` (SNMP RPC proxy)

## Implementation Phases

### Phase 1: Replace AMQ with Kafka IPC topic
- Add `opennms-ipc-events` Kafka topic
- Replace `JmsMessageBus` with `KafkaIpcEventPublisher` (publishes IPC events to Kafka)
- Replace `MessageBusEventListenerBridge` with `KafkaIpcEventConsumer` (consumes from Kafka)
- Remove AMQ dependency from daemon containers
- Test: Discovery → newSuspect → Kafka IPC → Provisiond (core)

### Phase 2: Add Minion to Delta-V compose
- Add Minion service to docker-compose.yml
- Configure Kafka transport for Sink/RPC
- Configure monitoring location
- Verify Minion heartbeat arrives at core

### Phase 3: Simplify Trapd/Syslogd containers
- Remove UDP listeners from Trapd/Syslogd containers
- Wire TrapSinkConsumer/SyslogSinkConsumer as primary daemon entry point
- Minion receives traps/syslog → Sink → Kafka → container's SinkConsumer → events
- Test: send trap to Minion → verify alarm in Alarmd

### Phase 4: Wire RPC transport for Pollerd/Collectd/Discovery/Enlinkd
- Configure RPC client in daemon containers to use Kafka transport
- Minion handles all SNMP/ICMP execution
- Test: Pollerd schedules poll → RPC → Minion → poll result

### Phase 5: Full integration test
- All daemon containers running
- Minion handling all network I/O
- Events flowing through Kafka topics
- Alarms being created
- Notifications being sent

## Key Existing Components

| Component | Location | Role |
|-----------|----------|------|
| `TrapSinkConsumer` | `features/events/traps/` | Converts Sink trap data → events |
| `SyslogSinkConsumer` | `features/events/syslog/` | Converts Sink syslog data → events |
| `LocationAwarePollerClient` | `features/poller/client-rpc/` | RPC dispatch for polls |
| `LocationAwareCollectorClient` | `features/collection/client-rpc/` | RPC dispatch for collections |
| `LocationAwareDetectorClient` | `opennms-provision/opennms-detectorclient-rpc/` | RPC dispatch for detection |
| `SnmpProxyRpcModule` | `core/snmp/proxy-rpc-impl/` | SNMP proxy RPC for Enlinkd |
| `KafkaEventForwarder` | `core/event-forwarder-kafka/` | Event enrichment + Kafka routing |
| `EventClassifier` | `features/events/daemon/router/` | FAULT/IPC/DUAL routing |
| `FaultEventPublisher` | `features/events/daemon/processor/` | Publishes fault events to Kafka |
| `KafkaFaultEventConsumer` | `features/events/daemon/consumer/` | Consumes fault events on core |

## What We Keep from Previous Work

- `FaultEventPublisher` + `KafkaFaultEventConsumer` (Tasks 6-7) — still needed for fault events
- `EventClassifier` and `EventRouter` — routing logic stays, only transport changes
- `KafkaEventForwarder` in daemon containers — still the primary event publisher
- `KafkaEventSubscriptionService` — still delivers events to `@EventHandler` in daemons
- All 14 daemon-loader modules — container extraction pattern is proven
- All daemon container overlays and docker-compose structure

## What We Remove/Replace

- `JmsMessageBus` blueprint for daemon containers (Task 1) — replaced by Kafka IPC
- AMQ broker URL config (Task 2) — no longer needed
- Core's `JmsMessageBus` wiring (Task 3) — replaced by Kafka IPC publisher
- `MessageBusEventListenerBridge` wiring (Task 4) — replaced by Kafka IPC consumer
- `LocalMessageBus` as fallback — Kafka is always available
