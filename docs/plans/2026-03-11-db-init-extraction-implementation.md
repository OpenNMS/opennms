# db-init Extraction Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the 35.6 GB `opennms/horizon` image used by db-init with a ~200 MB standalone Spring Boot 4.0.3 CLI app.

**Architecture:** New `core/db-init/` module wraps the existing `core/schema/` Migrator as a Spring Boot `CommandLineRunner`. Fat JAR packaged into a minimal Alpine JRE image. Docker-compose updated to use new image with standard Spring Boot env vars.

**Tech Stack:** Java 21, Spring Boot 4.0.3, Liquibase 3.6.3, PostgreSQL JDBC, Testcontainers, eclipse-temurin:21-jre-alpine

**Design Spec:** `docs/plans/2026-03-11-db-init-extraction-design.md`

---

## File Map

| Action | File | Purpose |
|--------|------|---------|
| Create | `core/db-init/pom.xml` | Maven POM: Spring Boot 4.0.3, Java 21, fat JAR |
| Create | `core/db-init/src/main/java/org/opennms/core/dbinit/DbInitApplication.java` | Spring Boot main class |
| Create | `core/db-init/src/main/java/org/opennms/core/dbinit/DataSourceConfig.java` | Two-datasource config (admin + app) |
| Create | `core/db-init/src/main/java/org/opennms/core/dbinit/DbInitProperties.java` | @ConfigurationProperties record |
| Create | `core/db-init/src/main/java/org/opennms/core/dbinit/DbInitRunner.java` | CommandLineRunner → Migrator |
| Create | `core/db-init/src/main/resources/application.yml` | Default config + logging |
| Create | `core/db-init/src/test/java/org/opennms/core/dbinit/DbInitIntegrationTest.java` | Testcontainers integration test |
| Create | `core/db-init/Dockerfile` | Alpine JRE 21 image |
| Modify | `core/pom.xml:49` | Add `<module>db-init</module>` |
| Modify | `opennms-container/delta-v/docker-compose.yml:62-82` | Replace db-init service config |
| Modify | `opennms-container/delta-v/docker-compose.yml:678-679` | Remove db-init-etc/db-init-data volumes |
| Modify | `opennms-container/delta-v/build.sh` | Add `do_db_init_image()` |
| Modify | `BUILD.md` | Add db-init rebuild instructions |

---

## Task 1: Create the Maven Module

**Files:**
- Create: `core/db-init/pom.xml`
- Modify: `core/pom.xml:49` (add module)

- [ ] **Step 1: Create `core/db-init/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!--
      This module does NOT inherit from the OpenNMS parent POM.
      It is a standalone Spring Boot application with its own dependency
      management, Java version, and build lifecycle.
    -->
    <groupId>org.opennms.core</groupId>
    <artifactId>org.opennms.core.db-init</artifactId>
    <version>36.0.0-SNAPSHOT</version>
    <name>OpenNMS :: Core :: DB Init</name>
    <description>Standalone Spring Boot CLI for PostgreSQL schema migration</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.3</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>21</java.version>
        <opennms.version>36.0.0-SNAPSHOT</opennms.version>
        <liquibase.version>3.6.3</liquibase.version>
    </properties>

    <dependencies>
        <!-- Migrator library (Migrator.java, custom Liquibase extensions) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.schema</artifactId>
            <version>${opennms.version}</version>
            <exclusions>
                <!-- Exclude transitive Spring 4.x / Liquibase managed by Boot -->
                <exclusion>
                    <groupId>org.opennms.dependencies</groupId>
                    <artifactId>spring-dependencies</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.opennms.dependencies</groupId>
                    <artifactId>liquibase-dependencies</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <!-- Changelog XMLs (separate assembly JAR, NOT in main schema JAR) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.schema</artifactId>
            <version>${opennms.version}</version>
            <classifier>liquibase</classifier>
        </dependency>

        <!-- Spring Boot (JDBC only, no web server) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <!-- PostgreSQL JDBC driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>

        <!-- Liquibase — pinned to 3.6.3 to match core/schema compilation -->
        <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-core</artifactId>
            <version>${liquibase.version}</version>
        </dependency>

        <!-- commons-lang (transitive from core.schema, but explicit) -->
        <dependency>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
            <version>2.6</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>db-init-${project.version}</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

Key design decisions in this POM:
- **Parent is `spring-boot-starter-parent`**, not the OpenNMS root POM. This avoids
  the Java `[17,18)` enforcer, the OSGi bundle plugin, and the legacy dependency
  management. The module version is hardcoded to `36.0.0-SNAPSHOT`.
- **Excludes `spring-dependencies` and `liquibase-dependencies`** POM aggregators
  from `core.schema` — Spring Boot manages its own Spring and we pin Liquibase to 3.6.3.
- **`finalName`** ensures the JAR is named `db-init-36.0.0-SNAPSHOT.jar` for the Dockerfile COPY.

- [ ] **Step 2: Add module to `core/pom.xml`**

In `core/pom.xml`, add `<module>db-init</module>` after the `schema` module (line 49):

```xml
    <module>schema</module>
    <module>db-init</module>
```

- [ ] **Step 3: Create directory structure**

```bash
mkdir -p core/db-init/src/main/java/org/opennms/core/dbinit
mkdir -p core/db-init/src/main/resources
mkdir -p core/db-init/src/test/java/org/opennms/core/dbinit
```

- [ ] **Step 4: Verify the POM resolves**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
  ./maven/bin/mvn -pl core/db-init -N validate
```

If Java 21 is not installed, install it first:
```bash
brew install --cask temurin@21
```

Expected: `BUILD SUCCESS` (validates POM syntax and dependency resolution).

Note: If Spring Boot 4.0.3 cannot resolve the `spring-boot-starter-parent`, check
Maven Central. Fall back to the latest available 4.0.x release.

- [ ] **Step 5: Commit**

```bash
git add core/db-init/pom.xml core/pom.xml
git commit -m "feat(db-init): create Spring Boot module skeleton with POM"
```

---

## Task 2: Create the Application Classes

**Files:**
- Create: `core/db-init/src/main/java/org/opennms/core/dbinit/DbInitApplication.java`
- Create: `core/db-init/src/main/java/org/opennms/core/dbinit/DataSourceConfig.java`
- Create: `core/db-init/src/main/java/org/opennms/core/dbinit/DbInitProperties.java`
- Create: `core/db-init/src/main/java/org/opennms/core/dbinit/DbInitRunner.java`
- Create: `core/db-init/src/main/resources/application.yml`

- [ ] **Step 1: Create `DbInitApplication.java`**

```java
package org.opennms.core.dbinit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)
public class DbInitApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
            SpringApplication.run(DbInitApplication.class, args)
        ));
    }
}
```

- [ ] **Step 2: Create `DataSourceConfig.java`**

```java
package org.opennms.core.dbinit;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.admin")
    public DataSourceProperties adminDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource adminDataSource() {
        return adminDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties appDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return appDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }
}
```

Note: Using `DataSourceProperties` beans with `@ConfigurationProperties` is the
standard Spring Boot pattern for multiple datasources. This avoids direct use of
`DataSourceBuilder` which can have issues with property binding for the secondary
datasource.

- [ ] **Step 3: Create `DbInitProperties.java`**

```java
package org.opennms.core.dbinit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opennms.dbinit")
public record DbInitProperties(
    String databaseName,
    String databaseUser,
    String databasePassword,
    String adminUser,
    String adminPassword,
    boolean createUser,
    boolean createDatabase,
    boolean iplike,
    boolean timescaleDb,
    boolean vacuum,
    boolean fullVacuum
) {}
```

- [ ] **Step 4: Create `DbInitRunner.java`**

```java
package org.opennms.core.dbinit;

import javax.sql.DataSource;

import org.opennms.core.schema.Migrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DbInitRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DbInitRunner.class);

    private final DataSource adminDataSource;
    private final DataSource dataSource;
    private final ApplicationContext context;
    private final DbInitProperties properties;

    public DbInitRunner(DataSource adminDataSource,
                        DataSource dataSource,
                        ApplicationContext context,
                        DbInitProperties properties) {
        this.adminDataSource = adminDataSource;
        this.dataSource = dataSource;
        this.context = context;
        this.properties = properties;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Starting database initialization...");

        var migrator = new Migrator();
        migrator.setAdminDataSource(adminDataSource);
        migrator.setDataSource(dataSource);
        migrator.setApplicationContext(context);
        migrator.setDatabaseName(properties.databaseName());
        migrator.setDatabaseUser(properties.databaseUser());
        migrator.setDatabasePassword(properties.databasePassword());
        migrator.setAdminUser(properties.adminUser());
        migrator.setAdminPassword(properties.adminPassword());
        migrator.setCreateUser(properties.createUser());
        migrator.setCreateDatabase(properties.createDatabase());

        migrator.setupDatabase(
            true,
            properties.vacuum(),
            properties.fullVacuum(),
            properties.iplike(),
            properties.timescaleDb()
        );

        LOG.info("Database initialization complete.");
    }
}
```

- [ ] **Step 5: Create `application.yml`**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/opennms
    username: opennms
    password: opennms
    admin:
      url: jdbc:postgresql://localhost:5432/template1
      username: postgres
      password: ""
  liquibase:
    enabled: false

opennms:
  dbinit:
    database-name: opennms
    database-user: opennms
    database-password: opennms
    admin-user: postgres
    admin-password: ""
    create-user: true
    create-database: true
    iplike: true
    timescale-db: false
    vacuum: false
    full-vacuum: false

logging:
  level:
    org.opennms.core.schema: INFO
    org.opennms.core.dbinit: INFO
    liquibase: INFO
```

- [ ] **Step 6: Verify compilation**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
  ./maven/bin/mvn -pl core/db-init -DskipTests compile
```

Expected: `BUILD SUCCESS`. If there are Spring API compatibility issues between
`Migrator.java` (compiled against Spring 4.2.x) and Spring 7.x at runtime, they
will surface here or in the next task's integration test.

Potential issues to watch for:
- `SpringLiquibase` class in Liquibase 3.6.3 may conflict with Spring Boot 4.0.x's
  managed Liquibase version. The POM pins `liquibase-core` to 3.6.3, but verify
  no other transitive path pulls in a newer version.
- If `commons-lang` 2.6 conflicts, may need an exclusion.

- [ ] **Step 7: Commit**

```bash
git add core/db-init/src/
git commit -m "feat(db-init): add Spring Boot application classes and config"
```

---

## Task 3: Integration Test with Testcontainers

**Files:**
- Create: `core/db-init/src/test/java/org/opennms/core/dbinit/DbInitIntegrationTest.java`

- [ ] **Step 1: Create `DbInitIntegrationTest.java`**

```java
package org.opennms.core.dbinit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DbInitIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("template1")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Admin datasource — connects as postgres to template1
        registry.add("spring.datasource.admin.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.admin.username", postgres::getUsername);
        registry.add("spring.datasource.admin.password", postgres::getPassword);

        // App datasource — Migrator will CREATE DATABASE opennms, then
        // connect to it. Point at same host but with opennms DB name.
        String opennmsUrl = postgres.getJdbcUrl().replace("/template1", "/opennms");
        registry.add("spring.datasource.url", () -> opennmsUrl);
        registry.add("spring.datasource.username", () -> "opennms");
        registry.add("spring.datasource.password", () -> "opennms");

        // DbInit properties — Migrator creates the user and database
        registry.add("opennms.dbinit.database-name", () -> "opennms");
        registry.add("opennms.dbinit.database-user", () -> "opennms");
        registry.add("opennms.dbinit.database-password", () -> "opennms");
        registry.add("opennms.dbinit.admin-user", postgres::getUsername);
        registry.add("opennms.dbinit.admin-password", postgres::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DbInitRunner runner;

    @Test
    void migrationCreatesAlarmTable() throws Exception {
        // The CommandLineRunner already ran during @SpringBootTest startup.
        // Verify the alarms table exists (created by Liquibase changesets).
        try (var conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, "public", "alarms", null)) {
            assertThat(rs.next())
                    .as("alarms table should exist after migration")
                    .isTrue();
        }
    }

    @Test
    void migrationCreatesNodeTable() throws Exception {
        try (var conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, "public", "node", null)) {
            assertThat(rs.next())
                    .as("node table should exist after migration")
                    .isTrue();
        }
    }

    @Test
    void eventsTableDoesNotExist() throws Exception {
        // Delta-V: events table is eliminated (36.0.0 changeset drops it)
        try (var conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, "public", "events", null)) {
            assertThat(rs.next())
                    .as("events table should NOT exist (dropped by 36.0.0)")
                    .isFalse();
        }
    }

    @Test
    void migrationIsIdempotent() {
        // Running a second time should be a no-op (Liquibase skips applied changesets)
        assertThatNoException().isThrownBy(() -> runner.run());
    }
}
```

Design notes:
- The test container is created with `template1` as the initial database. Migrator
  connects as postgres admin, creates the `opennms` user and `opennms` database,
  then runs all changesets against `opennms`.
- The `@SpringBootTest` annotation triggers the full application context, which
  runs `DbInitRunner` automatically. Tests verify the post-migration state.
- `eventsTableDoesNotExist` validates the Delta-V iron rule: events are never
  written to PostgreSQL.

- [ ] **Step 2: Run the integration test**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
  ./maven/bin/mvn -pl core/db-init verify
```

Expected: All 4 tests PASS. The first run takes ~30s (PostgreSQL container startup +
665 changesets). Subsequent runs are faster (container reuse).

If the test fails, check:
1. `Migrator` Spring API compatibility — `Resource`, `ApplicationContext` signatures
2. Liquibase 3.6.3 vs Spring Boot's managed version — ensure 3.6.3 wins
3. Changelog discovery — ensure `-liquibase.jar` is on classpath and `core.schema`
   filter matches the nested JAR path
4. `iplike.sql` resource — must be loadable from `core.schema` main JAR classpath

- [ ] **Step 3: Commit**

```bash
git add core/db-init/src/test/
git commit -m "test(db-init): add Testcontainers integration test for schema migration"
```

---

## Task 4: Create the Dockerfile

**Files:**
- Create: `core/db-init/Dockerfile`
- Create: `core/db-init/.dockerignore`

- [ ] **Step 1: Create `Dockerfile`**

```dockerfile
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 10001 opennms && \
    adduser -u 10001 -G opennms -D opennms

COPY --chown=opennms:opennms target/db-init-*.jar /app/db-init.jar

USER opennms

# JAVA_TOOL_OPTIONS is read natively by the JVM — no shell wrapper needed.
# Override via env var in docker-compose: JAVA_TOOL_OPTIONS=-Xms256m -Xmx512m
ENTRYPOINT ["java", "-jar", "/app/db-init.jar"]
```

- [ ] **Step 2: Create `.dockerignore`**

```
src/
pom.xml
*.md
target/classes
target/test-classes
target/maven-*
target/surefire-*
```

- [ ] **Step 3: Build the fat JAR**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
  ./maven/bin/mvn -pl core/db-init -DskipTests package
```

Expected: `target/db-init-36.0.0-SNAPSHOT.jar` (~50-80 MB)

- [ ] **Step 4: Build the Docker image**

```bash
cd core/db-init && docker build -t opennms/db-init:36.0.0-SNAPSHOT -t opennms/db-init:latest .
```

Expected: Image size ~150-200 MB.

Verify:
```bash
docker images opennms/db-init
```

- [ ] **Step 5: Smoke test the image against the running Delta-V postgres**

```bash
docker run --rm --network delta-v_default \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/opennms \
  -e SPRING_DATASOURCE_USERNAME=opennms \
  -e SPRING_DATASOURCE_PASSWORD=opennms \
  -e SPRING_DATASOURCE_ADMIN_URL=jdbc:postgresql://postgres:5432/template1 \
  -e SPRING_DATASOURCE_ADMIN_USERNAME=postgres \
  -e SPRING_DATASOURCE_ADMIN_PASSWORD=opennms \
  -e OPENNMS_DBINIT_ADMIN_PASSWORD=opennms \
  opennms/db-init:36.0.0-SNAPSHOT
```

Expected: Runs all 665 changesets (or skips already-applied ones), exits with code 0.

- [ ] **Step 6: Commit**

```bash
git add core/db-init/Dockerfile core/db-init/.dockerignore
git commit -m "feat(db-init): add Dockerfile for Alpine JRE 21 image"
```

---

## Task 5: Update Docker Compose and Build Infrastructure

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml:62-82`
- Modify: `opennms-container/delta-v/docker-compose.yml:678-679`
- Modify: `opennms-container/delta-v/build.sh`
- Modify: `BUILD.md`

- [ ] **Step 1: Update db-init service in `docker-compose.yml`**

Replace lines 62-82 (the current db-init service definition):

**Before:**
```yaml
  db-init:
    image: opennms/horizon:${VERSION}
    container_name: delta-v-db-init
    command: ["-i"]
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      POSTGRES_HOST: postgres
      POSTGRES_PORT: "5432"
      POSTGRES_USER: opennms
      POSTGRES_PASSWORD: opennms
      OPENNMS_DBNAME: opennms
      OPENNMS_DBUSER: opennms
      OPENNMS_DBPASS: opennms
      JAVA_OPTS: >-
        -Xms512m -Xmx1g
        -Djava.security.egd=file:/dev/./urandom
    volumes:
      - db-init-etc:/opt/opennms/etc
      - db-init-data:/opennms-data
```

**After:**
```yaml
  db-init:
    image: opennms/db-init:${VERSION}
    container_name: delta-v-db-init
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/opennms
      SPRING_DATASOURCE_USERNAME: opennms
      SPRING_DATASOURCE_PASSWORD: opennms
      SPRING_DATASOURCE_ADMIN_URL: jdbc:postgresql://postgres:5432/template1
      SPRING_DATASOURCE_ADMIN_USERNAME: postgres
      SPRING_DATASOURCE_ADMIN_PASSWORD: opennms
      OPENNMS_DBINIT_ADMIN_PASSWORD: opennms
      JAVA_TOOL_OPTIONS: -Xms256m -Xmx512m
```

Changes: new image, no command, no volumes, standard Spring Boot env vars,
`JAVA_TOOL_OPTIONS` instead of `JAVA_OPTS`.

- [ ] **Step 2: Remove db-init volumes from the volumes section**

At the bottom of `docker-compose.yml`, delete these two lines:
```yaml
  db-init-etc:
  db-init-data:
```

- [ ] **Step 3: Add `do_db_init_image()` to `build.sh`**

Add after the `do_webapp_overlay()` function:

```bash
do_db_init_image() {
    log "Building db-init image (opennms/db-init:$VERSION)..."
    cd "$REPO_ROOT"
    JAVA_HOME="${JAVA_HOME_21:-/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home}" \
        ./maven/bin/mvn -pl core/db-init -DskipTests package
    cd "$REPO_ROOT/core/db-init"
    docker build -t "opennms/db-init:$VERSION" -t "opennms/db-init:latest" .
}
```

Update the `do_images()` function to call it:

```bash
do_images() {
    ...existing horizon and daemon image builds...

    do_db_init_image

    log "Docker images built:"
    docker images --format "  {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep -E "(horizon|daemon|sentinel|db-init)" | head -15
}
```

Update the `main()` `all` case to call `do_db_init_image` before `do_images`:

```bash
        all)
            do_compile
            do_assemble
            do_webapp_overlay
            do_db_init_image
            do_images
            ...
```

- [ ] **Step 4: Update `BUILD.md`**

Add to the "Docker Images" table in step 4:

```markdown
| `opennms/db-init` | `core/db-init/` | Schema migration (run-and-exit) |
```

Add a new subsection under "Rebuilding Individual Components":

```markdown
### Changed Liquibase schema or db-init module

```bash
./maven/bin/mvn -DskipTests -pl core/schema,core/db-init install
cd core/db-init && docker build -t opennms/db-init:36.0.0-SNAPSHOT .
cd opennms-container/delta-v
COMPOSE_PROFILES=full docker compose down -v
COMPOSE_PROFILES=full docker compose up -d
```
```

- [ ] **Step 5: Commit**

```bash
git add opennms-container/delta-v/docker-compose.yml opennms-container/delta-v/build.sh BUILD.md
git commit -m "feat(db-init): update docker-compose and build.sh for standalone db-init image"
```

---

## Task 6: End-to-End Verification

- [ ] **Step 1: Tear down the existing Delta-V stack**

```bash
cd opennms-container/delta-v
COMPOSE_PROFILES=full docker compose down -v
```

- [ ] **Step 2: Build the db-init image (if not already done)**

```bash
cd core/db-init && docker build -t opennms/db-init:36.0.0-SNAPSHOT -t opennms/db-init:latest . && cd ../..
```

- [ ] **Step 3: Bring up Delta-V with the new db-init**

```bash
cd opennms-container/delta-v
docker compose up -d
```

Expected: db-init starts, runs migrations, exits with code 0. Webapp starts after.

Verify:
```bash
docker compose ps -a
```

Expected output: `delta-v-db-init` shows `Exited (0)`.

- [ ] **Step 4: Verify webapp is healthy**

```bash
curl -sf -u admin:admin http://localhost:8980/opennms/rest/info
```

Expected: JSON response with version info.

- [ ] **Step 5: Bring up full profile**

```bash
COMPOSE_PROFILES=full docker compose up -d
```

Wait 60s, then:
```bash
COMPOSE_PROFILES=full docker compose ps
```

Expected: All 16 daemon containers + webapp show `(healthy)`.

- [ ] **Step 6: Run E2E test (if passive profile services are up)**

```bash
./test-e2e.sh
```

Expected: All 11 tests pass (3 phases).

- [ ] **Step 7: Verify image size reduction**

```bash
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | grep -E "db-init|horizon"
```

Expected: `opennms/db-init` is ~150-200 MB vs `opennms/horizon` at 35.6 GB.

- [ ] **Step 8: Commit (if any fixups were needed)**

```bash
git add -A && git commit -m "fix(db-init): address issues found during E2E verification"
```

Only commit if changes were required. If E2E passed cleanly, skip this step.

---

## Summary

| Task | Description | Commits |
|------|-------------|---------|
| 1 | Maven module skeleton | `feat(db-init): create Spring Boot module skeleton with POM` |
| 2 | Application classes + config | `feat(db-init): add Spring Boot application classes and config` |
| 3 | Testcontainers integration test | `test(db-init): add Testcontainers integration test for schema migration` |
| 4 | Dockerfile + image build | `feat(db-init): add Dockerfile for Alpine JRE 21 image` |
| 5 | Docker compose + build.sh + BUILD.md | `feat(db-init): update docker-compose and build.sh for standalone db-init image` |
| 6 | E2E verification | (fixup commit only if needed) |
