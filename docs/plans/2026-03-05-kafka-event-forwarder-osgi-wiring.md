# KafkaEventForwarder OSGi Wiring Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Register KafkaEventForwarder + KafkaEventSubscriptionService as OSGi services via Karaf blueprint so they can replace EventIpcManagerDefaultImpl in Strike Fighter containers.

**Architecture:** Create a `KafkaEventIpcManagerAdapter` that implements the composite `EventIpcManager` interface by delegating to KafkaEventForwarder (for send) and KafkaEventSubscriptionService (for subscribe). A Karaf blueprint wires all beans and registers the adapter as an OSGi service. A Karaf feature definition makes it installable.

**Tech Stack:** Karaf Blueprint XML, OSGi services, Kafka clients, existing EventExpander/TsidAssigner/EventClassifier from events-daemon module.

---

## Context

Phase 1 & 2 created the library classes:
- `KafkaEventForwarder` implements `EventForwarder` — enriches events and routes to Kafka/MessageBus
- `KafkaEventSubscriptionService` implements `EventSubscriptionService` — Kafka consumer with UEI dispatch
- `KafkaAnnotationEventListenerAdapter` — wires `@EventHandler` annotations to any EventSubscriptionService

These are raw Java classes with no OSGi wiring. All daemons in the monolith get EventForwarder via `<onmsgi:reference>` in `applicationContext-daemon.xml`, which resolves to `EventIpcManagerDefaultImpl` registered by `applicationContext-eventDaemon.xml`. To replace it, we need to register our Kafka-backed implementation as an OSGi service.

**Key interface hierarchy:**
```
EventIpcManager extends EventSubscriptionService, EventProxy, EventForwarder
  - EventForwarder: sendNow(Event), sendNow(Log), sendNowSync(Event), sendNowSync(Log)
  - EventProxy: send(Event), send(Log)
  - EventSubscriptionService: addEventListener (x3), removeEventListener (x3), hasEventListener
EventIpcBroadcaster: broadcastNow(Event, boolean)  (standalone, not part of EventIpcManager)
```

`EventIpcManager` has NO methods of its own — it's a pure composite interface.

**Existing blueprint pattern** (from `features/events/sink/dispatcher/`):
```xml
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0" ...>
    <cm:property-placeholder persistent-id="org.opennms.features.events.sink" .../>
    <reference id="messageDispatcherFactory" interface="..."/>
    <bean id="eventDispatcher" class="...EventDispatcherImpl">
        <argument ref="..."/>
    </bean>
    <service ref="eventDispatcher" interface="org.opennms.netmgt.events.api.EventForwarder"/>
</blueprint>
```

**Dependencies for bean construction:**
- `EventExpander` (final class): needs `EventConfDao`, `EventUtil`, `MetricRegistry`
- `TsidAssigner`: needs `TsidFactory`
- `EventClassifier`: no-arg constructor
- `IpcMessageConverter`: no-arg constructor
- `KafkaProducer<Long, byte[]>`: needs bootstrap.servers config
- `KafkaConsumer<Long, byte[]>`: needs bootstrap.servers, group.id, deserializers config
- `MessageBus`: OSGi reference (JmsMessageBus or LocalMessageBus)
- `EventDeserializer`: OSGi reference (from events-kafka-consumer bundle)

---

### Task 1: Create KafkaEventIpcManagerAdapter

**Files:**
- Create: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventIpcManagerAdapter.java`
- Test: `core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaEventIpcManagerAdapterTest.java`

**What:** A thin adapter implementing `EventIpcManager` (and `EventIpcBroadcaster`) that delegates to the Kafka-backed implementations. This lets existing daemon code that references `EventIpcManager` work unchanged.

**Step 1: Write the failing test**

```java
package org.opennms.core.event.forwarder.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

public class KafkaEventIpcManagerAdapterTest {

    private EventForwarder mockForwarder;
    private EventSubscriptionService mockSubscriptionService;
    private KafkaEventIpcManagerAdapter adapter;

    @Before
    public void setUp() {
        mockForwarder = mock(EventForwarder.class);
        mockSubscriptionService = mock(EventSubscriptionService.class);
        adapter = new KafkaEventIpcManagerAdapter(mockForwarder, mockSubscriptionService);
    }

    @Test
    public void testSendNowDelegatesToForwarder() {
        Event event = new Event();
        adapter.sendNow(event);
        verify(mockForwarder).sendNow(event);
    }

    @Test
    public void testSendNowLogDelegatesToForwarder() {
        Log log = new Log();
        adapter.sendNow(log);
        verify(mockForwarder).sendNow(log);
    }

    @Test
    public void testSendNowSyncDelegatesToForwarder() {
        Event event = new Event();
        adapter.sendNowSync(event);
        verify(mockForwarder).sendNowSync(event);
    }

    @Test
    public void testSendSyncLogDelegatesToForwarder() {
        Log log = new Log();
        adapter.sendNowSync(log);
        verify(mockForwarder).sendNowSync(log);
    }

    @Test
    public void testEventProxySendDelegatesToForwarder() throws Exception {
        Event event = new Event();
        adapter.send(event);
        verify(mockForwarder).sendNow(event);
    }

    @Test
    public void testEventProxySendLogDelegatesToForwarder() throws Exception {
        Log log = new Log();
        adapter.send(log);
        verify(mockForwarder).sendNow(log);
    }

    @Test
    public void testAddEventListenerDelegatesToSubscriptionService() {
        EventListener listener = mock(EventListener.class);
        adapter.addEventListener(listener);
        verify(mockSubscriptionService).addEventListener(listener);
    }

    @Test
    public void testAddEventListenerWithUeiDelegates() {
        EventListener listener = mock(EventListener.class);
        adapter.addEventListener(listener, "uei.opennms.org/test");
        verify(mockSubscriptionService).addEventListener(listener, "uei.opennms.org/test");
    }

    @Test
    public void testAddEventListenerWithUeiCollectionDelegates() {
        EventListener listener = mock(EventListener.class);
        Collection<String> ueis = Arrays.asList("uei1", "uei2");
        adapter.addEventListener(listener, ueis);
        verify(mockSubscriptionService).addEventListener(listener, ueis);
    }

    @Test
    public void testRemoveEventListenerDelegates() {
        EventListener listener = mock(EventListener.class);
        adapter.removeEventListener(listener);
        verify(mockSubscriptionService).removeEventListener(listener);
    }

    @Test
    public void testHasEventListenerDelegates() {
        when(mockSubscriptionService.hasEventListener("uei.opennms.org/test")).thenReturn(true);
        assertThat(adapter.hasEventListener("uei.opennms.org/test")).isTrue();
        verify(mockSubscriptionService).hasEventListener("uei.opennms.org/test");
    }

    @Test
    public void testBroadcastNowIsNoOp() {
        // broadcastNow is an internal Eventd concern — no-op in Kafka mode
        Event event = new Event();
        adapter.broadcastNow(event, false);
        // Should not throw and should not delegate anywhere
        verifyNoMoreInteractions(mockForwarder, mockSubscriptionService);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./compile.pl --projects :org.opennms.core.event-forwarder-kafka verify`
Expected: FAIL — `KafkaEventIpcManagerAdapter` class does not exist

**Step 3: Write minimal implementation**

```java
package org.opennms.core.event.forwarder.kafka;

import java.util.Collection;
import java.util.Objects;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that implements the composite {@link EventIpcManager} interface
 * (and {@link EventIpcBroadcaster}) by delegating to separate Kafka-backed
 * implementations.
 *
 * <p>{@link EventForwarder} methods delegate to the Kafka event forwarder.
 * {@link org.opennms.netmgt.events.api.EventProxy} methods also delegate
 * to the forwarder (send = sendNow).
 * {@link EventSubscriptionService} methods delegate to the Kafka consumer.</p>
 *
 * <p>{@link EventIpcBroadcaster#broadcastNow} is a no-op — in Kafka mode,
 * broadcasting is handled by the Kafka consumer poll loop in
 * {@link KafkaEventSubscriptionService}.</p>
 */
public class KafkaEventIpcManagerAdapter implements EventIpcManager, EventIpcBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventIpcManagerAdapter.class);

    private final EventForwarder eventForwarder;
    private final EventSubscriptionService subscriptionService;

    public KafkaEventIpcManagerAdapter(EventForwarder eventForwarder,
                                        EventSubscriptionService subscriptionService) {
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
        this.subscriptionService = Objects.requireNonNull(subscriptionService);
    }

    // -------- EventForwarder --------

    @Override
    public void sendNow(Event event) {
        eventForwarder.sendNow(event);
    }

    @Override
    public void sendNow(Log eventLog) {
        eventForwarder.sendNow(eventLog);
    }

    @Override
    public void sendNowSync(Event event) {
        eventForwarder.sendNowSync(event);
    }

    @Override
    public void sendNowSync(Log eventLog) {
        eventForwarder.sendNowSync(eventLog);
    }

    // -------- EventProxy --------

    @Override
    public void send(Event event) throws EventProxyException {
        eventForwarder.sendNow(event);
    }

    @Override
    public void send(Log eventLog) throws EventProxyException {
        eventForwarder.sendNow(eventLog);
    }

    // -------- EventSubscriptionService --------

    @Override
    public void addEventListener(EventListener listener) {
        subscriptionService.addEventListener(listener);
    }

    @Override
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        subscriptionService.addEventListener(listener, ueis);
    }

    @Override
    public void addEventListener(EventListener listener, String uei) {
        subscriptionService.addEventListener(listener, uei);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        subscriptionService.removeEventListener(listener);
    }

    @Override
    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        subscriptionService.removeEventListener(listener, ueis);
    }

    @Override
    public void removeEventListener(EventListener listener, String uei) {
        subscriptionService.removeEventListener(listener, uei);
    }

    @Override
    public boolean hasEventListener(String uei) {
        return subscriptionService.hasEventListener(uei);
    }

    // -------- EventIpcBroadcaster --------

    @Override
    public void broadcastNow(Event event, boolean synchronous) {
        LOG.debug("broadcastNow is a no-op in Kafka mode (event UEI: {})", event.getUei());
    }
}
```

**Step 4: Run tests to verify they pass**

Run: `./compile.pl --projects :org.opennms.core.event-forwarder-kafka verify`
Expected: All tests PASS (existing 21 + 13 new = 34 total)

**Step 5: Commit**

```bash
git add core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventIpcManagerAdapter.java \
        core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaEventIpcManagerAdapterTest.java
git commit -m "feat: add KafkaEventIpcManagerAdapter for EventIpcManager interface compatibility"
```

---

### Task 2: Create Karaf Blueprint for OSGi Service Registration

**Files:**
- Create: `core/event-forwarder-kafka/src/main/resources/OSGI-INF/blueprint/blueprint-event-forwarder-kafka.xml`

**What:** Blueprint XML that constructs all beans (EventExpander, TsidAssigner, EventClassifier, KafkaProducer, KafkaConsumer, KafkaEventForwarder, KafkaEventSubscriptionService, KafkaEventIpcManagerAdapter) and registers the adapter as an OSGi service under all 5 interfaces.

**Step 1: Create the blueprint**

The blueprint needs to:
1. Read Kafka config from a `.cfg` file via `<cm:property-placeholder>`
2. Get OSGi service references for `EventConfDao`, `MessageBus`, `EventDeserializer`, `EventUtil`, `MetricRegistry`
3. Construct all bean instances
4. Register `KafkaEventIpcManagerAdapter` as OSGi services under `EventForwarder`, `EventSubscriptionService`, `EventIpcManager`, `EventProxy`, `EventIpcBroadcaster`
5. Call `start()` on `KafkaEventSubscriptionService` init and `stop()` on destroy

```xml
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:cm="http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.3.0"
           xsi:schemaLocation="
        http://www.osgi.org/xmlns/blueprint/v1.0.0
        https://osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd
        http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.3.0
        http://aries.apache.org/schemas/blueprint-cm/blueprint-cm-1.3.0.xsd">

    <!-- Configuration from etc/org.opennms.core.event.forwarder.kafka.cfg -->
    <cm:property-placeholder id="kafkaEventForwarderProperties"
                             persistent-id="org.opennms.core.event.forwarder.kafka"
                             update-strategy="none">
        <cm:default-properties>
            <cm:property name="bootstrap.servers" value="localhost:9092"/>
            <cm:property name="topic.name" value="opennms-fault-events"/>
            <cm:property name="consumer.group.id" value="opennms-core"/>
            <cm:property name="poll.timeout.ms" value="100"/>
        </cm:default-properties>
    </cm:property-placeholder>

    <!-- OSGi service references -->
    <reference id="eventConfDao" interface="org.opennms.netmgt.config.api.EventConfDao"/>
    <reference id="messageBus" interface="org.opennms.core.messagebus.MessageBus" availability="optional"/>
    <reference id="eventDeserializer" interface="org.opennms.features.events.kafka.consumer.EventDeserializer"/>
    <reference id="eventUtil" interface="org.opennms.netmgt.eventd.EventUtil"/>

    <!-- MetricRegistry for EventExpander -->
    <bean id="metricRegistry" class="com.codahale.metrics.MetricRegistry"/>

    <!-- EventExpander (enriches events from event.conf) -->
    <bean id="eventExpander" class="org.opennms.netmgt.eventd.EventExpander"
          init-method="afterPropertiesSet">
        <argument ref="metricRegistry"/>
        <property name="eventConfDao" ref="eventConfDao"/>
        <property name="eventUtil" ref="eventUtil"/>
    </bean>

    <!-- TsidAssigner (assigns unique TSID) -->
    <bean id="tsidFactory" class="org.opennms.core.tsid.TsidFactory">
        <argument value="0"/>
    </bean>
    <bean id="tsidAssigner" class="org.opennms.netmgt.eventd.processor.TsidAssigner">
        <argument ref="tsidFactory"/>
    </bean>

    <!-- EventClassifier + IpcMessageConverter (stateless) -->
    <bean id="eventClassifier" class="org.opennms.netmgt.eventd.router.EventClassifier"/>
    <bean id="ipcMessageConverter" class="org.opennms.netmgt.eventd.router.IpcMessageConverter"/>

    <!-- Kafka Producer -->
    <bean id="kafkaProducerProps" class="java.util.Properties">
        <property name="bootstrapServers" value="${bootstrap.servers}"/>
        <property name="keySerializer" value="org.apache.kafka.common.serialization.LongSerializer"/>
        <property name="valueSerializer" value="org.apache.kafka.common.serialization.ByteArraySerializer"/>
    </bean>

    <!-- NOTE: KafkaProducer and KafkaConsumer beans will need a factory bean
         or factory-method to construct properly from Properties.
         See implementation notes below. -->

    <!-- KafkaEventForwarder -->
    <bean id="kafkaEventForwarder" class="org.opennms.core.event.forwarder.kafka.KafkaEventForwarder">
        <argument ref="eventExpander"/>
        <argument ref="tsidAssigner"/>
        <argument ref="eventClassifier"/>
        <argument ref="ipcMessageConverter"/>
        <argument ref="messageBus"/>
        <argument ref="kafkaProducer"/>
        <argument value="${topic.name}"/>
    </bean>

    <!-- KafkaEventSubscriptionService -->
    <bean id="kafkaEventSubscriptionService"
          class="org.opennms.core.event.forwarder.kafka.KafkaEventSubscriptionService"
          init-method="start" destroy-method="stop">
        <argument ref="kafkaConsumer"/>
        <argument value="${topic.name}"/>
        <argument ref="eventDeserializer"/>
        <argument>
            <bean class="java.time.Duration" factory-method="ofMillis">
                <argument value="${poll.timeout.ms}"/>
            </bean>
        </argument>
    </bean>

    <!-- Adapter that implements EventIpcManager by delegating -->
    <bean id="kafkaEventIpcManagerAdapter"
          class="org.opennms.core.event.forwarder.kafka.KafkaEventIpcManagerAdapter">
        <argument ref="kafkaEventForwarder"/>
        <argument ref="kafkaEventSubscriptionService"/>
    </bean>

    <!-- Register as OSGi services under all 5 interfaces -->
    <service ref="kafkaEventIpcManagerAdapter">
        <interfaces>
            <value>org.opennms.netmgt.events.api.EventForwarder</value>
            <value>org.opennms.netmgt.events.api.EventIpcBroadcaster</value>
            <value>org.opennms.netmgt.events.api.EventIpcManager</value>
            <value>org.opennms.netmgt.events.api.EventProxy</value>
            <value>org.opennms.netmgt.events.api.EventSubscriptionService</value>
        </interfaces>
    </service>
</blueprint>
```

**Implementation notes for the subagent:**
- KafkaProducer and KafkaConsumer cannot be directly constructed in blueprint XML because their constructors take `Map<String, Object>` or `Properties`. You will need to create small factory classes:
  - `KafkaProducerFactory` — static factory method that takes bootstrap.servers and returns `KafkaProducer<Long, byte[]>`
  - `KafkaConsumerFactory` — static factory method that takes bootstrap.servers, group.id and returns `KafkaConsumer<Long, byte[]>`
- These factories should live in the `org.opennms.core.event.forwarder.kafka` package
- The `EventUtil` interface is at `org.opennms.netmgt.eventd.EventUtil` — check if it's registered as an OSGi service or if it needs to be instantiated locally
- The TsidFactory constructor argument `0` is the node-id — should be configurable via the `.cfg` file or system property `org.opennms.tsid.node-id`
- The `availability="optional"` on messageBus allows startup without ActiveMQ/JMS; IPC events will be dropped with a warning log

**Step 2: Verify blueprint is valid XML**

Check with an XML validator or by installing the feature in Karaf shell.

**Step 3: Commit**

```bash
git add core/event-forwarder-kafka/src/main/resources/OSGI-INF/blueprint/
git commit -m "feat: add Karaf blueprint for KafkaEventForwarder OSGi service registration"
```

---

### Task 3: Create Kafka Factory Beans + Configuration File

**Files:**
- Create: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaProducerFactory.java`
- Create: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaConsumerFactory.java`
- Test: `core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaProducerFactoryTest.java`
- Test: `core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaConsumerFactoryTest.java`

**What:** Factory classes for creating KafkaProducer and KafkaConsumer instances from simple string config parameters (blueprint-friendly).

**Step 1: Write tests**

```java
package org.opennms.core.event.forwarder.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import java.util.Properties;

public class KafkaProducerFactoryTest {
    @Test
    public void testBuildProducerProperties() {
        Properties props = KafkaProducerFactory.buildProperties("localhost:9092");
        assertThat(props.getProperty("bootstrap.servers")).isEqualTo("localhost:9092");
        assertThat(props.getProperty("key.serializer"))
                .isEqualTo("org.apache.kafka.common.serialization.LongSerializer");
        assertThat(props.getProperty("value.serializer"))
                .isEqualTo("org.apache.kafka.common.serialization.ByteArraySerializer");
    }
}

public class KafkaConsumerFactoryTest {
    @Test
    public void testBuildConsumerProperties() {
        Properties props = KafkaConsumerFactory.buildProperties("kafka:9092", "opennms-core");
        assertThat(props.getProperty("bootstrap.servers")).isEqualTo("kafka:9092");
        assertThat(props.getProperty("group.id")).isEqualTo("opennms-core");
        assertThat(props.getProperty("key.deserializer"))
                .isEqualTo("org.apache.kafka.common.serialization.LongDeserializer");
        assertThat(props.getProperty("value.deserializer"))
                .isEqualTo("org.apache.kafka.common.serialization.ByteArrayDeserializer");
        assertThat(props.getProperty("auto.offset.reset")).isEqualTo("earliest");
    }
}
```

**Step 2: Implement factories**

```java
package org.opennms.core.event.forwarder.kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;

public class KafkaProducerFactory {
    public static Properties buildProperties(String bootstrapServers) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put("acks", "all");
        return props;
    }

    public static KafkaProducer<Long, byte[]> create(String bootstrapServers) {
        return new KafkaProducer<>(buildProperties(bootstrapServers));
    }
}
```

```java
package org.opennms.core.event.forwarder.kafka;

import java.util.Properties;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class KafkaConsumerFactory {
    public static Properties buildProperties(String bootstrapServers, String groupId) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "true");
        return props;
    }

    public static KafkaConsumer<Long, byte[]> create(String bootstrapServers, String groupId) {
        return new KafkaConsumer<>(buildProperties(bootstrapServers, groupId));
    }
}
```

**Step 3: Update the blueprint** to use factory-method beans:

Replace the placeholder KafkaProducer/KafkaConsumer beans with:
```xml
<bean id="kafkaProducer" class="org.opennms.core.event.forwarder.kafka.KafkaProducerFactory"
      factory-method="create" destroy-method="close">
    <argument value="${bootstrap.servers}"/>
</bean>

<bean id="kafkaConsumer" class="org.opennms.core.event.forwarder.kafka.KafkaConsumerFactory"
      factory-method="create">
    <argument value="${bootstrap.servers}"/>
    <argument value="${consumer.group.id}"/>
</bean>
```

**Step 4: Run tests**

Run: `./compile.pl --projects :org.opennms.core.event-forwarder-kafka verify`
Expected: All tests PASS

**Step 5: Commit**

```bash
git add core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaProducerFactory.java \
        core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaConsumerFactory.java \
        core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaProducerFactoryTest.java \
        core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaConsumerFactoryTest.java \
        core/event-forwarder-kafka/src/main/resources/OSGI-INF/blueprint/
git commit -m "feat: add Kafka producer/consumer factories and finalize blueprint wiring"
```

---

### Task 4: Create Karaf Feature Definition

**Files:**
- Modify: `container/features/src/main/resources/features.xml` (add feature after `opennms-events-store`)

**What:** Define a Karaf feature `opennms-event-forwarder-kafka` that bundles our module with all its dependencies.

**Step 1: Add feature definition**

Insert after the `opennms-events-store` feature (around line 2161):

```xml
<feature name="opennms-event-forwarder-kafka" version="${project.version}"
         description="OpenNMS :: Event Forwarder :: Kafka">
    <feature>opennms-events-api</feature>
    <feature>opennms-events-kafka-consumer</feature>
    <feature>opennms-core-messagebus-api</feature>
    <feature>opennms-core-tsid</feature>
    <feature>opennms-kafka</feature>
    <feature>dropwizard-metrics</feature>
    <bundle>mvn:org.opennms.features.events/org.opennms.features.events.daemon/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.event-forwarder-kafka/${project.version}</bundle>
</feature>
```

**Notes for the subagent:**
- The events-daemon bundle is needed because KafkaEventForwarder depends on EventExpander, TsidAssigner, EventClassifier, IpcMessageConverter from that module
- Check if `opennms-kafka` feature already exists (it should provide the Kafka client bundle)
- `dropwizard-metrics` provides `com.codahale.metrics.MetricRegistry` for EventExpander
- Verify the feature name doesn't conflict with existing features

**Step 2: Commit**

```bash
git add container/features/src/main/resources/features.xml
git commit -m "feat: add opennms-event-forwarder-kafka Karaf feature definition"
```

---

### Task 5: Add Feature to Strike Fighter Container + Test

**Files:**
- Modify: `opennms-container/strike-fighter/core-overlay/etc/featuresBoot.d/strike-fighter.boot`
- Create: `opennms-container/strike-fighter/core-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`

**What:** Install the new Karaf feature in the Strike Fighter core container and provide its Kafka configuration.

**Step 1: Check current featuresBoot**

Read: `opennms-container/strike-fighter/core-overlay/etc/featuresBoot.d/strike-fighter.boot`

**Step 2: Add feature to boot**

Append to the featuresBoot file:
```
opennms-event-forwarder-kafka
```

**Step 3: Create Kafka config**

```properties
# Kafka Event Forwarder configuration
bootstrap.servers=kafka:9092
topic.name=opennms-fault-events
consumer.group.id=opennms-core
poll.timeout.ms=100
```

**Step 4: Add the config file volume mount to docker-compose.yml**

In the `core` service volumes section, add:
```yaml
- ./core-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg:/opt/opennms-etc-overlay/org.opennms.core.event.forwarder.kafka.cfg:ro
```

**Step 5: Commit**

```bash
git add opennms-container/strike-fighter/core-overlay/etc/featuresBoot.d/strike-fighter.boot \
        opennms-container/strike-fighter/core-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg \
        opennms-container/strike-fighter/docker-compose.yml
git commit -m "feat: wire KafkaEventForwarder into Strike Fighter core container"
```

---

## Build & Verification

After all tasks are complete:

```bash
# 1. Build the module
./compile.pl -DskipTests --projects :org.opennms.core.event-forwarder-kafka install

# 2. Run unit tests
./compile.pl --projects :org.opennms.core.event-forwarder-kafka verify

# 3. Full assembly (for container build)
./assemble.pl -Dopennms.home=/opt/opennms -DskipTests -p dir

# 4. Build container images
cd opennms-container/core && make image

# 5. Test in Strike Fighter
cd ../strike-fighter && docker compose up -d
docker compose logs core --tail=200 | grep -i "kafka.*event\|event.*forwarder\|blueprint"
```
