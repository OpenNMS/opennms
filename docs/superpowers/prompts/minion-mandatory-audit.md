# Next Session: Minion-Mandatory Monitor/Collector Audit

## Goal

Audit all ~65+ ServiceMonitor and ~13+ ServiceCollector implementations in the Delta-V codebase. Classify each as distributable (runs on Minion), non-distributable (needs refactoring), or dead (delete). This is Phase 1 — research only, no code changes.

Then write an implementation plan for Phase 2 based on the audit findings.

## Context

Delta-V is a microservice decomposition of OpenNMS Horizon with 17 daemon containers + Minion. The architecture principle is: **daemons schedule, Minion executes all network I/O**. No daemon container should open a network connection to a managed node.

### What's Already Done
- All 6 RPC daemons use `LocationAwarePollerClientImpl` / `LocationAwareCollectorClientImpl` with `force-remote=true` via Kafka RPC
- PassiveStatusKeeper merged into Pollerd container
- Default-location Minion in docker-compose, E2E tests pass through Minion path
- All 39 E2E tests passing on Java 21 / Karaf 4.4.9

### What This Session Needs to Do

**Phase 1 — Audit (research, no code):**
1. Read the design spec at `docs/superpowers/specs/2026-03-14-minion-mandatory-audit-design.md`
2. Scan every ServiceMonitor and ServiceCollector implementation across all source locations listed in the spec
3. For each, classify as: distributable, non-distributable, dead, or self-monitoring anti-pattern
4. Save the classification table to `docs/superpowers/specs/2026-03-14-minion-mandatory-audit-results.md`
5. Commit

**Phase 2 — Plan:**
1. Based on audit results, write an implementation plan for:
   - Deleting dead monitors/collectors and self-monitoring anti-patterns (MinionHeartbeatMonitor, MinionRpcMonitor)
   - Refactoring non-distributable monitors (at minimum PassiveServiceMonitor via Twin API)
   - Config cleanup (remove Minion monitoring package from poller-configuration.xml)
   - README update documenting Minion-mandatory as achieved milestone
2. Save plan to `docs/superpowers/plans/`

## Key Design Decisions (Already Made)

- **PassiveServiceMonitor**: Refactor via Twin API — publish PassiveStatusKeeper state to Minion so the monitor can execute there. See spec for detailed component design.
- **Self-monitoring monitors** (MinionHeartbeatMonitor, MinionRpcMonitor): Delete. Platform health is an external concern (Docker healthchecks, K8s probes, Spring Boot actuators), not the polling infrastructure's job.
- **SystemExecuteMonitor, BSFMonitor**: Distributable — they run on Minion host, behavior depends on scripts/commands deployed there.
- **LoopMonitor**: Distributable — synthetic, no network I/O or local state.
- **`getEffectiveLocation()` contract**: Default `AbstractServiceMonitor` implementation passes location through unchanged = inherently distributable. Only overrides returning `"Default"` or `DEFAULT_MONITORING_LOCATION_ID` indicate non-distributable.

## Classification Signals

| Signal | Classification |
|--------|---------------|
| `getEffectiveLocation()` override → `"Default"` | Non-distributable |
| `PassiveStatusKeeper.getInstance()` or similar singletons | Non-distributable |
| Socket/HTTP/SNMP/ICMP network I/O | Distributable (this IS what runs on Minion) |
| Process execution, script execution | Distributable (runs on Minion host) |
| References deleted features/tables | Dead |
| Monitors Delta-V infrastructure health | Self-monitoring anti-pattern → delete |
| No `getEffectiveLocation()` override | Inherently distributable |

## Source Locations to Scan

- `features/poller/monitors/core/` (~57 monitors via ServiceLoader)
- `opennms-services/src/main/java/.../poller/monitors/`
- `integrations/opennms-vmware/`
- `protocols/*/` (Selenium, NSClient, CIFS, RADIUS)
- `opennms-wmi/`
- `opennms-asterisk/`
- `features/wsman/`
- All `META-INF/services/org.opennms.netmgt.poller.ServiceMonitor` files
- All `META-INF/services/org.opennms.netmgt.collection.api.ServiceCollector` files

## Branch and Remote

- **Branch:** `eventbus-redesign`
- **Remote:** push to `delta-v` (pbrane/delta-v), PRs to `pbrane/delta-v`
- **Base branch for PRs:** `develop`
