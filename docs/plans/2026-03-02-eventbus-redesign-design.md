# EventBus Redesign: Kafka Fault Events, JMS IPC MessageBus, Microservice Extraction

**Date:** 2026-03-02
**Status:** Approved
**Target:** New major version (clean break, no backward compatibility)

## Objectives

1. **Stop persisting events to PostgreSQL.** Remove the DB bottleneck and the blocking dependency on the `eventsNxtId` sequence that gates event publishing.
2. **Enable cross-JVM daemon communication.** ServiceDaemons running in separate containers must communicate via a message broker.
3. **Isolate IPC messages from fault events.** Fault events flow through Kafka. IPC messages flow through JMS/ActiveMQ as a separate `MessageBus` with a new `IpcMessage` POJO — not Event objects.

The immediate outcome is enabling Phase 1 of the microservice plan: extracting Alarmd into its own container by disabling all other daemons in `service-configuration.xml`.

## Approach: Event Router Pattern

A new `EventRouter` replaces the internals of `EventIpcManagerDefaultImpl`. The `EventIpcManager` interface is preserved as a facade. The router classifies incoming events and delegates to the appropriate transport.

```
EventIpcManager (unchanged interface)
  +-- EventRouterImpl (new implementation)
       |-- FaultEventPublisher  -> Kafka ("opennms-fault-events" topic)
       |-- IpcMessagePublisher  -> JMS/ActiveMQ (domain-scoped topics)
       +-- LocalEventBroadcaster -> in-JVM listener dispatch
```

## 1. New Interfaces and Event Classification

### IpcMessage POJO

Lightweight replacement for Event XML objects in inter-daemon communication:

```java
public class IpcMessage implements Serializable {
    private String type;              // e.g., "reloadDaemonConfig", "newSuspect"
    private String source;            // originating daemon
    private long timestamp;
    private Long nodeId;              // optional context
    private String interfaceAddress;  // optional context
    private Map<String, String> parameters;
}
```

### MessageBus Interface

```java
public interface MessageBus {
    void publish(IpcMessage message);
    void subscribe(String messageType, MessageHandler handler);
    void subscribe(Collection<String> messageTypes, MessageHandler handler);
    void unsubscribe(MessageHandler handler);
}

public interface MessageHandler {
    String getName();
    void onMessage(IpcMessage message);
}
```

### Classification Rules

The `EventRouterImpl` classifies events using this precedence:

1. **Has `alarm-data`** -> Fault event -> Kafka (even if UEI is `internal/*`)
2. **UEI starts with `uei.opennms.org/internal/`** and no alarm-data -> IPC -> convert to `IpcMessage` -> JMS
3. **Everything else** (traps, syslog, thresholds, node events) -> Fault event -> Kafka

Edge cases where internal events have alarm-data (e.g., `reloadDaemonConfigFailed`) go to both Kafka and JMS.

### Processing Pipeline

**Current:** EventExpander -> HibernateEventWriter -> EventIpcBroadcastProcessor

**New:** EventExpander -> TsidAssigner -> EventRouter (classify -> publish to Kafka/JMS -> local broadcast)

`HibernateEventWriter` is removed entirely.

## 2. Kafka Fault Event Pipeline

### Topic Design

```
Topic: opennms-fault-events
  Partitions: configurable (default 16)
  Key: nodeId (Long)
  Value: Event serialized as Protobuf (existing EventsProto.proto)
  Retention: configurable (default 30 days)
```

Partitioned by `nodeId` to guarantee per-node event ordering, which is critical for alarm reduction logic.

### FaultEventPublisher

An `EventProcessor` implementation that replaces `HibernateEventWriter` in the processing chain. Serializes events to Protobuf using the existing `ProtobufMapper` and publishes to Kafka with `nodeId` as the partition key.

### Alarmd as Kafka Consumer

Alarmd subscribes to `opennms-fault-events` as consumer group `alarmd-group`. Receives the full Event object from Kafka deserialization. No DB event lookup needed.

### Event Retention and Lookup

- Recent events: Kafka consumer seek by timestamp
- Event history for an alarm: stored in `last_event_data` JSONB on the alarm row
- UI event list: future Kafka-backed materialized view (optional, Phase 2+)

## 3. IPC MessageBus over JMS/ActiveMQ

### Embedded Broker Topology

Each JVM embeds an ActiveMQ broker. In monolith mode, all daemons connect via `vm://localhost`. In microservice mode, brokers join a network-of-brokers topology via `networkConnector` over TCP.

```properties
# Monolith
org.opennms.messagebus.broker-url=vm://localhost

# Microservice
org.opennms.messagebus.broker-url=tcp://eventd-host:61616
org.opennms.messagebus.network-connectors=tcp://pollerd-host:61616,tcp://alarmd-host:61616
```

### JMS Topic Design

| JMS Topic | Message Types | Producers | Consumers |
|-----------|--------------|-----------|-----------|
| `opennms.ipc.config` | reloadDaemonConfig, reloadDaemonConfigSuccessful/Failed, thresholdConfigChange, eventsConfigChange, schedOutagesChanged | WebUI, Admin Shell | All daemons |
| `opennms.ipc.discovery` | newSuspect, discoveryConfigChange, discPause/discResume | Discovery, WebUI | Provisiond, Discovery |
| `opennms.ipc.provisioning` | reloadImport, importStarted/Successful/Failed, nodeScanCompleted/Aborted | Provisiond, WebUI | Discovery, Pollerd, Collectd |
| `opennms.ipc.polling` | outageCreated/Resolved, suspendPollingService/resumePollingService | Pollerd | RTC, Correlator |
| `opennms.ipc.node` | addNode, deleteNode, deleteInterface, changeService, interfaceManaged/Unmanaged | WebUI, Provisiond | Pollerd, Collectd, Enlinkd |
| `opennms.ipc.system` | monitoringSystemAdded/Deleted/LocationChanged, authentication events | Various | MinionStatusTracker, WebUI |

### Daemon Migration Pattern

Listeners migrate from:
```java
eventIpcManager.addEventListener(this, EventConstants.RELOAD_DAEMON_CONFIG_UEI);
```
To:
```java
messageBus.subscribe("reloadDaemonConfig", this::onReloadConfig);
```

Producers migrate from:
```java
eventForwarder.sendNow(new EventBuilder("uei.opennms.org/internal/reloadDaemonConfig", "webui").getEvent());
```
To:
```java
messageBus.publish(new IpcMessage("reloadDaemonConfig", "webui", Map.of("daemonName", "pollerd")));
```

## 4. TSID Generation

64-bit Snowflake-style time-sorted IDs replace the PostgreSQL `eventsNxtId` sequence:

```
63                                              0
|-- 42 bits: milliseconds since epoch --|-- 10 bits: node --|-- 12 bits: sequence --|
```

- 42 bits of time: ~139 years
- 10 bits of node ID: 1024 JVM instances
- 12 bits of sequence: 4096 events/ms/node

`TsidAssigner` runs as the first processor after `EventExpander`. Every event gets an ID before classification or publishing. No blocking, no DB round-trip.

Node ID is configurable via `org.opennms.tsid.node-id`, auto-derived from container hostname hash, or coordinated via file-based claim.

## 5. Alarm Schema Changes

### New Columns on `alarms` Table

```sql
ALTER TABLE alarms ADD COLUMN event_tsid BIGINT NOT NULL;
ALTER TABLE alarms ADD COLUMN event_uei VARCHAR(256) NOT NULL;
ALTER TABLE alarms ADD COLUMN event_source VARCHAR(256);
ALTER TABLE alarms ADD COLUMN event_severity INTEGER;
ALTER TABLE alarms ADD COLUMN event_timestamp TIMESTAMPTZ;
ALTER TABLE alarms ADD COLUMN event_node_id BIGINT;
ALTER TABLE alarms ADD COLUMN event_log_msg TEXT;
ALTER TABLE alarms ADD COLUMN last_event_data JSONB;

CREATE INDEX idx_alarms_event_tsid ON alarms (event_tsid);

ALTER TABLE alarms DROP CONSTRAINT IF EXISTS fk_alarms_eventid;
ALTER TABLE alarms DROP COLUMN IF EXISTS lasteventid;
ALTER TABLE alarms DROP COLUMN IF EXISTS firsteventid;
```

### AlarmPersisterImpl

Removes `eventDao.get(event.getDbid())`. Populates alarm directly from the in-memory Event object: denormalized columns for indexed queries + full event serialized as JSONB in `last_event_data`.

### Removed

- `events` table + `eventsNxtId` sequence
- `HibernateEventWriter`
- `EventDao` / `EventDaoHibernate`
- `OnmsEvent` JPA entity (kept temporarily as transient mapping utility)
- `EventIpcBroadcastProcessor` (absorbed into `EventRouter`)

## 6. Alarmd Extraction (Phase 1 Microservice)

### Container Topology

```
+------------------------------------------+
|  Core Container (monolith minus Alarmd)  |
|                                          |
|  service-configuration.xml:              |
|    Eventd Y  Pollerd Y  Collectd Y      |
|    Provisiond Y  Discovery Y  Notifd Y  |
|    Alarmd N (disabled)                   |
|                                          |
|  EventRouter -> Kafka + ActiveMQ         |
+------------------------------------------+
          |              |
     Kafka|         JMS/TCP
          |              |
+------------------------------------------+
|  Alarmd Container                        |
|                                          |
|  service-configuration.xml:              |
|    Manager Y  Alarmd Y                   |
|    (everything else disabled)            |
|                                          |
|  KafkaFaultEventConsumer -> Alarmd       |
|  Alarmd -> AlarmPersister -> PostgreSQL  |
+------------------------------------------+
```

### Alarmd Container Dependencies

| Dependency | Satisfied By |
|-----------|-------------|
| Fault events | Kafka consumer on `opennms-fault-events` |
| IPC messages (config reload) | JMS subscriber via network-of-brokers |
| Alarm persistence | Direct PostgreSQL connection (shared DB) |
| Drools rules | Packaged in container classpath |
| Northbound interfaces | Packaged in container |

### Transport Modes

```properties
# Small install: in-memory event routing (no Kafka)
org.opennms.eventrouter.fault-transport=local

# Production / microservice: Kafka-backed
org.opennms.eventrouter.fault-transport=kafka
org.opennms.eventrouter.kafka.bootstrap-servers=kafka:9092
```

When `fault-transport=local`, the EventRouter bypasses Kafka and broadcasts fault events directly to in-JVM listeners. Microservice extraction requires `fault-transport=kafka`.

## 7. Risk Areas

1. **Events table removal breaks reporting.** Jasper Reports, database reports, and Vacuumd automations reference the events table. Must audit and migrate to alarm data or Kafka-backed materialized views.
2. **Vacuumd SQL automations.** Vacuumd runs SQL-based automations referencing the events table directly. These need rewriting.
3. **Notifd dependency.** Notifd listens to all events for notification triggers. Needs the same Kafka consumer treatment as Alarmd (Phase 2).
4. **Third-party JDBC integrations.** External dashboards, Grafana datasource configs, and custom scripts querying the events table will break.
5. **Event-to-alarm timing.** Kafka per-partition ordering guarantees correct sequencing per node. Alarmd must handle events entirely from Kafka messages with no DB fallback.

## New Components Summary

| Component | Module | Description |
|-----------|--------|-------------|
| `EventRouter` / `EventRouterImpl` | `features/events/daemon` | Classifies events, delegates to Kafka/JMS/local |
| `FaultEventPublisher` | `features/events/daemon` | EventProcessor publishing to Kafka |
| `InMemoryFaultEventPublisher` | `features/events/daemon` | Local-mode alternative for small installs |
| `IpcMessage` | `core/messagebus-api` (new) | IPC message POJO |
| `MessageBus` / `MessageHandler` | `core/messagebus-api` (new) | IPC pub/sub interface |
| `JmsMessageBus` | `core/messagebus-jms` (new) | ActiveMQ MessageBus implementation |
| `TsidAssigner` | `features/events/daemon` | EventProcessor generating TSIDs |
| `TsidFactory` | `core/tsid` (new) | TSID generation utility |
| `KafkaFaultEventConsumer` | `features/events/kafka-consumer` (new) | Kafka consumer feeding events to local listeners |
