# KafkaEventForwarder Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the centralized Eventd daemon with a KafkaEventForwarder library that each daemon carries, enabling per-daemon-per-container deployment.

**Architecture:** Each daemon embeds a `KafkaEventForwarder` (implementing `EventForwarder`) that enriches events locally via `EventExpander`, assigns TSIDs, classifies events (FAULT/IPC/DUAL), and publishes directly to Kafka or ActiveMQ. A `KafkaEventSubscriptionService` (implementing `EventSubscriptionService`) replaces local `EventIpcManager` listener dispatch by consuming from Kafka with UEI-based filtering.

**Tech Stack:** Java 17, Kafka 3.6.2 (ServiceMix bundle), ActiveMQ 5.16.8 (JMS MessageBus), JUnit 4 + Mockito + AssertJ, Maven OSGi bundle packaging.

**Design Doc:** `docs/plans/2026-03-05-eliminate-eventd-kafka-event-forwarder-design.md`

---

## Phase 1: KafkaEventForwarder Library

### Task 1: Create `core/event-forwarder-kafka/` Maven Module

**Files:**
- Create: `core/event-forwarder-kafka/pom.xml`
- Modify: `core/pom.xml` (add module declaration)

**Step 1: Create the module POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.opennms</groupId>
        <artifactId>org.opennms.core</artifactId>
        <version>36.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>org.opennms.core.event-forwarder-kafka</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Core :: Event Forwarder Kafka</name>
    <description>
        EventForwarder implementation that enriches events locally and publishes
        to Kafka (FAULT events) or JMS MessageBus (IPC events).
    </description>

    <dependencies>
        <!-- Event API (EventForwarder, EventProcessor interfaces) -->
        <dependency>
            <groupId>org.opennms.features.events</groupId>
            <artifactId>org.opennms.features.events.api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- MessageBus API (IPC event routing) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>org.opennms.core.messagebus.api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Event daemon (EventExpander, TsidAssigner, EventClassifier, IpcMessageConverter) -->
        <dependency>
            <groupId>org.opennms.features.events</groupId>
            <artifactId>org.opennms.features.events.daemon</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Event model (Event, Log JAXB classes) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-model</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Kafka client -->
        <dependency>
            <groupId>org.apache.servicemix.bundles</groupId>
            <artifactId>org.apache.servicemix.bundles.kafka-clients</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

**Step 2: Add module to `core/pom.xml`**

Find the `<modules>` section in `core/pom.xml` and add:
```xml
<module>event-forwarder-kafka</module>
```

Add it alphabetically near existing `messagebus-*` modules.

**Step 3: Create source directories**

```bash
mkdir -p core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka
mkdir -p core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka
```

**Step 4: Verify module compiles**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.event-forwarder-kafka -am install
```
Expected: BUILD SUCCESS (empty module compiles)

**Step 5: Commit**

```bash
git add core/event-forwarder-kafka/pom.xml core/pom.xml
git commit -m "feat: create core/event-forwarder-kafka Maven module skeleton"
```

---

### Task 2: Implement `KafkaEventForwarder`

**Files:**
- Create: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarder.java`
- Test: `core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarderTest.java`

**Step 1: Write the failing test**

```java
package org.opennms.core.event.forwarder.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.function.Function;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.eventd.processor.TsidAssigner;
import org.opennms.netmgt.eventd.router.EventClassifier;
import org.opennms.netmgt.eventd.router.IpcMessageConverter;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;

public class KafkaEventForwarderTest {

    private EventExpander mockExpander;
    private TsidAssigner mockTsidAssigner;
    @SuppressWarnings("unchecked")
    private KafkaProducer<Long, byte[]> mockProducer = mock(KafkaProducer.class);
    private MessageBus mockMessageBus;
    private Function<Event, byte[]> serializer;
    private KafkaEventForwarder forwarder;

    @Before
    public void setUp() {
        mockExpander = mock(EventExpander.class);
        mockTsidAssigner = mock(TsidAssigner.class);
        mockMessageBus = mock(MessageBus.class);
        serializer = event -> new byte[]{1, 2, 3};

        forwarder = new KafkaEventForwarder(
                mockExpander,
                mockTsidAssigner,
                mockProducer,
                mockMessageBus,
                new EventClassifier(),
                new IpcMessageConverter(),
                "opennms-fault-events",
                serializer
        );
    }

    @Test
    public void shouldPublishFaultEventToKafka() {
        Event event = createFaultEvent("uei.opennms.org/nodes/nodeDown", 42L);

        forwarder.sendNow(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer, times(1)).send(captor.capture());
        assertThat(captor.getValue().topic()).isEqualTo("opennms-fault-events");
        assertThat(captor.getValue().key()).isEqualTo(42L);
        verify(mockMessageBus, never()).publish(any());
    }

    @Test
    public void shouldPublishIpcEventToMessageBus() {
        Event event = createIpcEvent("uei.opennms.org/internal/reloadDaemonConfig");

        forwarder.sendNow(event);

        verify(mockMessageBus, times(1)).publish(any(IpcMessage.class));
        verify(mockProducer, never()).send(any());
    }

    @Test
    public void shouldPublishDualEventToBothKafkaAndMessageBus() {
        Event event = createDualEvent("uei.opennms.org/internal/alarmWithData", 10L);

        forwarder.sendNow(event);

        verify(mockProducer, times(1)).send(any());
        verify(mockMessageBus, times(1)).publish(any(IpcMessage.class));
    }

    @Test
    public void shouldCallExpanderAndTsidAssignerBeforeRouting() {
        Event event = createFaultEvent("uei.opennms.org/nodes/nodeDown", 1L);

        forwarder.sendNow(event);

        verify(mockExpander, times(1)).process(any());
        verify(mockTsidAssigner, times(1)).process(any());
    }

    @Test
    public void shouldUseZeroKeyForNodelessEvents() {
        Event event = new Event();
        event.setUei("uei.opennms.org/nodes/nodeDown");
        event.setAlarmData(new AlarmData());
        // no nodeId set

        forwarder.sendNow(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<Long, byte[]>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer, times(1)).send(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo(0L);
    }

    private Event createFaultEvent(String uei, Long nodeId) {
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid(nodeId);
        event.setAlarmData(new AlarmData());
        return event;
    }

    private Event createIpcEvent(String uei) {
        Event event = new Event();
        event.setUei(uei);
        return event;
    }

    private Event createDualEvent(String uei, Long nodeId) {
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid(nodeId);
        event.setAlarmData(new AlarmData());
        return event;
    }
}
```

**Step 2: Run test to verify it fails**

```bash
./compile.pl -T org.opennms.core.event.forwarder.kafka.KafkaEventForwarderTest \
  --projects :org.opennms.core.event-forwarder-kafka -am install
```
Expected: FAIL — `KafkaEventForwarder` class does not exist yet.

**Step 3: Write the implementation**

```java
package org.opennms.core.event.forwarder.kafka;

import java.util.Objects;
import java.util.function.Function;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.eventd.processor.TsidAssigner;
import org.opennms.netmgt.eventd.router.EventClassification;
import org.opennms.netmgt.eventd.router.EventClassifier;
import org.opennms.netmgt.eventd.router.IpcMessageConverter;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EventForwarder} implementation that enriches events locally and publishes
 * them directly to Kafka (FAULT events) or JMS MessageBus (IPC events).
 *
 * <p>This replaces the centralized Eventd daemon for microservice deployments.
 * Each daemon container carries this library and publishes events independently.</p>
 */
public class KafkaEventForwarder implements EventForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventForwarder.class);

    private final EventExpander eventExpander;
    private final TsidAssigner tsidAssigner;
    private final KafkaProducer<Long, byte[]> kafkaProducer;
    private final MessageBus messageBus;
    private final EventClassifier classifier;
    private final IpcMessageConverter ipcMessageConverter;
    private final String faultTopicName;
    private final Function<Event, byte[]> eventSerializer;

    public KafkaEventForwarder(EventExpander eventExpander,
                                TsidAssigner tsidAssigner,
                                KafkaProducer<Long, byte[]> kafkaProducer,
                                MessageBus messageBus,
                                EventClassifier classifier,
                                IpcMessageConverter ipcMessageConverter,
                                String faultTopicName,
                                Function<Event, byte[]> eventSerializer) {
        this.eventExpander = Objects.requireNonNull(eventExpander);
        this.tsidAssigner = Objects.requireNonNull(tsidAssigner);
        this.kafkaProducer = Objects.requireNonNull(kafkaProducer);
        this.messageBus = Objects.requireNonNull(messageBus);
        this.classifier = Objects.requireNonNull(classifier);
        this.ipcMessageConverter = Objects.requireNonNull(ipcMessageConverter);
        this.faultTopicName = Objects.requireNonNull(faultTopicName);
        this.eventSerializer = Objects.requireNonNull(eventSerializer);
    }

    @Override
    public void sendNow(Event event) {
        Log eventLog = wrapInLog(event);
        enrichAndRoute(eventLog);
    }

    @Override
    public void sendNow(Log eventLog) {
        enrichAndRoute(eventLog);
    }

    @Override
    public void sendNowSync(Event event) {
        sendNow(event);
    }

    @Override
    public void sendNowSync(Log eventLog) {
        sendNow(eventLog);
    }

    private void enrichAndRoute(Log eventLog) {
        try {
            eventExpander.process(eventLog);
            tsidAssigner.process(eventLog);
        } catch (EventProcessorException e) {
            LOG.error("Failed to enrich event log", e);
            return;
        }

        if (eventLog.getEvents() == null) {
            return;
        }
        for (Event event : eventLog.getEvents().getEvent()) {
            routeEvent(event);
        }
    }

    private void routeEvent(Event event) {
        EventClassification classification = classifier.classify(event);
        switch (classification) {
            case FAULT:
                publishToKafka(event);
                break;
            case IPC:
                publishToMessageBus(event);
                break;
            case DUAL:
                publishToKafka(event);
                publishToMessageBus(event);
                break;
        }
    }

    private void publishToKafka(Event event) {
        byte[] value = eventSerializer.apply(event);
        Long key = event.getNodeid() != null ? event.getNodeid() : 0L;
        kafkaProducer.send(new ProducerRecord<>(faultTopicName, key, value));
        LOG.debug("Published fault event {} (TSID={}) to Kafka topic {}",
                event.getUei(), event.getDbid(), faultTopicName);
    }

    private void publishToMessageBus(Event event) {
        messageBus.publish(ipcMessageConverter.convert(event));
        LOG.debug("Published IPC event {} to MessageBus", event.getUei());
    }

    private static Log wrapInLog(Event event) {
        Log log = new Log();
        Events events = new Events();
        events.getEvent().add(event);
        log.setEvents(events);
        return log;
    }
}
```

**Step 4: Run tests to verify they pass**

```bash
./compile.pl -T org.opennms.core.event.forwarder.kafka.KafkaEventForwarderTest \
  --projects :org.opennms.core.event-forwarder-kafka -am install
```
Expected: PASS (all 5 tests)

**Step 5: Commit**

```bash
git add core/event-forwarder-kafka/src/
git commit -m "feat: implement KafkaEventForwarder with event enrichment and routing"
```

---

## Phase 2: KafkaEventSubscriptionService

### Task 3: Implement `KafkaEventSubscriptionService`

This extends the existing `KafkaFaultEventConsumer` pattern from `features/events/kafka-consumer/` to implement the full `EventSubscriptionService` interface with UEI-based filtering.

**Files:**
- Create: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventSubscriptionService.java`
- Test: `core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaEventSubscriptionServiceTest.java`

**Step 1: Write the failing test**

```java
package org.opennms.core.event.forwarder.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.xml.event.Event;

public class KafkaEventSubscriptionServiceTest {

    private static final String TOPIC = "opennms-fault-events";

    @SuppressWarnings("unchecked")
    private KafkaConsumer<Long, byte[]> mockConsumer = mock(KafkaConsumer.class);
    private KafkaEventSubscriptionService service;

    @Before
    public void setUp() {
        service = new KafkaEventSubscriptionService(
                mockConsumer, TOPIC,
                data -> {
                    // Simple deserializer: UEI is encoded as string in the bytes
                    Event e = new Event();
                    e.setUei(new String(data));
                    return e;
                },
                Duration.ofMillis(50)
        );
    }

    @After
    public void tearDown() {
        service.stop();
    }

    @Test
    public void shouldDispatchToListenerByUei() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        EventListener listener = new EventListener() {
            @Override public String getName() { return "test"; }
            @Override public void onEvent(IEvent e) {
                received.add(e);
                latch.countDown();
            }
        };

        service.addEventListener(listener, "uei.opennms.org/nodes/nodeDown");
        feedRecords("uei.opennms.org/nodes/nodeDown");
        service.start();

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(received).hasSize(1);
        assertThat(received.get(0).getUei()).isEqualTo("uei.opennms.org/nodes/nodeDown");
    }

    @Test
    public void shouldNotDispatchUnmatchedUei() throws Exception {
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        EventListener listener = new EventListener() {
            @Override public String getName() { return "test"; }
            @Override public void onEvent(IEvent e) { received.add(e); }
        };

        service.addEventListener(listener, "uei.opennms.org/nodes/nodeUp");
        feedRecords("uei.opennms.org/nodes/nodeDown"); // different UEI
        service.start();

        Thread.sleep(500);
        assertThat(received).isEmpty();
    }

    @Test
    public void shouldSupportWildcardPrefixMatching() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        EventListener listener = new EventListener() {
            @Override public String getName() { return "test"; }
            @Override public void onEvent(IEvent e) {
                received.add(e);
                latch.countDown();
            }
        };

        // Subscribe to prefix — should match any UEI under /nodes/
        service.addEventListener(listener, "uei.opennms.org/nodes/");
        feedRecords("uei.opennms.org/nodes/nodeDown");
        service.start();

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(received).hasSize(1);
    }

    @Test
    public void shouldDispatchToAllEventsListener() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        EventListener listener = new EventListener() {
            @Override public String getName() { return "catch-all"; }
            @Override public void onEvent(IEvent e) {
                received.add(e);
                latch.countDown();
            }
        };

        // No UEI filter — listens to all events
        service.addEventListener(listener);
        feedRecords("uei.opennms.org/anything");
        service.start();

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(received).hasSize(1);
    }

    @Test
    public void shouldRemoveListener() throws Exception {
        CopyOnWriteArrayList<IEvent> received = new CopyOnWriteArrayList<>();

        EventListener listener = new EventListener() {
            @Override public String getName() { return "test"; }
            @Override public void onEvent(IEvent e) { received.add(e); }
        };

        service.addEventListener(listener, "uei.opennms.org/nodes/nodeDown");
        service.removeEventListener(listener);
        feedRecords("uei.opennms.org/nodes/nodeDown");
        service.start();

        Thread.sleep(500);
        assertThat(received).isEmpty();
    }

    private void feedRecords(String uei) {
        TopicPartition tp = new TopicPartition(TOPIC, 0);
        ConsumerRecord<Long, byte[]> record = new ConsumerRecord<>(
                TOPIC, 0, 0L, 0L, uei.getBytes());
        Map<TopicPartition, List<ConsumerRecord<Long, byte[]>>> recordMap = new HashMap<>();
        recordMap.put(tp, Collections.singletonList(record));
        ConsumerRecords<Long, byte[]> records = new ConsumerRecords<>(recordMap);

        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(ConsumerRecords.empty());
    }
}
```

**Step 2: Run test to verify it fails**

```bash
./compile.pl -T org.opennms.core.event.forwarder.kafka.KafkaEventSubscriptionServiceTest \
  --projects :org.opennms.core.event-forwarder-kafka -am install
```
Expected: FAIL — class does not exist.

**Step 3: Write the implementation**

```java
package org.opennms.core.event.forwarder.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.features.events.kafka.consumer.EventDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EventSubscriptionService} implementation that consumes FAULT events
 * from a Kafka topic and dispatches to registered {@link EventListener} instances
 * based on UEI matching.
 *
 * <p>Supports exact UEI matching and wildcard prefix matching (e.g., subscribing
 * to {@code "uei.opennms.org/nodes/"} matches all UEIs under that prefix).</p>
 *
 * <p>Each daemon type should use a distinct Kafka consumer group so that all
 * daemon types receive every event (fanout), while instances of the same type
 * share partitions (load balancing).</p>
 */
public class KafkaEventSubscriptionService implements EventSubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventSubscriptionService.class);

    private final KafkaConsumer<Long, byte[]> consumer;
    private final String topicName;
    private final EventDeserializer deserializer;
    private final Duration pollTimeout;

    private final Map<String, List<EventListener>> ueiListeners = new ConcurrentHashMap<>();
    private final List<EventListener> allEventsListeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread pollThread;

    public KafkaEventSubscriptionService(KafkaConsumer<Long, byte[]> consumer,
                                          String topicName,
                                          EventDeserializer deserializer,
                                          Duration pollTimeout) {
        this.consumer = Objects.requireNonNull(consumer);
        this.topicName = Objects.requireNonNull(topicName);
        this.deserializer = Objects.requireNonNull(deserializer);
        this.pollTimeout = Objects.requireNonNull(pollTimeout);
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            consumer.subscribe(Collections.singletonList(topicName));
            pollThread = new Thread(this::pollLoop, "kafka-event-subscription-poller");
            pollThread.setDaemon(true);
            pollThread.start();
            LOG.info("KafkaEventSubscriptionService started, topic={}", topicName);
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            consumer.wakeup();
            if (pollThread != null) {
                try {
                    pollThread.join(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            LOG.info("KafkaEventSubscriptionService stopped");
        }
    }

    @Override
    public void addEventListener(EventListener listener) {
        allEventsListeners.add(listener);
    }

    @Override
    public void addEventListener(EventListener listener, Collection<String> ueis) {
        for (String uei : ueis) {
            addEventListener(listener, uei);
        }
    }

    @Override
    public void addEventListener(EventListener listener, String uei) {
        ueiListeners.computeIfAbsent(uei, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void removeEventListener(EventListener listener) {
        allEventsListeners.remove(listener);
        ueiListeners.values().forEach(list -> list.remove(listener));
    }

    @Override
    public void removeEventListener(EventListener listener, Collection<String> ueis) {
        for (String uei : ueis) {
            removeEventListener(listener, uei);
        }
    }

    @Override
    public void removeEventListener(EventListener listener, String uei) {
        List<EventListener> listeners = ueiListeners.get(uei);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public boolean hasEventListener(String uei) {
        List<EventListener> listeners = ueiListeners.get(uei);
        return (listeners != null && !listeners.isEmpty()) || !allEventsListeners.isEmpty();
    }

    private void pollLoop() {
        while (running.get()) {
            try {
                ConsumerRecords<Long, byte[]> records = consumer.poll(pollTimeout);
                for (ConsumerRecord<Long, byte[]> record : records) {
                    processRecord(record);
                }
            } catch (org.apache.kafka.common.errors.WakeupException e) {
                if (running.get()) {
                    LOG.warn("KafkaConsumer wakeup while still running");
                }
            } catch (Exception e) {
                if (running.get()) {
                    LOG.error("Error polling Kafka topic {}", topicName, e);
                }
            }
        }
        consumer.close();
    }

    private void processRecord(ConsumerRecord<Long, byte[]> record) {
        Event mutableEvent;
        try {
            mutableEvent = deserializer.deserialize(record.value());
        } catch (Exception e) {
            LOG.error("Failed to deserialize event from partition={} offset={}",
                    record.partition(), record.offset(), e);
            return;
        }

        IEvent immutableEvent = ImmutableMapper.fromMutableEvent(mutableEvent);
        dispatch(immutableEvent);
    }

    private void dispatch(IEvent event) {
        String uei = event.getUei();
        Set<EventListener> dispatched = new HashSet<>();

        // 1. Dispatch to all-events listeners
        for (EventListener listener : allEventsListeners) {
            safeOnEvent(listener, event);
            dispatched.add(listener);
        }

        // 2. Exact UEI match + wildcard prefix matching
        // Matches "uei.opennms.org/nodes/nodeDown" against:
        //   "uei.opennms.org/nodes/nodeDown" (exact)
        //   "uei.opennms.org/nodes/" (prefix)
        //   "uei.opennms.org/" (prefix)
        for (String matchUei = uei; matchUei.length() > 0; ) {
            List<EventListener> listeners = ueiListeners.get(matchUei);
            if (listeners != null) {
                for (EventListener listener : listeners) {
                    if (dispatched.add(listener)) {
                        safeOnEvent(listener, event);
                    }
                }
            }
            int i = matchUei.lastIndexOf("/", matchUei.length() - 2);
            if (i > 0) {
                matchUei = matchUei.substring(0, i + 1);
            } else {
                break;
            }
        }
    }

    private void safeOnEvent(EventListener listener, IEvent event) {
        try {
            listener.onEvent(event);
        } catch (Exception e) {
            LOG.warn("Listener {} failed processing event {}",
                    listener.getName(), event.getUei(), e);
        }
    }
}
```

**Step 4: Add dependency on kafka-consumer module** (for EventDeserializer interface)

Add to `core/event-forwarder-kafka/pom.xml`:
```xml
<dependency>
    <groupId>org.opennms.features.events</groupId>
    <artifactId>org.opennms.features.events.kafka-consumer</artifactId>
    <version>${project.version}</version>
</dependency>
```

**Step 5: Run tests to verify they pass**

```bash
./compile.pl -T org.opennms.core.event.forwarder.kafka.KafkaEventSubscriptionServiceTest \
  --projects :org.opennms.core.event-forwarder-kafka -am install
```
Expected: PASS (all 5 tests)

**Step 6: Commit**

```bash
git add core/event-forwarder-kafka/src/ core/event-forwarder-kafka/pom.xml
git commit -m "feat: implement KafkaEventSubscriptionService with UEI wildcard matching"
```

---

### Task 4: Implement `KafkaAnnotationEventListenerAdapter`

Adapts beans with `@EventHandler` annotations to subscribe via `KafkaEventSubscriptionService` instead of the local `EventIpcManager`.

**Files:**
- Create: `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaAnnotationEventListenerAdapter.java`
- Test: `core/event-forwarder-kafka/src/test/java/org/opennms/core/event/forwarder/kafka/KafkaAnnotationEventListenerAdapterTest.java`

**Step 1: Write the failing test**

```java
package org.opennms.core.event.forwarder.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;

public class KafkaAnnotationEventListenerAdapterTest {

    @org.opennms.netmgt.events.api.annotations.EventListener(
            name = "TestDaemon", logPrefix = "test")
    public static class AnnotatedDaemon {
        @EventHandler(uei = "uei.opennms.org/nodes/nodeDown")
        public void handleNodeDown(org.opennms.netmgt.events.api.model.IEvent event) {
            // handler logic
        }

        @EventHandler(uei = "uei.opennms.org/nodes/nodeUp")
        public void handleNodeUp(org.opennms.netmgt.events.api.model.IEvent event) {
            // handler logic
        }
    }

    @Test
    public void shouldRegisterListenerForAnnotatedUeis() {
        EventSubscriptionService mockService = mock(EventSubscriptionService.class);
        AnnotatedDaemon daemon = new AnnotatedDaemon();

        KafkaAnnotationEventListenerAdapter adapter =
                new KafkaAnnotationEventListenerAdapter(daemon, mockService);
        adapter.afterPropertiesSet();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> ueiCaptor =
                ArgumentCaptor.forClass(Collection.class);
        verify(mockService).addEventListener(any(EventListener.class), ueiCaptor.capture());

        Collection<String> registeredUeis = ueiCaptor.getValue();
        assertThat(registeredUeis).containsExactlyInAnyOrder(
                "uei.opennms.org/nodes/nodeDown",
                "uei.opennms.org/nodes/nodeUp"
        );
    }
}
```

**Step 2: Run test to verify it fails**

```bash
./compile.pl -T org.opennms.core.event.forwarder.kafka.KafkaAnnotationEventListenerAdapterTest \
  --projects :org.opennms.core.event-forwarder-kafka -am install
```
Expected: FAIL — class does not exist.

**Step 3: Write the implementation**

This is a simplified version of `AnnotationBasedEventListenerAdapter` from
`features/events/api/src/main/java/org/opennms/netmgt/events/api/AnnotationBasedEventListenerAdapter.java`.
It reuses the same `@EventHandler` and `@EventListener` annotations but registers with
`EventSubscriptionService` (which can be the Kafka-backed implementation).

```java
package org.opennms.core.event.forwarder.kafka;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Discovers {@link EventHandler} annotations on a target bean and registers
 * the handler methods with an {@link EventSubscriptionService}.
 *
 * <p>This is the Kafka-compatible replacement for
 * {@code AnnotationBasedEventListenerAdapter}. Instead of registering with
 * the local {@code EventIpcManager}, it registers with whichever
 * {@code EventSubscriptionService} is injected — typically a
 * {@link KafkaEventSubscriptionService}.</p>
 */
public class KafkaAnnotationEventListenerAdapter implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAnnotationEventListenerAdapter.class);

    private final Object annotatedListener;
    private final EventSubscriptionService subscriptionService;
    private final String name;
    private final Map<String, Method> ueiToHandler = new HashMap<>();

    public KafkaAnnotationEventListenerAdapter(Object annotatedListener,
                                                EventSubscriptionService subscriptionService) {
        this.annotatedListener = Objects.requireNonNull(annotatedListener);
        this.subscriptionService = Objects.requireNonNull(subscriptionService);

        var listenerAnnotation = annotatedListener.getClass()
                .getAnnotation(org.opennms.netmgt.events.api.annotations.EventListener.class);
        this.name = listenerAnnotation != null ? listenerAnnotation.name()
                : annotatedListener.getClass().getSimpleName();
    }

    @Override
    public void afterPropertiesSet() {
        discoverHandlers();
        registerWithSubscriptionService();
    }

    private void discoverHandlers() {
        for (Method method : annotatedListener.getClass().getMethods()) {
            EventHandler handler = method.getAnnotation(EventHandler.class);
            if (handler != null) {
                String uei = handler.uei();
                if (uei != null && !uei.isEmpty()) {
                    ueiToHandler.put(uei, method);
                    LOG.debug("{}: registered handler {} for UEI {}", name, method.getName(), uei);
                }
            }
        }
    }

    private void registerWithSubscriptionService() {
        if (ueiToHandler.isEmpty()) {
            LOG.warn("{}: no @EventHandler methods found", name);
            return;
        }

        List<String> ueis = new ArrayList<>(ueiToHandler.keySet());
        EventListener delegate = new EventListener() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public void onEvent(IEvent event) {
                Method handler = ueiToHandler.get(event.getUei());
                if (handler == null) {
                    return;
                }
                try {
                    handler.invoke(annotatedListener, event);
                } catch (Exception e) {
                    LOG.error("{}: handler {} failed for event {}",
                            name, handler.getName(), event.getUei(), e);
                }
            }
        };

        subscriptionService.addEventListener(delegate, ueis);
        LOG.info("{}: registered for {} UEIs via {}", name, ueis.size(),
                subscriptionService.getClass().getSimpleName());
    }
}
```

**Step 4: Run tests to verify they pass**

```bash
./compile.pl -T org.opennms.core.event.forwarder.kafka.KafkaAnnotationEventListenerAdapterTest \
  --projects :org.opennms.core.event-forwarder-kafka -am install
```
Expected: PASS

**Step 5: Commit**

```bash
git add core/event-forwarder-kafka/src/
git commit -m "feat: implement KafkaAnnotationEventListenerAdapter for @EventHandler discovery"
```

---

### Task 5: Run All Module Tests and Verify Clean Build

**Step 1: Run all tests in the new module**

```bash
./compile.pl --projects :org.opennms.core.event-forwarder-kafka -am verify
```
Expected: All tests pass, BUILD SUCCESS.

**Step 2: Verify the module builds cleanly with the full project**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.event-forwarder-kafka -am install
```
Expected: BUILD SUCCESS, all dependencies resolve.

**Step 3: Commit any fixups**

If any import or dependency issues arose, fix and commit:
```bash
git add -u
git commit -m "fix: resolve build issues in event-forwarder-kafka module"
```

---

## Summary: What These 5 Tasks Deliver

After completing Tasks 1-5, you have:

1. **`KafkaEventForwarder`** — Drop-in `EventForwarder` replacement that enriches events locally and publishes to Kafka (FAULT) or ActiveMQ (IPC). Any daemon can use this instead of `EventIpcManager.sendNow()`.

2. **`KafkaEventSubscriptionService`** — Drop-in `EventSubscriptionService` replacement that consumes FAULT events from Kafka and dispatches to registered listeners with UEI wildcard matching. Replaces `EventIpcManager`'s local broadcast.

3. **`KafkaAnnotationEventListenerAdapter`** — Discovers `@EventHandler` annotations and wires them to `KafkaEventSubscriptionService`. Drop-in replacement for `AnnotationBasedEventListenerAdapter`.

## Next Steps (Not in This Plan)

- **Phase 3:** Wire `KafkaEventForwarder` + `KafkaEventSubscriptionService` into the first daemon (Pollerd) with a flat standalone Spring context
- **Phase 4:** Make Eventd disableable in confd templates
- **Phase 5:** Extract remaining daemons into per-container deployment
