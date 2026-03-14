# Minion-Mandatory Architecture: Monitor/Collector Audit — Design Spec

**Date:** 2026-03-14
**Branch:** `eventbus-redesign`

## Goal

Audit all ServiceMonitor and ServiceCollector implementations to classify them as distributable, non-distributable, or dead. This is the prerequisite for declaring Delta-V "Minion-mandatory" — where daemon containers are pure schedulers and all network I/O with managed nodes flows through Minion via Kafka RPC.

## Architecture Principle

**Daemons schedule, Minion executes.** No ServiceMonitor or ServiceCollector running in a daemon container should open a network connection to a managed node. All network operations are delegated to Minion via `LocationAwarePollerClientImpl` / `LocationAwareCollectorClientImpl` backed by `KafkaRpcClientFactory` with `force-remote=true`.

A Minion at the Default location is always deployed in the container environment, ensuring monitoring works even for nodes without an explicit remote location.

## Current State

### Already Complete
- **RPC migration:** All 6 RPC daemons (Pollerd, Collectd, Discovery, Enlinkd, Provisiond, PerspectivePollerd) use real Kafka RPC with `force-remote=true`
- **PassiveStatusKeeper:** Merged into Pollerd container (no longer a separate PassiveStatusd)
- **Default Minion:** Already in docker-compose, E2E tests pass through Minion path

### Remaining Work
- Full audit of ~65+ monitors and ~13+ collectors across multiple modules
- Refactoring of non-distributable implementations (at minimum PassiveServiceMonitor)
- Deletion of dead and self-monitoring monitors
- Documentation of Minion-mandatory architecture

## Scope

### Phase 1: Audit (This Spec)

Static code analysis of every ServiceMonitor and ServiceCollector implementation. For each, determine:

1. Does it open sockets, send packets, or do DNS lookups? → **Distributable** (network I/O belongs on Minion)
2. Does it access local JVM state (singletons, static fields, local files)? → **Non-distributable** (needs refactoring)
3. Does it reference deleted features, unused tables, or duplicate functionality? → **Dead** (delete)
4. Does it monitor Delta-V infrastructure rather than managed nodes? → **Self-monitoring anti-pattern** (delete — platform health is an external concern)

**Signals in code:**
- `PassiveStatusKeeper.getInstance()` or similar static singletons → non-distributable
- `getEffectiveLocation()` override returning `"Default"` or `DEFAULT_MONITORING_LOCATION_ID` → explicitly non-distributable
- Default `AbstractServiceMonitor.getEffectiveLocation()` passes location through unchanged → inherently distributable
- Direct socket, datagram, HTTP client, ICMP, SNMP usage → distributable (this IS the network I/O that runs on Minion)
- Process execution (e.g., `SystemExecuteMonitor`) → distributable but runs on Minion host, not managed node
- Arbitrary script execution (e.g., `BSFMonitor`) → distributable but behavior depends on script content
- Synthetic/no-op monitors (e.g., `LoopMonitor`) → distributable (no network I/O, no local state)
- References to deleted tables/features → dead
- `MinionHeartbeatMonitor`, `MinionRpcMonitor` → self-monitoring anti-pattern

**Source locations to scan:**
- `features/poller/monitors/core/` (~57 monitors registered via ServiceLoader)
- `opennms-services/src/main/java/.../poller/monitors/` (PassiveServiceMonitor, etc.)
- `integrations/opennms-vmware/` (VmwareMonitor, VmwareCimMonitor)
- `protocols/*/` (Selenium, NSClient, CIFS, RADIUS monitors)
- `opennms-wmi/` (WmiMonitor)
- `opennms-asterisk/` (AsteriskSIPPeerMonitor)
- `features/wsman/` (WsManMonitor)
- All `META-INF/services/org.opennms.netmgt.poller.ServiceMonitor` registration files
- All `META-INF/services/org.opennms.netmgt.collection.api.ServiceCollector` registration files

**Deliverables:**
1. Classification table for every monitor and collector — saved to `docs/superpowers/specs/2026-03-14-minion-mandatory-audit-results.md`
2. Deletion list (dead + self-monitoring), including config cleanup (e.g., Minion package in `poller-configuration.xml`)
3. Refactoring list with proposed fix for each non-distributable implementation

### Phase 2: Implementation (Follow-Up)

1. Delete dead monitors/collectors
2. Implement PassiveServiceMonitor Twin API refactoring
3. Implement any other non-distributable fixes found in audit
4. Verify all monitors/collectors execute on Minion
5. Update README with Minion-mandatory milestone

## PassiveServiceMonitor Refactoring (Known Non-Distributable)

### Current Architecture

```
Pollerd container:
  PassiveStatusKeeper (static HashMap) ← receives passiveServiceStatus events
  PassiveServiceMonitor.poll() → reads HashMap → returns status
  getEffectiveLocation() → returns "Default" (forces local execution)
```

### Target Architecture

```
Pollerd container:
  PassiveStatusKeeper (HashMap) ← receives passiveServiceStatus events
  PassiveStatusTwinPublisher → publishes status map to Twin API on changes

Minion:
  PassiveStatusTwinSubscriber → receives status map from Twin API
  PassiveServiceMonitor.poll() → reads local copy → returns status
```

### Components

1. **PassiveStatusTwinPublisher** (Pollerd side) — watches PassiveStatusKeeper for changes, publishes the full status map via `TwinPublisher.register()`. Follows the proven SnmpV3UserTwinPublisher pattern.
2. **PassiveStatusTwinSubscriber** (Minion side) — subscribes to the status map via `TwinSubscriber.subscribe()`, maintains a local copy.
3. **PassiveServiceMonitor refactor** — remove `getEffectiveLocation()` override (no longer forcing Default), read from a Twin-backed status provider instead of `PassiveStatusKeeper.getInstance()`.

### Why Twin API

Twin API is the established pattern for daemon→Minion state sync in Delta-V (Trapd config, SNMPv3 users). It handles initial state + incremental updates, and the subscriber is location-aware.

## Handling Other Non-Distributable Findings

| Situation | Strategy |
|-----------|----------|
| Depends on local state publishable via Twin | Same pattern as PassiveServiceMonitor |
| Depends on complex local state (too complex for Twin) | Flag for future redesign; document limitation |
| Dead/unused in Delta-V | Delete |
| Self-monitoring (monitors Delta-V infrastructure, not managed nodes) | Delete — platform health is an external concern, not the polling infrastructure's job |

### Self-Monitoring Anti-Pattern

MinionHeartbeatMonitor, MinionRpcMonitor, and similar monitors that test Delta-V infrastructure health are anti-patterns in the containerized architecture. Application/infrastructure health should be monitored externally by the hosting framework (Kubernetes probes, Docker healthchecks, Spring Boot actuators).

In production, Minions are deployed at remote locations outside the Delta-V container environment. External monitoring of remote Minions requires a separate purpose-built health solution (future task), not self-monitoring via the OpenNMS polling subsystem.

## Success Criteria

- Every ServiceMonitor and ServiceCollector classified
- No monitor/collector in daemon containers opens network connections to managed nodes
- All managed-node monitoring flows through Minion
- Self-monitoring monitors deleted
- E2E tests pass (39/39)
- README documents Minion-mandatory as achieved milestone

## Files Changed (Phase 1 — Audit Only)

Phase 1 produces a classification document only — no code changes. Phase 2 (implementation) will have its own plan with specific file changes.
