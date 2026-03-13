# Minion-Mandatory RPC Migration Design

## Goal

Replace all Local*Client stubs in daemon containers with real `LocationAware*ClientImpl` RPC implementations backed by `KafkaRpcClientFactory`, making Minion delegation mandatory for all polling, collection, detection, SNMP, ping, and DNS operations. Add a `force-remote` flag to `KafkaRpcClientFactory` so daemon containers never execute RPC locally.

## Architecture

All daemon containers currently use local stub implementations (e.g., `LocalPollerClient`, `LocalSnmpClient`) that execute operations in-process, ignoring the location parameter. This means services assigned to non-Default locations are polled/collected/scanned from the wrong vantage point.

The migration replaces these stubs with the real `LocationAware*ClientImpl` classes that delegate operations to Minions via Kafka RPC. A shared Spring XML fragment provides the common `KafkaRpcClientFactory` wiring, and a new `force-remote` system property ensures daemon containers never short-circuit to local execution.

**PerspectivePollerd** (already migrated) serves as the reference implementation.

## Tech Stack

- Spring XML (daemon-loader pattern)
- KafkaRpcClientFactory (Kafka RPC client)
- OSGi bundles (Karaf feature assembly)
- Docker Compose (daemon container configuration)

---

## Component Design

### 1. Shared XML Fragment Module (`core/daemon-loader-shared/`)

A new OSGi bundle module containing a reusable Spring XML fragment that all daemon-loaders import via `<import resource="classpath:kafka-rpc-client-factory.xml"/>`.

**Fragment contents** (`kafka-rpc-client-factory.xml`):

```xml
<!-- Identity resolution from DistPollerDao -->
<bean id="rpcIdentity" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
    <property name="targetObject" ref="distPollerDao"/>
    <property name="targetMethod" value="whoami"/>
</bean>
<bean id="rpcLocation" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
    <property name="targetObject" ref="rpcIdentity"/>
    <property name="targetMethod" value="getLocation"/>
</bean>

<!-- Shared infrastructure -->
<bean id="kafkaRpcMetricRegistry" class="com.codahale.metrics.MetricRegistry"/>
<bean id="rpcTargetHelper" class="org.opennms.core.rpc.utils.RpcTargetHelper"/>

<!-- No-op tracer (KafkaRpcClientFactory has @Autowired TracerRegistry) -->
<bean id="tracerRegistry"
      class="org.opennms.core.daemon.loader.NoOpTracerRegistry"/>

<!-- KafkaRpcClientFactory -->
<bean id="rpcClientFactory"
      class="org.opennms.core.ipc.rpc.kafka.KafkaRpcClientFactory"
      init-method="start" destroy-method="stop">
    <property name="location" ref="rpcLocation"/>
    <property name="metrics" ref="kafkaRpcMetricRegistry"/>
</bean>
```

**Contract**: The importing daemon-loader context must provide:
- `distPollerDao` bean via `<onmsgi:reference interface="org.opennms.netmgt.dao.api.DistPollerDao"/>` — currently only PerspectivePollerd and Provisiond have this; **Pollerd, Collectd, Discovery, and Enlinkd must add it**

The fragment itself provides: `rpcIdentity`, `rpcLocation`, `kafkaRpcMetricRegistry`, `rpcTargetHelper`, `tracerRegistry`, `rpcClientFactory`.

**Java classes in this module**:
- `NoOpTracerRegistry` — moved here from `daemon-loader-perspectivepoller` and `daemon-loader-provisiond` (consolidate the duplicate copies)

**POM**: OSGi bundle packaging, depends on `core/ipc/rpc/kafka`, `core/rpc/utils`, `opennms-dao-api`, `metrics`, `opennms-javautil` (for TracerRegistry).

### 2. Force-Remote Flag in KafkaRpcClientFactory

A system property `org.opennms.core.ipc.rpc.force-remote` that controls line 164's local-execution shortcut in `KafkaRpcClientFactory.getClient()`:

```java
// Current behavior:
if (request.getLocation() == null || request.getLocation().equals(location)) {
    return module.execute(request);
}

// New behavior:
boolean forceRemote = Boolean.getBoolean("org.opennms.core.ipc.rpc.force-remote");
if (!forceRemote && (request.getLocation() == null || request.getLocation().equals(location))) {
    return module.execute(request);
}

// Null location defaulting (required for force-remote topic routing):
String effectiveLocation = request.getLocation();
if (effectiveLocation == null) {
    effectiveLocation = location; // daemon's own location (e.g., "Default")
}
String requestTopic = topicProvider.getRequestTopicAtLocation(effectiveLocation, module.getId());
```

**Null location handling**: When `force-remote=true` and `request.getLocation()` is null, `effectiveLocation` defaults to the daemon's own location (from `DistPollerDao.whoami().getLocation()`). This ensures `topicProvider.getRequestTopicAtLocation()` receives a non-null location for Kafka topic routing. The existing non-force-remote path already handles this case by executing locally.

**Who sets it**: All daemon containers add `-Dorg.opennms.core.ipc.rpc.force-remote=true` to JAVA_OPTS. Minion does not set it (preserves local execution for same-location requests).

### 3. Provisiond Migration (3 clients)

Replace `LocalSnmpClient`, `LocalDetectorClient`, `LocalDnsLookupClient` with real RPC implementations.

**Beans to add**:

| Bean | Class | Satisfies |
|------|-------|-----------|
| `locationAwareSnmpClient` | `LocationAwareSnmpClientRpcImpl` | `@Autowired RpcClientFactory` (from shared fragment) + `SnmpProxyRpcModule.INSTANCE` (static singleton) |
| `serviceDetectorRegistry` | `LocalServiceDetectorRegistry` (new) | `@Autowired ServiceDetectorRegistry` in DetectorClientRpcModule |
| `scanExecutor` | `Executors.newCachedThreadPool()` | `@Autowired @Qualifier("scanExecutor") Executor` in DetectorClientRpcModule |
| `detectorClientRpcModule` | `DetectorClientRpcModule` | `@Autowired` in LocationAwareDetectorClientRpcImpl |
| `locationAwareDetectorClient` | `LocationAwareDetectorClientRpcImpl` | `@Autowired RpcClientFactory` + `@Autowired EntityScopeProvider` (already present) |
| `dnsLookupClientRpcModule` | `DnsLookupClientRpcModule(4)` | Constructor arg: threadCount=4 |
| `locationAwareDnsLookupClient` | `LocationAwareDnsLookupClientRpcImpl` | `@Autowired RpcClientFactory` + `@Autowired DnsLookupClientRpcModule` |

**Beans to remove**: `locationAwareSnmpClient` (LocalSnmpClient), `locationAwareDetectorClient` (LocalDetectorClient), `locationAwareDnsLookupClient` (LocalDnsLookupClient)

**Additional context changes**:
- Add `<import resource="classpath:kafka-rpc-client-factory.xml"/>` (shared fragment)
- Add `<onmsgi:reference id="distPollerDao" interface="org.opennms.netmgt.dao.api.DistPollerDao"/>` (required by shared fragment — **not currently present**)
- Remove the now-redundant `tracerRegistry` bean (provided by shared fragment)

**Cleanup**: Delete `LocalSnmpClient.java`, `LocalDetectorClient.java`, `LocalDnsLookupClient.java` from `daemon-loader-provisiond`.

### 4. Discovery Migration (2 clients)

Replace `LocalLocationAwarePingClient` and wire detector client directly.

**Beans to add**:

| Bean | Class | Satisfies |
|------|-------|-----------|
| `pingerFactory` | `BestMatchPingerFactory` | `@Autowired PingerFactory` in PingProxyRpcModule (JNA, pure Java — with `force-remote=true` never actually invoked, but Spring needs it for injection) |
| `pingProxyRpcModule` | `PingProxyRpcModule` | `@Autowired` in LocationAwarePingClientImpl |
| `pingSweepRpcModule` | `PingSweepRpcModule` | `@Autowired` in LocationAwarePingClientImpl |
| `locationAwarePingClient` | `LocationAwarePingClientImpl` | `@Autowired RpcClientFactory` (from shared fragment) |
| `serviceDetectorRegistry` | `LocalServiceDetectorRegistry` | `@Autowired ServiceDetectorRegistry` in DetectorClientRpcModule |
| `scanExecutor` | `Executors.newCachedThreadPool()` | `@Autowired @Qualifier("scanExecutor") Executor` in DetectorClientRpcModule |
| `detectorClientRpcModule` | `DetectorClientRpcModule` | `@Autowired` in LocationAwareDetectorClientRpcImpl |
| `locationAwareDetectorClient` | `LocationAwareDetectorClientRpcImpl` | `@Autowired RpcClientFactory` + `@Autowired EntityScopeProvider` |
| `entityScopeProvider` | `onmsgi:reference` | Discovery does not currently have this — must add for LocationAwareDetectorClientRpcImpl |

**Beans to remove**: `locationAwarePingClient` (LocalLocationAwarePingClient), `onmsgi:reference` for `LocationAwareDetectorClient`

**Additional context changes**:
- Add `<import resource="classpath:kafka-rpc-client-factory.xml"/>` (shared fragment)
- Add `<onmsgi:reference id="distPollerDao" interface="org.opennms.netmgt.dao.api.DistPollerDao"/>` (required by shared fragment)
- Note: Discovery's detector support is slated for future removal, but wire properly for now

**Cleanup**: Delete `LocalLocationAwarePingClient.java` from `daemon-loader-discovery`.

### 5. Pollerd Migration (1 client)

Replace `LocalPollerClient` with `LocationAwarePollerClientImpl`.

**Beans to add**:

| Bean | Class | Satisfies |
|------|-------|-----------|
| `pollerClientRpcModule` | `PollerClientRpcModule` | `@Autowired ServiceMonitorRegistry` (existing) + `@Autowired @Qualifier("pollerExecutor") Executor` (existing) |
| `locationAwarePollerClient` | `LocationAwarePollerClientImpl` | `@Autowired RpcClientFactory` (from shared fragment) + `@Autowired RpcTargetHelper` (from shared fragment) + `@Autowired EntityScopeProvider` |

**Beans to remove**: `locationAwarePollerClient` (LocalPollerClient)

**Additional context changes**:
- Add `<import resource="classpath:kafka-rpc-client-factory.xml"/>` (shared fragment)
- Add `<onmsgi:reference id="distPollerDao" interface="org.opennms.netmgt.dao.api.DistPollerDao"/>` (required by shared fragment — **not currently present**)
- Add `<osgi:reference id="filterDao" interface="org.opennms.netmgt.filter.api.FilterDao"/>` (FilterDaoFactory/PollerConfigFactory race guard — **not currently present**)
- Add `<onmsgi:reference id="entityScopeProvider" interface="org.opennms.core.mate.api.EntityScopeProvider"/>` (**not currently present** — needed by LocationAwarePollerClientImpl for MATE variable interpolation)
- Remove the now-redundant `tracerRegistry` bean if present (provided by shared fragment)
- Keep `<onmsgi:service interface="LocationAwarePollerClient" ref="locationAwarePollerClient"/>` — the new bean implements the same interface

**Cleanup**: Delete `LocalPollerClient.java`, `LocalPollerRequestBuilder.java` from `daemon-loader-pollerd`.

### 6. Collectd Migration (1 client)

Replace `LocalCollectorClient` with `LocationAwareCollectorClientImpl`.

**Beans to add**:

| Bean | Class | Satisfies |
|------|-------|-----------|
| `collectorClientRpcModule` | `CollectorClientRpcModule` | `@Autowired ServiceCollectorRegistry` (existing) + `@Autowired @Qualifier("collectorExecutor") Executor` (existing) |
| `locationAwareCollectorClient` | `LocationAwareCollectorClientImpl` | `@Autowired RpcClientFactory` (from shared fragment) + `@Autowired RpcTargetHelper` (from shared fragment) + `@Autowired EntityScopeProvider` |

**Beans to remove**: `locationAwareCollectorClient` (LocalCollectorClient)

**Additional context changes**:
- Add `<import resource="classpath:kafka-rpc-client-factory.xml"/>` (shared fragment)
- Add `<onmsgi:reference id="distPollerDao" interface="org.opennms.netmgt.dao.api.DistPollerDao"/>` (required by shared fragment — **not currently present**)
- Add `<onmsgi:reference id="entityScopeProvider" interface="org.opennms.core.mate.api.EntityScopeProvider"/>` (**not currently present** — needed by LocationAwareCollectorClientImpl)
- Keep `<onmsgi:service interface="LocationAwareCollectorClient" ref="locationAwareCollectorClient"/>` — the new bean implements the same interface

**Cleanup**: Delete `LocalCollectorClient.java`, `LocalCollectorRequestBuilder.java` from `daemon-loader-collectd`.

### 7. Enlinkd Migration (1 client)

Replace `onmsgi:reference` for `LocationAwareSnmpClient` with direct `LocationAwareSnmpClientRpcImpl` bean.

**Why the onmsgi:reference doesn't work**: The `core/snmp/proxy-rpc-impl` bundle's Spring context (`applicationContext-rpc-snmp.xml`) has no `Spring-Context` header, so it never loads in Karaf. The `LocationAwareSnmpClientRpcImpl` is never created and never registered. Enlinkd's `onmsgi:reference` resolves to an empty proxy.

**Beans to add**:

| Bean | Class | Satisfies |
|------|-------|-----------|
| `locationAwareSnmpClient` | `LocationAwareSnmpClientRpcImpl` | `@Autowired RpcClientFactory` (from shared fragment) + `SnmpProxyRpcModule.INSTANCE` (static singleton) |

**Beans to remove**: `onmsgi:reference` for `LocationAwareSnmpClient`

**Additional context changes**:
- Add `<import resource="classpath:kafka-rpc-client-factory.xml"/>` (shared fragment)
- Add `<onmsgi:reference id="distPollerDao" interface="org.opennms.netmgt.dao.api.DistPollerDao"/>` (required by shared fragment — **not currently present**)

**Cleanup**: No files to delete (Enlinkd had no Local*Client stubs — just an empty onmsgi:reference proxy).

### 8. PerspectivePollerd Retrofit

Replace PerspectivePollerd's inline KafkaRpcClientFactory block (~20 lines: identity, location, MetricRegistry, rpcTargetHelper, rpcClientFactory beans) with:

```xml
<import resource="classpath:kafka-rpc-client-factory.xml"/>
```

Add `-Dorg.opennms.core.ipc.rpc.force-remote=true` to its JAVA_OPTS.

### 9. Docker Compose Changes

Add to JAVA_OPTS for all 6 RPC-using daemons (provisiond, discovery, pollerd, collectd, enlinkd, perspectivepollerd):

```
-Dorg.opennms.core.ipc.rpc.kafka.bootstrap.servers=kafka:9092
-Dorg.opennms.core.ipc.rpc.force-remote=true
```

Daemons that do NOT use RPC (alarmd, trapd, syslogd, eventtranslator, ticketer, scriptd, bsmd, rtcd) are unchanged.

---

## Migration Order

1. **Shared infrastructure first**: `core/daemon-loader-shared/` module + `KafkaRpcClientFactory` force-remote flag
2. **Provisiond** — most complex (3 clients), highest impact, proven with Minion E2E
3. **Discovery** — 2 clients (ping + detector)
4. **Pollerd** — 1 client, mirrors PerspectivePollerd
5. **Collectd** — 1 client, same pattern
6. **Enlinkd** — 1 client, onmsgi:reference → direct bean
7. **PerspectivePollerd retrofit** — replace inline block with shared import

## New Classes Required

| Class | Module | Purpose |
|-------|--------|---------|
| `NoOpTracerRegistry` | `daemon-loader-shared` | Moved from daemon-loader-perspectivepoller and daemon-loader-provisiond (consolidate duplicates) |
| `LocalServiceDetectorRegistry` | `daemon-loader-provisiond` (also used by daemon-loader-discovery) | Discovers ServiceDetector implementations via ServiceLoader (same pattern as LocalServiceMonitorRegistry) |

## Karaf Feature Assembly Changes

The `core/daemon-loader-shared` module must be added to:
- `container/features/src/main/resources/features.xml` — as a bundle in each daemon feature that uses RPC (or as a shared dependency feature)
- `features/container/sentinel/pom.xml` — in `<installedFeatures>` so Maven places the JAR in `system/`

Each daemon Karaf feature that imports the shared fragment must declare `<bundle>mvn:org.opennms.core/daemon-loader-shared/${project.version}</bundle>` (or depend on a shared feature that includes it).

## Classes to Delete

| Class | Module |
|-------|--------|
| `LocalSnmpClient` | `daemon-loader-provisiond` |
| `LocalDetectorClient` | `daemon-loader-provisiond` |
| `LocalDnsLookupClient` | `daemon-loader-provisiond` |
| `LocalLocationAwarePingClient` | `daemon-loader-discovery` |
| `LocalPollerClient` | `daemon-loader-pollerd` |
| `LocalPollerRequestBuilder` | `daemon-loader-pollerd` |
| `LocalCollectorClient` | `daemon-loader-collectd` |
| `LocalCollectorRequestBuilder` | `daemon-loader-collectd` |

## Verification Strategy

**Per-daemon verification**:
1. Build chain: daemon-loader module → container/features → sentinel → daemon assembly → docker image
2. Container health: `docker compose up -d --force-recreate <service>` → health probe `"Everything is awesome"`
3. Log verification: `karaf.log` shows KafkaRpcClientFactory initialized with `bootstrap.servers=kafka:9092`, no OSGi resolution errors

**End-to-end validation (after all migrations)**:
- Run `test-minion-e2e.sh` — verify trap→newSuspect→Provisiond→alarm flow works with Minion RPC
- Verify Provisiond node scan delegates SNMP walks to Minion (check Minion logs for SNMP RPC requests)
- Verify Discovery pings route through Minion (check Minion logs for PING RPC requests)

## Risk Considerations

- **Build chain duration**: Each daemon migration requires full rebuild (~10-15 min). Plan for 5 cycles minimum.
- **PingerFactory in containers**: `BestMatchPingerFactory` satisfies `@Autowired` in `PingProxyRpcModule`. With `force-remote=true`, local ICMP is never invoked — the bean exists solely for dependency injection.
- **Null location in force-remote mode**: Must default null locations to the daemon's own location for Kafka topic routing. Without this, `topicProvider.getRequestTopicAtLocation(null, moduleId)` would fail.
- **Enlinkd's resilient wiring**: Enlinkd bundles use `Import-Package: *;resolution:=optional` + `DynamicImport-Package: *`. After migration, verify SNMP operations actually work (not just silently fail).
