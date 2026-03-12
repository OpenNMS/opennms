# PerspectivePollerd Migration & Dead Daemon Cleanup — Design

**Date:** 2026-03-12
**Branch:** `eventbus-redesign`

## Context

The Strike Fighter plan (18/18 tasks), Phase A core elimination (7/7 tasks), EventDao/Notifd/Minion REST elimination, and Minion E2E pipeline are all complete. Three cleanup items remain from the project status analysis:

1. **PerspectivePollerd migration** — Extract to standalone daemon container (TSID=7 reserved)
2. **Remove dead eventconf definitions for promoteQueueData UEI** — Already done (commit `2dda3efde6a`)
3. **Verify no Queued references remain in JMX datacollection configs** — Already done; only `core/upgrade/` test snapshots remain (correct to keep)

Additionally, the dead **Notifd mbean** in `jmx-datacollection-config.xml` (lines 57-68) needs removal — Notifd was eliminated but its JMX monitoring definition was missed.

## Scope

| Item | Status | Action |
|------|--------|--------|
| PerspectivePollerd daemon container | Not started | Full daemon-loader extraction |
| promoteQueueData eventconf | Already removed | None — verify only |
| Queued JMX datacollection | Already removed from production | None — verify only |
| Dead Notifd mbean in JMX config | Orphaned | Delete mbean block |

## PerspectivePollerd Migration

### What PerspectivePollerd Does

PerspectivePollerd polls services from multiple geographic locations ("perspectives") using Minion RPC. It tracks service availability per-perspective, creates outages, persists response-time metrics, and sends perspective-specific lost/regained events. Unlike regular Pollerd (which polls from a single location), PerspectivePollerd dispatches polls to remote Minions via `LocationAwarePollerClient`.

### Current State

- **Source:** `features/perspectivepoller/` (PerspectivePollerd.java, PerspectiveServiceTracker.java, PerspectivePollJob.java)
- **Spring XML:** `applicationContext-perspectivePollerDaemon.xml` (11 `onmsgi:reference` beans)
- **Lifecycle:** Implements `SpringServiceDaemon` (not `AbstractServiceDaemon`)
- **EventDao:** Already clean — no EventDao references, uses event TSID+UEI pattern
- **Docker Compose:** Disabled in webapp (`CORE_SERVICE_PERSPECTIVEPOLLER_ENABLED: "false"`), no standalone container
- **Karaf feature:** `opennms-perspective-poller` exists in features.xml (line 1872, single bundle) but NOT in Sentinel assembly `<installedFeatures>`
- **TSID:** Node-id 7 reserved

### Architecture Decision: LocationAwarePollerClient via Kafka RPC

PerspectivePollerd requires the real `LocationAwarePollerClient` (not a local stub like Pollerd uses). This client delegates polls to Minions at perspective locations via Kafka RPC. Without multi-location polling, PerspectivePollerd has no purpose.

**How LocationAwarePollerClient is wired:**
- `LocationAwarePollerClientImpl` is defined in `features/poller/client-rpc/src/main/resources/META-INF/opennms/applicationContext-rpc-poller.xml`
- It's registered via `<onmsgi:service>` (NOT Blueprint) — so consumers must use `onmsgi:reference`
- `LocationAwarePollerClientImpl` depends on `RpcClientFactory` via `@Autowired`
- `RpcClientFactory` is provided by the `opennms-core-ipc-rpc-kafka` Karaf feature (registered via Blueprint in the OSGi Framework registry)

**Approach:** Wire `LocationAwarePollerClientImpl` and its `PollerClientRpcModule` directly in the daemon-loader Spring XML. The `poller.client-rpc` bundle has no `Spring-Context` manifest header (Spring-DM extender won't auto-discover it in Karaf), so we include it as a bundle and create the beans explicitly in the daemon-loader context. `RpcClientFactory` is obtained via `osgi:reference` from the Kafka RPC Blueprint.

### Daemon-Loader Module

**New module:** `core/daemon-loader-perspectivepoller/`

Flat Spring XML context following the established daemon-loader pattern.

**Dependencies from OSGi registries:**

| Dependency | Source | Registry Type | Notes |
|---|---|---|---|
| `SessionUtils` | distributed-dao-impl | `onmsgi:reference` | |
| `MonitoringLocationDao` | distributed-dao-impl | `onmsgi:reference` | |
| `MonitoredServiceDao` | distributed-dao-impl | `onmsgi:reference` | |
| `ApplicationDao` | distributed-dao-impl | `onmsgi:reference` | |
| `OutageDao` | distributed-dao-impl | `onmsgi:reference` | |
| `PersisterFactory` | timeseries feature | `onmsgi:reference` | |
| `ThresholdingService` | thresholding feature | `onmsgi:reference` | |
| `ServiceRegistry` | service-registry module | `osgi:reference` | Required by all `onmsgi:reference` proxies to resolve services |
| `EventIpcManager` | event-forwarder-kafka Blueprint | `osgi:reference` | Also serves as EventForwarder and EventSubscriptionService |
| `RpcClientFactory` | opennms-core-ipc-rpc-kafka Blueprint | `osgi:reference` | For LocationAwarePollerClientImpl |
| `EntityScopeProvider` | distributed-dao-impl | `onmsgi:reference` | For LocationAwarePollerClientImpl metadata interpolation |
| `PlatformTransactionManager` | distributed-dao-impl | `onmsgi:reference` | For `<tx:annotation-driven/>` |

**Local beans (created in daemon-loader XML):**

| Bean | Class | Notes |
|---|---|---|
| `pollerConfig` | `PollerConfigFactory` | Local factory init + getInstance pattern |
| `locationAwarePollerClient` | `LocationAwarePollerClientImpl` | Wired with `RpcClientFactory` from `osgi:reference` |
| `pollerClientRpcModule` | `PollerClientRpcModule` | Required by LocationAwarePollerClientImpl |
| `pollerExecutor` | `Executors.newCachedThreadPool()` | Required by PollerClientRpcModule via `@Qualifier("pollerExecutor")` |
| `serviceMonitorRegistry` | `LocalServiceMonitorRegistry` | Required by LocationAwarePollerClientImpl for ServiceMonitorLocator lookup |
| `rpcTargetHelper` | `RpcTargetHelper` | Required by LocationAwarePollerClientImpl for RPC routing (no-arg constructor, optional ServiceRegistry autowire) |
| `collectionAgentFactory` | `DefaultSnmpCollectionAgentFactory` | Constructor parameter (unused at runtime but required by signature) |
| `tracerRegistry` | `NoOpTracerRegistry` | Local no-op stub (same as Provisiond) |
| `tracker` | `PerspectiveServiceTracker` | Local bean |
| `daemon` | `PerspectivePollerd` | The daemon itself, constructor-injected via `@Autowired` |
| `serviceTrackerListener` | `AnnotationBasedEventListenerAdapter` | Wires tracker to EventSubscriptionService |
| `daemonListener` | `AnnotationBasedEventListenerAdapter` | Wires daemon to EventSubscriptionService |
| `daemonLifecycleManager` | `SpringDaemonLifecycleManager` | Calls start/stop (SpringServiceDaemon lifecycle) |

**Event subscription wiring:**
- `AnnotationBasedEventListenerAdapter` for both `PerspectivePollerd` (daemon) and `PerspectiveServiceTracker` (tracker)
- `EventSubscriptionService` property set to `eventIpcManager` ref (EventIpcManager implements EventSubscriptionService)
- `<alias name="eventIpcManager" alias="eventForwarder"/>` — PerspectivePollerd's constructor expects `EventForwarder`; `KafkaEventIpcManagerAdapter` implements both `EventIpcManager` and `EventForwarder`, so an alias satisfies the `@Autowired` match by bean name
- `<alias name="eventIpcManager" alias="eventSubscriptionService"/>` — similarly for `EventSubscriptionService` used by `AnnotationBasedEventListenerAdapter`

**Lifecycle:** `SpringDaemonLifecycleManager` (not `DaemonLifecycleManager`) because PerspectivePollerd implements `SpringServiceDaemon`.

**POM configuration:**
- `packaging: bundle`
- All deps `scope: provided`
- Maven Bundle Plugin with:
  - `Import-Package: *;resolution:=optional`
  - `DynamicImport-Package: *`
  - `Spring-Context: META-INF/opennms/*.xml;publish-context:=false;create-asynchronously:=true`
- **Must add `<module>daemon-loader-perspectivepoller</module>` to `core/pom.xml`**

### Karaf Feature

```xml
<feature name="opennms-daemon-perspectivepoller" version="${project.version}"
         description="OpenNMS :: Daemon Loader :: PerspectivePollerd">
    <!-- Spring DM Extender (processes Spring-Context manifest headers) -->
    <feature>opennms-spring-extender</feature>
    <!-- Event transport (Kafka-based, replaces Eventd) -->
    <feature>opennms-event-forwarder-kafka</feature>
    <!-- DAO infrastructure -->
    <feature>opennms-distributed-core-impl</feature>
    <feature>opennms-persistence</feature>
    <!-- Poller + daemon config -->
    <feature>opennms-config</feature>
    <!-- Core daemon (SpringServiceDaemon, DaemonTools) -->
    <feature>opennms-core-daemon</feature>
    <!-- Kafka RPC (provides RpcClientFactory for LocationAwarePollerClient) -->
    <feature>opennms-core-ipc-rpc-kafka</feature>
    <!-- API bundles needed by onmsgi:reference proxies (class loading) -->
    <bundle>mvn:org.opennms.features.collection/org.opennms.features.collection.thresholding.api/${project.version}</bundle>
    <bundle>mvn:org.opennms.features.collection/org.opennms.features.collection.api/${project.version}</bundle>
    <bundle>mvn:org.opennms/opennms-icmp-api/${project.version}</bundle>
    <bundle>mvn:org.opennms.features.poller/org.opennms.features.poller.api/${project.version}</bundle>
    <bundle>mvn:org.opennms.features.distributed/org.opennms.features.distributed.kv-store.api/${project.version}</bundle>
    <bundle>mvn:org.opennms/org.opennms.config-dao.poll-outages.api/${project.version}</bundle>
    <bundle>mvn:org.opennms.core.mate/org.opennms.core.mate.api/${project.version}</bundle>
    <!-- Poller RPC client (LocationAwarePollerClientImpl, PollerClientRpcModule) -->
    <bundle>mvn:org.opennms.features.poller/org.opennms.features.poller.client-rpc/${project.version}</bundle>
    <!-- Poller daemon classes (ServiceMonitor impls, DaemonTools) -->
    <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
    <!-- PerspectivePoller feature bundle -->
    <bundle>mvn:org.opennms.features/org.opennms.features.perspectivepoller/${project.version}</bundle>
    <!-- Daemon loader (creates PerspectivePollerd, starts it) -->
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-perspectivepoller/${project.version}</bundle>
</feature>
```

Key differences vs `opennms-daemon-pollerd`:
- Adds `opennms-core-ipc-rpc-kafka` feature (provides `RpcClientFactory` for Minion RPC)
- Adds `poller.client-rpc` bundle (provides `LocationAwarePollerClientImpl`)
- Does NOT include `LocalPollerClient` (uses real RPC via LocationAwarePollerClientImpl instead)
- Still includes `LocalServiceMonitorRegistry` (needed by PollerRequestBuilderImpl for monitor lookup on client side)

### Sentinel Assembly

Add `opennms-daemon-perspectivepoller` to `<installedFeatures>` in `features/container/sentinel/pom.xml` so Maven places all required JARs in `system/`.

### Docker Compose Service

```yaml
perspectivepollerd:
  image: opennms/daemon:delta-v
  container_name: delta-v-perspectivepollerd
  hostname: perspectivepollerd
  profiles: [full]
  environment:
    JAVA_OPTS: >-
      -Xmx512m -Xms256m
      -Dorg.opennms.tsid.node-id=7
    CORE_SERVICE_PERSPECTIVEPOLLER_ENABLED: "true"
    KAFKA_IPC_BOOTSTRAP_SERVERS: kafka:9092
    POSTGRES_HOST: postgres
    POSTGRES_PORT: "5432"
    POSTGRES_USER: opennms
    POSTGRES_PASSWORD: opennms
    POSTGRES_DB: opennms
  volumes:
    - perspectivepollerd-data:/opt/sentinel/data
    - ./perspectivepollerd-overlay/etc:/opt/sentinel-etc-overlay:ro
    # JAR overlays for dev iteration
    - ../../core/event-forwarder-kafka/target/org.opennms.core.event-forwarder-kafka-36.0.0-SNAPSHOT.jar:/opt/sentinel/system/org/opennms/core/org.opennms.core.event-forwarder-kafka/36.0.0-SNAPSHOT/org.opennms.core.event-forwarder-kafka-36.0.0-SNAPSHOT.jar:ro
    - ../../container/features/target/classes/features.xml:/opt/sentinel/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml:ro
    - ../../features/events/daemon/target/org.opennms.features.events.daemon-36.0.0-SNAPSHOT.jar:/opt/sentinel/system/org/opennms/features/events/org.opennms.features.events.daemon/36.0.0-SNAPSHOT/org.opennms.features.events.daemon-36.0.0-SNAPSHOT.jar:ro
  depends_on:
    kafka: { condition: service_healthy }
    postgres: { condition: service_healthy }
    db-init: { condition: service_completed_successfully }
  healthcheck:
    test: ["CMD-SHELL", "curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe || exit 1"]
    interval: 30s
    timeout: 10s
    retries: 10
    start_period: 120s
```

**Container overlay** (`opennms-container/delta-v/perspectivepollerd-overlay/`):
- `etc/featuresBoot.d/perspectivepoller.boot` → `opennms-daemon-perspectivepoller`
- `etc/org.opennms.core.health.cfg.cfg` — health ignore for known-failing bundles (distributed.datasource, core-impl, dao-impl pattern)

**Named volume:** Add `perspectivepollerd-data:` to the `volumes:` section at bottom of compose file.

**Service count:** 17 → 18 services.

## Dead Notifd Mbean Cleanup

Remove the `OpenNMS.Notifd` mbean block (lines 57-68) from `opennms-base-assembly/src/main/filtered/etc/jmx-datacollection-config.xml`. Notifd was fully eliminated — its JMX MBean no longer exists, so this monitoring definition is dead.

## Verification

After all changes, confirm:
1. No references to deleted daemons (Notifd, Queued, Vacuumd, Statsd, Actiond, Ackd) in `opennms-base-assembly/src/main/filtered/etc/` active configs
2. `promoteQueueData` absent from active eventconf and SQL bootstrap
3. PerspectivePollerd container starts healthy in Docker Compose

## Files Modified/Created

**Created:**
- `core/daemon-loader-perspectivepoller/pom.xml`
- `core/daemon-loader-perspectivepoller/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-perspectivepoller.xml`
- `opennms-container/delta-v/perspectivepollerd-overlay/etc/featuresBoot.d/perspectivepoller.boot`
- `opennms-container/delta-v/perspectivepollerd-overlay/etc/org.opennms.core.health.cfg.cfg`

**Modified:**
- `core/pom.xml` — add `<module>daemon-loader-perspectivepoller</module>`
- `container/features/src/main/resources/features.xml` — add `opennms-daemon-perspectivepoller` feature
- `features/container/sentinel/pom.xml` — add to `<installedFeatures>`
- `opennms-container/delta-v/docker-compose.yml` — add perspectivepollerd service + volume
- `opennms-base-assembly/src/main/filtered/etc/jmx-datacollection-config.xml` — remove Notifd mbean
