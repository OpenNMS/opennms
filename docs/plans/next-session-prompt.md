# Next Session Prompt: EventDao Removal Audit + Minion REST Elimination

> Copy everything below the line into the next Claude Code conversation.

---

## Context

We're on the `eventbus-redesign` branch of OpenNMS Horizon (Delta-V architecture). The Strike Fighter plan is 100% complete (18/18 tasks). Two deferred tasks remain from the broader Delta-V roadmap:

1. **EventDao interface removal audit** — The events table was dropped from PostgreSQL (iron rule: events flow via Kafka only, never written to DB). But `EventDao` and its Hibernate implementation still exist in the codebase with 34 consumers. We deferred removal during Strike Fighter because "EventDao has many consumers — it will require a separate careful analysis."

2. **Minion REST dependency elimination** — The Minion currently requires `OPENNMS_HTTP_URL` pointing at the webapp to fetch Trapd/Syslogd configuration at boot. This should be replaced with the IPC Twin API (Kafka-only), eliminating the REST dependency entirely.

## Task 1: EventDao Interface Removal Audit

### What we know

- `EventDao` interface: `opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/EventDao.java`
- Hibernate impl: `opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/EventDaoHibernate.java`
- Mock impl: `opennms-dao-mock/src/main/java/org/opennms/netmgt/dao/mock/MockEventDao.java`
- 34 files import EventDao

### Key consumer categories to analyze

**Production code (critical path):**
- `QueryManagerDaoImpl` — uses `m_eventDao.get(eventId)` to link `OnmsEvent` to `OnmsOutage` records. Outages have FK to events table. **This is the hardest consumer** — outages need event references.
- `DaoWebEventRepository` — webapp event list/search/ack UI. Queries events table for display. **Events table is gone**, so this entire class is dead code.
- `PerspectivePollerd` — uses EventDao in both main code and test
- `UsageStatisticsReporter` — datachoices feature, likely counts events
- `OpenNMSReportPlugin` — system-report feature
- `DatabasePopulator` — test infrastructure, creates sample events

**Karaf shell commands (dead in Delta-V):**
- `EventListCommand`, `EventCommand` — `events:list` and `events:show` Karaf commands that query the events table. Dead code since events table doesn't exist.

**Test-only code (17+ files):**
- `AlarmDaoIT`, `OutageDaoIT`, `NotificationDaoIT`, `MemoDaoIT`, `AcknowledgmentDaoIT`, `EventDaoIT`, etc.
- Various REST service ITs that create events as test fixtures

### What to investigate

1. **OnmsEvent model class** — Does `OnmsEvent` still need to exist as a Hibernate entity? Alarms reference events (FK). What happens to `OnmsAlarm.getLastEvent()` / `OnmsOutage.getServiceLostEvent()`?
2. **Outage-Event FK** — `OnmsOutage` has `@ManyToOne` to `OnmsEvent`. If events table is gone, can outages still reference events? Or do outages need to store event details inline (denormalize)?
3. **Alarm-Event FK** — `OnmsAlarm` has `lastEvent` FK to events table. Same question.
4. **Scope decision** — Full removal (delete EventDao + OnmsEvent entity + all consumers) vs. partial (keep OnmsEvent as a transient/embedded model, remove DB persistence)?

### Desired output

Use the brainstorming skill to design the EventDao removal. We need:
- A classification of all 34 consumers into: DELETE (dead code), REFACTOR (needs alternative), KEEP (still valid)
- A migration strategy for the OnmsEvent FK relationships in OnmsAlarm and OnmsOutage
- A concrete plan for what to delete, what to refactor, and what tests need updating

## Task 2: Minion REST Dependency Elimination

### What we know

- Minion currently has `OPENNMS_HTTP_URL: http://webapp:8980/opennms` in docker-compose
- The `opennms.http` SCV credential is set in the Minion entrypoint
- Minion uses `RestClient` interface (`features/distributed/core/api/`) to fetch configs
- The REST endpoints are in `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/config/`:
  - `TrapdConfigurationResource` — `GET /rest/config/trapd`
  - Plus SNMP, datacollection, JMX, javamail, syslog-northbounder configs

### Twin API already exists for Trapd

Trapd already has Twin API wiring:
- **Publisher side**: `Trapd.java` uses `TwinPublisher` to publish `TrapListenerConfig`
- **Subscriber side**: `TrapListener.java` uses `TwinSubscriber` to receive config updates
- Blueprint wiring: `blueprint-trapd-listener.xml` has `<reference-list interface="TwinSubscriber">`
- **This means Trapd config sync via Twin already works on Minion** — the question is whether the Minion still falls back to REST when Twin is unavailable, and what OTHER configs it fetches via REST

### What to investigate

1. **What configs does Minion fetch via REST?** Audit all `RestClient` usage in Minion/Sentinel codebase. Determine the complete list of REST endpoints the Minion calls.
2. **Which configs already have Twin publishers?** Trapd has one. Does Syslogd? Do other configs?
3. **Can we simply remove the REST fallback?** If Twin covers everything, maybe we just need to remove the REST client dependency and the `OPENNMS_HTTP_*` env vars.
4. **What breaks if we remove `OPENNMS_HTTP_URL`?** Health checks? Version checking? Other features?

### Desired output

Use the brainstorming skill to design the Minion REST elimination. We need:
- A complete inventory of what the Minion fetches via REST
- A gap analysis: which of those already have Twin publishers, which need new ones
- A concrete plan to remove REST dependency and the `OPENNMS_HTTP_*` configuration

## Approach

Please use the **brainstorming** skill to work through both tasks. Start with Task 1 (EventDao) since it's more self-contained and the analysis directly informs what code we can delete. Task 2 (Minion REST) requires more investigation of the Twin API infrastructure.

For both tasks, the end goal is a design doc and implementation plan. We want to remove dead code aggressively — the events table is gone, so anything that reads/writes it is dead.

## Key files to reference

- Design: `docs/plans/2026-03-07-strike-fighter-completion-design.md`
- Implementation: `docs/plans/2026-03-07-strike-fighter-completion-implementation.md`
- Minion deferral: `docs/plans/2026-03-09-minion-mandatory-deferral-design.md`
- Docker compose: `opennms-container/delta-v/docker-compose.yml`
- BUILD.md: `BUILD.md`
