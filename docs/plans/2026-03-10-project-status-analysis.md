# Delta-V / Strike Fighter Project Status Analysis

**Date:** 2026-03-10

## Plan 1: Strike Fighter Completion Design

**File:** `docs/plans/2026-03-07-strike-fighter-completion-design.md`

**Target:** 15 services (Core + Webapp + 13 daemons)

**Status: SUPERSEDED by Phase A — target exceeded**

The design called for extracting 8 more daemons (Rtcd, PassiveStatusd, Notifd, Discovery, Trapd, Syslogd, Ticketer, EventTranslator) alongside 4 dead daemon deletions and dead event infrastructure removal. All of this was completed before Phase A. Phase A then added Provisiond, Bsmd, Enlinkd, and Scriptd — bringing the total to 17 services (beyond the original 15 target).

| Design Element | Status |
|---|---|
| Delete Vacuumd, Statsd, Actiond, Ackd | DONE |
| Remove events table infrastructure | DONE |
| Extract Rtcd, PassiveStatusd, Notifd, Discovery | DONE |
| Extract Trapd, Syslogd, Ticketer, EventTranslator | DONE |
| Webapp stays as opennms/horizon | DONE |
| One container per daemon | DONE |
| Each daemon embeds KafkaEventForwarder | DONE |
| Scriptd stays in core | CHANGED — extracted to own container |

---

## Plan 2: Strike Fighter Implementation Plan

**File:** `docs/plans/2026-03-07-strike-fighter-completion-implementation.md`

**18 Tasks across 4 Phases**

| Phase | Task | Description | Status |
|---|---|---|---|
| **Phase 1** | Task 1 | Delete Vacuumd | DONE |
| | Task 2 | Delete Statsd | DONE |
| | Task 3 | Delete Actiond | DONE |
| | Task 4 | Delete Ackd | DONE |
| | Task 5 | Delete Dead Event Infrastructure | DONE |
| | Task 6 | Delete Event UI Pages | DONE |
| **Phase 2** | Task 7 | Create daemon-loader-rtcd | DONE |
| | Task 8 | Create daemon-loader-passivestatusd | DONE (later merged into Pollerd) |
| | Task 9 | Wire Phase 2 Karaf Features + Overlays | DONE |
| **Phase 3** | Task 10 | Create daemon-loader-notifd | DONE |
| | Task 11 | Create daemon-loader-discovery | DONE |
| | Task 12 | Create daemon-loader-trapd | DONE |
| | Task 13 | Create daemon-loader-syslogd | DONE |
| | Task 14 | Create daemon-loader-ticketer | DONE |
| | Task 15 | Create daemon-loader-eventtranslator | DONE |
| **Phase 4** | Task 16 | Update Docker Compose (15 services) | DONE (exceeded — 17 services) |
| | Task 17 | Add Daemon Features to Sentinel Assembly | DONE |
| | Task 18 | Build and Test End-to-End | DONE (all 14 daemon containers verified healthy) |

**Completion: 18/18 tasks — 100%**

---

## Plan 3: Minion-Mandatory Deferral Design

**File:** `docs/plans/2026-03-09-minion-mandatory-deferral-design.md`

This was a deferral document with near-term items.

| Item | Status |
|---|---|
| Minion-mandatory architecture deferred | DONE (documented) |
| PassiveStatusd/Pollerd shared-state fix (Option C: merge) | DONE (Phase A Task 5) |
| Default Minion in Docker Compose | DONE |
| Docker Compose native profiles | DONE |
| Investigate Alarmd on opennms/daemon image | DONE (Phase A Task 4: migrated) |
| Eliminate Minion REST dependency (Twin API) | NOT STARTED — post-Strike-Fighter |

**Completion: 5/6 items — 83%** (REST elimination is explicitly deferred)

---

## Plan 4: Phase A — Core Container Elimination

**File:** `docs/plans/2026-03-09-phase-a-core-elimination.md`

| Task | Description | Status |
|---|---|---|
| Task 1 | Delete Queued Daemon | DONE |
| Task 2 | Create daemon-loader-provisiond | DONE |
| Task 3 | Create daemon-loader-bsmd | DONE |
| Task 4 | Migrate Alarmd to opennms/daemon | DONE |
| Task 5 | Merge PassiveStatusKeeper into Pollerd | DONE |
| Task 6 | Eliminate ActiveMQ | DONE |
| Task 7 | Eliminate Core Container (replace with db-init) | DONE |

**Completion: 7/7 tasks — 100%** (PR #7 merged)

---

## Architectural Milestones Achieved

1. **Events table ELIMINATED** from PostgreSQL
2. **ActiveMQ ELIMINATED** entirely
3. **Core container ELIMINATED** — replaced by one-shot db-init
4. **14 daemon-loader modules** created (alarmd, bsmd, collectd, discovery, enlinkd, eventtranslator, notifd, pollerd, provisiond, rtcd, scriptd, syslogd, ticketer, trapd)
5. **17-service Delta-V architecture** running on 3 images (opennms/horizon, opennms/daemon, opennms/minion)
6. **Kafka-only event transport** — two topics (`opennms-fault-events`, `opennms-ipc-events`)
7. **5 dead daemons deleted** — Vacuumd, Statsd, Actiond, Ackd, Queued
8. **PassiveStatusd eliminated** as standalone daemon (merged into Pollerd)

---

## Remaining Work

### Must-Have (production readiness)

- **End-to-end integration test** — in progress (`docs/plans/2026-03-10-e2e-integration-test-implementation.md`)
- **Eliminate Minion REST dependency** — migrate Trapd/Syslogd config sync to Twin API (deferred, documented)

### Should-Have (cleanup)

- Remove dead eventconf definitions for `promoteQueueData` UEI
- Add health ignore cfg files for daemon containers that need them (bsmd, provisiond)
- Verify all MessageBus references cleaned up from daemon source code

### Nice-to-Have (future)

- Minion-mandatory architecture — audit all ServiceMonitor/ServiceCollector implementations
- EventDao interface removal — still exists for read paths (alarm correlation)
- Verify no Queued references remain in JMX datacollection configs
