# Java 21 Runtime for Delta-V Daemon Containers — Design Spec

**Date:** 2026-03-13
**Branch:** `eventbus-redesign`
**Reference:** Christian Pape's commit `1932d089` in OpenNMS/opennms (NMS-19451)

## Goal

Upgrade all 17 daemon containers + Minion to run on JRE 21 by upgrading the Karaf OSGi runtime from 4.3.10 to 4.4.9 and bumping all associated dependencies. The webapp (opennms/horizon) container stays on Java 17.

## Scope

### In Scope
- Daemon containers (alarmd, pollerd, collectd, rtcd, perspectivepollerd, discovery, trapd, syslogd, ticketer, eventtranslator, enlinkd, scriptd, provisiond, bsmd, telemetryd) + Minion
- Root POM version bumps
- Karaf container configuration (config.properties, startup.properties, overrides.properties, blacklisted.properties)
- Features XML updates (pax-http → pax-web-http-jetty renames, websocket-api additions)
- Drools/MVEL Java 21 compatibility fix
- Dockerfiles (sentinel, minion base images → jre-21)
- build.sh (accept Java 21)
- Post-build webapp-overlay features.xml re-extraction

### Out of Scope
- opennms/horizon (webapp) container and Dockerfile.webapp
- opennms-container/core/ Dockerfile
- opennms-full-assembly/pom.xml
- runjava script
- Maven Enforcer `[17,18)` compile range
- Source/target compiler level (stays at 17)

## Design

### 1. Root POM Version Bumps

All changes in `/pom.xml` properties section.

**Updated properties:**

| Property | From | To | Reason |
|----------|------|----|--------|
| `karafVersion` | 4.3.10 | 4.4.9 | Core upgrade — Java 21 support |
| `eclipseOsgiVersion` | 3.17.200 | 3.18.0 | Match Karaf 4.4 embedded Equinox |
| `felixCmJsonVersion` | 1.0.6 | 1.0.8 | Match Karaf 4.4 embedded Felix |
| `osgiVersion` | 7.0.0 | 8.0.0 | OSGi R8 for Karaf 4.4 |
| `osgiAnnotationVersion` | 7.0.0 | 8.0.0 | Match osgiVersion |
| `jansiVersion` | 2.4.0 | 2.4.2 | Karaf 4.4 console dependency |
| `jlineVersion` | 3.21.0 | 3.30.6 | Karaf 4.4 console dependency |
| `paxLoggingVersion` | 2.0.19 | 2.3.0 | Pax Logging for Karaf 4.4 |
| `paxSwissboxVersion` | 1.8.5 | 1.9.0 | Pax Swissbox core for Karaf 4.4 |
| `paxUrlAetherVersion` | 2.6.14 | 2.6.17 | Pax URL for Karaf 4.4 |
| `paxWebVersion` | 7.3.29 | 8.0.34 | Pax Web 8 for Karaf 4.4 |
| `bouncyCastleVersion` | 1.78.1 | 1.83 | Java 21 compatibility |
| `minaSshdVersion` | 2.12.1 | 2.15.0 | Java 21 compatibility |

**New properties (version splits):**

| Property | Value | Reason |
|----------|-------|--------|
| `osgiUtilFunctionVersion` | 1.2.0 | Split from `osgiUtilVersion` — function stays 1.2.0 |
| `osgiUtilPromiseVersion` | 1.3.0 | Split from `osgiUtilVersion` — promise bumps to 1.3.0 |
| `paxSwissboxOptionalJclVersion` | 1.8.5 | Split from `paxSwissboxVersion` — optional-jcl stays 1.8.5 |

**Removed property:** `osgiUtilVersion` (replaced by the two split properties)

### 2. Karaf Container Configuration

#### 2a. `container/karaf/pom.xml` and `container/shared/pom.xml`
- `<javase>1.8</javase>` → `<javase>11</javase>`

#### 2b. `container/karaf/.../config.properties`
- Felix Framework: `6.0.5` → `7.0.5`
- OSGi framework version: `1.9` → `1.10` (two lines: `org.osgi.framework` and `org.osgi.framework.dto`)

#### 2c. `container/karaf/.../startup.properties` and `container/shared/.../startup.properties`
Both files — osgiUtilVersion split:
- `org.osgi.util.function/${osgiUtilVersion}` → `org.osgi.util.function/${osgiUtilFunctionVersion}`
- `org.osgi.util.promise/${osgiUtilVersion}` → `org.osgi.util.promise/${osgiUtilPromiseVersion}`

#### 2d. `container/karaf/.../blacklisted.properties`
- `pax-war` → `pax-web-war`

#### 2e. `container/shared/.../overrides.properties`
Widen pax-logging override range on three lines:
- `[1.0,2.0)` → `[1.0,2.3)` for pax-logging-api, pax-logging-log4j2, pax-logging-logback

### 3. Features XML Changes

#### 3a. `container/features/.../features.xml`
1. `opennms-alarm-history-rest`: `<feature>pax-http</feature>` → `<feature>pax-web-http-jetty</feature>`
2. `opennms-bridge-http-service`: `<capability>http-service;provider:=pax-http</capability>` → `<capability>http-service;provider:=pax-web-http-jetty</capability>`

#### 3b. `container/features/.../features-sentinel.xml`
Add before existing noop-jetty-extension bundle:
```xml
<bundle start-level="30">mvn:org.eclipse.jetty.websocket/websocket-api/${jettyVersion}</bundle>
```

#### 3c. `container/features/.../features-minion.xml`
Same websocket-api addition in `minion-health-check` feature:
```xml
<bundle start-level="30">mvn:org.eclipse.jetty.websocket/websocket-api/${jettyVersion}</bundle>
```

#### 3d. `features/minion/repository/.../features.boot`
- `pax-war` → `pax-web-war`

#### 3e. `opennms-container/delta-v/minion-overlay/features.boot`
- `pax-war` → `pax-web-war` (delta-v overlay copy — without this, Minion container fails to boot because `pax-war` is blacklisted)

### 4. Drools/MVEL Fix and Other Module Changes

#### 4a. `dependencies/drools/pom.xml`
1. Add explicit dependency: `org.mvel:mvel2:2.5.2.Final`
2. Add exclusion on `drools-mvel`: exclude transitive `org.mvel:mvel2`
3. Add exclusion on `drools-serialization-protobuf`: exclude transitive `org.drools:drools-mvel`

#### 4b. `features/config/osgi/del/pom.xml`
- Hardcoded pax-logging-api version: `2.0.9` → `2.3.0`

#### 4c. `features/vaadin-components/extender-service/pom.xml`
- `pax-swissbox-optional-jcl` version: `${paxSwissboxVersion}` → `${paxSwissboxOptionalJclVersion}`

### 5. Docker and Build Changes

#### 5a. `opennms-container/sentinel/Dockerfile`
- `opennms/deploy-base:ubi9-3.6.3.b335-jre-17` → `opennms/deploy-base:ubi9-3.6.3.b335-jre-21`

#### 5b. `opennms-container/minion/Dockerfile`
- `opennms/deploy-base:ubi9-3.6.3.b335-jre-17` → `opennms/deploy-base:ubi9-3.6.3.b335-jre-21`

#### 5c. `opennms-container/common.mk`
- `BASE_IMAGE` → `opennms/deploy-base:ubi9-3.6.3.b335-jre-21`

#### 5d. `opennms-container/delta-v/build.sh`
- Lines 38-39: Temurin auto-detection path `temurin-17` → `temurin-21`
- Line 43: Version check `[ "$java_version" = "17" ]` → `[ "$java_version" = "21" ]`
- Lines 88-95: db-init special-case — since the main build is now also Java 21, the separate `JAVA_HOME` override for db-init may no longer be needed. Simplify or remove if both use the same JDK.

#### 5e. Post-build: webapp-overlay features.xml
After rebuild with Karaf 4.4, re-extract the patched `features.xml` from the rebuilt image and update `opennms-container/delta-v/webapp-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml`. This file currently contains Karaf 4.3.10 references that must be updated to reflect the 4.4.9 feature resolution.

**Note:** `config.properties` already has `eecap-21` capability declarations — no change needed there, confirming Java 21 EE readiness.

### 6. Verification

#### Build
```bash
jenv shell 21
./build.sh compile && ./build.sh assemble && ./build.sh images && ./build.sh deltav
```

#### Health checks
All 17 daemon containers + minion must pass health checks via `docker compose up`.

#### E2E tests
1. `test-e2e.sh` — core daemon lifecycle
2. `test-minion-e2e.sh` — minion registration + trap forwarding
3. `test-syslog-e2e.sh` — syslog reception + alarm lifecycle

#### Rollback
Single commit (or small series) on `eventbus-redesign` — `git revert` returns to working state.

## Key Risks

1. **Karaf 4.3 → 4.4 bundle resolution** — OSGi R8 changes may break bundles relying on `Import-Package: *;resolution:=optional` + `DynamicImport-Package: *`
2. **Pax Web 8 API changes** — CXF health endpoint registration may differ
3. **MVEL/Drools on Java 21** — alarm correlation engine uses Drools rules; MVEL 2.5.2.Final pin is the mitigation
4. **Webapp compile-time impact** — POM version bumps are global; webapp compiles with new dependency versions but runs on Java 17 — should be fine but watch for compile errors
5. **`karafSshdVersion` derived property** — `${karafVersion}.ONMS_1` automatically becomes `4.4.9.ONMS_1`. This is safe — it's only a feature version label on a locally-defined SSH feature in `features-core.xml`, not a Maven artifact. The actual bundles use `${karafVersion}` and `${minaSshdVersion}` which are standard Maven Central artifacts.

## Files Changed (Complete List)

1. `pom.xml` — version properties
2. `container/karaf/pom.xml` — javase 1.8→11
3. `container/karaf/src/main/filtered-resources/etc/config.properties` — Felix 7.0.5, OSGi 1.10
4. `container/karaf/src/main/filtered-resources/etc/startup.properties` — util version split
5. `container/karaf/src/main/filtered-resources/etc/blacklisted.properties` — pax-web-war
6. `container/shared/pom.xml` — javase 1.8→11
7. `container/shared/src/main/filtered-resources/etc/overrides.properties` — logging range
8. `container/shared/src/main/filtered-resources/etc/startup.properties` — util version split
9. `container/features/src/main/resources/features.xml` — pax-web-http-jetty
10. `container/features/src/main/resources/features-sentinel.xml` — websocket-api
11. `container/features/src/main/resources/features-minion.xml` — websocket-api
12. `dependencies/drools/pom.xml` — MVEL fix
13. `features/config/osgi/del/pom.xml` — pax-logging 2.3.0
14. `features/vaadin-components/extender-service/pom.xml` — swissbox split
15. `features/minion/repository/src/main/resources/features.boot` — pax-web-war
16. `opennms-container/sentinel/Dockerfile` — jre-21
17. `opennms-container/minion/Dockerfile` — jre-21
18. `opennms-container/common.mk` — jre-21
19. `opennms-container/delta-v/build.sh` — accept Java 21
20. `opennms-container/delta-v/minion-overlay/features.boot` — pax-web-war
