# Kafka IPC Topic Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace AMQ-based IPC event transport with a second Kafka topic (`opennms-ipc-events`), making Kafka the single inter-service transport.

**Architecture:** Two Kafka topics — `opennms-fault-events` (traps, syslog, alarm-bearing events) and `opennms-ipc-events` (internal daemon-to-daemon events like newSuspect, nodeScanCompleted, reloadDaemonConfig). `KafkaEventForwarder` in daemon containers publishes to both topics based on `EventClassifier`. `KafkaEventSubscriptionService` subscribes to both topics. Core's `EventRouter` publishes to both topics via `FaultEventPublisher` (existing) and new `IpcEventPublisher`. Core consumes from both via existing `KafkaFaultEventConsumer` and new `KafkaIpcEventConsumer`.

**Tech Stack:** Kafka, Spring XML, OSGi Blueprint, existing `KafkaEventForwarder`/`KafkaEventSubscriptionService`/`EventRouter` classes.

**Design doc:** `docs/plans/2026-03-09-minion-first-architecture-design.md`

---

## Summary of Changes

| Component | Current (AMQ path) | New (Kafka IPC path) |
|-----------|-------------------|---------------------|
| `KafkaEventForwarder.routeEvent()` | IPC → `messageBus.publish(ipcMessage)` | IPC → `kafkaProducer.send(ipcTopic, eventXml)` |
| `KafkaEventSubscriptionService` | Subscribes to 1 topic (`opennms-fault-events`) | Subscribes to 2 topics (`opennms-fault-events`, `opennms-ipc-events`) |
| `EventRouter.process()` (core) | IPC → `messageBus.publish(ipcMessage)` | IPC → `ipcEventPublisher.process()` (Kafka) |
| Core consumption | `KafkaFaultEventConsumer` (1 topic) | + `KafkaIpcEventConsumer` (second topic) |
| `MessageBus` dependency | Required in both daemon containers and core | Removed entirely |

---

## Task 1: Modify KafkaEventForwarder to Publish IPC Events to Kafka

Replace the `publishToMessageBus()` path with `publishToIpcKafka()` using the same `KafkaProducer` but a different topic name.

**Files:**
- Modify: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarder.java`
- Modify: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarderFactory.java`

**Step 1: Read current KafkaEventForwarder**

Read `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarder.java`.

Current fields and constructor:
```java
public class KafkaEventForwarder implements EventForwarder {
    private final EventProcessor eventExpander;
    private final EventProcessor tsidAssigner;
    private final EventClassifier eventClassifier;
    private final IpcMessageConverter ipcMessageConverter;
    private final KafkaProducer<Long, byte[]> kafkaProducer;
    private final String topicName;
    volatile MessageBus messageBus;  // optional, injected via setter

    public KafkaEventForwarder(EventProcessor eventExpander,
                               EventProcessor tsidAssigner,
                               EventClassifier eventClassifier,
                               IpcMessageConverter ipcMessageConverter,
                               KafkaProducer<Long, byte[]> kafkaProducer,
                               String topicName) { ... }
```

Current routing (line ~152-171):
```java
private void routeEvent(Event event) {
    EventClassification classification = eventClassifier.classify(event);
    switch (classification) {
        case FAULT: publishToKafka(event); break;
        case IPC:   publishToMessageBus(event); break;
        case DUAL:  publishToKafka(event); publishToMessageBus(event); break;
    }
}
```

**Step 2: Replace MessageBus with IPC topic**

Changes needed in `KafkaEventForwarder.java`:

1. Remove `IpcMessageConverter ipcMessageConverter` field and constructor arg
2. Remove `volatile MessageBus messageBus` field and its setter `setMessageBus()`
3. Add `String ipcTopicName` field and setter
4. Replace `publishToMessageBus()` with `publishToIpcKafka()`:

```java
// Remove these:
//   private final IpcMessageConverter ipcMessageConverter;
//   volatile MessageBus messageBus;
//   public void setMessageBus(MessageBus messageBus) { ... }
//   private void publishToMessageBus(Event event) { ... }

// Add:
private volatile String ipcTopicName;

public void setIpcTopicName(String ipcTopicName) {
    this.ipcTopicName = ipcTopicName;
}

// Replace publishToMessageBus with:
private void publishToIpcKafka(Event event) {
    if (ipcTopicName == null) {
        LOG.debug("IPC topic not configured, dropping IPC event {}", event.getUei());
        return;
    }
    byte[] payload = JaxbUtils.marshal(event).getBytes(StandardCharsets.UTF_8);
    long key = event.getNodeid() != null ? event.getNodeid() : 0L;
    ProducerRecord<Long, byte[]> record = new ProducerRecord<>(ipcTopicName, key, payload);
    kafkaProducer.send(record, (metadata, exception) -> {
        if (exception != null) {
            LOG.error("Failed to publish IPC event {} to Kafka", event.getUei(), exception);
        }
    });
}
```

5. Update constructor to remove `ipcMessageConverter`:
```java
public KafkaEventForwarder(EventProcessor eventExpander,
                           EventProcessor tsidAssigner,
                           EventClassifier eventClassifier,
                           KafkaProducer<Long, byte[]> kafkaProducer,
                           String topicName) {
    this.eventExpander = eventExpander;
    this.tsidAssigner = tsidAssigner;
    this.eventClassifier = eventClassifier;
    this.kafkaProducer = kafkaProducer;
    this.topicName = topicName;
}
```

6. Update `routeEvent()`:
```java
private void routeEvent(Event event) {
    EventClassification classification = eventClassifier.classify(event);
    LOG.debug("Routing event {} as {}", event.getUei(), classification);
    switch (classification) {
        case FAULT: publishToKafka(event); break;
        case IPC:   publishToIpcKafka(event); break;
        case DUAL:  publishToKafka(event); publishToIpcKafka(event); break;
    }
}
```

**Step 3: Update KafkaEventForwarderFactory**

Read and modify `KafkaEventForwarderFactory.java`. Remove `IpcMessageConverter` from the `create()` method:

```java
public static KafkaEventForwarder create(String bootstrapServers, String topicName) {
    // ... existing TsidAssigner and EventClassifier creation ...
    return new KafkaEventForwarder(
            new NoOpEventProcessor(),
            tsidAssigner,
            new EventClassifier(),
            KafkaProducerFactory.create(bootstrapServers),
            topicName);
}
```

**Step 4: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.event-forwarder-kafka -am install
```

Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarder.java
git add core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarderFactory.java
git commit -m "feat: replace MessageBus IPC path with Kafka IPC topic in KafkaEventForwarder

IPC-classified events now publish to a second Kafka topic (configurable
via setIpcTopicName) instead of MessageBus/AMQ. Same KafkaProducer handles
both fault and IPC topics. Removes IpcMessageConverter and MessageBus deps."
```

---

## Task 2: Modify KafkaEventSubscriptionService to Subscribe to Multiple Topics

Change from single-topic to multi-topic subscription so daemon containers consume from both `opennms-fault-events` and `opennms-ipc-events`.

**Files:**
- Modify: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventSubscriptionService.java`

**Step 1: Read current subscription**

Current constructor takes `String topicName` (single topic). At line ~118 in `start()`:
```java
consumer.subscribe(Collections.singletonList(topicName));
```

**Step 2: Change to accept comma-separated topic names**

Change the `topicName` field to `topicNames` and split on comma:

```java
// Change field:
private final List<String> topicNames;

// Change constructor parameter:
public KafkaEventSubscriptionService(
        KafkaConsumer<Long, byte[]> consumer,
        String topicNames,           // comma-separated
        EventDeserializer deserializer,
        Duration pollTimeout,
        TsidFactory tsidFactory) {
    this.consumer = consumer;
    this.topicNames = Arrays.stream(topicNames.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    this.deserializer = deserializer;
    this.pollTimeout = pollTimeout;
    this.tsidFactory = tsidFactory;
}

// Change subscription in start():
consumer.subscribe(topicNames);
```

Also update the `create()` factory method parameter name from `topicName` to `topicNames`:
```java
public static KafkaEventSubscriptionService create(
        String bootstrapServers,
        String consumerGroupId,
        String topicNames,      // comma-separated
        long pollTimeoutMs) {
    // ... same implementation, topicNames passed through
}
```

The rest of the class is unchanged — the poll loop and dispatch logic work regardless of how many topics are subscribed.

**Step 3: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.event-forwarder-kafka -am install
```

**Step 4: Commit**

```bash
git add core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventSubscriptionService.java
git commit -m "feat: support multiple Kafka topics in KafkaEventSubscriptionService

Accept comma-separated topic names so daemon containers can subscribe to
both opennms-fault-events and opennms-ipc-events from a single consumer."
```

---

## Task 3: Update Blueprint and Config for Dual Topics

Wire the second topic in the daemon container blueprint and config files.

**Files:**
- Modify: `core/event-forwarder-kafka/src/main/resources/OSGI-INF/blueprint/blueprint-event-forwarder-kafka.xml`
- Modify: `opennms-container/delta-v/*/etc/org.opennms.core.event.forwarder.kafka.cfg` (all 14 daemon overlays)

**Step 1: Update blueprint default properties**

In `blueprint-event-forwarder-kafka.xml`, add IPC topic config:

```xml
<cm:property-placeholder id="eventForwarderKafkaProperties"
                         persistent-id="org.opennms.core.event.forwarder.kafka"
                         update-strategy="reload">
    <cm:default-properties>
        <cm:property name="bootstrap.servers" value="localhost:9092"/>
        <cm:property name="topic.name" value="opennms-fault-events"/>
        <cm:property name="ipc.topic.name" value="opennms-ipc-events"/>
        <cm:property name="consumer.group.id" value="opennms-core"/>
        <cm:property name="poll.timeout.ms" value="100"/>
    </cm:default-properties>
</cm:property-placeholder>
```

**Step 2: Update KafkaEventForwarder bean to set IPC topic**

```xml
<bean id="kafkaEventForwarder"
      class="org.opennms.core.event.forwarder.kafka.KafkaEventForwarderFactory"
      factory-method="create">
    <argument value="${bootstrap.servers}"/>
    <argument value="${topic.name}"/>
    <property name="ipcTopicName" value="${ipc.topic.name}"/>
</bean>
```

Note: Remove the `<property name="messageBus" .../>` line (MessageBus no longer needed).

**Step 3: Update KafkaEventSubscriptionService to subscribe to both topics**

```xml
<bean id="kafkaEventSubscriptionService"
      class="org.opennms.core.event.forwarder.kafka.KafkaEventSubscriptionService"
      factory-method="create"
      init-method="start"
      destroy-method="stop">
    <argument value="${bootstrap.servers}"/>
    <argument value="${consumer.group.id}"/>
    <argument value="${topic.name},${ipc.topic.name}"/>
    <argument value="${poll.timeout.ms}" type="long"/>
</bean>
```

The third argument is now `"opennms-fault-events,opennms-ipc-events"` (comma-separated).

**Step 4: Remove MessageBus OSGi reference from blueprint**

Remove this line from the blueprint:
```xml
<!-- REMOVE: <reference id="messageBus" interface="org.opennms.core.messagebus.MessageBus" availability="optional"/> -->
```

**Step 5: Update daemon overlay config files**

Each daemon container's `org.opennms.core.event.forwarder.kafka.cfg` already has `bootstrap.servers` and `topic.name`. Add the IPC topic:

```properties
bootstrap.servers = kafka:9092
topic.name = opennms-fault-events
ipc.topic.name = opennms-ipc-events
```

Update all 14 daemon overlay config files:
- `pollerd-daemon-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `collectd-daemon-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `alarmd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `rtcd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `passivestatusd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `notifd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `discovery-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `trapd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `syslogd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `ticketer-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `eventtranslator-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `enlinkd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- `scriptd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`

Use a script:
```bash
cd opennms-container/delta-v
for cfg in */etc/org.opennms.core.event.forwarder.kafka.cfg; do
  if ! grep -q "ipc.topic.name" "$cfg" 2>/dev/null; then
    echo 'ipc.topic.name = opennms-ipc-events' >> "$cfg"
  fi
done
```

**Step 6: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.event-forwarder-kafka -am install
```

**Step 7: Commit**

```bash
git add core/event-forwarder-kafka/src/main/resources/OSGI-INF/blueprint/blueprint-event-forwarder-kafka.xml
git add opennms-container/delta-v/*/etc/org.opennms.core.event.forwarder.kafka.cfg
git commit -m "feat: configure dual Kafka topics in daemon container blueprint

Blueprint now wires ipc.topic.name for KafkaEventForwarder and subscribes
KafkaEventSubscriptionService to both fault and IPC topics. Removes
MessageBus OSGi reference. All daemon overlay configs updated."
```

---

## Task 4: Create IpcEventPublisher for Core's EventRouter

Core needs a Kafka publisher for IPC events, similar to `FaultEventPublisher` but targeting `opennms-ipc-events`.

**Files:**
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/IpcEventPublisher.java`
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml`

**Step 1: Create IpcEventPublisher**

This class wraps a `KafkaProducer` to publish IPC events. It implements `EventProcessor` so `EventRouter` can call it the same way as `FaultEventPublisher`.

```java
package org.opennms.netmgt.eventd.processor;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes IPC events to Kafka {@code opennms-ipc-events} topic.
 * Same structure as {@link FaultEventPublisher} but for internal/IPC events.
 */
public class IpcEventPublisher implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(IpcEventPublisher.class);

    private final KafkaProducer<Long, byte[]> producer;
    private final String topicName;
    private final Function<Event, byte[]> serializer;

    public IpcEventPublisher(KafkaProducer<Long, byte[]> producer,
                             String topicName,
                             Function<Event, byte[]> serializer) {
        this.producer = producer;
        this.topicName = topicName;
        this.serializer = serializer;
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog.getEvents() == null) return;
        for (Event event : eventLog.getEvents().getEventCollection()) {
            publishEvent(event);
        }
    }

    private void publishEvent(Event event) {
        try {
            byte[] payload = serializer.apply(event);
            long key = event.getNodeid() != null ? event.getNodeid() : 0L;
            ProducerRecord<Long, byte[]> record = new ProducerRecord<>(topicName, key, payload);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    LOG.error("Failed to publish IPC event {} to Kafka", event.getUei(), exception);
                }
            });
            LOG.debug("Published IPC event to Kafka: uei={} topic={}", event.getUei(), topicName);
        } catch (Exception e) {
            LOG.error("Error serializing IPC event {}", event.getUei(), e);
        }
    }
}
```

Create at: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/IpcEventPublisher.java`

**Step 2: Wire IpcEventPublisher in applicationContext-eventDaemon.xml**

Add after the `faultEventPublisher` bean:

```xml
<!-- IPC event publisher: sends internal/IPC events to Kafka opennms-ipc-events topic.
     Uses the same Kafka producer connection as FaultEventPublisher. -->
<bean id="ipcEventPublisher"
      class="org.opennms.netmgt.eventd.processor.IpcEventPublisherFactory"
      factory-method="create">
    <constructor-arg value="${org.opennms.kafka.bootstrap.servers:localhost:9092}"/>
    <constructor-arg value="${org.opennms.kafka.ipc.topic:opennms-ipc-events}"/>
</bean>
```

Wait — we can reuse `FaultEventPublisherFactory` since `IpcEventPublisher` has the same structure.
Actually, let's just create the bean inline (same pattern as `faultEventPublisher`):

```xml
<bean id="ipcEventPublisher"
      class="org.opennms.netmgt.eventd.processor.IpcEventPublisher">
    <constructor-arg>
        <bean class="org.opennms.netmgt.eventd.processor.FaultEventPublisherFactory"
              factory-method="createProducer">
            <constructor-arg value="${org.opennms.kafka.bootstrap.servers:localhost:9092}"/>
        </bean>
    </constructor-arg>
    <constructor-arg value="${org.opennms.kafka.ipc.topic:opennms-ipc-events}"/>
    <constructor-arg>
        <bean class="org.opennms.netmgt.eventd.processor.XmlEventSerializer"/>
    </constructor-arg>
</bean>
```

Alternatively, add a static factory method to `FaultEventPublisherFactory` that just creates a producer, or create a new simple factory. Simplest approach — duplicate the factory pattern:

Create `IpcEventPublisherFactory`:
```java
package org.opennms.netmgt.eventd.processor;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;

public class IpcEventPublisherFactory {
    private IpcEventPublisherFactory() {}

    public static IpcEventPublisher create(String bootstrapServers, String topicName) {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        KafkaProducer<Long, byte[]> producer =
                new KafkaProducer<>(props, new LongSerializer(), new ByteArraySerializer());
        return new IpcEventPublisher(producer, topicName, new XmlEventSerializer());
    }
}
```

Create at: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/IpcEventPublisherFactory.java`

Then the Spring XML bean is simply:
```xml
<bean id="ipcEventPublisher"
      class="org.opennms.netmgt.eventd.processor.IpcEventPublisherFactory"
      factory-method="create">
    <constructor-arg value="${org.opennms.kafka.bootstrap.servers:localhost:9092}"/>
    <constructor-arg value="${org.opennms.kafka.ipc.topic:opennms-ipc-events}"/>
</bean>
```

**Step 3: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.features.events.daemon -am install
```

**Step 4: Commit**

```bash
git add features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/IpcEventPublisher.java
git add features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/IpcEventPublisherFactory.java
git add features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml
git commit -m "feat: add IpcEventPublisher for core's Kafka IPC topic

Publishes IPC-classified events to opennms-ipc-events Kafka topic.
Same pattern as FaultEventPublisher but for internal daemon-to-daemon events."
```

---

## Task 5: Modify EventRouter to Use IpcEventPublisher Instead of MessageBus

Replace the MessageBus dependency in `EventRouter` with the new `IpcEventPublisher`.

**Files:**
- Modify: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/router/EventRouter.java`
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml`

**Step 1: Read EventRouter**

Current constructor:
```java
public EventRouter(EventClassifier classifier,
                   EventProcessor faultEventPublisher,
                   MessageBus messageBus,
                   EventIpcBroadcaster localBroadcaster,
                   IpcMessageConverter ipcMessageConverter)
```

Current IPC path:
```java
private void publishIpcMessage(Event event) {
    IpcMessage message = ipcMessageConverter.convert(event);
    messageBus.publish(message);
}
```

**Step 2: Replace MessageBus with IpcEventPublisher**

New constructor:
```java
public EventRouter(EventClassifier classifier,
                   EventProcessor faultEventPublisher,
                   EventProcessor ipcEventPublisher,
                   EventIpcBroadcaster localBroadcaster)
```

Remove `MessageBus` and `IpcMessageConverter` fields. Add `EventProcessor ipcEventPublisher`.

New IPC path:
```java
private void publishIpcEvent(Log originalLog, Event event, boolean synchronous)
        throws EventProcessorException {
    Log singleEventLog = new Log();
    Events events = new Events();
    events.addEvent(event);
    singleEventLog.setEvents(events);
    singleEventLog.setHeader(originalLog.getHeader());
    ipcEventPublisher.process(singleEventLog, synchronous);
}
```

Updated switch in `process()`:
```java
switch (classification) {
    case FAULT:
        publishFaultEvent(eventLog, event, synchronous);
        broadcastLocally(event, synchronous);
        break;
    case IPC:
        publishIpcEvent(eventLog, event, synchronous);
        broadcastLocally(event, synchronous);
        break;
    case DUAL:
        publishFaultEvent(eventLog, event, synchronous);
        publishIpcEvent(eventLog, event, synchronous);
        broadcastLocally(event, synchronous);
        break;
}
```

**Step 3: Update Spring XML wiring**

In `applicationContext-eventDaemon.xml`, update the `eventRouter` bean:

Before:
```xml
<bean id="eventRouter" class="org.opennms.netmgt.eventd.router.EventRouter">
    <constructor-arg ref="eventClassifier"/>
    <constructor-arg ref="faultEventPublisher"/>
    <constructor-arg ref="messageBus"/>
    <constructor-arg ref="eventIpcManagerImpl"/>
    <constructor-arg ref="ipcMessageConverter"/>
</bean>
```

After:
```xml
<bean id="eventRouter" class="org.opennms.netmgt.eventd.router.EventRouter">
    <constructor-arg ref="eventClassifier"/>
    <constructor-arg ref="faultEventPublisher"/>
    <constructor-arg ref="ipcEventPublisher"/>
    <constructor-arg ref="eventIpcManagerImpl"/>
</bean>
```

**Step 4: Remove MessageBus beans from applicationContext-eventDaemon.xml**

Remove these beans (they were the AMQ infrastructure):
- `messageBus` (JmsMessageBus bean)
- `messageBusFactory-setInstance` (MessageBusFactory static setter)
- The `onmsgi:service ref="messageBus"` registration
- `ipcMessageConverter` bean

**Step 5: Add Kafka IPC system property to core docker-compose**

In `docker-compose.yml`, add to core's `JAVA_OPTS`:
```
-Dorg.opennms.kafka.ipc.topic=opennms-ipc-events
```

**Step 6: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.features.events.daemon -am install
```

**Step 7: Commit**

```bash
git add features/events/daemon/src/main/java/org/opennms/netmgt/eventd/router/EventRouter.java
git add features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml
git add opennms-container/delta-v/docker-compose.yml
git commit -m "feat: replace MessageBus with IpcEventPublisher in core EventRouter

EventRouter now publishes IPC events to Kafka opennms-ipc-events topic
instead of AMQ MessageBus. Removes MessageBus, MessageBusFactory, and
IpcMessageConverter beans from applicationContext-eventDaemon.xml."
```

---

## Task 6: Create KafkaIpcEventConsumer for Core

Core needs to consume IPC events published by daemon containers (e.g., Discovery publishes `newSuspect` to `opennms-ipc-events`, core's Provisiond needs to receive it).

**Files:**
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/consumer/KafkaIpcEventConsumer.java`
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml`

**Step 1: Create KafkaIpcEventConsumer**

This is structurally identical to `KafkaFaultEventConsumer` but subscribes to `opennms-ipc-events`. We can either duplicate the class with a different topic, or refactor both into one class with configurable topic name.

**Recommended: Refactor KafkaFaultEventConsumer into a generic KafkaEventTopicConsumer and create two instances.**

Rename `KafkaFaultEventConsumer` → `KafkaEventTopicConsumer`:

```java
package org.opennms.netmgt.eventd.consumer;

// ... same imports as KafkaFaultEventConsumer ...

/**
 * Generic Kafka event consumer that polls a single topic and broadcasts
 * events to core's local EventIpcBroadcaster. Skips events originated
 * by this core instance (TSID node-id check) to prevent echo loops.
 */
public class KafkaEventTopicConsumer implements InitializingBean, DisposableBean {

    // ... same implementation as KafkaFaultEventConsumer ...
    // Constructor: bootstrapServers, topicName, groupId, coreNodeId, localBroadcaster
    // The only change: use topicName in thread name for debugging
    //   pollThread = new Thread(this::pollLoop, "kafka-consumer-" + topicName);
}
```

Then in `applicationContext-eventDaemon.xml`, create two instances:

```xml
<!-- Fault event consumer: polls opennms-fault-events from daemon containers -->
<bean id="kafkaFaultEventConsumer"
      class="org.opennms.netmgt.eventd.consumer.KafkaEventTopicConsumer">
    <constructor-arg value="${org.opennms.kafka.bootstrap.servers:localhost:9092}"/>
    <constructor-arg value="${org.opennms.kafka.fault.topic:opennms-fault-events}"/>
    <constructor-arg value="${org.opennms.kafka.fault.consumer.group:opennms-core-fault}"/>
    <constructor-arg value="${org.opennms.tsid.node-id:1}"/>
    <constructor-arg ref="eventIpcManagerImpl"/>
</bean>

<!-- IPC event consumer: polls opennms-ipc-events from daemon containers -->
<bean id="kafkaIpcEventConsumer"
      class="org.opennms.netmgt.eventd.consumer.KafkaEventTopicConsumer">
    <constructor-arg value="${org.opennms.kafka.bootstrap.servers:localhost:9092}"/>
    <constructor-arg value="${org.opennms.kafka.ipc.topic:opennms-ipc-events}"/>
    <constructor-arg value="${org.opennms.kafka.ipc.consumer.group:opennms-core-ipc}"/>
    <constructor-arg value="${org.opennms.tsid.node-id:1}"/>
    <constructor-arg ref="eventIpcManagerImpl"/>
</bean>
```

Note: Different consumer groups so each consumer gets ALL messages from its topic independently.

**Step 2: Update core docker-compose JAVA_OPTS**

Add the IPC consumer system properties:
```
-Dorg.opennms.kafka.ipc.topic=opennms-ipc-events
-Dorg.opennms.kafka.ipc.consumer.group=opennms-core-ipc
```

**Step 3: Delete old KafkaFaultEventConsumer.java**

Since we renamed it to `KafkaEventTopicConsumer`, delete the old file.

**Step 4: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.features.events.daemon -am install
```

**Step 5: Commit**

```bash
git add features/events/daemon/src/main/java/org/opennms/netmgt/eventd/consumer/KafkaEventTopicConsumer.java
git rm features/events/daemon/src/main/java/org/opennms/netmgt/eventd/consumer/KafkaFaultEventConsumer.java
git add features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml
git add opennms-container/delta-v/docker-compose.yml
git commit -m "feat: add KafkaIpcEventConsumer and refactor to generic KafkaEventTopicConsumer

Rename KafkaFaultEventConsumer to KafkaEventTopicConsumer (generic).
Create two instances in applicationContext-eventDaemon.xml: one for
opennms-fault-events, one for opennms-ipc-events. Both use TSID node-id
echo prevention."
```

---

## Task 7: Remove AMQ/MessageBus Infrastructure from Daemon Containers

With Kafka handling both IPC and fault events, the AMQ infrastructure is no longer needed.

**Files to modify/remove:**
- Remove: `core/messagebus-jms/src/main/resources/OSGI-INF/blueprint/blueprint-messagebus-jms.xml`
- Modify: `container/features/src/main/resources/features.xml` — remove `opennms-core-messagebus-jms` from `opennms-event-forwarder-kafka` feature
- Remove: `opennms-container/delta-v/*/etc/org.opennms.core.messagebus.jms.cfg` (all daemon overlays)
- Remove: `opennms-container/delta-v/shared-daemon-overlay/etc/org.opennms.core.messagebus.jms.cfg`
- Modify: `opennms-container/delta-v/docker-compose.yml` — remove AMQ-related volume mounts and env vars

**Step 1: Remove JmsMessageBus blueprint**

```bash
rm core/messagebus-jms/src/main/resources/OSGI-INF/blueprint/blueprint-messagebus-jms.xml
```

**Step 2: Remove messagebus-jms feature from features.xml**

In `container/features/src/main/resources/features.xml`, find the `opennms-event-forwarder-kafka` feature and remove the `opennms-core-messagebus-jms` dependency:

Before:
```xml
<feature>opennms-core-messagebus-jms</feature>   <!-- REMOVE THIS LINE -->
```

**Step 3: Remove AMQ config files from all daemon overlays**

```bash
cd opennms-container/delta-v
find . -name "org.opennms.core.messagebus.jms.cfg" -delete
rm -rf shared-daemon-overlay/etc/org.opennms.core.messagebus.jms.cfg
```

**Step 4: Remove AMQ port exposure from docker-compose.yml (if daemon containers don't need 61616)**

If no daemon container connects to core's AMQ broker, the port exposure on core can optionally stay (for Minion Camel transport if used later) but daemon containers don't need `OPENNMS_BROKER_URL` env vars anymore.

Check docker-compose.yml for any `OPENNMS_BROKER_URL` references or AMQ-related environment variables in daemon container sections and remove them.

**Step 5: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.event-forwarder-kafka -am install
```

**Step 6: Commit**

```bash
git rm core/messagebus-jms/src/main/resources/OSGI-INF/blueprint/blueprint-messagebus-jms.xml
git add container/features/src/main/resources/features.xml
git rm opennms-container/delta-v/*/etc/org.opennms.core.messagebus.jms.cfg
git rm opennms-container/delta-v/shared-daemon-overlay/etc/org.opennms.core.messagebus.jms.cfg
git add opennms-container/delta-v/docker-compose.yml
git commit -m "refactor: remove AMQ/MessageBus infrastructure from daemon containers

JmsMessageBus blueprint, AMQ config files, and MessageBus feature dependency
all removed. Kafka is now the single inter-service transport."
```

---

## Task 8: Update Tests

Update existing tests to reflect the new Kafka IPC path and removal of MessageBus.

**Files:**
- Modify: `core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarderTest.java`
- Modify: `features/events/daemon/src/test/java/org/opennms/netmgt/eventd/router/EventRouterTest.java`
- Modify: `core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaEventSubscriptionServiceTest.java`

**Step 1: Update KafkaEventForwarderTest**

The test currently verifies:
- FAULT → Kafka only
- IPC → MessageBus only
- DUAL → both

Change IPC test to verify:
- IPC → Kafka IPC topic (verify `kafkaProducer.send()` called with IPC topic name)
- Remove MessageBus mock verification
- Add verification that IPC events go to the correct topic name

**Step 2: Update EventRouterTest**

Change IPC test to verify:
- IPC → `ipcEventPublisher.process()` instead of `messageBus.publish()`
- Remove MessageBus and IpcMessageConverter mocks
- Add `ipcEventPublisher` mock

**Step 3: Update KafkaEventSubscriptionServiceTest**

If the test hardcodes a single topic name, update to test with comma-separated topics.

**Step 4: Compile and run tests**

```bash
./compile.pl --projects :org.opennms.core.event-forwarder-kafka -am verify
./compile.pl --projects :org.opennms.features.events.daemon -am verify
```

Expected: All tests pass.

**Step 5: Commit**

```bash
git add core/event-forwarder-kafka/src/test/
git add features/events/daemon/src/test/
git commit -m "test: update event routing tests for Kafka IPC topic

Remove MessageBus mocks, verify IPC events published to Kafka IPC topic.
Update EventRouter tests for IpcEventPublisher. Test multi-topic subscription."
```

---

## Task 9: Integration Test — Discovery → Kafka IPC → Provisiond (Core)

Verify the full IPC flow works via Kafka instead of AMQ.

**Prerequisites:**
- Rebuild all modified modules
- Rebuild container/features, sentinel assembly, daemon assembly, Docker images
- Delete old Docker volumes

**Step 1: Start test stack**

```bash
cd opennms-container/delta-v
docker compose up -d postgres kafka core discovery
```

**Step 2: Verify Discovery publishes newSuspect to Kafka IPC topic**

```bash
# Check the opennms-ipc-events topic has messages
docker compose exec kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic opennms-ipc-events \
  --from-beginning --timeout-ms 30000
```

Expected: XML event with `uei.opennms.org/internal/discovery/newSuspect`.

**Step 3: Verify core received newSuspect and Provisiond processed it**

```bash
docker compose logs core 2>&1 | grep -i "newSuspect\|NewSuspectScan\|Broadcasting.*ipc"
```

Expected: Core's `KafkaIpcEventConsumer` received the event, broadcast locally, Provisiond created a node.

**Step 4: Verify node in PostgreSQL**

```bash
docker compose exec postgres psql -U opennms -c \
  "SELECT nodeid, nodelabel, foreignsource FROM node;"
```

**Step 5: Verify no AMQ connections from daemon containers**

```bash
# Core's AMQ broker should show no daemon container connections
docker compose logs core 2>&1 | grep -i "ActiveMQ.*connection\|JmsMessageBus"
```

Expected: No JmsMessageBus logs from daemon containers. Only core's internal AMQ usage (if any).

**Step 6: Document results**

```bash
git add opennms-container/delta-v/docker-compose.yml  # if any tweaks needed
git commit -m "test: verify Discovery → Kafka IPC → Provisiond end-to-end"
```

---

## Build and Deploy Checklist

```bash
# 1. Rebuild modified modules
./compile.pl -DskipTests --projects \
  :org.opennms.core.event-forwarder-kafka,\
  :org.opennms.features.events.daemon \
  -am install

# 2. Rebuild container features
./compile.pl -DskipTests -pl container/features install

# 3. Rebuild sentinel assembly
./compile.pl -DskipTests -pl features/container/sentinel install

# 4. Rebuild daemon assembly
cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install && cd ../..

# 5. Rebuild Docker images
cd opennms-container/sentinel && docker build -t opennms/daemon:delta-v .
# Also rebuild core if applicationContext-eventDaemon.xml changed:
cd opennms-full-assembly && ../compile.pl -DskipTests -Passemblies install

# 6. Delete old Docker volumes
docker compose -f opennms-container/delta-v/docker-compose.yml down -v

# 7. Start fresh
docker compose -f opennms-container/delta-v/docker-compose.yml up -d
```
