# Minion-Mandatory Architecture: Phase 2 Implementation Plan

**Date:** 2026-03-14
**Branch:** `eventbus-redesign`
**Depends on:** Audit results in `docs/superpowers/specs/2026-03-14-minion-mandatory-audit-results.md`

## Overview

The audit found 78 total monitors/collectors. 73 are already distributable. This plan covers the 5 monitors requiring action:
- **2 self-monitoring anti-patterns** → delete
- **2 deprecated monitors** → delete
- **1 non-distributable monitor** → refactor via Twin API

Plus configuration cleanup and documentation.

---

## Step 1: Delete Self-Monitoring Monitors

**Goal:** Remove MinionHeartbeatMonitor and MinionRpcMonitor — they monitor Delta-V infrastructure health, which should be handled externally (Docker healthchecks, K8s probes, Spring Boot actuators).

### Files to Delete

```
features/poller/monitors/core/src/main/java/org/opennms/netmgt/poller/monitors/MinionHeartbeatMonitor.java
features/poller/monitors/core/src/main/java/org/opennms/netmgt/poller/monitors/MinionRpcMonitor.java
```

### Files to Check for Associated Tests

Search for and delete any test classes:
```
features/poller/monitors/core/src/test/java/**/MinionHeartbeatMonitor*
features/poller/monitors/core/src/test/java/**/MinionRpcMonitor*
```

### ServiceLoader Registration

Edit `features/poller/monitors/core/src/main/resources/META-INF/services/org.opennms.netmgt.poller.ServiceMonitor`:
- Remove line: `org.opennms.netmgt.poller.monitors.MinionHeartbeatMonitor`
- Remove line: `org.opennms.netmgt.poller.monitors.MinionRpcMonitor`

### Configuration Cleanup

In all 3 `poller-configuration.xml` files:
1. `opennms-base-assembly/src/main/filtered/etc/poller-configuration.xml`
2. `opennms-container/delta-v/pollerd-daemon-overlay/etc/poller-configuration.xml`
3. `opennms-container/delta-v/perspectivepollerd-overlay/etc/poller-configuration.xml`

Changes:
- Remove the entire `<package name="Minion">` block (contains Minion-Heartbeat, Minion-RPC, JMX-Minion, SNMP services for the Minions foreignSource)
- Remove `<monitor service="Minion-Heartbeat" .../>` line
- Remove `<monitor service="Minion-RPC" .../>` line
- Remove `<monitor service="JMX-Minion" .../>` line

**Decision needed:** Whether to also remove `<monitor service="OpenNMS-JVM" .../>` and `<monitor service="JMX-Kafka" .../>`. These use the generic `Jsr160Monitor` (distributable), but they're configured to self-monitor Delta-V infrastructure. Recommend removing from default config — users who need JMX monitoring of their own brokers/JVMs can add custom configs.

### Verification

- Build: `./compile.pl -DskipTests --projects :org.opennms.features.poller.monitors.core -am install`
- Run monitor tests: `./compile.pl --projects :org.opennms.features.poller.monitors.core -am verify`
- Grep for remaining references: `rg "MinionHeartbeatMonitor|MinionRpcMonitor" --type java`

---

## Step 2: Delete Deprecated Monitors

**Goal:** Remove GpMonitor and JschSshMonitor — both are deprecated with existing replacements.

### Files to Delete

```
features/poller/monitors/core/src/main/java/org/opennms/netmgt/poller/monitors/GpMonitor.java
features/poller/monitors/core/src/main/java/org/opennms/netmgt/poller/monitors/JschSshMonitor.java
```

### Files to Check for Associated Tests

```
features/poller/monitors/core/src/test/java/**/GpMonitor*
features/poller/monitors/core/src/test/java/**/JschSshMonitor*
```

### ServiceLoader Registration

Edit `features/poller/monitors/core/src/main/resources/META-INF/services/org.opennms.netmgt.poller.ServiceMonitor`:
- Remove line: `org.opennms.netmgt.poller.monitors.GpMonitor`
- Remove line: `org.opennms.netmgt.poller.monitors.JschSshMonitor`

### Verification

- Build: `./compile.pl -DskipTests --projects :org.opennms.features.poller.monitors.core -am install`
- Grep for remaining references: `rg "GpMonitor|JschSshMonitor" --type java`

---

## Step 3: Refactor PassiveServiceMonitor via Twin API

**Goal:** Make PassiveServiceMonitor distributable by publishing PassiveStatusKeeper state to Minion via Twin API.

### 3a: Create PassiveStatusTwinPublisher (Pollerd side)

**New file:** `opennms-services/src/main/java/org/opennms/netmgt/passive/PassiveStatusTwinPublisher.java`

**Pattern to follow:** `SnmpV3UserTwinPublisher` — register a Twin publisher that publishes the status map when PassiveStatusKeeper state changes.

```java
// Pseudocode
public class PassiveStatusTwinPublisher {
    private final TwinPublisher twinPublisher;

    // Register twin for passive status map
    // Listen for PassiveStatusKeeper changes
    // Publish full status map on each change
    // Key: "passive-status" or similar well-known key
    // Value: serialized Map<PassiveStatusKey, PollStatus>
}
```

**Integration point:** Initialize in Pollerd's Spring context alongside PassiveStatusKeeper. When PassiveStatusKeeper processes a `passiveServiceStatus` event and updates its map, trigger a Twin publish.

### 3b: Create PassiveStatusTwinSubscriber (Minion side)

**New file:** Location TBD (Minion module, likely `features/minion/` or alongside existing Twin subscribers)

```java
// Pseudocode
public class PassiveStatusTwinSubscriber {
    private final TwinSubscriber twinSubscriber;
    private volatile Map<PassiveStatusKey, PollStatus> statusMap;

    // Subscribe to "passive-status" twin
    // Maintain local copy of status map
    // Provide getStatus(nodeLabel, ipAddr, svcName) method
}
```

### 3c: Refactor PassiveServiceMonitor

**File:** `opennms-services/src/main/java/org/opennms/netmgt/poller/monitors/PassiveServiceMonitor.java`

Changes:
1. **Remove** `getEffectiveLocation()` override (no longer forces Default)
2. **Replace** `PassiveStatusKeeper.getInstance().getStatus(...)` with a call to the Twin-backed status provider
3. The provider is obtained via ServiceLoader or OSGi service registry (same pattern as other Minion-side services)

### 3d: Serialization

Define serialization for the status map. Options:
- JSON via Jackson (simplest, matches Twin API conventions)
- Protobuf (if performance is critical)

The map keys are `(nodeLabel, ipAddr, svcName)` tuples; values are `PollStatus` (status code + reason string).

### Verification

- Unit test: PassiveServiceMonitor reads from Twin-backed provider
- Integration test: Publish status via Twin API, verify Minion-side subscriber receives it
- E2E: PassiveServiceMonitor executes on Minion (no `getEffectiveLocation()` override)

---

## Step 4: Verify E2E Tests

**Goal:** Confirm all 39 E2E tests still pass after changes.

```bash
# Run full E2E test suite
# (Specific command depends on E2E test runner — likely docker-compose based)
```

Key validations:
- No monitor/collector in daemon containers opens network connections to managed nodes
- All managed-node monitoring flows through Minion
- PassiveServiceMonitor works via Twin API
- Self-monitoring monitors are gone without breaking anything

---

## Step 5: Documentation

### Update README or DELTA-V_Status.md

Add Minion-mandatory as an achieved milestone:

```markdown
### Minion-Mandatory Architecture (Achieved)
- All 64 ServiceMonitor implementations are distributable (execute on Minion)
- All 14 ServiceCollector implementations are distributable
- Daemon containers are pure schedulers — no network connections to managed nodes
- Self-monitoring monitors (MinionHeartbeatMonitor, MinionRpcMonitor) deleted
- PassiveServiceMonitor refactored to use Twin API for state sync
- Default-location Minion always deployed in container environment
```

---

## Commit Strategy

Each step should be a separate commit for clean history:

1. `refactor: delete self-monitoring monitors (MinionHeartbeat, MinionRpc)`
2. `refactor: delete deprecated monitors (GpMonitor, JschSshMonitor)`
3. `feat: add PassiveStatus Twin API publisher/subscriber`
4. `refactor: make PassiveServiceMonitor distributable via Twin API`
5. `docs: document Minion-mandatory as achieved milestone`

---

## Risk Assessment

| Risk | Mitigation |
|------|------------|
| Removing Minion package breaks monitoring of remote Minions | Remote Minion health monitoring is a separate future concern; Docker healthchecks cover container env |
| PassiveServiceMonitor Twin API adds complexity | Follow proven SnmpV3UserTwinPublisher pattern; well-understood by the codebase |
| Deprecated monitors may have external users | GpMonitor and JschSshMonitor have drop-in replacements (SystemExecuteMonitor, SshMonitor); not in default config |
| Config drift across 3 poller-configuration.xml copies | Apply same changes to all 3; consider deduplicating configs in future |

## Dependencies

- Twin API infrastructure (already exists — used by Trapd config, SNMPv3 users)
- Default-location Minion in docker-compose (already exists)
- Kafka RPC with force-remote=true (already configured)
