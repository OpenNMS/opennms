# Microservice Event Architecture Design

## Goal

Enable full cross-container event flow in the Strike Fighter architecture using two
independent transport layers: AMQ for IPC messages, Kafka for fault events.

## Context

15 daemon containers exist but cross-container event flow is broken. Core (opennms/horizon)
has no Kafka integration because Karaf is disabled. Daemon containers use LocalMessageBus
(in-JVM only) so IPC messages never leave the container. The integration test proved that
Discovery can ping and publish to Kafka, and Provisiond can create nodes — but only when
both run in the same JVM on core.

## Transport Architecture

Two independent transport layers, each with a single responsibility:

| Layer | Transport | What flows | Direction |
|-------|-----------|-----------|-----------|
| **IPC** | AMQ (hub-and-spoke) | Internal daemon-to-daemon messages (`uei.opennms.org/internal/*`) | Bidirectional between all containers |
| **Fault** | Kafka | External events (traps, syslog, thresholds, node events with alarm-data) | Daemon containers → Kafka ← consumers (Alarmd, core, etc.) |

**EventClassifier remains the routing decision point:**
- `FAULT` → Kafka only
- `IPC` → MessageBus (AMQ) only
- `DUAL` → both Kafka and AMQ

## IPC Layer: AMQ Hub-and-Spoke

Core hosts the embedded AMQ broker on port 61616. All daemon containers connect as spoke
clients via `failover:tcp://core:61616`.

### Why Hub-and-Spoke (not Mesh)

- Core already embeds AMQ — zero new infrastructure
- Core as SPOF is acceptable: if core is down, Provisiond is down anyway
- Daemons that matter without core (Trapd, Collectd, Syslogd) use Kafka, not AMQ
- No throwaway work: daemon code is identical regardless of broker topology

### Migration Path

1. **Now**: Core (opennms/horizon) hosts embedded AMQ, daemons connect as spokes
2. **Delta-V Option 3**: Move core to opennms/daemon (lightweight Karaf-only)
3. **Multiple cores**: Core replicas behind K8s Service, or dedicated AMQ statefulset
4. **L3 K8s Operator**: Operator provisions broker topology based on deployment scale —
   daemon code never changes, only the broker URL

### Changes for Daemon Containers

1. **Wire JmsMessageBus instead of LocalMessageBus.** The `event-forwarder-kafka` blueprint
   (or a companion blueprint) creates a `JmsMessageBus` bean with an AMQ `ConnectionFactory`
   pointing at `${OPENNMS_BROKER_URL}`.

2. **KafkaEventForwarder gets JmsMessageBus injected.** Currently MessageBus is null in
   daemon containers. With a real `JmsMessageBus`, IPC-classified events flow to AMQ.

3. **MessageBusEventListenerBridge wires up.** Already exists in daemon containers via the
   `events.daemon` bundle. Once it has a working MessageBus, `@EventHandler` annotations
   start receiving cross-container IPC events automatically.

### Core (No Changes for IPC)

Core already has `JmsMessageBus` wired to the embedded broker. `EventRouter` already
publishes IPC events to MessageBus → AMQ Topics → all connected daemon containers.

### IPC Event Flow (End-to-End)

```
Discovery (standalone container)
  → eventForwarder.sendNow(newSuspect)
  → KafkaEventForwarder → EventClassifier → IPC
  → JmsMessageBus.publish("discovery/newSuspect", ...)
  → AMQ Topic "OpenNMS.IPC.discovery/newSuspect"
  → Core's JmsMessageBus receives
  → MessageBusEventListenerBridge
  → Provisioner.handleNewSuspectEvent()
  → Creates node, fires nodeScanCompleted
  → EventRouter → IPC → JmsMessageBus
  → AMQ Topic "OpenNMS.IPC.node/nodeScanCompleted"
  → Enlinkd container's JmsMessageBus receives
  → MessageBusEventListenerBridge
  → EnhancedLinkd handles topology discovery
```

## Fault Layer: Kafka on Core (Spring-Wired)

Core gets two new Spring beans in `applicationContext-eventDaemon.xml`, wired directly
(no Karaf/OSGi needed). This is a temporary bridge until core moves to opennms/daemon
where the existing `event-forwarder-kafka` blueprint takes over naturally.

### FaultEventPublisher (Kafka Producer)

`EventRouter` already delegates FAULT and DUAL events to `FaultEventPublisher`.

- Takes `bootstrap.servers` and `topic.name` from system properties
- Serializes events to XML (same format as KafkaEventForwarder)
- Publishes to `opennms-fault-events` topic, keyed by node ID
- No EventClassifier needed — EventRouter already made the classification

### KafkaFaultEventConsumer (Kafka Consumer → Broadcast)

A new Spring bean that polls Kafka and delivers to local listeners.

- Subscribes to `opennms-fault-events` with consumer group `opennms-core`
- Deserializes events from XML
- Calls `EventIpcBroadcaster.broadcastNow()` — local delivery only, no re-routing
- Skips events originated by core itself (checks TSID node-id prefix to avoid echo)
- Runs on a daemon thread, started by Spring lifecycle

### Fault Event Flow (End-to-End)

```
Trapd (standalone container)
  → receives SNMP trap
  → KafkaEventForwarder → EventClassifier → FAULT
  → Kafka topic "opennms-fault-events"
  → Alarmd (standalone) polls Kafka → creates alarm
  → Core polls Kafka → KafkaFaultEventConsumer
  → EventIpcBroadcaster.broadcastNow() → local listeners
```

## Docker Compose Changes

```yaml
# All daemon containers get:
environment:
  OPENNMS_BROKER_URL: "failover:tcp://core:61616"
  # Existing Kafka config unchanged
  OPENNMS_KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"

# Core gets:
environment:
  OPENNMS_KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"
  OPENNMS_KAFKA_FAULT_TOPIC: "opennms-fault-events"
  OPENNMS_KAFKA_CONSUMER_GROUP: "opennms-core"
```

No new containers. AMQ broker is embedded in core. Kafka cluster already exists.

Once Phase 1 (AMQ IPC) works, revert `CORE_SERVICE_DISCOVERY_ENABLED` back to `"false"`
on core — standalone Discovery container handles it via AMQ.

## UEI Classification Audit (Future Critical Work)

EventClassifier routes based on UEI prefix: `uei.opennms.org/internal/*` → IPC. Many
legacy events are functionally IPC messages but live in non-internal namespaces.

**Known misclassified events (need migration or EventClassifier whitelist):**
- `uei.opennms.org/nodes/nodeAdded` — functionally IPC (lifecycle notification)
- `uei.opennms.org/nodes/nodeGainedService` — functionally DUAL

**Correctly classified:**
- `uei.opennms.org/internal/discovery/newSuspect` — IPC
- `uei.opennms.org/internal/node/nodeScanCompleted` — IPC
- `uei.opennms.org/nodes/nodeDown` — FAULT
- `uei.opennms.org/nodes/nodeUp` — FAULT
- `uei.opennms.org/nodes/nodeLostService` — FAULT

A full audit is required before all 15 daemon containers can operate end-to-end.

## Implementation Phases

### Phase 1: IPC via AMQ (unblocks cross-container daemon communication)
- Wire `JmsMessageBus` in daemon containers (replace LocalMessageBus)
- Add `OPENNMS_BROKER_URL` env var to all daemon containers in compose
- Add AMQ `ConnectionFactory` bean to `event-forwarder-kafka` blueprint
- Test: Discovery → newSuspect → AMQ → Provisiond → nodeScanCompleted → AMQ → Enlinkd

### Phase 2: Kafka on Core (unblocks core ↔ daemon fault event flow)
- Implement `FaultEventPublisher` as Kafka producer in `applicationContext-eventDaemon.xml`
- Implement `KafkaFaultEventConsumer` as Kafka consumer, broadcast-only
- Test: Trapd → trap event → Kafka → core receives → Alarmd also receives

### Phase 3: UEI Classification Audit
- Catalog all events, classify as FAULT vs IPC vs DUAL
- Migrate misclassified events (move UEIs or add to EventClassifier whitelist)
- Test: full end-to-end with all 15 daemon containers

## Key Existing Components

| Component | Location | Role |
|-----------|----------|------|
| `JmsMessageBus` | `core/messagebus-jms/` | AMQ-backed MessageBus (JMS Topics) |
| `LocalMessageBus` | `core/messagebus-api/local/` | In-JVM only (current daemon default) |
| `MessageBusEventListenerBridge` | `features/events/daemon/bridge/` | MessageBus → @EventHandler |
| `EventClassifier` | `features/events/daemon/router/` | FAULT/IPC/DUAL routing |
| `EventRouter` | `features/events/daemon/router/` | Dispatches to Kafka, MessageBus, broadcast |
| `KafkaEventForwarder` | `core/event-forwarder-kafka/` | Event enrichment + Kafka/MessageBus routing |
| `KafkaEventSubscriptionService` | `core/event-forwarder-kafka/` | Kafka consumer for daemon containers |
| `KafkaEventIpcManagerAdapter` | `core/event-forwarder-kafka/` | Composite EventIpcManager for daemons |
| `activemq-dispatcher.xml` | `features/activemq/broker/` | AMQ network connector pattern |
