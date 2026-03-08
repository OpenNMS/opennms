# EventBus Redesign Follow-ups Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Resolve all outstanding items from the EventBus redesign to complete the migration.

**Architecture:** These follow-ups fall into three categories: (A) code gaps from the daemon migration, (B) a materialized event view to replace direct DB queries, and (C) consumer migrations to use the new view.

**Tech Stack:** Java 17, Spring 4.2.x, Kafka 3.6.2, ActiveMQ 5.16.8, PostgreSQL, Jasper Reports

**Prerequisite:** EventBus redesign Phase 1 + Phase 2 complete and merged.

---

## Priority Order

1. **Task 1:** MessageBusFactory (unblocks Vacuumd and any other JMX singletons)
2. **Task 2:** Vacuumd full MessageBus integration (depends on Task 1)
3. **Task 3:** MessageBus-to-EventListener bridge (unblocks PerspectiveServiceTracker)
4. **Task 4:** PerspectiveServiceTracker migration (depends on Task 3)
5. **Task 5:** DefaultPollContext outage event redesign
6. **Task 6:** Materialized event view (unblocks REST, UI, reports, Vacuumd automations)
7. **Task 7:** Vacuumd SQL automation migration (depends on Task 6)
8. **Task 8:** REST API event endpoint migration (depends on Task 6)
9. **Task 9:** Vue UI event page migration (depends on Task 8)
10. **Task 10:** Legacy JSP event page migration (depends on Task 8)
11. **Task 11:** Jasper Reports migration (depends on Task 6)
12. **Task 12:** Smoke test — disable core Alarmd for full independence

---

## Task 1: Create MessageBusFactory

Static factory following the EventIpcManagerFactory pattern, enabling MessageBus access from JMX singleton daemons that lack Spring/OSGi injection.

**Files:**
- Create: `core/messagebus-api/src/main/java/org/opennms/core/messagebus/MessageBusFactory.java`
- Create: `core/messagebus-api/src/test/java/org/opennms/core/messagebus/MessageBusFactoryTest.java`

**Step 1: Write the failing test**

```java
package org.opennms.core.messagebus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.opennms.core.messagebus.local.LocalMessageBus;
import org.junit.After;
import org.junit.Test;

public class MessageBusFactoryTest {

    @After
    public void tearDown() {
        MessageBusFactory.reset();
    }

    @Test
    public void shouldReturnConfiguredMessageBus() {
        MessageBus bus = new LocalMessageBus();
        MessageBusFactory.setMessageBus(bus);
        assertThat(MessageBusFactory.getMessageBus()).isSameAs(bus);
    }

    @Test
    public void shouldThrowWhenNotInitialized() {
        assertThatThrownBy(MessageBusFactory::getMessageBus)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldResetCleanly() {
        MessageBusFactory.setMessageBus(new LocalMessageBus());
        MessageBusFactory.reset();
        assertThatThrownBy(MessageBusFactory::getMessageBus)
                .isInstanceOf(IllegalStateException.class);
    }
}
```

**Step 2: Run test to verify it fails**

Run: `./compile.pl --projects :org.opennms.core.messagebus.api -am verify -Dtest=MessageBusFactoryTest`

**Step 3: Implement MessageBusFactory**

```java
package org.opennms.core.messagebus;

public class MessageBusFactory {

    private static volatile MessageBus s_messageBus;

    private MessageBusFactory() {}

    public static MessageBus getMessageBus() {
        MessageBus bus = s_messageBus;
        if (bus == null) {
            throw new IllegalStateException(
                    "MessageBus not initialized. Call MessageBusFactory.setMessageBus() first.");
        }
        return bus;
    }

    public static void setMessageBus(MessageBus messageBus) {
        s_messageBus = messageBus;
    }

    public static void reset() {
        s_messageBus = null;
    }
}
```

**Step 4: Initialize MessageBusFactory in Eventd startup**

In `applicationContext-eventDaemon.xml`, add an init bean that calls `MessageBusFactory.setMessageBus(messageBus)` during Eventd startup, similar to how `EventIpcManagerFactory` is initialized.

**Step 5: Run tests, commit**

```bash
git add core/messagebus-api/ features/events/daemon/
git commit -m "feat: add MessageBusFactory static accessor for JMX singleton daemons"
```

---

## Task 2: Vacuumd Full MessageBus Integration

Replace the null-guard fallback in Vacuumd with `MessageBusFactory.getMessageBus()`.

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/vacuumd/Vacuumd.java`

**Step 1: Replace null-guarded MessageBus with factory lookup**

In `Vacuumd.init()` or the constructor, replace:
```java
// OLD: MessageBus injected, may be null
if (messageBus != null) {
    messageBus.subscribe(...);
}

// NEW: Always available via factory
MessageBus bus = MessageBusFactory.getMessageBus();
bus.subscribe("reloadVacuumdConfig", this::onReloadVacuumdConfig);
bus.subscribe("reloadDaemonConfig", this::onReloadDaemonConfig);
```

**Step 2: Remove null-guards from publish calls**

Replace all `if (messageBus != null) messageBus.publish(...)` with direct `MessageBusFactory.getMessageBus().publish(...)`.

**Step 3: Run tests, commit**

```bash
git add opennms-services/
git commit -m "feat: integrate Vacuumd with MessageBusFactory, remove null-guards"
```

---

## Task 3: Create MessageBus-to-EventListener Bridge

An adapter that allows components using Spring's `@EventListener` / `@EventHandler` annotations to receive IPC messages from the MessageBus without code changes to the annotated class.

**Files:**
- Create: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/bridge/MessageBusEventListenerBridge.java`
- Create: `features/events/daemon/src/test/java/org/opennms/netmgt/eventd/bridge/MessageBusEventListenerBridgeTest.java`

**Step 1: Understand the @EventListener/@EventHandler annotation processing**

Read how `AnnotationBasedEventListenerAdapter` (or similar) in the events module processes `@EventHandler(ueis={...})` to register listeners. The bridge needs to:
1. Scan for `@EventHandler` annotations on the target bean
2. Extract UEI subscriptions
3. Map internal UEIs to MessageBus message types
4. Subscribe to the MessageBus
5. Convert incoming `IpcMessage` back to a synthetic `IEvent` for the annotated method

**Step 2: Write the bridge**

```java
package org.opennms.netmgt.eventd.bridge;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.core.messagebus.MessageHandler;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

public class MessageBusEventListenerBridge {

    private static final String INTERNAL_UEI_PREFIX = "uei.opennms.org/internal/";

    private final MessageBus messageBus;

    public MessageBusEventListenerBridge(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public void register(Object bean) {
        for (Method method : bean.getClass().getMethods()) {
            EventHandler handler = method.getAnnotation(EventHandler.class);
            if (handler == null) continue;

            for (String uei : handler.ueis()) {
                if (!uei.startsWith(INTERNAL_UEI_PREFIX)) continue;

                String messageType = uei.substring(INTERNAL_UEI_PREFIX.length());
                messageBus.subscribe(messageType, new MessageHandler() {
                    @Override
                    public String getName() {
                        return bean.getClass().getSimpleName() + "." + method.getName();
                    }

                    @Override
                    public void onMessage(IpcMessage message) {
                        Event syntheticEvent = toSyntheticEvent(message, uei);
                        try {
                            method.invoke(bean, syntheticEvent);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to invoke " + method, e);
                        }
                    }
                });
            }
        }
    }

    private Event toSyntheticEvent(IpcMessage message, String uei) {
        Event event = new Event();
        event.setUei(uei);
        event.setSource(message.getSource());
        event.setNodeid(message.getNodeId());
        event.setInterface(message.getInterfaceAddress());
        for (var entry : message.getParameters().entrySet()) {
            Parm parm = new Parm();
            parm.setParmName(entry.getKey());
            Value value = new Value();
            value.setContent(entry.getValue());
            parm.setValue(value);
            event.addParm(parm);
        }
        return event;
    }
}
```

**Step 3: Write test, run, commit**

```bash
git add features/events/daemon/
git commit -m "feat: add MessageBusEventListenerBridge for @EventHandler annotated beans"
```

---

## Task 4: Migrate PerspectiveServiceTracker

**Files:**
- Modify: `features/perspectivepoller/src/main/java/org/opennms/netmgt/perspectivepoller/PerspectiveServiceTracker.java`
- Modify: PerspectivePoller Spring context XML

**Step 1: Register PerspectiveServiceTracker with the bridge**

In the Spring context, add:
```xml
<bean class="org.opennms.netmgt.eventd.bridge.MessageBusEventListenerBridge"
      init-method="register">
    <constructor-arg ref="messageBus"/>
    <property name="target" ref="perspectiveServiceTracker"/>
</bean>
```

Or programmatically in `PerspectivePollerd.init()`:
```java
MessageBusEventListenerBridge bridge = new MessageBusEventListenerBridge(messageBus);
bridge.register(perspectiveServiceTracker);
```

**Step 2: Note — PerspectiveServiceTracker also listens to non-internal events**

It subscribes to 13 events, most of which are NOT internal (e.g., NODE_GAINED_SERVICE, SERVICE_DELETED, NODE_ADDED). These are fault events, not IPC. They will arrive via the EventIpcManager/Kafka path, not the MessageBus.

Only SUSPEND_POLLING_SERVICE and RESUME_POLLING_SERVICE are internal IPC events. The bridge only needs to handle these two. The rest continue through EventIpcManager as fault events.

**Step 3: Run tests, commit**

```bash
git add features/perspectivepoller/
git commit -m "feat: bridge PerspectiveServiceTracker internal events to MessageBus"
```

---

## Task 5: DefaultPollContext Outage Event Redesign

The core problem: `DefaultPollContext` fires OUTAGE_CREATED/RESOLVED events and then tracks `PendingPollEvent` objects, waiting for the event to be processed and matched back via `onEvent()`. This is a synchronous request-response pattern disguised as events.

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/poller/DefaultPollContext.java`

**Step 1: Analyze the PendingPollEvent pattern**

The current flow:
1. `openOutage()` creates an OUTAGE_CREATED event and wraps it in a `PendingPollEvent`
2. The event goes through EventIpcManager → gets eventId from DB → broadcasts
3. `onEvent()` callback receives the event back, matches it by content, and marks the `PendingPollEvent` as complete
4. The outage record's `svclosteventid` is set to the matched event's ID

**Step 2: Replace with direct MessageBus + TSID**

Since events now get TSIDs before broadcast, the flow simplifies:

1. `openOutage()` generates a TSID, assigns it to the event, publishes to MessageBus
2. The outage record's `svclosteventid` is set immediately to the TSID — no need to wait for broadcast callback
3. Remove the `PendingPollEvent` tracking entirely
4. The event is still published to Kafka as a fault event (it has alarm-data for serviceDown)

```java
// OLD:
PendingPollEvent pollEvent = new PendingPollEvent(createEvent(...));
getEventManager().sendNow(pollEvent.getEvent());
// ... wait for onEvent callback ...
outage.setSvcLostEventId(pollEvent.getEvent().getDbid());

// NEW:
Event event = createEvent(EventConstants.OUTAGE_CREATED_EVENT_UEI, ...);
long tsid = TsidFactory.getInstance().create();
event.setDbid(tsid);
messageBus.publish(new IpcMessage("poller/outageCreated", "pollerd",
    Map.of("nodeId", ..., "interface", ..., "service", ...)));
eventForwarder.sendNow(event); // fault event → Kafka
outage.setSvcLostEventId(tsid); // immediately available
```

**Step 3: Remove PendingPollEvent, onEvent matching, and related state**

Delete or simplify `PendingPollEvent` class. Remove the `onEvent()` method's event matching logic for OUTAGE_CREATED/RESOLVED. Keep `onEvent()` for other events it handles (SERVICE_UNRESPONSIVE, etc.).

**Step 4: Update RTC BroadcastEventProcessor**

RTC's `BroadcastEventProcessor` listens for OUTAGE_CREATED/RESOLVED. Migrate it to `messageBus.subscribe("poller/outageCreated", ...)` and `messageBus.subscribe("poller/outageResolved", ...)`.

**Step 5: Update MinionStatusTracker**

`MinionStatusTracker` also listens for OUTAGE_CREATED/RESOLVED. Same MessageBus subscription migration.

**Step 6: Run tests, commit**

```bash
git add opennms-services/ features/
git commit -m "feat: redesign DefaultPollContext outage events with direct TSID assignment"
```

---

## Task 6: Create Materialized Event View

A Kafka consumer that materializes recent events into a queryable store, replacing the `events` table for REST API, UI, and reporting queries.

**Files:**
- Create: `features/events/event-store/pom.xml`
- Create: `features/events/event-store/src/main/java/org/opennms/features/events/store/EventStore.java`
- Create: `features/events/event-store/src/main/java/org/opennms/features/events/store/JdbcEventStore.java`
- Modify: `features/events/pom.xml` — add `<module>event-store</module>`

**Step 1: Design the event store**

The materialized view is a lightweight read-only table populated by a Kafka consumer. It is NOT the authoritative event source (Kafka is). It exists solely for query convenience.

```java
public interface EventStore {
    Optional<StoredEvent> getByTsid(long tsid);
    List<StoredEvent> findByCriteria(EventCriteria criteria);
    long count(EventCriteria criteria);
}
```

**Step 2: Create a read-only events_archive table**

Liquibase migration:
```sql
CREATE TABLE events_archive (
    event_tsid BIGINT PRIMARY KEY,
    event_uei VARCHAR(256) NOT NULL,
    event_source VARCHAR(256),
    event_severity INTEGER,
    event_time TIMESTAMPTZ NOT NULL,
    node_id BIGINT,
    ip_addr VARCHAR(64),
    service_name VARCHAR(256),
    event_log_msg TEXT,
    event_descr TEXT,
    event_display VARCHAR(1) DEFAULT 'Y',
    event_log VARCHAR(1) DEFAULT 'Y',
    event_data JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_events_archive_time ON events_archive (event_time DESC);
CREATE INDEX idx_events_archive_uei ON events_archive (event_uei);
CREATE INDEX idx_events_archive_node ON events_archive (node_id);
```

**Step 3: Create Kafka consumer that populates the archive**

A Kafka consumer subscribed to `opennms-fault-events` that deserializes events and INSERTs into `events_archive`. This runs asynchronously — the archive is eventually consistent.

Configure retention: a scheduled task (or Vacuumd automation) purges rows older than the configured retention period (default: 6 weeks, matching the old vacuumd purge).

**Step 4: Create EventStore implementation**

`JdbcEventStore` queries `events_archive` using JDBC/Hibernate, providing the same query capabilities as the old `EventDao`.

**Step 5: Run tests, commit**

```bash
git add features/events/event-store/ features/events/pom.xml core/schema/
git commit -m "feat: add materialized event store with Kafka consumer and events_archive table"
```

---

## Task 7: Migrate Vacuumd SQL Automations

**Files:**
- Modify: `opennms-base-assembly/src/main/filtered/etc/vacuumd-configuration.xml`

**Step 1: Update the purge events statement**

The old statement deletes from `events`. Replace with deletion from `events_archive`:

```sql
-- OLD:
DELETE FROM events WHERE NOT EXISTS (...) AND eventtime < now() - interval '6 weeks';

-- NEW:
DELETE FROM events_archive WHERE event_time < now() - interval '6 weeks';
```

The archive is the only events table now. Retention is simpler — no need to check outage/notification FKs since those relationships are denormalized on the alarm.

**Step 2: Update outage references**

The `outages` table has `svclosteventid` and `svcregainedeventid` columns that previously FK'd to `events.eventid`. These now contain TSIDs. Update any Vacuumd automations or report queries that JOIN on these columns to use `events_archive.event_tsid` instead.

**Step 3: Run tests, commit**

```bash
git add opennms-base-assembly/
git commit -m "feat: migrate Vacuumd SQL automations from events table to events_archive"
```

---

## Task 8: REST API Event Endpoint Migration

**Files:**
- Modify: `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/EventRestService.java`
- Modify: `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/EventRestService.java`

**Step 1: Replace EventDao with EventStore**

Both V1 and V2 REST services inject `EventDao`. Replace with `EventStore`:

```java
// OLD:
@Autowired
private EventDao m_eventDao;

// NEW:
@Autowired
private EventStore m_eventStore;
```

**Step 2: Update query methods**

Map existing `CriteriaBuilder` queries to `EventCriteria` objects. The `EventStore` provides the same filtering capabilities (by UEI, node, time range, severity) but queries `events_archive` instead of `events`.

**Step 3: Update DTOs**

The response DTOs may reference `eventId` (the old DB sequence Long). Map this to `eventTsid` in the response, or keep the field name as `id` with the TSID value for API compatibility.

**Step 4: Run tests, commit**

```bash
git add opennms-webapp-rest/
git commit -m "feat: migrate REST event endpoints from EventDao to EventStore"
```

---

## Task 9: Vue UI Event Page Migration

**Files:**
- Modify: `ui/src/services/eventService.ts`
- Modify: `ui/src/stores/eventStore.ts`
- Modify any Vue components that display event data

**Step 1: Update eventService.ts**

The REST endpoint paths stay the same (Task 8 preserves the URL structure). The response shape may change slightly if field names changed (e.g., `id` → `eventTsid`). Update the TypeScript interfaces to match.

**Step 2: Update eventStore.ts**

If the response structure changed, update the Pinia store's type definitions.

**Step 3: Test in browser**

Run `cd ui && pnpm dev` and verify event list, event detail, and event search pages render correctly.

**Step 4: Run tests, commit**

```bash
cd ui && pnpm test
git add ui/
git commit -m "feat: update Vue UI event pages for EventStore API"
```

---

## Task 10: Legacy JSP Event Page Migration

**Files:**
- Modify: `opennms-webapp/src/main/webapp/WEB-INF/jsp/event/list.jsp`
- Modify: `opennms-webapp/src/main/webapp/WEB-INF/jsp/event/detail.jsp`
- Modify: `opennms-webapp/src/main/webapp/WEB-INF/jsp/event/index.jsp`
- Modify: `opennms-webapp/src/main/webapp/includes/eventlist.jsp`
- Modify related Java controllers/servlets

**Step 1: Identify the backing controllers**

JSP pages are served by controllers that query `EventDao` or `WebEventRepository`. Find these controllers and update them to use `EventStore`.

**Step 2: Update JSP field references**

If field names changed in the model (e.g., `event.id` → `event.eventTsid`), update the JSP EL expressions.

**Step 3: Run tests, commit**

```bash
git add opennms-webapp/
git commit -m "feat: migrate legacy JSP event pages from EventDao to EventStore"
```

---

## Task 11: Jasper Reports Migration

**Files:**
- Modify: `opennms-base-assembly/src/main/filtered/etc/report-templates/EventAnalysis.jrxml`
- Modify: `opennms-base-assembly/src/main/filtered/etc/report-templates/Early-Morning-Report.jrxml`
- Modify: All other `.jrxml` files that reference the `events` table

**Step 1: Find all affected reports**

Search for `FROM events` or `JOIN events` in all `.jrxml` files.

**Step 2: Update SQL queries**

Replace `events` table references with `events_archive`:

```sql
-- OLD:
SELECT eventuei, ... FROM events WHERE ...

-- NEW:
SELECT event_uei, ... FROM events_archive WHERE ...
```

Update column name mappings:
- `eventid` → `event_tsid`
- `eventuei` → `event_uei`
- `eventsource` → `event_source`
- `eventseverity` → `event_severity`
- `eventtime` → `event_time`
- `nodeid` → `node_id`
- `eventlogmsg` → `event_log_msg`
- `eventdisplay` → `event_display`

**Step 3: Update outage joins**

Reports that JOIN `events ON outages.svclosteventid = events.eventid` need to use:
```sql
JOIN events_archive ON outages.svclosteventid = events_archive.event_tsid
```

**Step 4: Test reports**

Run the reports locally or via the reporting REST API to verify they render correctly.

**Step 5: Commit**

```bash
git add opennms-base-assembly/
git commit -m "feat: migrate Jasper Reports from events table to events_archive"
```

---

## Task 12: Smoke Test — Disable Core Alarmd

**Files:**
- Modify: `smoke-test/src/test/java/org/opennms/smoketest/AlarmdExtractionIT.java`
- Create: test resource `service-configuration-no-alarmd.xml`

**Step 1: Create overlay that disables core Alarmd**

Create a `service-configuration.xml` overlay where Alarmd is `enabled="false"`. Mount this into the core container during the smoke test.

**Step 2: Update AlarmdExtractionIT**

Add a test that:
1. Starts core container with Alarmd disabled
2. Starts standalone Alarmd container
3. Sends a fault event (e.g., nodeDown)
4. Verifies NO alarm is created by the core (Alarmd is disabled)
5. Verifies the standalone Alarmd creates the alarm from Kafka
6. Verifies the alarm has correct denormalized event data and JSONB

**Step 3: Run smoke test**

Run: `./compile.pl -t --projects :smoke-test -am verify -Dtest=AlarmdExtractionIT`

**Step 4: Commit**

```bash
git add smoke-test/
git commit -m "feat: smoke test proving standalone Alarmd with core Alarmd disabled"
```

---

## Parallelization Guide

| Window | Tasks | Notes |
|--------|-------|-------|
| **A** | 1, 2 | MessageBusFactory + Vacuumd. Sequential, small. |
| **B** | 3, 4 | EventListener bridge + PerspectiveServiceTracker. Sequential. |
| **C** | 5 | DefaultPollContext redesign. Standalone, complex. |
| **D** | 6 | Materialized event store. Standalone, complex. Unblocks 7-11. |
| After A-D merge: | | |
| **E** | 7, 8, 9, 10, 11 | Consumer migrations. Can parallelize across windows. |
| **F** | 12 | Smoke test. Depends on everything. |

**Recommended 4-window parallel start:** A, B, C, D simultaneously. Then E+F after merge.
