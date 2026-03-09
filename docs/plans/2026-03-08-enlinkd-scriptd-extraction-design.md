# Enlinkd & Scriptd Daemon Container Extraction Design

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:writing-plans to create the implementation plan from this design.

**Goal:** Extract Enlinkd and Scriptd from the core container into standalone `opennms/daemon` containers using the proven daemon-loader pattern.

**Context:** Part of the Delta-V Option 3 effort to move core off the 20.4GB `opennms/horizon` image onto the 1.31GB `opennms/daemon` image. Enlinkd and Scriptd are the next two daemons to extract, following the successful extraction of 12 daemons (Alarmd, Pollerd, Collectd, Rtcd, PassiveStatusd, Notifd, Discovery, Trapd, Syslogd, Ticketer, EventTranslator).

---

## Architecture

Both daemons are `AbstractServiceDaemon` subclasses and use `DaemonLifecycleManager` (not `SpringDaemonLifecycleManager`). Each gets a new `core/daemon-loader-<name>/` module with a flat Spring XML context that wires the daemon to OSGi services.

### Container Identity

| Container | TSID Node ID | Image |
|-----------|-------------|-------|
| enlinkd | 14 | `opennms/daemon:strike-fighter` |
| scriptd | 15 | `opennms/daemon:strike-fighter` |

---

## Enlinkd Daemon-Loader

### Daemon Class

`org.opennms.netmgt.enlinkd.EnhancedLinkd` (extends `AbstractServiceDaemon`, implements `ReloadableTopologyDaemon`)

Source: `features/enlinkd/daemon/src/main/java/org/opennms/netmgt/enlinkd/EnhancedLinkd.java`

### Dependencies from OSGi (already available in daemon image)

All enlinkd persistence and service DAOs are shaded into `distributed-dao-impl` via the Maven Shade Plugin's `XmlAppendingTransformer`. They are registered as `<onmsgi:service>` exports and available in every daemon container.

**From distributed-dao-impl (`<onmsgi:reference>`):**
- 17 persistence DAOs: CdpLinkDao, CdpElementDao, LldpLinkDao, LldpElementDao, OspfAreaDao, OspfLinkDao, OspfElementDao, IsIsLinkDao, IsIsElementDao, IpNetToMediaDao, BridgeMacLinkDao, BridgeBridgeLinkDao, BridgeStpLinkDao, BridgeElementDao, TopologyEntityDao, TopologyEntityCache, UserDefinedLinkDao
- 8 topology services: NodeTopologyService, UserDefinedLinkTopologyService, BridgeTopologyService, IpNetToMediaTopologyService, CdpTopologyService, IsisTopologyService, LldpTopologyService, OspfTopologyService
- OnmsTopologyDao (in-memory implementation)
- LocationAwareSnmpClient

**From event-forwarder-kafka blueprint (`<osgi:reference>`):**
- EventIpcManager
- EventSubscriptionService

**From service-registry bundle (`<osgi:reference>`):**
- ServiceRegistry

### Beans wired locally in daemon-loader Spring XML

- `EnhancedLinkdConfigFactory` — static `init()` via `MethodInvokingFactoryBean`, then `factory-method="getInstance"`
- `EnhancedLinkd` daemon bean — properties: linkdConfig, queryManager (nodeTopologyService), bridgeTopologyService, cdpTopologyService, isisTopologyService, ipNetToMediaTopologyService, lldpTopologyService, ospfTopologyService
- 9 topology updater beans: NodesOnmsTopologyUpdater, NetworkRouterTopologyUpdater, UserDefinedLinkTopologyUpdater, BridgeOnmsTopologyUpdater, CdpOnmsTopologyUpdater, IsisOnmsTopologyUpdater, LldpOnmsTopologyUpdater, OspfOnmsTopologyUpdater, OspfAreaOnmsTopologyUpdater
- DiscoveryBridgeDomains bean
- EventProcessor bean (`@EventListener`) with AnnotationBasedEventListenerAdapter
- DaemonLifecycleManager

### MessageBus

Enlinkd's `EventProcessor` has `@Autowired(required=false) MessageBus`. Left unwired — Enlinkd handles null gracefully. Will be connected when network-of-brokers AMQ topology is implemented.

### SNMP Polling

Enlinkd polls devices directly via `LocationAwareSnmpClient`. For nodes in non-default locations, the SNMP API transparently proxies queries to Minion via RPC. No changes needed to Enlinkd's polling logic.

---

## Scriptd Daemon-Loader

### Daemon Class

`org.opennms.netmgt.scriptd.Scriptd` (extends `AbstractServiceDaemon`)

Source: `opennms-services/src/main/java/org/opennms/netmgt/scriptd/Scriptd.java`

### Legacy Wiring Challenge

Scriptd uses two legacy static APIs instead of Spring injection:

1. **`EventIpcManagerFactory.getIpcManager()`** — used by `BroadcastEventProcessor` to subscribe to events
2. **`BeanUtils.getBeanFactory("daoContext")`** — used by `Executor` to look up `NodeDao` and `SessionUtils`

### Solution

**EventIpcManagerFactory:** Call `setIpcManager()` via `MethodInvokingBean` with the `KafkaEventIpcManagerAdapter` from OSGi. Same pattern used in other daemon-loaders.

**BeanUtils shim:** Create a small `ScriptdBeanFactoryInitializer` class in the daemon-loader bundle that registers a minimal `BeanFactory` under the name "daoContext" containing `NodeDao` and `SessionUtils` from OSGi references. This avoids modifying the core `Scriptd` class in `opennms-services`.

### Dependencies from OSGi

**From distributed-dao-impl (`<onmsgi:reference>`):**
- NodeDao
- SessionUtils

**From event-forwarder-kafka blueprint (`<osgi:reference>`):**
- EventIpcManager

### Beans wired locally

- `ScriptdConfigFactory` — static init + `getInstance()`
- `EventIpcManagerFactory.setIpcManager()` via `MethodInvokingBean`
- `ScriptdBeanFactoryInitializer` — registers "daoContext" BeanFactory shim
- `Scriptd` daemon bean — singleton, `setInstance()` via `MethodInvokingBean`
- `DaemonLifecycleManager`

### BSF/Beanshell

BSF and Beanshell JARs are runtime dependencies of `opennms-services` bundle, which has `Import-Package: *;resolution:=optional` and `DynamicImport-Package: *`. Available in the daemon image via Karaf features.

---

## Karaf Features

### `opennms-daemon-enlinkd` (in features.xml)

Dependencies:
- `opennms-event-forwarder-kafka`
- `distributed-dao-impl` (already provides all enlinkd DAOs)
- All enlinkd adapter bundles (collectors, updaters, discovers)
- `opennms-daemon-loader-enlinkd` bundle

### `opennms-daemon-scriptd` (in features.xml)

Dependencies:
- `opennms-event-forwarder-kafka`
- `distributed-dao-impl` (for NodeDao, SessionUtils)
- `opennms-daemon-loader-scriptd` bundle

Both features added to `features-sentinel.xml` and installed in the Sentinel container POM (`features/container/sentinel/pom.xml`).

---

## Docker Compose Changes

### New services

```yaml
enlinkd:
  image: opennms/daemon:strike-fighter
  environment:
    TSID_NODE_ID: 14
    POSTGRES_HOST: postgres
    OPENNMS_INSTANCE_ID: strike-fighter
    KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    FEATURES_BOOT: "opennms-daemon-enlinkd"
  depends_on:
    core: { condition: service_healthy }
    kafka: { condition: service_healthy }

scriptd:
  image: opennms/daemon:strike-fighter
  environment:
    TSID_NODE_ID: 15
    POSTGRES_HOST: postgres
    OPENNMS_INSTANCE_ID: strike-fighter
    KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    FEATURES_BOOT: "opennms-daemon-scriptd"
  depends_on:
    core: { condition: service_healthy }
    kafka: { condition: service_healthy }
```

### Core container disablement

```yaml
CORE_SERVICE_ENLINKD_ENABLED: "false"
CORE_SERVICE_SCRIPTD_ENABLED: "false"
```

### Health ignore list

Both containers need `org.opennms.core.health.cfg.cfg` entries to ignore `opennms-services` and enlinkd bundles whose own Spring contexts conflict with the daemon-loader contexts.

---

## Out of Scope

- Telemetryd extraction (deferred — complex, multiple UDP listeners)
- Network-of-brokers AMQ topology (deferred — independent of daemon extraction)
- Eventd, Provisiond, Bsmd, Correlator (deferred — pending further discussion)
- Modifications to Enlinkd or Scriptd source code

---

## Deferred Design Note: IPC Architecture Gap

The original eventbus-redesign design specifies each JVM embeds an ActiveMQ broker in a network-of-brokers topology for IPC events. Current implementation has a single AMQ broker on core with daemon containers as clients. The IPC/Fault/DUAL classification code (EventClassifier, EventRouter, KafkaEventForwarder) is correctly built. The gap is the embedded broker per container + network connectors. This will be addressed after these daemon extractions are complete.

---

## Verification

After each daemon-loader:
1. `./compile.pl -DskipTests` passes
2. Docker compose starts, container reaches healthy state
3. Enlinkd: topology tables populated in PostgreSQL
4. Scriptd: daemon starts, processes events (if scripts configured)
