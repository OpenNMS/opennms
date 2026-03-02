# EventBus Redesign Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace the monolithic event bus with a Kafka-backed fault event pipeline and JMS-backed IPC MessageBus, enabling Alarmd extraction as the first microservice.

**Architecture:** EventRouter pattern — sits behind the existing EventIpcManager facade, classifies events (fault vs IPC), and delegates to Kafka (faults) or JMS/ActiveMQ (IPC). TSID replaces the PostgreSQL sequence for event identity. Alarm schema denormalized with JSONB event data.

**Tech Stack:** Java 17, Kafka 3.6.2, ActiveMQ 5.16.8, Protobuf 3.25.5, Liquibase 3.6.3, Spring 4.2.x, OSGi/Karaf 4.3.10

---

## Phase A: Foundation

### Task 1: Create core/tsid Module — TsidFactory

**Files:**
- Create: `core/tsid/pom.xml`
- Create: `core/tsid/src/main/java/org/opennms/core/tsid/TsidFactory.java`
- Create: `core/tsid/src/test/java/org/opennms/core/tsid/TsidFactoryTest.java`
- Modify: `core/pom.xml` — add `<module>tsid</module>`

**Step 1: Create module POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.opennms</groupId>
        <artifactId>org.opennms.core</artifactId>
        <version>36.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>org.opennms.core.tsid</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Core :: TSID</name>
    <description>Time-Sorted ID generation (Snowflake-style) for events and messages.</description>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
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

**Step 2: Add module to core/pom.xml**

Add `<module>tsid</module>` to the `<modules>` list in `core/pom.xml`.

**Step 3: Write the failing test**

```java
package org.opennms.core.tsid;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class TsidFactoryTest {

    @Test
    public void shouldGeneratePositiveLong() {
        TsidFactory factory = new TsidFactory(1);
        long id = factory.create();
        assertThat(id).isPositive();
    }

    @Test
    public void shouldGenerateMonotonicallyIncreasingIds() {
        TsidFactory factory = new TsidFactory(1);
        long prev = factory.create();
        for (int i = 0; i < 1000; i++) {
            long next = factory.create();
            assertThat(next).isGreaterThan(prev);
            prev = next;
        }
    }

    @Test
    public void shouldGenerateUniqueIdsAcrossThreads() throws Exception {
        TsidFactory factory = new TsidFactory(1);
        int threadCount = 8;
        int idsPerThread = 10_000;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                for (int i = 0; i < idsPerThread; i++) {
                    ids.add(factory.create());
                }
                latch.countDown();
            });
        }
        latch.await();
        executor.shutdown();
        assertThat(ids).hasSize(threadCount * idsPerThread);
    }

    @Test
    public void shouldRejectInvalidNodeId() {
        org.junit.Assert.assertThrows(IllegalArgumentException.class, () -> new TsidFactory(-1));
        org.junit.Assert.assertThrows(IllegalArgumentException.class, () -> new TsidFactory(1024));
    }
}
```

**Step 4: Run test to verify it fails**

Run: `./compile.pl --projects :org.opennms.core.tsid -am verify`
Expected: FAIL — `TsidFactory` class does not exist.

**Step 5: Implement TsidFactory**

```java
package org.opennms.core.tsid;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates 64-bit time-sorted unique IDs (Snowflake-style).
 *
 * Layout (64 bits):
 *   [1 bit unused] [41 bits: ms since epoch] [10 bits: nodeId] [12 bits: sequence]
 *
 * - 41 bits of time: ~69 years from custom epoch
 * - 10 bits of node: 1024 distinct JVMs
 * - 12 bits of sequence: 4096 IDs per millisecond per node
 */
public class TsidFactory {

    private static final long CUSTOM_EPOCH = 1704067200000L; // 2024-01-01T00:00:00Z
    private static final int NODE_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final long MAX_NODE_ID = (1L << NODE_BITS) - 1;  // 1023
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1; // 4095
    private static final int NODE_SHIFT = SEQUENCE_BITS;
    private static final int TIMESTAMP_SHIFT = NODE_BITS + SEQUENCE_BITS;

    private final long nodeId;
    private final AtomicLong state; // packed: [timestamp | sequence]

    public TsidFactory(long nodeId) {
        if (nodeId < 0 || nodeId > MAX_NODE_ID) {
            throw new IllegalArgumentException(
                    "nodeId must be between 0 and " + MAX_NODE_ID + ", got: " + nodeId);
        }
        this.nodeId = nodeId;
        this.state = new AtomicLong(packState(currentTimestamp(), 0));
    }

    public long create() {
        while (true) {
            long oldState = state.get();
            long oldTs = unpackTimestamp(oldState);
            long oldSeq = unpackSequence(oldState);
            long now = currentTimestamp();

            long newTs;
            long newSeq;
            if (now > oldTs) {
                newTs = now;
                newSeq = 0;
            } else {
                // Same millisecond or clock went backwards
                newSeq = oldSeq + 1;
                if (newSeq > MAX_SEQUENCE) {
                    // Sequence exhausted for this ms, advance to next
                    newTs = oldTs + 1;
                    newSeq = 0;
                } else {
                    newTs = oldTs;
                }
            }

            long newState = packState(newTs, newSeq);
            if (state.compareAndSet(oldState, newState)) {
                return (newTs << TIMESTAMP_SHIFT) | (nodeId << NODE_SHIFT) | newSeq;
            }
            // CAS failed, retry
        }
    }

    private long currentTimestamp() {
        return System.currentTimeMillis() - CUSTOM_EPOCH;
    }

    private static long packState(long timestamp, long sequence) {
        return (timestamp << SEQUENCE_BITS) | sequence;
    }

    private static long unpackTimestamp(long state) {
        return state >>> SEQUENCE_BITS;
    }

    private static long unpackSequence(long state) {
        return state & MAX_SEQUENCE;
    }
}
```

**Step 6: Run test to verify it passes**

Run: `./compile.pl --projects :org.opennms.core.tsid -am verify`
Expected: PASS — all 4 tests green.

**Step 7: Commit**

```bash
git add core/tsid/ core/pom.xml
git commit -m "feat: add core/tsid module with Snowflake-style TsidFactory"
```

---

### Task 2: Create core/messagebus-api Module — IpcMessage, MessageBus, MessageHandler

**Files:**
- Create: `core/messagebus-api/pom.xml`
- Create: `core/messagebus-api/src/main/java/org/opennms/core/messagebus/IpcMessage.java`
- Create: `core/messagebus-api/src/main/java/org/opennms/core/messagebus/MessageBus.java`
- Create: `core/messagebus-api/src/main/java/org/opennms/core/messagebus/MessageHandler.java`
- Create: `core/messagebus-api/src/test/java/org/opennms/core/messagebus/IpcMessageTest.java`
- Modify: `core/pom.xml` — add `<module>messagebus-api</module>`

**Step 1: Create module POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.opennms</groupId>
        <artifactId>org.opennms.core</artifactId>
        <version>36.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>org.opennms.core.messagebus.api</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Core :: MessageBus :: API</name>
    <description>IPC MessageBus API for inter-daemon communication.</description>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
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

**Step 2: Add module to core/pom.xml**

Add `<module>messagebus-api</module>` to the `<modules>` list.

**Step 3: Write IpcMessage**

```java
package org.opennms.core.messagebus;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IpcMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String type;
    private final String source;
    private final long timestamp;
    private final Long nodeId;
    private final String interfaceAddress;
    private final Map<String, String> parameters;

    public IpcMessage(String type, String source) {
        this(type, source, System.currentTimeMillis(), null, null, Collections.emptyMap());
    }

    public IpcMessage(String type, String source, Map<String, String> parameters) {
        this(type, source, System.currentTimeMillis(), null, null, parameters);
    }

    public IpcMessage(String type, String source, long timestamp,
                      Long nodeId, String interfaceAddress,
                      Map<String, String> parameters) {
        this.type = Objects.requireNonNull(type, "type");
        this.source = Objects.requireNonNull(source, "source");
        this.timestamp = timestamp;
        this.nodeId = nodeId;
        this.interfaceAddress = interfaceAddress;
        this.parameters = parameters != null
                ? Collections.unmodifiableMap(new HashMap<>(parameters))
                : Collections.emptyMap();
    }

    public String getType() { return type; }
    public String getSource() { return source; }
    public long getTimestamp() { return timestamp; }
    public Long getNodeId() { return nodeId; }
    public String getInterfaceAddress() { return interfaceAddress; }
    public Map<String, String> getParameters() { return parameters; }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    @Override
    public String toString() {
        return "IpcMessage{type='" + type + "', source='" + source + "', nodeId=" + nodeId + "}";
    }
}
```

**Step 4: Write MessageHandler**

```java
package org.opennms.core.messagebus;

public interface MessageHandler {
    String getName();
    void onMessage(IpcMessage message);
}
```

**Step 5: Write MessageBus**

```java
package org.opennms.core.messagebus;

import java.util.Collection;

public interface MessageBus {
    void publish(IpcMessage message);
    void subscribe(String messageType, MessageHandler handler);
    void subscribe(Collection<String> messageTypes, MessageHandler handler);
    void unsubscribe(MessageHandler handler);
}
```

**Step 6: Write test for IpcMessage**

```java
package org.opennms.core.messagebus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.Test;

public class IpcMessageTest {

    @Test
    public void shouldCreateWithRequiredFields() {
        IpcMessage msg = new IpcMessage("reloadDaemonConfig", "webui");
        assertThat(msg.getType()).isEqualTo("reloadDaemonConfig");
        assertThat(msg.getSource()).isEqualTo("webui");
        assertThat(msg.getTimestamp()).isPositive();
        assertThat(msg.getParameters()).isEmpty();
    }

    @Test
    public void shouldCreateWithParameters() {
        IpcMessage msg = new IpcMessage("reloadDaemonConfig", "webui",
                Map.of("daemonName", "pollerd"));
        assertThat(msg.getParameter("daemonName")).isEqualTo("pollerd");
    }

    @Test
    public void shouldRejectNullType() {
        assertThatThrownBy(() -> new IpcMessage(null, "webui"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldReturnUnmodifiableParameters() {
        IpcMessage msg = new IpcMessage("test", "src", Map.of("k", "v"));
        assertThatThrownBy(() -> msg.getParameters().put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
```

**Step 7: Run tests**

Run: `./compile.pl --projects :org.opennms.core.messagebus.api -am verify`
Expected: PASS

**Step 8: Commit**

```bash
git add core/messagebus-api/ core/pom.xml
git commit -m "feat: add core/messagebus-api module with IpcMessage, MessageBus, MessageHandler"
```

---

### Task 3: Create TsidAssigner EventProcessor

**Files:**
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/TsidAssigner.java`
- Create: `features/events/daemon/src/test/java/org/opennms/netmgt/eventd/processor/TsidAssignerTest.java`
- Modify: `features/events/daemon/pom.xml` — add dependency on `org.opennms.core.tsid`

**Step 1: Add TSID dependency to features/events/daemon/pom.xml**

Add to `<dependencies>`:
```xml
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.tsid</artifactId>
    <version>${project.version}</version>
</dependency>
```

**Step 2: Write the failing test**

```java
package org.opennms.netmgt.eventd.processor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.tsid.TsidFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class TsidAssignerTest {

    private TsidAssigner tsidAssigner;

    @Before
    public void setUp() {
        tsidAssigner = new TsidAssigner(new TsidFactory(0));
    }

    @Test
    public void shouldAssignTsidToEvent() throws Exception {
        Event event = new Event();
        event.setUei("uei.opennms.org/test");
        Log log = createLog(event);

        tsidAssigner.process(log);

        assertThat(event.getDbid()).isNotNull().isPositive();
    }

    @Test
    public void shouldAssignUniqueTsidsToMultipleEvents() throws Exception {
        Event event1 = new Event();
        event1.setUei("uei.opennms.org/test1");
        Event event2 = new Event();
        event2.setUei("uei.opennms.org/test2");
        Log log = createLog(event1, event2);

        tsidAssigner.process(log);

        assertThat(event1.getDbid()).isNotNull();
        assertThat(event2.getDbid()).isNotNull();
        assertThat(event1.getDbid()).isNotEqualTo(event2.getDbid());
    }

    @Test
    public void shouldNotOverwriteExistingDbid() throws Exception {
        Event event = new Event();
        event.setUei("uei.opennms.org/test");
        event.setDbid(42L);
        Log log = createLog(event);

        tsidAssigner.process(log);

        assertThat(event.getDbid()).isEqualTo(42L);
    }

    private Log createLog(Event... events) {
        Events eventsContainer = new Events();
        for (Event e : events) {
            eventsContainer.addEvent(e);
        }
        Log log = new Log();
        log.setEvents(eventsContainer);
        return log;
    }
}
```

**Step 3: Run test to verify it fails**

Run: `./compile.pl --projects :org.opennms.features.events.daemon -am verify -Dtest=TsidAssignerTest`
Expected: FAIL — TsidAssigner does not exist.

**Step 4: Implement TsidAssigner**

```java
package org.opennms.netmgt.eventd.processor;

import org.opennms.core.tsid.TsidFactory;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TsidAssigner implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TsidAssigner.class);

    private final TsidFactory tsidFactory;

    public TsidAssigner(TsidFactory tsidFactory) {
        this.tsidFactory = tsidFactory;
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog.getEvents() == null) {
            return;
        }
        for (Event event : eventLog.getEvents().getEvent()) {
            if (event.getDbid() == null || event.getDbid() == 0) {
                long tsid = tsidFactory.create();
                event.setDbid(tsid);
                LOG.debug("Assigned TSID {} to event {}", tsid, event.getUei());
            }
        }
    }
}
```

**Step 5: Run test to verify it passes**

Run: `./compile.pl --projects :org.opennms.features.events.daemon -am verify -Dtest=TsidAssignerTest`
Expected: PASS

**Step 6: Commit**

```bash
git add features/events/daemon/pom.xml \
      features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/TsidAssigner.java \
      features/events/daemon/src/test/java/org/opennms/netmgt/eventd/processor/TsidAssignerTest.java
git commit -m "feat: add TsidAssigner EventProcessor for Snowflake-style event IDs"
```

---

### Task 4: Create FaultEventPublisher EventProcessor

**Files:**
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/FaultEventPublisher.java`
- Create: `features/events/daemon/src/test/java/org/opennms/netmgt/eventd/processor/FaultEventPublisherTest.java`
- Modify: `features/events/daemon/pom.xml` — add Kafka client dependency

**Step 1: Add Kafka dependency to features/events/daemon/pom.xml**

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>${kafkaVersion}</version>
    <scope>provided</scope>
</dependency>
```

Note: `provided` scope because the Kafka bundle is loaded via Karaf feature at runtime.

**Step 2: Write the failing test**

```java
package org.opennms.netmgt.eventd.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class FaultEventPublisherTest {

    private KafkaProducer<Long, byte[]> mockProducer;
    private FaultEventPublisher publisher;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        mockProducer = mock(KafkaProducer.class);
        publisher = new FaultEventPublisher(mockProducer, "opennms-fault-events");
    }

    @Test
    public void shouldPublishFaultEventToKafka() throws Exception {
        Event event = createFaultEvent("uei.opennms.org/nodes/nodeDown", 42L);
        Log log = createLog(event);

        publisher.process(log);

        ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer, times(1)).send(captor.capture());
        ProducerRecord<Long, byte[]> record = captor.getValue();
        assertThat(record.topic()).isEqualTo("opennms-fault-events");
        assertThat(record.key()).isEqualTo(42L);
        assertThat(record.value()).isNotEmpty();
    }

    @Test
    public void shouldSkipEventsWithoutAlarmData() throws Exception {
        Event event = new Event();
        event.setUei("uei.opennms.org/internal/reloadDaemonConfig");
        Log log = createLog(event);

        publisher.process(log);

        verify(mockProducer, never()).send(any());
    }

    @Test
    public void shouldUseZeroKeyForNodelessEvents() throws Exception {
        Event event = createFaultEvent("uei.opennms.org/threshold/highThreshold", null);
        Log log = createLog(event);

        publisher.process(log);

        ArgumentCaptor<ProducerRecord> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer, times(1)).send(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo(0L);
    }

    private Event createFaultEvent(String uei, Long nodeId) {
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid(nodeId != null ? nodeId : 0L);
        event.setDbid(123456789L);
        AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey(uei + ":" + nodeId);
        alarmData.setAlarmType(1);
        event.setAlarmData(alarmData);
        return event;
    }

    private Log createLog(Event... events) {
        Events eventsContainer = new Events();
        for (Event e : events) {
            eventsContainer.addEvent(e);
        }
        Log log = new Log();
        log.setEvents(eventsContainer);
        return log;
    }
}
```

**Step 3: Run test to verify it fails**

Run: `./compile.pl --projects :org.opennms.features.events.daemon -am verify -Dtest=FaultEventPublisherTest`
Expected: FAIL — FaultEventPublisher does not exist.

**Step 4: Implement FaultEventPublisher**

```java
package org.opennms.netmgt.eventd.processor;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaultEventPublisher implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(FaultEventPublisher.class);

    private final KafkaProducer<Long, byte[]> producer;
    private final String topicName;

    public FaultEventPublisher(KafkaProducer<Long, byte[]> producer, String topicName) {
        this.producer = producer;
        this.topicName = topicName;
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog.getEvents() == null) {
            return;
        }
        for (Event event : eventLog.getEvents().getEvent()) {
            if (!isFaultEvent(event)) {
                LOG.debug("Skipping non-fault event: {}", event.getUei());
                continue;
            }
            byte[] value = serializeEvent(event);
            Long key = event.getNodeid() != null ? event.getNodeid() : 0L;
            producer.send(new ProducerRecord<>(topicName, key, value));
            LOG.debug("Published fault event {} (TSID={}) to Kafka topic {}",
                    event.getUei(), event.getDbid(), topicName);
        }
    }

    private boolean isFaultEvent(Event event) {
        return event.getAlarmData() != null;
    }

    private byte[] serializeEvent(Event event) {
        // Protobuf serialization - delegates to ProtobufMapper
        // For now, use XML serialization as a baseline; Task 9 will integrate ProtobufMapper
        try {
            return event.marshal().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event: " + event.getUei(), e);
        }
    }
}
```

**Step 5: Run test to verify it passes**

Run: `./compile.pl --projects :org.opennms.features.events.daemon -am verify -Dtest=FaultEventPublisherTest`
Expected: PASS

**Step 6: Commit**

```bash
git add features/events/daemon/pom.xml \
      features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/FaultEventPublisher.java \
      features/events/daemon/src/test/java/org/opennms/netmgt/eventd/processor/FaultEventPublisherTest.java
git commit -m "feat: add FaultEventPublisher EventProcessor for Kafka fault event pipeline"
```

---

### Task 5: Create EventRouter — Classification and Routing Logic

**Files:**
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/router/EventClassification.java`
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/router/EventClassifier.java`
- Create: `features/events/daemon/src/test/java/org/opennms/netmgt/eventd/router/EventClassifierTest.java`
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/router/EventRouter.java`
- Modify: `features/events/daemon/pom.xml` — add dependency on `org.opennms.core.messagebus.api`

**Step 1: Add MessageBus API dependency**

Add to features/events/daemon/pom.xml `<dependencies>`:
```xml
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.messagebus.api</artifactId>
    <version>${project.version}</version>
</dependency>
```

**Step 2: Write EventClassification enum**

```java
package org.opennms.netmgt.eventd.router;

public enum EventClassification {
    FAULT,      // -> Kafka + local broadcast
    IPC,        // -> JMS MessageBus + local broadcast
    DUAL        // -> both Kafka AND JMS (internal events with alarm-data)
}
```

**Step 3: Write the failing test for EventClassifier**

```java
package org.opennms.netmgt.eventd.router;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;

public class EventClassifierTest {

    private final EventClassifier classifier = new EventClassifier();

    @Test
    public void shouldClassifyEventWithAlarmDataAsFault() {
        Event event = eventWithAlarmData("uei.opennms.org/nodes/nodeDown");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.FAULT);
    }

    @Test
    public void shouldClassifyInternalEventWithoutAlarmDataAsIpc() {
        Event event = eventWithoutAlarmData("uei.opennms.org/internal/reloadDaemonConfig");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.IPC);
    }

    @Test
    public void shouldClassifyInternalEventWithAlarmDataAsDual() {
        Event event = eventWithAlarmData("uei.opennms.org/internal/reloadDaemonConfigFailed");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.DUAL);
    }

    @Test
    public void shouldClassifyTrapAsFault() {
        Event event = eventWithoutAlarmData("uei.opennms.org/traps/linkDown");
        // Traps without alarm-data are still fault events (they may be informational)
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.FAULT);
    }

    @Test
    public void shouldClassifyCapsdInternalAsIpc() {
        Event event = eventWithoutAlarmData("uei.opennms.org/internal/capsd/forceRescan");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.IPC);
    }

    @Test
    public void shouldClassifyDiscoveryInternalAsIpc() {
        Event event = eventWithoutAlarmData("uei.opennms.org/internal/discovery/newSuspect");
        assertThat(classifier.classify(event)).isEqualTo(EventClassification.IPC);
    }

    private Event eventWithAlarmData(String uei) {
        Event event = new Event();
        event.setUei(uei);
        AlarmData ad = new AlarmData();
        ad.setReductionKey(uei + ":1");
        ad.setAlarmType(1);
        event.setAlarmData(ad);
        return event;
    }

    private Event eventWithoutAlarmData(String uei) {
        Event event = new Event();
        event.setUei(uei);
        return event;
    }
}
```

**Step 4: Run test to verify it fails**

Run: `./compile.pl --projects :org.opennms.features.events.daemon -am verify -Dtest=EventClassifierTest`
Expected: FAIL

**Step 5: Implement EventClassifier**

```java
package org.opennms.netmgt.eventd.router;

import org.opennms.netmgt.xml.event.Event;

public class EventClassifier {

    private static final String INTERNAL_UEI_PREFIX = "uei.opennms.org/internal/";

    public EventClassification classify(Event event) {
        boolean hasAlarmData = event.getAlarmData() != null;
        boolean isInternal = event.getUei() != null
                && event.getUei().startsWith(INTERNAL_UEI_PREFIX);

        if (isInternal && hasAlarmData) {
            return EventClassification.DUAL;
        }
        if (isInternal) {
            return EventClassification.IPC;
        }
        // Everything else is a fault event: traps, syslog, thresholds, node events, etc.
        return EventClassification.FAULT;
    }
}
```

**Step 6: Run test to verify it passes**

Run: `./compile.pl --projects :org.opennms.features.events.daemon -am verify -Dtest=EventClassifierTest`
Expected: PASS

**Step 7: Write EventRouter**

The EventRouter is an `EventProcessor` that replaces both `HibernateEventWriter` and `EventIpcBroadcastProcessor` in the chain. It classifies each event and delegates to the appropriate publisher while also broadcasting locally.

```java
package org.opennms.netmgt.eventd.router;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventRouter implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(EventRouter.class);

    private final EventClassifier classifier;
    private final EventProcessor faultEventPublisher;
    private final MessageBus messageBus;
    private final EventIpcBroadcaster localBroadcaster;
    private final IpcMessageConverter ipcMessageConverter;

    public EventRouter(EventClassifier classifier,
                       EventProcessor faultEventPublisher,
                       MessageBus messageBus,
                       EventIpcBroadcaster localBroadcaster,
                       IpcMessageConverter ipcMessageConverter) {
        this.classifier = classifier;
        this.faultEventPublisher = faultEventPublisher;
        this.messageBus = messageBus;
        this.localBroadcaster = localBroadcaster;
        this.ipcMessageConverter = ipcMessageConverter;
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog.getEvents() == null) {
            return;
        }
        for (Event event : eventLog.getEvents().getEvent()) {
            EventClassification classification = classifier.classify(event);
            LOG.debug("Event {} classified as {}", event.getUei(), classification);

            switch (classification) {
                case FAULT:
                    publishFaultEvent(eventLog, event, synchronous);
                    broadcastLocally(event, synchronous);
                    break;
                case IPC:
                    publishIpcMessage(event);
                    broadcastLocally(event, synchronous);
                    break;
                case DUAL:
                    publishFaultEvent(eventLog, event, synchronous);
                    publishIpcMessage(event);
                    broadcastLocally(event, synchronous);
                    break;
            }
        }
    }

    private void publishFaultEvent(Log originalLog, Event event, boolean synchronous)
            throws EventProcessorException {
        // Delegate to FaultEventPublisher for Kafka publishing
        Log singleEventLog = new Log();
        org.opennms.netmgt.xml.event.Events events = new org.opennms.netmgt.xml.event.Events();
        events.addEvent(event);
        singleEventLog.setEvents(events);
        singleEventLog.setHeader(originalLog.getHeader());
        faultEventPublisher.process(singleEventLog, synchronous);
    }

    private void publishIpcMessage(Event event) {
        IpcMessage message = ipcMessageConverter.convert(event);
        messageBus.publish(message);
    }

    private void broadcastLocally(Event event, boolean synchronous) {
        if (event.getLogmsg() != null && "suppress".equals(event.getLogmsg().getDest())) {
            LOG.debug("Suppressing local broadcast for event {}", event.getUei());
            return;
        }
        localBroadcaster.broadcastNow(event, synchronous);
    }
}
```

**Step 8: Write IpcMessageConverter**

```java
package org.opennms.netmgt.eventd.router;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

public class IpcMessageConverter {

    private static final String INTERNAL_UEI_PREFIX = "uei.opennms.org/internal/";

    public IpcMessage convert(Event event) {
        String type = deriveMessageType(event.getUei());
        Map<String, String> parameters = new HashMap<>();
        if (event.getParmCollection() != null) {
            for (Parm parm : event.getParmCollection()) {
                if (parm.getValue() != null) {
                    parameters.put(parm.getParmName(), parm.getValue().getContent());
                }
            }
        }
        return new IpcMessage(
                type,
                event.getSource(),
                event.getTime() != null ? event.getTime().getTime() : System.currentTimeMillis(),
                event.getNodeid(),
                event.getInterface(),
                parameters
        );
    }

    private String deriveMessageType(String uei) {
        if (uei != null && uei.startsWith(INTERNAL_UEI_PREFIX)) {
            // "uei.opennms.org/internal/reloadDaemonConfig" -> "reloadDaemonConfig"
            // "uei.opennms.org/internal/discovery/newSuspect" -> "discovery/newSuspect"
            return uei.substring(INTERNAL_UEI_PREFIX.length());
        }
        return uei;
    }
}
```

**Step 9: Commit**

```bash
git add features/events/daemon/pom.xml \
      features/events/daemon/src/main/java/org/opennms/netmgt/eventd/router/
git commit -m "feat: add EventRouter with classification, Kafka fault publishing, and IPC routing"
```

---

## Phase B: Alarm Schema and Persistence

### Task 6: Liquibase Migration — Alarm Schema Changes

**Files:**
- Create: `core/schema/src/main/liquibase/36.0.0/changelog.xml`
- Modify: `core/schema/src/assembly/liquibase.xml` — include new changelog

**Step 1: Check existing Liquibase structure**

Read `core/schema/src/assembly/liquibase.xml` to determine how to add a new version changelog.

**Step 2: Create the migration changelog**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd">

    <changeSet id="36.0.0-add-alarm-event-denormalization" author="opennms">
        <comment>Denormalize event data onto alarms for Kafka-backed event pipeline</comment>

        <addColumn tableName="alarms">
            <column name="event_tsid" type="BIGINT"/>
            <column name="event_uei" type="VARCHAR(256)"/>
            <column name="event_source" type="VARCHAR(256)"/>
            <column name="event_severity" type="INTEGER"/>
            <column name="event_timestamp" type="TIMESTAMP WITH TIME ZONE"/>
            <column name="event_node_id" type="BIGINT"/>
            <column name="event_log_msg" type="TEXT"/>
            <column name="last_event_data" type="TEXT"/>
        </addColumn>

        <createIndex tableName="alarms" indexName="idx_alarms_event_tsid">
            <column name="event_tsid"/>
        </createIndex>
    </changeSet>

    <changeSet id="36.0.0-backfill-alarm-event-data" author="opennms">
        <comment>Backfill denormalized event data from existing events table</comment>
        <sql>
            UPDATE alarms a SET
                event_tsid = e.eventid,
                event_uei = e.eventuei,
                event_source = e.eventsource,
                event_severity = e.eventseverity,
                event_timestamp = e.eventtime,
                event_node_id = e.nodeid,
                event_log_msg = e.eventlogmsg
            FROM events e
            WHERE a.lasteventid = e.eventid
              AND a.event_tsid IS NULL;
        </sql>
    </changeSet>

    <changeSet id="36.0.0-drop-alarm-event-fk" author="opennms">
        <comment>Drop FK from alarms to events table — events move to Kafka</comment>
        <dropForeignKeyConstraint baseTableName="alarms"
                                  constraintName="fk_alarms_eventid"/>
        <dropColumn tableName="alarms" columnName="lasteventid"/>
    </changeSet>

    <changeSet id="36.0.0-drop-events-table" author="opennms">
        <comment>Remove events table — events are now stored in Kafka</comment>
        <dropTable tableName="events" cascadeConstraints="true"/>
        <sql>DROP SEQUENCE IF EXISTS eventsnxtid;</sql>
    </changeSet>

</databaseChangeLog>
```

Note: The `last_event_data` column uses `TEXT` instead of `JSONB` for Liquibase/Hibernate compatibility. The application layer serializes/deserializes JSON. Alternatively, if the PostgreSQL version supports it and Hibernate mapping allows, use native `JSONB`.

**Step 3: Register in master changelog**

Add `<include file="36.0.0/changelog.xml"/>` to the master `liquibase.xml`.

**Step 4: Commit**

```bash
git add core/schema/src/main/liquibase/36.0.0/ core/schema/src/assembly/liquibase.xml
git commit -m "feat: add Liquibase migration for alarm event denormalization and events table removal"
```

---

### Task 7: Modify OnmsAlarm Entity — New Columns, Drop FK

**Files:**
- Modify: `opennms-model/src/main/java/org/opennms/netmgt/model/OnmsAlarm.java`

**Step 1: Read the current OnmsAlarm entity**

Read the full file to understand the existing fields, especially `m_lastEvent` and `getLastEvent()`.

**Step 2: Add new fields and remove lastEvent FK**

Add new fields:
```java
private Long m_eventTsid;
private String m_eventUei;
private String m_eventSource;
private Integer m_eventSeverity;
private Date m_eventTimestamp;
private Long m_eventNodeId;
private String m_eventLogMsg;
private String m_lastEventData; // JSON string
```

Add getters/setters with JPA column mappings:
```java
@Column(name="event_tsid")
public Long getEventTsid() { return m_eventTsid; }
public void setEventTsid(Long eventTsid) { m_eventTsid = eventTsid; }

@Column(name="event_uei", length=256)
public String getEventUei() { return m_eventUei; }
public void setEventUei(String eventUei) { m_eventUei = eventUei; }

@Column(name="event_source", length=256)
public String getEventSource() { return m_eventSource; }
public void setEventSource(String eventSource) { m_eventSource = eventSource; }

@Column(name="event_severity")
public Integer getEventSeverity() { return m_eventSeverity; }
public void setEventSeverity(Integer eventSeverity) { m_eventSeverity = eventSeverity; }

@Column(name="event_timestamp")
@Temporal(TemporalType.TIMESTAMP)
public Date getEventTimestamp() { return m_eventTimestamp; }
public void setEventTimestamp(Date eventTimestamp) { m_eventTimestamp = eventTimestamp; }

@Column(name="event_node_id")
public Long getEventNodeId() { return m_eventNodeId; }
public void setEventNodeId(Long eventNodeId) { m_eventNodeId = eventNodeId; }

@Column(name="event_log_msg")
public String getEventLogMsg() { return m_eventLogMsg; }
public void setEventLogMsg(String eventLogMsg) { m_eventLogMsg = eventLogMsg; }

@Column(name="last_event_data", columnDefinition="TEXT")
public String getLastEventData() { return m_lastEventData; }
public void setLastEventData(String lastEventData) { m_lastEventData = lastEventData; }
```

Remove or deprecate:
```java
// Remove: @OneToOne @JoinColumn(name="lastEventId")
// Remove: getLastEvent() / setLastEvent(OnmsEvent)
// Remove: m_lastEvent field
```

**Step 3: Run existing alarm tests to check for breakage**

Run: `./compile.pl --projects :opennms-alarms-daemon -am verify`
Expected: Some tests will fail due to the removed `lastEvent` field. Note which tests fail.

**Step 4: Fix broken tests**

Update test code that references `alarm.getLastEvent()` to use the new denormalized fields instead.

**Step 5: Commit**

```bash
git add opennms-model/src/main/java/org/opennms/netmgt/model/OnmsAlarm.java
git commit -m "feat: add denormalized event columns to OnmsAlarm, remove lastEvent FK"
```

---

### Task 8: Modify AlarmPersisterImpl — Remove EventDao Dependency

**Files:**
- Modify: `opennms-alarms/daemon/src/main/java/org/opennms/netmgt/alarmd/AlarmPersisterImpl.java`
- Modify: `opennms-alarms/daemon/src/test/java/org/opennms/netmgt/alarmd/AlarmdIT.java` (fix tests)

**Step 1: Read AlarmPersisterImpl fully**

Understand the `addOrReduceEventAsAlarm()` method flow, especially:
- Line 132: `final OnmsEvent persistedEvent = m_eventDao.get(event.getDbid());`
- `createNewAlarm(persistedEvent, event)`
- `reduceEvent(persistedEvent, alarm, event)`

**Step 2: Remove EventDao dependency, populate alarm from in-memory Event**

Replace the `addOrReduceEventAsAlarm()` method's event retrieval:

```java
// OLD:
// final OnmsEvent persistedEvent = m_eventDao.get(event.getDbid());

// NEW: Build alarm fields directly from the in-memory Event
```

In `createNewAlarm()` and `reduceEvent()`, replace all `persistedEvent` references:

```java
private void populateAlarmFromEvent(OnmsAlarm alarm, Event event) {
    alarm.setEventTsid(event.getDbid());
    alarm.setEventUei(event.getUei());
    alarm.setEventSource(event.getSource());
    if (event.getSeverity() != null) {
        alarm.setEventSeverity(OnmsSeverity.get(event.getSeverity()).getId());
    }
    alarm.setEventTimestamp(event.getTime());
    alarm.setEventNodeId(event.getNodeid());
    if (event.getLogmsg() != null) {
        alarm.setEventLogMsg(event.getLogmsg().getContent());
    }
    alarm.setLastEventData(serializeEventToJson(event));
    alarm.setLastEventTime(event.getTime());
}

private String serializeEventToJson(Event event) {
    try {
        // Use Jackson ObjectMapper to serialize Event to JSON
        return objectMapper.writeValueAsString(event);
    } catch (Exception e) {
        LOG.warn("Failed to serialize event to JSON", e);
        return null;
    }
}
```

Remove `@Autowired private EventDao m_eventDao;`

**Step 3: Run alarm tests**

Run: `./compile.pl --projects :opennms-alarms-daemon -am verify`
Expected: Tests may fail due to EventDao removal. Fix tests to not mock EventDao.

**Step 4: Fix AlarmdIT and other alarm tests**

Remove all `eventDao.get()` mocking/setup from test code. Update assertions that check `alarm.getLastEvent()` to check the new denormalized fields instead.

**Step 5: Commit**

```bash
git add opennms-alarms/daemon/src/main/java/org/opennms/netmgt/alarmd/AlarmPersisterImpl.java \
      opennms-alarms/daemon/src/test/
git commit -m "feat: decouple AlarmPersisterImpl from EventDao, populate alarm from in-memory Event"
```

---

## Phase C: Alarmd Extraction

### Task 9: Create KafkaFaultEventConsumer

**Files:**
- Create: `features/events/kafka-consumer/pom.xml`
- Create: `features/events/kafka-consumer/src/main/java/org/opennms/features/events/kafka/consumer/KafkaFaultEventConsumer.java`
- Create: `features/events/kafka-consumer/src/test/java/org/opennms/features/events/kafka/consumer/KafkaFaultEventConsumerTest.java`
- Modify: `features/events/pom.xml` — add `<module>kafka-consumer</module>`

**Step 1: Create module POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.opennms.features.events</groupId>
        <artifactId>org.opennms.features.events</artifactId>
        <version>36.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>org.opennms.features.events.kafka-consumer</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Features :: Events :: Kafka Consumer</name>
    <dependencies>
        <dependency>
            <groupId>org.opennms.features.events</groupId>
            <artifactId>org.opennms.features.events.api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>${kafkaVersion}</version>
        </dependency>
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
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 2: Implement KafkaFaultEventConsumer**

```java
package org.opennms.features.events.kafka.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaFaultEventConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFaultEventConsumer.class);

    private final KafkaConsumer<Long, byte[]> consumer;
    private final String topicName;
    private final List<EventListener> listeners;
    private final EventDeserializer deserializer;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Duration pollTimeout;

    public KafkaFaultEventConsumer(KafkaConsumer<Long, byte[]> consumer,
                                    String topicName,
                                    List<EventListener> listeners,
                                    EventDeserializer deserializer,
                                    Duration pollTimeout) {
        this.consumer = consumer;
        this.topicName = topicName;
        this.listeners = listeners;
        this.deserializer = deserializer;
        this.pollTimeout = pollTimeout;
    }

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList(topicName));
        LOG.info("KafkaFaultEventConsumer started, subscribed to topic: {}", topicName);

        while (running.get()) {
            try {
                ConsumerRecords<Long, byte[]> records = consumer.poll(pollTimeout);
                for (ConsumerRecord<Long, byte[]> record : records) {
                    try {
                        Event event = deserializer.deserialize(record.value());
                        for (EventListener listener : listeners) {
                            listener.onEvent(event);
                        }
                    } catch (Exception e) {
                        LOG.error("Failed to process event from Kafka partition={} offset={}",
                                record.partition(), record.offset(), e);
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    LOG.error("Error polling Kafka", e);
                }
            }
        }

        consumer.close();
        LOG.info("KafkaFaultEventConsumer stopped");
    }

    public void stop() {
        running.set(false);
        consumer.wakeup();
    }
}
```

**Step 3: Write EventDeserializer interface**

```java
package org.opennms.features.events.kafka.consumer;

import org.opennms.netmgt.xml.event.Event;

public interface EventDeserializer {
    Event deserialize(byte[] data);
}
```

**Step 4: Write test**

```java
package org.opennms.features.events.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.xml.event.Event;

public class KafkaFaultEventConsumerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDeserializeAndDispatchToListeners() throws Exception {
        KafkaConsumer<Long, byte[]> mockConsumer = mock(KafkaConsumer.class);
        Event expectedEvent = new Event();
        expectedEvent.setUei("uei.opennms.org/nodes/nodeDown");
        expectedEvent.setDbid(12345L);

        EventDeserializer deserializer = data -> expectedEvent;

        CountDownLatch latch = new CountDownLatch(1);
        EventListener listener = new EventListener() {
            @Override
            public String getName() { return "test"; }
            @Override
            public void onEvent(org.opennms.netmgt.events.api.model.IEvent e) {
                assertThat(e.getUei()).isEqualTo("uei.opennms.org/nodes/nodeDown");
                latch.countDown();
            }
        };

        TopicPartition tp = new TopicPartition("opennms-fault-events", 0);
        ConsumerRecord<Long, byte[]> record = new ConsumerRecord<>(
                "opennms-fault-events", 0, 0L, 42L, new byte[]{1, 2, 3});
        Map<TopicPartition, List<ConsumerRecord<Long, byte[]>>> recordMap = new HashMap<>();
        recordMap.put(tp, Collections.singletonList(record));
        ConsumerRecords<Long, byte[]> records = new ConsumerRecords<>(recordMap);

        // First poll returns records, second poll triggers stop
        when(mockConsumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenAnswer(inv -> {
                    Thread.sleep(50);
                    return ConsumerRecords.empty();
                });

        KafkaFaultEventConsumer consumer = new KafkaFaultEventConsumer(
                mockConsumer, "opennms-fault-events",
                Collections.singletonList(listener), deserializer,
                Duration.ofMillis(100));

        Thread thread = new Thread(consumer);
        thread.start();
        boolean dispatched = latch.await(5, TimeUnit.SECONDS);
        consumer.stop();
        thread.join(2000);

        assertThat(dispatched).isTrue();
    }
}
```

Note: The test for `EventListener.onEvent(IEvent)` may need adjustment based on the exact IEvent interface. The consumer should wrap Event into an ImmutableEvent.

**Step 5: Run test**

Run: `./compile.pl --projects :org.opennms.features.events.kafka-consumer -am verify`
Expected: PASS

**Step 6: Commit**

```bash
git add features/events/kafka-consumer/ features/events/pom.xml
git commit -m "feat: add KafkaFaultEventConsumer for Alarmd microservice extraction"
```

---

### Task 10: Wire New Processing Pipeline in Spring Context

**Files:**
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml`

**Step 1: Read the current Spring context**

Read the full `applicationContext-eventDaemon.xml` to understand all existing bean definitions.

**Step 2: Replace the processor chain**

Change the `eventProcessors` list from:
```xml
<property name="eventProcessors">
    <list>
        <ref bean="eventExpander"/>
        <ref bean="eventWriter"/>
        <ref bean="eventIpcBroadcastProcessor"/>
    </list>
</property>
```

To:
```xml
<property name="eventProcessors">
    <list>
        <ref bean="eventExpander"/>
        <ref bean="tsidAssigner"/>
        <ref bean="eventRouter"/>
    </list>
</property>
```

Add new bean definitions:
```xml
<bean id="tsidFactory" class="org.opennms.core.tsid.TsidFactory">
    <constructor-arg value="${org.opennms.tsid.node-id:0}"/>
</bean>

<bean id="tsidAssigner" class="org.opennms.netmgt.eventd.processor.TsidAssigner">
    <constructor-arg ref="tsidFactory"/>
</bean>

<bean id="eventClassifier" class="org.opennms.netmgt.eventd.router.EventClassifier"/>

<bean id="ipcMessageConverter" class="org.opennms.netmgt.eventd.router.IpcMessageConverter"/>

<bean id="eventRouter" class="org.opennms.netmgt.eventd.router.EventRouter">
    <constructor-arg ref="eventClassifier"/>
    <constructor-arg ref="faultEventPublisher"/>
    <constructor-arg ref="messageBus"/>
    <constructor-arg ref="eventIpcManagerImpl"/>
    <constructor-arg ref="ipcMessageConverter"/>
</bean>
```

The `faultEventPublisher` and `messageBus` beans are defined conditionally based on transport mode — this will require a property-based bean selection or a Spring profile. For Phase 1, define a local-mode MessageBus (no-op or in-memory):

```xml
<!-- Local-mode MessageBus: broadcasts IPC messages in-JVM -->
<bean id="messageBus" class="org.opennms.core.messagebus.local.LocalMessageBus"/>

<!-- Fault event publisher: Kafka or local mode -->
<bean id="faultEventPublisher"
      class="org.opennms.netmgt.eventd.processor.FaultEventPublisher">
    <!-- Kafka producer bean configured separately -->
</bean>
```

**Step 3: Run Eventd tests**

Run: `./compile.pl --projects :org.opennms.features.events.daemon -am verify`
Expected: Some tests may fail if they expect the old processor chain. Fix accordingly.

**Step 4: Commit**

```bash
git add features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml
git commit -m "feat: wire new EventRouter processing pipeline replacing HibernateEventWriter"
```

---

## Phase D: JMS MessageBus Implementation

### Task 11: Create core/messagebus-jms Module — JmsMessageBus

**Files:**
- Create: `core/messagebus-jms/pom.xml`
- Create: `core/messagebus-jms/src/main/java/org/opennms/core/messagebus/jms/JmsMessageBus.java`
- Create: `core/messagebus-api/src/main/java/org/opennms/core/messagebus/local/LocalMessageBus.java`
- Create: `core/messagebus-jms/src/test/java/org/opennms/core/messagebus/jms/JmsMessageBusTest.java`
- Modify: `core/pom.xml` — add `<module>messagebus-jms</module>`

**Step 1: Create LocalMessageBus in messagebus-api (in-JVM fallback)**

```java
package org.opennms.core.messagebus.local;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.core.messagebus.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalMessageBus implements MessageBus {

    private static final Logger LOG = LoggerFactory.getLogger(LocalMessageBus.class);

    private final Map<String, List<MessageHandler>> handlersByType = new ConcurrentHashMap<>();

    @Override
    public void publish(IpcMessage message) {
        List<MessageHandler> handlers = handlersByType.get(message.getType());
        if (handlers != null) {
            for (MessageHandler handler : handlers) {
                try {
                    handler.onMessage(message);
                } catch (Exception e) {
                    LOG.warn("Handler {} failed processing message type {}",
                            handler.getName(), message.getType(), e);
                }
            }
        }
    }

    @Override
    public void subscribe(String messageType, MessageHandler handler) {
        handlersByType.computeIfAbsent(messageType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    @Override
    public void subscribe(Collection<String> messageTypes, MessageHandler handler) {
        for (String type : messageTypes) {
            subscribe(type, handler);
        }
    }

    @Override
    public void unsubscribe(MessageHandler handler) {
        handlersByType.values().forEach(list -> list.remove(handler));
    }
}
```

**Step 2: Create JmsMessageBus module POM and implementation**

The JMS implementation wraps ActiveMQ JMS Topics. Each message type maps to a JMS topic based on the topic design in the design doc. The `JmsMessageBus` uses a `ConnectionFactory` (from the embedded ActiveMQ broker) and creates JMS producers/consumers.

This is a larger implementation that depends on the ActiveMQ broker being available. Write the implementation following the same pattern as the existing ActiveMQ code in `features/activemq/`.

**Step 3: Write tests using embedded ActiveMQ**

Test with an in-memory ActiveMQ broker for integration testing.

**Step 4: Commit**

```bash
git add core/messagebus-api/src/main/java/org/opennms/core/messagebus/local/ \
      core/messagebus-jms/ core/pom.xml
git commit -m "feat: add LocalMessageBus and JmsMessageBus implementations"
```

---

### Task 12: Migrate First Daemon to MessageBus (Proof of Concept — Discovery)

**Files:**
- Modify: `features/discovery/src/main/java/org/opennms/netmgt/discovery/Discovery.java`

**Step 1: Read Discovery daemon to find internal event usage**

Find all `addEventListener` calls and `sendNow` calls with internal UEIs.

**Step 2: Replace event listener registrations with MessageBus subscriptions**

Replace:
```java
getEventManager().addEventListener(this, EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI);
```
With:
```java
messageBus.subscribe("discoveryConfigChange", this::onConfigChanged);
```

Replace event sending:
```java
eventForwarder.sendNow(new EventBuilder(EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI, ...).getEvent());
```
With:
```java
messageBus.publish(new IpcMessage("discoveryConfigChange", "discovery"));
```

**Step 3: Inject MessageBus into Discovery**

Add `MessageBus` as a dependency in Discovery's Spring context or constructor.

**Step 4: Run Discovery tests**

Run: `./compile.pl --projects :org.opennms.features.discovery -am verify`
Expected: PASS after fixing test mocks to provide MessageBus.

**Step 5: Commit**

```bash
git add features/discovery/
git commit -m "feat: migrate Discovery daemon from EventIpcManager to MessageBus for IPC"
```

---

## Task Dependency Summary

```
Task 1: TsidFactory (standalone)
Task 2: MessageBus API (standalone)
    |
    +---> Task 3: TsidAssigner (depends on Task 1)
    +---> Task 4: FaultEventPublisher (standalone)
    +---> Task 5: EventRouter (depends on Tasks 3, 4, 2)
    |
    +---> Task 6: Liquibase migration (standalone)
    +---> Task 7: OnmsAlarm entity (depends on Task 6)
    +---> Task 8: AlarmPersisterImpl (depends on Task 7)
    |
    +---> Task 9: KafkaFaultEventConsumer (depends on Tasks 4, 8)
    +---> Task 10: Spring wiring (depends on Tasks 3, 5)
    |
    +---> Task 11: JmsMessageBus (depends on Task 2)
    +---> Task 12: Discovery migration (depends on Task 11)
```

**Critical path:** Tasks 1 → 3 → 5 → 10 (enables the new event pipeline)
**Parallel path:** Tasks 6 → 7 → 8 (alarm schema changes, can run in parallel with Phase A)
**Integration:** Task 9 + 10 together enable Alarmd extraction

## Remaining Work (Not in This Plan)

These are follow-on tasks after the core architecture is proven:

1. **Migrate remaining ~14 daemons** from EventIpcManager to MessageBus for IPC events (mechanical, same pattern as Task 12 repeated per daemon: Pollerd, Collectd, Provisiond, Vacuumd, Notifd, Actiond, Scriptd, Rtcd, Enlinkd, EventTranslator, PassiveStatusd, Statsd, Telemetryd, Correlator)
2. **Integrate ProtobufMapper** into FaultEventPublisher for efficient Kafka serialization (replacing XML baseline)
3. **Update REST API** event endpoints to query alarm JSONB or Kafka-backed read model
4. **Update Vacuumd** SQL automations that reference the events table
5. **Update Jasper Reports** and database reports
6. **Update Grafana datasource** queries
7. **Karaf feature definitions** for new modules in `container/features/src/main/resources/features.xml`
8. **Container/Docker configuration** for Alarmd standalone container
9. **Network-of-brokers configuration** for ActiveMQ in microservice mode
10. **Smoke tests** validating Alarmd container extraction
