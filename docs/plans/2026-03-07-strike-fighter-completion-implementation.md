# Strike Fighter Completion Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** Complete the Strike Fighter microservice architecture by deleting 4 dead daemons, removing defunct events table infrastructure, and extracting 8 daemons into standalone Karaf-only containers.

**Architecture:** Each extracted daemon gets a daemon-loader bundle (flat Spring context with OSGi service references), a Karaf feature definition, and a container overlay for the `opennms/daemon` Docker image. Events flow via Kafka (KafkaEventForwarder). Dead daemons and the events table are deleted entirely.

**Tech Stack:** Java 17, Kafka 3.6.2, Karaf 4.3.10, OSGi/Spring DM, Maven bundle packaging, Docker Compose.

**Design Doc:** `docs/plans/2026-03-07-strike-fighter-completion-design.md`

**Reference Implementations:** `core/daemon-loader-pollerd/`, `core/daemon-loader-alarmd/`, `core/daemon-loader-collectd/`

---

## Phase 1: Delete Dead Daemons and Event Infrastructure

### Task 1: Delete Vacuumd

**Files:**
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/vacuumd/` (entire directory)
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/vacuumd/jmx/` (if separate)
- Delete: `opennms-services/src/test/java/org/opennms/netmgt/vacuumd/` (entire directory)
- Delete: `opennms-config/src/main/java/org/opennms/netmgt/config/VacuumdConfigFactory.java`
- Delete: `opennms-config/src/test/java/org/opennms/netmgt/config/VacuumdConfigFactoryTest.java`
- Delete: `opennms-config-model/src/main/java/org/opennms/netmgt/config/vacuumd/` (entire directory)
- Delete: `opennms-config-model/src/test/java/org/opennms/netmgt/config/vacuumd/` (entire directory)
- Delete: `opennms-base-assembly/src/main/filtered/etc/vacuumd-configuration.xml`
- Delete: `opennms-base-assembly/src/main/filtered/etc/examples/event-proxy/vacuumd-configuration.xml`
- Modify: `opennms-config-model/src/main/resources/defaults/service-configuration.xml` — remove Vacuumd service entry

**Step 1: Delete all Vacuumd source files**

```bash
rm -rf opennms-services/src/main/java/org/opennms/netmgt/vacuumd
rm -rf opennms-services/src/test/java/org/opennms/netmgt/vacuumd
rm -f opennms-config/src/main/java/org/opennms/netmgt/config/VacuumdConfigFactory.java
rm -f opennms-config/src/test/java/org/opennms/netmgt/config/VacuumdConfigFactoryTest.java
rm -rf opennms-config-model/src/main/java/org/opennms/netmgt/config/vacuumd
rm -rf opennms-config-model/src/test/java/org/opennms/netmgt/config/vacuumd
rm -f opennms-base-assembly/src/main/filtered/etc/vacuumd-configuration.xml
rm -f opennms-base-assembly/src/main/filtered/etc/examples/event-proxy/vacuumd-configuration.xml
```

**Step 2: Remove Vacuumd from service-configuration.xml**

In `opennms-config-model/src/main/resources/defaults/service-configuration.xml`, remove the entire `<service>` block for `OpenNMS:Name=Vacuumd`.

**Step 3: Fix compilation errors**

Search for references to Vacuumd, VacuumdConfigFactory, or vacuumd classes across the codebase. Remove or update any imports, Spring context references, or test helpers that reference them.

```bash
# Find all references
rg -l "Vacuumd|VacuumdConfig" --type java --type xml
```

**Step 4: Verify compilation**

```bash
./compile.pl -DskipTests --projects :opennms-services,:opennms-config,:opennms-config-model -am install
```
Expected: BUILD SUCCESS

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: delete Vacuumd daemon entirely

Vacuumd's primary job was purging the events table, which is no longer
written to. Remaining alarm cleanup is handled by Drools rules."
```

---

### Task 2: Delete Statsd

**Files:**
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/statsd/` (entire directory)
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/statsd/jmx/` (if separate)
- Delete: `opennms-services/src/test/java/org/opennms/netmgt/statsd/` (entire directory)
- Delete: `opennms-services/src/main/resources/META-INF/opennms/applicationContext-statisticsDaemon.xml`
- Delete: `opennms-config-model/src/main/java/org/opennms/netmgt/config/statsd/` (entire directory)
- Delete: `opennms-config-model/src/test/java/org/opennms/netmgt/config/statsd/` (entire directory)
- Delete: `opennms-base-assembly/src/main/filtered/etc/statsd-configuration.xml`
- Modify: `opennms-config-model/src/main/resources/defaults/service-configuration.xml` — remove Statsd service entry

**Step 1: Delete all Statsd source files**

```bash
rm -rf opennms-services/src/main/java/org/opennms/netmgt/statsd
rm -rf opennms-services/src/test/java/org/opennms/netmgt/statsd
rm -f opennms-services/src/main/resources/META-INF/opennms/applicationContext-statisticsDaemon.xml
rm -rf opennms-config-model/src/main/java/org/opennms/netmgt/config/statsd
rm -rf opennms-config-model/src/test/java/org/opennms/netmgt/config/statsd
rm -f opennms-base-assembly/src/main/filtered/etc/statsd-configuration.xml
```

**Step 2: Remove Statsd from service-configuration.xml**

Remove the `<service>` block for `OpenNMS:Name=Statsd`.

**Step 3: Fix compilation errors**

```bash
rg -l "Statsd|StatsdConfig|statisticsDaemon|StatisticsDaemon" --type java --type xml
```

Remove references. Check `opennms-dao/src/main/resources/META-INF/opennms/applicationContext-dao.xml` for any `statsdConfigDao` bean.

**Step 4: Verify compilation**

```bash
./compile.pl -DskipTests --projects :opennms-services,:opennms-config-model -am install
```

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: delete Statsd daemon entirely — unused statistics reporting"
```

---

### Task 3: Delete Actiond

**Files:**
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/actiond/` (entire directory)
- Delete: `opennms-config/src/main/java/org/opennms/netmgt/config/ActiondConfigFactory.java`
- Delete: `opennms-config-model/src/main/java/org/opennms/netmgt/config/actiond/` (entire directory)
- Delete: `opennms-config-model/src/test/java/org/opennms/netmgt/config/actiond/` (entire directory)
- Delete: `opennms-base-assembly/src/main/filtered/etc/actiond-configuration.xml`
- Modify: `opennms-config-model/src/main/resources/defaults/service-configuration.xml` — remove Actiond service entry

**Step 1: Delete all Actiond source files**

```bash
rm -rf opennms-services/src/main/java/org/opennms/netmgt/actiond
rm -f opennms-config/src/main/java/org/opennms/netmgt/config/ActiondConfigFactory.java
rm -rf opennms-config-model/src/main/java/org/opennms/netmgt/config/actiond
rm -rf opennms-config-model/src/test/java/org/opennms/netmgt/config/actiond
rm -f opennms-base-assembly/src/main/filtered/etc/actiond-configuration.xml
```

**Step 2: Remove Actiond from service-configuration.xml**

Remove the `<service>` block for `OpenNMS:Name=Actiond`.

**Step 3: Fix compilation errors**

```bash
rg -l "Actiond|ActiondConfig" --type java --type xml
```

**Step 4: Verify compilation**

```bash
./compile.pl -DskipTests --projects :opennms-services,:opennms-config,:opennms-config-model -am install
```

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: delete Actiond daemon entirely — legacy shell-command execution"
```

---

### Task 4: Delete Ackd

**Files:**
- Delete: `opennms-ackd/` (entire module directory)
- Delete: `opennms-config-model/src/main/java/org/opennms/netmgt/config/ackd/` (entire directory)
- Delete: `opennms-config-model/src/test/java/org/opennms/netmgt/config/ackd/` (entire directory)
- Delete: `opennms-base-assembly/src/main/filtered/etc/ackd-configuration.xml`
- Modify: `pom.xml` (root) — remove `<module>opennms-ackd</module>` and its dependency entry
- Modify: `opennms-config-model/src/main/resources/defaults/service-configuration.xml` — remove Ackd service entry

**Step 1: Delete the entire opennms-ackd module**

```bash
rm -rf opennms-ackd
rm -rf opennms-config-model/src/main/java/org/opennms/netmgt/config/ackd
rm -rf opennms-config-model/src/test/java/org/opennms/netmgt/config/ackd
rm -f opennms-base-assembly/src/main/filtered/etc/ackd-configuration.xml
```

**Step 2: Remove module from root pom.xml**

Remove `<module>opennms-ackd</module>` and any `<dependency>` block referencing `opennms-ackd` from the root `pom.xml`.

**Step 3: Remove Ackd from service-configuration.xml**

Remove the `<service>` block for `OpenNMS:Name=Ackd`.

**Step 4: Fix compilation errors**

```bash
rg -l "opennms-ackd|ackd|Ackd" --type java --type xml --type pom
```

Remove references from any assembly descriptors, dependency BOMs, or test configurations.

**Step 5: Verify compilation**

```bash
./compile.pl -DskipTests --projects :opennms-config-model -am install
```

**Step 6: Commit**

```bash
git add -A
git commit -m "feat: delete Ackd module entirely — unused acknowledgment daemon"
```

---

### Task 5: Delete Dead Event Infrastructure

**Files:**
- Delete: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/HibernateEventWriter.java`
- Delete: `features/events/daemon/src/test/java/org/opennms/netmgt/eventd/processor/HibernateEventWriter*.java`
- Delete: `features/events/daemon/src/test/java/org/opennms/netmgt/eventd/nms16978/` (if exists)
- Delete: `features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/EventWriter.java` (if exists)
- Delete: `features/events/api/src/main/java/org/opennms/netmgt/events/api/EventWriter.java` (if exists)
- Delete: `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/EventRestService.java`
- Delete: `opennms-webapp-rest/src/test/java/org/opennms/web/rest/v1/EventRestServiceIT.java`
- Delete: `opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/EventRestService.java`
- Delete: `opennms-webapp-rest/src/test/java/org/opennms/web/rest/v2/EventRestServiceIT.java`
- Delete: `opennms-base-assembly/src/main/filtered/etc/report-templates/EventAnalysis.jrxml`
- Modify: `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml` — remove legacy HibernateEventWriter bean definitions

**Step 1: Delete HibernateEventWriter and EventWriter**

```bash
rm -f features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/HibernateEventWriter.java
rm -f features/events/daemon/src/main/java/org/opennms/netmgt/eventd/processor/EventWriter.java
rm -f features/events/api/src/main/java/org/opennms/netmgt/events/api/EventWriter.java
rm -rf features/events/daemon/src/test/java/org/opennms/netmgt/eventd/processor/HibernateEventWriter*
rm -rf features/events/daemon/src/test/java/org/opennms/netmgt/eventd/nms16978
```

**Step 2: Delete Event REST endpoints**

```bash
rm -f opennms-webapp-rest/src/main/java/org/opennms/web/rest/v1/EventRestService.java
rm -f opennms-webapp-rest/src/test/java/org/opennms/web/rest/v1/EventRestServiceIT.java
rm -f opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/EventRestService.java
rm -f opennms-webapp-rest/src/test/java/org/opennms/web/rest/v2/EventRestServiceIT.java
```

**Step 3: Delete EventAnalysis Jasper Report**

```bash
rm -f opennms-base-assembly/src/main/filtered/etc/report-templates/EventAnalysis.jrxml
```

**Step 4: Remove legacy bean definitions from applicationContext-eventDaemon.xml**

Read `features/events/daemon/src/main/resources/META-INF/opennms/applicationContext-eventDaemon.xml` and remove the `eventWriter` / `hibernateEventWriter` / `eventIpcBroadcastProcessor` legacy beans that are commented as "kept for backward compatibility".

**Step 5: Fix compilation errors**

```bash
rg -l "HibernateEventWriter|EventWriter|EventRestService" --type java --type xml
```

Remove imports and references. Note: `EventDao` has many consumers — do NOT delete EventDao in this task. It will require a separate careful analysis of all consumers (alarms, outages, etc. reference events by ID).

**Step 6: Verify compilation**

```bash
./compile.pl -DskipTests --projects :org.opennms.features.events.daemon,:opennms-webapp-rest -am install
```

**Step 7: Commit**

```bash
git add -A
git commit -m "feat: delete dead event infrastructure — HibernateEventWriter, Event REST endpoints, EventAnalysis report

Events no longer written to PostgreSQL. Event pipeline routes to Kafka
only. REST event endpoints and Jasper Reports referencing the events
table are removed."
```

---

### Task 6: Delete Event UI Pages

**Files:**
- Delete: `ui/src/containers/EventConfiguration.vue`
- Delete: `ui/src/containers/EventConfigurationDetail.vue`
- Delete: `ui/src/containers/EventConfigEventCreate.vue`
- Delete: `ui/src/components/EventConfiguration/` (entire directory)
- Delete: `ui/src/components/EventConfigurationDetail/` (entire directory)
- Delete: `ui/src/components/EventConfigEventCreate/` (entire directory)
- Delete: `ui/src/components/Nodes/EventsTable.vue`
- Delete: `ui/src/stores/eventStore.ts`
- Delete: `ui/src/stores/eventConfigDetailStore.ts`
- Delete: `ui/src/stores/eventConfigStore.ts`
- Delete: `ui/src/stores/eventModificationStore.ts`
- Delete: `ui/src/services/eventService.ts`
- Delete: `ui/src/services/eventConfigService.ts`
- Delete: `ui/src/types/eventConfig.d.ts`
- Delete: `ui/src/mappers/eventConfig.mapper.ts`
- Modify: `ui/src/router/index.ts` — remove event-related routes
- Delete: `opennms-webapp/src/main/webapp/includes/event-advquerypanel.jsp`
- Delete: `opennms-webapp/src/main/webapp/includes/eventlist.jsp`
- Delete: `opennms-webapp/src/main/webapp/includes/event-querypanel.jsp`

**Step 1: Delete Vue event components, stores, and services**

```bash
rm -rf ui/src/containers/EventConfiguration.vue
rm -rf ui/src/containers/EventConfigurationDetail.vue
rm -rf ui/src/containers/EventConfigEventCreate.vue
rm -rf ui/src/components/EventConfiguration
rm -rf ui/src/components/EventConfigurationDetail
rm -rf ui/src/components/EventConfigEventCreate
rm -f ui/src/components/Nodes/EventsTable.vue
rm -f ui/src/stores/eventStore.ts
rm -f ui/src/stores/eventConfigDetailStore.ts
rm -f ui/src/stores/eventConfigStore.ts
rm -f ui/src/stores/eventModificationStore.ts
rm -f ui/src/services/eventService.ts
rm -f ui/src/services/eventConfigService.ts
rm -f ui/src/types/eventConfig.d.ts
rm -f ui/src/mappers/eventConfig.mapper.ts
```

**Step 2: Remove event routes from Vue Router**

Read `ui/src/router/index.ts` and remove routes for `/event-config`, `/event-config/:id`, `/event-config/create`, and any other event-related routes.

**Step 3: Remove event references from other Vue components**

Search `ui/src/` for imports of deleted stores/services/components. Update or remove references (e.g., NodeActionsDropdown may link to event pages).

**Step 4: Delete event JSP includes**

```bash
rm -f opennms-webapp/src/main/webapp/includes/event-advquerypanel.jsp
rm -f opennms-webapp/src/main/webapp/includes/eventlist.jsp
rm -f opennms-webapp/src/main/webapp/includes/event-querypanel.jsp
```

**Step 5: Verify Vue build**

```bash
cd ui && pnpm install && pnpm build && pnpm test
```

**Step 6: Commit**

```bash
git add -A
git commit -m "feat: delete event UI pages — Vue components, stores, JSP includes

Events are no longer queryable from PostgreSQL. Event configuration
pages (eventconf editor) are kept as they configure event definitions,
not query stored events."
```

---

## Phase 2: Extract EASY Daemons

### Task 7: Create daemon-loader-rtcd

RTCManager is a simple timer-based daemon that calculates real-time availability. No event subscriptions. Dependencies: DataSender, RTCConfigFactory.

**Files:**
- Create: `core/daemon-loader-rtcd/pom.xml`
- Create: `core/daemon-loader-rtcd/src/main/java/org/opennms/core/daemon/loader/DaemonLifecycleManager.java`
- Create: `core/daemon-loader-rtcd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-rtcd.xml`
- Modify: `core/pom.xml` — add `<module>daemon-loader-rtcd</module>`

**Step 1: Create the POM**

Model after `core/daemon-loader-pollerd/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.opennms</groupId>
        <artifactId>org.opennms.core</artifactId>
        <version>36.0.0-SNAPSHOT</version>
    </parent>

    <groupId>org.opennms.core</groupId>
    <artifactId>org.opennms.core.daemon-loader-rtcd</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Core :: Daemon Loader :: Rtcd</name>
    <description>
        Karaf-only Rtcd loader. Creates and starts the RTCManager daemon in a
        Karaf container without the Manager/Eventd.
    </description>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <extensions>true</extensions>
                <configuration>
                    <instructions>
                        <Bundle-RequiredExecutionEnvironment>JavaSE-17</Bundle-RequiredExecutionEnvironment>
                        <Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
                        <Bundle-Version>${project.version}</Bundle-Version>
                        <DynamicImport-Package>*</DynamicImport-Package>
                        <Spring-Context>
                            META-INF/opennms/*.xml;publish-context:=false;create-asynchronously:=false
                        </Spring-Context>
                    </instructions>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-services</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms.features.events</groupId>
            <artifactId>org.opennms.features.events.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-dao-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-config</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.servicemix.bundles</groupId>
            <artifactId>org.apache.servicemix.bundles.spring-beans</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Step 2: Copy DaemonLifecycleManager**

Copy `core/daemon-loader-pollerd/src/main/java/org/opennms/core/daemon/loader/DaemonLifecycleManager.java` to `core/daemon-loader-rtcd/src/main/java/org/opennms/core/daemon/loader/DaemonLifecycleManager.java` (identical file).

**Step 3: Create the Spring context**

Read `opennms-services/src/main/resources/META-INF/opennms/applicationContext-rtc.xml` to understand the beans needed (RTCManager, DataSender, AvailabilityService, RTCConfigFactory).

Create `core/daemon-loader-rtcd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-rtcd.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:onmsgi="http://xmlns.opennms.org/xsd/spring/onms-osgi"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.2.xsd
           http://xmlns.opennms.org/xsd/spring/onms-osgi http://xmlns.opennms.org/xsd/spring/onms-osgi.xsd
       ">

    <context:annotation-config />

    <!-- OSGi Service References -->
    <onmsgi:reference id="eventIpcManager"
                      interface="org.opennms.netmgt.events.api.EventIpcManager"/>
    <onmsgi:reference id="monitoredServiceDao"
                      interface="org.opennms.netmgt.dao.api.MonitoredServiceDao"/>
    <onmsgi:reference id="outageDao"
                      interface="org.opennms.netmgt.dao.api.OutageDao"/>
    <onmsgi:reference id="transactionTemplate"
                      interface="org.springframework.transaction.support.TransactionOperations"/>

    <!-- RTC Config Factory -->
    <bean id="rtcConfigFactory" class="org.opennms.netmgt.config.RTCConfigFactory"
          factory-method="getInstance"/>

    <!-- Availability Service (Hibernate-backed) -->
    <bean id="availabilityService"
          class="org.opennms.netmgt.rtc.AvailabilityServiceHibernateImpl"/>

    <!-- Data Sender (sends RTC data to web UI via HTTP POST) -->
    <bean id="dataSender" class="org.opennms.netmgt.rtc.DataSender">
        <constructor-arg ref="availabilityService"/>
    </bean>

    <!-- RTCManager daemon -->
    <bean name="daemon" class="org.opennms.netmgt.rtc.RTCManager"/>

    <!-- Lifecycle manager (replaces Manager) -->
    <bean name="daemonLifecycleManager"
          class="org.opennms.core.daemon.loader.DaemonLifecycleManager">
        <constructor-arg ref="daemon"/>
    </bean>
</beans>
```

**Step 4: Add module to core/pom.xml**

Add `<module>daemon-loader-rtcd</module>` to the `<modules>` section.

**Step 5: Verify compilation**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-rtcd -am install
```

**Step 6: Commit**

```bash
git add core/daemon-loader-rtcd/ core/pom.xml
git commit -m "feat: create daemon-loader-rtcd for standalone Rtcd container"
```

---

### Task 8: Create daemon-loader-passivestatusd

PassiveStatusKeeper listens for passive status events and tracks service states. Uses singleton pattern and EventIpcManager.

**Files:**
- Create: `core/daemon-loader-passivestatusd/pom.xml`
- Create: `core/daemon-loader-passivestatusd/src/main/java/org/opennms/core/daemon/loader/DaemonLifecycleManager.java`
- Create: `core/daemon-loader-passivestatusd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-passivestatusd.xml`
- Modify: `core/pom.xml` — add `<module>daemon-loader-passivestatusd</module>`

**Step 1: Create the POM**

Same structure as Task 7 POM but with `artifactId` = `org.opennms.core.daemon-loader-passivestatusd`. Dependencies: `core.daemon`, `opennms-services`, `events.api`, `opennms-dao-api`.

**Step 2: Copy DaemonLifecycleManager**

Same as Task 7.

**Step 3: Create the Spring context**

Read the PassiveStatusKeeper class to understand dependencies (EventIpcManager, DataSource). Create `applicationContext-daemon-loader-passivestatusd.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:onmsgi="http://xmlns.opennms.org/xsd/spring/onms-osgi"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
           http://xmlns.opennms.org/xsd/spring/onms-osgi http://xmlns.opennms.org/xsd/spring/onms-osgi.xsd
       ">

    <!-- OSGi Service References -->
    <onmsgi:reference id="eventIpcManager"
                      interface="org.opennms.netmgt.events.api.EventIpcManager"/>
    <onmsgi:reference id="dataSource"
                      interface="javax.sql.DataSource"/>

    <!-- PassiveStatusKeeper daemon (singleton pattern — set instance) -->
    <bean name="daemon" class="org.opennms.netmgt.passive.PassiveStatusKeeper">
        <property name="eventManager" ref="eventIpcManager"/>
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!-- Lifecycle manager -->
    <bean name="daemonLifecycleManager"
          class="org.opennms.core.daemon.loader.DaemonLifecycleManager">
        <constructor-arg ref="daemon"/>
    </bean>
</beans>
```

**Step 4: Add module to core/pom.xml**

**Step 5: Verify compilation**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-passivestatusd -am install
```

**Step 6: Commit**

```bash
git add core/daemon-loader-passivestatusd/ core/pom.xml
git commit -m "feat: create daemon-loader-passivestatusd for standalone container"
```

---

### Task 9: Wire Phase 2 Karaf Features and Container Overlays

**Files:**
- Modify: `container/features/src/main/resources/features.xml` — add opennms-daemon-rtcd and opennms-daemon-passivestatusd features
- Create: `opennms-container/strike-fighter/rtcd-overlay/etc/featuresBoot.d/rtcd.boot`
- Create: `opennms-container/strike-fighter/rtcd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- Create: `opennms-container/strike-fighter/passivestatusd-overlay/etc/featuresBoot.d/passivestatusd.boot`
- Create: `opennms-container/strike-fighter/passivestatusd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`

**Step 1: Add Karaf feature definitions**

In `container/features/src/main/resources/features.xml`, after the `opennms-daemon-collectd` feature, add:

```xml
    <!-- Karaf-only Rtcd: loads RTCManager daemon without Manager/Eventd -->
    <feature name="opennms-daemon-rtcd" version="${project.version}"
             description="OpenNMS :: Daemon Loader :: Rtcd">
        <feature>opennms-spring-extender</feature>
        <feature>opennms-event-forwarder-kafka</feature>
        <feature>opennms-distributed-core-impl</feature>
        <feature>opennms-persistence</feature>
        <feature>opennms-config</feature>
        <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
        <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-rtcd/${project.version}</bundle>
    </feature>

    <!-- Karaf-only PassiveStatusd: loads PassiveStatusKeeper daemon -->
    <feature name="opennms-daemon-passivestatusd" version="${project.version}"
             description="OpenNMS :: Daemon Loader :: PassiveStatusd">
        <feature>opennms-spring-extender</feature>
        <feature>opennms-event-forwarder-kafka</feature>
        <feature>opennms-distributed-core-impl</feature>
        <feature>opennms-persistence</feature>
        <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
        <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-passivestatusd/${project.version}</bundle>
    </feature>
```

**Step 2: Create container overlays**

For each daemon, create overlay directories with featuresBoot and Kafka config:

`opennms-container/strike-fighter/rtcd-overlay/etc/featuresBoot.d/rtcd.boot`:
```
opennms-daemon-rtcd
opennms-health-rest-service
```

`opennms-container/strike-fighter/rtcd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`:
```properties
bootstrap.servers=kafka:9092
topic.name=opennms-fault-events
consumer.group.id=opennms-rtcd
poll.timeout.ms=100
```

`opennms-container/strike-fighter/passivestatusd-overlay/etc/featuresBoot.d/passivestatusd.boot`:
```
opennms-daemon-passivestatusd
opennms-health-rest-service
```

`opennms-container/strike-fighter/passivestatusd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`:
```properties
bootstrap.servers=kafka:9092
topic.name=opennms-fault-events
consumer.group.id=opennms-passivestatusd
poll.timeout.ms=100
```

**Step 3: Commit**

```bash
git add container/features/ opennms-container/strike-fighter/rtcd-overlay/ opennms-container/strike-fighter/passivestatusd-overlay/
git commit -m "feat: add Karaf features and container overlays for Rtcd and PassiveStatusd"
```

---

## Phase 3: Extract MEDIUM Daemons

Each task below follows the identical pattern: create daemon-loader module, add Karaf feature, create overlay. Only the Spring context XML and POM dependencies differ per daemon.

### Task 10: Create daemon-loader-notifd

Notifd is event-driven (via BroadcastEventProcessor), uses 6 config factories, and manages notification queues.

**Files:**
- Create: `core/daemon-loader-notifd/pom.xml`
- Create: `core/daemon-loader-notifd/src/main/java/org/opennms/core/daemon/loader/DaemonLifecycleManager.java`
- Create: `core/daemon-loader-notifd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-notifd.xml`
- Modify: `core/pom.xml` — add module
- Modify: `container/features/src/main/resources/features.xml` — add feature
- Create: `opennms-container/strike-fighter/notifd-overlay/` — featuresBoot + Kafka cfg

**Step 1: Create POM** — same pattern. Additional deps: `opennms-config` (NotifdConfigFactory, GroupFactory, UserFactory, etc.).

**Step 2: Copy DaemonLifecycleManager**

**Step 3: Create Spring context**

Read `opennms-services/src/main/resources/META-INF/opennms/applicationContext-notifd.xml` for the full bean graph. The key challenge is the 6 static config factories that require `init()`/`getInstance()` initialization. Create `applicationContext-daemon-loader-notifd.xml` that instantiates each factory via `factory-method="getInstance"` after calling static init.

Key beans: Notifd, BroadcastEventProcessor, NotifdConfigManager, NotificationManager, NodeDao, GroupFactory, UserFactory, DestinationPathFactory, NotificationCommandFactory.

**Step 4: Add Karaf feature**

```xml
<feature name="opennms-daemon-notifd" version="${project.version}"
         description="OpenNMS :: Daemon Loader :: Notifd">
    <feature>opennms-spring-extender</feature>
    <feature>opennms-event-forwarder-kafka</feature>
    <feature>opennms-distributed-core-impl</feature>
    <feature>opennms-persistence</feature>
    <feature>opennms-config</feature>
    <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-notifd/${project.version}</bundle>
</feature>
```

**Step 5: Create overlay** — featuresBoot: `opennms-daemon-notifd`, consumer group: `opennms-notifd`

**Step 6: Verify and commit**

```bash
./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-notifd -am install
git add core/daemon-loader-notifd/ core/pom.xml container/features/ opennms-container/strike-fighter/notifd-overlay/
git commit -m "feat: create daemon-loader-notifd for standalone Notifd container"
```

---

### Task 11: Create daemon-loader-discovery

Discovery is timer-based with MessageBus integration. Dependencies: DiscoveryConfigFactory, DiscoveryTaskExecutor, EventForwarder, MessageBus.

**Files:** Same pattern as Task 10 but for Discovery.

**Step 1-2: Create POM and DaemonLifecycleManager**

Additional deps: `features/discovery/` module.

**Step 3: Create Spring context**

Read `features/discovery/src/main/resources/META-INF/opennms/applicationContext-discovery.xml`. Key beans: Discovery, DiscoveryConfigFactory, DiscoveryTaskExecutor. Discovery uses `@Autowired` for EventForwarder and MessageBus.

**Step 4: Add Karaf feature**

```xml
<feature name="opennms-daemon-discovery" version="${project.version}"
         description="OpenNMS :: Daemon Loader :: Discovery">
    <feature>opennms-spring-extender</feature>
    <feature>opennms-event-forwarder-kafka</feature>
    <feature>opennms-distributed-core-impl</feature>
    <feature>opennms-persistence</feature>
    <feature>opennms-config</feature>
    <bundle>mvn:org.opennms.features/org.opennms.features.discovery/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-discovery/${project.version}</bundle>
</feature>
```

**Step 5: Create overlay** — consumer group: `opennms-discovery`

**Step 6: Verify and commit**

```bash
git commit -m "feat: create daemon-loader-discovery for standalone Discovery container"
```

---

### Task 12: Create daemon-loader-trapd

Trapd is a UDP listener (port 1162) with SNMP trap processing. Uses @EventListener annotations, TrapListener, SecureCredentialsVault, TwinPublisher.

**Files:** Same pattern. Additional: UDP port exposure in compose.

**Step 1-2: Create POM and DaemonLifecycleManager**

Additional deps: `features/events/traps/` module, SNMP bundles.

**Step 3: Create Spring context**

Read `features/events/traps/src/main/resources/META-INF/opennms/applicationContext-trapDaemon.xml`. Key beans: Trapd, TrapListener, TrapSinkModule, TrapdConfigFactory. Note: SecureCredentialsVault and TwinPublisher may need to be optional OSGi references.

**Step 4: Add Karaf feature**

```xml
<feature name="opennms-daemon-trapd" version="${project.version}"
         description="OpenNMS :: Daemon Loader :: Trapd">
    <feature>opennms-spring-extender</feature>
    <feature>opennms-event-forwarder-kafka</feature>
    <feature>opennms-distributed-core-impl</feature>
    <feature>opennms-persistence</feature>
    <feature>opennms-config</feature>
    <bundle>mvn:org.opennms.features.events/org.opennms.features.events.traps/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-trapd/${project.version}</bundle>
</feature>
```

**Step 5: Create overlay** — consumer group: `opennms-trapd`. Note: compose service needs `ports: ["1162:1162/udp"]`.

**Step 6: Verify and commit**

```bash
git commit -m "feat: create daemon-loader-trapd for standalone Trapd container (UDP :1162)"
```

---

### Task 13: Create daemon-loader-syslogd

Syslogd is a UDP listener (port 10514) with syslog message parsing. Uses MessageBus for reload, SyslogReceiver.

**Files:** Same pattern. Additional: UDP port exposure in compose.

**Step 1-2: Create POM and DaemonLifecycleManager**

Additional deps: `features/events/syslog/` module.

**Step 3: Create Spring context**

Read `features/events/syslog/src/main/resources/META-INF/opennms/applicationContext-syslogDaemon.xml`. Key beans: Syslogd, SyslogReceiverJavaNetImpl (or appropriate receiver), SyslogdConfigFactory.

**Step 4: Add Karaf feature**

```xml
<feature name="opennms-daemon-syslogd" version="${project.version}"
         description="OpenNMS :: Daemon Loader :: Syslogd">
    <feature>opennms-spring-extender</feature>
    <feature>opennms-event-forwarder-kafka</feature>
    <feature>opennms-distributed-core-impl</feature>
    <feature>opennms-persistence</feature>
    <feature>opennms-config</feature>
    <bundle>mvn:org.opennms.features.events/org.opennms.features.events.syslog/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-syslogd/${project.version}</bundle>
</feature>
```

**Step 5: Create overlay** — consumer group: `opennms-syslogd`. Compose: `ports: ["10514:10514/udp"]`.

**Step 6: Verify and commit**

```bash
git commit -m "feat: create daemon-loader-syslogd for standalone Syslogd container (UDP :10514)"
```

---

### Task 14: Create daemon-loader-ticketer

TroubleTicketer implements SpringServiceDaemon (not AbstractServiceDaemon). Uses EventListener interface for 4 ticket UEIs and MessageBus for reload.

**Files:** Same pattern.

**Step 1-2: Create POM and DaemonLifecycleManager**

Note: TroubleTicketer implements `SpringServiceDaemon`, not `AbstractServiceDaemon`. DaemonLifecycleManager needs to handle this — either modify it to accept `SpringServiceDaemon`, or create a thin adapter. Check if `SpringServiceDaemon` has `init()`/`start()`/`stop()` methods. If it uses `afterPropertiesSet()`/`start()`/`destroy()` from Spring, the lifecycle manager may need adjustment.

Additional deps: `features/ticketing/daemon/`, `features/ticketing/api/`.

**Step 3: Create Spring context**

Read `features/ticketing/daemon/src/main/resources/META-INF/opennms/applicationContext-troubleTicketer.xml`. Key beans: TroubleTicketer, TicketerServiceLayer, EventIpcManager. The Spring context handles lifecycle via `InitializingBean`.

**Step 4: Add Karaf feature**

```xml
<feature name="opennms-daemon-ticketer" version="${project.version}"
         description="OpenNMS :: Daemon Loader :: TroubleTicketer">
    <feature>opennms-spring-extender</feature>
    <feature>opennms-event-forwarder-kafka</feature>
    <feature>opennms-distributed-core-impl</feature>
    <feature>opennms-persistence</feature>
    <feature>opennms-config</feature>
    <bundle>mvn:org.opennms.features.ticketing/org.opennms.features.ticketing.daemon/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-ticketer/${project.version}</bundle>
</feature>
```

**Step 5: Create overlay** — consumer group: `opennms-ticketer`

**Step 6: Verify and commit**

```bash
git commit -m "feat: create daemon-loader-ticketer for standalone TroubleTicketer container"
```

---

### Task 15: Create daemon-loader-eventtranslator

EventTranslator uses singleton pattern, EventListener with dynamic UEI list, and MessageBus for reload. Translates events based on configuration rules.

**Files:** Same pattern.

**Step 1-2: Create POM and DaemonLifecycleManager**

**Step 3: Create Spring context**

EventTranslator uses singleton pattern (`EventTranslator.getInstance()`). The loader context needs to:
1. Create an EventTranslator instance
2. Set the singleton instance (`EventTranslator.setInstance()`)
3. Set EventIpcManager, EventTranslatorConfig, DataSource, MessageBus via property setters

Key beans: EventTranslator, EventTranslatorConfig (from `opennms-config`), DataSource (from OSGi).

**Step 4: Add Karaf feature**

```xml
<feature name="opennms-daemon-eventtranslator" version="${project.version}"
         description="OpenNMS :: Daemon Loader :: EventTranslator">
    <feature>opennms-spring-extender</feature>
    <feature>opennms-event-forwarder-kafka</feature>
    <feature>opennms-distributed-core-impl</feature>
    <feature>opennms-persistence</feature>
    <feature>opennms-config</feature>
    <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-eventtranslator/${project.version}</bundle>
</feature>
```

**Step 5: Create overlay** — consumer group: `opennms-eventtranslator`

**Step 6: Verify and commit**

```bash
git commit -m "feat: create daemon-loader-eventtranslator for standalone EventTranslator container"
```

---

## Phase 4: Integration and Cleanup

### Task 16: Update Docker Compose with All 15 Services

**Files:**
- Modify: `opennms-container/strike-fighter/docker-compose.yml`

**Step 1: Add compose service entries for each new daemon**

For each of the 8 new daemons, add a service entry following the Pollerd/Collectd pattern:

```yaml
  rtcd:
    image: opennms/daemon:${VERSION}
    container_name: strike-fighter-rtcd
    hostname: rtcd
    depends_on:
      core:
        condition: service_healthy
    environment:
      POSTGRES_HOST: postgres
      POSTGRES_PORT: "5432"
      POSTGRES_USER: opennms
      POSTGRES_PASSWORD: opennms
      POSTGRES_DB: opennms
      JAVA_OPTS: >-
        -Xms256m -Xmx512m
        -XX:MaxMetaspaceSize=256m
        -Djava.security.egd=file:/dev/./urandom
        -Dorg.opennms.tsid.node-id=6
    volumes:
      - rtcd-data:/opt/daemon/data
      - ./rtcd-overlay:/opt/daemon-overlay:ro
    healthcheck:
      test: ["CMD-SHELL", "curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe || exit 1"]
      interval: 15s
      timeout: 10s
      retries: 20
      start_period: 60s
```

Repeat for: passivestatusd (TSID=7), notifd (TSID=8), discovery (TSID=9), trapd (TSID=10, add `ports: ["1162:1162/udp"]`), syslogd (TSID=11, add `ports: ["10514:10514/udp"]`), ticketer (TSID=12), eventtranslator (TSID=13).

**Step 2: Disable extracted daemons in core container**

Add to core environment:
```yaml
      CORE_SERVICE_RTCD_ENABLED: "false"
      CORE_SERVICE_PASSIVESTATUSD_ENABLED: "false"
      CORE_SERVICE_NOTIFD_ENABLED: "false"
      CORE_SERVICE_DISCOVERY_ENABLED: "false"
      CORE_SERVICE_TRAPD_ENABLED: "false"
      CORE_SERVICE_SYSLOGD_ENABLED: "false"
      CORE_SERVICE_TICKETER_ENABLED: "false"
      CORE_SERVICE_EVENTTRANSLATOR_ENABLED: "false"
```

Also add the deleted daemons (they're gone from code, but confd templates may still check):
```yaml
      CORE_SERVICE_VACUUMD_ENABLED: "false"
      CORE_SERVICE_STATSD_ENABLED: "false"
      CORE_SERVICE_ACTIOND_ENABLED: "false"
      CORE_SERVICE_ACKD_ENABLED: "false"
```

**Step 3: Add volumes for new services**

Add to the `volumes:` section:
```yaml
  rtcd-data:
  passivestatusd-data:
  notifd-data:
  discovery-data:
  trapd-data:
  syslogd-data:
  ticketer-data:
  eventtranslator-data:
```

**Step 4: Update README.md**

Update `opennms-container/strike-fighter/README.md` with the new 15-service architecture.

**Step 5: Commit**

```bash
git add opennms-container/strike-fighter/
git commit -m "feat: update Strike Fighter compose to 15-service architecture

Core now runs only: Eventd, Provisiond, Enlinkd, Telemetryd, Bsmd,
Correlator, Scriptd. All other daemons are standalone containers or
deleted."
```

---

### Task 17: Add Daemon Features to Sentinel Assembly

The `opennms/daemon` Docker image is built from the Sentinel Karaf assembly. New daemon features must be added to the assembly's `installedFeatures` so the `karaf-maven-plugin` resolves all required bundles into the `system/` directory.

**Files:**
- Modify: `features/container/sentinel/pom.xml` — add all new daemon features to `installedFeatures`
- Modify: `container/features/pom.xml` — add Maven dependencies for new daemon-loader modules

**Step 1: Add dependencies to container/features/pom.xml**

For each new daemon-loader module, add a `<dependency>` block:

```xml
<dependency>
    <groupId>org.opennms.core</groupId>
    <artifactId>org.opennms.core.daemon-loader-rtcd</artifactId>
    <version>${project.version}</version>
</dependency>
<!-- ... repeat for passivestatusd, notifd, discovery, trapd, syslogd, ticketer, eventtranslator -->
```

**Step 2: Add installedFeatures to sentinel/pom.xml**

Find the `karaf-maven-plugin` configuration in `features/container/sentinel/pom.xml` and add:

```xml
<installedFeature>opennms-daemon-rtcd</installedFeature>
<installedFeature>opennms-daemon-passivestatusd</installedFeature>
<installedFeature>opennms-daemon-notifd</installedFeature>
<installedFeature>opennms-daemon-discovery</installedFeature>
<installedFeature>opennms-daemon-trapd</installedFeature>
<installedFeature>opennms-daemon-syslogd</installedFeature>
<installedFeature>opennms-daemon-ticketer</installedFeature>
<installedFeature>opennms-daemon-eventtranslator</installedFeature>
```

**Step 3: Verify the assembly builds**

```bash
./compile.pl -DskipTests --projects :org.opennms.features.container.sentinel -am install
```

**Step 4: Commit**

```bash
git add features/container/sentinel/pom.xml container/features/pom.xml
git commit -m "feat: add all daemon features to Sentinel assembly for opennms/daemon image"
```

---

### Task 18: Build and Test End-to-End

**Step 1: Build the full daemon assembly chain**

```bash
# Build new daemon-loader modules
./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-rtcd,:org.opennms.core.daemon-loader-passivestatusd,:org.opennms.core.daemon-loader-notifd,:org.opennms.core.daemon-loader-discovery,:org.opennms.core.daemon-loader-trapd,:org.opennms.core.daemon-loader-syslogd,:org.opennms.core.daemon-loader-ticketer,:org.opennms.core.daemon-loader-eventtranslator -am install

# Rebuild features XML
./compile.pl -DskipTests -pl container/features install

# Rebuild sentinel container (resolves all features into system/)
./compile.pl -DskipTests -pl features/container/sentinel install

# Build daemon assembly
./compile.pl -DskipTests -pl opennms-assemblies/daemon install
```

**Step 2: Build Docker images**

```bash
cd opennms-container/daemon && make
cd ../strike-fighter && docker compose build
```

**Step 3: Start Strike Fighter**

```bash
cd opennms-container/strike-fighter
docker compose up -d
docker compose ps  # wait for all healthy
```

**Step 4: Verify each daemon container starts**

For each daemon container, check:
```bash
docker compose logs rtcd | tail -20
docker compose logs passivestatusd | tail -20
# ... repeat for each
```

Look for: `Daemon started: <name>` in the logs.

**Step 5: Verify health endpoints**

```bash
for svc in rtcd passivestatusd notifd discovery trapd syslogd ticketer eventtranslator; do
  echo "=== $svc ==="
  docker compose exec $svc curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe
  echo
done
```

**Step 6: Verify Kafka consumer groups**

```bash
docker compose exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --list
```

Expected: consumer groups for each daemon (opennms-rtcd, opennms-notifd, etc.)

**Step 7: Commit any fixes**

```bash
git add -A
git commit -m "fix: resolve integration issues for 15-service Strike Fighter deployment"
```

---

## Summary

| Phase | Tasks | Deliverable |
|-------|-------|-------------|
| **Phase 1** | Tasks 1-6 | 4 daemons deleted, dead event infrastructure removed |
| **Phase 2** | Tasks 7-9 | Rtcd + PassiveStatusd standalone containers |
| **Phase 3** | Tasks 10-15 | 6 more daemons as standalone containers |
| **Phase 4** | Tasks 16-18 | Full 15-service compose, assembly wiring, E2E test |

**Parallelization:**
- Tasks 1-4 (deletions) are independent — can run in parallel
- Tasks 5-6 (event infra deletion) depend on Tasks 1-4 completing
- Tasks 7-8 (EASY daemons) are independent of each other
- Tasks 10-15 (MEDIUM daemons) are independent of each other
- Task 9 depends on Tasks 7-8
- Tasks 16-18 depend on all prior tasks
