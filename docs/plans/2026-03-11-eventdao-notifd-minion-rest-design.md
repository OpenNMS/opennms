# EventDao Removal, Notifd Elimination, and Minion REST Elimination — Design

**Date:** 2026-03-11
**Branch:** `eventbus-redesign`
**Approach:** Bottom-Up Deletion (denormalize first, then cascade deletions)

## Context

The Strike Fighter plan is 100% complete (18/18 tasks). Three deferred cleanup tasks remain:

1. **EventDao interface removal** — The events table was dropped from PostgreSQL (iron rule: events flow via Kafka only). But `EventDao`, its Hibernate implementation, and the `OnmsEvent` entity still exist with 34 consumers.
2. **Notifd elimination** — Notification functionality will be replaced by a separate service. The entire Notifd daemon, notification entities, and notification tables can be removed.
3. **Minion REST dependency elimination** — Minion currently requires `OPENNMS_HTTP_URL` for REST config fetching. This should be replaced with Twin API (Kafka-only).

Additionally, PerspectivePollerd was identified as a valuable daemon that was overlooked in the Delta-V migration. Its migration is scoped here as a follow-on task.

### Iron Rules

- **NO EVENTS TABLE**: Events are NEVER written to PostgreSQL. Only alarms are persisted (by Alarmd).
- **NO ACTIVEMQ**: All cross-container communication uses Kafka topics.
- **Each daemon container is self-contained**: Own EventWriter, EventListener, EventExpander, KafkaEventForwarder.

### DO NOT DELETE — EventConf Infrastructure

`EventConfEventDao`, `EventConfEventDaoHibernate`, and all `EventConf*` classes are part of the **eventconf-in-DB infrastructure** (the `event_conf_event` table). These manage event configuration definitions loaded by daemon containers and are actively used by `EventConfExtensionManager`, `EventConfInitializer`, and `EventConfServiceHelper`. They have nothing to do with the dropped `events` table. **Do not confuse them with `EventDao`.**

## Section 1: Outage Denormalization

### Problem

`OnmsOutage` has two `@ManyToOne` FK relationships to `OnmsEvent`:
- `svcLostEventId` — the event that caused service loss
- `svcRegainedEventId` — the event that restored service

The events table is dropped. These FK columns are orphaned integers pointing at nothing. `QueryManagerDaoImpl` calls `eventDao.get(eventId)` to fetch `OnmsEvent` objects and link them to outages — this returns null since the table doesn't exist.

### Solution

Denormalize outage-event relationships, mirroring the pattern already applied to `OnmsAlarm`.

#### Liquibase Migration

Add denormalized event columns to `outages` table:

| New Column | Type | Purpose |
|---|---|---|
| `svc_lost_event_tsid` | BIGINT | TSID of the service-lost event |
| `svc_lost_event_uei` | VARCHAR(256) | UEI of the service-lost event |
| `svc_regained_event_tsid` | BIGINT | TSID of the service-regained event |
| `svc_regained_event_uei` | VARCHAR(256) | UEI of the service-regained event |

Then drop the old FK columns (`svcLostEventId`, `svcRegainedEventId`). No backfill needed — the events table is already gone, so these columns are already null/orphaned.

Note: New column names use `snake_case` (e.g., `svc_lost_event_tsid`) rather than the legacy PostgreSQL convention (`svclosteventid`). This is intentional modernization, consistent with the alarm denormalization columns (`event_tsid`, `event_uei`, etc.).

Drop and recreate the `node_outages` view (already recreated in 36.0.0 changelog referencing `outages.svclosteventid`/`svcregainedeventid` — those columns are being dropped, so the view must be recreated again with the new column names).

#### OnmsOutage Entity Change

Replace:
```java
@ManyToOne(fetch=FetchType.LAZY)
@JoinColumn(name="svcLostEventId")
private OnmsEvent m_serviceLostEvent;

@ManyToOne(fetch=FetchType.LAZY)
@JoinColumn(name="svcRegainedEventId")
private OnmsEvent m_serviceRegainedEvent;
```

With:
```java
@Column(name="svc_lost_event_tsid")
private Long m_svcLostEventTsid;

@Column(name="svc_lost_event_uei")
private String m_svcLostEventUei;

@Column(name="svc_regained_event_tsid")
private Long m_svcRegainedEventTsid;

@Column(name="svc_regained_event_uei")
private String m_svcRegainedEventUei;
```

Update constructors, getters/setters, and `toString()`. Remove all `OnmsEvent` imports.

#### QueryManager Interface Change

Methods that accept `eventId` parameters change to accept event TSID + UEI:

| Old Signature | New Signature |
|---|---|
| `updateOpenOutageWithEventId(int outageId, long eventId)` | `updateOpenOutageWithEvent(int outageId, long eventTsid, String eventUei)` |
| `updateResolvedOutageWithEventId(int outageId, long eventId)` | `updateResolvedOutageWithEvent(int outageId, long eventTsid, String eventUei)` |
| `closeOutagesForNode(Date, long eventId, int nodeId)` | `closeOutagesForNode(Date, long eventTsid, String eventUei, int nodeId)` |
| `closeOutagesForInterface(Date, long eventId, int nodeId, String ipAddr)` | `closeOutagesForInterface(Date, long eventTsid, String eventUei, int nodeId, String ipAddr)` |
| `closeOutagesForService(Date, long eventId, int nodeId, String ipAddr, String svcName)` | `closeOutagesForService(Date, long eventTsid, String eventUei, int nodeId, String ipAddr, String svcName)` |

#### QueryManagerDaoImpl Change

Remove `@Autowired EventDao m_eventDao`. All methods set denormalized fields directly:

```java
// Before:
outage.setServiceRegainedEvent(m_eventDao.get(eventId));

// After:
outage.setSvcRegainedEventTsid(eventTsid);
outage.setSvcRegainedEventUei(eventUei);
```

All callers of `QueryManager` (primarily Pollerd) must pass event TSID + UEI from the event they already have in hand.

## Section 2: OnmsEvent Entity Deletion Cascade

With outages denormalized and Notifd being eliminated (Section 3), `OnmsEvent` has zero remaining consumers.

### Entities to Delete

| Class | Module | Reason |
|---|---|---|
| `OnmsEvent` | `opennms-model` | Zombie entity mapping non-existent `events` table |
| `OnmsEventParameter` | `opennms-model` | Child entity of OnmsEvent (composite PK) |
| `OnmsEventCollection` | `opennms-model` | JAXB list wrapper for OnmsEvent |
| `OnmsEventTest` | `opennms-model` (test) | Unit test for deleted entity |

### DAO Layer to Delete

| Class | Module | Reason |
|---|---|---|
| `EventDao` | `opennms-dao-api` | Interface for deleted table |
| `EventDaoHibernate` | `opennms-dao` | Hibernate impl for deleted table |
| `MockEventDao` | `opennms-dao-mock` | Mock impl for deleted interface |
| `EventDaoIT` | `opennms-dao` (test) | Integration test for deleted DAO |

### Dead Production Code to Delete

| Class | Module | Reason |
|---|---|---|
| `DaoWebEventRepository` | `opennms-webapp` | Queries non-existent events table for web UI |
| `DaoWebEventRepositoryIT` | `opennms-webapp` (test) | Integration test for deleted class |
| `daoWebRepositoryTestContext.xml` | `opennms-webapp` (test resources) | Test context for deleted class |
| `EventControllerTest` | `opennms-webapp` (test) | Test for deleted event web functionality |
| `EventCommand` | `features/events/shell-commands` | Karaf `events:show` — queries non-existent table |
| `EventListCommand` | `features/events/shell-commands` | Karaf `events:list` — queries non-existent table |
| `EventRestApi` | `opennms-webapp-rest` | Orphaned REST interface (`@Path("events")`), no implementation |

### Production Code to Refactor

| Class | Module | Change |
|---|---|---|
| `QueryManagerDaoImpl` | `opennms-services` | Remove EventDao (covered in Section 1) |
| `QueryManager` | `opennms-services` | Update interface signatures (covered in Section 1) |
| `PerspectivePollerd` | `features/perspectivepoller` | Remove `eventDao.get()` calls, use event object from handler directly |
| `DefaultPollContext` | `opennms-services` | Callers of QueryManager methods with changed signatures — must pass event TSID + UEI |
| `PollerEventProcessor` | `opennms-services` | Callers of QueryManager methods with changed signatures — must pass event TSID + UEI |
| `MockQueryManager` | `opennms-services` (test) | Mock impl of QueryManager — update to match new interface |
| `OpenNMSReportPlugin` | `features/system-report` | Remove event count metric, report 0 |
| `UsageStatisticsReporter` | `features/datachoices` | Remove event count fields (`countAll`, `getNumEventsLastHours`) |

### Spring/OSGi Config to Update

| File | Change |
|---|---|
| `applicationContext-shared.xml` | Remove `eventDao` bean definition + `onmsgi:service` registration |
| `applicationContext-mockDao.xml` | Remove mock EventDao bean |
| Daemon-loader Spring XMLs (trapd, bsmd, perspectivepoller) | Remove `onmsgi:reference` / `osgi:reference` for EventDao |

### Liquibase

- Drop `event_parameters` table explicitly. Liquibase `cascadeConstraints=true` only drops FK constraints on child tables, not the child tables themselves. The `event_parameters` table still exists as an orphaned table with no FK constraint but no parent.

### Test Impact

~20 integration tests reference EventDao for setup/teardown. Case-by-case:
- **Delete**: `EventDaoIT` (tests deleted DAO)
- **DO NOT DELETE**: `EventConfEventDaoIT`, `EventConfSourceDaoIT` — these test `EventConfEventDao` (eventconf-in-DB), not `EventDao`
- **Refactor setup**: `AlarmDaoIT`, `OutageDaoIT`, `NotificationDaoIT`, `MemoDaoIT`, `AcknowledgmentDaoIT` — remove EventDao from test fixture setup, use denormalized fields instead
- **Refactor setup**: REST ITs (`AlarmRestServiceIT`, `AlarmStatsRestServiceIT`, `IPhoneRestServiceIT`) — remove event creation from test data builders
- **Delete or skip**: `DatabasePopulator` event population methods — remove event-related populate calls
- **Evaluate**: Smoke tests, status API tests, system report tests, Eventd tests — determine if EventDao-dependent or just event-related

## Section 3: Notifd Elimination

Notifd functionality will be replaced by a separate service in the future. The entire notification subsystem is removed.

### Daemon Code to Delete

- Notifd daemon class and related code in `opennms-services/` (Notifd.java, BroadcastEventProcessor for notifications, notification strategies, notification task classes)
- `core/daemon-loader-notifd/` — entire module (if exists)
- Notifd Karaf feature definition in `features.xml`
- Remove `notifd` service from Delta-V `docker-compose.yml` (TSID node-id 8 freed)
- Remove from `service-configuration.xml` if present

### Entities to Delete

| Class | Module | Reason |
|---|---|---|
| `OnmsNotification` | `opennms-model` | Entity for dropped notifications table |
| `OnmsUserNotification` | `opennms-model` | Entity for dropped usersNotified table |
| Notification collection/wrapper classes | `opennms-model` | JAXB wrappers |

### DAO Layer to Delete

| Class | Module |
|---|---|
| `NotificationDao` | `opennms-dao-api` |
| `NotificationDaoHibernate` | `opennms-dao` |
| `UserNotificationDao` | `opennms-dao-api` |
| `UserNotificationDaoHibernate` | `opennms-dao` |
| Mock impls | `opennms-dao-mock` |

### Production Code Consumers to Delete or Refactor

| Class | Module | Action |
|---|---|---|
| `AcknowledgmentDaoHibernate` | `opennms-dao` | **Refactor** — has HQL referencing `OnmsNotification` for notification acks. Alarm ack functionality must be preserved; remove notification ack code only. |
| `AcknowledgmentRestService` | `opennms-webapp-rest` | **Refactor** — calls `notificationDao.get()` for notification acks. Remove notification ack path. |
| `DaoWebNotificationRepository` | `opennms-webapp` | **Delete** — web UI notification repository |
| `NotificationRestService` (v1 and v2) | `opennms-webapp-rest` | **Delete** — REST endpoints for notifications |
| `SurveillanceViewNotificationTable` | `features/vaadin-surveillance-views` | **Delete** — Vaadin UI notification panel |
| `DefaultSurveillanceViewService` | `features/vaadin-surveillance-views` | **Refactor** — remove notification-related methods |
| `NoticeFactory`, `NotificationModel` | `opennms-webapp` | **Delete** — legacy notification web helpers |
| `NotifdIT` | `smoke-test` | **Delete** — smoke test for Notifd |

### Config Layer to Delete

- `NotifdConfigManager`, `NotificationManager`, `DestinationPathManager`
- Config model classes in `opennms-config-model/` for notification configs
- Manager/factory classes in `opennms-config/` for notification configs
- Config files: `notifications.xml`, `destinationPaths.xml`, `notificationCommands.xml`, `notifd-configuration.xml`

**DO NOT DELETE:** `GroupManager` — shared infrastructure for user/group/role management (29 consumers across webapp, Spring Security, role management, datachoices). Not notification-specific.

### REST Endpoints to Delete

- `NotificationRestService` v1 (`opennms-webapp-rest/.../v1/`)
- `NotificationRestService` v2 (`opennms-webapp-rest/.../v2/`)
- Associated REST API interfaces

### Liquibase Migration

- Drop `notifications` table
- Drop `usersnotified` table
- Drop related sequences

### Spring/OSGi Config Cleanup

- Remove notification DAO beans from `applicationContext-shared.xml`
- Remove `onmsgi:service` registrations for notification DAOs
- Update any views referencing notifications tables

## Section 4: Minion REST Dependency Elimination

### Docker Compose Changes

Remove from Minion service in `docker-compose.yml`:
- `OPENNMS_HTTP_URL: http://webapp:8980/opennms`
- `OPENNMS_HTTP_USER` / `OPENNMS_HTTP_PASS`

### Minion Entrypoint Changes

Remove from `opennms-container/minion/container-fs/entrypoint.sh`:
- `scvcli set opennms.http` credential storage
- `OPENNMS_HTTP_*` environment variable handling

### Code to Delete

| Class | Module | Reason |
|---|---|---|
| `RestClient` | `features/distributed/core/api` | Interface (3 methods: `getVersion`, `ping`, `getSnmpV3Users`) |
| `HealthTrackingRestClient` | `features/distributed/core/impl` | Decorator for deleted interface |
| Concrete RestClient impls | Various | Any production implementations |
| `ControllerConfig` HTTP URL handling | `features/minion/core/impl` | REST-specific config |

### REST Config Endpoints — Evaluate Individually

The 9 config resources in `opennms-webapp-rest/.../config/` may still serve the webapp UI:

| Resource | Minion Consumer? | Webapp Consumer? | Action |
|---|---|---|---|
| `TrapdConfigurationResource` | No (uses Twin) | Evaluate | Keep if webapp uses it |
| `SnmpConfigurationResource` | Was REST, now Twin | Evaluate | Keep if webapp uses it |
| `AgentConfigurationResource` | Evaluate | Evaluate | Keep if webapp uses it |
| `DataCollectionConfigResource` | Evaluate | Evaluate | Keep if webapp uses it |
| `JmxDataCollectionConfigResource` | Evaluate | Evaluate | Keep if webapp uses it |
| `JavamailConfigurationResource` | Unlikely | Evaluate | Keep if webapp uses it |
| `EmailNorthbounderConfigurationResource` | No | Evaluate | Keep if webapp uses it |
| `SnmpTrapNorthbounderConfigurationResource` | No | Evaluate | Keep if webapp uses it |
| `SyslogNorthbounderConfigurationResource` | No | Evaluate | Keep if webapp uses it |

### SNMPv3 Users — Mandatory Twin Publisher (New Feature)

`getSnmpV3Users()` on `RestClient` represents a real capability that must not be lost. Minion needs SNMPv3 credentials (user, auth protocol/passphrase, priv protocol/passphrase) to perform SNMP operations. Without REST and without a Twin publisher, this config channel would be broken.

**Implementation pattern** (following proven Trapd Twin pattern):

1. **Model class**: Create `SnmpV3UserConfig` with `TWIN_KEY` constant. Wraps the SNMP v3 user credentials from `SnmpPeerFactory`.

2. **Publisher (webapp/core side)**:
   - Watches `SnmpPeerFactory` for configuration changes
   - Publishes via `TwinPublisher.register(SnmpV3UserConfig.TWIN_KEY, SnmpV3UserConfig.class, location)`
   - Sends initial config on registration + updates on change

3. **Subscriber (Minion side)**:
   - Subscribes via `TwinSubscriber.subscribe(SnmpV3UserConfig.TWIN_KEY, SnmpV3UserConfig.class, consumer)`
   - On receipt, applies credentials to local `SnmpPeerFactory`

4. **Blueprint wiring**: Mirror `TrapListener.java` pattern with `<reference interface="TwinSubscriber">`

### Verification Plan

1. Remove `OPENNMS_HTTP_URL` from compose
2. Start Minion
3. Verify health checks pass (Kafka RPC/Sink/Twin)
4. Verify Trapd config arrives via Twin
5. Configure SNMPv3 user, verify it propagates to Minion via Twin
6. Send a trap through Minion, confirm end-to-end flow
7. Run SNMP collection through Minion with v3 credentials

## Section 5: PerspectivePollerd Delta-V Migration (Follow-On)

PerspectivePollerd is a valuable daemon for monitoring from multiple vantage points. It was overlooked during the Delta-V migration and needs to be added as a standalone daemon container.

**Scoped here, detailed design deferred to a separate brainstorm:**

- Create `core/daemon-loader-perspectivepoller/` module (POM, DaemonLifecycleManager, Spring XML)
- Add Karaf feature `opennms-daemon-perspectivepoller` to `features.xml`
- Add to Sentinel assembly's `<installedFeatures>` in `features/container/sentinel/pom.xml`
- Add `perspectivepoller` service to Delta-V `docker-compose.yml` (TSID node-id 7, which is free)
- EventDao dependency removal is covered by Section 2 refactoring
- Evaluate additional dependencies (outage tracking integration, perspective location config)

## Execution Order

Bottom-up, with Notifd and Minion REST as parallel workstreams:

```
Phase 1: Outage Denormalization (Section 1)
  ├── Liquibase migration
  ├── OnmsOutage entity refactor
  ├── QueryManager/QueryManagerDaoImpl refactor
  └── Verify: Pollerd outage tracking still works

Phase 2: Notifd Elimination (Section 3) — can parallel with Phase 1
  ├── Drop notifications/usersNotified tables (Liquibase)
  ├── Delete daemon code, entities, DAOs, configs
  ├── Refactor AcknowledgmentDaoHibernate (remove notification ack code)
  └── Remove from compose
  NOTE: Must delete OnmsNotification BEFORE Phase 3 deletes OnmsEvent
        (OnmsNotification has @ManyToOne FK to OnmsEvent)

Phase 3: OnmsEvent Cascade Deletion (Section 2) — AFTER Phases 1 and 2
  ├── Delete entities (OnmsEvent, OnmsEventParameter, OnmsEventCollection)
  ├── Delete DAOs (EventDao, EventDaoHibernate, MockEventDao)
  ├── Delete dead code (DaoWebEventRepository, shell commands, EventRestApi)
  ├── Refactor remaining consumers (PerspectivePollerd, report plugins)
  ├── Clean up Spring/OSGi configs
  ├── Drop event_parameters table (Liquibase)
  └── Update/delete tests

Phase 4: Minion REST Elimination (Section 4) — can parallel with Phase 3
  ├── Build SNMPv3 Twin publisher/subscriber
  ├── Delete RestClient, HealthTrackingRestClient
  ├── Remove OPENNMS_HTTP_* from compose/entrypoint
  └── Verify Minion end-to-end

Phase 5: PerspectivePollerd Migration (Section 5) — after Phase 3
  └── Separate brainstorm + implementation cycle
```

## Risk Assessment

| Risk | Mitigation |
|---|---|
| OnmsOutage denormalization breaks outage REST API | Verify outage REST responses still include event metadata via new fields |
| Notifd deletion breaks other daemons that send notification events | Verify no daemon depends on Notifd processing; events still flow via Kafka regardless |
| Minion can't boot without REST | Test incrementally; Twin infra is proven |
| SNMPv3 Twin publisher has edge cases | Test with multiple v3 users, different locations |
| Test cascade is large (~20 files) | Handle tests per-phase, not all at once |
| Shared file conflicts (applicationContext-shared.xml) | Handle shared files in Phase 3 after parallel phases complete |
| Parallel agents overwrite shared files | If Phases 1+2 run as parallel agents, coordinate shared files (Liquibase changelog, applicationContext-shared.xml, features.xml) centrally after agents complete |
| OnmsNotification → OnmsEvent ordering | Phase 2 must delete OnmsNotification before Phase 3 deletes OnmsEvent (compile dependency) |
