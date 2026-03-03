# EventBus Redesign Phase 2: Daemon Migration, Protobuf, Karaf Features, Docker

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Complete the EventBus redesign by migrating all daemons to the MessageBus, integrating Protobuf serialization, registering new Karaf features, and creating the Alarmd Docker container.

**Architecture:** Mechanical migration of daemon IPC from EventIpcManager to MessageBus, followed by infrastructure work to make Alarmd runnable as a standalone container.

**Tech Stack:** Java 17, ActiveMQ 5.16.8, Kafka 3.6.2, Protobuf 3.25.5, Karaf 4.3.10, Docker (UBI9 base)

**Prerequisite:** Phase 1 complete (Tasks 1-12 from `2026-03-02-eventbus-redesign-implementation.md`)

---

## Known Deviations (from execution)

### 1. DefaultPollContext — OUTAGE_CREATED/OUTAGE_RESOLVED not migrated
Pollerd's `DefaultPollContext` publishes `OUTAGE_CREATED` and `OUTAGE_RESOLVED` events that are consumed by a self-referential event correlation pattern for outage tracking. These events cannot simply move to the MessageBus without breaking outage state management. **Follow-up required:** Redesign the outage tracking correlation to work with MessageBus, or keep these two specific events on the EventIpcManager as a controlled exception.

### 2. PerspectiveServiceTracker — uses @EventListener annotations
The plan assumed `PerspectiveServiceTracker` uses direct `addEventListener()` calls, but it uses Spring `@EventListener` annotations instead. These are wired differently and cannot be mechanically replaced with `messageBus.subscribe()`. **Follow-up required:** Either migrate the `@EventListener` annotation handler to delegate to MessageBus, or create a MessageBus-to-Spring bridge that republishes IPC messages as Spring application events.

### 3. Vacuumd — JMX singleton prevents MessageBus injection
Vacuumd is wired via the legacy JMX singleton wrapper (`Vacuumd.getInstance()`) which has no Spring/OSGi context for dependency injection. The MessageBus field is null-guarded so Vacuumd falls back gracefully. **Follow-up required:** Either create a `MessageBusFactory` static accessor (similar to existing `EventIpcManagerFactory`), or refactor Vacuumd to be fully Spring-managed like the `SimpleSpringContextJmxServiceDaemon` pattern used by Alarmd and Bsmd.

### 4. NorthbounderManager dual-path pattern (Window C insight)
`NorthbounderManager.handleReloadEvent(IEvent)` was refactored to extract an `onReloadDaemonConfig(String)` method. Both the legacy IEvent path and the new MessageBus path share the same NBI reload logic without creating synthetic events. This is the recommended pattern for any component that must support both paths during transition.

### 5. Smoke test runs both core and standalone Alarmd simultaneously
The AlarmdExtractionIT smoke test intentionally runs both the core's built-in Alarmd and the standalone Alarmd container. This validates the Kafka pipeline works across process boundaries without requiring a core overlay to disable Alarmd. **Follow-up required:** Add a `service-configuration.xml` overlay to the core container that disables Alarmd (`enabled="false"`), proving full independence where only the standalone container handles alarm creation.

### 6. Alarmd forwards ALL reloadDaemonConfig to NorthbounderManager
Unlike other daemons that filter `reloadDaemonConfig` on their own name, Alarmd forwards all reload messages to `NorthbounderManager`, which matches against individual NBI names internally. Drools reload only triggers when `daemonName` is specifically `"alarmd"`. The MessageBus subscription in Alarmd must NOT filter on daemon name.

---

## Priority Order

1. **Task 1:** ProtobufMapper integration (unblocks efficient Kafka serialization)
2. **Tasks 2-5:** Daemon migrations — complex daemons first (Pollerd, Collectd, Provisiond, Vacuumd)
3. **Tasks 6-9:** Daemon migrations — simple reload-only daemons (batch, parallelizable)
4. **Task 10:** DaemonTools.handleReloadEvent migration (covers many daemons at once)
5. **Task 11:** Karaf feature definitions for new modules
6. **Task 12:** Alarmd Docker container
7. **Task 13:** Smoke test for Alarmd container extraction

---

## Task 1: Integrate ProtobufMapper into FaultEventPublisher

**Files:**
- Modify: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/FaultEventPublisher.java`
- Modify: `features/events/daemon/pom.xml` — add dependency on `org.opennms.features.kafka.producer`
- Modify: `features/events/daemon/src/test/java/org/opennms/netmgt/eventd/processor/FaultEventPublisherTest.java`

**Step 1: Add kafka producer dependency to features/events/daemon/pom.xml**

```xml
<dependency>
    <groupId>org.opennms.features.kafka</groupId>
    <artifactId>org.opennms.features.kafka.producer</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
```

**Step 2: Replace XML serialization with Protobuf in FaultEventPublisher**

Replace the `serializeEvent()` method:

```java
// OLD:
private byte[] serializeEvent(Event event) {
    try {
        return event.marshal().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
        throw new RuntimeException("Failed to serialize event: " + event.getUei(), e);
    }
}

// NEW:
private final ProtobufMapper protobufMapper;

// Constructor updated to accept ProtobufMapper
public FaultEventPublisher(KafkaProducer<Long, byte[]> producer,
                           String topicName,
                           ProtobufMapper protobufMapper) {
    this.producer = producer;
    this.topicName = topicName;
    this.protobufMapper = protobufMapper;
}

private byte[] serializeEvent(Event event) {
    return protobufMapper.toEvent(event).build().toByteArray();
}
```

**Step 3: Update test to use ProtobufMapper**

Mock the ProtobufMapper in tests. Verify that `toEvent()` is called and the result is serialized.

**Step 4: Run tests**

Run: `./compile.pl --projects :org.opennms.features.events.daemon -am verify -Dtest=FaultEventPublisherTest`
Expected: PASS

**Step 5: Commit**

```bash
git add features/events/daemon/
git commit -m "feat: integrate ProtobufMapper into FaultEventPublisher for efficient Kafka serialization"
```

---

## Task 2: Migrate Pollerd to MessageBus

Pollerd has the most complex IPC usage — it both produces and consumes custom internal events.

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/poller/PollerEventProcessor.java`
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/poller/DefaultPollContext.java`
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/perspectivepoller/PerspectiveServiceTracker.java`
- Modify: `opennms-services/src/main/resources/META-INF/opennms/applicationContext-pollerd.xml`
- Modify: `opennms-services/pom.xml` — add messagebus-api dependency

**Step 1: Add messagebus-api dependency to opennms-services/pom.xml**

```xml
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.messagebus.api</artifactId>
    <version>${project.version}</version>
</dependency>
```

**Step 2: Migrate PollerEventProcessor listeners**

Replace in PollerEventProcessor:
```java
// OLD:
addEventListener(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI);
addEventListener(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI);
addEventListener(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI);
addEventListener(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI);

// NEW:
messageBus.subscribe("poller/suspendPollingService", this::onSuspendPolling);
messageBus.subscribe("poller/resumePollingService", this::onResumePolling);
messageBus.subscribe("thresholdConfigChange", this::onThresholdConfigChange);
messageBus.subscribe("schedOutagesChanged", this::onSchedOutagesChanged);
```

**Step 3: Migrate DefaultPollContext publishers**

Replace in DefaultPollContext:
```java
// OLD:
sendEvent(createEvent(EventConstants.OUTAGE_CREATED_EVENT_UEI, ...));
sendEvent(createEvent(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, ...));

// NEW:
messageBus.publish(new IpcMessage("poller/outageCreated", "pollerd",
    Map.of("nodeId", String.valueOf(nodeId), "interface", ipAddr, "service", svcName)));
messageBus.publish(new IpcMessage("poller/outageResolved", "pollerd",
    Map.of("nodeId", String.valueOf(nodeId), "interface", ipAddr, "service", svcName)));
```

**Step 4: Migrate PerspectiveServiceTracker**

Same pattern — replace `addEventListener` for SUSPEND/RESUME with `messageBus.subscribe`.

**Step 5: Update Spring context**

Add `messageBus` bean reference to `applicationContext-pollerd.xml`.

**Step 6: Run Poller tests**

Run: `./compile.pl --projects :opennms-services -am verify -Dtest="*Poller*"`
Expected: Fix any test failures from changed dependencies.

**Step 7: Commit**

```bash
git add opennms-services/
git commit -m "feat: migrate Pollerd from EventIpcManager to MessageBus for IPC"
```

---

## Task 3: Migrate Collectd to MessageBus

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/collectd/Collectd.java`

**Step 1: Migrate listeners**

Replace:
```java
// OLD:
addEventListener(EventConstants.CONFIGURE_SNMP_EVENT_UEI);
addEventListener(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI);
addEventListener(EventConstants.RELOAD_DAEMON_CONFIG_UEI);
addEventListener(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI);

// NEW:
messageBus.subscribe("configureSNMP", this::onConfigureSnmp);
messageBus.subscribe("thresholdConfigChange", this::onThresholdConfigChange);
messageBus.subscribe("reloadDaemonConfig", this::onReloadDaemonConfig);
messageBus.subscribe("schedOutagesChanged", this::onSchedOutagesChanged);
```

**Step 2: Migrate reload success/failure publishers**

Replace `sendEvent` calls for RELOAD_DAEMON_CONFIG_SUCCESSFUL/FAILED with `messageBus.publish`.

**Step 3: Run tests**

Run: `./compile.pl --projects :opennms-services -am verify -Dtest="*Collectd*"`

**Step 4: Commit**

```bash
git add opennms-services/
git commit -m "feat: migrate Collectd from EventIpcManager to MessageBus for IPC"
```

---

## Task 4: Migrate Provisiond to MessageBus

**Files:**
- Modify: `opennms-provision/opennms-provisiond/src/main/java/org/opennms/netmgt/provision/service/Provisioner.java`
- Modify: `opennms-provision/opennms-provisiond/src/main/java/org/opennms/netmgt/provision/service/DefaultProvisionService.java`
- Modify: `opennms-provision/opennms-provisiond/pom.xml`

**Step 1: Add messagebus-api dependency**

**Step 2: Migrate Provisioner listeners**

Replace RELOAD_IMPORT listener with `messageBus.subscribe("importer/reloadImport", ...)`.

**Step 3: Migrate Provisioner publishers**

Replace:
```java
// IMPORT_STARTED, IMPORT_SUCCESSFUL, IMPORT_FAILED
// PROVISION_SCAN_COMPLETE, PROVISION_SCAN_ABORTED
```
With MessageBus publish calls using types: `importer/importStarted`, `importer/importSuccessful`, `importer/importFailed`, `provisiond/nodeScanCompleted`, `provisiond/nodeScanAborted`.

**Step 4: Migrate DefaultProvisionService publishers**

Replace SUSPEND/RESUME_POLLING_SERVICE event sends with MessageBus publish.

**Step 5: Run tests**

Run: `./compile.pl --projects :opennms-provisiond -am verify`

**Step 6: Commit**

```bash
git add opennms-provision/
git commit -m "feat: migrate Provisiond from EventIpcManager to MessageBus for IPC"
```

---

## Task 5: Migrate Vacuumd to MessageBus

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/vacuumd/Vacuumd.java`

**Step 1: Migrate listeners**

Replace RELOAD_VACUUMD_CONFIG and RELOAD_DAEMON_CONFIG listeners with MessageBus subscriptions.

**Step 2: Migrate publishers**

Replace RELOAD_DAEMON_CONFIG_SUCCESSFUL/FAILED sends with MessageBus publish.

**Step 3: Run tests**

Run: `./compile.pl --projects :opennms-services -am verify -Dtest="*Vacuumd*"`

**Step 4: Commit**

```bash
git add opennms-services/
git commit -m "feat: migrate Vacuumd from EventIpcManager to MessageBus for IPC"
```

---

## Task 6: Migrate Rtcd to MessageBus

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/rtc/DataSender.java`
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/rtc/BroadcastEventProcessor.java` (RTC's own event processor)
- Modify: `opennms-web-api/src/main/java/org/opennms/web/category/RTCPostSubscriber.java`

**Step 1: Migrate DataSender**

Replace RTC_SUBSCRIBE/UNSUBSCRIBE listeners and OUTAGE_CREATED/RESOLVED listeners with MessageBus subscriptions.

**Step 2: Migrate RTCPostSubscriber**

Replace RTC_SUBSCRIBE/UNSUBSCRIBE event sends from the web layer with MessageBus publish.

**Step 3: Run tests**

Run: `./compile.pl --projects :opennms-services -am verify -Dtest="*Rtc*,*RTC*"`

**Step 4: Commit**

```bash
git add opennms-services/ opennms-web-api/
git commit -m "feat: migrate Rtcd from EventIpcManager to MessageBus for IPC"
```

---

## Task 7: Migrate Enlinkd EventProcessor to MessageBus

**Files:**
- Modify: `features/enlinkd/daemon/src/main/java/org/opennms/netmgt/enlinkd/EventProcessor.java`

**Step 1: Migrate RELOAD_TOPOLOGY publisher**

Replace RELOAD_TOPOLOGY event send with `messageBus.publish(new IpcMessage("reloadTopology", "enlinkd"))`.

**Step 2: Commit**

```bash
git add features/enlinkd/
git commit -m "feat: migrate Enlinkd from EventIpcManager to MessageBus for IPC"
```

---

## Task 8: Migrate EventTranslator to MessageBus

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/translator/EventTranslator.java`

**Step 1: Migrate RELOAD_DAEMON_CONFIG listener and success/failure publishers**

Same mechanical pattern as other daemons.

**Step 2: Commit**

```bash
git add opennms-services/
git commit -m "feat: migrate EventTranslator from EventIpcManager to MessageBus for IPC"
```

---

## Task 9: Migrate Remaining Reload-Only Daemons (Batch)

These daemons only listen to RELOAD_DAEMON_CONFIG and publish success/failure. They all use `DaemonTools.handleReloadEvent()`. Can be migrated together.

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/statsd/Statsd.java`
- Modify: `features/telemetry/daemon/src/main/java/org/opennms/netmgt/telemetry/daemon/Telemetryd.java`
- Modify: `opennms-correlation/opennms-correlator/src/main/java/org/opennms/netmgt/correlation/Correlator.java`
- Modify: `features/bsm/daemon/src/main/java/org/opennms/netmgt/bsm/daemon/Bsmd.java`
- Modify: `features/ticketing/daemon/src/main/java/org/opennms/netmgt/ticketd/TroubleTicketer.java`
- Modify: `features/events/syslog/src/main/java/org/opennms/netmgt/syslogd/Syslogd.java`
- Modify: `opennms-alarms/daemon/src/main/java/org/opennms/netmgt/alarmd/Alarmd.java`

**Step 1: For each daemon, replace the reload listener**

Same pattern for all:
```java
// OLD (via addEventListener or DaemonTools):
addEventListener(EventConstants.RELOAD_DAEMON_CONFIG_UEI);

// NEW:
messageBus.subscribe("reloadDaemonConfig", msg -> {
    String daemonName = msg.getParameter("daemonName");
    if (getName().equals(daemonName)) {
        handleReload();
    }
});
```

**Step 2: Add messagebus-api dependency to each module's pom.xml where missing**

Modules that need the dependency added:
- `features/telemetry/daemon/pom.xml`
- `opennms-correlation/opennms-correlator/pom.xml`
- `features/bsm/daemon/pom.xml`
- `features/ticketing/daemon/pom.xml`
- `features/events/syslog/pom.xml`

**Step 3: Run tests for each**

Run per module or all at once if time allows.

**Step 4: Commit**

```bash
git add opennms-services/ features/ opennms-correlation/ opennms-alarms/
git commit -m "feat: migrate remaining daemons (Statsd, Telemetryd, Correlator, Bsmd, Ticketer, Syslogd, Alarmd) to MessageBus for IPC"
```

---

## Task 10: Migrate DaemonTools.handleReloadEvent to MessageBus

Many daemons delegate to `DaemonTools.handleReloadEvent()` for the reload pattern. Migrating this utility centralizes the change.

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/daemon/DaemonTools.java`

**Step 1: Read DaemonTools.handleReloadEvent()**

Understand the current implementation — it sends RELOAD_DAEMON_CONFIG_SUCCESSFUL or RELOAD_DAEMON_CONFIG_FAILED events.

**Step 2: Add MessageBus-based overload**

```java
public static void handleReloadEvent(MessageBus messageBus,
                                      String daemonName,
                                      Runnable reloadAction) {
    try {
        reloadAction.run();
        messageBus.publish(new IpcMessage("reloadDaemonConfigSuccessful", daemonName,
            Map.of("daemonName", daemonName)));
    } catch (Exception e) {
        messageBus.publish(new IpcMessage("reloadDaemonConfigFailed", daemonName,
            Map.of("daemonName", daemonName, "reason", e.getMessage())));
    }
}
```

**Step 3: Update callers to use the new overload**

Find all callers of `DaemonTools.handleReloadEvent` and switch them to the MessageBus variant.

**Step 4: Run tests**

Run: `./compile.pl --projects :opennms-services -am verify -Dtest="*DaemonTools*"`

**Step 5: Commit**

```bash
git add opennms-services/
git commit -m "feat: add MessageBus-based DaemonTools.handleReloadEvent"
```

---

## Task 11: Register New Modules in Karaf Features

**Files:**
- Modify: `container/features/src/main/resources/features.xml`
- Modify: `container/features/pom.xml` — add dependencies for new modules

**Step 1: Add opennms-core-tsid feature**

```xml
<feature name="opennms-core-tsid" version="${project.version}">
    <bundle>mvn:org.opennms/org.opennms.core.tsid/${project.version}</bundle>
</feature>
```

**Step 2: Add opennms-core-messagebus-api feature**

```xml
<feature name="opennms-core-messagebus-api" version="${project.version}">
    <bundle>mvn:org.opennms/org.opennms.core.messagebus.api/${project.version}</bundle>
</feature>
```

**Step 3: Add opennms-core-messagebus-jms feature**

```xml
<feature name="opennms-core-messagebus-jms" version="${project.version}">
    <feature>opennms-core-messagebus-api</feature>
    <feature>activemq-client</feature>
    <bundle>mvn:org.opennms/org.opennms.core.messagebus.jms/${project.version}</bundle>
</feature>
```

**Step 4: Add opennms-events-kafka-consumer feature**

```xml
<feature name="opennms-events-kafka-consumer" version="${project.version}">
    <feature>opennms-kafka</feature>
    <feature>opennms-events-api</feature>
    <bundle>mvn:org.opennms.features.events/org.opennms.features.events.kafka-consumer/${project.version}</bundle>
</feature>
```

**Step 5: Update opennms-events-api feature to depend on tsid**

Add `<feature>opennms-core-tsid</feature>` to the opennms-events-api feature.

**Step 6: Update features-core.xml or features.xml with messagebus dependency for all daemon features**

Add `<feature>opennms-core-messagebus-api</feature>` as a dependency of features that load daemons using the MessageBus.

**Step 7: Add Maven dependencies to container/features/pom.xml**

```xml
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.tsid</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.messagebus.api</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.messagebus.jms</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.opennms.features.events</groupId>
    <artifactId>org.opennms.features.events.kafka-consumer</artifactId>
    <version>${project.version}</version>
</dependency>
```

**Step 8: Commit**

```bash
git add container/features/
git commit -m "feat: register new EventBus modules in Karaf features.xml"
```

---

## Task 12: Create Alarmd Docker Container

**Files:**
- Create: `opennms-container/alarmd/Dockerfile`
- Create: `opennms-container/alarmd/Makefile`
- Create: `opennms-assemblies/alarmd/pom.xml`
- Modify: `opennms-assemblies/pom.xml` — add `<module>alarmd</module>`

**Step 1: Create Alarmd assembly POM**

Model after `opennms-assemblies/sentinel/pom.xml`. The Alarmd assembly packages:
- Minimal Karaf container
- Alarmd feature + dependencies
- Kafka consumer feature
- MessageBus JMS feature
- Alarm DAO + Hibernate
- Drools correlation rules
- Northbound interface plugins

**Step 2: Create Alarmd Dockerfile**

Model after `opennms-container/sentinel/Dockerfile`:

```dockerfile
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.6.3.b335-jre-17"

FROM ${BASE_IMAGE} AS alarmd-tarball
ADD --chown=10001:0 ./tarball-root/ /opt/opennms-alarmd/
RUN chmod -R g-w /opt/opennms-alarmd && \
    chmod -R g=u /opt/opennms-alarmd/etc /opt/opennms-alarmd/data

FROM alarmd-tarball AS alarmd-base
RUN /opt/opennms-alarmd/bin/fix-permissions /opt/opennms-alarmd/etc && \
    /opt/opennms-alarmd/bin/fix-permissions /opt/opennms-alarmd/data

FROM alarmd-base
USER 10001

LABEL maintainer="The OpenNMS Group" \
      name="OpenNMS Alarmd"

WORKDIR /opt/opennms-alarmd

ENTRYPOINT ["/opt/opennms-alarmd/bin/karaf", "server"]

EXPOSE 8201/tcp

ENV OPENNMS_HOME="/opt/opennms-alarmd" \
    JAVA_OPTS=""

VOLUME ["/opt/opennms-alarmd/etc", "/opt/opennms-alarmd/data"]
```

**Step 3: Create Makefile**

Model after `opennms-container/sentinel/Makefile`, referencing `common.mk`.

**Step 4: Create service-configuration.xml for Alarmd container**

A minimal `service-configuration.xml` with only Manager and Alarmd enabled.

**Step 5: Commit**

```bash
git add opennms-container/alarmd/ opennms-assemblies/alarmd/ opennms-assemblies/pom.xml
git commit -m "feat: add Alarmd Docker container and assembly"
```

---

## Task 13: Smoke Test for Alarmd Container Extraction

**Files:**
- Create: `smoke-test/src/main/java/org/opennms/smoketest/containers/AlarmdContainer.java`
- Create: `smoke-test/src/test/java/org/opennms/smoketest/AlarmdExtractionIT.java`

**Step 1: Create AlarmdContainer (Testcontainers)**

Model after existing `MinionContainer.java` or `SentinelContainer.java`. The container:
- Starts from the Alarmd Docker image
- Connects to shared Kafka and PostgreSQL
- Configures broker URLs and database connection

**Step 2: Write the smoke test**

```java
public class AlarmdExtractionIT {

    @Test
    public void shouldCreateAlarmFromKafkaEvent() {
        // 1. Start Core container with Alarmd disabled
        // 2. Start Alarmd container (standalone)
        // 3. Send a fault event to the Core container (e.g., nodeDown)
        // 4. Verify the event is published to Kafka
        // 5. Verify Alarmd container creates an alarm in PostgreSQL
        // 6. Verify alarm has denormalized event data and JSONB
    }

    @Test
    public void shouldReloadConfigViaMessageBus() {
        // 1. Both containers running
        // 2. Send reloadDaemonConfig IPC message targeting Alarmd
        // 3. Verify Alarmd receives it via JMS and reloads
    }
}
```

**Step 3: Run smoke test**

Run: `./compile.pl -t --projects :smoke-test -am verify -Dtest=AlarmdExtractionIT`
Expected: PASS — Alarmd running in its own container, creating alarms from Kafka events.

**Step 4: Commit**

```bash
git add smoke-test/
git commit -m "feat: add smoke test for Alarmd container extraction"
```

---

## Parallelization Guide

These tasks can be distributed across windows:

| Window | Tasks | Notes |
|--------|-------|-------|
| **Window 1** | Task 1 (Protobuf) | Quick, unblocks nothing but improves perf |
| **Window 2** | Tasks 2-5 (complex daemons) | Sequential within window — shared opennms-services module |
| **Window 3** | Tasks 6-9 (simple daemons) | Can parallelize internally — different modules |
| **Window 4** | Task 10 (DaemonTools) | Depends on Tasks 2-9 ideally, but can start independently |
| **Window 5** | Tasks 11-12 (Karaf + Docker) | Independent of daemon migration |
| **Window 6** | Task 13 (smoke test) | Depends on ALL other tasks |

**Recommended grouping for 3 windows:**
- **Window A:** Tasks 1, 11, 12 (infrastructure — Protobuf, Karaf, Docker)
- **Window B:** Tasks 2, 3, 4, 5, 10 (opennms-services daemons + DaemonTools)
- **Window C:** Tasks 6, 7, 8, 9 (feature module daemons)
- **Then:** Task 13 (smoke test) after merging all three
