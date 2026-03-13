# Minion-Only Network Listeners Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove all network UDP/TCP listeners from daemon containers, making Minion the sole network ingress point. Add Syslogd KafkaSinkBridge and Telemetryd standalone container.

**Architecture:** Sequential deletion of dead listeners (Eventd, DHCP), then Syslogd bridge (copy Trapd pattern), then Telemetryd container extraction. Each task is independently compilable and committable.

**Tech Stack:** Java 17, Maven, OSGi/Karaf, Spring XML, Kafka, Netty, Protobuf

**Design spec:** `docs/plans/2026-03-13-minion-only-listeners-design.md`

---

## Chunk 1: Eventd Listener Deletion + DHCP Monitor Deletion

### Task 1: Delete Eventd TCP/UDP Listeners

These listeners accept XML events on port 5817 via Netty. In Delta-V, events arrive via Kafka — these are dead code.

**Files:**
- Delete: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/TcpListener.java`
- Delete: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/UdpListener.java`
- Delete: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/XmlEventProcessor.java`
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml:177-185`
- Modify: `opennms-config-api/src/main/java/org/opennms/netmgt/config/api/EventdConfig.java`
- Modify: `opennms-config-model/src/main/java/org/opennms/netmgt/config/eventd/EventdConfiguration.java`
- Modify: `opennms-config/src/main/java/org/opennms/netmgt/config/EventdConfigManager.java`
- Modify: `opennms-base-assembly/src/main/filtered/etc/eventd-configuration.xml`
- Delete: `opennms-base-assembly/src/main/filtered/bin/send-event.pl`

- [ ] **Step 1: Delete the listener Java files**

```bash
rm -f features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/TcpListener.java
rm -f features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/UdpListener.java
rm -f features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/XmlEventProcessor.java
```

If the `listener/` directory is now empty, delete it too:
```bash
rmdir features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/
```

- [ ] **Step 2: Remove listener beans from applicationContext-eventDaemon.xml**

In `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml`, remove lines 177-185 (the `tcpListener` and `udpListener` bean definitions):

```xml
<!-- DELETE these beans -->
<bean id="tcpListener" class="org.opennms.netmgt.eventd.listener.TcpListener"
      init-method="start" destroy-method="stop">
    <constructor-arg ref="eventdConfigManager"/>
    <constructor-arg ref="eventForwarder"/>
</bean>
<bean id="udpListener" class="org.opennms.netmgt.eventd.listener.UdpListener"
      init-method="start" destroy-method="stop">
    <constructor-arg ref="eventdConfigManager"/>
    <constructor-arg ref="eventForwarder"/>
</bean>
```

Also remove any `depends-on` references to these beans in the `eventDaemon` bean definition.

Also update line 76 — the `eventIpcManagerHandlerPoolSize` bean references `getReceivers` which is being deleted:
```xml
<!-- CHANGE factory-method from "getReceivers" to "getNumThreads" -->
<bean id="eventIpcManagerHandlerPoolSize" factory-bean="eventdConfigManager" factory-method="getNumThreads"/>
```

- [ ] **Step 3: Remove TCP/UDP methods from EventdConfig interface**

In `opennms-config-api/src/main/java/org/opennms/netmgt/config/api/EventdConfig.java`:

**Methods to REMOVE** (all listener-related):
- `getTCPIpAddress()` (line 36)
- `getTCPPort()` (line 43)
- `getUDPIpAddress()` (line 50)
- `getUDPPort()` (line 57)
- `getReceivers()` (line 64)
- `getSocketSoTimeoutRequired()` (line 71)
- `getSocketSoTimeoutPeriod()` (line 78)
- `hasSocketSoTimeoutPeriod()` (line 92)

**Methods to KEEP** (used by EventSinkConsumer and EventDispatcherImpl):
- `getGetNextEventID()`
- `getNumThreads()`
- `getQueueLength()`
- `getQueueSize()`
- `getBatchSize()`
- `getBatchIntervalMs()`

- [ ] **Step 4: Remove TCP/UDP fields from EventdConfiguration JAXB model**

In `opennms-config-model/src/main/java/org/opennms/netmgt/config/eventd/EventdConfiguration.java`:

**Fields to REMOVE** (lines 55-117 selectively):
- `m_tcpAddress` (lines 55-56) + `@XmlAttribute`
- `m_tcpPort` (lines 62-63) + `@XmlAttribute`
- `m_udpAddress` (lines 70-71) + `@XmlAttribute`
- `m_udpPort` (lines 77-78) + `@XmlAttribute`
- `m_receivers` (lines 84-85) + `@XmlAttribute`
- `m_socketSoTimeoutRequired` (lines 108-109) + `@XmlAttribute`
- `m_socketSoTimeoutPeriod` (lines 116-117) + `@XmlAttribute`
- All corresponding getter/setter methods
- `has*` methods for the removed fields

**Fields to KEEP:**
- `m_logEventSummaries`
- `m_queueLength`
- `m_getNextEventID`
- `m_threads`, `m_queueSize`, `m_batchSize`, `m_batchInterval`

**Also update:** `hashCode()` (line 276) and `equals()` (line 295) — remove all references to deleted fields from these methods.

- [ ] **Step 5: Remove TCP/UDP methods from EventdConfigManager**

In `opennms-config/src/main/java/org/opennms/netmgt/config/EventdConfigManager.java`, remove the implementations of all methods deleted from the interface in Step 3:
- `getTCPIpAddress()`, `getTCPPort()`, `getUDPIpAddress()`, `getUDPPort()`
- `getReceivers()`, `getSocketSoTimeoutRequired()`, `getSocketSoTimeoutPeriod()`, `hasSocketSoTimeoutPeriod()`

Keep all methods corresponding to the kept interface methods.

- [ ] **Step 6: Strip listener attributes from eventd-configuration.xml**

In `opennms-base-assembly/src/main/filtered/etc/eventd-configuration.xml`, remove the `TCPAddress`, `TCPPort`, `UDPAddress`, `UDPPort`, `receivers`, `socketSoTimeoutRequired`, `socketSoTimeoutPeriod` attributes from the root element. Keep `logEventSummaries` and any other non-listener attributes.

- [ ] **Step 7: Delete send-event.pl**

```bash
rm -f opennms-base-assembly/src/main/filtered/bin/send-event.pl
```

This script hardcodes `$PORT_TO = 5817` (line 75) and sends events via TCP socket. With Eventd listeners deleted, it cannot function. A REST/gRPC replacement is deferred to post-Spring Boot migration.

- [ ] **Step 8: Search for dangling references**

Search for any remaining references to the deleted classes or listener config:

```bash
rg -l "TcpListener|UdpListener|XmlEventProcessor" --type java --type xml . | grep -v target | grep -v telemetry
rg -l "TCPPort|UDPPort|TCPAddress|UDPAddress" --type java --type xml . | grep -v target
rg -l "send-event\.pl" . | grep -v target | grep -v ".git"
rg -l "getReceivers|getSocketSoTimeout" --type java . | grep -v target
```

Fix any dangling imports or references. The telemetry `TcpListener`/`UdpListener` are in a different package (`org.opennms.netmgt.telemetry.listeners`) and are unrelated — do NOT delete those.

Check specifically:
- `MockEventIpcManager` and test configs that may reference removed methods
- `CustomEventdConfigManager` in syslog test code — this class overrides `getTCPIpAddress()`, `getTCPPort()`, `getUDPIpAddress()`, `getUDPPort()`. **Remove these overrides** since the parent methods are being deleted.

- [ ] **Step 9: Compile and verify**

```bash
./compile.pl -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat(delta-v): delete Eventd TCP/UDP listeners — events arrive via Kafka only"
```

---

### Task 2: Delete DHCP Monitor

The DHCP monitor binds privileged port 68, is rarely used, and requires the `features/dhcpd` module. Delete both the monitor and its backing service, plus the DHCP detector.

**Files:**
- Delete: `features/dhcpd/` (entire module)
- Delete: `opennms-provision/opennms-detector-dhcp/` (entire module)
- Delete: `features/poller/monitors/core/src/main/java/org/opennms/netmgt/poller/monitors/DhcpMonitor.java`
- Modify: `features/pom.xml:35`
- Modify: `opennms-provision/pom.xml:24`
- Modify: `container/features/src/main/resources/features.xml:870,879,891-893,931`
- Modify: `opennms-container/delta-v/webapp-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml:870,879,891-893,931`
- Modify: `opennms-container/delta-v/webapp-overlay/etc/org.apache.karaf.features.cfg:68`
- Modify: `pom.xml:1784,2727-2731,3986-3989`

- [ ] **Step 1: Delete dhcpd module and detector module**

```bash
rm -rf features/dhcpd/
rm -rf opennms-provision/opennms-detector-dhcp/
```

- [ ] **Step 2: Delete DhcpMonitor from poller monitors**

```bash
rm -f features/poller/monitors/core/src/main/java/org/opennms/netmgt/poller/monitors/DhcpMonitor.java
```

Also remove `org.opennms.netmgt.poller.monitors.DhcpMonitor` from the ServiceMonitor service file at `features/poller/monitors/core/src/main/resources/META-INF/services/org.opennms.netmgt.poller.ServiceMonitor` (line 8).

Also check and remove DhcpMonitor from the Blueprint registration in `features/poller/monitors/core/src/main/resources/OSGI-INF/blueprint/blueprint.xml`. Search for `dhcpMonitor` or `DhcpMonitor` and remove the bean definition and service registration.

- [ ] **Step 3: Remove module declarations from parent POMs**

In `features/pom.xml`, remove line 35:
```xml
<module>dhcpd</module>
```

In `opennms-provision/pom.xml`, remove line 24:
```xml
<module>opennms-detector-dhcp</module>
```

- [ ] **Step 4: Remove opennms-dhcpd from source features.xml**

In `container/features/src/main/resources/features.xml`:

Remove the feature definition (lines 891-893, includes the dhcp4java bundle):
```xml
<feature name="opennms-dhcpd" description="OpenNMS :: Features :: DHCPD" version="${project.version}">
    <bundle>mvn:org.opennms.features/org.opennms.features.dhcpd/${project.version}</bundle>
    <bundle>mvn:com.helger/dhcp4java/${dhcp4javaVersion}</bundle>
</feature>
```

Remove the references to it:
- Line 870: `<feature>opennms-dhcpd</feature>` (in `opennms-provisioning-adapters-all` feature)
- Line 931: `<feature>opennms-dhcpd</feature>` (in `opennms-poller-monitors-core` feature)

Also remove the `opennms-detector-dhcp` bundle reference (line ~879 in `opennms-provisioning-adapters-all`):
```xml
<bundle>mvn:org.opennms/opennms-detector-dhcp/${project.version}</bundle>
```

- [ ] **Step 5: Remove opennms-dhcpd from overlay features.xml**

In `opennms-container/delta-v/webapp-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml`:

Apply the same removals as Step 4:
- Remove feature definition (lines 891-893)
- Remove references at lines 870 and 931
- Remove `opennms-detector-dhcp` bundle reference

- [ ] **Step 6: Remove opennms-dhcpd from boot features**

In `opennms-container/delta-v/webapp-overlay/etc/org.apache.karaf.features.cfg`, remove `opennms-dhcpd` from the `featuresBoot` list (line 68). Be careful with trailing commas/backslash continuation.

- [ ] **Step 7: Remove dhcp4java and dhcpd dependency management**

In root `pom.xml`:
- Remove the property `<dhcp4javaVersion>1.1.0</dhcp4javaVersion>` (line 1784)
- Remove the dependency management entry for `com.helger:dhcp4java` (lines 3986-3989)
- Remove the dependency management entry for `org.opennms.features:org.opennms.features.dhcpd` (lines 2727-2731)

- [ ] **Step 8: Remove DHCP from poller-configuration.xml**

Search for and remove DHCP service/monitor entries from configuration examples:

```bash
rg -l "DhcpMonitor|DhcpDetector|service=\"DHCP\"" --type xml . | grep -v target
```

Remove DHCP service definitions from `poller-configuration.xml` and detector entries from `foreign-sources.xml` if present. These are in `opennms-base-assembly/src/main/filtered/etc/`.

- [ ] **Step 9: Search for dangling references**

```bash
rg -l "dhcpd|DhcpMonitor|DhcpDetector|Dhcpd|dhcp4java" --type java --type xml --type properties . | grep -v target | grep -v node_modules
```

Fix any remaining imports, config references, or test fixtures. Common locations:
- `integration-tests/config/.../WillItUnmarshalIT.java`
- `smoke-test/` test classes
- `opennms-base-assembly/` config files

- [ ] **Step 10: Compile and verify**

```bash
./compile.pl -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "feat(delta-v): delete DHCP monitor and detector — privileged port 68, rarely used"
```

---

## Chunk 2: Syslogd KafkaSinkBridge

### Task 3: Replace Syslogd UDP Listener with KafkaSinkBridge

Currently the Syslogd container listens directly on UDP 10514. Replace with the Trapd pattern: Minion listens for syslog, forwards via Kafka Sink topic `OpenNMS.Sink.Syslog`, and a `KafkaSinkBridge` in the Syslogd container consumes it.

**Reference implementation:** `core/daemon-loader-trapd/` — follow this pattern exactly.

**Key pattern (from Trapd):**
1. `KafkaSinkBridge` takes `LocalMessageConsumerManager` via constructor injection
2. `LocalMessageConsumerManager` has a `setKafkaSinkBridge()` property that links the two
3. When `SyslogSinkConsumer` registers via `messageConsumerManager.registerConsumer()`, the `startConsumingForModule()` callback fires, which calls `kafkaSinkBridge.setModule(module)`
4. The bridge thread (waiting in `pollLoop()`) sees the module, computes the topic name (`OpenNMS.Sink.Syslog`), and starts consuming
5. Bootstrap servers and group ID come from system properties (not Spring injection)

**Files:**
- Create: `core/daemon-loader-syslogd/src/main/java/org/opennms/core/daemon/loader/KafkaSinkBridge.java`
- Modify: `core/daemon-loader-syslogd/src/main/java/org/opennms/core/daemon/loader/LocalMessageConsumerManager.java`
- Modify: `core/daemon-loader-syslogd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-syslogd.xml`
- Modify: `core/daemon-loader-syslogd/pom.xml`
- Modify: `opennms-container/delta-v/docker-compose.yml` (syslogd + minion services)

- [ ] **Step 1: Add Kafka + Protobuf dependencies to daemon-loader-syslogd POM**

In `core/daemon-loader-syslogd/pom.xml`, add these dependencies (copy from `core/daemon-loader-trapd/pom.xml` lines 176-204):

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>${kafkaBundleVersion}</version>
    <exclusions>
        <exclusion>
            <groupId>org.lz4</groupId>
            <artifactId>lz4-java</artifactId>
        </exclusion>
        <exclusion>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>${protobufVersion}</version>
</dependency>
```

- [ ] **Step 2: Copy KafkaSinkBridge.java from Trapd**

Copy `core/daemon-loader-trapd/src/main/java/org/opennms/core/daemon/loader/KafkaSinkBridge.java` to `core/daemon-loader-syslogd/src/main/java/org/opennms/core/daemon/loader/KafkaSinkBridge.java`.

The file is **identical** — no modifications needed. It is module-agnostic:
- Gets its `SinkModule` via `setModule()` callback from `LocalMessageConsumerManager.startConsumingForModule()`
- Computes topic from `module.getId()` → `OpenNMS.Sink.Syslog`
- Reads bootstrap servers from system property `org.opennms.core.ipc.sink.kafka.bootstrap.servers`
- Reads group ID from system property `org.opennms.core.ipc.sink.kafka.group.id` (default: `opennms-trapd-sink`)

- [ ] **Step 3: Update LocalMessageConsumerManager to support KafkaSinkBridge**

The syslogd's `LocalMessageConsumerManager` is the older version without the bridge hook. Replace it with the trapd version.

Copy `core/daemon-loader-trapd/src/main/java/org/opennms/core/daemon/loader/LocalMessageConsumerManager.java` to `core/daemon-loader-syslogd/src/main/java/org/opennms/core/daemon/loader/LocalMessageConsumerManager.java`.

This adds:
- `private KafkaSinkBridge kafkaSinkBridge;` field
- `setKafkaSinkBridge()` setter
- In `startConsumingForModule()`: calls `kafkaSinkBridge.setModule(module)` when bridge is configured

- [ ] **Step 4: Rewire applicationContext-daemon-loader-syslogd.xml**

Replace the Syslogd daemon bean and UDP receiver with the KafkaSinkBridge pattern.

**Remove:**
- The `syslogReceiverCamelNettyImpl` bean (UDP listener)
- The `daemon` (Syslogd) bean — it would NPE without a receiver (`m_udpEventReceiver.run()` in `onStart()`)

**Replace the `messageConsumerManager` bean** to add the bridge hook (matching Trapd's wiring at lines 99-115 of `applicationContext-daemon-loader-trapd.xml`):

```xml
<!-- Sink API: local in-process dispatch + Kafka Sink bridge for Minion-forwarded syslog.
     The KafkaSinkBridge consumes from the Kafka Sink topic (OpenNMS.Sink.Syslog) and
     dispatches to the LocalMessageConsumerManager, which delivers to SyslogSinkConsumer. -->
<bean id="messageConsumerManager"
      class="org.opennms.core.daemon.loader.LocalMessageConsumerManager">
    <property name="kafkaSinkBridge" ref="kafkaSinkBridge"/>
</bean>
<bean id="kafkaSinkBridge"
      class="org.opennms.core.daemon.loader.KafkaSinkBridge"
      init-method="afterPropertiesSet" destroy-method="destroy">
    <constructor-arg ref="messageConsumerManager"/>
</bean>
<bean id="messageDispatcherFactory"
      class="org.opennms.core.daemon.loader.LocalMessageDispatcherFactory">
    <constructor-arg ref="messageConsumerManager"/>
</bean>
```

**Keep** these existing beans unchanged:
- `serviceRegistry` (osgi:reference)
- `syslogdConfig` (SyslogdConfig factory)
- `syslogSinkConsumer` (SyslogSinkConsumer)
- `daemonLifecycleManager` (DaemonLifecycleManager) — update `depends-on` if it references the deleted daemon bean
- All `onmsgi:reference` beans for DAOs
- `distPollerDao` (osgi:reference)

- [ ] **Step 5: Update syslogd docker-compose service**

In `opennms-container/delta-v/docker-compose.yml`, find the `syslogd` service:

1. **Remove** the UDP port mapping:
```yaml
    ports:
      - "10514:10514/udp"
```

2. **Add** Kafka Sink system properties to JAVA_OPTS:
```
-Dorg.opennms.core.ipc.sink.kafka.bootstrap.servers=kafka:9092
-Dorg.opennms.core.ipc.sink.kafka.group.id=opennms-syslogd-sink
```

3. **Add** volume mount for the rebuilt daemon-loader-syslogd JAR (the bridge is a new class in the bundle):
```yaml
      - ../../core/daemon-loader-syslogd/target/daemon-loader-syslogd-36.0.0-SNAPSHOT.jar:/opt/sentinel/system/org/opennms/core/daemon-loader-syslogd/36.0.0-SNAPSHOT/daemon-loader-syslogd-36.0.0-SNAPSHOT.jar:ro
```

- [ ] **Step 6: Add syslog UDP port to Minion docker-compose service**

In `opennms-container/delta-v/docker-compose.yml`, find the `minion` service and add syslog port mapping:

```yaml
    ports:
      - "11162:1162/udp"    # existing SNMP trap port
      - "1514:10514/udp"    # NEW: syslog port
```

Minion is now the sole syslog ingress point.

- [ ] **Step 7: Compile and verify**

```bash
./compile.pl -DskipTests --projects :daemon-loader-syslogd -am install
```

Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat(delta-v): replace Syslogd UDP listener with KafkaSinkBridge via Minion"
```

---

## Chunk 3: Telemetryd Standalone Container

### Task 4: Create Telemetryd Daemon Container + Remove Listeners from Daemon-Side

Create a new Telemetryd standalone container (TSID=18) that consumes telemetry from Kafka Sink topics. Remove the listener bundle references from daemon-side Karaf features (Minion keeps its listeners).

**Key difference from Trapd/Syslogd:** Telemetry uses per-queue Sink topics. `TelemetrySinkModule` computes `moduleId = "Telemetry-" + queueConfig.getName()`, producing separate Kafka topics like `OpenNMS.Sink.Telemetry-Netflow-5`, `OpenNMS.Sink.Telemetry-IPFIX`, etc.

**Multi-module bridge pattern:** The `LocalMessageConsumerManager.startConsumingForModule()` callback fires once per registered module. For Telemetryd with N protocol queues, this fires N times. We extend `LocalMessageConsumerManager` to spawn a per-module `KafkaSinkBridge` thread for each registered module, giving each its own Kafka consumer on its own topic.

**Files:**
- Create: `core/daemon-loader-telemetryd/pom.xml`
- Create: `core/daemon-loader-telemetryd/src/main/java/org/opennms/core/daemon/loader/DaemonLifecycleManager.java`
- Create: `core/daemon-loader-telemetryd/src/main/java/org/opennms/core/daemon/loader/MultiModuleKafkaSinkBridge.java`
- Create: `core/daemon-loader-telemetryd/src/main/java/org/opennms/core/daemon/loader/TelemetryMessageConsumerManager.java`
- Create: `core/daemon-loader-telemetryd/src/main/java/org/opennms/core/daemon/loader/LocalMessageDispatcherFactory.java`
- Create: `core/daemon-loader-telemetryd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-telemetryd.xml`
- Modify: `core/pom.xml` (add module)
- Modify: `container/features/src/main/resources/features.xml` (remove listener bundle from 3 features, add telemetryd feature)
- Modify: `opennms-container/delta-v/webapp-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml` (same)
- Modify: `features/container/sentinel/pom.xml` (add telemetryd to installed features)
- Modify: `opennms-container/delta-v/docker-compose.yml` (add telemetryd service)

**Important:** Do NOT remove `<module>listeners</module>` from `features/telemetry/pom.xml` — Minion's `features-minion.xml` references the listeners bundle. Only remove the bundle references from non-Minion Karaf features.

- [ ] **Step 1: Create daemon-loader-telemetryd POM**

Create `core/daemon-loader-telemetryd/pom.xml` using `core/daemon-loader-trapd/pom.xml` as template. Key differences:
- `<artifactId>daemon-loader-telemetryd</artifactId>`
- `<name>OpenNMS :: Core :: Daemon Loader :: Telemetryd</name>`
- Spring-Context header: `META-INF/opennms/applicationContext-daemon-loader-telemetryd.xml;create-asynchronously:=true`
- Dependencies: replace trap-specific deps with telemetry deps:
  - `org.opennms.features.telemetry:org.opennms.features.telemetry.api`
  - `org.opennms.features.telemetry:org.opennms.features.telemetry.common`
  - `org.opennms.features.telemetry:org.opennms.features.telemetry.daemon`
  - Keep: kafka-clients, protobuf, sink.api, sink.common, dao-api, metrics, core.lib
- Add `Import-Package: *;resolution:=optional` + `DynamicImport-Package: *` (telemetry has many optional imports)

- [ ] **Step 2: Copy standard daemon-loader classes**

Copy from `core/daemon-loader-trapd/src/main/java/org/opennms/core/daemon/loader/`:
- `DaemonLifecycleManager.java`
- `LocalMessageDispatcherFactory.java`

Do NOT copy `LocalMessageConsumerManager.java` or `KafkaSinkBridge.java` — we create telemetry-specific variants in Steps 3-4.

- [ ] **Step 3: Create TelemetryMessageConsumerManager.java**

Create `core/daemon-loader-telemetryd/src/main/java/org/opennms/core/daemon/loader/TelemetryMessageConsumerManager.java`.

This extends `AbstractMessageConsumerManager` and spawns a `KafkaSinkBridge` per module when `startConsumingForModule()` is called:

```java
package org.opennms.core.daemon.loader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Multi-module MessageConsumerManager for Telemetryd.
 *
 * Unlike the single-module LocalMessageConsumerManager (used by Trapd/Syslogd),
 * this manager spawns a separate KafkaSinkBridge per registered SinkModule.
 * Each bridge consumes from its own Kafka topic (e.g., OpenNMS.Sink.Telemetry-Netflow-5).
 */
public class TelemetryMessageConsumerManager extends AbstractMessageConsumerManager
        implements DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryMessageConsumerManager.class);

    private final Map<String, KafkaSinkBridge> bridges = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    protected void startConsumingForModule(SinkModule<?, Message> module) {
        String moduleId = module.getId();
        LOG.info("Telemetry sink consumer registered for module: {}", moduleId);

        if (bridges.containsKey(moduleId)) {
            LOG.warn("Bridge already exists for module: {}", moduleId);
            return;
        }

        KafkaSinkBridge bridge = new KafkaSinkBridge(this);
        bridge.setModule(module);
        bridges.put(moduleId, bridge);

        try {
            bridge.afterPropertiesSet();
            LOG.info("KafkaSinkBridge started for telemetry module: {}", moduleId);
        } catch (Exception e) {
            LOG.error("Failed to start KafkaSinkBridge for module {}: {}", moduleId, e.getMessage(), e);
            bridges.remove(moduleId);
        }
    }

    @Override
    protected void stopConsumingForModule(SinkModule<?, Message> module) {
        String moduleId = module.getId();
        KafkaSinkBridge bridge = bridges.remove(moduleId);
        if (bridge != null) {
            bridge.destroy();
            LOG.info("KafkaSinkBridge stopped for telemetry module: {}", moduleId);
        }
    }

    @Override
    public void destroy() {
        for (Map.Entry<String, KafkaSinkBridge> entry : bridges.entrySet()) {
            entry.getValue().destroy();
            LOG.info("KafkaSinkBridge destroyed for module: {}", entry.getKey());
        }
        bridges.clear();
    }
}
```

Also copy `KafkaSinkBridge.java` from Trapd. **One change required:** change the constructor parameter type from `LocalMessageConsumerManager` to `AbstractMessageConsumerManager`:

```java
// In the telemetryd copy only — change constructor:
private final AbstractMessageConsumerManager consumerManager;

public KafkaSinkBridge(AbstractMessageConsumerManager consumerManager) {
    this.consumerManager = consumerManager;
}
```

This is necessary because `TelemetryMessageConsumerManager` extends `AbstractMessageConsumerManager` (not `LocalMessageConsumerManager`), and the `dispatch(module, message)` method is defined on `AbstractMessageConsumerManager`.

- [ ] **Step 4: Create applicationContext-daemon-loader-telemetryd.xml**

Create `core/daemon-loader-telemetryd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-telemetryd.xml`. Use Trapd's context as template, with telemetry-specific wiring:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:osgi="http://www.springframework.org/schema/osgi"
       xmlns:onmsgi="http://xmlns.opennms.org/xsd/spring/onms-osgi"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.2.xsd
           http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi.xsd
           http://xmlns.opennms.org/xsd/spring/onms-osgi http://xmlns.opennms.org/xsd/spring/onms-osgi.xsd
       ">

    <context:annotation-config />

    <osgi:reference id="serviceRegistry"
                    interface="org.opennms.core.soa.ServiceRegistry"/>

    <!-- Event infrastructure -->
    <osgi:reference id="eventIpcManager"
                    interface="org.opennms.netmgt.events.api.EventIpcManager"/>
    <osgi:reference id="eventSubscriptionService"
                    interface="org.opennms.netmgt.events.api.EventSubscriptionService"/>

    <!-- DAOs -->
    <osgi:reference id="distPollerDao"
                    interface="org.opennms.netmgt.dao.api.DistPollerDao"/>

    <!-- Multi-module Sink API with per-module Kafka bridges -->
    <bean id="messageConsumerManager"
          class="org.opennms.core.daemon.loader.TelemetryMessageConsumerManager"/>
    <bean id="messageDispatcherFactory"
          class="org.opennms.core.daemon.loader.LocalMessageDispatcherFactory">
        <constructor-arg ref="messageConsumerManager"/>
    </bean>

    <!-- Daemon lifecycle -->
    <bean name="daemonLifecycleManager"
          class="org.opennms.core.daemon.loader.DaemonLifecycleManager">
        <!-- Telemetryd daemon is loaded via Karaf feature (opennms-telemetry-daemon).
             The daemon registers telemetry consumers with messageConsumerManager,
             which triggers per-module KafkaSinkBridge creation. -->
    </bean>
</beans>
```

**Note:** The `Telemetryd` daemon class and its telemetry adapters (Netflow, sFlow, IPFIX consumers) are loaded as OSGi bundles via the `opennms-telemetry-daemon` Karaf feature. When `Telemetryd.start()` runs, it creates `TelemetrySinkModule` instances per queue and registers them with the `MessageConsumerManager` (our `TelemetryMessageConsumerManager`). This triggers `startConsumingForModule()` which spawns a `KafkaSinkBridge` per module.

If the `Telemetryd` daemon bean needs explicit Spring wiring instead of OSGi feature loading, add it to this context following the pattern of other daemon-loaders (e.g., Trapd). This will be resolved during implementation based on how Telemetryd discovers the `MessageConsumerManager`.

**Important — telemetryd-configuration.xml on daemon side:** The daemon container's `telemetryd-configuration.xml` must contain **no `<listener>` elements** — only `<queue>` elements with adapters. `Telemetryd.start()` (lines 134-148) iterates `config.getListeners()` and calls `telemetryRegistry.getListener(listenerConfig)`. If listener factories aren't available (listeners bundle removed), this throws `IllegalStateException`. With no `<listener>` elements, the loop body never executes. Minion keeps its own telemetryd-configuration.xml with listeners.

- [ ] **Step 5: Add daemon-loader-telemetryd to core/pom.xml**

In `core/pom.xml`, add the new module near the other daemon-loader modules:

```xml
<module>daemon-loader-telemetryd</module>
```

- [ ] **Step 6: Remove listener bundle from daemon-side Karaf features**

In `container/features/src/main/resources/features.xml`, remove the telemetry listeners bundle reference from all 3 referencing features:

1. `opennms-telemetry-collection` feature (line 1032):
```xml
<!-- DELETE this line -->
<bundle>mvn:org.opennms.features.telemetry/org.opennms.features.telemetry.listeners/${project.version}</bundle>
```

2. `opennms-telemetry-daemon` feature (line 1056):
```xml
<!-- DELETE this line -->
<bundle>mvn:org.opennms.features.telemetry/org.opennms.features.telemetry.listeners/${project.version}</bundle>
```

3. `opennms-flows` feature (line 1193):
```xml
<!-- DELETE this line -->
<bundle>mvn:org.opennms.features.telemetry/org.opennms.features.telemetry.listeners/${project.version}</bundle>
```

Do NOT remove from `features-minion.xml` — Minion keeps its listeners.

Apply the same removals to the overlay features.xml at `opennms-container/delta-v/webapp-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml`.

- [ ] **Step 7: Add Karaf feature for daemon-loader-telemetryd**

In `container/features/src/main/resources/features.xml`, add a new feature definition:

```xml
<feature name="opennms-daemon-telemetryd" description="OpenNMS :: Daemon Loader :: Telemetryd" version="${project.version}">
    <feature>opennms-core-daemon</feature>
    <feature>opennms-telemetry-daemon</feature>
    <bundle>mvn:org.opennms.core/daemon-loader-telemetryd/${project.version}</bundle>
</feature>
```

Also add to the sentinel `<installedFeatures>` in `features/container/sentinel/pom.xml` so Maven places the JAR in the `system/` directory:

```xml
<installedFeature>opennms-daemon-telemetryd</installedFeature>
```

- [ ] **Step 8: Add telemetryd service to docker-compose.yml**

In `opennms-container/delta-v/docker-compose.yml`, add the telemetryd service. Copy the full service block from an existing daemon service (e.g., syslogd) — do NOT use YAML anchors (the compose file doesn't define them).

Key settings for telemetryd:
- `container_name: delta-v-telemetryd`
- `hostname: telemetryd`
- `profiles: [full]`
- `CORE_SERVICE_TELEMETRYD_ENABLED: "true"`
- `JAVA_OPTS` must include:
  - `-Dorg.opennms.tsid.node-id=18`
  - `-Dorg.opennms.core.ipc.sink.kafka.bootstrap.servers=kafka:9092`
  - `-Dorg.opennms.core.ipc.sink.kafka.group.id=opennms-telemetryd-sink`
- No port mappings (consumes from Kafka only)
- Volume mount for daemon-loader-telemetryd JAR overlay
- Volume mount for shared features.xml overlay

Add the volume declaration:
```yaml
  telemetryd-data:
```

**Keep** `CORE_SERVICE_TELEMETRYD_ENABLED: "false"` in the webapp service — removing it would allow telemetryd to start inside the webapp container (default is "true").

- [ ] **Step 9: Compile and verify**

```bash
./compile.pl -DskipTests --projects :daemon-loader-telemetryd -am install
```

Expected: BUILD SUCCESS

Full compile:
```bash
./compile.pl -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat(delta-v): add Telemetryd standalone container (TSID=18), remove listeners from daemon-side"
```

---

## Chunk 4: Final Verification + Docker E2E

### Task 5: End-to-End Verification

Verify the full stack works with Minion as sole network ingress.

- [ ] **Step 1: Build daemon assembly**

```bash
cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install
cd ../..
```

Extract tarball and rebuild Docker image if needed:
```bash
cd opennms-container/sentinel
# Follow existing build process for opennms/daemon image
```

- [ ] **Step 2: Start Docker Compose stack**

```bash
cd opennms-container/delta-v

# Stop any running containers and clean volumes
docker compose down -v

# Start full profile (all 19 services)
COMPOSE_PROFILES=full docker compose up -d
```

Wait for all services to become healthy:
```bash
docker compose ps
```

Expected: 19 services running (telemetryd is new, TSID=18).

- [ ] **Step 3: Verify no daemon container binds monitoring ports**

```bash
# Check that only Minion exposes UDP/TCP monitoring ports
docker compose port minion 1162
docker compose port minion 10514

# Verify syslogd does NOT expose port 10514
docker compose port syslogd 10514 2>&1 | grep -q "No port" && echo "PASS: syslogd has no UDP port"
```

- [ ] **Step 4: Run existing trap E2E test**

```bash
./test-e2e.sh
```

Expected: All tests pass (traps via Minion → Kafka → Trapd → Alarmd).

- [ ] **Step 5: Run Minion E2E test**

```bash
./test-minion-e2e.sh
```

Expected: All 13 tests pass.

- [ ] **Step 6: Test syslog via Minion**

First verify Minion's syslog listener is running:
```bash
docker compose exec minion ss -ulnp | grep 10514
```

If the syslog listener is active, send a test syslog message:
```bash
echo "<14>Mar 13 12:00:00 testhost syslog-test: Delta-V syslog via Minion" | nc -u -w1 localhost 1514
```

Verify the event appears in Kafka:
```bash
docker compose exec kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic opennms-fault-events --from-beginning --timeout-ms 10000 | grep -q "syslog-test" && echo "PASS: syslog event in Kafka"
```

If Minion's syslog listener is not enabled by default, configure it via `syslogd-configuration.xml` overlay on the Minion and restart.

- [ ] **Step 7: Verify telemetryd container is healthy**

```bash
docker compose logs telemetryd | tail -20
# Should show: TelemetryMessageConsumerManager or KafkaSinkBridge started messages
```

Check health endpoint (via exec since no port is mapped):
```bash
docker compose exec telemetryd curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe
```

- [ ] **Step 8: Commit verification notes**

No code changes — just confirm everything works. If any issues, fix and commit.

- [ ] **Step 9: Push**

```bash
git push delta-v eventbus-redesign
```
