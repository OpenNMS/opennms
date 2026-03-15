# PassiveServiceMonitor Module Move — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make PassiveServiceMonitor loadable on Minion by moving it and its lightweight dependencies to `features/poller/api` (available on both Pollerd and Minion via OSGi).

**Architecture:** Create a thin `PassiveStatusHolder` static singleton in `features/poller/api`. Both Pollerd (via PassiveStatusKeeper) and Minion (via PassiveStatusTwinSubscriber) write to it. PassiveServiceMonitor reads from it. The full PassiveStatusKeeper daemon stays in `opennms-services` — only the shared read interface moves.

**Tech Stack:** Java 17, OSGi/Karaf, Maven, Twin API, ServiceLoader

---

## File Map

| Action | File | Responsibility |
|--------|------|---------------|
| Create | `features/poller/api/.../passive/PassiveStatusHolder.java` | Static singleton: getStatus() + update(). Thread-safe. |
| Create | `features/poller/api/.../passive/PassiveStatusKey.java` | Immutable value object (nodeLabel, ipAddr, svcName) |
| Create | `features/poller/api/.../passive/PassiveStatusConfig.java` | Twin API DTO with TWIN_KEY, serializable entries list |
| Move | `features/poller/api/.../monitors/PassiveServiceMonitor.java` | Monitor reads from PassiveStatusHolder |
| Modify | `features/poller/api/.../ServiceMonitor` (ServiceLoader) | Add PassiveServiceMonitor registration |
| Modify | `opennms-services/.../passive/PassiveStatusKeeper.java` | On setStatus(), also call PassiveStatusHolder.update() |
| Delete | `opennms-services/.../monitors/PassiveServiceMonitor.java` | Moved to poller-api |
| Delete | `opennms-services/.../passive/PassiveStatusKey.java` | Moved to poller-api |
| Delete | `opennms-services/.../passive/PassiveStatusConfig.java` | Moved to poller-api |
| Modify | `opennms-services/.../passive/PassiveStatusTwinPublisher.java` | Import from new package |
| Modify | `opennms-services/META-INF/services/...ServiceMonitor` | Remove PassiveServiceMonitor |
| Modify | `features/minion/core/impl/.../PassiveStatusTwinSubscriber.java` | Use PassiveStatusHolder instead of PassiveStatusKeeper |
| Modify | `features/minion/core/impl/pom.xml` | Remove opennms-services dep, add poller-api if missing |
| Modify | `features/minion/core/impl/.../blueprint.xml` | Keep subscriber wiring |

**Package structure after move:**
```
features/poller/api/src/main/java/org/opennms/netmgt/poller/passive/
  ├── PassiveStatusHolder.java      (static singleton)
  ├── PassiveStatusKey.java         (value object)
  └── PassiveStatusConfig.java      (Twin DTO)
features/poller/api/src/main/java/org/opennms/netmgt/poller/monitors/
  └── PassiveServiceMonitor.java    (reads from PassiveStatusHolder)
```

---

### Task 1: Create PassiveStatusHolder in poller-api

**Files:**
- Create: `features/poller/api/src/main/java/org/opennms/netmgt/poller/passive/PassiveStatusHolder.java`
- Create: `features/poller/api/src/main/java/org/opennms/netmgt/poller/passive/PassiveStatusKey.java`
- Create: `features/poller/api/src/main/java/org/opennms/netmgt/poller/passive/PassiveStatusConfig.java`

- [ ] **Step 1: Create PassiveStatusKey in poller-api**

Copy from `opennms-services/src/main/java/org/opennms/netmgt/passive/PassiveStatusKey.java`, change package to `org.opennms.netmgt.poller.passive`.

- [ ] **Step 2: Create PassiveStatusConfig in poller-api**

Copy from `opennms-services/src/main/java/org/opennms/netmgt/passive/PassiveStatusConfig.java`, change package to `org.opennms.netmgt.poller.passive`. Update imports for PassiveStatusKey.

- [ ] **Step 3: Create PassiveStatusHolder**

```java
package org.opennms.netmgt.poller.passive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opennms.netmgt.poller.PollStatus;

/**
 * Thread-safe static holder for passive service status.
 * Written to by PassiveStatusKeeper (Pollerd) or PassiveStatusTwinSubscriber (Minion).
 * Read by PassiveServiceMonitor.
 */
public class PassiveStatusHolder {
    private static final Map<PassiveStatusKey, PollStatus> STATUS_MAP = new ConcurrentHashMap<>();

    public static PollStatus getStatus(String nodeLabel, String ipAddr, String svcName) {
        PollStatus status = STATUS_MAP.get(new PassiveStatusKey(nodeLabel, ipAddr, svcName));
        return status != null ? status : PollStatus.up();
    }

    public static void setStatus(String nodeLabel, String ipAddr, String svcName, PollStatus status) {
        STATUS_MAP.put(new PassiveStatusKey(nodeLabel, ipAddr, svcName), status);
    }

    public static void updateFromConfig(PassiveStatusConfig config) {
        Map<PassiveStatusKey, PollStatus> newMap = config.toStatusMap();
        STATUS_MAP.clear();
        STATUS_MAP.putAll(newMap);
    }
}
```

- [ ] **Step 4: Build poller-api to verify compilation**

Run: `./maven/bin/mvn -DskipTests -pl features/poller/api install`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```
feat: add PassiveStatusHolder to poller-api module

Shared static holder for passive service status, accessible from both
Pollerd (via PassiveStatusKeeper) and Minion (via TwinSubscriber).
```

---

### Task 2: Move PassiveServiceMonitor to poller-api

**Files:**
- Create: `features/poller/api/src/main/java/org/opennms/netmgt/poller/monitors/PassiveServiceMonitor.java`
- Modify: `features/poller/api/src/main/resources/META-INF/services/org.opennms.netmgt.poller.ServiceMonitor`

- [ ] **Step 1: Create PassiveServiceMonitor in poller-api**

```java
package org.opennms.netmgt.poller.monitors;

import java.util.Map;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.passive.PassiveStatusHolder;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

public class PassiveServiceMonitor extends AbstractServiceMonitor {
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        return PassiveStatusHolder.getStatus(svc.getNodeLabel(), svc.getIpAddr(), svc.getSvcName());
    }
}
```

- [ ] **Step 2: Check if poller-api has a ServiceMonitor ServiceLoader file**

Look for `features/poller/api/src/main/resources/META-INF/services/org.opennms.netmgt.poller.ServiceMonitor`. If it doesn't exist, create it. Add: `org.opennms.netmgt.poller.monitors.PassiveServiceMonitor`

- [ ] **Step 3: Build to verify**

Run: `./maven/bin/mvn -DskipTests -pl features/poller/api install`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
feat: move PassiveServiceMonitor to poller-api module

Reads from PassiveStatusHolder instead of PassiveStatusKeeper singleton.
Available on both Pollerd and Minion via the poller-api OSGi bundle.
```

---

### Task 3: Update PassiveStatusKeeper to write to PassiveStatusHolder

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/passive/PassiveStatusKeeper.java`
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/poller/monitors/PassiveServiceMonitor.java`
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/passive/PassiveStatusKey.java`
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/passive/PassiveStatusConfig.java`
- Modify: `opennms-services/src/main/resources/META-INF/services/org.opennms.netmgt.poller.ServiceMonitor`

- [ ] **Step 1: Delete moved files from opennms-services**

Delete PassiveServiceMonitor.java, PassiveStatusKey.java, PassiveStatusConfig.java.
Remove `org.opennms.netmgt.poller.monitors.PassiveServiceMonitor` from ServiceLoader file.

- [ ] **Step 2: Update PassiveStatusKeeper imports and setStatus()**

Change imports from `org.opennms.netmgt.passive.PassiveStatusKey` to `org.opennms.netmgt.poller.passive.PassiveStatusKey`. Same for PassiveStatusConfig.

In `setStatus(PassiveStatusKey key, PollStatus pollStatus)`, add:
```java
PassiveStatusHolder.setStatus(key.getNodeLabel(), key.getIpAddr(), key.getServiceName(), pollStatus);
```

Also import `org.opennms.netmgt.poller.passive.PassiveStatusHolder`.

- [ ] **Step 3: Update PassiveStatusTwinPublisher imports**

Change imports from `org.opennms.netmgt.passive.PassiveStatusConfig` to `org.opennms.netmgt.poller.passive.PassiveStatusConfig`.
Same for PassiveStatusKey.

- [ ] **Step 4: Update PassiveStatusValue imports**

Change `org.opennms.netmgt.passive.PassiveStatusKey` → `org.opennms.netmgt.poller.passive.PassiveStatusKey`.

- [ ] **Step 5: Build opennms-services**

Run: `./maven/bin/mvn -DskipTests -pl opennms-services install`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```
refactor: PassiveStatusKeeper writes to shared PassiveStatusHolder

Moved PassiveServiceMonitor, PassiveStatusKey, PassiveStatusConfig
to poller-api. PassiveStatusKeeper now writes to PassiveStatusHolder
on each status change so the monitor works on both Pollerd and Minion.
```

---

### Task 4: Update PassiveStatusTwinSubscriber for Minion

**Files:**
- Modify: `features/minion/core/impl/src/main/java/org/opennms/minion/core/impl/PassiveStatusTwinSubscriber.java`
- Modify: `features/minion/core/impl/pom.xml`

- [ ] **Step 1: Remove opennms-services dependency from pom.xml**

Delete the `<dependency>` block for `org.opennms:opennms-services` (scope: provided).

- [ ] **Step 2: Update PassiveStatusTwinSubscriber to use PassiveStatusHolder**

Replace:
```java
import org.opennms.netmgt.passive.PassiveStatusConfig;
import org.opennms.netmgt.passive.PassiveStatusKeeper;
```
With:
```java
import org.opennms.netmgt.poller.passive.PassiveStatusConfig;
import org.opennms.netmgt.poller.passive.PassiveStatusHolder;
```

Replace `applyPassiveStatus()`:
```java
private void applyPassiveStatus(PassiveStatusConfig config) {
    if (config == null) {
        LOG.debug("Received null passive status config, ignoring");
        return;
    }
    LOG.info("Received passive status update with {} entries", config.getEntries().size());
    PassiveStatusHolder.updateFromConfig(config);
}
```

- [ ] **Step 3: Verify poller-api is already a dependency of minion-core-impl**

Check pom.xml. If not present, add:
```xml
<dependency>
    <groupId>org.opennms.features.poller</groupId>
    <artifactId>org.opennms.features.poller.api</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **Step 4: Build minion-core-impl**

Run: `./maven/bin/mvn -DskipTests -pl features/minion/core/impl install`
Expected: BUILD SUCCESS (no unresolvable OSGi imports)

- [ ] **Step 5: Commit**

```
refactor: Minion twin subscriber uses PassiveStatusHolder from poller-api

Removes opennms-services dependency that caused OSGi resolution failure
on Minion. Twin subscriber now writes to PassiveStatusHolder (in
poller-api, already on Minion's classpath) instead of PassiveStatusKeeper.
```

---

### Task 5: Rebuild Docker Images and Test

**Files:**
- Modify: `opennms-container/delta-v/Dockerfile.minion` (revert to no minion-core-impl override if needed)
- Modify: `opennms-container/delta-v/build.sh` (remove minion-core-impl from staging if present)

- [ ] **Step 1: Remove minion-core-impl from staging (if present)**

Check `build.sh` staging pairs for `minion-core-impl.jar` line. If present, remove it (the base Minion image will have the original core-impl which doesn't import opennms-services).

Wait — the base Minion image has the ORIGINAL core-impl that imports opennms-services. We need the UPDATED core-impl that uses poller-api instead. So keep the staging + Dockerfile COPY for minion-core-impl.

Actually, re-add the Minion Dockerfile COPY:
```dockerfile
COPY staging/daemon/minion-core-impl.jar \
     /opt/minion/repositories/core/org/opennms/features/minion/core-impl/${VERSION}/core-impl-${VERSION}.jar
```

- [ ] **Step 2: Rebuild all affected modules**

```bash
./maven/bin/mvn -DskipTests -pl features/poller/api,opennms-services,features/minion/core/impl,core/daemon-loader-pollerd install
```

- [ ] **Step 3: Rebuild Delta-V images**

```bash
cd opennms-container/delta-v
./build.sh deltav
```

- [ ] **Step 4: Clean deploy and test**

```bash
./deploy.sh reset
./deploy.sh up passive
docker compose up -d minion pollerd
sleep 120
./test-passive-e2e.sh
```

Expected: 17/17 PASS

- [ ] **Step 5: Commit and push**

```
build: rebuild images with PassiveServiceMonitor in poller-api

All 17 E2E tests passing. PassiveServiceMonitor now runs on Minion
via Kafka RPC, reading status from PassiveStatusHolder populated by
Twin API subscriber.
```
