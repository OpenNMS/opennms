# Strike Fighter / Delta-V Status Report

**Date:** 2026-03-12
**Branch:** `eventbus-redesign`

---

## Original Strike Fighter Plan (Mar 7) — 18/18 tasks, 100%

| Phase | Status |
|-------|--------|
| Phase 1: Delete dead daemons (Vacuumd, Statsd, Actiond, Ackd) + event infrastructure | **DONE** |
| Phase 2: Extract easy daemons (Rtcd, PassiveStatusd→merged into Pollerd) | **DONE** |
| Phase 3: Extract medium daemons (Notifd, Discovery, Trapd, Syslogd, Ticketer, EventTranslator) | **DONE** |
| Phase 4: Docker Compose integration + E2E test | **DONE** (exceeded target — 17 services vs 15) |

## Phase A: Core Elimination (Mar 9) — 7/7 tasks, 100%

Provisiond, Bsmd extracted. Alarmd migrated to daemon image. ActiveMQ eliminated. Core container replaced by one-shot `db-init`.

## EventDao/Notifd/Minion REST (Mar 11) — ALL DONE

- Outage denormalization (OnmsOutage FKs → inline TSID+UEI columns)
- Notifd fully eliminated (daemon, entities, DAOs, REST, tables, Vaadin UI)
- OnmsEvent entity graph deleted (EventDao, MockEventDao, DaoWebEventRepository, Karaf commands)
- OnmsEventParameter refactored to plain POJO
- Minion REST eliminated (RestClient deleted, SNMPv3 Twin API, OPENNMS_HTTP removed)

## Minion E2E Pipeline (Mar 12) — 11/11 tests passing

- KafkaSinkBridge, EventConfInitializer retry, AlarmPersisterImpl systemId fallback
- PR #13 merged

## PerspectivePollerd Migration (Mar 12) — Design Complete

- Design spec written and reviewed (3 rounds): `docs/plans/2026-03-12-perspectivepollerd-cleanup-design.md`
- Implementation pending — will bring service count from 17 → 18
- Dead Notifd mbean cleanup included in scope

## Minion-Mandatory Architecture (Mar 14) — DONE

Audited all 78 ServiceMonitor/ServiceCollector implementations. 73 were already distributable.

- **Deleted** self-monitoring anti-patterns: MinionHeartbeatMonitor, MinionRpcMonitor
- **Deleted** deprecated monitors: GpMonitor (→SystemExecuteMonitor), JschSshMonitor (→SshMonitor)
- **Refactored** PassiveServiceMonitor via Twin API (PassiveStatusKeeper state synced to Minion)
- **Removed** Minion self-monitoring package from poller-configuration.xml (all 3 copies)
- **Removed** OpenNMS-JVM, JMX-Minion, JMX-Kafka self-monitoring service registrations
- All 60 remaining monitors are distributable (execute on Minion via Kafka RPC)
- All 14 collectors are distributable (11 collector-level RPC, 3 protocol-level RPC)
- Audit results: `docs/superpowers/specs/2026-03-14-minion-mandatory-audit-results.md`

---

## Architectural Milestones Achieved

1. Events table **eliminated** from PostgreSQL
2. ActiveMQ **eliminated** entirely
3. Core container **eliminated** → one-shot db-init (312 MB vs 35.6 GB)
4. 14 daemon-loader modules created
5. **17-service Delta-V architecture** on 3 images (horizon, daemon, minion)
6. Kafka-only event transport (2 topics: `opennms-fault-events`, `opennms-ipc-events`)
7. 6 dead daemons deleted (Vacuumd, Statsd, Actiond, Ackd, Queued, Notifd)
8. Minion REST dependency eliminated (Twin API)
9. Full Minion E2E pipeline proven (trap → Minion → Kafka → alarm → PostgreSQL)
10. **Minion-mandatory** — daemons schedule, Minion executes all network I/O

## Current Architecture

```
                    ┌─────────────────────────────────────────────┐
                    │              Docker Compose                  │
                    │                                             │
   ┌────────┐      │  ┌──────────┐  ┌───────────────────────┐   │
   │ Minion │──────┼──│  Kafka   │──│  opennms-fault-events │   │
   │ (traps,│      │  │          │  │  opennms-ipc-events   │   │
   │  SNMP) │      │  └──────────┘  └───────────┬───────────┘   │
   └────────┘      │                            │               │
                    │       ┌───────────────────┼───────────┐   │
                    │       ▼                    ▼           │   │
                    │  ┌─────────┐  ┌──────────────────┐    │   │
                    │  │ Alarmd  │  │   14 Daemon      │    │   │
                    │  │ (TSID=2)│  │   Containers     │    │   │
                    │  │         │  │ (pollerd,trapd,  │    │   │
                    │  │  Kafka  │  │  syslogd,etc.)   │    │   │
                    │  │  → DB   │  │                  │    │   │
                    │  └────┬────┘  └──────────────────┘    │   │
                    │       │                                │   │
                    │       ▼                                │   │
                    │  ┌──────────┐  ┌──────────┐           │   │
                    │  │PostgreSQL│  │  Webapp   │           │   │
                    │  │ (alarms  │  │ (horizon  │           │   │
                    │  │  only)   │  │  image)   │           │   │
                    │  └──────────┘  └──────────┘           │   │
                    │                                        │   │
                    │  ┌──────────┐                          │   │
                    │  │ db-init  │ (one-shot, 312 MB)      │   │
                    │  └──────────┘                          │   │
                    └─────────────────────────────────────────────┘
```

### 17 Services (3 Docker Images)

| Image | Services |
|-------|----------|
| `opennms/horizon` | webapp |
| `opennms/daemon` | alarmd, pollerd, collectd, rtcd, discovery, trapd, syslogd, ticketer, eventtranslator, enlinkd, scriptd, provisiond, bsmd |
| `opennms/minion` | minion |
| `opennms/db-init` | db-init (one-shot) |
| (third-party) | postgres, kafka |

### TSID Node-ID Assignments

| Container | Node-ID | Status |
|-----------|---------|--------|
| (freed — was Core) | 1 | Eliminated |
| Alarmd | 2 | Running |
| Webapp | 3 | Running |
| Pollerd | 4 | Running |
| Collectd | 5 | Running |
| Rtcd | 6 | Running |
| PerspectivePollerd | 7 | Pending extraction |
| (freed — was Notifd) | 8 | Eliminated |
| Discovery | 9 | Running |
| Trapd | 10 | Running |
| Syslogd | 11 | Running |
| Ticketer | 12 | Running |
| EventTranslator | 13 | Running |
| Enlinkd | 14 | Running |
| Scriptd | 15 | Running |
| Provisiond | 16 | Running |
| Bsmd | 17 | Running |

## Remaining Work

| Item | Priority | Status |
|------|----------|--------|
| PerspectivePollerd daemon container extraction | Next | Design complete, implementation pending |
| Dead Notifd mbean in JMX config | Next | Included in PerspectivePollerd scope |
| Minion-mandatory architecture (audit monitors/collectors) | Future | Deferred by design |
| promoteQueueData eventconf cleanup | Done | Already removed (commit `2dda3efde6a`) |
| Queued JMX datacollection cleanup | Done | Already removed from production configs |

## Key Design Documents

| Document | Path |
|----------|------|
| Strike Fighter Design | `docs/plans/2026-03-07-strike-fighter-completion-design.md` |
| Strike Fighter Implementation | `docs/plans/2026-03-07-strike-fighter-completion-implementation.md` |
| Phase A Core Elimination | `docs/plans/2026-03-09-phase-a-core-elimination.md` |
| EventDao/Notifd/Minion REST Design | `docs/plans/2026-03-11-eventdao-notifd-minion-rest-design.md` |
| Minion E2E Pipeline Report | `docs/plans/2026-03-12-minion-e2e-pipeline-report.md` |
| PerspectivePollerd Design | `docs/plans/2026-03-12-perspectivepollerd-cleanup-design.md` |
| Project Status Analysis | `docs/plans/2026-03-10-project-status-analysis.md` |
