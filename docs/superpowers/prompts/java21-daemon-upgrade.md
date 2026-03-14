# Next Session: Java 21 Runtime for Delta-V Daemon Containers

## Goal

Upgrade the **opennms/daemon** container stack (all 17 daemon services + Minion) to run on Java 21. The **opennms/horizon** (webapp) container is explicitly OUT OF SCOPE — it stays on Java 17 for now.

This is a runtime upgrade (JRE 21), and we also want to compile with Java 21 for the daemon-related modules. The webapp and full-assembly remain on Java 17.

## Reference Commit

Christian Pape's commit in OpenNMS/opennms provides the roadmap:
- **Commit:** `1932d089db15d7ba3f8c9284ce3edadbea60cab8`
- **Author:** Christian Pape, 2026-03-03
- **Title:** `NMS-19451: Running Core, Sentinel and Minion using JDK21`
- **Repo:** OpenNMS/opennms (NOT in our fork — fetch via `gh api repos/OpenNMS/opennms/commits/1932d08`)

His commit upgraded the runtime to Java 21 while keeping compile at Java 17. The key changes were:

### Karaf 4.3.10 → 4.4.9 (the big cascade)
- Felix Framework 6.0.5 → 7.0.5
- OSGi spec 7.0 → 8.0
- Pax Web 7.x → 8.x (feature renames: `pax-http` → `pax-web-http-jetty`, `pax-war` → `pax-web-war`)
- Pax Logging 2.0.19 → 2.3.0
- Pax Swissbox version split (optional-jcl stays at 1.8.5, core goes to 1.9.0)
- OSGi util.function/util.promise version split
- Karaf `javase` property: `1.8` → `11`

### Dependency bumps
- BouncyCastle 1.78.1 → 1.83
- Mina SSHD 2.12.1 → 2.15.0
- Drools/MVEL: explicit MVEL 2.5.2.Final + exclusion fixes (MVEL broken on Java 21)
- JLine 3.21.0 → 3.30.6, Jansi 2.4.0 → 2.4.2
- Pax URL Aether 2.6.14 → 2.6.17

### Container/runtime changes
- Docker base images: `jre-17` → `jre-21`
- `runjava` maximum_version: `17.9999` → `21.9999`
- Minion `features.boot`: `pax-war` → `pax-web-war`
- `blacklisted.properties`: `pax-war` → `pax-web-war`
- `config.properties`: Felix framework + OSGi framework version bumps
- `startup.properties`: util.function/util.promise version refs
- `overrides.properties`: pax-logging range `[1.0,2.3)`
- Websocket-api bundle added to minion + sentinel health-check features (Jetty startup race fix)

### Features.xml changes
- `opennms-alarm-history-rest`: `pax-http` → `pax-web-http-jetty`
- `opennms-bridge-http-service`: capability `pax-http` → `pax-web-http-jetty`
- `opennms-full-assembly`: `pax-http` → `pax-web-http-jetty`

## Scope for This Session

### IN SCOPE (daemon containers + Minion)
1. **Root POM version bumps** — Karaf, Felix, OSGi, Pax Web/Logging/Swissbox, BouncyCastle, Mina SSHD, Drools/MVEL, JLine, Jansi, Pax URL Aether
2. **container/karaf/** — config.properties, startup.properties, javase bump
3. **container/shared/** — overrides.properties, startup.properties, javase bump
4. **container/features/** — features.xml, features-sentinel.xml, features-minion.xml (pax-http→pax-web-http-jetty, websocket-api bundle, etc.)
5. **features/container/sentinel/** — any feature renames needed
6. **opennms-container/sentinel/** — Dockerfile base image → jre-21
7. **opennms-container/delta-v/build.sh** — allow Java 21, update JAVA_HOME detection
8. **opennms-container/delta-v/Dockerfile.daemon** — base image uses opennms/daemon which uses sentinel which uses jre-21
9. **opennms-container/delta-v/Dockerfile.minion** — base image update
10. **opennms-container/minion/** — Dockerfile base image, features.boot (`pax-war` → `pax-web-war`)
11. **dependencies/drools/** — MVEL fix
12. **Patched features.xml** in `webapp-overlay/system/` — must be re-extracted from the rebuilt image after Karaf 4.4 changes
13. **All 17 daemon containers + Minion must pass health checks**
14. **E2E tests** — `test-e2e.sh`, `test-minion-e2e.sh`, `test-syslog-e2e.sh` must all pass

### OUT OF SCOPE (leave on Java 17)
- **opennms/horizon** (webapp) container and `Dockerfile.webapp`
- **opennms-container/core/** Dockerfile
- **opennms-full-assembly/** (except feature renames that affect shared features.xml)
- Maven Enforcer `[17,18)` range for compile (keep compiling with 17 for now — compile-with-21 is a separate future step)
- `runjava` script (webapp-only)

## Current State

- **Branch:** `eventbus-redesign`
- **Remote:** push to `delta-v` (pbrane/delta-v), PRs to `pbrane/delta-v`
- **Current Karaf:** 4.3.10, OSGi 7.0, Pax Web 7.3.29, Felix 6.0.5
- **Current Java:** compile=17, runtime=17 (except db-init which is Spring Boot 4 / Java 21)
- **Docker images:** `opennms/daemon-deltav`, `opennms/horizon-deltav`, `opennms/minion-deltav` (layered on base images)
- **build.sh** currently enforces `java_version = 17` and auto-detects temurin-17
- **18 services in docker-compose:** 17 daemons + webapp + minion (alarmd, pollerd, collectd, rtcd, perspectivepollerd, discovery, trapd, syslogd, ticketer, eventtranslator, enlinkd, scriptd, provisiond, bsmd, telemetryd + webapp + minion)

## Approach

Use the brainstorming skill to design the approach, then write a plan and execute. The general strategy:

1. Apply Christian's POM version bumps to root pom.xml
2. Update Karaf container config files (config.properties, startup.properties, overrides.properties, blacklisted.properties)
3. Update features.xml with Pax Web 8 renames and websocket-api additions
4. Update Dockerfiles to use jre-21 base images
5. Update build.sh to accept Java 21
6. Rebuild: `./build.sh compile && ./build.sh assemble && ./build.sh images && ./build.sh deltav`
7. Re-extract and patch features.xml for webapp-overlay
8. Bring up all containers, verify health
9. Run all three E2E tests
10. Commit and PR

## Key Risk

The Karaf 4.3 → 4.4 upgrade is the riskiest part. OSGi bundle resolution may break for daemon containers that rely on `Import-Package: *;resolution:=optional` + `DynamicImport-Package: *` workarounds. Watch for:
- Bundle refresh cycles causing temporary unavailability
- Pax Web 8 API changes breaking the CXF health endpoint
- Feature resolution failures from renamed features
- MVEL/Drools failures in Alarmd (uses Drools for alarm correlation)
