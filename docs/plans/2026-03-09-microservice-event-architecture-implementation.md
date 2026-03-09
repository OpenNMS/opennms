# Microservice Event Architecture Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Enable full cross-container event flow — IPC via AMQ hub-and-spoke, fault events via Kafka — so all 15 daemon containers can communicate.

**Architecture:** Two transport layers. IPC messages (internal UEIs) flow via JmsMessageBus → core's embedded AMQ broker. Fault events flow via Kafka. Core gets a Spring-wired Kafka producer/consumer (no Karaf needed). Daemon containers switch from LocalMessageBus to JmsMessageBus.

**Tech Stack:** ActiveMQ 5.16.x (embedded in core), Kafka, JMS Topics (`OpenNMS.IPC.*`), Spring XML contexts, OSGi Blueprint.

**Design doc:** `docs/plans/2026-03-09-microservice-event-architecture-design.md`

---

## Phase 1: IPC via AMQ (Cross-Container Daemon Communication)

### Background

Daemon containers currently use `LocalMessageBus` (in-JVM only). IPC events classified by `EventClassifier` as `IPC` or `DUAL` are published to MessageBus, but they never leave the container. Core also uses `LocalMessageBus` — same problem for events going outward.

The fix: replace `LocalMessageBus` with `JmsMessageBus` everywhere. `JmsMessageBus` (`core/messagebus-jms/`) maps message types to JMS Topics with prefix `OpenNMS.IPC.` and uses a `javax.jms.ConnectionFactory` for transport. Core already has an embedded AMQ broker on port 61616.

Key files to understand before starting:
- `core/messagebus-jms/src/main/java/org/opennms/core/messagebus/jms/JmsMessageBus.java` — constructor takes `ConnectionFactory`, has `start()`/`stop()` lifecycle
- `core/event-forwarder-kafka/src/main/resources/OSGI-INF/blueprint/blueprint-event-forwarder-kafka.xml:27-29` — already has `<reference id="messageBus" ... availability="optional"/>`
- `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarder.java:101-102` — `setMessageBus()` setter, null-safe at line 186
- `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml:109` — core's `LocalMessageBus` bean
- `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/bridge/MessageBusEventListenerBridge.java` — bridges MessageBus → `@EventHandler` annotations
- `container/features/src/main/resources/features.xml:2078-2082` — `opennms-core-messagebus-jms` feature (depends on `activemq-client`)

---

### Task 1: Create JmsMessageBus Blueprint for Daemon Containers

Daemon containers need a Blueprint that creates `JmsMessageBus` with an `ActiveMQConnectionFactory` pointing at core's broker, and registers it as an OSGi service so the existing `event-forwarder-kafka` blueprint picks it up.

**Files:**
- Create: `core/messagebus-jms/src/main/resources/OSGI-INF/blueprint/blueprint-messagebus-jms.xml`
- Modify: `container/features/src/main/resources/features.xml` (~line 2078)

**Step 1: Create the Blueprint**

```xml
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0"
           xmlns:cm="http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.3.0">

    <!-- Configuration from org.opennms.core.messagebus.jms.cfg -->
    <cm:property-placeholder id="messageBusJmsProperties"
                             persistent-id="org.opennms.core.messagebus.jms"
                             update-strategy="reload">
        <cm:default-properties>
            <cm:property name="broker.url" value="tcp://127.0.0.1:61616"/>
        </cm:default-properties>
    </cm:property-placeholder>

    <!-- ActiveMQ ConnectionFactory pointing at core's broker -->
    <bean id="jmsConnectionFactory"
          class="org.apache.activemq.ActiveMQConnectionFactory">
        <property name="brokerURL" value="${broker.url}"/>
    </bean>

    <!-- JmsMessageBus with lifecycle management -->
    <bean id="jmsMessageBus"
          class="org.opennms.core.messagebus.jms.JmsMessageBus"
          init-method="start"
          destroy-method="stop">
        <argument ref="jmsConnectionFactory"/>
    </bean>

    <!-- Register as OSGi service — picked up by event-forwarder-kafka blueprint -->
    <service ref="jmsMessageBus"
             interface="org.opennms.core.messagebus.MessageBus"/>

</blueprint>
```

Create file at: `core/messagebus-jms/src/main/resources/OSGI-INF/blueprint/blueprint-messagebus-jms.xml`

**Step 2: Verify messagebus-jms POM has correct OSGi metadata**

Read `core/messagebus-jms/pom.xml` and verify it has:
- `<packaging>bundle</packaging>`
- `maven-bundle-plugin` configuration
- The blueprint will be auto-discovered from `OSGI-INF/blueprint/` directory

**Step 3: Add `opennms-core-messagebus-jms` to `opennms-event-forwarder-kafka` feature**

Modify `container/features/src/main/resources/features.xml`. Find the `opennms-event-forwarder-kafka` feature (around line 2089) and add the JMS messagebus feature:

```xml
<feature name="opennms-event-forwarder-kafka" version="${project.version}"
         description="OpenNMS :: Event Forwarder :: Kafka">
    <feature>opennms-events-api</feature>
    <feature>opennms-events-kafka-consumer</feature>
    <feature>opennms-events-sink-dispatcher</feature>
    <feature>opennms-core-messagebus-api</feature>
    <feature>opennms-core-messagebus-jms</feature>   <!-- ADD THIS LINE -->
    <feature>opennms-core-tsid</feature>
    <feature>opennms-kafka</feature>
    <feature>dropwizard-metrics</feature>
    <bundle>mvn:org.opennms.features.events/org.opennms.features.events.daemon/${project.version}</bundle>
    <bundle>mvn:org.opennms/org.opennms.core.event-forwarder-kafka/${project.version}</bundle>
</feature>
```

This means ALL 12 daemon features (pollerd, collectd, alarmd, etc.) automatically get `JmsMessageBus` because they all depend on `opennms-event-forwarder-kafka`.

**Step 4: Compile to verify**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.messagebus.jms -am install
```

Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add core/messagebus-jms/src/main/resources/OSGI-INF/blueprint/blueprint-messagebus-jms.xml
git add container/features/src/main/resources/features.xml
git commit -m "feat: add JmsMessageBus blueprint for daemon containers

Creates ActiveMQConnectionFactory from broker.url config property and
registers JmsMessageBus as OSGi service. Added opennms-core-messagebus-jms
to opennms-event-forwarder-kafka feature so all daemon containers get it."
```

---

### Task 2: Add AMQ Broker URL Config to Daemon Container Overlays

Each daemon container needs a config file that tells JmsMessageBus where core's AMQ broker is.

**Files:**
- Create: `opennms-container/delta-v/shared-daemon-overlay/etc/org.opennms.core.messagebus.jms.cfg`
- Modify: `opennms-container/delta-v/docker-compose.yml` — add `OPENNMS_BROKER_URL` env var and shared overlay volume to all 12 daemon containers

**Step 1: Create the shared cfg file**

```properties
# JmsMessageBus configuration — connects to core's embedded AMQ broker
broker.url = failover:tcp://core:61616
```

Create at: `opennms-container/delta-v/shared-daemon-overlay/etc/org.opennms.core.messagebus.jms.cfg`

**Step 2: Mount the shared overlay in docker-compose.yml**

For each of the 12 daemon containers (pollerd, collectd, rtcd, passivestatusd, notifd, discovery, trapd, syslogd, ticketer, eventtranslator, enlinkd, scriptd), add the shared overlay volume mount.

Example for pollerd (apply same pattern to all 12):

```yaml
  pollerd:
    volumes:
      - pollerd-data:/opt/sentinel/data
      - ./pollerd-daemon-overlay/etc:/opt/sentinel-etc-overlay:ro
      - ./shared-daemon-overlay/etc/org.opennms.core.messagebus.jms.cfg:/opt/sentinel-etc-overlay/org.opennms.core.messagebus.jms.cfg:ro
```

**Important:** The shared cfg file must be mounted as a single file into the overlay directory, not as a directory mount (which would shadow the per-daemon overlay).

Alternatively, copy the cfg file into each daemon's overlay directory. This is simpler and avoids mount ordering issues:

```bash
# Copy to each daemon overlay
for dir in pollerd-daemon-overlay collectd-daemon-overlay rtcd-overlay \
           passivestatusd-overlay notifd-overlay discovery-overlay \
           trapd-overlay syslogd-overlay ticketer-overlay \
           eventtranslator-overlay enlinkd-overlay scriptd-overlay; do
  cp shared-daemon-overlay/etc/org.opennms.core.messagebus.jms.cfg \
     "opennms-container/delta-v/${dir}/etc/org.opennms.core.messagebus.jms.cfg"
done
```

**Step 3: Also add to alarmd overlay**

Alarmd uses a different image (`opennms/alarmd`) but needs the same config:

```bash
cp shared-daemon-overlay/etc/org.opennms.core.messagebus.jms.cfg \
   opennms-container/delta-v/alarmd-overlay/etc/org.opennms.core.messagebus.jms.cfg
```

**Step 4: Commit**

```bash
git add opennms-container/delta-v/shared-daemon-overlay/
git add opennms-container/delta-v/*/etc/org.opennms.core.messagebus.jms.cfg
git commit -m "feat: add AMQ broker URL config to all daemon container overlays

Each daemon container gets org.opennms.core.messagebus.jms.cfg with
broker.url = failover:tcp://core:61616, connecting JmsMessageBus to
core's embedded AMQ broker (hub-and-spoke topology)."
```

---

### Task 3: Switch Core from LocalMessageBus to JmsMessageBus

Core uses `LocalMessageBus` in `applicationContext-eventDaemon.xml`. Core's embedded AMQ broker runs on `vm://localhost`. We switch to `JmsMessageBus` so IPC events published by core's `EventRouter` flow to AMQ Topics and reach daemon containers.

**Files:**
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml:109`

**Step 1: Read the current wiring**

Read `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml` to understand the current `messageBus` bean and its dependents.

Current (line 109):
```xml
<bean id="messageBus" class="org.opennms.core.messagebus.local.LocalMessageBus"/>
```

Core already has `jmsConnectionFactory` in `core/daemon/src/main/resources/META-INF/opennms/applicationContext-daemon.xml` (loaded earlier in context hierarchy):
```xml
<bean id="jmsConnectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
    <property name="brokerURL" value="vm://localhost?create=false&amp;jms.useAsyncSend=true"/>
</bean>
```

**Step 2: Replace LocalMessageBus with JmsMessageBus**

Change line 109 of `applicationContext-eventDaemon.xml`:

Before:
```xml
<!-- Local-mode MessageBus: in-JVM pub/sub for IPC events -->
<bean id="messageBus" class="org.opennms.core.messagebus.local.LocalMessageBus"/>
```

After:
```xml
<!-- JMS-backed MessageBus: publishes IPC events to AMQ Topics (OpenNMS.IPC.*)
     so daemon containers connected to core's broker receive them.
     Uses jmsConnectionFactory from applicationContext-daemon.xml (vm://localhost). -->
<bean id="messageBus" class="org.opennms.core.messagebus.jms.JmsMessageBus"
      init-method="start" destroy-method="stop">
    <constructor-arg ref="jmsConnectionFactory"/>
</bean>
```

Everything else stays the same — the `onmsgi:service` registration (line 111), `MessageBusFactory` setup (lines 113-120), and `EventRouter` constructor arg (line 129) all reference `messageBus` by ID.

**Step 3: Add messagebus-jms dependency to events-daemon POM**

Read `features/events/daemon/pom.xml` and add:

```xml
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.messagebus.jms</artifactId>
    <version>${project.version}</version>
</dependency>
```

Also verify `activemq-client` is available (it should be — core already uses ActiveMQ).

**Step 4: Compile to verify**

```bash
./compile.pl -DskipTests --projects :org.opennms.features.events.daemon -am install
```

Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml
git add features/events/daemon/pom.xml
git commit -m "feat: switch core Eventd from LocalMessageBus to JmsMessageBus

Core's EventRouter now publishes IPC events to AMQ Topics via
JmsMessageBus, making them available to all daemon containers
connected to core's embedded broker."
```

---

### Task 4: Wire MessageBusEventListenerBridge in Daemon Containers

Daemon containers need `MessageBusEventListenerBridge` to receive IPC events from AMQ and deliver them to `@EventHandler`-annotated methods in daemon beans.

The bridge scans a bean for `@EventHandler(uei="uei.opennms.org/internal/...")` annotations, subscribes to the corresponding MessageBus type, and invokes the annotated method when a message arrives.

Currently, no daemon-loader Spring context creates this bridge. Each daemon-loader that has `@EventHandler` methods for internal UEIs needs it.

**Files:**
- Modify: Each daemon-loader Spring context that needs IPC event reception. Start with the two critical ones for integration testing:
  - `core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-enlinkd.xml` (needs `nodeScanCompleted`)

**Step 1: Identify which daemons need the bridge**

Search for `@EventHandler` annotations with internal UEIs across daemon code. The key ones for Phase 1 testing:

- **Enlinkd** — needs `nodeScanCompleted` and `nodeAdded` (to discover topology for new nodes)

For Provisiond: it runs on core where EventRouter publishes IPC events to the local MessageBus and MessageBusEventListenerBridge is already wired. No changes needed for Provisiond.

**Step 2: Check if Enlinkd has @EventHandler annotations**

Read `features/enlinkd/daemon/src/main/java/org/opennms/netmgt/enlinkd/EnhancedLinkd.java` and search for `@EventHandler` or `addEventListener` to understand how Enlinkd subscribes to events.

Note: Enlinkd may use `EventIpcManager.addEventListener()` instead of `@EventHandler`. If so, the events flow through `KafkaEventSubscriptionService` (Kafka consumer) not MessageBus. In that case, `nodeScanCompleted` needs to be a FAULT or DUAL event (published to Kafka), not purely IPC.

**This step requires investigation.** Read the Enlinkd source to determine the subscription pattern before wiring the bridge.

**Step 3: For each daemon that uses @EventHandler for internal UEIs, add bridge bean**

In the daemon-loader Spring context, add:

```xml
<!-- Bridge MessageBus IPC events to @EventHandler methods on the daemon bean -->
<bean id="messageBusEventListenerBridge"
      class="org.opennms.netmgt.eventd.bridge.MessageBusEventListenerBridge">
    <constructor-arg>
        <osgi:reference interface="org.opennms.core.messagebus.MessageBus"/>
    </constructor-arg>
</bean>

<bean class="org.springframework.beans.factory.config.MethodInvokingBean">
    <property name="targetObject" ref="messageBusEventListenerBridge"/>
    <property name="targetMethod" value="register"/>
    <property name="arguments">
        <list><ref bean="daemon"/></list>
    </property>
</bean>
```

**Step 4: Compile and test**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-enlinkd -am install
```

**Step 5: Commit**

```bash
git add core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-enlinkd.xml
git commit -m "feat: wire MessageBusEventListenerBridge in Enlinkd daemon container

Enlinkd can now receive IPC events (nodeScanCompleted, nodeAdded) via
AMQ MessageBus from core's Provisiond."
```

---

### Task 5: Integration Test — Discovery → Provisiond → Enlinkd via AMQ

Verify the full IPC flow works cross-container.

**Prerequisites:**
- Rebuild daemon image: `cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install`
- Rebuild container/features: `./compile.pl -DskipTests -pl container/features install`
- Rebuild Docker image
- Delete old Docker volumes: `docker volume rm delta-v_discovery-data delta-v_enlinkd-data`

**Step 1: Revert Discovery to standalone container**

In `docker-compose.yml`, change core's Discovery back to disabled:
```yaml
CORE_SERVICE_DISCOVERY_ENABLED: "false"
```

**Step 2: Start the test stack**

```bash
cd opennms-container/delta-v
docker compose up -d postgres kafka core discovery enlinkd
```

**Step 3: Verify AMQ connectivity**

Check daemon container logs for JmsMessageBus:
```bash
docker compose logs discovery 2>&1 | grep -i "JmsMessageBus"
docker compose logs enlinkd 2>&1 | grep -i "JmsMessageBus"
```

Expected: `JmsMessageBus started` in both containers.

**Step 4: Verify Discovery → newSuspect → AMQ → Provisiond**

Wait for Discovery's initial sleep (10 seconds), then:
```bash
# Check Discovery published newSuspect to MessageBus
docker compose logs discovery 2>&1 | grep -i "newSuspect"

# Check core received newSuspect via AMQ and Provisiond processed it
docker compose logs core 2>&1 | grep -i "newSuspect\|NewSuspectScan"
```

Expected: Provisiond on core creates a node for `host.docker.internal`.

**Step 5: Verify node created in PostgreSQL**

```bash
docker compose exec postgres psql -U opennms -c \
  "SELECT nodeid, nodelabel, foreignsource FROM node;"
```

Expected: Row with `foreignsource=delta-v-hosts`.

**Step 6: Verify nodeScanCompleted → AMQ → Enlinkd**

```bash
docker compose logs enlinkd 2>&1 | grep -i "nodeScanCompleted\|scheduling\|topology"
```

Expected: Enlinkd receives the event and begins topology discovery for the new node.

**Step 7: Commit the Discovery revert**

```bash
git add opennms-container/delta-v/docker-compose.yml
git commit -m "fix: revert Discovery to standalone container (AMQ IPC now works)"
```

---

## Phase 2: Kafka on Core (Fault Event Flow)

### Background

Core uses `LocalFaultEventPublisher` which logs events instead of sending to Kafka. Daemon containers publish fault events to Kafka but core never receives them. We need:
1. Replace `LocalFaultEventPublisher` with `FaultEventPublisher` (real Kafka producer)
2. Add a `KafkaFaultEventConsumer` that polls Kafka and broadcasts to local listeners

Key existing code:
- `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/FaultEventPublisher.java` — already exists, takes `KafkaProducer<Long, byte[]>`, topic name, and serializer function
- `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/LocalFaultEventPublisher.java` — current no-op placeholder
- `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml:124` — wires `LocalFaultEventPublisher`
- `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaProducerFactory.java` — creates `KafkaProducer<Long, byte[]>`

---

### Task 6: Wire FaultEventPublisher (Kafka Producer) on Core

Replace the `LocalFaultEventPublisher` with the real `FaultEventPublisher` in core's Eventd context.

**Files:**
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml:122-124`
- Modify: `features/events/daemon/pom.xml` (add kafka-clients dependency)

**Step 1: Read FaultEventPublisher constructor signature**

Already known from exploration:
```java
public FaultEventPublisher(KafkaProducer<Long, byte[]> producer,
                           String topicName,
                           Function<Event, byte[]> eventSerializer)
```

The `eventSerializer` converts Event → byte[]. In daemon containers, this uses `ProtobufMapper`. But core doesn't have the `kafka-producer` module dependency. We need a simpler serializer — XML serialization (same format used by `KafkaEventForwarder` in daemon containers).

**Step 2: Create XmlEventSerializer class**

Create a simple `Function<Event, byte[]>` that serializes events to XML (same format daemon containers use for Kafka):

```java
package org.opennms.netmgt.eventd.processor;

import java.io.StringWriter;
import java.util.function.Function;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.event.Event;

public class XmlEventSerializer implements Function<Event, byte[]> {
    @Override
    public byte[] apply(Event event) {
        return JaxbUtils.marshal(event).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
```

Create at: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/XmlEventSerializer.java`

**Step 3: Replace LocalFaultEventPublisher bean in applicationContext-eventDaemon.xml**

Before (lines 122-124):
```xml
<!-- Local-mode fault event publisher (logs instead of sending to Kafka). -->
<bean id="faultEventPublisher" class="org.opennms.netmgt.eventd.processor.LocalFaultEventPublisher"/>
```

After:
```xml
<!-- Kafka producer for fault events -->
<bean id="kafkaProducer"
      class="org.opennms.core.event.forwarder.kafka.KafkaProducerFactory"
      factory-method="create">
    <constructor-arg value="${org.opennms.kafka.bootstrap.servers:localhost:9092}"/>
</bean>

<!-- XML serializer for events on Kafka -->
<bean id="xmlEventSerializer"
      class="org.opennms.netmgt.eventd.processor.XmlEventSerializer"/>

<!-- Fault event publisher: sends events with alarm-data to Kafka -->
<bean id="faultEventPublisher"
      class="org.opennms.netmgt.eventd.processor.FaultEventPublisher">
    <constructor-arg ref="kafkaProducer"/>
    <constructor-arg value="${org.opennms.kafka.fault.topic:opennms-fault-events}"/>
    <constructor-arg ref="xmlEventSerializer"/>
</bean>
```

**Step 4: Add dependencies to events-daemon POM**

Add to `features/events/daemon/pom.xml`:

```xml
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.event-forwarder-kafka</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
</dependency>
```

**Step 5: Add Kafka system properties to core container env**

In `docker-compose.yml`, add to core's environment:
```yaml
JAVA_OPTS: >-
    ...existing opts...
    -Dorg.opennms.kafka.bootstrap.servers=kafka:9092
    -Dorg.opennms.kafka.fault.topic=opennms-fault-events
```

**Step 6: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.features.events.daemon -am install
```

**Step 7: Commit**

```bash
git add features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/XmlEventSerializer.java
git add features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml
git add features/events/daemon/pom.xml
git add opennms-container/delta-v/docker-compose.yml
git commit -m "feat: wire FaultEventPublisher (Kafka producer) on core

Core now publishes fault events (events with alarm-data) to Kafka topic
opennms-fault-events. Replaces LocalFaultEventPublisher no-op. Uses XML
serialization for event payloads."
```

---

### Task 7: Create KafkaFaultEventConsumer for Core

Core needs to consume fault events published by daemon containers (traps from Trapd, etc.) and broadcast them to local listeners.

**Files:**
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/consumer/KafkaFaultEventConsumer.java`
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml`

**Step 1: Write the consumer class**

```java
package org.opennms.netmgt.eventd.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Consumes fault events from Kafka and broadcasts them to core's local
 * EventIpcManager listeners. Skips events originated by core itself
 * (detected via TSID node-id prefix) to prevent echo loops.
 */
public class KafkaFaultEventConsumer implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaFaultEventConsumer.class);

    private final String bootstrapServers;
    private final String topicName;
    private final String groupId;
    private final int coreNodeId;
    private final EventIpcBroadcaster localBroadcaster;

    private KafkaConsumer<Long, byte[]> consumer;
    private volatile boolean running;
    private Thread pollThread;

    public KafkaFaultEventConsumer(String bootstrapServers,
                                   String topicName,
                                   String groupId,
                                   int coreNodeId,
                                   EventIpcBroadcaster localBroadcaster) {
        this.bootstrapServers = bootstrapServers;
        this.topicName = topicName;
        this.groupId = groupId;
        this.coreNodeId = coreNodeId;
        this.localBroadcaster = localBroadcaster;
    }

    @Override
    public void afterPropertiesSet() {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        consumer = new KafkaConsumer<>(props, new LongDeserializer(), new ByteArrayDeserializer());
        consumer.subscribe(Collections.singletonList(topicName));

        running = true;
        pollThread = new Thread(this::pollLoop, "kafka-fault-event-consumer");
        pollThread.setDaemon(true);
        pollThread.start();

        LOG.info("KafkaFaultEventConsumer started: topic={}, group={}", topicName, groupId);
    }

    @Override
    public void destroy() {
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
        if (pollThread != null) {
            try {
                pollThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (consumer != null) {
            consumer.close();
        }
        LOG.info("KafkaFaultEventConsumer stopped");
    }

    private void pollLoop() {
        while (running) {
            try {
                ConsumerRecords<Long, byte[]> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<Long, byte[]> record : records) {
                    processRecord(record);
                }
            } catch (RecordDeserializationException e) {
                LOG.warn("Skipping undeserializable record at offset {}", e.offset(), e);
                consumer.seek(e.topicPartition(), e.offset() + 1);
            } catch (org.apache.kafka.common.errors.WakeupException e) {
                if (running) {
                    LOG.warn("Unexpected wakeup", e);
                }
            } catch (Exception e) {
                LOG.error("Error in Kafka poll loop", e);
                try { Thread.sleep(1000); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processRecord(ConsumerRecord<Long, byte[]> record) {
        try {
            String xml = new String(record.value(), java.nio.charset.StandardCharsets.UTF_8);
            Event event = JaxbUtils.unmarshal(Event.class, xml);

            // Skip events originated by core to prevent echo
            if (isFromCore(event)) {
                LOG.trace("Skipping core-originated event: {}", event.getUei());
                return;
            }

            LOG.debug("Broadcasting Kafka fault event: uei={} dbid={}", event.getUei(), event.getDbid());
            localBroadcaster.broadcastNow(event, false);
        } catch (Exception e) {
            LOG.error("Failed to process Kafka record at offset {}", record.offset(), e);
        }
    }

    private boolean isFromCore(Event event) {
        // TSID high bits encode node-id. Core uses node-id=1.
        // TsidFactory: node-id occupies bits 52-61 (10 bits).
        if (event.getDbid() != null && event.getDbid() > 0) {
            long tsid = event.getDbid();
            int nodeId = (int) ((tsid >> 22) & 0x3FF); // 10-bit node-id field
            return nodeId == coreNodeId;
        }
        return false;
    }
}
```

Create at: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/consumer/KafkaFaultEventConsumer.java`

**Step 2: Wire in applicationContext-eventDaemon.xml**

Add after the `faultEventPublisher` bean (around line 135):

```xml
<!-- Kafka consumer: polls fault events from daemon containers, broadcasts locally.
     Skips events originated by core (TSID node-id=1) to prevent echo. -->
<bean id="kafkaFaultEventConsumer"
      class="org.opennms.netmgt.eventd.consumer.KafkaFaultEventConsumer">
    <constructor-arg value="${org.opennms.kafka.bootstrap.servers:localhost:9092}"/>
    <constructor-arg value="${org.opennms.kafka.fault.topic:opennms-fault-events}"/>
    <constructor-arg value="${org.opennms.kafka.consumer.group:opennms-core-consumer}"/>
    <constructor-arg value="${org.opennms.tsid.node-id:1}"/>
    <constructor-arg ref="eventIpcManagerImpl"/>
</bean>
```

Note: consumer group must differ from the producer's group. Using `opennms-core-consumer`.

**Step 3: Compile**

```bash
./compile.pl -DskipTests --projects :org.opennms.features.events.daemon -am install
```

**Step 4: Commit**

```bash
git add features/events/daemon/src/main/java/org/opennms/netmgt/eventd/consumer/KafkaFaultEventConsumer.java
git add features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml
git commit -m "feat: add KafkaFaultEventConsumer to core for cross-container fault events

Core now polls opennms-fault-events Kafka topic and broadcasts incoming
events to local listeners via EventIpcBroadcaster. Skips core-originated
events using TSID node-id to prevent echo loops."
```

---

### Task 8: Integration Test — Trapd → Kafka → Core + Alarmd

Verify fault events from daemon containers reach both core and Alarmd.

**Step 1: Start the test stack**

```bash
cd opennms-container/delta-v
docker compose up -d postgres kafka core trapd alarmd
```

**Step 2: Send a test trap**

```bash
docker compose exec kafka kafka-console-producer.sh \
  --broker-list localhost:9092 \
  --topic opennms-fault-events <<'EOF'
<event xmlns="http://xmlns.opennms.org/xsd/event">
  <uei>uei.opennms.org/generic/traps/SNMP_Cold_Start</uei>
  <source>trapd-test</source>
  <time>2026-03-09T12:00:00.000-07:00</time>
  <creation-time>2026-03-09T12:00:00.000-07:00</creation-time>
  <severity>Warning</severity>
  <alarm-data reduction-key="uei.opennms.org/generic/traps/SNMP_Cold_Start::1" alarm-type="1"/>
</event>
EOF
```

**Step 3: Verify core received the event**

```bash
docker compose logs core 2>&1 | grep -i "Broadcasting Kafka fault event\|SNMP_Cold_Start"
```

**Step 4: Verify Alarmd created an alarm**

```bash
docker compose exec postgres psql -U opennms -c \
  "SELECT alarmid, uei, severity FROM alarms;"
```

**Step 5: Commit test results/notes if any fixes needed**

---

## Phase 3: UEI Classification Audit

### Task 9: Catalog All Event UEIs and Classify

This is an investigation task. Identify all events that daemons subscribe to and classify each as FAULT, IPC, or DUAL.

**Step 1: Find all @EventHandler annotations**

```bash
grep -rn "@EventHandler" --include="*.java" features/ opennms-provision/ opennms-services/ \
  | grep -v "test/" | grep -v "Test.java"
```

**Step 2: Find all addEventListener calls**

```bash
grep -rn "addEventListener" --include="*.java" features/ opennms-provision/ opennms-services/ \
  | grep -v "test/" | grep -v "Test.java" | grep -v "removeEventListener"
```

**Step 3: For each UEI found, determine:**

1. Is it functionally an IPC message (lifecycle notification, configuration change) or a fault (something went wrong)?
2. Does it have alarm-data in eventconf?
3. Which daemons produce it and which consume it?
4. Does it need to cross container boundaries?

**Step 4: Update EventClassifier**

For UEIs that are functionally IPC but not in the `uei.opennms.org/internal/` namespace, add them to `CROSS_CONTAINER_INTERNAL_UEIS` in `EventClassifier.java` or create a new classification category.

Known candidates from design doc:
- `uei.opennms.org/nodes/nodeAdded` — IPC
- `uei.opennms.org/nodes/nodeGainedService` — DUAL
- Others TBD from audit

**Step 5: Commit**

```bash
git add features/events/daemon/src/main/java/org/opennms/netmgt/eventd/router/EventClassifier.java
git commit -m "feat: update EventClassifier with audited UEI classifications"
```

---

## Build and Deploy Checklist

After all tasks, full rebuild:

```bash
# 1. Rebuild modified modules
./compile.pl -DskipTests --projects \
  :org.opennms.core.messagebus.jms,\
  :org.opennms.features.events.daemon,\
  :org.opennms.core.event-forwarder-kafka \
  -am install

# 2. Rebuild container features
./compile.pl -DskipTests -pl container/features install

# 3. Rebuild sentinel assembly
./compile.pl -DskipTests -pl features/container/sentinel install

# 4. Rebuild daemon assembly
cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install && cd ../..

# 5. Rebuild Docker images
# (daemon image rebuild steps per existing process)

# 6. Delete old Docker volumes
docker compose -f opennms-container/delta-v/docker-compose.yml down -v

# 7. Start fresh
docker compose -f opennms-container/delta-v/docker-compose.yml up -d
```
