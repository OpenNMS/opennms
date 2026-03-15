# Next Session: Spring Boot 4 Migration Planning

## Goal

Plan the migration of Delta-V daemon containers from Apache Karaf (OSGi) to Spring Boot 4. This eliminates OSGi classloader isolation, Karaf bundle caching, ServiceLoader failures, and the entire `features.xml` / blueprint / Spring DM extender infrastructure.

## Why Spring Boot 4

The current Karaf-based architecture creates a wall of OSGi complexity:
- **ServiceLoader doesn't work across bundles** — monitors/collectors can't be discovered
- **Bundle caching** — Karaf persists stale bundle state in data volumes, requiring volume deletion on every JAR update
- **Missing .sha1 files** — Karaf's Maven resolver silently ignores JARs without checksums
- **No Spring-Context header = no wiring** — bundles with Spring XML are dead unless the manifest declares them
- **Missing OSGi services cascade** — EntityScopeProvider, PersisterFactory, etc. each require stubs or graceful fallbacks
- **12+ workaround classes** — InlineProvisiondConfigDao, InlineIdentity, LocalServiceMonitorRegistry, NoOpTracerRegistry, NoOpSnmpProfileMapper, PassiveServiceMonitorRegistrar, etc.

Spring Boot 4 eliminates ALL of this with flat classpath scanning and `@Component`/`@Autowired`.

## Current State

### What Exists
- **17 daemon containers** sharing one `opennms/daemon-deltav` Docker image
- **14 `daemon-loader-*` modules** — each a Karaf OSGi bundle with Spring DM context
- **Karaf features.xml** — `opennms-daemon-pollerd`, `opennms-daemon-trapd`, etc.
- **`features.boot` per service** — selects which Karaf feature to load at startup
- **OSGi service registry** — `onmsgi:reference`, `osgi:reference` for cross-bundle deps
- **Spring DM Extender** — processes `Spring-Context` manifest headers
- **Blueprint XML** — for non-Spring bundles (minion-core-impl, event-forwarder-kafka, etc.)

### What We Built (That Transfers to Spring Boot)
- PassiveStatusHolder (static shared singleton) — works in any framework
- Twin API publisher/subscriber wiring — just needs `@Bean` instead of XML
- InlineProvisiondConfigDao (reads config from filesystem) — becomes a `@Component`
- ImportJob EntityScopeProvider fallback — stays as-is
- PollerRequestBuilderImpl EntityScopeProvider fallback — stays as-is
- All daemon lifecycle logic (start/stop) — `CommandLineRunner` or `SmartLifecycle`

### Docker Compose Services
Each daemon service in docker-compose overrides `KARAF_FEATURE_BOOT` env var to select its feature. With Spring Boot, each would instead activate a Spring profile or run a specific `@SpringBootApplication` main class.

## Scope

### Phase 1: Planning (This Session)
1. Catalog every OSGi/Karaf-specific file and pattern in the codebase
2. Map the Spring DM context XML beans to `@Component`/`@Configuration` equivalents
3. Identify shared infrastructure (Kafka, DAOs, event transport) that all daemons need
4. Design the Spring Boot module structure (one module per daemon? shared base?)
5. Plan the migration order (which daemon first?)

### Phase 2: Prototype (Next Session)
- Pick one daemon (suggest: Trapd — simplest, few dependencies)
- Create a Spring Boot 4 application that replaces the Karaf daemon-loader
- Prove it works in Docker with the same compose setup
- Establish the pattern for the remaining 16 daemons

## Key Design Questions

1. **One Spring Boot app per daemon or one app with profiles?**
   - Per-daemon: clean separation, independent deployment, but 17 JAR/image builds
   - Shared app with profiles: one build, select daemon at runtime via `SPRING_PROFILES_ACTIVE`
   - The current architecture already uses one image with runtime feature selection

2. **How to handle the DAO layer?**
   - Currently: OSGi services registered by `dao-impl` bundle, consumed via `onmsgi:reference`
   - Spring Boot: `@ComponentScan` finds DAOs, `@Autowired` injects them
   - Hibernate/JPA config needs to move from OSGi-specific to standard Spring Boot auto-config

3. **How to handle Kafka transport?**
   - Currently: KafkaRpcClientFactory, KafkaEventForwarder — Spring beans in daemon-loader contexts
   - Spring Boot: same classes, just `@Bean` methods in a `@Configuration` class
   - The `kafka-rpc-client-factory.xml` shared fragment becomes a `@Configuration` class

4. **What about the Minion?**
   - Minion is ALSO Karaf-based. Migrate it too? Or later?
   - Minion has a different architecture (feature-based, not daemon-loader-based)
   - Could be a separate phase

5. **What Spring Boot version?**
   - Spring Boot 4 requires Spring Framework 7 + Jakarta EE (javax → jakarta)
   - Hibernate 3.6 → Hibernate 6.x migration (significant)
   - Or start with Spring Boot 3.x first (Spring Framework 6, still Jakarta EE)

## Files to Catalog

### Per-Daemon (14 daemon-loaders)
```
core/daemon-loader-pollerd/
  src/main/resources/META-INF/opennms/applicationContext-daemon-loader-pollerd.xml
  src/main/resources/kafka-rpc-client-factory.xml
  src/main/java/org/opennms/core/daemon/loader/*.java  (workaround classes)

core/daemon-loader-trapd/
  ... (same pattern)
```

### Shared Infrastructure
```
container/features/src/main/resources/features.xml          → Spring Boot starters
opennms-container/delta-v/daemon-overlay/                    → features.xml overlay
opennms-container/delta-v/*/featuresBoot.d/*.boot            → Spring profiles
features/events/daemon/                                      → Event transport
core/ipc/rpc/kafka/                                          → Kafka RPC
core/ipc/twin/kafka/                                         → Twin API
```

### OSGi-Specific (DELETE in Spring Boot)
```
**/OSGI-INF/blueprint/*.xml                                  → @Configuration
**/META-INF/services/*                                       → @Component scan
features.xml feature definitions                             → Spring Boot starters
Spring-Context manifest headers                              → @SpringBootApplication
onmsgi:reference / osgi:reference                            → @Autowired
```

## Branch and Remote

- **Branch:** new branch from `develop` (post-PR #24 merge)
- **Remote:** push to `delta-v` (pbrane/delta-v)
- **Base branch for PRs:** `develop`
