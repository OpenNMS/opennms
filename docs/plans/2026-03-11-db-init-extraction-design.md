# Design: Extract db-init into a Standalone Spring Boot Container

**Date:** 2026-03-11
**Status:** Proposed
**Branch:** eventbus-redesign

## Problem

The db-init service runs Liquibase schema migrations against PostgreSQL, then exits.
Today it uses the `opennms/horizon` image (35.6 GB) — the full Horizon distribution
with Karaf, Jetty, all MIBs, reports, and the legacy webapp. The only code it
actually executes is `Migrator.java` (1107 lines) with Liquibase and a PostgreSQL
JDBC driver.

This is the largest image in the Delta-V stack by an order of magnitude. It takes
7-8 minutes to build, transfers 17 GB of Docker context, and is architecturally
misleading — a batch job wearing a monolith's clothes.

## Goals

1. Reduce the db-init image from 35.6 GB to ~200 MB
2. Establish the **library + application** module pattern for future Spring Boot extractions
3. Use modern Java 21 and Spring Boot 4.0.x as the standard for all future greenfield modules
4. Zero changes to the existing `core/schema/` module (Migrator stays a reusable library)
5. Preserve all existing db-init behavior: create user, create database, PL/pgSQL, iplike, 665 changesets

## Non-Goals

- Migrating any daemon container to Spring Boot (future work)
- Upgrading the legacy codebase's Spring 4.2.x (only the new module uses Spring Boot 4.0.x)
- Changing the Liquibase changelog structure or migration logic
- TimescaleDB support (currently a no-op in Delta-V)

## Architecture

### Module Structure

```
core/schema/                              ← existing, unchanged
  src/main/java/.../Migrator.java           library: migration logic
  src/main/java/.../ExistingResourceAccessor.java
  src/main/java/.../MigrationException.java
  src/main/java/liquibase/ext/opennms/      custom Liquibase extensions
  src/main/liquibase/                       665 changesets in 80+ XMLs
  target/*-liquibase.jar                    changelog assembly JAR

core/db-init/                             ← new Spring Boot application
  pom.xml                                  spring-boot-maven-plugin, Java 21
  Dockerfile                               eclipse-temurin:21-jre-alpine
  src/main/java/
    org/opennms/core/dbinit/
      DbInitApplication.java               @SpringBootApplication + main()
      DbInitRunner.java                    CommandLineRunner → Migrator
      DbInitProperties.java               @ConfigurationProperties for OpenNMS-specific settings
      DataSourceConfig.java                Two-datasource configuration (admin + app)
  src/main/resources/
    application.yml                        datasource defaults, logging config
```

This establishes the pattern for future daemon extractions:

```
existing-module/     ← library (business logic, unchanged)
new-module-app/      ← Spring Boot app (thin shell, Dockerfile)
```

### Spring Boot Application

**Framework:** Spring Boot 4.0.3 (Spring Framework 7.x, Jakarta EE 11)
**Java:** 21 (LTS)
**Packaging:** Executable fat JAR via `spring-boot-maven-plugin`
**Web server:** None — no `spring-boot-starter-web`, headless CLI only

The application consists of four classes:

#### DbInitApplication.java

```java
@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)
public class DbInitApplication {
    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
            SpringApplication.run(DbInitApplication.class, args)
        ));
    }
}
```

Spring Boot runs all `CommandLineRunner` beans, then `SpringApplication.exit()`
returns the appropriate exit code (0 on success, 1 on failure).

#### DataSourceConfig.java

Configures two `DataSource` beans:

- **admin** — connects as the PostgreSQL superuser (e.g., `postgres`) to `template1`.
  Used by Migrator to `CREATE DATABASE`, `CREATE USER`, check encoding.
- **app** — connects as the application user (e.g., `opennms`) to the target database.
  Used by Liquibase for all 665 changesets.

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.admin")
    public DataSource adminDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

#### DbInitProperties.java

```java
@ConfigurationProperties(prefix = "opennms.dbinit")
public record DbInitProperties(
    String databaseName,       // default: "opennms"
    String databaseUser,       // default: "opennms" — used by CREATE USER and Liquibase params
    String databasePassword,   // default: "opennms"
    String adminUser,          // default: "postgres" — passed to Liquibase as admin param
    String adminPassword,      // default: ""
    boolean createUser,        // default: true
    boolean createDatabase,    // default: true
    boolean iplike,            // default: true
    boolean timescaleDb,       // default: false
    boolean vacuum,            // default: false
    boolean fullVacuum         // default: false
) {}
```

#### DbInitRunner.java

```java
@Component
public class DbInitRunner implements CommandLineRunner {

    private final DataSource adminDataSource;
    private final DataSource dataSource;
    private final ApplicationContext context;
    private final DbInitProperties properties;

    // constructor injection

    @Override
    public void run(String... args) throws Exception {
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
            true,                        // updateDatabase
            properties.vacuum(),
            properties.fullVacuum(),
            properties.iplike(),
            properties.timescaleDb()
        );
    }
}
```

### Configuration

**application.yml defaults:**

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
    liquibase: INFO
```

All values are overridable via environment variables using Spring Boot's
relaxed binding:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/opennms
SPRING_DATASOURCE_USERNAME=opennms
SPRING_DATASOURCE_PASSWORD=opennms
SPRING_DATASOURCE_ADMIN_URL=jdbc:postgresql://postgres:5432/template1
SPRING_DATASOURCE_ADMIN_USERNAME=postgres
SPRING_DATASOURCE_ADMIN_PASSWORD=
```

### Docker Image

**Dockerfile:**

```dockerfile
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 10001 opennms && \
    adduser -u 10001 -G opennms -D opennms

COPY --chown=opennms:opennms target/db-init-*.jar /app/db-init.jar

USER opennms

# JAVA_TOOL_OPTIONS is read natively by the JVM — no shell wrapper needed.
# Set via env var in docker-compose: JAVA_TOOL_OPTIONS=-Xms256m -Xmx512m
ENTRYPOINT ["java", "-jar", "/app/db-init.jar"]
```

**Estimated image size:** ~150-200 MB
- eclipse-temurin:21-jre-alpine base: ~100 MB
- Spring Boot fat JAR (JDBC + Liquibase + Migrator + changelogs): ~50-80 MB

### Docker Compose Changes

**Before (docker-compose.yml):**

```yaml
db-init:
  image: opennms/horizon:${VERSION}
  command: ["-i"]
  environment:
    POSTGRES_HOST: postgres
    POSTGRES_PORT: "5432"
    POSTGRES_USER: opennms
    POSTGRES_PASSWORD: opennms
    OPENNMS_DBNAME: opennms
    OPENNMS_DBUSER: opennms
    OPENNMS_DBPASS: opennms
    JAVA_OPTS: -Xms512m -Xmx1g
  volumes:
    - db-init-etc:/opt/opennms/etc
    - db-init-data:/opennms-data
  depends_on:
    postgres:
      condition: service_healthy
```

**After:**

```yaml
db-init:
  image: opennms/db-init:${VERSION}
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/opennms
    SPRING_DATASOURCE_USERNAME: opennms
    SPRING_DATASOURCE_PASSWORD: opennms
    SPRING_DATASOURCE_ADMIN_URL: jdbc:postgresql://postgres:5432/template1
    SPRING_DATASOURCE_ADMIN_USERNAME: postgres
    SPRING_DATASOURCE_ADMIN_PASSWORD: ""
    JAVA_TOOL_OPTIONS: -Xms256m -Xmx512m
  depends_on:
    postgres:
      condition: service_healthy
```

Key changes:
- Image: `opennms/horizon` → `opennms/db-init` (35.6 GB → ~200 MB)
- No `command:` needed (Spring Boot app runs migrations by default)
- No volumes needed (no etc-pristine, no opennms-data, no confd)
- Standard Spring Boot env vars replace OpenNMS-specific ones
- Less memory needed (256m-512m vs 512m-1g)
- All downstream `service_completed_successfully` dependencies unchanged

### Dependencies

The `core/db-init` POM requires two artifacts from `core/schema`:

1. **Main JAR** (`org.opennms.core.schema`) — contains `Migrator.java`,
   `ExistingResourceAccessor`, `MigrationException`, and custom Liquibase extensions.
2. **Liquibase assembly JAR** (`org.opennms.core.schema`, classifier `liquibase`) —
   contains the 80+ changelog XML files. These are NOT in the main JAR; the schema
   module's `src/main/liquibase/` directory is only included via a Maven assembly
   plugin into a separate `-liquibase.jar`.

Both JARs must be on the classpath. `Migrator.getLiquibaseChangelogs()` uses
`ApplicationContext.getResources("classpath*:/changelog.xml")` to discover changelogs.
The production changelog filter checks that the resource URI contains `core.schema`
(the artifact ID), which matches the `-liquibase.jar` filename inside the fat JAR.

Spring Boot's Liquibase auto-configuration must be **disabled** — the application
manages Liquibase directly via `Migrator`, not via Spring Boot's `SpringLiquibaseAutoConfiguration`.
Add `@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)` or set
`spring.liquibase.enabled=false` in `application.yml`.

Additional dependencies:
- `spring-boot-starter-jdbc` — datasource auto-configuration, HikariCP
- `postgresql` — JDBC driver
- `liquibase-core` (version 3.6.3, pinned) — must match the version `core/schema` was compiled against

### core/schema Compatibility with Spring 7.x

`Migrator.java` uses these Spring APIs:
- `org.springframework.core.io.Resource` — stable across Spring 4→7
- `org.springframework.context.ApplicationContext` — stable across Spring 4→7
- `liquibase.integration.spring.SpringLiquibase` — Liquibase's own Spring integration

These APIs have not changed signatures between Spring 4.2.x and Spring 7.x.
The `core/schema` module's POM declares Spring as a `provided` dependency (it's
an OSGi bundle), so the Spring Boot fat JAR's dependency management will supply
Spring 7.x at runtime. No changes to `core/schema` source code are needed.

If the `core/schema` POM enforces a specific Spring version at compile time that
conflicts, the db-init module can shade or use Maven dependency management to
override. This is a build-time concern, not a design concern.

### Java 21 Enforcer Exclusion

The root POM enforces `[17,18)` for Java. The `core/db-init` module needs Java 21.
Options:
- Skip the enforcer plugin in `core/db-init/pom.xml` via `<plugin><skip>true</skip>`
- Or add a profile that relaxes the range for Spring Boot modules

Since db-init produces a standalone fat JAR (never loaded into Karaf), Java 21
bytecode won't leak into the rest of the build.

### Build Integration

**build.sh changes:**

Add a new `do_db_init_image()` function:

```bash
do_db_init_image() {
    log "Building db-init image (opennms/db-init:$VERSION)..."
    cd "$REPO_ROOT/core/db-init"
    $REPO_ROOT/maven/bin/mvn -DskipTests package
    docker build -t "opennms/db-init:$VERSION" -t "opennms/db-init:latest" .
}
```

The db-init image build takes seconds (no 17 GB context transfer), compared to
7-8 minutes for the Horizon image.

**BUILD.md updates:**

Add db-init to the "Rebuilding Individual Components" section:

```bash
# Changed Liquibase schema
./maven/bin/mvn -DskipTests -pl core/schema,core/db-init install
cd core/db-init && docker build -t opennms/db-init:36.0.0-SNAPSHOT .
docker compose up -d --force-recreate db-init
```

## Testing

### Unit Tests

`DbInitRunner` is thin enough that it doesn't need its own unit tests — the logic
lives in `Migrator` which has existing tests in `core/schema`.

### Integration Test (Testcontainers)

A single integration test in `core/db-init` verifies the full migration path:

```java
@SpringBootTest
@Testcontainers
class DbInitIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.admin.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.admin.username", postgres::getUsername);
        registry.add("spring.datasource.admin.password", postgres::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Test
    void migrationCreatesAlarmTable() throws Exception {
        // CommandLineRunner already ran — verify schema exists
        try (var conn = dataSource.getConnection();
             var rs = conn.getMetaData().getTables(null, "public", "alarms", null)) {
            assertTrue(rs.next(), "alarms table should exist after migration");
        }
    }

    @Test
    void migrationIsIdempotent() throws Exception {
        // Run a second time — should be a no-op
        // (Liquibase skips already-applied changesets)
        var runner = new DbInitRunner(/* inject deps */);
        assertDoesNotThrow(() -> runner.run());
    }
}
```

### E2E Verification

After deployment, the existing `test-e2e.sh` validates the full pipeline.
If db-init fails, all downstream containers fail to start (`service_completed_successfully`),
making the failure immediately visible.

## Migration Path

1. Build and test the new `core/db-init/` module against a clean PostgreSQL instance
2. Verify all 665 changesets apply successfully (same as current db-init)
3. Verify idempotency (run twice — second run should be a no-op)
4. Update `docker-compose.yml` to use `opennms/db-init` image
5. Remove the `db-init-etc` and `db-init-data` named volumes (no longer needed)
6. Update `build.sh` to build the db-init image
7. Update `BUILD.md` with new instructions
8. Run `test-e2e.sh` to verify the full pipeline still works

## Future Pattern

This extraction establishes the template for migrating each daemon to Spring Boot:

```
Phase 1 (this design):  core/schema      → core/db-init        (batch job)
Phase 2 (future):       features/alarms  → features/alarmd-app  (long-running service)
Phase 3 (future):       opennms-webapp   → webapp-app           (REST API server)
...
Phase N:                each daemon      → daemon-app            (Spring Boot service)
```

Each extraction follows the same pattern:
1. Existing module stays as a library (no framework opinion)
2. New `-app` module wraps it in Spring Boot
3. Dockerfile produces a minimal image
4. Docker-compose swaps the image reference

The Karaf/OSGi containers continue running unchanged until their daemon is extracted.
