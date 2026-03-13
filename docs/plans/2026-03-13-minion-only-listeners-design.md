# Minion-Only Network Listeners Design

**Date:** 2026-03-13
**Branch:** eventbus-redesign
**Status:** Approved

## Goal

Make Minion the **sole network-facing component** for all monitoring data ingress (SNMP traps, syslog, telemetry flows). All daemon containers consume exclusively from Kafka Sink topics. No daemon container binds a UDP or TCP port for external protocol traffic.

## Iron Rules

- **No daemon container listens on a network port for monitoring data.** Only Minion binds UDP/TCP ports for traps, syslog, and telemetry.
- **Each protocol family gets its own container.** Trapd, Syslogd, and Telemetryd are separate daemon containers that consume from Kafka Sink topics and process the data.
- **Eventd TCP/UDP listeners are deleted.** Events are produced to Kafka, not injected via socket. A REST/gRPC event ingestion API will replace this after the Spring Boot migration.
- **DHCP monitor is deleted entirely.** Privileged port 68 binding, niche protocol, rarely used in modern deployments.

## Architectural Context

Delta-V already established Minion as the primary network-facing component for SNMP traps (Minion → Kafka Sink → KafkaSinkBridge → Trapd). This design extends that pattern to all remaining listeners, creating a uniform architecture:

```
External source → UDP/TCP → Minion → Kafka Sink topic → KafkaSinkBridge → Daemon container → process → Kafka fault/ipc events
```

After the future Spring Boot migration, Trapd, Syslogd, and Telemetryd containers become natural homes for their respective ingestion REST/gRPC APIs.

## Changes by Component

### 1. Eventd TCP/UDP Listener Deletion

**Risk:** Zero — dead code in Delta-V, no container exposes port 5817.

**Remove:**
- `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/TcpListener.java`
- `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/UdpListener.java`
- `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/listener/XmlEventProcessor.java`
- `tcpListener` and `udpListener` bean definitions from `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml`
- TCP/UDP port methods from `EventdConfig` interface (`opennms-config-api/src/main/java/org/opennms/netmgt/config/api/EventdConfig.java`): `getTCPIpAddress()`, `getTCPPort()`, `getUDPIpAddress()`, `getUDPPort()`, `getReceivers()`, `getSocketSoTimeoutRequired()`
- Corresponding TCP/UDP fields and methods from `EventdConfigManager` implementation
- TCP/UDP attributes from `EventdConfiguration` JAXB config model class
- `eventd-configuration.xml` listener-related attributes (keep non-listener settings if any remain)
- `opennms-base-assembly/src/main/filtered/bin/send-event.pl` (hardcodes `$PORT_TO = 5817` at line 75)

**Keep:** The `Eventd` daemon class (manages `BroadcastEventProcessor` and local event dispatch). In Delta-V, events arrive via Kafka consumers (`kafkaFaultEventConsumer`, `kafkaIpcEventConsumer`).

**Future:** REST/gRPC event ingestion API after Spring Boot migration.

### 2. DHCP Monitor Deletion

**Risk:** Low — isolated leaf-node module, but has a detector module dependency to clean up.

**Remove:**
- `features/dhcpd/` — entire module (interface, impl, listener, transaction, blueprint, POM)
- `opennms-provision/opennms-detector-dhcp/` — DHCP detector module (`DhcpDetector`, `DhcpDetectorFactory`, `DhcpClient`, Blueprint) — depends on `features/dhcpd` interface and will fail to build without it
- `DhcpMonitor.java` from `features/poller/monitors/core/`
- `<module>dhcpd</module>` from `features/pom.xml`
- `opennms-dhcpd` Karaf feature definition from source `features.xml`
- `opennms-dhcpd` feature definition and references (lines 870, 891, 931) from overlay `features.xml` at `opennms-container/delta-v/webapp-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml`
- `opennms-dhcpd` from boot features in `opennms-container/delta-v/webapp-overlay/etc/org.apache.karaf.features.cfg`
- `opennms-detector-dhcp` bundle reference from `opennms-provisioning-adapters-all` feature in `features.xml`
- DHCP monitor registration from poller monitors Blueprint
- DHCP service definitions from `poller-configuration.xml` examples
- `dhcp4java` dependency from dependency management

### 3. Syslogd UDP Listener Removal + KafkaSinkBridge

**Risk:** Medium — requires new code (KafkaSinkBridge), but follows proven Trapd pattern exactly.

**Approach for Syslogd daemon:** Skip the `Syslogd` daemon bean entirely. The daemon-loader will run only `KafkaSinkBridge` + `LocalMessageConsumerManager` + `SyslogSinkConsumer`, matching the Trapd pattern where no daemon class is needed. The `Syslogd` class calls `m_udpEventReceiver.run()` in `onStart()` which would NPE without a receiver — rather than adding a no-op shim, we eliminate the daemon bean. The `SyslogSinkConsumer` registered with `LocalMessageConsumerManager` handles all processing.

**Important:** The `SyslogReceiverCamelNettyImpl` and `SyslogReceiverJavaNetImpl` source classes in `features/events/syslog/` are **kept** — Minion uses them. Only the daemon-loader's bean wiring is removed.

**Remove:**
- `syslogReceiverCamelNettyImpl` bean from `applicationContext-daemon-loader-syslogd.xml`
- `daemon` (Syslogd) bean and its `syslogReceiver` property from `applicationContext-daemon-loader-syslogd.xml`
- UDP port mapping `10514:10514/udp` from syslogd service in `docker-compose.yml`

**Add:**
- `KafkaSinkBridge.java` in `core/daemon-loader-syslogd/` — consumes from `OpenNMS.Sink.Syslog` Kafka topic, dispatches to `LocalMessageConsumerManager` (direct copy of Trapd's `KafkaSinkBridge` pattern at `core/daemon-loader-trapd/src/main/java/org/opennms/core/daemon/loader/KafkaSinkBridge.java`)
- Bean wiring in `applicationContext-daemon-loader-syslogd.xml`:
  - `kafkaSinkBridge` bean with bootstrap servers from system property `org.opennms.core.ipc.sink.kafka.bootstrap.servers`
  - Consumer group: `opennms-syslogd-sink`

**Modify on Minion:**
- Verify Minion's syslog listener is wired (SyslogReceiverCamelNettyImpl → SyslogSinkModule → Kafka Sink)
- Add UDP port mapping to Minion service in `docker-compose.yml`: `1514:10514/udp`

**Note:** The syslogd service in `docker-compose.yml` bind-mounts `features.xml` as an overlay. This overlay must include the syslogd KafkaSinkBridge bundle, similar to how Trapd's daemon-loader JAR is overlayed.

**Data flow:**
```
Syslog source → UDP:1514 → Minion → SyslogSinkModule → Kafka (OpenNMS.Sink.Syslog)
    → KafkaSinkBridge (Syslogd container) → LocalMessageConsumerManager
    → SyslogSinkConsumer → EventCreator → KafkaEventForwarder → opennms-fault-events
```

### 4. Telemetryd Standalone Container + Listener Removal

**Risk:** Medium-high — new daemon container with multi-topic Sink bridge complexity.

**Multi-topic Sink design:** Unlike Trapd (single topic `OpenNMS.Sink.Trap`) and Syslogd (single topic `OpenNMS.Sink.Syslog`), telemetry uses per-queue Sink topics. `TelemetrySinkModule` computes `moduleId = "Telemetry-" + queueConfig.getName()`, producing separate Kafka topics like `OpenNMS.Sink.Telemetry-Netflow-5`, `OpenNMS.Sink.Telemetry-Netflow-9`, `OpenNMS.Sink.Telemetry-IPFIX`, etc.

The `KafkaSinkBridge` for Telemetryd must handle multiple topics. Two options:
- **(Recommended)** A single `TelemetryKafkaSinkBridge` that subscribes via Kafka regex pattern `OpenNMS\.Sink\.Telemetry-.*` — simple, auto-discovers new protocols without config changes.
- (Alternative) One bridge instance per protocol queue — more explicit but requires config per protocol.

**Remove from daemon-side:**
- `features/telemetry/listeners/` — the listener module (TcpListener, UdpListener, TcpListenerFactory, UdpListenerFactory, Blueprint)
- `<module>listeners</module>` from `features/telemetry/pom.xml`
- Listener bundle (`org.opennms.features.telemetry.listeners`) from all 3 referencing Karaf features:
  - `opennms-telemetry-collection`
  - `opennms-telemetry-daemon`
  - sentinel/flows feature

**Add:**
- `core/daemon-loader-telemetryd/` — new daemon-loader module following existing pattern:
  - POM (bundle packaging)
  - `DaemonLifecycleManager` (or `SpringDaemonLifecycleManager`)
  - `TelemetryKafkaSinkBridge.java` — regex-subscribing bridge for `OpenNMS\.Sink\.Telemetry-.*` topics
  - `LocalMessageConsumerManager` + `LocalMessageDispatcherFactory`
  - Spring XML context wiring telemetry adapters (Netflow, sFlow, IPFIX, etc.)
- TSID assignment: **18** (next available after Bsmd=17)
- `telemetryd` service in `docker-compose.yml`:
  - Image: `opennms/daemon`
  - `CORE_SERVICE_TELEMETRYD_ENABLED: "true"`
  - No port mappings (consumes from Kafka only)
- Karaf feature for daemon-loader-telemetryd

**Keep on Minion:**
- All telemetry listeners (UDP/TCP) — Minion is the only telemetry receiver
- Minion forwards raw telemetry via Kafka Sink topics per protocol

**Data flow:**
```
Flow source → UDP:2055 → Minion → NetflowSinkModule → Kafka (OpenNMS.Sink.Telemetry-Netflow-5)
    → TelemetryKafkaSinkBridge (Telemetryd container) → LocalMessageConsumerManager
    → Protocol Adapter → persist/forward
```

### 5. Minion as Sole Network Ingress

After all changes, Minion is the **only** container binding external monitoring ports.

**Minion port mappings (docker-compose.yml):**

| Port Mapping | Protocol | Purpose |
|-------------|----------|---------|
| `11162:1162/udp` | SNMP | Trap reception (existing) |
| `1514:10514/udp` | Syslog | Syslog reception (new) |
| `2055:2055/udp` | Netflow v5/v9 | Flow reception (new, when enabled) |
| `4729:4729/udp` | sFlow | Flow reception (new, when enabled) |
| `4738:4738/tcp` | IPFIX | Flow reception (new, when enabled) |

Telemetry ports are protocol-dependent and configurable via `telemetryd-configuration.xml` on the Minion.

**Containers losing port mappings:**
- `syslogd` — loses `10514:10514/udp`

**New container:**
- `telemetryd` (TSID=18) — no external ports, Kafka-only ingress

**Updated service count:** 18 → 19 services

## Implementation Order

Sequential by risk, with compile verification after each:

1. **Eventd TCP/UDP listeners** — dead code deletion, zero risk
2. **DHCP monitor** — isolated module deletion, low risk
3. **Syslogd KafkaSinkBridge** — single-topic bridge (proven pattern), medium risk
4. **Telemetryd standalone container** — multi-topic bridge + new daemon-loader, highest complexity (done last)

Each step gets its own commit.

## Verification

After each step: `./compile.pl -DskipTests` must succeed.

After all steps:
- `docker compose up -d` with full profile — all 19 services healthy
- Trap E2E test via Minion: `snmptrap` → Minion:11162 → Kafka → Trapd → Alarmd → PostgreSQL (existing test)
- Syslog E2E test via Minion: `logger` or `nc -u` → Minion:1514 → Kafka → Syslogd → event pipeline
- Verify no daemon container binds any external UDP/TCP monitoring port

## Out of Scope

- Webapp HTTP listeners (Jetty) — being replaced separately
- Karaf SSH/HTTP console ports — internal management, not monitoring data
- REST/gRPC event ingestion API — deferred to Spring Boot migration
- Telemetry protocol-specific configuration — Minion telemetry listener config is unchanged
- KSC reports, Grafana integration — unrelated
