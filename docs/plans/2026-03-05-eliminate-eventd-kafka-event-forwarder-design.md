# Design: Eliminate Eventd via KafkaEventForwarder Library

**Date:** 2026-03-05
**Branch:** eventbus-redesign
**Status:** Approved

## Problem Statement

OpenNMS daemons are coupled through a centralized Eventd daemon that runs an in-process event bus (`EventIpcManager`). All daemons in the same JVM publish and subscribe to events through this singleton. This prevents running daemons in separate JVM containers because:

1. `EventIpcManager` is in-memory only — no network transport
2. Daemons inherit event system beans from a shared Spring context hierarchy (`daemonContext`)
3. Event enrichment (`EventExpander`) and routing (`EventRouter`) run inside Eventd's processing pipeline
4. `@EventHandler` annotations register listeners with the local `EventIpcManager` — cross-JVM delivery is impossible

## Context: What the eventbus-redesign Branch Has Already Done

The branch has laid significant groundwork:

- **EventRouter** classifies events into FAULT (external UEIs with alarmData → Kafka), IPC (internal UEIs → MessageBus), and DUAL (both)
- **Event persistence removed from pipeline** — `HibernateEventWriter` is no longer in the processor chain
- **`events` table dropped** — replaced by `events_archive`, populated by `EventArchiveWriter` consuming from Kafka
- **All UI/REST/Report consumers migrated** to `JdbcEventStore` querying `events_archive`
- **Standalone Alarmd** already runs in its own JVM/container consuming FAULT events from Kafka
- **MessageBusEventListenerBridge** adapts `@EventHandler` annotations for IPC events to the `MessageBus` abstraction
- **JmsMessageBus** provides remote-capable MessageBus over ActiveMQ JMS Topics

## Solution: KafkaEventForwarder Library

### Architecture

Replace the centralized Eventd daemon with a **KafkaEventForwarder** library that every daemon container carries. Each daemon enriches and publishes its own events. No central event daemon.

```
Any Daemon Container (Pollerd, Collectd, Provisiond, etc.)
  daemon code
    eventForwarder.sendNow(event)
      KafkaEventForwarder (implements EventForwarder)
        EventExpander       — enrich from local event.conf
        TsidAssigner        — assign unique event ID
        EventRouter logic:
          FAULT → KafkaProducer (fault-events topic)
          IPC   → JmsMessageBus (ActiveMQ topic)
          DUAL  → both

Consumers:
  Alarmd         — Kafka consumer (fault-events topic) → creates alarms
  EventArchiver  — Kafka consumer (fault-events topic) → writes events_archive
  Notifd         — Kafka consumer (fault-events topic, filtered by UEI) → sends notifications
  Any daemon     — JMS subscriber (IPC messages) → handles reloadDaemonConfig, etc.
```

### Component Changes

| Component | Current State | Target State |
|-----------|--------------|--------------|
| Eventd daemon | Active ServiceDaemon, hardcoded enabled in confd | **Eliminated** — disabled in all containers |
| EventIpcManager | In-process singleton, local broadcast | **Eliminated** — replaced by KafkaEventForwarder |
| EventForwarder impl | `EventIpcManagerDefaultImpl` (local only) | `KafkaEventForwarder` (Kafka + JMS) |
| EventExpander | Runs inside Eventd pipeline | Library call inside KafkaEventForwarder |
| EventSubscriptionService | Local listener registration via EventIpcManager | `KafkaEventSubscriptionService` (Kafka consumer) |
| `@EventHandler` (FAULT) | `AnnotationBasedEventListenerAdapter` → EventIpcManager | `KafkaAnnotationEventListenerAdapter` → Kafka consumer |
| `@EventHandler` (IPC) | `MessageBusEventListenerBridge` → MessageBus | No change — already remote-capable via JmsMessageBus |
| Event persistence | `EventArchiveWriter` consuming from Kafka | No change |
| Alarmd | Standalone Kafka consumer | No change |
| Trapd/Syslogd | Submit to local Eventd | Publish directly via KafkaEventForwarder |

### What Stays the Same

- `EventForwarder` interface (4 methods: sendNow/sendNowSync for Event/Log)
- Kafka fault-events topic
- `events_archive` table + `EventArchiveWriter`
- `MessageBus` abstraction (local or JMS-backed)
- `EventConfDao` (file-based event.conf)
- Alarmd as standalone Kafka consumer

## New Modules

### 1. `core/event-forwarder-kafka/`

**Purpose:** `EventForwarder` implementation that enriches events locally and publishes to Kafka (FAULT) or JMS (IPC).

```java
public class KafkaEventForwarder implements EventForwarder {
    private final EventExpander eventExpander;
    private final TsidAssigner tsidAssigner;
    private final KafkaProducer<String, byte[]> kafkaProducer;
    private final MessageBus messageBus;
    private final Function<Event, byte[]> serializer; // ProtobufMapper

    @Override
    public void sendNow(Event event) {
        // 1. Enrich
        eventExpander.process(eventLog(event));
        // 2. Assign TSID
        tsidAssigner.process(eventLog(event));
        // 3. Classify and route
        EventClassification type = classify(event);
        switch (type) {
            case FAULT -> kafkaProducer.send(new ProducerRecord<>(
                FAULT_TOPIC, nodeKey(event), serializer.apply(event)));
            case IPC -> messageBus.publish(toIpcMessage(event));
            case DUAL -> {
                kafkaProducer.send(new ProducerRecord<>(
                    FAULT_TOPIC, nodeKey(event), serializer.apply(event)));
                messageBus.publish(toIpcMessage(event));
            }
        }
    }

    @Override
    public void sendNowSync(Event event) {
        sendNow(event); // Kafka acks provide delivery guarantee
    }

    private EventClassification classify(Event event) {
        boolean isInternal = event.getUei().startsWith("uei.opennms.org/internal/");
        boolean hasAlarmData = event.getAlarmData() != null;
        if (isInternal && hasAlarmData) return DUAL;
        if (isInternal) return IPC;
        return FAULT;
    }
}
```

**Dependencies:**
- `EventConfDao` — loaded from local `event.conf` files
- `EventUtil` — parameter expansion
- `TsidAssigner` — TSID generation (already per-JVM via `org.opennms.tsid.node-id`)
- `KafkaProducer` — standard Kafka client
- `MessageBus` — JmsMessageBus (ActiveMQ-backed)
- `ProtobufMapper` — event serialization for Kafka

### 2. `core/event-consumer-kafka/`

**Purpose:** `EventSubscriptionService` implementation that consumes events from Kafka and dispatches to `@EventHandler` annotated methods.

```java
public class KafkaEventSubscriptionService implements EventSubscriptionService {
    private final KafkaConsumer<String, byte[]> consumer;
    private final Map<String, List<EventListener>> ueiListeners;
    private final Function<byte[], Event> deserializer;

    @Override
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        for (String uei : ueis) {
            ueiListeners.computeIfAbsent(uei, k -> new ArrayList<>()).add(listener);
        }
    }

    // Consumer poll loop (runs in daemon thread)
    public void pollLoop() {
        while (running) {
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(100));
            for (var record : records) {
                Event event = deserializer.apply(record.value());
                dispatch(event);
            }
        }
    }

    private void dispatch(Event event) {
        String uei = event.getUei();
        // Exact match
        List<EventListener> listeners = ueiListeners.get(uei);
        if (listeners != null) {
            listeners.forEach(l -> l.onEvent(event));
        }
        // Wildcard match (prefix listeners)
        // ... matching logic from EventIpcManagerDefaultImpl
    }
}
```

**Kafka Consumer Group Design:**
- Each daemon type uses its own consumer group: `opennms-pollerd`, `opennms-notifd`, `opennms-alarmd`
- Kafka delivers each event to ONE consumer per group → all daemon types receive every event
- Within a daemon type, partitions distribute load across scaled instances
- This provides both fanout (every daemon type sees every event) and load balancing (within a daemon type)

### 3. `KafkaAnnotationEventListenerAdapter`

Replaces `AnnotationBasedEventListenerAdapter` for FAULT events:

```java
public class KafkaAnnotationEventListenerAdapter {
    private final KafkaEventSubscriptionService subscriptionService;

    public void register(Object bean) {
        // Scan for @EventHandler annotations
        // Extract UEI list
        // Register with KafkaEventSubscriptionService
    }
}
```

## Daemon Container Requirements

Each daemon container needs:

| Dependency | Source | Notes |
|-----------|--------|-------|
| Kafka connectivity | `kafka:9092` | Publish and consume events |
| ActiveMQ connectivity | `core:61616` or standalone broker | IPC messages (reloadDaemonConfig, etc.) |
| PostgreSQL connectivity | `postgres:5432` | Daemon-specific DAOs |
| `event.conf` | Local file (container volume/overlay) | For EventExpander enrichment |
| Daemon-specific config | Local files | poller-config.xml, collectd-config.xml, etc. |
| `org.opennms.tsid.node-id` | Unique per container | TSID generation |

**Not needed:**
- Shared Spring context hierarchy (commonContext → daemonContext → ...)
- OSGi service registry / Karaf
- EventIpcManager
- Eventd daemon
- ContextRegistry / BeanFactoryLocator

## Spring Context Flattening

Current hierarchy (single JVM):
```
commonContext → daemonContext → daoContext → pollerConfigContext → thresholdingContext
                                                                      └→ pollerdContext
```

Target (per-daemon container):
```
pollerd-standalone-context.xml
  - DataSource (direct PostgreSQL)
  - DAOs (NodeDao, MonitoredServiceDao, OutageDao, etc.)
  - KafkaEventForwarder (replaces EventIpcManager)
  - KafkaEventSubscriptionService (replaces local listener registration)
  - JmsMessageBus (ActiveMQ-backed, replaces LocalMessageBus)
  - EventConfDao (file-based, local event.conf)
  - PollerConfig, ThresholdingConfig (local files)
  - Poller daemon bean
```

Each daemon gets a single flat Spring context with only the beans it needs. No parent chain, no OSGi, no Karaf.

## Event Flow Examples

### Pollerd detects service down (FAULT event)

```
Pollerd container:
  1. Poller detects service down
  2. Creates Event with UEI=uei.opennms.org/nodes/nodeLostService
  3. eventForwarder.sendNow(event)
  4. KafkaEventForwarder:
     a. EventExpander enriches (severity, description, logmsg from event.conf)
     b. TsidAssigner assigns unique ID
     c. classify() → FAULT (not internal, has alarmData)
     d. KafkaProducer.send(fault-events topic, event)

Alarmd container (Kafka consumer group: opennms-alarmd):
  5. Receives event from Kafka
  6. Creates/updates alarm in PostgreSQL

EventArchiver container (Kafka consumer group: opennms-event-archiver):
  7. Receives event from Kafka
  8. Writes to events_archive table

Notifd container (Kafka consumer group: opennms-notifd):
  9. Receives event from Kafka
  10. Evaluates notification rules, sends notification
```

### Admin reloads daemon config (IPC event)

```
Webapp container:
  1. Admin clicks "Reload Daemon Config" in UI
  2. REST endpoint calls eventForwarder.sendNow(reloadDaemonConfig event)
  3. KafkaEventForwarder:
     a. classify() → IPC (internal UEI, no alarmData)
     b. messageBus.publish(IpcMessage("reloadDaemonConfig", ...))
     c. JmsMessageBus publishes to ActiveMQ topic

Pollerd container (JMS subscriber):
  4. MessageBusEventListenerBridge receives IPC message
  5. Invokes @EventHandler(uei="uei.opennms.org/internal/reloadDaemonConfig")
  6. Pollerd reloads poller-config.xml
```

## Implementation Phases

### Phase 1: KafkaEventForwarder Library
- Create `core/event-forwarder-kafka/` module
- Implement `KafkaEventForwarder` with embedded EventExpander + TsidAssigner + EventRouter logic
- Unit tests with embedded Kafka

### Phase 2: KafkaEventSubscriptionService
- Create `core/event-consumer-kafka/` module
- Implement Kafka consumer with UEI-based filtering and dispatch
- Implement `KafkaAnnotationEventListenerAdapter`
- Unit tests with embedded Kafka

### Phase 3: Wire First Daemon (Pollerd)
- Create `pollerd-standalone-context.xml` — flat Spring context
- Replace EventIpcManager references with KafkaEventForwarder
- Replace EventSubscriptionService with KafkaEventSubscriptionService
- Test in Strike Fighter Docker Compose

### Phase 4: Make Eventd Disableable
- Update confd template: add `CORE_SERVICE_EVENTD_ENABLED` env var
- In Strike Fighter compose, disable Eventd on all containers
- Verify event flow works entirely through Kafka/JMS

### Phase 5: Extract Remaining Daemons
- Repeat Phase 3 pattern for Collectd, Provisiond, Notifd, etc.
- Each daemon gets its own flat Spring context and container definition

## Kafka Topic Design

| Topic | Content | Producers | Consumers |
|-------|---------|-----------|-----------|
| `fault-events` | All FAULT + DUAL events (Protobuf) | Every daemon via KafkaEventForwarder | Alarmd, EventArchiver, Notifd, any daemon needing FAULT events |
| `OpenNMS.IPC.*` | IPC messages (JMS Topics via ActiveMQ) | Any daemon via JmsMessageBus | Daemons subscribed to specific IPC message types |

**Consumer Group Convention:** `opennms-<daemon-name>` (e.g., `opennms-alarmd`, `opennms-notifd`, `opennms-event-archiver`)

## Migration Path

The KafkaEventForwarder can coexist with EventIpcManager during migration:

1. **Hybrid mode:** KafkaEventForwarder publishes to Kafka AND calls local EventIpcManager.broadcastNow() — allows gradual migration of listeners
2. **Kafka-only mode:** KafkaEventForwarder publishes to Kafka only — all listeners must be Kafka consumers
3. **Per-daemon toggle:** Each daemon can independently switch from local to Kafka-backed event handling

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Increased event latency (Kafka vs in-memory) | Microseconds → milliseconds | Acceptable for all known use cases; Kafka partitioning provides throughput |
| Event ordering across partitions | Events for different nodes may arrive out of order | Key by node_id — events for same node stay ordered |
| EventConfDao divergence across containers | Different containers could have stale event.conf | Mount shared config volume or use config management |
| Kafka unavailability | Events lost during Kafka outage | KafkaProducer retry + acks=all; consider local queue for buffering |
| Consumer group rebalancing | Brief processing gap during daemon restart | Standard Kafka consumer group protocol handles this |

## Success Criteria

1. All daemons can run in separate containers with no shared JVM state
2. Events flow through Kafka/JMS with no EventIpcManager involvement
3. Eventd daemon is disabled in all containers
4. No Spring context hierarchy — each daemon has a flat, self-contained context
5. Event latency (publish to consume) under 100ms at steady state
6. Zero event loss under normal operation (Kafka acks=all)
