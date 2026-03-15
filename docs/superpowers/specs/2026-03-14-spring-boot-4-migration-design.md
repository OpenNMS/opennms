# Spring Boot 4 Migration Design

## Overview

Migrate the 15 Delta-V daemon containers (14 active + RTCd which is deleted) from Apache Karaf (OSGi) to Spring Boot 4.0.3. This eliminates OSGi classloader isolation, Karaf bundle caching, ServiceLoader failures, Spring-DM Extender, `features.xml`, Blueprint XML, and 12+ workaround classes that exist solely to bridge OSGi gaps.

## Goals

- Replace Karaf/OSGi runtime with Spring Boot 4.0.3 flat-classpath applications
- Each daemon becomes an independent Spring Boot application
- Shared infrastructure pulled in as Maven dependencies
- REST APIs from the dead webapp migrate into their respective daemon containers
- Monorepo now, separate repos later once shared library boundaries stabilize

## Non-Goals

- Minion migration (stays on Karaf — communicates via Kafka, framework-agnostic)
- Webapp preservation (webapp is dead)
- Per-daemon Docker images (one image for now, split later)

## Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Spring Boot version | 4.0.3 (GA) | Stable, combined with OSGi removal |
| App structure | Independent app per daemon | Small, independently manageable projects |
| Repo strategy | Monorepo now, separate repos later | Discover shared boundaries before extracting |
| Prototype daemon | Alarmd | Simplest daemon-loader, fewest workaround classes |
| `javax → jakarta` | Migrate shared source in-place | Webapp is dead, all daemons migrating anyway |
| Hibernate | Per-daemon scoped migration | Only migrate DAOs each daemon needs |
| Docker image | Single image during migration | Matches current model, split to per-daemon later |
| RTCd | Delete | Dead code, no longer needed without webapp |

## Architecture

### Module Structure

#### New Modules

```
core/daemon-common/                         ← Spring Boot starter shared by all daemons
  src/main/java/org/opennms/core/daemon/common/
    DaemonCommonAutoConfiguration.java       ← Master auto-config
    DaemonDataSourceConfiguration.java       ← HikariCP DataSource from opennms-datasources.xml
    KafkaEventTransportConfiguration.java    ← KafkaEventForwarder → EventIpcManager
    DaemonHealthConfiguration.java           ← Actuator health endpoint
    DaemonSmartLifecycle.java                ← Wraps AbstractServiceDaemon into SmartLifecycle

core/daemon-boot-alarmd/                    ← Alarmd Spring Boot application (prototype)
  src/main/java/org/opennms/netmgt/alarmd/boot/
    AlarmdApplication.java                   ← @SpringBootApplication main class
    AlarmdConfiguration.java                 ← Alarmd-specific bean wiring
  src/main/resources/
    application.yml                          ← Spring Boot externalized config
```

#### Shared Modules (Emerge During Migration)

| Module | Created When | Contents |
|--------|-------------|----------|
| `daemon-common` | Alarmd (daemon 1) | DataSource, Kafka event transport, health, `DaemonSmartLifecycle` |
| `core/daemon-sink-kafka` | Trapd (daemon 5) | `KafkaSinkBridge`, `LocalMessageConsumerManager`, `LocalMessageDispatcherFactory` |
| `core/daemon-rpc-kafka` | Discovery (daemon 8) | `KafkaRpcClientFactory`, `RpcTargetHelper`, `NoOpTracerRegistry` |

### daemon-common Auto-Configuration

#### DataSource Configuration

- Reads `${opennms.home}/etc/opennms-datasources.xml` for backward compatibility
- Creates HikariCP DataSource
- Creates `PlatformTransactionManager`
- Creates Hibernate 6.x `SessionFactory` with entity scanning
- Replaces: `opennms-distributed-core-impl` bundle + `onmsgi:reference id="dataSource"`

#### Kafka Event Transport Configuration

- `KafkaEventForwarder` — produces events to Kafka topics
- `KafkaEventSubscriptionService` — consumes events from Kafka topics
- `KafkaEventIpcManagerAdapter` — wraps both into `EventIpcManager` interface
- Replaces: `opennms-event-forwarder-kafka` Blueprint bundle + `osgi:reference id="eventIpcManager"`

#### Health Endpoint

- Spring Boot Actuator health endpoint at `/actuator/health`
- Replaces: `opennms-health-rest-service` Karaf feature

#### DaemonSmartLifecycle

- Reusable adapter wrapping `AbstractServiceDaemon` (`init()`/`start()`/`stop()`) into Spring Boot's `SmartLifecycle` interface
- Replaces both `DaemonLifecycleManager` and `SpringDaemonLifecycleManager` across all daemons

#### What daemon-common Does NOT Include

- Kafka RPC client factory — only needed by daemons talking to Minions
- Twin API — only Pollerd uses the publisher side
- PersisterFactory — only Collectd/Pollerd need persistence
- Daemon-specific workaround classes

### Alarmd Application (Prototype)

#### Bean Wiring

```java
@SpringBootApplication(scanBasePackages = {
    "org.opennms.core.daemon.common",
    "org.opennms.netmgt.alarmd"
})
public class AlarmdApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlarmdApplication.class, args);
    }
}
```

```java
@Configuration
public class AlarmdConfiguration {
    @Bean
    public SmartLifecycle alarmdLifecycle(Alarmd alarmd) {
        return new DaemonSmartLifecycle(alarmd);
    }
}
```

#### DAOs Required

- `AlarmDao` / `AlarmDaoHibernate`
- `AlarmEntityNotifierImpl`
- `EventDao` / `EventDaoHibernate`
- `NodeDao` / `NodeDaoHibernate`
- `AcknowledgmentDao` / `AcknowledgmentDaoHibernate`
- `MonitoredServiceDao`
- `DistPollerDao`

These DAOs and their entity annotations need `javax.persistence → jakarta.persistence` and Hibernate 3.6 → 6.x API updates.

#### application.yml (Skeleton)

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/opennms}
    username: ${SPRING_DATASOURCE_USERNAME:opennms}
    password: ${SPRING_DATASOURCE_PASSWORD:opennms}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none  # Liquibase manages schema
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health

opennms:
  home: ${OPENNMS_HOME:/opt/opennms}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
    event-topic: ${KAFKA_EVENT_TOPIC:opennms-fault-events}

logging:
  level:
    org.opennms: INFO
    org.hibernate: WARN
```

## javax → jakarta Migration

### Affected Namespaces

- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.ws.rs.*` → `jakarta.ws.rs.*`
- `javax.xml.bind.*` → `jakarta.xml.bind.*`
- `javax.annotation.*` → `jakarta.annotation.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.inject.*` → `jakarta.inject.*`

### Approach

The `javax → jakarta` rename and the Alarmd Spring Boot migration must land together. Running OpenRewrite across the entire source tree before any daemon is on Spring Boot would leave the system uncompilable — Karaf bundles expect `javax.*` packages.

**Execution plan:**

1. Run OpenRewrite `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta` recipe across the entire source tree
2. Simultaneously, create the `daemon-common` and `daemon-boot-alarmd` modules with Spring Boot 4.0.3
3. Remove or update Karaf daemon-loader modules so they compile against `jakarta.*` (even though they'll be migrated next — they must still compile for `./compile.pl -DskipTests` to pass)
4. Fix any compilation errors from API changes beyond simple renames
5. Validate the full build compiles: `./compile.pl -DskipTests`

This is the largest single commit/PR in the migration. After this, each subsequent daemon migration is incremental.

### Java Version Requirement

Spring Boot 4.0.x depends on Spring Framework 7, which may require Java 21 as baseline. The current project enforces Java 17 (`[17,18)`). Verify the exact requirement for Spring Boot 4.0.3 GA and update the enforced range in the parent POM if needed (e.g., `[21,22)`).

## Hibernate 3.6 → 6.x Migration

### Key API Changes

- `org.hibernate.Criteria` → JPA `CriteriaBuilder`/`CriteriaQuery`
- `HibernateTemplate`/`HibernateCallback` → direct `Session`/`EntityManager`
- `OnmsHibernateTemplate` → rewritten compatibility base class
- `Type` system → redesigned, custom `UserType` implementations need updating
- `.hbm.xml` mapping files → still supported in 6.x (deprecated, but functional)

### Compatibility Base Class

```java
public abstract class AbstractDaoHibernate<T, K extends Serializable>
        implements OnmsDao<T, K> {

    @PersistenceContext
    private EntityManager entityManager;

    protected Session currentSession() {
        return entityManager.unwrap(Session.class);
    }

    // Implements all 16 OnmsDao<T, K> methods.
    // Key translations:
    //   - findMatching(Criteria) → translates OpenNMS Criteria to JPA CriteriaBuilder
    //   - countMatching(Criteria) → CriteriaBuilder count query
    //   - lock() → SELECT ... FOR UPDATE via EntityManager
    //   - load(K) → entityManager.getReference(entityClass, id)
    //   - flush(), clear() → delegate to entityManager
    // See OnmsDao interface for full 16-method contract.

    protected CriteriaBuilder criteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }
}
```

Replaces existing `AbstractDaoHibernate` (which extends `HibernateDaoSupport`). Must implement the full `OnmsDao<T, K>` interface contract, including `findAll()`, `countAll()`, and `findMatching(Criteria)`. Note: the `Criteria` parameter in `findMatching` is the OpenNMS `org.opennms.core.criteria.Criteria` wrapper, not `org.hibernate.Criteria` — this wrapper must be translated to JPA `CriteriaBuilder` queries. Exposes `Session` for DAOs with complex HQL. Individual DAOs can be incrementally modernized from HQL → CriteriaBuilder.

### Per-Daemon Scoping

Each daemon only migrates the DAOs it uses. A DAO migrated for Alarmd stays migrated — the next daemon that needs it pulls it in as-is. The migration naturally fans out: early daemons migrate core DAOs, later daemons find theirs already done.

## Migration Sequence

### Tier 1: Simple Daemons (No Kafka RPC, No Sink)

Only need `daemon-common`:

| Order | Daemon | Notes |
|-------|--------|-------|
| 1 | **Alarmd** | Prototype — proves the pattern |
| 2 | **EventTranslator** | Minimal — event rules engine |
| 3 | **Ticketer** | Minimal — trouble ticket integration |
| 4 | **Scriptd** | Minimal — BSF script handler |
| — | **~~RTCd~~** | **Delete — dead code** |

### Tier 2: Kafka Sink Consumers (Introduces `daemon-sink-kafka`)

| Order | Daemon | Notes |
|-------|--------|-------|
| 5 | **Trapd** | Single sink module, in-process Twin API |
| 6 | **Syslogd** | Same sink pattern as Trapd |
| 7 | **Telemetryd** | Multi-module sink (per-protocol bridges) |

### Tier 3: Kafka RPC Consumers (Introduces `daemon-rpc-kafka`)

| Order | Daemon | Notes |
|-------|--------|-------|
| 8 | **Discovery** | Simple RPC — ping sweeps via Minion |
| 9 | **Enlinkd** | RPC for LLDP/CDP/OSPF topology discovery. No Java workaround classes — only Spring XML + RPC, making it simpler than other Tier 3 daemons. |
| 10 | **Collectd** | RPC + `LocalServiceCollectorRegistry` + PersisterFactory |
| 11 | **Provisiond** | RPC + most workaround classes |

### Tier 4: Complex Daemons

| Order | Daemon | Notes |
|-------|--------|-------|
| 12 | **Pollerd** | Most complex — RPC, Twin publisher, PassiveStatus, 8 workaround classes |
| 13 | **PerspectivePoller** | Similar to Pollerd, depends on Pollerd's patterns |
| 14 | **BSMd** | AlarmLifecycleListenerManager, EventConfInitializer |

### REST API Migration

As each daemon is migrated, its corresponding REST APIs move from the dead webapp into the daemon's Spring Boot app. The existing REST endpoints use CXF/JAX-RS annotations (`@Path`, `@GET`, `@POST`, etc.). These will be rewritten as Spring MVC `@RestController` classes — Spring Boot 4 does not embed CXF, and Spring MVC is the native REST framework.

**Daemon-to-endpoint mapping (major endpoints):**

| Daemon | REST Endpoints |
|--------|---------------|
| Alarmd | `/api/v2/alarms`, `/api/v2/alarm-acknowledges` |
| Provisiond | `/api/v2/requisitions`, `/api/v2/foreign-sources`, `/api/v2/nodes` |
| Pollerd | `/api/v2/outages`, `/api/v2/monitors` |
| Collectd | `/api/v2/graphs`, `/api/v2/resources`, `/api/v2/measurements` |
| Discovery | `/api/v2/discovery` |
| Enlinkd | `/api/v2/topology` |
| BSMd | `/api/v2/business-services` |
| Trapd | `/api/v2/snmp-traps` (if applicable) |

This mapping will be refined during implementation as the full set of webapp REST endpoints is cataloged.

## Logging

Current daemons inherit Karaf's Pax Logging (Log4j2). Spring Boot defaults to Logback.

**Strategy:** Use Spring Boot's default Logback. Each daemon runs in its own container, so per-daemon log files are unnecessary — logs go to stdout (standard container practice). Log level configuration via `application.yml` or `LOGGING_LEVEL_*` environment variables.

If Log4j2 is preferred (e.g., for async appender performance), Spring Boot supports it via `spring-boot-starter-log4j2` exclusion/replacement.

## Testing Strategy

### Unit Tests

Existing unit tests for DAO and service classes migrate with their source. `javax → jakarta` renames apply to test imports. Hibernate 6.x DAO tests may need `SessionFactory` configuration updates.

### Integration Tests

Each daemon boot module includes an integration test that:
- Starts the Spring Boot application context
- Uses Testcontainers for PostgreSQL and Kafka
- Verifies the daemon starts, processes a test event, and shuts down cleanly

### Smoke Tests (Hybrid Period)

During the hybrid period, the Docker compose stack must pass end-to-end tests with a mix of Karaf and Spring Boot daemons. The existing passive status E2E test suite validates cross-daemon event flow.

## Port Allocation

Each daemon runs in its own Docker container. All daemons expose Actuator on port `8080` (container-internal) — no conflicts since containers are isolated. Docker compose maps no host ports for Actuator; healthchecks use container-internal URLs.

For REST API endpoints (post-migration), each daemon serves its REST APIs on the same `8080` port. An API gateway or reverse proxy (e.g., nginx) routes external requests to the appropriate daemon container by path prefix.

## Docker and Deployment

### During Migration (Hybrid Period)

Single `opennms/daemon-deltav` image contains both Karaf runtime (un-migrated daemons) and Spring Boot fat JARs (migrated daemons).

```dockerfile
# Karaf bundles (existing)
COPY daemon-loader-*/target/*.jar /opt/opennms/system/...

# Spring Boot fat JARs (new)
COPY daemon-boot-alarmd/target/daemon-boot-alarmd.jar /opt/opennms/lib/
```

### docker-compose.yml

Migrated daemons switch from Karaf feature boot to `java -jar`:

```yaml
# Before (Karaf)
alarmd:
  image: opennms/daemon-deltav:${VERSION}
  volumes:
    - ./alarmd-overlay/etc/featuresBoot.d:/opt/opennms/etc/featuresBoot.d

# After (Spring Boot)
alarmd:
  image: opennms/daemon-deltav:${VERSION}
  command: ["java", "-jar", "/opt/opennms/lib/daemon-boot-alarmd.jar"]
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/opennms
    KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    OPENNMS_HOME: /opt/opennms
```

### Configuration Sources

1. `application.yml` embedded in fat JAR — defaults
2. Environment variables — overrides (standard Spring Boot externalized config)
3. `${opennms.home}/etc/` — existing OpenNMS config files, read by daemon-specific config DAOs

### Health Checks

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
```

### Post-Migration (All Daemons on Spring Boot)

- Remove Karaf runtime from image
- Remove all `daemon-loader-*` modules
- Remove `features.xml` daemon entries
- Remove all `*-overlay/etc/featuresBoot.d/` directories
- Base image becomes plain JDK instead of Karaf
- Future: split to per-daemon Docker images

## Cleanup Inventory

### Delete Immediately (Pre-Migration)

| Path | Reason |
|------|--------|
| `core/daemon-loader-rtcd/` | Dead code |
| `opennms-container/delta-v/rtcd-overlay/` | Dead code |
| RTCd feature entry in `features.xml` | Dead code |
| RTCd service in `docker-compose.yml` | Dead code |

### Delete Per-Daemon (As Each Migrates)

- `core/daemon-loader-{name}/` — entire module
- `opennms-container/delta-v/{name}-overlay/` — featuresBoot.d and config overlays
- Feature entry in `container/features/src/main/resources/features.xml`

### Delete After All Daemons Migrated

| Path/Component | Reason |
|----------------|--------|
| `container/features/src/main/resources/features.xml` daemon entries | All moved to Spring Boot |
| `opennms-container/delta-v/daemon-overlay/` | Karaf features.xml overlay |
| `Dockerfile.daemon` Karaf layers | No Karaf runtime |
| `opennms-spring-extender` feature | Spring-DM Extender |
| `core/event-forwarder-kafka/.../OSGI-INF/blueprint/` | Blueprint → `@Configuration` |
| All `onmsgi:` and `osgi:` namespace XML | OSGi service registry gone |
| `OnmsOSGiBridgeActivator` | OSGi ↔ ServiceRegistry bridge |
| `core/daemon-loader-shared/` | Workarounds absorbed or deleted |
| Workaround classes: `InlineIdentity`, `InlineProvisiondConfigDao`, `NoOpEntityScopeProvider`, `NoOpSnmpProfileMapper`, `NoOpTracerRegistry`, `LocalServiceMonitorRegistry`, `LocalServiceCollectorRegistry`, `LocalServiceDetectorRegistry` | Flat classpath eliminates classloader issues |
| `opennms-container/delta-v/webapp-overlay/` | Webapp dead |
