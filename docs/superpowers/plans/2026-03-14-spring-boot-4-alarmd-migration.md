# Spring Boot 4 Alarmd Migration — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the Alarmd daemon from Karaf/OSGi to Spring Boot 4.0.3 as the prototype, establishing the pattern for all 14 daemon migrations.

**Architecture:** Create two new Maven modules — `core/daemon-common` (shared Spring Boot starter) and `core/daemon-boot-alarmd` (Alarmd Spring Boot app). The Alarmd app consumes Kafka events, persists alarms via Hibernate 6.x DAOs, and runs as a fat JAR inside the existing `opennms/daemon-deltav` Docker image. All other daemons remain on Karaf and must continue to compile and pass E2E tests.

**Tech Stack:** Spring Boot 4.0.3, Spring Framework 7, Hibernate ORM 6.x, Jakarta EE (jakarta.persistence), HikariCP, Apache Kafka client, Spring Boot Actuator, Testcontainers (PostgreSQL + Kafka)

**Spec:** `docs/superpowers/specs/2026-03-14-spring-boot-4-migration-design.md`

---

## Chunk 1: Prerequisites — Delete RTCd and Verify Java Version

### Task 1: Verify Spring Boot 4.0.3 Java Baseline

Spring Boot 4.0.x requires Spring Framework 7. Determine if Java 21 is required and update the enforced version range if needed.

**Files:**
- Modify: `pom.xml` (root — Maven Enforcer Plugin java version range)

- [ ] **Step 1: Check Spring Boot 4.0.3 Java requirement**

Run:
```bash
# Check the Spring Boot 4 docs or POM for minimum Java version
# If Spring Framework 7 requires Java 21, update the enforcer
```

Search the web for "Spring Boot 4.0.3 minimum Java version" or check the spring-boot-starter-parent POM.

- [ ] **Step 2: Update Java version range if needed**

If Java 21 is required, in `pom.xml` find the Maven Enforcer Plugin `requireJava` rule and update:
```xml
<!-- FROM -->
<version>[17,18)</version>
<!-- TO (if Java 21 required) -->
<version>[21,22)</version>
```

Note: The daemon tier already runs on Java 21 (Karaf 4.4.9 upgrade from prior work). The enforcer range in the root POM may still say `[17,18)` — widen it to `[17,22)` to support both Karaf and Spring Boot daemons during the hybrid period.

- [ ] **Step 3: Validate the build still compiles**

Run: `./compile.pl -DskipTests --projects :org.opennms.core -am install`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "build: widen Java version range for Spring Boot 4 compatibility"
```

### Task 2: Delete RTCd (Dead Code)

**Files:**
- Delete: `core/daemon-loader-rtcd/` (entire directory)
- Delete: `opennms-container/delta-v/rtcd-overlay/` (entire directory)
- Modify: `core/pom.xml` (remove `<module>daemon-loader-rtcd</module>`)
- Modify: `container/features/src/main/resources/features.xml` (remove `opennms-daemon-rtcd` feature)
- Modify: `opennms-container/delta-v/daemon-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml` (remove `opennms-daemon-rtcd` feature)
- Modify: `opennms-container/delta-v/docker-compose.yml` (remove `rtcd` service + `rtcd-data` volume declaration)

Note: RTCd does NOT appear in `build.sh` or `Dockerfile.daemon` — it runs from the Karaf base image via features, not from staged JARs.

- [ ] **Step 1: Remove RTCd module from core/pom.xml**

In `core/pom.xml`, delete the line:
```xml
<module>daemon-loader-rtcd</module>
```

- [ ] **Step 2: Delete RTCd daemon-loader directory**

Run: `rm -rf core/daemon-loader-rtcd`

- [ ] **Step 3: Delete RTCd overlay directory**

Run: `rm -rf opennms-container/delta-v/rtcd-overlay`

- [ ] **Step 4: Remove RTCd feature from features.xml (source)**

In `container/features/src/main/resources/features.xml`, find and delete the `<feature name="opennms-daemon-rtcd"...>...</feature>` block.

- [ ] **Step 5: Remove RTCd feature from features.xml (daemon overlay)**

In `opennms-container/delta-v/daemon-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml`, find and delete the `<feature name="opennms-daemon-rtcd"...>...</feature>` block.

- [ ] **Step 6: Remove RTCd service and volume from docker-compose.yml**

In `opennms-container/delta-v/docker-compose.yml`:
1. Delete the entire `rtcd:` service block
2. Delete the `rtcd-data:` entry from the `volumes:` section at the bottom of the file

- [ ] **Step 7: Validate build**

Run: `./compile.pl -DskipTests`
Expected: BUILD SUCCESS (rtcd module no longer referenced)

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "refactor: delete RTCd daemon (dead code — no longer needed without webapp)"
```

---

## Chunk 2: Create daemon-common Module

### Task 3: Create daemon-common Maven Module

This is the shared Spring Boot starter that all migrated daemons will depend on. It provides DataSource, Kafka event transport, health endpoint, and daemon lifecycle management.

**Files:**
- Create: `core/daemon-common/pom.xml`
- Modify: `core/pom.xml` (add `<module>daemon-common</module>`)

- [ ] **Step 1: Create directory structure**

Run:
```bash
mkdir -p core/daemon-common/src/main/java/org/opennms/core/daemon/common
mkdir -p core/daemon-common/src/main/resources
mkdir -p core/daemon-common/src/test/java/org/opennms/core/daemon/common
```

- [ ] **Step 2: Create pom.xml**

Create `core/daemon-common/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.opennms</groupId>
        <artifactId>org.opennms.core</artifactId>
        <version>36.0.0-SNAPSHOT</version>
    </parent>

    <groupId>org.opennms.core</groupId>
    <artifactId>org.opennms.core.daemon-common</artifactId>
    <packaging>jar</packaging>
    <name>OpenNMS :: Core :: Daemon Common</name>

    <properties>
        <java.version>21</java.version>
        <spring-boot.version>4.0.3</spring-boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Import Spring Boot BOM for version management -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>

        <!-- Kafka -->
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
        </dependency>

        <!-- OpenNMS APIs (javax versions — consumed as-is during hybrid period) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.opennms.features.events</groupId>
            <artifactId>org.opennms.features.events.api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

**Important note for implementation:** The `spring-boot-starter-data-jpa` pulls in Hibernate 6.x and Jakarta Persistence API. The OpenNMS shared modules (`opennms-dao-api`, `opennms-model`) still use `javax.persistence`. During the hybrid period, use Eclipse Transformer or equivalent to bridge the namespace gap at build time. The exact mechanism will be determined in Task 5 when wiring the DataSource.

- [ ] **Step 3: Add module to core/pom.xml**

In `core/pom.xml`, add within the `<modules>` block:
```xml
<module>daemon-common</module>
```

- [ ] **Step 4: Validate it compiles**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-common -am install`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add core/daemon-common/pom.xml core/pom.xml
git commit -m "feat: add daemon-common Maven module (Spring Boot 4 shared starter)"
```

### Task 4: Implement DaemonSmartLifecycle

This adapter wraps any `AbstractServiceDaemon` (which has `init()`/`start()`/`stop()`) into Spring Boot's `SmartLifecycle` interface. It replaces both `DaemonLifecycleManager` and `SpringDaemonLifecycleManager` used across all 14 Karaf daemon-loaders.

**Files:**
- Create: `core/daemon-common/src/main/java/org/opennms/core/daemon/common/DaemonSmartLifecycle.java`
- Create: `core/daemon-common/src/test/java/org/opennms/core/daemon/common/DaemonSmartLifecycleTest.java`

- [ ] **Step 1: Write the failing test**

Create `core/daemon-common/src/test/java/org/opennms/core/daemon/common/DaemonSmartLifecycleTest.java`:
```java
package org.opennms.core.daemon.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;

class DaemonSmartLifecycleTest {

    private static class TestDaemon extends AbstractServiceDaemon {
        boolean initialized = false;
        boolean started = false;
        boolean stopped = false;

        TestDaemon() {
            super("test-daemon");
        }

        @Override
        protected void onInit() {
            initialized = true;
        }

        @Override
        protected void onStart() {
            started = true;
        }

        @Override
        protected void onStop() {
            stopped = true;
        }
    }

    @Test
    void startCallsInitThenStart() {
        var daemon = new TestDaemon();
        var lifecycle = new DaemonSmartLifecycle(daemon);

        lifecycle.start();

        assertThat(daemon.initialized).isTrue();
        assertThat(daemon.started).isTrue();
        assertThat(lifecycle.isRunning()).isTrue();
    }

    @Test
    void stopCallsStop() {
        var daemon = new TestDaemon();
        var lifecycle = new DaemonSmartLifecycle(daemon);

        lifecycle.start();
        lifecycle.stop();

        assertThat(daemon.stopped).isTrue();
        assertThat(lifecycle.isRunning()).isFalse();
    }

    @Test
    void isAutoStartupReturnsTrue() {
        var daemon = new TestDaemon();
        var lifecycle = new DaemonSmartLifecycle(daemon);

        assertThat(lifecycle.isAutoStartup()).isTrue();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd core/daemon-common && ../../maven/bin/mvn test -Dtest=DaemonSmartLifecycleTest -pl .`
Expected: FAIL — `DaemonSmartLifecycle` class does not exist

- [ ] **Step 3: Implement DaemonSmartLifecycle**

Create `core/daemon-common/src/main/java/org/opennms/core/daemon/common/DaemonSmartLifecycle.java`:
```java
package org.opennms.core.daemon.common;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

public class DaemonSmartLifecycle implements SmartLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(DaemonSmartLifecycle.class);

    private final AbstractServiceDaemon daemon;
    private volatile boolean running = false;

    public DaemonSmartLifecycle(AbstractServiceDaemon daemon) {
        this.daemon = daemon;
    }

    @Override
    public void start() {
        LOG.info("Starting daemon: {}", daemon.getName());
        daemon.init();
        daemon.start();
        running = true;
        LOG.info("Daemon started: {}", daemon.getName());
    }

    @Override
    public void stop() {
        LOG.info("Stopping daemon: {}", daemon.getName());
        daemon.stop();
        running = false;
        LOG.info("Daemon stopped: {}", daemon.getName());
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // Start last, stop first
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd core/daemon-common && ../../maven/bin/mvn test -Dtest=DaemonSmartLifecycleTest -pl .`
Expected: PASS — all 3 tests green

- [ ] **Step 5: Commit**

```bash
git add core/daemon-common/src/
git commit -m "feat: add DaemonSmartLifecycle adapter for Spring Boot daemon lifecycle"
```

### Task 5: Implement KafkaEventTransportConfiguration

This `@Configuration` class replaces the Aries Blueprint `blueprint-event-forwarder-kafka.xml`. It creates the `KafkaEventForwarder`, `KafkaEventSubscriptionService`, and wraps them in `KafkaEventIpcManagerAdapter` which implements `EventIpcManager`.

**Files:**
- Create: `core/daemon-common/src/main/java/org/opennms/core/daemon/common/KafkaEventTransportConfiguration.java`
- Create: `core/daemon-common/src/test/java/org/opennms/core/daemon/common/KafkaEventTransportConfigurationTest.java`

- [ ] **Step 1: Write the failing test**

Create `core/daemon-common/src/test/java/org/opennms/core/daemon/common/KafkaEventTransportConfigurationTest.java`:
```java
package org.opennms.core.daemon.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = KafkaEventTransportConfiguration.class)
@TestPropertySource(properties = {
    "opennms.kafka.bootstrap-servers=localhost:9092",
    "opennms.kafka.event-topic=test-events"
})
class KafkaEventTransportConfigurationTest {

    @Autowired
    private EventIpcManager eventIpcManager;

    @Test
    void eventIpcManagerBeanIsCreated() {
        assertThat(eventIpcManager).isNotNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd core/daemon-common && ../../maven/bin/mvn test -Dtest=KafkaEventTransportConfigurationTest -pl .`
Expected: FAIL — class does not exist

- [ ] **Step 3: Implement KafkaEventTransportConfiguration**

Create `core/daemon-common/src/main/java/org/opennms/core/daemon/common/KafkaEventTransportConfiguration.java`:
```java
package org.opennms.core.daemon.common;

import org.opennms.core.event.forwarder.kafka.KafkaEventForwarder;
import org.opennms.core.event.forwarder.kafka.KafkaEventIpcManagerAdapter;
import org.opennms.core.event.forwarder.kafka.KafkaEventSubscriptionService;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaEventTransportConfiguration {

    @Value("${opennms.kafka.bootstrap-servers:kafka:9092}")
    private String bootstrapServers;

    @Value("${opennms.kafka.event-topic:opennms-fault-events}")
    private String eventTopic;

    @Value("${opennms.kafka.consumer-group:opennms-core}")
    private String consumerGroup;

    @Value("${opennms.kafka.poll-timeout-ms:100}")
    private long pollTimeoutMs;

    @Bean(destroyMethod = "close")
    public KafkaEventSubscriptionService kafkaEventSubscriptionService() {
        return KafkaEventSubscriptionService.create(
                bootstrapServers, consumerGroup, eventTopic, pollTimeoutMs);
    }

    @Bean
    public KafkaEventForwarder kafkaEventForwarder() {
        return KafkaEventForwarder.create(bootstrapServers, eventTopic);
    }

    @Bean
    public EventIpcManager eventIpcManager(
            KafkaEventForwarder forwarder,
            KafkaEventSubscriptionService subscriptionService) {
        return new KafkaEventIpcManagerAdapter(forwarder, subscriptionService);
    }
}
```

**IMPORTANT — the code above is pseudocode.** The actual APIs differ significantly:

- `KafkaEventForwarder` constructor requires: `EventProcessor` (event expander), `EventProcessor` (TSID assigner), `EventClassifier`, `KafkaProducer<Long, byte[]>`, and `String topicName`
- `KafkaEventSubscriptionService` constructor requires: `KafkaConsumer<Long, byte[]>`, `String topicNames`, `EventDeserializer`, `Duration pollTimeout`, `TsidFactory`
- Both have static `create()` factory methods with different signatures

**Read these files to get the actual APIs:**
- `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventForwarder.java`
- `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/KafkaEventSubscriptionService.java`
- `core/event-forwarder-kafka/src/main/resources/OSGI-INF/blueprint/blueprint-event-forwarder-kafka.xml` (shows exact wiring)

The test for this task should also be adapted to the actual API once determined. Use `@SpringBootTest` with a test `@SpringBootApplication` class (not just the `@Configuration` class alone — a bare `@Configuration` class won't trigger auto-configuration).

- [ ] **Step 4: Run test to verify it passes**

Run: `cd core/daemon-common && ../../maven/bin/mvn test -Dtest=KafkaEventTransportConfigurationTest -pl .`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/daemon-common/src/
git commit -m "feat: add KafkaEventTransportConfiguration for Spring Boot daemons"
```

### Task 6: Implement DaemonDataSourceConfiguration

This `@Configuration` class creates a HikariCP DataSource, Hibernate 6.x SessionFactory, and JPA transaction manager. It replaces the `opennms-distributed-core-impl` OSGi bundle and all `onmsgi:reference id="dataSource"` lookups.

**Files:**
- Create: `core/daemon-common/src/main/java/org/opennms/core/daemon/common/DaemonDataSourceConfiguration.java`
- Create: `core/daemon-common/src/test/java/org/opennms/core/daemon/common/DaemonDataSourceConfigurationTest.java`

- [ ] **Step 1: Write the failing test**

Create `core/daemon-common/src/test/java/org/opennms/core/daemon/common/DaemonDataSourceConfigurationTest.java`:
```java
package org.opennms.core.daemon.common;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = DaemonDataSourceConfiguration.class)
@Testcontainers
class DaemonDataSourceConfigurationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("opennms")
                    .withUsername("opennms")
                    .withPassword("opennms");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Test
    void dataSourceBeanIsCreated() {
        assertThat(dataSource).isNotNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd core/daemon-common && ../../maven/bin/mvn test -Dtest=DaemonDataSourceConfigurationTest -pl .`
Expected: FAIL — class does not exist

- [ ] **Step 3: Implement DaemonDataSourceConfiguration**

Create `core/daemon-common/src/main/java/org/opennms/core/daemon/common/DaemonDataSourceConfiguration.java`:
```java
package org.opennms.core.daemon.common;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "org.opennms.netmgt.model")
public class DaemonDataSourceConfiguration {
    // Spring Boot auto-configuration handles:
    // - HikariCP DataSource from spring.datasource.* properties
    // - Hibernate SessionFactory from spring.jpa.* properties
    // - JpaTransactionManager
    //
    // Entity scanning finds @Entity classes in opennms-model.
    // Schema management is "none" (Liquibase manages via db-init container).
}
```

**Note for implementer:** Spring Boot 4 auto-configures DataSource, EntityManagerFactory, and TransactionManager from `application.yml` properties. The `@EntityScan` tells Hibernate where to find `@Entity` classes (OnmsAlarm, OnmsNode, etc. in `opennms-model`). If entity classes use `javax.persistence` annotations, you will need to either:
- Use Eclipse Transformer to convert the `opennms-model` JAR to `jakarta.persistence` at build time
- Or fork `opennms-model` as `opennms-model-jakarta` with renamed imports
Determine this during implementation based on what compiles.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd core/daemon-common && ../../maven/bin/mvn test -Dtest=DaemonDataSourceConfigurationTest -pl .`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/daemon-common/src/
git commit -m "feat: add DaemonDataSourceConfiguration with JPA auto-config"
```

### Task 7: Create application.yml defaults

Application config belongs in the bootable app module, not the library module. `daemon-common` is a library — placing `application.yml` in it would unexpectedly override consuming applications' defaults.

**Files:**
- Create: `core/daemon-boot-alarmd/src/main/resources/application.yml`

- [ ] **Step 1: Create application.yml**

Create `core/daemon-boot-alarmd/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/opennms}
    username: ${SPRING_DATASOURCE_USERNAME:opennms}
    password: ${SPRING_DATASOURCE_PASSWORD:opennms}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
    open-in-view: false

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always

opennms:
  home: ${OPENNMS_HOME:/opt/opennms}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
    event-topic: ${KAFKA_EVENT_TOPIC:opennms-fault-events}
    consumer-group: ${KAFKA_CONSUMER_GROUP:opennms-core}
    poll-timeout-ms: ${KAFKA_POLL_TIMEOUT_MS:100}

logging:
  level:
    org.opennms: INFO
    org.hibernate: WARN
    org.hibernate.SQL: ${HIBERNATE_SQL_LOG_LEVEL:WARN}
```

- [ ] **Step 2: Commit**

```bash
git add core/daemon-boot-alarmd/src/main/resources/application.yml
git commit -m "feat: add application.yml for Alarmd Spring Boot app"
```

---

## Chunk 3: javax → jakarta Strategy for Alarmd's Dependencies

### Task 8: Determine javax → jakarta Bridge Mechanism

Alarmd depends on shared modules that use `javax.persistence` annotations: `opennms-model`, `opennms-dao-api`, `opennms-dao`. These modules are also consumed by un-migrated Karaf daemons that need `javax`. We need a strategy to consume them in a `jakarta`-based Spring Boot app.

**Files:**
- Potentially modify: `core/daemon-common/pom.xml` (add transformer plugin)
- Potentially create: new `-jakarta` module variants

- [ ] **Step 1: Evaluate Eclipse Transformer approach**

Check if the `org.eclipse.transformer:transformer-maven-plugin` can be applied in the `daemon-common` or `daemon-boot-alarmd` POM to transform `javax.*` → `jakarta.*` at build time on specific dependencies.

Example Maven plugin config to test:
```xml
<plugin>
    <groupId>org.eclipse.transformer</groupId>
    <artifactId>transformer-maven-plugin</artifactId>
    <version>0.5.0</version>
    <extensions>true</extensions>
    <configuration>
        <classifier>jakarta</classifier>
    </configuration>
    <executions>
        <execution>
            <id>transform-jar</id>
            <phase>package</phase>
            <goals>
                <goal>jar</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 2: If transformer works, configure it in daemon-common POM**

Add the transformer plugin to transform `opennms-model`, `opennms-dao-api`, and `opennms-dao` JARs from `javax.persistence` to `jakarta.persistence` at build time.

- [ ] **Step 3: If transformer doesn't work, create fork modules**

Create `opennms-model-jakarta`, `opennms-dao-api-jakarta`, `opennms-dao-jakarta` by copying the source and running OpenRewrite on just those copies:

```bash
cp -r opennms-model opennms-model-jakarta
cd opennms-model-jakarta
# Run OpenRewrite javax→jakarta recipe on this copy only
```

Update `daemon-common/pom.xml` to depend on the `-jakarta` variants.

- [ ] **Step 4: Validate that both Karaf daemons and daemon-common compile**

Run: `./compile.pl -DskipTests`
Expected: BUILD SUCCESS — all modules compile, no Karaf daemon broken

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "build: add javax→jakarta bridge for Spring Boot daemon dependencies"
```

---

## Chunk 4: Hibernate 6.x DAO Compatibility Layer

### Task 9: Create AbstractDaoHibernate Replacement

The existing `AbstractDaoHibernate` extends `HibernateDaoSupport` (removed in Spring 6). Create a JPA-native replacement that implements the full `OnmsDao<T, K>` contract (16 methods).

**Files:**
- Create: `core/daemon-common/src/main/java/org/opennms/core/daemon/common/AbstractDaoJpa.java`
- Create: `core/daemon-common/src/test/java/org/opennms/core/daemon/common/AbstractDaoJpaTest.java`

- [ ] **Step 1: Write the failing test**

Create `core/daemon-common/src/test/java/org/opennms/core/daemon/common/AbstractDaoJpaTest.java`:
```java
package org.opennms.core.daemon.common;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Import(TestDaoConfig.class)
@Testcontainers
class AbstractDaoJpaTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestEntityDao testDao;

    @Test
    void saveThenGet() {
        var entity = new TestEntity();
        entity.setName("test");
        var id = testDao.save(entity);
        assertThat(testDao.get(id)).isNotNull();
        assertThat(testDao.get(id).getName()).isEqualTo("test");
    }

    @Test
    void findAll() {
        var e1 = new TestEntity();
        e1.setName("a");
        testDao.save(e1);
        var e2 = new TestEntity();
        e2.setName("b");
        testDao.save(e2);
        assertThat(testDao.findAll()).hasSize(2);
    }

    @Test
    void countAll() {
        var e = new TestEntity();
        e.setName("c");
        testDao.save(e);
        assertThat(testDao.countAll()).isEqualTo(1);
    }

    @Test
    void delete() {
        var e = new TestEntity();
        e.setName("d");
        var id = testDao.save(e);
        testDao.delete(testDao.get(id));
        assertThat(testDao.countAll()).isEqualTo(0);
    }
}
```

The `TestEntity`, `TestEntityDao`, and `TestDaoConfig` are test-only helpers created alongside.

- [ ] **Step 2: Run test to verify it fails**

Expected: FAIL — `AbstractDaoJpa` does not exist

- [ ] **Step 3: Implement AbstractDaoJpa**

Create `core/daemon-common/src/main/java/org/opennms/core/daemon/common/AbstractDaoJpa.java`:
```java
package org.opennms.core.daemon.common;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.OnmsDao;

public abstract class AbstractDaoJpa<T, K extends Serializable> implements OnmsDao<T, K> {

    @PersistenceContext
    private EntityManager entityManager;

    private final Class<T> entityClass;

    protected AbstractDaoJpa(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected EntityManager entityManager() {
        return entityManager;
    }

    protected Session currentSession() {
        return entityManager.unwrap(Session.class);
    }

    protected CriteriaBuilder criteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    @Override
    public void lock() {
        // Lock via SELECT ... FOR UPDATE on a sentinel row
        // Implementation depends on AccessLock table pattern
    }

    @Override
    public void initialize(Object obj) {
        org.hibernate.Hibernate.initialize(obj);
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public void clear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public int countAll() {
        CriteriaBuilder cb = criteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(entityClass)));
        return entityManager.createQuery(cq).getSingleResult().intValue();
    }

    @Override
    public int countMatching(org.opennms.core.criteria.Criteria criteria) {
        // Translate OpenNMS Criteria → JPA CriteriaBuilder count query
        // Use HibernateCriteriaConverter pattern adapted for JPA
        return 0; // Placeholder — implement with CriteriaConverter
    }

    @Override
    public void delete(T entity) {
        entityManager.remove(entityManager.contains(entity)
                ? entity : entityManager.merge(entity));
    }

    @Override
    public void delete(K id) {
        T entity = get(id);
        if (entity != null) {
            delete(entity);
        }
    }

    @Override
    public List<T> findAll() {
        CriteriaBuilder cb = criteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        cq.from(entityClass);
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<T> findMatching(org.opennms.core.criteria.Criteria criteria) {
        // Translate OpenNMS Criteria → JPA CriteriaBuilder query
        // Use HibernateCriteriaConverter pattern adapted for JPA
        return List.of(); // Placeholder — implement with CriteriaConverter
    }

    @Override
    public T get(K id) {
        return entityManager.find(entityClass, id);
    }

    @Override
    public T load(K id) {
        return entityManager.getReference(entityClass, id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public K save(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return (K) entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil().getIdentifier(entity);
    }

    @Override
    public void saveOrUpdate(T entity) {
        entityManager.merge(entity);
    }

    @Override
    public void update(T entity) {
        entityManager.merge(entity);
    }
}
```

**IMPORTANT — `findMatching` and `countMatching` cannot remain as stubs.** `AlarmPersisterImpl` calls `alarmDao.findByReductionKey()` which likely uses `findMatching(Criteria)` internally. If these return empty results, alarm deduplication (the core purpose of Alarmd) will silently break — every event creates a new alarm instead of reducing into an existing one.

The existing `HibernateCriteriaConverter` class (in `opennms-dao`) translates the OpenNMS `org.opennms.core.criteria.Criteria` wrapper to Hibernate Criteria API calls. You need to create a `JpaCriteriaConverter` that translates to JPA `CriteriaBuilder` instead. At minimum, implement the criteria operations that `AlarmDaoHibernate.findByReductionKey()` uses — typically equality restrictions and ordering. Read the `HibernateCriteriaConverter` source to understand the full set of operators.

- [ ] **Step 4: Run tests to verify they pass**

Expected: PASS — basic CRUD operations work

- [ ] **Step 5: Commit**

```bash
git add core/daemon-common/src/
git commit -m "feat: add AbstractDaoJpa — JPA-native replacement for AbstractDaoHibernate"
```

---

## Chunk 5: Alarmd Spring Boot Application

### Task 10: Create daemon-boot-alarmd Maven Module

**Files:**
- Create: `core/daemon-boot-alarmd/pom.xml`
- Create: `core/daemon-boot-alarmd/src/main/java/org/opennms/netmgt/alarmd/boot/AlarmdApplication.java`
- Modify: `core/pom.xml` (add `<module>daemon-boot-alarmd</module>`)

- [ ] **Step 1: Create directory structure**

Run:
```bash
mkdir -p core/daemon-boot-alarmd/src/main/java/org/opennms/netmgt/alarmd/boot
mkdir -p core/daemon-boot-alarmd/src/main/resources
mkdir -p core/daemon-boot-alarmd/src/test/java/org/opennms/netmgt/alarmd/boot
```

- [ ] **Step 2: Create pom.xml**

Create `core/daemon-boot-alarmd/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.opennms</groupId>
        <artifactId>org.opennms.core</artifactId>
        <version>36.0.0-SNAPSHOT</version>
    </parent>

    <groupId>org.opennms.core</groupId>
    <artifactId>org.opennms.core.daemon-boot-alarmd</artifactId>
    <packaging>jar</packaging>
    <name>OpenNMS :: Core :: Daemon Boot :: Alarmd</name>

    <properties>
        <java.version>21</java.version>
        <spring-boot.version>4.0.3</spring-boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Shared daemon infrastructure -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon-common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Alarmd implementation -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-alarmd</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-alarm-api</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- DAOs -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-dao-api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-dao</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-model</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Create AlarmdApplication.java**

Create `core/daemon-boot-alarmd/src/main/java/org/opennms/netmgt/alarmd/boot/AlarmdApplication.java`:
```java
package org.opennms.netmgt.alarmd.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

- [ ] **Step 4: Add module to core/pom.xml**

In `core/pom.xml`, add within the `<modules>` block:
```xml
<module>daemon-boot-alarmd</module>
```

- [ ] **Step 5: Validate it compiles**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-boot-alarmd -am install`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add core/daemon-boot-alarmd/ core/pom.xml
git commit -m "feat: add daemon-boot-alarmd Spring Boot application module"
```

### Task 11: Create AlarmdConfiguration

This wires Alarmd-specific beans: the Alarmd daemon itself, its lifecycle, and the AlarmPersisterImpl with its dependencies.

**Files:**
- Create: `core/daemon-boot-alarmd/src/main/java/org/opennms/netmgt/alarmd/boot/AlarmdConfiguration.java`

- [ ] **Step 1: Create AlarmdConfiguration.java**

Create `core/daemon-boot-alarmd/src/main/java/org/opennms/netmgt/alarmd/boot/AlarmdConfiguration.java`:
```java
package org.opennms.netmgt.alarmd.boot;

import io.dropwizard.metrics5.MetricRegistry;
import org.opennms.core.daemon.common.DaemonSmartLifecycle;
import org.opennms.netmgt.alarmd.Alarmd;
import org.opennms.netmgt.alarmd.AlarmLifecycleListenerManager;
import org.opennms.netmgt.alarmd.AlarmPersisterImpl;
import org.opennms.netmgt.alarmd.NorthbounderManager;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.hibernate.AlarmEntityNotifierImpl;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.events.api.EventUtilDaoImpl;
import org.opennms.netmgt.events.api.EventUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.SmartLifecycle;
import org.springframework.transaction.support.TransactionOperations;

@Configuration
public class AlarmdConfiguration {

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public EventUtil eventUtil(MetricRegistry metricRegistry) {
        return new EventUtilDaoImpl(metricRegistry);
    }

    @Bean
    public AlarmEntityNotifier alarmEntityNotifier() {
        return new AlarmEntityNotifierImpl();
    }

    @Bean
    public AlarmPersisterImpl alarmPersister(
            AlarmDao alarmDao,
            NodeDao nodeDao,
            DistPollerDao distPollerDao,
            ServiceTypeDao serviceTypeDao,
            EventUtil eventUtil,
            AlarmEntityNotifier alarmEntityNotifier,
            TransactionOperations transactionOperations) {
        var persister = new AlarmPersisterImpl();
        // AlarmPersisterImpl uses @Autowired field injection for most deps.
        // Spring Boot component scanning will satisfy these if it's annotated.
        // If not, use reflection or convert to setter injection.
        return persister;
    }

    @Bean
    public AlarmLifecycleListenerManager alarmLifecycleListenerManager() {
        return new AlarmLifecycleListenerManager();
    }

    @Bean
    public NorthbounderManager northbounderManager() {
        return new NorthbounderManager();
    }

    @Bean
    public Alarmd alarmd(AlarmPersisterImpl alarmPersister) {
        var alarmd = new Alarmd();
        alarmd.setPersister(alarmPersister);
        return alarmd;
    }

    @Bean
    public AnnotationBasedEventListenerAdapter daemonListener(
            Alarmd alarmd,
            EventSubscriptionService eventSubscriptionService) {
        // This adapter wires the @EventHandler annotation on Alarmd.onEvent()
        // to the EventSubscriptionService so Alarmd actually receives events.
        var adapter = new AnnotationBasedEventListenerAdapter(alarmd, eventSubscriptionService);
        return adapter;
    }

    @Bean
    public SmartLifecycle alarmdLifecycle(Alarmd alarmd) {
        return new DaemonSmartLifecycle(alarmd);
    }
}
```

**Critical notes for implementer:**
1. `AlarmPersisterImpl` uses `@Autowired` field injection for `AlarmDao`, `NodeDao`, `EventUtil`, `AlarmEntityNotifier`, `TransactionOperations`, etc. Spring Boot's component scanning will satisfy these automatically IF the beans are present in the context. The explicit `@Bean` methods above ensure all dependencies exist.
2. The `AnnotationBasedEventListenerAdapter` is **essential** — without it, Alarmd will start but **never receive any events**. It bridges the `@EventHandler` annotation on `Alarmd.onEvent()` to the `EventSubscriptionService`. Check the exact constructor in `features/events/api/`.
3. `EventUtilDaoImpl` itself has `@Autowired` dependencies on `AssetRecordDao`, `IpInterfaceDao`, `HwEntityDao`, `NodeDao`, and `SessionUtils`. These DAOs must also be available in the context (either via component scanning of `opennms-dao` or explicit `@Bean` definitions).
4. `KafkaEventSubscriptionService` implements `EventSubscriptionService`, so the `KafkaEventTransportConfiguration` bean satisfies this dependency — but the `@Bean` method there must expose it as `EventSubscriptionService` (not just as part of the adapter).

- [ ] **Step 2: Validate it compiles**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-boot-alarmd -am install`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add core/daemon-boot-alarmd/src/
git commit -m "feat: add AlarmdConfiguration with bean wiring for Spring Boot"
```

### Task 12: Alarmd Integration Test

This test starts the full Alarmd Spring Boot application with Testcontainers (PostgreSQL + Kafka), sends an event, and verifies an alarm is created.

**Files:**
- Create: `core/daemon-boot-alarmd/src/test/java/org/opennms/netmgt/alarmd/boot/AlarmdApplicationIT.java`

- [ ] **Step 1: Write the integration test**

Create `core/daemon-boot-alarmd/src/test/java/org/opennms/netmgt/alarmd/boot/AlarmdApplicationIT.java`:
```java
package org.opennms.netmgt.alarmd.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = AlarmdApplication.class)
@Testcontainers
class AlarmdApplicationIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("opennms")
                    .withUsername("opennms")
                    .withPassword("opennms");

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("opennms.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private EventIpcManager eventIpcManager;

    @Test
    void contextLoads() {
        assertThat(alarmDao).isNotNull();
        assertThat(eventIpcManager).isNotNull();
    }

    @Test
    void eventCreatesAlarm() {
        var event = new EventBuilder("uei.opennms.org/test/alarm", "test")
                .setNodeid(1)
                .getEvent();
        event.setAlarmData(new org.opennms.netmgt.xml.event.AlarmData());
        event.getAlarmData().setReductionKey("test:1");
        event.getAlarmData().setAlarmType(1);

        eventIpcManager.sendNow(event);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            var alarms = alarmDao.findAll();
            assertThat(alarms).isNotEmpty();
            assertThat(alarms.get(0).getReductionKey()).isEqualTo("test:1");
        });
    }
}
```

**Note for implementer:** This integration test requires the database schema to exist. In production, `db-init` (Liquibase) creates the schema. `hibernate.ddl-auto=create-drop` will NOT work — the OpenNMS entity model uses custom Hibernate types, database views, stored procedures, and Liquibase-managed features that Hibernate cannot auto-generate.

**Recommended approach:** Use the `db-init` container image (or the Liquibase migration JARs directly) to initialize the Testcontainers PostgreSQL instance before the Spring Boot context starts. Alternatively, extract the DDL from a running `db-init` container and load it as a SQL init script on the Testcontainer.

- [ ] **Step 2: Run the integration test**

Run: `cd core/daemon-boot-alarmd && ../../maven/bin/mvn verify -Dit.test=AlarmdApplicationIT -pl .`
Expected: PASS — context loads, event creates alarm

- [ ] **Step 3: Commit**

```bash
git add core/daemon-boot-alarmd/src/test/
git commit -m "test: add Alarmd Spring Boot integration test with Testcontainers"
```

---

## Chunk 6: Docker Integration

### Task 13: Update Build Infrastructure

Add the Alarmd Spring Boot fat JAR to the Docker image build pipeline.

**Files:**
- Modify: `opennms-container/delta-v/build.sh` (add fat JAR staging)
- Modify: `opennms-container/delta-v/Dockerfile.daemon` (add COPY for fat JAR)

- [ ] **Step 1: Update build.sh**

In `opennms-container/delta-v/build.sh`, add to `do_stage_daemon_jars()`:
```bash
# Spring Boot fat JARs
cp "$REPO_ROOT/core/daemon-boot-alarmd/target/org.opennms.core.daemon-boot-alarmd-${VERSION}.jar" \
   "$staging/daemon-boot-alarmd.jar"
```

- [ ] **Step 2: Update Dockerfile.daemon**

In `opennms-container/delta-v/Dockerfile.daemon`, add after the existing daemon-loader COPY lines:
```dockerfile
# Spring Boot fat JARs (migrated daemons)
COPY staging/daemon/daemon-boot-alarmd.jar /opt/daemon-boot-alarmd.jar
```

- [ ] **Step 3: Commit**

```bash
git add opennms-container/delta-v/build.sh opennms-container/delta-v/Dockerfile.daemon
git commit -m "build: add Alarmd Spring Boot fat JAR to Docker image pipeline"
```

### Task 14: Update docker-compose.yml

Switch the alarmd service from Karaf feature boot to Spring Boot `java -jar`.

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml` (alarmd service)

- [ ] **Step 1: Update alarmd service**

In `opennms-container/delta-v/docker-compose.yml`, replace the `alarmd` service definition.

Before:
```yaml
alarmd:
  profiles: [lite, passive, full]
  image: opennms/daemon-deltav:${VERSION}
  container_name: delta-v-alarmd
  hostname: alarmd
  depends_on:
    db-init:
      condition: service_completed_successfully
    kafka:
      condition: service_healthy
  environment:
    POSTGRES_HOST: postgres
    POSTGRES_PORT: "5432"
    POSTGRES_USER: opennms
    POSTGRES_PASSWORD: opennms
    POSTGRES_DB: opennms
    JAVA_OPTS: >-
      -Xms512m -Xmx1g
      ...
  volumes:
    - alarmd-data:/opt/sentinel/data
    - ./alarmd-overlay/etc:/opt/sentinel-etc-overlay:ro
  healthcheck:
    test: ["CMD-SHELL", "curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe || exit 1"]
    ...
```

After:
```yaml
alarmd:
  profiles: [lite, passive, full]
  image: opennms/daemon-deltav:${VERSION}
  container_name: delta-v-alarmd
  hostname: alarmd
  command: ["java", "-Xms512m", "-Xmx1g", "-jar", "/opt/daemon-boot-alarmd.jar"]
  depends_on:
    db-init:
      condition: service_completed_successfully
    kafka:
      condition: service_healthy
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/opennms
    SPRING_DATASOURCE_USERNAME: opennms
    SPRING_DATASOURCE_PASSWORD: opennms
    KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    OPENNMS_HOME: /opt/opennms
    JAVA_OPTS: >-
      -Dorg.opennms.tsid.node-id=2
  healthcheck:
    test: ["CMD-SHELL", "curl -sf http://localhost:8080/actuator/health || exit 1"]
    interval: 15s
    timeout: 10s
    retries: 20
    start_period: 30s
```

Key changes:
- `command:` runs `java -jar` instead of Karaf
- Environment variables use Spring Boot conventions
- No more volumes for Karaf overlay or sentinel data
- Healthcheck hits Actuator `/actuator/health` instead of Sentinel REST
- Faster `start_period` (30s vs 60s — no Karaf boot overhead)

- [ ] **Step 2: Commit**

```bash
git add opennms-container/delta-v/docker-compose.yml
git commit -m "feat: switch alarmd to Spring Boot in docker-compose"
```

### Task 15: End-to-End Validation

Build the full stack and run E2E tests to verify Alarmd on Spring Boot works alongside Karaf daemons.

- [ ] **Step 1: Build everything**

Run:
```bash
cd opennms-container/delta-v
./build.sh
```
Expected: BUILD SUCCESS — includes daemon-boot-alarmd fat JAR in Docker image

- [ ] **Step 2: Deploy the stack**

Run:
```bash
cd opennms-container/delta-v
./deploy.sh reset
./deploy.sh up full
```
Expected: All services start, including alarmd on Spring Boot

- [ ] **Step 3: Verify Alarmd health**

Run:
```bash
docker exec delta-v-alarmd curl -sf http://localhost:8080/actuator/health
```
Expected: `{"status":"UP"}`

- [ ] **Step 4: Run E2E tests**

Run:
```bash
cd opennms-container/delta-v
./test-e2e.sh
./test-passive-e2e.sh
./test-syslog-e2e.sh
```
Expected: All tests pass — Alarmd processes events and creates alarms, other Karaf daemons unaffected

- [ ] **Step 5: Commit any fixes from E2E testing**

```bash
git add -A
git commit -m "fix: address E2E test issues from Alarmd Spring Boot migration"
```

---

## Chunk 7: Cleanup and Documentation

### Task 16: Delete daemon-loader-alarmd

Now that Alarmd runs on Spring Boot, remove the Karaf daemon-loader.

**Files:**
- Delete: `core/daemon-loader-alarmd/` (entire directory)
- Modify: `core/pom.xml` (remove `<module>daemon-loader-alarmd</module>`)
- Modify: `container/features/src/main/resources/features.xml` (remove `opennms-daemon-alarmd` feature)
- Modify: `opennms-container/delta-v/build.sh` (remove alarmd daemon-loader JAR staging)
- Modify: `opennms-container/delta-v/Dockerfile.daemon` (remove daemon-loader-alarmd COPY)
- Delete: `opennms-container/delta-v/alarmd-overlay/` (Karaf overlay no longer needed)

- [ ] **Step 1: Remove module from core/pom.xml**

Delete the line: `<module>daemon-loader-alarmd</module>`

- [ ] **Step 2: Delete daemon-loader-alarmd directory**

Run: `rm -rf core/daemon-loader-alarmd`

- [ ] **Step 3: Remove feature from features.xml (source)**

In `container/features/src/main/resources/features.xml`, delete the `<feature name="opennms-daemon-alarmd"...>...</feature>` block.

- [ ] **Step 4: Remove feature from features.xml (daemon overlay)**

In `opennms-container/delta-v/daemon-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml`, delete the `<feature name="opennms-daemon-alarmd"...>...</feature>` block. This is the file that actually gets baked into the Docker image.

- [ ] **Step 5: Remove from build.sh and Dockerfile.daemon**

Remove the daemon-loader-alarmd JAR staging pair from `build.sh` and the COPY line from `Dockerfile.daemon` (if they exist — check first, as alarmd may load from the Karaf base image via features rather than staged JARs).

- [ ] **Step 6: Delete alarmd overlay**

Run: `rm -rf opennms-container/delta-v/alarmd-overlay`

- [ ] **Step 7: Validate full build**

Run: `./compile.pl -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 8: Run E2E tests again**

Run:
```bash
cd opennms-container/delta-v
./deploy.sh reset && ./deploy.sh up full
./test-e2e.sh
```
Expected: All tests pass with cleaned-up Alarmd

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "refactor: delete daemon-loader-alarmd — Alarmd now runs on Spring Boot 4"
```

### Task 17: Final PR

- [ ] **Step 1: Push branch and create PR**

```bash
git push delta-v HEAD
gh pr create --base develop --title "feat: migrate Alarmd to Spring Boot 4.0.3" --body "$(cat <<'EOF'
## Summary

- Migrates Alarmd from Karaf/OSGi to Spring Boot 4.0.3 as the prototype for all daemon migrations
- Creates `daemon-common` shared module (DataSource, Kafka event transport, health, DaemonSmartLifecycle)
- Creates `daemon-boot-alarmd` Spring Boot application
- Deletes RTCd (dead code)
- Establishes the pattern for migrating the remaining 13 daemons

## Test plan

- [ ] `./compile.pl -DskipTests` passes (all modules compile, Karaf daemons unaffected)
- [ ] Alarmd integration test passes (Testcontainers: PostgreSQL + Kafka)
- [ ] `./deploy.sh up full && ./test-e2e.sh` passes (Alarmd on Spring Boot alongside Karaf daemons)
- [ ] `./test-passive-e2e.sh` passes
- [ ] `./test-syslog-e2e.sh` passes
- [ ] Alarmd Actuator health endpoint returns UP
EOF
)"
```

---

## Explicitly Deferred Items

The following items from the design spec are intentionally NOT included in this plan. They will be addressed in follow-up plans:

### REST API Migration for Alarmd

The spec says `/api/v2/alarms` and `/api/v2/alarm-acknowledges` should migrate from the dead webapp into the Alarmd Spring Boot app as `@RestController` classes. This is deferred because:
1. The prototype's goal is proving the daemon lifecycle and event processing work on Spring Boot
2. REST API migration requires rewriting JAX-RS (`@Path`, `@GET`) to Spring MVC (`@GetMapping`, `@RestController`) — a separate effort
3. REST APIs can be added to the already-running Spring Boot app in a subsequent PR

### Remaining 13 Daemon Migrations

Each subsequent daemon follows the pattern established here. A separate plan will be created for each tier (Tier 2: Sink daemons, Tier 3: RPC daemons, Tier 4: Complex daemons) once Alarmd is validated.

### Drools Integration

`DroolsAlarmContext` (593 lines) provides rule-based alarm correlation. It is `@Autowired(required = false)` in `Alarmd`, so it's optional. The prototype can run without it. Drools integration will be added after the core Alarmd lifecycle works.
