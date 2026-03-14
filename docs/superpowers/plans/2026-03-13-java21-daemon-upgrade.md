# Java 21 Daemon Upgrade Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade all 17 daemon containers + Minion to JRE 21 runtime via Karaf 4.3.10 → 4.4.9.

**Architecture:** Surgical config-only approach — apply version bumps to root POM, update Karaf config files for the new OSGi runtime, rename Pax Web features, fix MVEL for Java 21, and update Dockerfiles to use JRE 21 base images. Webapp stays on Java 17.

**Tech Stack:** Java 21, Apache Karaf 4.4.9, Felix 7.0.5, OSGi R8, Pax Web 8.0.34, Docker

**Spec:** `docs/superpowers/specs/2026-03-13-java21-daemon-upgrade-design.md`

---

## Chunk 1: POM and Karaf Configuration

### Task 1: Root POM version bumps

**Files:**
- Modify: `pom.xml:1744,1884-1902,1934-1935,1989`

- [ ] **Step 1: Update Karaf and OSGi core versions**

In `pom.xml`, change:
```xml
<!-- Line 1884 -->
<karafVersion>4.3.10</karafVersion>     →  <karafVersion>4.4.9</karafVersion>
<!-- Line 1886 -->
<eclipseOsgiVersion>3.17.200</eclipseOsgiVersion>  →  <eclipseOsgiVersion>3.18.0</eclipseOsgiVersion>
<!-- Line 1887 -->
<felixCmJsonVersion>1.0.6</felixCmJsonVersion>  →  <felixCmJsonVersion>1.0.8</felixCmJsonVersion>
<!-- Line 1934 -->
<osgiVersion>7.0.0</osgiVersion>  →  <osgiVersion>8.0.0</osgiVersion>
<!-- Line 1935 -->
<osgiAnnotationVersion>7.0.0</osgiAnnotationVersion>  →  <osgiAnnotationVersion>8.0.0</osgiAnnotationVersion>
```

- [ ] **Step 2: Update Pax and console dependency versions**

In `pom.xml`, change:
```xml
<!-- Line 1896 -->
<jansiVersion>2.4.0</jansiVersion>  →  <jansiVersion>2.4.2</jansiVersion>
<!-- Line 1897 -->
<jlineVersion>3.21.0</jlineVersion>  →  <jlineVersion>3.30.6</jlineVersion>
<!-- Line 1899 -->
<paxLoggingVersion>2.0.19</paxLoggingVersion>  →  <paxLoggingVersion>2.3.0</paxLoggingVersion>
<!-- Line 1900 -->
<paxSwissboxVersion>1.8.5</paxSwissboxVersion>  →  <paxSwissboxVersion>1.9.0</paxSwissboxVersion>
<!-- Line 1901 -->
<paxUrlAetherVersion>2.6.14</paxUrlAetherVersion>  →  <paxUrlAetherVersion>2.6.17</paxUrlAetherVersion>
<!-- Line 1902 -->
<paxWebVersion>7.3.29</paxWebVersion>  →  <paxWebVersion>8.0.34</paxWebVersion>
```

- [ ] **Step 3: Split osgiUtilVersion into function/promise**

In `pom.xml`, replace line 1898:
```xml
<!-- BEFORE -->
<osgiUtilVersion>1.2.0</osgiUtilVersion>

<!-- AFTER -->
<osgiUtilFunctionVersion>1.2.0</osgiUtilFunctionVersion>
<osgiUtilPromiseVersion>1.3.0</osgiUtilPromiseVersion>
```

- [ ] **Step 4: Add paxSwissboxOptionalJclVersion**

In `pom.xml`, add after `<paxSwissboxVersion>1.9.0</paxSwissboxVersion>` (line 1900):
```xml
<paxSwissboxOptionalJclVersion>1.8.5</paxSwissboxOptionalJclVersion>
```

- [ ] **Step 5: Update security and SSH versions**

In `pom.xml`, change:
```xml
<!-- Line 1744 -->
<bouncyCastleVersion>1.78.1</bouncyCastleVersion>  →  <bouncyCastleVersion>1.83</bouncyCastleVersion>
<!-- Line 1989 -->
<minaSshdVersion>2.12.1</minaSshdVersion>  →  <minaSshdVersion>2.15.0</minaSshdVersion>
```

- [ ] **Step 6: Commit**

```bash
git add pom.xml
git commit -m "feat(delta-v): bump Karaf 4.4.9, OSGi R8, Pax Web 8 and dependencies for Java 21 daemon runtime"
```

---

### Task 2: Karaf container POM javase bump

**Files:**
- Modify: `container/karaf/pom.xml:124`
- Modify: `container/shared/pom.xml:135`

- [ ] **Step 1: Update javase in both POMs**

In `container/karaf/pom.xml` line 124:
```xml
<javase>1.8</javase>  →  <javase>11</javase>
```

In `container/shared/pom.xml` line 135:
```xml
<javase>1.8</javase>  →  <javase>11</javase>
```

- [ ] **Step 2: Commit**

```bash
git add container/karaf/pom.xml container/shared/pom.xml
git commit -m "feat(delta-v): set Karaf javase baseline to 11 for Karaf 4.4"
```

---

### Task 3: Update config.properties

**Files:**
- Modify: `container/karaf/src/main/filtered-resources/etc/config.properties:54,63-64`

- [ ] **Step 1: Update Felix Framework version**

Line 54:
```properties
karaf.framework.felix=mvn\:org.apache.felix/org.apache.felix.framework/6.0.5
→
karaf.framework.felix=mvn\:org.apache.felix/org.apache.felix.framework/7.0.5
```

- [ ] **Step 2: Update OSGi framework versions**

Line 63:
```properties
 org.osgi.framework;version="1.9",\
→
 org.osgi.framework;version="1.10",\
```

Line 64:
```properties
 org.osgi.framework.dto;version="1.9";uses:="org.osgi.dto",\
→
 org.osgi.framework.dto;version="1.10";uses:="org.osgi.dto",\
```

- [ ] **Step 3: Commit**

```bash
git add container/karaf/src/main/filtered-resources/etc/config.properties
git commit -m "feat(delta-v): update config.properties for Felix 7.0.5 and OSGi 1.10"
```

---

### Task 4: Update startup.properties (both karaf and shared)

**Files:**
- Modify: `container/karaf/src/main/filtered-resources/etc/startup.properties:15-16`
- Modify: `container/shared/src/main/filtered-resources/etc/startup.properties:15-16`

- [ ] **Step 1: Split osgiUtilVersion in karaf startup.properties**

Lines 15-16:
```properties
mvn\:org.osgi/org.osgi.util.function/${osgiUtilVersion} = 9
mvn\:org.osgi/org.osgi.util.promise/${osgiUtilVersion} = 9
→
mvn\:org.osgi/org.osgi.util.function/${osgiUtilFunctionVersion} = 9
mvn\:org.osgi/org.osgi.util.promise/${osgiUtilPromiseVersion} = 9
```

- [ ] **Step 2: Same change in shared startup.properties**

Lines 15-16 — identical change as Step 1.

- [ ] **Step 3: Commit**

```bash
git add container/karaf/src/main/filtered-resources/etc/startup.properties \
      container/shared/src/main/filtered-resources/etc/startup.properties
git commit -m "feat(delta-v): split osgiUtilVersion into function/promise in startup.properties"
```

---

### Task 5: Update blacklisted.properties and overrides.properties

**Files:**
- Modify: `container/karaf/src/main/filtered-resources/etc/blacklisted.properties:18`
- Modify: `container/shared/src/main/filtered-resources/etc/overrides.properties:2-4`

- [ ] **Step 1: Rename pax-war in blacklisted.properties**

Line 18:
```properties
pax-war  →  pax-web-war
```

- [ ] **Step 2: Widen pax-logging override range in overrides.properties**

Lines 2-4:
```properties
mvn:org.ops4j.pax.logging/pax-logging-api/${paxLoggingVersion};range=[1.0,2.0)
mvn:org.ops4j.pax.logging/pax-logging-log4j2/${paxLoggingVersion};range=[1.0,2.0)
mvn:org.ops4j.pax.logging/pax-logging-logback/${paxLoggingVersion};range=[1.0,2.0)
→
mvn:org.ops4j.pax.logging/pax-logging-api/${paxLoggingVersion};range=[1.0,2.3)
mvn:org.ops4j.pax.logging/pax-logging-log4j2/${paxLoggingVersion};range=[1.0,2.3)
mvn:org.ops4j.pax.logging/pax-logging-logback/${paxLoggingVersion};range=[1.0,2.3)
```

- [ ] **Step 3: Commit**

```bash
git add container/karaf/src/main/filtered-resources/etc/blacklisted.properties \
      container/shared/src/main/filtered-resources/etc/overrides.properties
git commit -m "feat(delta-v): update blacklisted.properties and overrides.properties for Pax Web 8"
```

---

## Chunk 2: Features XML and Module Fixes

### Task 6: Update features.xml with Pax Web 8 renames

**Files:**
- Modify: `container/features/src/main/resources/features.xml:955,1902`

- [ ] **Step 1: Rename pax-http to pax-web-http-jetty in alarm-history-rest**

Line 955:
```xml
<feature>pax-http</feature>  →  <feature>pax-web-http-jetty</feature>
```

- [ ] **Step 2: Rename capability provider in bridge-http-service**

Line 1902:
```xml
<capability>http-service;provider:=pax-http</capability>
→
<capability>http-service;provider:=pax-web-http-jetty</capability>
```

- [ ] **Step 3: Commit**

```bash
git add container/features/src/main/resources/features.xml
git commit -m "feat(delta-v): rename pax-http to pax-web-http-jetty in features.xml"
```

---

### Task 7: Add websocket-api to sentinel and minion health-check features

**Files:**
- Modify: `container/features/src/main/resources/features-sentinel.xml:78`
- Modify: `container/features/src/main/resources/features-minion.xml:218`

- [ ] **Step 1: Add websocket-api bundle to features-sentinel.xml**

Insert before line 78 (`noop-jetty-extension`):
```xml
        <bundle start-level="30">mvn:org.eclipse.jetty.websocket/websocket-api/${jettyVersion}</bundle>
```

- [ ] **Step 2: Add websocket-api bundle to features-minion.xml**

Insert before line 218 (`noop-jetty-extension`):
```xml
        <bundle start-level="30">mvn:org.eclipse.jetty.websocket/websocket-api/${jettyVersion}</bundle>
```

- [ ] **Step 3: Commit**

```bash
git add container/features/src/main/resources/features-sentinel.xml \
      container/features/src/main/resources/features-minion.xml
git commit -m "feat(delta-v): add websocket-api bundle to health-check features for Pax Web 8 race fix"
```

---

### Task 8: Update features.boot files (pax-war → pax-web-war)

**Files:**
- Modify: `features/minion/repository/src/main/resources/features.boot:2`
- Modify: `opennms-container/delta-v/minion-overlay/features.boot:2`

- [ ] **Step 1: Rename pax-war in upstream features.boot**

Line 2:
```
pax-war  →  pax-web-war
```

- [ ] **Step 2: Rename pax-war in delta-v overlay features.boot**

Line 2:
```
pax-war  →  pax-web-war
```

- [ ] **Step 3: Commit**

```bash
git add features/minion/repository/src/main/resources/features.boot \
      opennms-container/delta-v/minion-overlay/features.boot
git commit -m "feat(delta-v): rename pax-war to pax-web-war in minion features.boot"
```

---

### Task 9: Fix Drools/MVEL for Java 21

**Files:**
- Modify: `dependencies/drools/pom.xml:36-45`

- [ ] **Step 1: Add explicit MVEL dependency and exclusions**

Replace lines 36-45 in `dependencies/drools/pom.xml`:
```xml
    <!-- BEFORE -->
    <dependency>
      <groupId>org.drools</groupId>
      <artifactId>drools-mvel</artifactId>
      <version>${droolsVersion}</version>
    </dependency>
    <dependency>
      <groupId>org.drools</groupId>
      <artifactId>drools-serialization-protobuf</artifactId>
      <version>${droolsVersion}</version>
    </dependency>

    <!-- AFTER -->
    <dependency>
      <groupId>org.mvel</groupId>
      <artifactId>mvel2</artifactId>
      <version>2.5.2.Final</version>
    </dependency>
    <dependency>
      <groupId>org.drools</groupId>
      <artifactId>drools-mvel</artifactId>
      <version>${droolsVersion}</version>
      <exclusions>
        <exclusion>
          <groupId>org.mvel</groupId>
          <artifactId>mvel2</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
    <dependency>
      <groupId>org.drools</groupId>
      <artifactId>drools-serialization-protobuf</artifactId>
      <version>${droolsVersion}</version>
      <exclusions>
        <exclusion>
          <groupId>org.drools</groupId>
          <artifactId>drools-mvel</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
```

- [ ] **Step 2: Commit**

```bash
git add dependencies/drools/pom.xml
git commit -m "fix(delta-v): pin MVEL 2.5.2.Final for Java 21 compatibility in Drools"
```

---

### Task 10: Update pax-logging hardcode and swissbox split

**Files:**
- Modify: `features/config/osgi/del/pom.xml:34`
- Modify: `features/vaadin-components/extender-service/pom.xml:68`

- [ ] **Step 1: Update hardcoded pax-logging-api version**

In `features/config/osgi/del/pom.xml` line 34:
```xml
<version>2.0.9</version><!-- version must match the one from startup.properties -->
→
<version>2.3.0</version><!-- version must match the one from startup.properties -->
```

- [ ] **Step 2: Use paxSwissboxOptionalJclVersion for optional-jcl**

In `features/vaadin-components/extender-service/pom.xml` line 68:
```xml
<version>${paxSwissboxVersion}</version>
→
<version>${paxSwissboxOptionalJclVersion}</version>
```

- [ ] **Step 3: Commit**

```bash
git add features/config/osgi/del/pom.xml \
      features/vaadin-components/extender-service/pom.xml
git commit -m "feat(delta-v): update pax-logging hardcode and swissbox version split"
```

---

## Chunk 3: Docker and Build Changes

### Task 11: Update Dockerfiles to JRE 21

**Files:**
- Modify: `opennms-container/sentinel/Dockerfile:26`
- Modify: `opennms-container/minion/Dockerfile:29`
- Modify: `opennms-container/common.mk:15`

- [ ] **Step 1: Update sentinel Dockerfile**

Line 26:
```dockerfile
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.6.3.b335-jre-17"
→
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.6.3.b335-jre-21"
```

- [ ] **Step 2: Update minion Dockerfile**

Line 29:
```dockerfile
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.6.3.b335-jre-17"
→
ARG BASE_IMAGE="opennms/deploy-base:ubi9-3.6.3.b335-jre-21"
```

- [ ] **Step 3: Update common.mk**

Line 15:
```makefile
BASE_IMAGE              := opennms/deploy-base:ubi9-3.6.3.b335-jre-17
→
BASE_IMAGE              := opennms/deploy-base:ubi9-3.6.3.b335-jre-21
```

- [ ] **Step 4: Commit**

```bash
git add opennms-container/sentinel/Dockerfile \
      opennms-container/minion/Dockerfile \
      opennms-container/common.mk
git commit -m "feat(delta-v): update Docker base images to JRE 21"
```

---

### Task 12: Update build.sh for Java 21

**Files:**
- Modify: `opennms-container/delta-v/build.sh:16,36-43,63,88-95,250`

- [ ] **Step 1: Update JAVA_HOME auto-detection and version check**

Line 16 comment:
```bash
#   JAVA_HOME         JDK 17 path (auto-detected if unset)
→
#   JAVA_HOME         JDK 21 path (auto-detected if unset)
```

Lines 36-43, replace the Java verification block:
```bash
    # Verify Java 21
    if [ -z "${JAVA_HOME:-}" ]; then
        if [ -d "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home" ]; then
            export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
        fi
    fi
    java_version=$(java -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\)\..*/\1/')
    [ "$java_version" = "21" ] || err "Java 21 required (found: $java_version)"
```

- [ ] **Step 2: Simplify db-init build (same JDK now)**

Lines 88-95, replace `do_db_init_image()`:
```bash
do_db_init_image() {
    log "Building db-init image (opennms/db-init:$VERSION)..."
    cd "$REPO_ROOT"
    ./maven/bin/mvn -f core/db-init/pom.xml -DskipTests package
    cd "$REPO_ROOT/core/db-init"
    docker build -t "opennms/db-init:$VERSION" -t "opennms/db-init:latest" .
}
```

- [ ] **Step 3: Update usage help text**

Line 250:
```bash
  JAVA_HOME         JDK 17 path
→
  JAVA_HOME         JDK 21 path
```

- [ ] **Step 4: Remove db-init exclusion from do_compile (same JDK now)**

Line 63:
```bash
    ./compile.pl $test_flag -pl '!core/db-init'
→
    ./compile.pl $test_flag
```

- [ ] **Step 5: Commit**

```bash
git add opennms-container/delta-v/build.sh
git commit -m "feat(delta-v): update build.sh to require Java 21, simplify db-init build"
```

---

## Chunk 4: Build, Verify, and Fix

### Task 13: Build and fix compilation issues

- [ ] **Step 1: Set Java 21 via jenv**

```bash
jenv shell 21
java -version  # Verify: openjdk version "21.x.x"
```

- [ ] **Step 2: Compile**

```bash
cd /Users/david/development/src/opennms/delta-v/opennms-container/delta-v
./build.sh compile
```

Expected: Compiles successfully. If there are compile errors related to the version bumps, fix them before proceeding.

- [ ] **Step 3: Assemble**

```bash
./build.sh assemble
```

- [ ] **Step 4: Build base images**

```bash
./build.sh images
```

- [ ] **Step 5: Build delta-v images**

```bash
./build.sh deltav
```

- [ ] **Step 6: Commit any additional fixes**

If compilation required additional changes beyond the spec, commit them:
```bash
git add -A
git commit -m "fix(delta-v): resolve compilation issues from Karaf 4.4 / Java 21 upgrade"
```

---

### Task 14: Re-extract webapp-overlay features.xml

**Files:**
- Modify: `opennms-container/delta-v/webapp-overlay/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml`

- [ ] **Step 1: Extract features.xml from rebuilt daemon image**

```bash
cd /Users/david/development/src/opennms/delta-v/opennms-container/delta-v
VERSION=$(./../.circleci/scripts/pom2version.sh ../../pom.xml 2>/dev/null || echo "36.0.0-SNAPSHOT")
docker run --rm opennms/daemon:$VERSION \
  cat /opt/opennms/system/org/opennms/karaf/opennms/$VERSION/opennms-$VERSION-features.xml \
  > webapp-overlay/system/org/opennms/karaf/opennms/$VERSION/opennms-$VERSION-features.xml
```

- [ ] **Step 2: Verify pax-web renames and Karaf 4.4 references are present**

```bash
grep -c "pax-web-http-jetty" webapp-overlay/system/org/opennms/karaf/opennms/*/opennms-*-features.xml
grep -c "4.4.9" webapp-overlay/system/org/opennms/karaf/opennms/*/opennms-*-features.xml
grep -c "4.3.10" webapp-overlay/system/org/opennms/karaf/opennms/*/opennms-*-features.xml
```

Expected: pax-web-http-jetty ≥ 1, 4.4.9 ≥ 1, 4.3.10 = 0.

- [ ] **Step 3: Rebuild delta-v images with updated features.xml**

```bash
./build.sh deltav
```

- [ ] **Step 4: Commit**

```bash
git add opennms-container/delta-v/webapp-overlay/
git commit -m "feat(delta-v): re-extract webapp-overlay features.xml for Karaf 4.4"
```

---

### Task 15: Health checks and E2E verification

- [ ] **Step 1: Start all containers**

```bash
cd /Users/david/development/src/opennms/delta-v/opennms-container/delta-v
docker compose up -d
```

- [ ] **Step 2: Wait for health checks**

```bash
# Wait for webapp (health endpoint)
timeout 120 bash -c 'until curl -s http://localhost:8980/opennms/rest/health 2>/dev/null | grep -q "Healthy"; do sleep 5; done'

# Check daemon containers
docker compose ps --format '{{.Name}}\t{{.Status}}' | sort
```

Expected: All containers show "Up" / "healthy".

- [ ] **Step 3: Check Karaf version in daemon container**

```bash
docker compose exec alarmd java -version 2>&1 | head -1
```

Expected: `openjdk version "21.x.x"`

- [ ] **Step 4: Run E2E tests**

```bash
cd /Users/david/development/src/opennms/delta-v/opennms-container/delta-v
./test-e2e.sh
./test-minion-e2e.sh
./test-syslog-e2e.sh
```

Expected: All three pass.

- [ ] **Step 5: If tests fail, debug and fix**

Check Karaf logs for bundle resolution failures:
```bash
docker compose logs alarmd 2>&1 | grep -i "exception\|unable to resolve\|error"
```

Fix any issues, rebuild affected images, and re-test.

- [ ] **Step 6: Final commit if any fixes were needed**

```bash
git add -A
git commit -m "fix(delta-v): resolve runtime issues from Java 21 daemon upgrade"
```
