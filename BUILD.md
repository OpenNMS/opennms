# Building Delta-V from Scratch

This guide covers building the OpenNMS Delta-V microservice architecture from source,
producing Docker images, and deploying locally.

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java | JDK 17 (exact) | Enforced range `[17,18)`. Temurin recommended. |
| Java | JDK 21 | Required for `core/db-init` Spring Boot module only. |
| Docker Desktop | 4.x+ | **16 GB memory** required for full profile (16 JVMs). 8 GB causes OOM kills. |
| Perl | 5.x | Required by `compile.pl` / `assemble.pl` wrappers. |
| pnpm | 10.24+ | For the Vue UI build (invoked automatically by Maven). |
| net-snmp | any | Optional. `snmptrap` needed for E2E tests. |

### Docker Desktop Memory

Open Docker Desktop > Settings > Resources and set Memory to **16 GB** (or higher).
With the full profile, 16 daemon JVMs + webapp + minion use ~14 GB.

### Java Home

If `JAVA_HOME` is not set, the build scripts auto-detect Temurin 17 on macOS:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

## Quick Start

The fastest path from a clean checkout to a running system:

```bash
cd opennms-container/delta-v

# Full build: compile + assemble + overlay + Docker images (~45 min first time)
./build.sh

# Deploy
docker compose up -d                           # Base: postgres + kafka + webapp + minion
COMPOSE_PROFILES=full docker compose up -d     # All 17 services
```

## Build Steps (Manual)

If you prefer to run each phase individually, or need to rebuild only part of the
stack, here are the steps that `./build.sh` performs:

### 1. Compile

Full Maven compile of all modules:

```bash
./compile.pl -DskipTests
```

This takes 30-60 minutes on a first run. Subsequent incremental builds are faster.

### 2. Assemble

Produces the Horizon distribution tarball and the Daemon (Sentinel-based) assembly:

```bash
# Horizon distribution (populates opennms-container/core/tarball-root/)
./assemble.pl -Dopennms.home=/opt/opennms -DskipTests -p dir

# Container features (Karaf feature descriptors)
./maven/bin/mvn -DskipTests -pl container/features install

# Sentinel features (Karaf assembly for daemon containers)
./maven/bin/mvn -DskipTests -pl features/container/sentinel install

# Daemon assembly (produces the tarball used by opennms-container/sentinel/)
cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install && cd ../..

# Alarmd assembly
cd opennms-assemblies/alarmd && ../../maven/bin/mvn -DskipTests install && cd ../..
```

### 3. Webapp Overlay

Copies updated webapp JARs and config into the overlay directory that gets
bind-mounted into the webapp container:

```bash
cd opennms-container/delta-v
./build.sh overlay
```

### 4. Docker Images

Builds three images:

| Image | Source | Purpose |
|-------|--------|---------|
| `opennms/horizon` | `opennms-container/core/` | Webapp container |
| `opennms/daemon` | `opennms-container/sentinel/` | All 14 daemon containers |
| `opennms/db-init` | `core/db-init/` | Schema migration (run-and-exit, ~312 MB) |

```bash
cd opennms-container/delta-v
./build.sh images
```

**Docker Desktop buildx note:** Docker Desktop defaults to the `desktop-linux` buildx
instance, but the Makefiles require `default`. The `build.sh` script handles this
automatically. If building manually:

```bash
docker context use default
cd opennms-container/core && make image && cd ../..
cd opennms-container/sentinel && make image && cd ../..
docker image tag opennms/sentinel:36.0.0-SNAPSHOT opennms/daemon:36.0.0-SNAPSHOT
```

The sentinel Makefile tags its output as `opennms/sentinel`, but the docker-compose
expects `opennms/daemon`. The re-tag step is required when building manually.

## Deployment

### Compose Profiles

The docker-compose uses native profiles to control which daemons run:

| Profile | Services | Use Case |
|---------|----------|----------|
| _(none)_ | postgres, kafka, db-init, webapp, minion | Webapp-only development |
| `lite` | + pollerd, collectd, rtcd, notifd, discovery, provisiond, bsmd, alarmd | Core monitoring without passive receivers |
| `passive` | + trapd, syslogd, eventtranslator | Trap/syslog processing |
| `full` | All 14 daemon containers | Complete Delta-V deployment |

```bash
cd opennms-container/delta-v

# Base profile (webapp + minion only)
docker compose up -d

# Lite profile
COMPOSE_PROFILES=lite docker compose up -d

# Full profile (all 17 services)
COMPOSE_PROFILES=full docker compose up -d
```

### Verifying Health

All containers expose health checks. Check status with:

```bash
COMPOSE_PROFILES=full docker compose ps
```

Expected: all containers show `(healthy)` except:
- `db-init` — exits with code 0 after schema migration
- `minion` — shows `(unhealthy)` because the Echo RPC (passive) check fails
  without a Core RPC responder. Kafka RPC/Sink/Twin connections are healthy.

Webapp REST API:
```bash
curl -u admin:admin http://localhost:8980/opennms/rest/info
```

### Clean Restart

To wipe all data and start fresh:

```bash
cd opennms-container/delta-v
./build.sh clean              # docker compose down -v
docker compose up -d          # fresh deployment
```

Or manually:

```bash
COMPOSE_PROFILES=full docker compose down -v
COMPOSE_PROFILES=full docker compose up -d
```

## Rebuilding Individual Components

After the initial build, you rarely need to rebuild everything. Common scenarios:

### Changed a daemon-loader module

Rebuild the module, then recreate the affected container:

```bash
./compile.pl -DskipTests --projects :opennms-daemon-loader-alarmd -am install
docker compose up -d --force-recreate alarmd
```

### Changed core/event-forwarder-kafka

This JAR is bind-mounted from the build tree into daemon containers, so just rebuild:

```bash
./compile.pl -DskipTests --projects :org.opennms.core.event-forwarder-kafka -am install
COMPOSE_PROFILES=full docker compose up -d --force-recreate
```

### Changed opennms-webapp

Rebuild the module, update the overlay, and restart:

```bash
./maven/bin/mvn -DskipTests -pl opennms-webapp -am install
cd opennms-container/delta-v && ./build.sh overlay
docker compose up -d --force-recreate webapp
```

### Changed Liquibase schema or db-init module

Rebuild the schema module, package the db-init fat JAR, rebuild the image,
and restart with clean volumes:

```bash
./maven/bin/mvn -DskipTests -pl core/schema install
cd core/db-init && \
  JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
  ../../maven/bin/mvn -DskipTests package && \
  docker build -t opennms/db-init:36.0.0-SNAPSHOT . && cd ../..
cd opennms-container/delta-v
COMPOSE_PROFILES=full docker compose down -v
COMPOSE_PROFILES=full docker compose up -d
```

### Changed Karaf features.xml

The features.xml overlay is pre-built and shared by webapp and minion. If you change
`container/features/src/main/resources/features.xml`:

```bash
./maven/bin/mvn -DskipTests -pl container/features install
```

Then extract the assembled features.xml from the image and patch as needed.
See the `webapp-overlay/system/` directory for the current overlay.

### Picking Up New JARs in Daemon Containers

Daemon containers cache JARs in named Docker volumes. After rebuilding JARs that
are overlayed into the Karaf `system/` directory, you must delete the volume:

```bash
docker volume rm delta-v_alarmd-data
docker compose up -d --force-recreate alarmd
```

Or use `docker compose down -v` to remove all volumes at once.

## End-to-End Testing

An integration test script validates the full trap-to-alarm pipeline:

```bash
cd opennms-container/delta-v

# Requires: passive profile + snmptrap (net-snmp)
COMPOSE_PROFILES=full docker compose up -d
./test-e2e.sh
```

The test sends SNMP traps through the pipeline and verifies:
1. **Phase 1:** coldStart trap -> newSuspect -> Provisiond -> node created
2. **Phase 2:** linkDown trap -> EventTranslator -> Alarmd -> alarm in PostgreSQL
3. **Phase 3:** linkUp trap -> EventTranslator -> Alarmd -> alarm cleared

The database must be clean (no pre-existing test node) for Phase 1 to pass.
Use `./build.sh clean` before re-running if the test node already exists.

## Troubleshooting

### `make image` fails with buildx error

```
DOCKERX_INSTANCE is not set but there is a non-default docker buildx instance
active: desktop-linux
```

Fix: `docker context use default` before running `make image`. The `build.sh` script
handles this automatically.

### Container exits with code 137

Out-of-memory kill. Increase Docker Desktop memory to 16 GB.

### db-init fails with Liquibase error

Check logs: `docker compose logs db-init`. If a table/sequence doesn't exist,
the changeset may need a `<preConditions onFail="MARK_RAN">` guard. This is common
when changesets drop objects that were already removed by earlier migrations.

### Webapp fails with ClassNotFoundException

The webapp overlay JAR at `webapp-jetty-webinf-overlay/lib/opennms-webapp-*.jar`
may be stale. Rebuild and re-run the overlay:

```bash
./maven/bin/mvn -DskipTests -pl opennms-webapp -am install
cd opennms-container/delta-v && ./build.sh overlay
docker compose up -d --force-recreate webapp
```

### Daemon container stays unhealthy

Check Karaf logs inside the container:

```bash
docker exec delta-v-alarmd cat /opt/sentinel/data/log/karaf.log | tail -50
```

Common causes:
- Missing bundles: check that the feature was added to `features/container/sentinel/pom.xml`
  `<installedFeatures>` so Maven places the JARs in `system/`.
- OSGi resolution failures: read error messages backwards from "Unable to resolve root".
- Stale volume data: `docker volume rm delta-v_<service>-data` and recreate.

## Architecture Reference

See `docs/plans/2026-03-07-strike-fighter-completion-design.md` for the full
Delta-V microservice architecture, including Kafka topic design, TSID assignment,
and the event routing model.
