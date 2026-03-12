# EventDao Removal, Notifd Elimination, and Minion REST Elimination — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the dead EventDao/OnmsEvent entity graph, eliminate the Notifd daemon, and replace Minion's REST dependency with Twin API.

**Architecture:** Bottom-up deletion — denormalize OnmsOutage first (Phase 1), delete Notifd (Phase 2), cascade-delete OnmsEvent (Phase 3), then remove Minion REST (Phase 4). Phases 1+2 can run in parallel. Phase 3 depends on both. Phase 4 is independent.

**Tech Stack:** Java 17, Hibernate/JPA, Liquibase, Spring XML, OSGi/Karaf, Kafka Twin API

**Spec:** `docs/plans/2026-03-11-eventdao-notifd-minion-rest-design.md`

---

## SAFETY RULES

- **DO NOT DELETE** any `EventConf*` class (`EventConfEventDao`, `EventConfEventDaoHibernate`, `EventConfSourceDao`, etc.) — these manage the living `event_conf_event` table
- **DO NOT DELETE** `GroupManager` — shared infrastructure (29 consumers), NOT notification-specific
- **DO NOT DELETE** `smoke-test/.../utils/RestClient.java` — different class from the distributed `RestClient`
- **DO NOT DELETE** `OnmsAcknowledgment` or `AcknowledgmentDao` — alarm acks must be preserved
- **DO NOT DELETE** `OnmsEventParameter` — used by `OnmsAlarm`, `NorthboundAlarm`, `AlarmMapper`, `ProtobufMapper`, and 10+ other alarm pipeline classes. Must be **refactored to a plain POJO**, not deleted.

---

## Chunk 1: Phase 1 — Outage Denormalization

### Task 1: Liquibase Migration for Outage Denormalization

**Files:**
- Modify: `core/schema/src/main/liquibase/36.0.0/changelog.xml` (append before closing tag)

- [ ] **Step 1: Add denormalization changeset**

Append before the `</databaseChangeLog>` closing tag in `core/schema/src/main/liquibase/36.0.0/changelog.xml`:

```xml
    <changeSet id="36.0.0-add-outage-event-denormalization" author="delta-v">
        <comment>Denormalize event data onto outages — events table is gone, FK columns are orphaned</comment>

        <addColumn tableName="outages">
            <column name="svc_lost_event_tsid" type="BIGINT"/>
            <column name="svc_lost_event_uei" type="VARCHAR(256)"/>
            <column name="svc_regained_event_tsid" type="BIGINT"/>
            <column name="svc_regained_event_uei" type="VARCHAR(256)"/>
        </addColumn>
    </changeSet>

    <changeSet id="36.0.0-drop-outage-event-fk-columns" author="delta-v">
        <comment>Drop orphaned FK columns — events table was dropped with cascadeConstraints</comment>

        <dropView viewName="node_outages"/>

        <dropColumn tableName="outages" columnName="svclosteventid"/>
        <dropColumn tableName="outages" columnName="svcregainedeventid"/>

        <createView replaceIfExists="true" viewName="node_outages">
            SELECT
              outages.outageid,
              outages.svc_lost_event_tsid,
              outages.svc_lost_event_uei,
              outages.svc_regained_event_tsid,
              outages.svc_regained_event_uei,
              outages.iflostservice,
              outages.ifregainedservice,
              outages.ifserviceid,
              5 AS eventseverity,
              (outages.ifregainedservice IS NOT NULL) AS resolved,
              s.servicename,
              ifs.serviceid,
              ipif.ipaddr,
              COALESCE(outages.ifregainedservice - outages.iflostservice, now() - outages.iflostservice) AS duration,
              nos.max_outage_severity,
              nc.*
            FROM
              outages
            JOIN ifservices ifs ON outages.ifserviceid = ifs.id
            JOIN service s ON ifs.serviceid = s.serviceid
            JOIN ipinterface ipif ON ifs.ipinterfaceid = ipif.id
            JOIN node_categories nc ON ipif.nodeid = nc.nodeid
            JOIN node_outage_status nos ON nc.nodeid = nos.nodeid
            WHERE outages.perspective IS NULL
        </createView>
    </changeSet>

```

- [ ] **Step 2: Verify Liquibase XML is well-formed**

Run: `xmllint --noout core/schema/src/main/liquibase/36.0.0/changelog.xml`
Expected: No output (valid XML)

- [ ] **Step 3: Commit**

```bash
git add core/schema/src/main/liquibase/36.0.0/changelog.xml
git commit -m "feat(schema): denormalize outage-event FKs, drop event_parameters and notifications tables"
```

---

### Task 2: Refactor OnmsOutage Entity

**Files:**
- Modify: `opennms-model/src/main/java/org/opennms/netmgt/model/OnmsOutage.java`

- [ ] **Step 1: Replace OnmsEvent FK fields with denormalized columns**

In `OnmsOutage.java`, replace the two `OnmsEvent` fields and their getters/setters (lines ~84-88, 226-280) with denormalized fields:

Remove these fields:
```java
private OnmsEvent m_serviceRegainedEvent;
private OnmsEvent m_serviceLostEvent;
```

Add these fields:
```java
private Long m_svcLostEventTsid;
private String m_svcLostEventUei;
private Long m_svcRegainedEventTsid;
private String m_svcRegainedEventUei;
```

Replace the `@ManyToOne` getters/setters for `serviceLostEvent` and `serviceRegainedEvent` with:

```java
@Column(name="svc_lost_event_tsid")
public Long getSvcLostEventTsid() {
    return m_svcLostEventTsid;
}

public void setSvcLostEventTsid(Long svcLostEventTsid) {
    m_svcLostEventTsid = svcLostEventTsid;
}

@Column(name="svc_lost_event_uei")
public String getSvcLostEventUei() {
    return m_svcLostEventUei;
}

public void setSvcLostEventUei(String svcLostEventUei) {
    m_svcLostEventUei = svcLostEventUei;
}

@Column(name="svc_regained_event_tsid")
public Long getSvcRegainedEventTsid() {
    return m_svcRegainedEventTsid;
}

public void setSvcRegainedEventTsid(Long svcRegainedEventTsid) {
    m_svcRegainedEventTsid = svcRegainedEventTsid;
}

@Column(name="svc_regained_event_uei")
public String getSvcRegainedEventUei() {
    return m_svcRegainedEventUei;
}

public void setSvcRegainedEventUei(String svcRegainedEventUei) {
    m_svcRegainedEventUei = svcRegainedEventUei;
}
```

- [ ] **Step 2: Update constructors**

Remove all `OnmsEvent` parameters from constructors. The full constructor becomes:
```java
public OnmsOutage(Date ifLostService, Date ifRegainedService, OnmsMonitoredService monitoredService, Date suppressTime, String suppressedBy) {
    m_ifLostService = ifLostService;
    m_ifRegainedService = ifRegainedService;
    m_monitoredService = monitoredService;
    m_suppressTime = suppressTime;
    m_suppressedBy = suppressedBy;
}
```

Remove the constructor `OnmsOutage(Date ifLostService, OnmsEvent eventBySvcLostEvent, OnmsMonitoredService monitoredService)`.

- [ ] **Step 3: Update toString()**

Remove `.add("ifRegainedServiceEvent", m_serviceRegainedEvent)` from `toString()`. Add:
```java
.add("svcLostEventTsid", m_svcLostEventTsid)
.add("svcLostEventUei", m_svcLostEventUei)
.add("svcRegainedEventTsid", m_svcRegainedEventTsid)
.add("svcRegainedEventUei", m_svcRegainedEventUei)
```

- [ ] **Step 4: Remove OnmsEvent imports**

Remove all `import org.opennms.netmgt.model.OnmsEvent;` from OnmsOutage.java.

- [ ] **Step 5: Compile check**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :opennms-model -am install`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add opennms-model/src/main/java/org/opennms/netmgt/model/OnmsOutage.java
git commit -m "refactor(model): denormalize OnmsOutage event fields, remove OnmsEvent FK"
```

---

### Task 3: Refactor QueryManager Interface and Implementation

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/poller/QueryManager.java`
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/poller/QueryManagerDaoImpl.java`

- [ ] **Step 1: Update QueryManager interface**

In `QueryManager.java`, change these method signatures:

```java
// Old:
void updateOpenOutageWithEventId(int outageId, long lostEventId);
void updateResolvedOutageWithEventId(int outageId, long regainedEventId);
void closeOutagesForNode(Date closeDate, long eventId, int nodeId);
void closeOutagesForInterface(Date closeDate, long eventId, int nodeId, String ipAddr);
void closeOutagesForService(Date closeDate, long eventId, int nodeId, String ipAddr, String serviceName);

// New:
void updateOpenOutageWithEvent(int outageId, long eventTsid, String eventUei);
void updateResolvedOutageWithEvent(int outageId, long eventTsid, String eventUei);
void closeOutagesForNode(Date closeDate, long eventTsid, String eventUei, int nodeId);
void closeOutagesForInterface(Date closeDate, long eventTsid, String eventUei, int nodeId, String ipAddr);
void closeOutagesForService(Date closeDate, long eventTsid, String eventUei, int nodeId, String ipAddr, String serviceName);
```

- [ ] **Step 2: Update QueryManagerDaoImpl**

In `QueryManagerDaoImpl.java`:

1. Remove `@Autowired private EventDao m_eventDao;` and its import.

2. Update `updateOpenOutageWithEventId` → `updateOpenOutageWithEvent`:
```java
@Override
public void updateOpenOutageWithEvent(int outageId, long eventTsid, String eventUei) {
    LOG.info("updating open outage {} with event tsid {}", outageId, eventTsid);
    final OnmsOutage outage = m_outageDao.get(outageId);
    if (outage == null) {
        LOG.warn("Failed to update outage {}. The outage no longer exists.", outageId);
        return;
    }
    outage.setSvcLostEventTsid(eventTsid);
    outage.setSvcLostEventUei(eventUei);
    m_outageDao.saveOrUpdate(outage);
}
```

3. Update `updateResolvedOutageWithEventId` → `updateResolvedOutageWithEvent`:
```java
@Override
public void updateResolvedOutageWithEvent(int outageId, long eventTsid, String eventUei) {
    LOG.info("updating resolved outage {} with event tsid {}", outageId, eventTsid);
    final OnmsOutage outage = m_outageDao.get(outageId);
    if (outage == null) {
        LOG.warn("Failed to update outage {}. The outage no longer exists.", outageId);
        return;
    }
    outage.setSvcRegainedEventTsid(eventTsid);
    outage.setSvcRegainedEventUei(eventUei);
    m_outageDao.saveOrUpdate(outage);
}
```

4. Update `closeOutagesForNode`, `closeOutagesForInterface`, `closeOutagesForService` — replace `m_eventDao.get(eventId)` with direct field setting:
```java
// In each close method, replace:
outage.setServiceRegainedEvent(m_eventDao.get(eventId));
// With:
outage.setSvcRegainedEventTsid(eventTsid);
outage.setSvcRegainedEventUei(eventUei);
```

5. Remove `import org.opennms.netmgt.dao.api.EventDao;` and `import org.opennms.netmgt.model.OnmsEvent;`.

- [ ] **Step 3: Commit**

```bash
git add opennms-services/src/main/java/org/opennms/netmgt/poller/QueryManager.java
git add opennms-services/src/main/java/org/opennms/netmgt/poller/QueryManagerDaoImpl.java
git commit -m "refactor(poller): update QueryManager to use event TSID+UEI instead of EventDao"
```

---

### Task 4: Update QueryManager Callers

**Files:**
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/poller/DefaultPollContext.java`
- Modify: `opennms-services/src/main/java/org/opennms/netmgt/poller/PollerEventProcessor.java`
- Modify: `opennms-services/src/test/java/org/opennms/netmgt/mock/MockQueryManager.java`

- [ ] **Step 1: Update DefaultPollContext**

In `DefaultPollContext.java`:

Line ~223-228 (`openOutage` method): Change:
```java
getQueryManager().updateOpenOutageWithEventId(outageId, eventId);
```
To pass TSID and UEI from the event object already available in the method. The `svcLostEvent` is the `Event` XML object. Use `svcLostEvent.getDbid()` as TSID and `svcLostEvent.getUei()` as UEI:
```java
getQueryManager().updateOpenOutageWithEvent(outageId, svcLostEvent.getDbid(), svcLostEvent.getUei());
```

Line ~241-253 (`resolveOutage` method): Same pattern:
```java
getQueryManager().updateResolvedOutageWithEvent(outageId, svcRegainEvent.getDbid(), svcRegainEvent.getUei());
```

- [ ] **Step 2: Update PollerEventProcessor**

In `PollerEventProcessor.java`:

Line ~338 (`nodeDeletedHandler`): Change:
```java
getPoller().getQueryManager().closeOutagesForNode(closeDate, event.getDbid(), nodeId.intValue());
```
To:
```java
getPoller().getQueryManager().closeOutagesForNode(closeDate, event.getDbid(), event.getUei(), nodeId.intValue());
```

Line ~409 (`interfaceDeletedHandler`): Change:
```java
getPoller().getQueryManager().closeOutagesForInterface(closeDate, event.getDbid(), nodeId.intValue(), str(ipAddr));
```
To:
```java
getPoller().getQueryManager().closeOutagesForInterface(closeDate, event.getDbid(), event.getUei(), nodeId.intValue(), str(ipAddr));
```

Line ~434 (`serviceDeletedHandler`): Change:
```java
getPoller().getQueryManager().closeOutagesForService(closeDate, event.getDbid(), nodeId.intValue(), str(ipAddr), service);
```
To:
```java
getPoller().getQueryManager().closeOutagesForService(closeDate, event.getDbid(), event.getUei(), nodeId.intValue(), str(ipAddr), service);
```

Line ~731-732 (`closeOutagesForService`): Same pattern — add `event.getUei()` parameter.

- [ ] **Step 3: Update MockQueryManager**

In `MockQueryManager.java`, update method signatures to match new interface. These are stubs, so just update signatures and add the `String eventUei` parameter.

- [ ] **Step 4: Compile check**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :opennms-services -am install`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add opennms-services/src/main/java/org/opennms/netmgt/poller/DefaultPollContext.java
git add opennms-services/src/main/java/org/opennms/netmgt/poller/PollerEventProcessor.java
git add opennms-services/src/test/java/org/opennms/netmgt/mock/MockQueryManager.java
git commit -m "refactor(poller): update QueryManager callers to pass event TSID+UEI"
```

---

## Chunk 2: Phase 2 — Notifd Elimination

### Task 5: Delete Notifd Daemon Code

**Files:**
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/notifd/` (entire package)
- Delete: `core/daemon-loader-notifd/` (entire module)
- Delete: `opennms-services/src/test/java/org/opennms/netmgt/notifd/` (entire test package)
- Delete: `smoke-test/src/test/java/org/opennms/smoketest/NotifdIT.java`
- Modify: `opennms-container/delta-v/docker-compose.yml` — remove `notifd` service

- [ ] **Step 1: Delete Notifd daemon package**

```bash
rm -rf opennms-services/src/main/java/org/opennms/netmgt/notifd/
```

- [ ] **Step 2: Delete Notifd test package**

```bash
rm -rf opennms-services/src/test/java/org/opennms/netmgt/notifd/
```

- [ ] **Step 3: Delete daemon-loader-notifd module**

```bash
rm -rf core/daemon-loader-notifd/
```

Remove the `<module>daemon-loader-notifd</module>` entry from `core/pom.xml`.

- [ ] **Step 4: Delete NotifdIT smoke test**

```bash
rm -f smoke-test/src/test/java/org/opennms/smoketest/NotifdIT.java
```

- [ ] **Step 5: Remove notifd from docker-compose.yml**

In `opennms-container/delta-v/docker-compose.yml`, remove the entire `notifd:` service block.

- [ ] **Step 6: Remove notifd from service-configuration.xml and confd template**

In `opennms-container/core/tarball-root/etc/service-configuration.xml`, remove:
```xml
<service enabled="true"><name>OpenNMS:Name=Notifd</name></service>
```

Also check and remove from the confd template at `opennms-container/core/container-fs/confd/templates/service-configuration.xml.tmpl`.

- [ ] **Step 7: Remove Notifd Karaf feature and Sentinel assembly entry**

In `container/features/src/main/resources/features.xml`, remove the `opennms-daemon-notifd` feature definition block.

In `features/container/sentinel/pom.xml`, remove `<feature>opennms-daemon-notifd</feature>` from `<installedFeatures>`.

- [ ] **Step 8: Delete notifd-overlay directory**

```bash
rm -rf opennms-container/delta-v/notifd-overlay/
```

- [ ] **Step 9: Clean up dispatcher-servlet.xml notification references**

In `opennms-container/delta-v/webapp-jetty-webinf-overlay/dispatcher-servlet.xml`, remove:
- `NotifdConfigFactory.init` bean
- `NotificationFactory.init` and `NotificationFactory` beans
- `AcknowledgeNotificationController` bean
- `NotificationFilterController` bean
- `EventNoticesController` bean
- Navigation entry for "Notifications"

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat(delta-v): eliminate Notifd daemon, daemon-loader, and compose service"
```

---

### Task 6: Delete Notification Entities and DAOs

**Files:**
- Delete: `opennms-model/src/main/java/org/opennms/netmgt/model/OnmsNotification.java`
- Delete: `opennms-model/src/main/java/org/opennms/netmgt/model/OnmsUserNotification.java`
- Delete: `opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/NotificationDao.java`
- Delete: `opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/UserNotificationDao.java`
- Delete: `opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/NotificationDaoHibernate.java`
- Delete: `opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/UserNotificationDaoHibernate.java`
- Delete: `opennms-dao-mock/src/main/java/org/opennms/netmgt/dao/mock/MockNotificationDao.java`
- Delete: `opennms-dao-mock/src/main/java/org/opennms/netmgt/dao/mock/MockUserNotificationDao.java`
- Modify: `opennms-dao/src/main/resources/META-INF/opennms/applicationContext-shared.xml` — remove notification DAO beans (lines ~211-214, ~236-239)
- Modify: `opennms-dao-mock/src/main/resources/META-INF/opennms/applicationContext-mockDao.xml` — remove mock notification DAO beans (lines ~111-112, ~151)

- [ ] **Step 1: Add Liquibase changeset to drop notifications tables**

Append to `core/schema/src/main/liquibase/36.0.0/changelog.xml` (before `</databaseChangeLog>`):

```xml
    <changeSet id="36.0.0-drop-notifications-tables" author="delta-v">
        <comment>Drop notification tables — Notifd daemon eliminated from Delta-V</comment>
        <dropTable tableName="usersnotified" cascadeConstraints="true"/>
        <dropTable tableName="notifications" cascadeConstraints="true"/>
        <sql>DROP SEQUENCE IF EXISTS notifynxtid;</sql>
    </changeSet>
```

- [ ] **Step 2: Delete entity classes**

```bash
rm -f opennms-model/src/main/java/org/opennms/netmgt/model/OnmsNotification.java
rm -f opennms-model/src/main/java/org/opennms/netmgt/model/OnmsUserNotification.java
```

Also search for and delete any collection wrappers:
```bash
find opennms-model -name "*Notification*Collection*" -delete
```

- [ ] **Step 3: Delete DAO interfaces**

```bash
rm -f opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/NotificationDao.java
rm -f opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/UserNotificationDao.java
```

- [ ] **Step 4: Delete DAO implementations**

```bash
rm -f opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/NotificationDaoHibernate.java
rm -f opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/UserNotificationDaoHibernate.java
rm -f opennms-dao-mock/src/main/java/org/opennms/netmgt/dao/mock/MockNotificationDao.java
rm -f opennms-dao-mock/src/main/java/org/opennms/netmgt/dao/mock/MockUserNotificationDao.java
```

- [ ] **Step 5: Remove notification DAO beans from applicationContext-shared.xml**

In `opennms-dao/src/main/resources/META-INF/opennms/applicationContext-shared.xml`, remove:
- The `notificationDao` bean definition + `onmsgi:service` (lines ~211-214)
- The `userNotificationDao` bean definition + `onmsgi:service` (lines ~236-239)

- [ ] **Step 6: Remove mock beans from applicationContext-mockDao.xml**

In `opennms-dao-mock/src/main/resources/META-INF/opennms/applicationContext-mockDao.xml`, remove:
- The `notificationDao` mock bean + `onmsgi:service` (lines ~111-112)
- The `userNotificationDao` mock bean (line ~151)

- [ ] **Step 7: Commit**

```bash
git add core/schema/src/main/liquibase/36.0.0/changelog.xml
git add opennms-model/ opennms-dao-api/ opennms-dao/ opennms-dao-mock/
git commit -m "refactor(dao): drop notification tables, delete notification entities, DAOs, and Spring bean wiring"
```

---

### Task 7: Delete Notification Consumers and Config

**Files:**
- Delete: `opennms-webapp/src/main/java/org/opennms/web/notification/DaoWebNotificationRepository.java`
- Delete: `opennms-webapp/src/main/java/org/opennms/web/notification/NoticeFactory.java`
- Delete: `opennms-webapp/src/main/java/org/opennms/web/notification/NotificationModel.java`
- Delete: `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/NotificationRestService.java`
- Delete: `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/NotificationRestService.java`
- Delete: `features/vaadin-surveillance-views/src/main/java/org/opennms/features/vaadin/surveillanceviews/ui/dashboard/SurveillanceViewNotificationTable.java`
- Modify: `features/vaadin-surveillance-views/src/main/java/org/opennms/features/vaadin/surveillanceviews/service/DefaultSurveillanceViewService.java` — remove notification methods
- Modify: `opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/AcknowledgmentDaoHibernate.java` — remove notification ack HQL (lines ~92-95, ~137)
- Modify: `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/AcknowledgmentRestService.java` — remove notification ack path
- Delete: `opennms-config/src/main/java/org/opennms/netmgt/config/NotifdConfigManager.java`
- Delete: `opennms-config/src/main/java/org/opennms/netmgt/config/NotificationManager.java`
- Delete: `opennms-config/src/main/java/org/opennms/netmgt/config/DestinationPathManager.java`

- [ ] **Step 1: Delete webapp notification code**

```bash
rm -f opennms-webapp/src/main/java/org/opennms/web/notification/DaoWebNotificationRepository.java
rm -f opennms-webapp/src/main/java/org/opennms/web/notification/NoticeFactory.java
rm -f opennms-webapp/src/main/java/org/opennms/web/notification/NotificationModel.java
```

- [ ] **Step 2: Delete notification REST services**

```bash
rm -f opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/NotificationRestService.java
rm -f opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/NotificationRestService.java
```

Also search for and delete associated REST API interfaces:
```bash
find opennms-webapp-rest -name "*NotificationRest*" -o -name "*NotificationApi*" | head -20
```
Delete any interface files found.

- [ ] **Step 3: Delete Vaadin notification UI**

```bash
rm -f features/vaadin-surveillance-views/src/main/java/org/opennms/features/vaadin/surveillanceviews/ui/dashboard/SurveillanceViewNotificationTable.java
```

In `DefaultSurveillanceViewService.java`, remove all methods that reference `NotificationDao`, `OnmsNotification`, or `UserNotificationDao`. Remove the corresponding `@Autowired` fields and imports.

- [ ] **Step 4: Refactor AcknowledgmentDaoHibernate**

In `opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/AcknowledgmentDaoHibernate.java`:
- Remove the HQL queries referencing `OnmsNotification` (lines ~92-95, ~137): `"from OnmsNotification as n where n.event.alarm = ?"`
- Remove any methods that return or process notification acknowledgments
- Keep all alarm acknowledgment code intact

- [ ] **Step 5: Refactor AcknowledgmentRestService**

In `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/AcknowledgmentRestService.java`:
- Remove `notificationDao.get()` calls and notification ack handling
- Keep alarm ack handling intact

- [ ] **Step 6: Delete notification config managers**

```bash
rm -f opennms-config/src/main/java/org/opennms/netmgt/config/NotifdConfigManager.java
rm -f opennms-config/src/main/java/org/opennms/netmgt/config/NotificationManager.java
rm -f opennms-config/src/main/java/org/opennms/netmgt/config/DestinationPathManager.java
```

Search for and delete any factory/impl subclasses of these:
```bash
grep -rl "extends NotifdConfigManager\|extends NotificationManager\|extends DestinationPathManager" --include="*.java" .
```
Delete all found files.

- [ ] **Step 7: Remove notification config model classes**

Search `opennms-config-model/` for notification-related JAXB model classes:
```bash
find opennms-config-model -name "*Notif*" -o -name "*DestinationPath*" -o -name "*NotificationCommand*" | head -30
```
Delete all found model classes. These are JAXB-generated or hand-written models for the deleted config files.

- [ ] **Step 8: Fix compilation errors**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :opennms-dao,:opennms-webapp,:opennms-webapp-rest -am install`

Fix any remaining compilation errors from notification references. Common patterns:
- Remove `NotificationDao` / `UserNotificationDao` from `@Autowired` fields
- Remove notification-related imports
- Remove notification-related Spring XML wiring

- [ ] **Step 9: Commit**

```bash
git add -A
git commit -m "feat(delta-v): delete notification consumers, REST endpoints, config managers, and Vaadin UI"
```

---

### Task 8: Clean Up DatabasePopulator

**Files:**
- Modify: `opennms-dao/src/main/java/org/opennms/netmgt/dao/DatabasePopulator.java`

- [ ] **Step 1: Remove notification and event DAO fields and methods**

In `DatabasePopulator.java`:
- Remove fields (lines ~145-149): `EventDao m_eventDao`, `NotificationDao m_notificationDao`, `UserNotificationDao m_userNotificationDao`
- Remove corresponding setter methods
- Remove all event/notification creation and population methods (lines ~267-271, ~334-335, ~389-398)
- Remove imports for EventDao, NotificationDao, UserNotificationDao, OnmsEvent, OnmsNotification, OnmsUserNotification

- [ ] **Step 2: Compile check**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :opennms-dao -am install`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add opennms-dao/src/main/java/org/opennms/netmgt/dao/DatabasePopulator.java
git commit -m "refactor(dao): remove event and notification code from DatabasePopulator"
```

---

## Chunk 3: Phase 3 — OnmsEvent Entity Deletion Cascade

### Task 9: Delete OnmsEvent and Refactor OnmsEventParameter to POJO

**Files:**
- Delete: `opennms-model/src/main/java/org/opennms/netmgt/model/OnmsEvent.java`
- Modify: `opennms-model/src/main/java/org/opennms/netmgt/model/OnmsEventParameter.java` — refactor from JPA entity to plain POJO
- Delete: `opennms-model/src/main/java/org/opennms/netmgt/model/OnmsEventCollection.java`
- Delete: `opennms-model/src/test/java/org/opennms/netmgt/model/OnmsEventTest.java`

**CRITICAL**: `OnmsEventParameter` is used by `OnmsAlarm` (lines 809-822), `NorthboundAlarm`, `AlarmMapper`, `ProtobufMapper`, `ModelMappers`, `SearchProperties`, `CriteriaBehaviors`, and 10+ other production classes. It must be **refactored to a plain POJO**, NOT deleted.

- [ ] **Step 1: Delete OnmsEvent and OnmsEventCollection**

```bash
rm -f opennms-model/src/main/java/org/opennms/netmgt/model/OnmsEvent.java
rm -f opennms-model/src/main/java/org/opennms/netmgt/model/OnmsEventCollection.java
rm -f opennms-model/src/test/java/org/opennms/netmgt/model/OnmsEventTest.java
```

- [ ] **Step 2: Refactor OnmsEventParameter from JPA entity to plain POJO**

In `OnmsEventParameter.java`:
- Remove `@Entity`, `@Table(name="event_parameters")`, `@IdClass(OnmsEventParameter.OnmsEventParameterId.class)` class-level annotations
- Remove the inner `OnmsEventParameterId` composite key class entirely
- Remove the `@Id @ManyToOne private OnmsEvent event` field and its getter/setter
- Remove the `@Id` annotation from the `name` field (keep the field itself)
- Remove the constructor `OnmsEventParameter(OnmsEvent event, Parm parm)` and `OnmsEventParameter(OnmsEvent event, String name, String type, String value)`
- Add a simple constructor: `OnmsEventParameter(String name, String type, String value)`
- Keep fields: `name`, `value`, `type`, `position` and their getters/setters
- Remove all `import javax.persistence.*` and `OnmsEvent` imports
- Keep the class as `public class OnmsEventParameter implements Serializable`

- [ ] **Step 3: Fix OnmsEventParameter consumers**

After removing the `OnmsEvent` field, any code that creates `OnmsEventParameter` with an `OnmsEvent` argument needs updating. Search:
```bash
grep -rn "new OnmsEventParameter(" --include="*.java" . | grep -v test
```
Update constructors to use the new `(String name, String type, String value)` form.

- [ ] **Step 4: Commit**

```bash
git add opennms-model/src/main/java/org/opennms/netmgt/model/OnmsEvent.java
git add opennms-model/src/main/java/org/opennms/netmgt/model/OnmsEventParameter.java
git add opennms-model/src/main/java/org/opennms/netmgt/model/OnmsEventCollection.java
git add opennms-model/src/test/java/org/opennms/netmgt/model/OnmsEventTest.java
git commit -m "refactor(model): delete OnmsEvent, refactor OnmsEventParameter to plain POJO"
```

---

### Task 10: Delete EventDao, Drop event_parameters Table

**Files:**
- Delete: `opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/EventDao.java`
- Delete: `opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/EventDaoHibernate.java`
- Delete: `opennms-dao-mock/src/main/java/org/opennms/netmgt/dao/mock/MockEventDao.java`
- Delete: `opennms-dao/src/test/java/org/opennms/netmgt/dao/EventDaoIT.java`
- Modify: `opennms-dao/src/main/resources/META-INF/opennms/applicationContext-shared.xml` — remove eventDao bean (lines ~133-136)
- Modify: `opennms-dao-mock/src/main/resources/META-INF/opennms/applicationContext-mockDao.xml` — remove mock eventDao bean (lines ~80-81)
- Modify: `core/schema/src/main/liquibase/36.0.0/changelog.xml` — add event_parameters drop

- [ ] **Step 1: Add Liquibase changeset to drop event_parameters table**

Append to `core/schema/src/main/liquibase/36.0.0/changelog.xml` (before `</databaseChangeLog>`):

```xml
    <changeSet id="36.0.0-drop-event-parameters-table" author="delta-v">
        <comment>Drop orphaned event_parameters table — parent events table was dropped but cascadeConstraints only removed FKs, not child tables</comment>
        <preConditions onFail="MARK_RAN">
            <tableExists tableName="event_parameters"/>
        </preConditions>
        <dropTable tableName="event_parameters" cascadeConstraints="true"/>
    </changeSet>
```

- [ ] **Step 2: Delete EventDao files**

```bash
rm -f opennms-dao-api/src/main/java/org/opennms/netmgt/dao/api/EventDao.java
rm -f opennms-dao/src/main/java/org/opennms/netmgt/dao/hibernate/EventDaoHibernate.java
rm -f opennms-dao-mock/src/main/java/org/opennms/netmgt/dao/mock/MockEventDao.java
rm -f opennms-dao/src/test/java/org/opennms/netmgt/dao/EventDaoIT.java
```

- [ ] **Step 3: Remove eventDao from applicationContext-shared.xml**

In `applicationContext-shared.xml`, remove:
```xml
<bean id="eventDao" class="org.opennms.netmgt.dao.hibernate.EventDaoHibernate">
    <property name="sessionFactory" ref="sessionFactory" />
</bean>
<onmsgi:service interface="org.opennms.netmgt.dao.api.EventDao" ref="eventDao" />
```

- [ ] **Step 4: Remove eventDao from applicationContext-mockDao.xml**

In `applicationContext-mockDao.xml`, remove:
```xml
<bean id="eventDao" class="org.opennms.netmgt.dao.mock.MockEventDao" />
<onmsgi:service interface="org.opennms.netmgt.dao.api.EventDao" ref="eventDao" />
```

- [ ] **Step 5: Commit**

```bash
git add core/schema/src/main/liquibase/36.0.0/changelog.xml
git add opennms-dao-api/ opennms-dao/ opennms-dao-mock/
git commit -m "refactor(dao): drop event_parameters table, delete EventDao, and remove Spring bean wiring"
```

---

### Task 11: Delete Dead Event Production Code

**Files:**
- Delete: `opennms-webapp/src/main/java/org/opennms/web/event/DaoWebEventRepository.java`
- Delete: `opennms-webapp/src/test/java/org/opennms/web/event/DaoWebEventRepositoryIT.java`
- Delete: `opennms-webapp/src/test/resources/daoWebRepositoryTestContext.xml`
- Delete: `opennms-webapp/src/test/java/org/opennms/web/event/EventControllerTest.java`
- Delete: `features/events/shell-commands/src/main/java/org/opennms/netmgt/events/commands/EventCommand.java`
- Delete: `features/events/shell-commands/src/main/java/org/opennms/netmgt/events/commands/EventListCommand.java`
- Delete: `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/api/EventRestApi.java`

- [ ] **Step 1: Delete webapp event code**

```bash
rm -f opennms-webapp/src/main/java/org/opennms/web/event/DaoWebEventRepository.java
rm -f opennms-webapp/src/test/java/org/opennms/web/event/DaoWebEventRepositoryIT.java
rm -f opennms-webapp/src/test/resources/daoWebRepositoryTestContext.xml
rm -f opennms-webapp/src/test/java/org/opennms/web/event/EventControllerTest.java
```

- [ ] **Step 2: Delete Karaf shell commands**

```bash
rm -f features/events/shell-commands/src/main/java/org/opennms/netmgt/events/commands/EventCommand.java
rm -f features/events/shell-commands/src/main/java/org/opennms/netmgt/events/commands/EventListCommand.java
```

- [ ] **Step 3: Delete orphaned REST interface**

```bash
rm -f opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/api/EventRestApi.java
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: delete dead event code (DaoWebEventRepository, Karaf commands, EventRestApi)"
```

---

### Task 12: Refactor Remaining EventDao Consumers

**Files:**
- Modify: `features/perspectivepoller/src/main/java/org/opennms/netmgt/perspectivepoller/PerspectivePollerd.java`
- Modify: `features/perspectivepoller/src/main/resources/META-INF/opennms/applicationContext-perspectivePollerDaemon.xml`
- Modify: `features/system-report/src/main/java/org/opennms/systemreport/opennms/OpenNMSReportPlugin.java`
- Modify: `features/datachoices/src/main/java/org/opennms/features/datachoices/internal/usagestatistics/UsageStatisticsReporter.java`
- Modify: `core/daemon-loader-pollerd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-pollerd.xml`

- [ ] **Step 1: Refactor PerspectivePollerd**

In `PerspectivePollerd.java`:
- Remove the `private final EventDao eventDao` field and constructor parameter
- At lines ~450 and ~473, replace `eventDao.get(e.getDbid())` with direct use of the event `e` already available in scope. The event handler receives `Event e` (the XML event object) which already has all needed fields (UEI, source, severity, parameters). Remove the `OnmsEvent` local variable and use `e` directly for whatever data was being extracted from `OnmsEvent`.
- Remove `import org.opennms.netmgt.dao.api.EventDao;` and `import org.opennms.netmgt.model.OnmsEvent;`

- [ ] **Step 2: Remove EventDao from PerspectivePoller Spring XML**

In `applicationContext-perspectivePollerDaemon.xml`, remove:
```xml
<onmsgi:reference id="eventDao" interface="org.opennms.netmgt.dao.api.EventDao"/>
```

- [ ] **Step 3: Remove EventDao from Pollerd daemon-loader Spring XML**

In `core/daemon-loader-pollerd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-pollerd.xml`, remove:
```xml
<onmsgi:reference id="eventDao" interface="org.opennms.netmgt.dao.api.EventDao"/>
```

- [ ] **Step 4: Refactor OpenNMSReportPlugin**

In `OpenNMSReportPlugin.java`:
- Remove `@Autowired public EventDao m_eventDao;`
- Remove line ~105: `map.put("Number of Events", getResource(Integer.toString(m_eventDao.countAll())));`
- Replace with: `map.put("Number of Events", getResource("N/A (events in Kafka)"));`
- Remove EventDao import

- [ ] **Step 5: Refactor UsageStatisticsReporter**

In `UsageStatisticsReporter.java`:
- Remove field `private EventDao m_eventDao;` (line ~143)
- Remove setter `setEventDao()` (line ~600)
- Remove lines ~283-284:
  ```java
  usageStatisticsReport.setEvents(m_eventDao.countAll());
  usageStatisticsReport.setEventsLastHours(m_eventDao.getNumEventsLastHours(DEFAULT_EVENTS_LAST_HOURS));
  ```
- Replace with:
  ```java
  usageStatisticsReport.setEvents(0L);
  usageStatisticsReport.setEventsLastHours(0L);
  ```
- Remove EventDao import

- [ ] **Step 6: Compile check**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :opennms-services,:org.opennms.features.perspectivepoller,:org.opennms.features.datachoices,:org.opennms.features.system-report -am install`
Expected: BUILD SUCCESS (or identify remaining compile errors)

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: remove EventDao from PerspectivePollerd, OpenNMSReportPlugin, UsageStatisticsReporter"
```

---

### Task 13: Fix Remaining Compilation Errors

This is a sweep task — after the previous deletions, there will be compilation errors from transitive references. These cannot be fully enumerated in advance.

**Files:** Various — discovered by compilation

- [ ] **Step 1: Full project compile to find errors**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests 2>&1 | grep "error:" | head -50`

- [ ] **Step 2: Fix each error**

Common patterns:
- Remove `import org.opennms.netmgt.model.OnmsEvent;` from files that no longer use it
- Remove `import org.opennms.netmgt.model.OnmsNotification;` and `OnmsUserNotification` imports
- Remove `import org.opennms.netmgt.dao.api.EventDao;` / `NotificationDao` / `UserNotificationDao`
- Remove `@Autowired EventDao` fields from any remaining classes
- Remove `onmsEvent` references from Hibernate mapping files
- Update test classes that create OnmsEvent objects for test fixtures
- Fix `OnmsAlarm` if it still has any `OnmsEvent` reference (it was already denormalized, but verify). Clean up line ~228 Javadoc `@param event` that references `OnmsEvent`

**CRITICAL**: Do NOT delete or modify any `EventConf*` classes. If a compile error references `EventConfEventDao`, it means something else is wrong.

- [ ] **Step 3: Iterate until clean compile**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "fix: resolve compilation errors from OnmsEvent and notification entity deletion"
```

---

## Chunk 4: Phase 4 — Minion REST Elimination

### Task 14: Build SNMPv3 Twin Publisher

**Files:**
- Create: `core/snmp/api/src/main/java/org/opennms/netmgt/snmp/SnmpV3UserConfig.java` (or appropriate module)
- Create: Publisher class on core/webapp side
- Create: Subscriber class on Minion side

Reference the proven pattern in `features/events/traps/src/main/java/org/opennms/netmgt/trapd/TrapListener.java` (lines 73-76, 144-151).

- [ ] **Step 1: Create SnmpV3UserConfig model**

Create a serializable config class with a Twin key constant. This wraps the SNMPv3 user credentials that Minion needs. Look at `SnmpPeerFactory` (`opennms-config/src/main/java/org/opennms/netmgt/config/SnmpPeerFactory.java`) to understand the SNMPv3 user data model (auth protocol, auth passphrase, priv protocol, priv passphrase, security name, security level).

```java
package org.opennms.netmgt.snmp;

import java.io.Serializable;
import java.util.List;

public class SnmpV3UserConfig implements Serializable {
    public static final String TWIN_KEY = "snmp-v3-users";

    private List<SnmpV3User> users;

    // Standard getters/setters

    public static class SnmpV3User implements Serializable {
        private String securityName;
        private String authProtocol;
        private String authPassPhrase;
        private String privProtocol;
        private String privPassPhrase;
        private String engineId;
        // Standard getters/setters
    }
}
```

- [ ] **Step 2: Create Twin publisher on core/webapp side**

Create a publisher that watches `SnmpPeerFactory` and publishes via `TwinPublisher`. Follow the pattern from `TrapListener.java`:

```java
@Autowired
private TwinPublisher twinPublisher;

public void init() {
    SnmpV3UserConfig config = buildConfigFromSnmpPeerFactory();
    twinPublisher.register(SnmpV3UserConfig.TWIN_KEY, SnmpV3UserConfig.class, config);
}
```

Wire this publisher in the appropriate Blueprint XML on the core/webapp side.

- [ ] **Step 3: Create Twin subscriber on Minion side**

Follow `TrapListener.java` lines 144-151:

```java
@Autowired
private TwinSubscriber twinSubscriber;
private Closeable twinSubscription;

public void subscribe() {
    twinSubscription = twinSubscriber.subscribe(
        SnmpV3UserConfig.TWIN_KEY,
        SnmpV3UserConfig.class,
        config -> applySnmpV3Users(config)
    );
}
```

Wire this subscriber in the Minion Blueprint XML.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat(snmp): add SNMPv3 user config Twin publisher/subscriber for Minion"
```

---

### Task 15: Remove Minion REST Dependency

**Files:**
- Delete: `features/distributed/core/api/src/main/java/org/opennms/distributed/core/api/RestClient.java`
- Delete: `features/distributed/core/impl/src/main/java/org/opennms/distributed/core/impl/HealthTrackingRestClient.java`
- Modify: `opennms-container/minion/container-fs/entrypoint.sh` — remove `OPENNMS_HTTP_*` handling
- Modify: `opennms-container/delta-v/docker-compose.yml` — remove `OPENNMS_HTTP_*` env vars from minion service

- [ ] **Step 1: Delete RestClient interface and impls**

```bash
rm -f features/distributed/core/api/src/main/java/org/opennms/distributed/core/api/RestClient.java
rm -f features/distributed/core/impl/src/main/java/org/opennms/distributed/core/impl/HealthTrackingRestClient.java
```

Search for any other RestClient implementations in features/distributed/:
```bash
grep -rl "implements RestClient" features/distributed/ --include="*.java"
```
Delete all found.

**DO NOT DELETE** `smoke-test/src/main/java/org/opennms/smoketest/utils/RestClient.java` — this is a different class used for smoke test HTTP requests.

**Note:** The 9 config resources in `opennms-webapp-rest/.../config/` (`TrapdConfigurationResource`, `SnmpConfigurationResource`, etc.) are intentionally NOT deleted here. They may still serve the webapp UI. A separate evaluation should determine which can be removed. See spec Section 4 table for details.

- [ ] **Step 2: Remove OPENNMS_HTTP from Minion entrypoint**

In `opennms-container/minion/container-fs/entrypoint.sh`:
- Remove line ~73: `${MINION_HOME}/bin/scvcli set opennms.http ${OPENNMS_HTTP_USER} ${OPENNMS_HTTP_PASS}`
- Remove the `setCredentials()` function (lines ~81-89)
- Remove any `OPENNMS_HTTP_*` environment variable handling

- [ ] **Step 3: Remove OPENNMS_HTTP env vars from docker-compose.yml**

In `opennms-container/delta-v/docker-compose.yml`, remove from the minion service:
```yaml
OPENNMS_HTTP_URL: http://webapp:8980/opennms
OPENNMS_HTTP_USER: admin
OPENNMS_HTTP_PASS: admin
```

- [ ] **Step 4: Fix compilation errors**

Search for any remaining references to the deleted `RestClient` interface:
```bash
grep -rl "import org.opennms.distributed.core.api.RestClient" --include="*.java" .
```
Fix or delete all found files.

- [ ] **Step 5: Compile check**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :org.opennms.features.distributed.core.api,:org.opennms.features.distributed.core.impl -am install`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat(delta-v): eliminate Minion REST dependency, remove OPENNMS_HTTP config"
```

---

### Task 16: Verify Minion End-to-End

This is a manual verification task using the Delta-V docker-compose environment.

- [ ] **Step 1: Rebuild affected images**

```bash
cd opennms-container/delta-v
./build.sh
```

- [ ] **Step 2: Start environment**

```bash
docker compose up -d
```

- [ ] **Step 3: Verify Minion health**

```bash
docker compose exec minion bin/client "opennms:health-check"
```
Expected: Kafka RPC, Sink, Twin all pass. No REST-related health checks should appear.

- [ ] **Step 4: Verify Trapd config via Twin**

Send a test trap through the Minion and verify end-to-end flow:
```bash
# From host, send SNMP trap to Minion port 1162
snmptrap -v2c -c public localhost:1162 '' .1.3.6.1.6.3.1.1.5.3
```
Check Kafka `opennms-fault-events` topic for the trap event.

- [ ] **Step 5: Document results**

Record verification results. If any check fails, investigate and fix before proceeding.

- [ ] **Step 6: Commit any fixes**

```bash
git add -A
git commit -m "fix: resolve issues found during Minion end-to-end verification"
```

---

## Chunk 5: Final Cleanup and Verification

### Task 17: Clean Up Remaining Tests

**Files:** Various test files referencing deleted entities/DAOs

- [ ] **Step 1: Find all test files still referencing deleted classes**

```bash
grep -rl "EventDao\|OnmsEvent\|OnmsNotification\|OnmsUserNotification\|NotificationDao\|UserNotificationDao" --include="*.java" */src/test/ | grep -v "EventConf"
```

- [ ] **Step 2: Fix each test file**

For each file found:
- If the test is testing deleted functionality → **delete the test file**
- If the test uses EventDao/OnmsEvent for setup fixtures → **remove event fixture code**, use denormalized fields instead
- If the test references NotificationDao → **remove notification setup code**

Key test files to handle:
- `AlarmDaoIT` — remove EventDao from setup, use denormalized alarm fields
- `OutageDaoIT` — remove EventDao from setup, use denormalized outage fields
- `AcknowledgmentDaoIT` — remove notification ack tests, keep alarm ack tests
- `MemoDaoIT` — remove event references from setup
- `NotificationDaoIT` — **delete entirely**
- `UserNotificationDaoIT` — **delete entirely**
- REST ITs — remove event/notification creation from test fixtures

- [ ] **Step 3: Run tests for affected modules**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :opennms-dao,:opennms-webapp-rest -am install && ./compile.pl --projects :opennms-dao verify`

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "test: update integration tests for EventDao and notification removal"
```

---

### Task 18: Full Build Verification

- [ ] **Step 1: Full compile**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 2: Run E2E test**

Run: `cd opennms-container/delta-v && ./test-e2e.sh`
Expected: All 11 tests pass (3 phases)

- [ ] **Step 3: Final commit if any fixes needed**

```bash
git add -A
git commit -m "fix: resolve final build and test issues"
```

---

### Task 19: Update Memory and Next-Session Prompt

**Files:**
- Modify: `/Users/david/.claude/projects/-Users-david-development-src-opennms-horizon/memory/MEMORY.md`
- Delete: `docs/plans/next-session-prompt.md`

- [ ] **Step 1: Update MEMORY.md**

Add to the memory file:
- EventDao, OnmsEvent, OnmsEventParameter, OnmsEventCollection deleted
- OnmsOutage denormalized (svc_lost_event_tsid/uei, svc_regained_event_tsid/uei)
- Notifd eliminated (daemon, entities, DAOs, config, tables)
- Minion REST eliminated (RestClient deleted, OPENNMS_HTTP removed)
- SNMPv3 Twin publisher added
- PerspectivePollerd migration scoped as follow-on (TSID node-id 7)

- [ ] **Step 2: Delete next-session-prompt.md**

```bash
rm -f docs/plans/next-session-prompt.md
git add -A
git commit -m "docs: update memory, remove completed next-session prompt"
```

---

## Out of Scope — Deferred to Separate Brainstorm

### PerspectivePollerd Delta-V Migration (Spec Section 5)

PerspectivePollerd was identified as a valuable daemon to migrate to the Delta-V architecture. The EventDao dependency is removed in Task 12 of this plan, but the full daemon-loader creation, Karaf feature, Sentinel assembly entry, and docker-compose service are **deferred to a separate brainstorm and implementation cycle**. TSID node-id 7 is reserved for PerspectivePollerd.
