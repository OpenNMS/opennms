# Phase A: Core Container Elimination Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Extract Provisiond and Bsmd into opennms/daemon containers, migrate Alarmd from opennms/alarmd to opennms/daemon, delete Queued, eliminate ActiveMQ, and remove the core container entirely.

**Architecture:** Each daemon becomes a standalone Karaf container (opennms/daemon image) with its own KafkaEventForwarder, KafkaEventSubscriptionService, and EventConfDao. Events flow exclusively via Kafka — NO ActiveMQ, NO events table in PostgreSQL. The core container (opennms/horizon with JettyServer disabled) is eliminated; database initialization moves to an init container.

**Tech Stack:** Java 17, Spring 4.2, OSGi/Karaf, Kafka, Docker Compose.

**IRON RULES:**
- Events are NEVER written to PostgreSQL. Only alarms are persisted (by Alarmd).
- NO ActiveMQ. All cross-container communication uses Kafka.
- Each daemon container is self-contained with its own event infrastructure.

---

## Task 1: Delete Queued Daemon

Queued is an RRD write optimization daemon. With the move to Newts/Cortex timeseries, it is no longer needed.

**Files:**
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/queued/Queued.java`
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/queued/jmx/Queued.java`
- Delete: `opennms-services/src/main/java/org/opennms/netmgt/queued/jmx/QueuedMBean.java`
- Delete: `opennms-services/src/main/resources/META-INF/opennms/applicationContext-queued.xml`
- Modify: `opennms-services/src/main/resources/beanRefContext.xml` (remove queuedContext bean)
- Modify: `opennms-container/core/container-fs/confd/templates/service-configuration.xml.tmpl` (remove Queued entry)
- Modify: `opennms-config-model/src/main/resources/defaults/service-configuration.xml` (remove Queued entry)

**Step 1: Delete source files**

Delete the three Java files:
- `opennms-services/src/main/java/org/opennms/netmgt/queued/Queued.java`
- `opennms-services/src/main/java/org/opennms/netmgt/queued/jmx/Queued.java`
- `opennms-services/src/main/java/org/opennms/netmgt/queued/jmx/QueuedMBean.java`

Then delete the entire `opennms-services/src/main/java/org/opennms/netmgt/queued/` directory tree.

**Step 2: Delete the Spring context**

Delete `opennms-services/src/main/resources/META-INF/opennms/applicationContext-queued.xml`.

**Step 3: Remove beanRefContext entry**

In `opennms-services/src/main/resources/beanRefContext.xml`, remove the `queuedContext` bean definition. It looks like:
```xml
<bean id="queuedContext" class="org.springframework.context.support.ClassPathXmlApplicationContext">
  <constructor-arg>
    <list>
      <value>META-INF/opennms/applicationContext-queued.xml</value>
    </list>
  </constructor-arg>
  <constructor-arg ref="daoContext" />
</bean>
```

**Step 4: Remove from service-configuration confd template**

In `opennms-container/core/container-fs/confd/templates/service-configuration.xml.tmpl`, remove the line:
```xml
<service enabled="{{getv (print $servicesPath "/queued/enabled") "true"}}"><name>OpenNMS:Name=Queued</name>...
```

**Step 5: Remove from default service-configuration**

In `opennms-config-model/src/main/resources/defaults/service-configuration.xml`, remove the `<service>` element for `OpenNMS:Name=Queued`.

**Step 6: Search for remaining references**

Run: `grep -r "Queued\|queued\|QUEUED" --include="*.java" --include="*.xml" --include="*.cfg" --include="*.properties" --include="*.tmpl" -l` (excluding test files)

Remove any remaining references found. Common locations: event definitions, JMX configs, documentation.

**Step 7: Verify build**

Run: `./compile.pl -DskipTests --projects :opennms-services -am install`

Expected: BUILD SUCCESS (no compilation errors from removed Queued references).

**Step 8: Commit**

```bash
git add -A
git commit -m "feat: delete Queued daemon (RRD write optimization)

Moving to Newts/Cortex timeseries makes RRD write optimization
unnecessary. Removes Queued ServiceDaemon, JMX wrapper, Spring
context, and all configuration references."
```

---

## Task 2: Create daemon-loader-provisiond

Provisiond is the node provisioning authority. It receives newSuspect events, performs node scans, and manages the node/interface/service inventory in PostgreSQL. It's a `SpringServiceDaemon` (like TroubleTicketer), NOT `AbstractServiceDaemon`.

**Key differences from simpler daemon loaders:**
- Uses `SpringDaemonLifecycleManager` (not `DaemonLifecycleManager`)
- Has complex @Autowired dependencies in `DefaultProvisionService` (12+ DAOs, 3 LocationAware clients)
- Has its own Quartz scheduler for import scheduling
- Has 4 thread pools (import, scan, write, rescan)
- Has 2 event listener adapters (daemon + adapterManager)

**Reference:** `core/daemon-loader-ticketer/` for SpringDaemonLifecycleManager pattern. `core/daemon-loader-discovery/` for complex wiring.

**Files:**
- Create: `core/daemon-loader-provisiond/pom.xml`
- Create: `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalSnmpClient.java`
- Create: `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalDnsLookupClient.java`
- Create: `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalDetectorClient.java`
- Create: `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java`
- Create: `core/daemon-loader-provisiond/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-provisiond.xml`
- Modify: `core/pom.xml` (add daemon-loader-provisiond module)

**Step 1: Create POM**

Create `core/daemon-loader-provisiond/pom.xml`:

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
    <artifactId>org.opennms.core.daemon-loader-provisiond</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Core :: Daemon Loader :: Provisiond</name>
    <description>
        Karaf-only Provisiond loader. Creates and starts the Provisioner daemon in a
        Karaf container without the Manager/Eventd, wiring all dependencies
        directly from the OSGi service registry.
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
                            META-INF/opennms/*.xml;publish-context:=false;create-asynchronously:=true
                        </Spring-Context>
                    </instructions>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <!-- Daemon base (SpringServiceDaemon) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Provisiond classes (Provisioner, DefaultProvisionService, etc.) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-provisiond</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Provision API (LocationAwareDetectorClient, ForeignSourceRepository) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-provision-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Provision persistence (ForeignSourceRepository, JSR223ScriptCache) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-provision-persistence</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Event API (EventIpcManager, EventForwarder, EventSubscriptionService) -->
        <dependency>
            <groupId>org.opennms.features.events</groupId>
            <artifactId>org.opennms.features.events.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- MessageBus API -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>org.opennms.core.messagebus.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- DAO APIs -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-dao-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Config (SnmpPeerFactory, ProvisiondConfigFactory) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-config</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- SNMP API (LocationAwareSnmpClient) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-snmp-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- ICMP proxy API (LocationAwarePingClient) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-icmp-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Concurrent utils (PausibleScheduledThreadPoolExecutor) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.lib</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Task coordinator -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.tasks</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- TSID -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.tsid</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Spring (for InitializingBean, DisposableBean) -->
        <dependency>
            <groupId>org.apache.servicemix.bundles</groupId>
            <artifactId>org.apache.servicemix.bundles.spring-beans</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.servicemix.bundles</groupId>
            <artifactId>org.apache.servicemix.bundles.spring-context-support</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- Quartz (for ImportScheduler) -->
        <dependency>
            <groupId>org.quartz-scheduler</groupId>
            <artifactId>quartz</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </dependency>
    </dependencies>
</project>
```

**Step 2: Create Local* stub classes**

Provisiond's `DefaultProvisionService` has `@Autowired` for `LocationAwareSnmpClient`, `LocationAwareDnsLookupClient`, `LocationAwareDetectorClient`, `SnmpProfileMapper`, and `TracerRegistry`. In a standalone container, these need local implementations (the full RPC stack isn't available).

Create `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/` with these stub classes. Follow the pattern from `core/daemon-loader-discovery/src/main/java/org/opennms/core/daemon/loader/LocalLocationAwarePingClient.java` and `core/daemon-loader-pollerd/` local implementations.

**NOTE TO IMPLEMENTER:** These stubs are complex. Look at how `core/daemon-loader-pollerd/` implements `LocalPollerClient`, `LocalServiceMonitorRegistry`, and `LocalLocationAwarePingClient`. Provisiond needs:

1. **`LocalSnmpClient`** — implements `LocationAwareSnmpClient`. For standalone container, SNMP operations execute locally (same JVM). Delegates to `SnmpUtils` directly.

2. **`LocalDnsLookupClient`** — implements `LocationAwareDnsLookupClient`. Delegates to `InetAddress.getByName()`.

3. **`LocalDetectorClient`** — implements `LocationAwareDetectorClient`. Delegates to detector's `isServiceDetected()` directly.

4. **`NoOpTracerRegistry`** — implements `TracerRegistry`. Returns no-op tracer. Same pattern as other daemon loaders.

5. **`NoOpSnmpProfileMapper`** — implements `SnmpProfileMapper`. Returns empty Optional (no profile mapping in standalone).

**Step 3: Create Spring XML context**

Create `core/daemon-loader-provisiond/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-provisiond.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  Karaf-only Provisiond loader context.

  In a Karaf-only container (no Manager, no Eventd), this context replaces the
  Spring hierarchy with a flat context that wires Provisioner beans directly to
  OSGi services.

  IRON RULE: Events are NEVER written to PostgreSQL. Events flow via Kafka only.
-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:osgi="http://www.springframework.org/schema/osgi"
       xmlns:onmsgi="http://xmlns.opennms.org/xsd/spring/onms-osgi"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.2.xsd
           http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi.xsd
           http://xmlns.opennms.org/xsd/spring/onms-osgi http://xmlns.opennms.org/xsd/spring/onms-osgi.xsd
       ">

    <context:annotation-config />

    <!-- ServiceRegistry from OSGi (NOT onmsgi:default-registry) -->
    <osgi:reference id="serviceRegistry"
                    interface="org.opennms.core.soa.ServiceRegistry"/>

    <!-- ============================================================== -->
    <!-- OSGi Service References                                        -->
    <!-- ============================================================== -->

    <!-- Event infrastructure (from opennms-event-forwarder-kafka blueprint) -->
    <osgi:reference id="eventIpcManager"
                    interface="org.opennms.netmgt.events.api.EventIpcManager"/>
    <osgi:reference id="eventSubscriptionService"
                    interface="org.opennms.netmgt.events.api.EventSubscriptionService"/>
    <osgi:reference id="eventForwarder"
                    interface="org.opennms.netmgt.events.api.EventForwarder"/>

    <!-- DAOs (from distributed DAO feature) -->
    <onmsgi:reference id="monitoringLocationDao"
                      interface="org.opennms.netmgt.dao.api.MonitoringLocationDao"/>
    <onmsgi:reference id="nodeDao"
                      interface="org.opennms.netmgt.dao.api.NodeDao"/>
    <onmsgi:reference id="ipInterfaceDao"
                      interface="org.opennms.netmgt.dao.api.IpInterfaceDao"/>
    <onmsgi:reference id="snmpInterfaceDao"
                      interface="org.opennms.netmgt.dao.api.SnmpInterfaceDao"/>
    <onmsgi:reference id="monitoredServiceDao"
                      interface="org.opennms.netmgt.dao.api.MonitoredServiceDao"/>
    <onmsgi:reference id="serviceTypeDao"
                      interface="org.opennms.netmgt.dao.api.ServiceTypeDao"/>
    <onmsgi:reference id="categoryDao"
                      interface="org.opennms.netmgt.dao.api.CategoryDao"/>
    <onmsgi:reference id="categoryAssociationDao"
                      interface="org.opennms.netmgt.dao.api.RequisitionedCategoryAssociationDao"/>
    <onmsgi:reference id="monitoringSystemDao"
                      interface="org.opennms.netmgt.dao.api.MonitoringSystemDao"/>

    <!-- Transaction management -->
    <onmsgi:reference id="transactionManager"
                      interface="org.springframework.transaction.PlatformTransactionManager"/>

    <!-- ForeignSourceRepository (from provisioning feature) -->
    <onmsgi:reference id="foreignSourceRepositoryFactory"
                      interface="org.opennms.netmgt.provision.persist.ForeignSourceRepository"
                      filter="(type=fused)"/>
    <onmsgi:reference id="fastFusedForeignSourceRepository"
                      interface="org.opennms.netmgt.provision.persist.ForeignSourceRepository"
                      filter="(type=fastFused)"/>
    <onmsgi:reference id="fastFilePendingForeignSourceRepository"
                      interface="org.opennms.netmgt.provision.persist.ForeignSourceRepository"
                      filter="(type=fastFilePending)"/>

    <!-- ============================================================== -->
    <!-- Local Implementations (standalone container stubs)             -->
    <!-- ============================================================== -->

    <!-- SNMP config factory (static init pattern) -->
    <bean id="init-snmpPeerFactory" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="staticMethod"><value>org.opennms.netmgt.config.SnmpPeerFactory.init</value></property>
    </bean>
    <bean id="snmpPeerFactory" class="org.opennms.netmgt.config.SnmpPeerFactory"
          depends-on="init-snmpPeerFactory" factory-method="getInstance"/>

    <!-- LocationAware clients — local implementations for standalone container -->
    <bean id="locationAwareSnmpClient"
          class="org.opennms.core.daemon.loader.LocalSnmpClient"/>
    <bean id="locationAwareDnsLookupClient"
          class="org.opennms.core.daemon.loader.LocalDnsLookupClient"/>
    <bean id="locationAwareDetectorClient"
          class="org.opennms.core.daemon.loader.LocalDetectorClient"/>
    <bean id="snmpProfileMapper"
          class="org.opennms.core.daemon.loader.NoOpSnmpProfileMapper"/>
    <bean id="tracerRegistry"
          class="org.opennms.core.daemon.loader.NoOpTracerRegistry"/>

    <!-- Transaction-aware event forwarder -->
    <bean id="transactionAwareEventForwarder"
          class="org.opennms.netmgt.dao.TransactionAwareEventForwarder">
        <qualifier value="transactionAware"/>
        <property name="eventForwarder" ref="eventForwarder"/>
    </bean>

    <!-- Provisiond config DAO -->
    <bean id="provisiondConfigDao"
          class="org.opennms.netmgt.dao.jaxb.DefaultProvisiondConfigurationDao"/>
    <onmsgi:service ref="provisiondConfigDao"
                    interface="org.opennms.netmgt.dao.api.ProvisiondConfigurationDao"/>

    <!-- ============================================================== -->
    <!-- Thread Pools (from provisiond applicationContext)              -->
    <!-- ============================================================== -->

    <bean id="importThreads" factory-bean="provisiondConfigDao" factory-method="getImportThreads"/>
    <bean id="scanThreads" factory-bean="provisiondConfigDao" factory-method="getScanThreads"/>
    <bean id="writeThreads" factory-bean="provisiondConfigDao" factory-method="getWriteThreads"/>
    <bean id="rescanThreads" factory-bean="provisiondConfigDao" factory-method="getRescanThreads"/>

    <bean id="importExecutor" class="org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean">
        <property name="poolSize" ref="importThreads"/>
    </bean>
    <bean id="scanExecutor" class="org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean">
        <property name="poolSize" ref="scanThreads"/>
    </bean>
    <bean id="writeExecutor" class="org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean">
        <property name="poolSize" ref="writeThreads"/>
    </bean>
    <bean id="nodeScanExecutor" class="org.springframework.scheduling.concurrent.CustomizableThreadFactory">
        <property name="threadNamePrefix" value="nodeScanExecutor-"/>
    </bean>
    <bean id="scheduledExecutor" class="org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor">
        <constructor-arg ref="rescanThreads"/>
        <constructor-arg ref="nodeScanExecutor"/>
    </bean>

    <!-- Task coordinator -->
    <bean id="taskCoordinator" class="org.opennms.core.tasks.DefaultTaskCoordinator">
        <constructor-arg value="Provisiond"/>
        <property name="defaultExecutor" value="scan"/>
        <property name="executors">
            <map>
                <entry key="import" value-ref="importExecutor"/>
                <entry key="scan" value-ref="scanExecutor"/>
                <entry key="write" value-ref="writeExecutor"/>
            </map>
        </property>
    </bean>

    <!-- Lifecycle repository -->
    <bean id="lifeCycleRepository" class="org.opennms.netmgt.provision.service.lifecycle.DefaultLifeCycleRepository">
        <constructor-arg ref="taskCoordinator"/>
        <property name="lifeCycles">
            <list>
                <bean class="org.opennms.netmgt.provision.service.lifecycle.LifeCycle">
                    <constructor-arg value="import"/>
                    <constructor-arg>
                        <list>
                            <value>validate</value>
                            <value>audit</value>
                            <value>scan</value>
                            <value>delete</value>
                            <value>update</value>
                            <value>insert</value>
                            <value>relate</value>
                        </list>
                    </constructor-arg>
                </bean>
                <bean class="org.opennms.netmgt.provision.service.lifecycle.LifeCycle">
                    <constructor-arg value="nodeImport"/>
                    <constructor-arg>
                        <list>
                            <value>scan</value>
                            <value>persist</value>
                        </list>
                    </constructor-arg>
                </bean>
            </list>
        </property>
    </bean>

    <!-- ============================================================== -->
    <!-- Provisiond Beans                                               -->
    <!-- ============================================================== -->

    <bean id="scriptCache" class="org.opennms.netmgt.provision.persist.JSR223ScriptCache"/>
    <bean id="pluginRegistry" class="org.opennms.netmgt.provision.service.DefaultPluginRegistry"/>
    <bean id="provisionService" class="org.opennms.netmgt.provision.service.DefaultProvisionService"/>
    <bean id="coreImportActivities" class="org.opennms.netmgt.provision.service.CoreImportActivities">
        <constructor-arg ref="provisionService"/>
    </bean>
    <bean id="adapterManager" class="org.opennms.netmgt.provision.service.ProvisioningAdapterManager">
        <property name="pluginRegistry" ref="pluginRegistry"/>
    </bean>
    <bean id="monitorHolder" class="org.opennms.netmgt.provision.service.MonitorHolder"/>

    <!-- Quartz import scheduler -->
    <bean id="quartzScheduler" class="org.springframework.scheduling.quartz.SchedulerFactoryBean">
        <property name="schedulerName" value="provisiond"/>
    </bean>
    <bean id="jobFactory" class="org.opennms.netmgt.provision.service.ImportJobFactory">
        <property name="provisioner" ref="daemon"/>
    </bean>
    <bean id="provisiondImportSchedule" class="org.opennms.netmgt.provision.service.ImportScheduler">
        <constructor-arg ref="quartzScheduler"/>
        <property name="provisioner" ref="daemon"/>
        <property name="importJobFactory" ref="jobFactory"/>
    </bean>

    <!-- ============================================================== -->
    <!-- Provisiond Daemon (SpringServiceDaemon)                        -->
    <!-- ============================================================== -->

    <bean id="daemon" class="org.opennms.netmgt.provision.service.Provisioner"
          depends-on="init-snmpPeerFactory">
        <property name="provisionService" ref="provisionService"/>
        <property name="eventForwarder" ref="transactionAwareEventForwarder"/>
        <property name="lifeCycleRepository" ref="lifeCycleRepository"/>
        <property name="scheduledExecutor" ref="scheduledExecutor"/>
        <property name="importSchedule" ref="provisiondImportSchedule"/>
        <property name="importActivities" ref="coreImportActivities"/>
        <property name="taskCoordinator" ref="taskCoordinator"/>
        <property name="agentConfigFactory" ref="snmpPeerFactory"/>
    </bean>

    <!-- Event listener adapters -->
    <bean id="daemonListener" class="org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter">
        <property name="annotatedListener" ref="daemon"/>
        <property name="eventSubscriptionService" ref="eventSubscriptionService"/>
    </bean>
    <bean id="adapterManagerListener" class="org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter">
        <property name="annotatedListener" ref="adapterManager"/>
        <property name="eventSubscriptionService" ref="eventSubscriptionService"/>
    </bean>

    <!-- Export ProvisiondConfigurationDao as OSGi service -->
    <onmsgi:service ref="provisiondConfigDao"
                    interface="org.opennms.netmgt.dao.api.ProvisiondConfigurationDao"/>

    <!-- ============================================================== -->
    <!-- Daemon Lifecycle (replaces Manager)                            -->
    <!-- ============================================================== -->

    <bean name="daemonLifecycleManager"
          class="org.opennms.core.daemon.loader.SpringDaemonLifecycleManager">
        <constructor-arg ref="daemon"/>
        <constructor-arg value="Provisiond"/>
    </bean>

</beans>
```

**Step 4: Add module to core/pom.xml**

In `core/pom.xml`, add `<module>daemon-loader-provisiond</module>` in the `<modules>` section, alongside the other daemon-loader modules.

**Step 5: Build**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-provisiond -am install`

Expected: BUILD SUCCESS.

**Step 6: Add to docker-compose.yml**

In `opennms-container/delta-v/docker-compose.yml`, add a `provisiond` service entry (following the pattern of existing daemon services like `pollerd`). Assign TSID node-id=16 (next available after Scriptd=15).

```yaml
  provisiond:
    profiles: [lite, full]
    image: opennms/daemon:${VERSION}
    container_name: delta-v-provisiond
    hostname: provisiond
    depends_on:
      core:
        condition: service_healthy
      kafka:
        condition: service_healthy
    environment:
      POSTGRES_HOST: postgres
      POSTGRES_PORT: "5432"
      POSTGRES_USER: opennms
      POSTGRES_PASSWORD: opennms
      OPENNMS_DBNAME: opennms
      OPENNMS_DBUSER: opennms
      OPENNMS_DBPASS: opennms
      JAVA_OPTS: >-
        -Xms512m -Xmx1g
        -XX:MaxMetaspaceSize=512m
        -Djava.security.egd=file:/dev/./urandom
        -Dorg.opennms.tsid.node-id=16
        -Dorg.opennms.kafka.bootstrap.servers=kafka:9092
        -Dorg.opennms.kafka.fault.topic=opennms-fault-events
        -Dorg.opennms.kafka.ipc.topic=opennms-ipc-events
        -Dorg.opennms.kafka.consumer.group=opennms-provisiond
    volumes:
      - provisiond-data:/opt/sentinel/data
    healthcheck:
      test: ["CMD", "/health.sh"]
      interval: 30s
      timeout: 10s
      retries: 10
      start_period: 90s
```

Add `provisiond-data:` to the `volumes:` section. Add `CORE_SERVICE_PROVISIOND_ENABLED: "false"` to the core service environment (if not already present).

**Step 7: Commit**

```bash
git add -A
git commit -m "feat: create daemon-loader-provisiond for standalone container

Provisiond runs as standalone opennms/daemon container with its own
Kafka event transport. Uses SpringDaemonLifecycleManager for
SpringServiceDaemon lifecycle. Local stubs for LocationAware clients
(SNMP, DNS, detector) since full RPC stack not available."
```

---

## Task 3: Create daemon-loader-bsmd

Bsmd is the Business Service Monitoring Daemon. It watches alarms (via `AlarmLifecycleListener`) and calculates business service operational status. It's a `SpringServiceDaemon` with moderate complexity.

**Key observations:**
- Bsmd `@Autowired` BusinessServiceStateMachine and BusinessServiceManager — already registered as OSGi services in `features/bsm/service/impl/` via `<onmsgi:service>`
- Bsmd implements `AlarmLifecycleListener` — in standalone container, alarm snapshots come from a LOCAL `AlarmLifecycleListenerManager` polling the shared PostgreSQL alarm table every 30 seconds (not from Alarmd's JVM)
- Bsmd uses `EventConfDao` to verify reduction keys at startup — needs `DefaultEventConfDao` + `EventConfInitializer` pattern (same as Trapd/Syslogd)
- Bsmd has optional `MessageBus` dependency — set to null in standalone container (Kafka handles IPC)

**Cross-container alarm notification approach:** `AlarmLifecycleListenerManager` (from `opennms-alarms/daemon`) has a built-in `Timer` that polls `AlarmDao.findAll()` and pushes snapshots to all registered `AlarmLifecycleListener` instances. We wire a local instance in Bsmd's container with a 30-second polling interval (system property `org.opennms.alarms.snapshot.sync.ms=30000`). Bsmd registers as a listener locally. This gives eventual consistency — alarm changes are visible within 30 seconds.

**Reference:** `core/daemon-loader-alarmd/` for `AlarmLifecycleListenerManager` wiring pattern.

**Files:**
- Create: `core/daemon-loader-bsmd/pom.xml`
- Create: `core/daemon-loader-bsmd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-bsmd.xml`
- Modify: `core/pom.xml` (add daemon-loader-bsmd module)

**Step 1: Create POM**

Create `core/daemon-loader-bsmd/pom.xml` following the daemon-loader pattern. Key dependencies:

```xml
<!-- BSM daemon class -->
<dependency>
    <groupId>org.opennms.features.bsm</groupId>
    <artifactId>org.opennms.features.bsm.daemon</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
<!-- BSM service API (BusinessServiceStateMachine, BusinessServiceManager) -->
<dependency>
    <groupId>org.opennms.features.bsm</groupId>
    <artifactId>org.opennms.features.bsm.service.api</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
<!-- Alarmd daemon (AlarmLifecycleListenerManager) -->
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>opennms-alarmd</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
<!-- Alarmd API (AlarmLifecycleListener) -->
<dependency>
    <groupId>org.opennms.features</groupId>
    <artifactId>org.opennms.features.alarmd.api</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
<!-- Event API -->
<dependency>
    <groupId>org.opennms.features.events</groupId>
    <artifactId>org.opennms.features.events.api</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
<!-- MessageBus API -->
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>org.opennms.core.messagebus.api</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
<!-- DAO APIs -->
<dependency>
    <groupId>org.opennms</groupId>
    <artifactId>opennms-dao-api</artifactId>
    <version>${project.version}</version>
    <scope>provided</scope>
</dependency>
```

Plus standard Spring/logging deps.

**Step 2: Create Spring XML context**

Create `core/daemon-loader-bsmd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-bsmd.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  Karaf-only Bsmd loader context.

  In a Karaf-only container (no Manager, no Eventd), this context wires Bsmd
  directly to OSGi services.

  CROSS-CONTAINER ALARM NOTIFICATION:
  AlarmLifecycleListenerManager polls AlarmDao.findAll() every 30 seconds
  and pushes snapshots to Bsmd (registered as local AlarmLifecycleListener).
  This replaces same-JVM callbacks from Alarmd.

  IRON RULE: Events are NEVER written to PostgreSQL. Events flow via Kafka only.
-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:osgi="http://www.springframework.org/schema/osgi"
       xmlns:onmsgi="http://xmlns.opennms.org/xsd/spring/onms-osgi"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.2.xsd
           http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi.xsd
           http://xmlns.opennms.org/xsd/spring/onms-osgi http://xmlns.opennms.org/xsd/spring/onms-osgi.xsd
       ">

    <context:annotation-config />

    <osgi:reference id="serviceRegistry"
                    interface="org.opennms.core.soa.ServiceRegistry"/>

    <!-- Event infrastructure -->
    <osgi:reference id="eventIpcManager"
                    interface="org.opennms.netmgt.events.api.EventIpcManager"/>
    <osgi:reference id="eventSubscriptionService"
                    interface="org.opennms.netmgt.events.api.EventSubscriptionService"/>

    <!-- BSM services (from bsm service.impl via onmsgi:service) -->
    <onmsgi:reference id="businessServiceStateMachine"
                      interface="org.opennms.netmgt.bsm.service.BusinessServiceStateMachine"/>
    <onmsgi:reference id="businessServiceManager"
                      interface="org.opennms.netmgt.bsm.service.BusinessServiceManager"/>

    <!-- EventConfDao — local instance loading from DB (same pattern as Trapd/Syslogd) -->
    <bean id="eventConfDao" class="org.opennms.netmgt.config.DefaultEventConfDao"/>

    <!-- Transaction template -->
    <onmsgi:reference id="transactionTemplate"
                      interface="org.springframework.transaction.support.TransactionTemplate"/>

    <!-- DAOs for AlarmLifecycleListenerManager (polls alarm table) -->
    <onmsgi:reference id="alarmDao"
                      interface="org.opennms.netmgt.dao.api.AlarmDao"/>
    <onmsgi:reference id="sessionUtils"
                      interface="org.opennms.netmgt.dao.api.SessionUtils"/>

    <!-- ============================================================== -->
    <!-- Alarm Lifecycle Snapshot Polling (30-second interval)          -->
    <!-- ============================================================== -->
    <!-- AlarmLifecycleListenerManager runs a Timer that polls
         AlarmDao.findAll() and pushes snapshots to all registered
         AlarmLifecycleListener instances. In standalone container,
         this replaces same-JVM callbacks from Alarmd.

         Polling interval: set via system property
         org.opennms.alarms.snapshot.sync.ms=30000 (JAVA_OPTS in compose) -->

    <bean id="alarmLifecycleListenerManager"
          class="org.opennms.netmgt.alarmd.AlarmLifecycleListenerManager"/>
    <!-- Register it as AlarmEntityListener so DAO change notifications flow -->
    <onmsgi:service interface="org.opennms.netmgt.dao.api.AlarmEntityListener"
                    ref="alarmLifecycleListenerManager">
        <onmsgi:service-properties>
            <entry key="registration.export" value="true"/>
        </onmsgi:service-properties>
    </onmsgi:service>
    <!-- Dynamically bind/unbind AlarmLifecycleListeners (Bsmd registers below) -->
    <onmsgi:list id="alarmLifecycleListeners"
                 interface="org.opennms.netmgt.alarmd.api.AlarmLifecycleListener">
        <onmsgi:listener ref="alarmLifecycleListenerManager"
                         bind-method="onListenerRegistered"
                         unbind-method="onListenerUnregistered"/>
    </onmsgi:list>

    <!-- ============================================================== -->
    <!-- Bsmd Daemon (SpringServiceDaemon)                             -->
    <!-- ============================================================== -->

    <bean id="daemon" class="org.opennms.netmgt.bsm.daemon.Bsmd"/>

    <!-- Event listener adapter -->
    <bean id="daemonListener" class="org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter">
        <property name="annotatedListener" ref="daemon"/>
        <property name="eventSubscriptionService" ref="eventSubscriptionService"/>
    </bean>

    <!-- Register Bsmd as AlarmLifecycleListener — the local
         AlarmLifecycleListenerManager will call handleAlarmSnapshot()
         every 30 seconds with all alarms from PostgreSQL -->
    <onmsgi:service interface="org.opennms.netmgt.alarmd.api.AlarmLifecycleListener" ref="daemon">
        <onmsgi:service-properties>
            <entry key="registration.export" value="true"/>
        </onmsgi:service-properties>
    </onmsgi:service>

    <!-- Daemon Lifecycle -->
    <bean name="daemonLifecycleManager"
          class="org.opennms.core.daemon.loader.SpringDaemonLifecycleManager">
        <constructor-arg ref="daemon"/>
        <constructor-arg value="Bsmd"/>
    </bean>

</beans>
```

**Step 3: Add module to core/pom.xml and build**

In `core/pom.xml`, add `<module>daemon-loader-bsmd</module>`.

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-bsmd -am install`

**Step 4: Add to docker-compose.yml**

TSID node-id=17. Profile: `[lite, full]`. Key JAVA_OPTS addition:
```
-Dorg.opennms.alarms.snapshot.sync.ms=30000
```

```yaml
  bsmd:
    profiles: [lite, full]
    image: opennms/daemon:${VERSION}
    container_name: delta-v-bsmd
    hostname: bsmd
    depends_on:
      core:
        condition: service_healthy
      kafka:
        condition: service_healthy
    environment:
      POSTGRES_HOST: postgres
      POSTGRES_PORT: "5432"
      POSTGRES_USER: opennms
      POSTGRES_PASSWORD: opennms
      OPENNMS_DBNAME: opennms
      OPENNMS_DBUSER: opennms
      OPENNMS_DBPASS: opennms
      JAVA_OPTS: >-
        -Xms256m -Xmx512m
        -XX:MaxMetaspaceSize=256m
        -Djava.security.egd=file:/dev/./urandom
        -Dorg.opennms.tsid.node-id=17
        -Dorg.opennms.kafka.bootstrap.servers=kafka:9092
        -Dorg.opennms.kafka.fault.topic=opennms-fault-events
        -Dorg.opennms.kafka.ipc.topic=opennms-ipc-events
        -Dorg.opennms.kafka.consumer.group=opennms-bsmd
        -Dorg.opennms.alarms.snapshot.sync.ms=30000
    volumes:
      - bsmd-data:/opt/sentinel/data
    healthcheck:
      test: ["CMD", "/health.sh"]
      interval: 30s
      timeout: 10s
      retries: 10
      start_period: 90s
```

Add `bsmd-data:` to volumes. Add `CORE_SERVICE_BSMD_ENABLED: "false"` to core environment.

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: create daemon-loader-bsmd for standalone container

Bsmd runs as standalone opennms/daemon container. Uses local
AlarmLifecycleListenerManager to poll alarm table every 30 seconds
(replacing same-JVM callbacks from Alarmd). EventConfDao loads
from DB. MessageBus not wired (Kafka handles all IPC)."
```

---

## Task 4: Migrate Alarmd from opennms/alarmd to opennms/daemon

Alarmd currently uses a custom `opennms/alarmd` Docker image built from `opennms-container/alarmd/`. The `core/daemon-loader-alarmd/` already exists and works. The migration is purely container/compose configuration — no code changes.

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml` (change alarmd image from `opennms/alarmd:${VERSION}` to `opennms/daemon:${VERSION}`)
- Modify: `opennms-container/delta-v/deploy.sh` (remove `opennms/alarmd` from image check)

**Step 1: Update docker-compose.yml**

Change the `alarmd` service definition:
- `image: opennms/alarmd:${VERSION}` → `image: opennms/daemon:${VERSION}`
- Update volume paths from `/opt/alarmd/` to `/opt/sentinel/` (daemon image uses sentinel paths)
- Update port mappings if needed
- Ensure all overlay JAR mounts point to `/opt/sentinel/system/` paths

**Step 2: Update deploy.sh**

Remove `"opennms/alarmd:$VERSION"` from the image check loop. Only `opennms/horizon`, `opennms/daemon`, and `opennms/minion` remain.

**Step 3: Verify**

Run: `cd opennms-container/delta-v && docker compose config --services`

Confirm alarmd is listed and uses `opennms/daemon` image.

**Step 4: Commit**

```bash
git add -A
git commit -m "refactor: migrate alarmd from opennms/alarmd to opennms/daemon image

Eliminates the separate opennms/alarmd Docker build. Alarmd now runs
on the standard opennms/daemon (Karaf-only) image like all other
standalone daemons. daemon-loader-alarmd already handles wiring."
```

---

## Task 5: Merge PassiveStatusKeeper into Pollerd

PassiveStatusKeeper is a singleton that tracks passive service status. Currently it runs in its own PassiveStatusd container, but Pollerd's PassiveServiceMonitor calls into it (expecting same-JVM singleton). In standalone containers, the singleton is empty.

**Fix:** Move PassiveStatusKeeper into Pollerd's daemon-loader Spring context. Eliminate the PassiveStatusd container.

**Files:**
- Modify: `core/daemon-loader-pollerd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-pollerd.xml`
- Modify: `opennms-container/delta-v/docker-compose.yml` (remove passivestatusd service)
- Delete (or archive): `core/daemon-loader-passivestatusd/`

**Step 1: Add PassiveStatusKeeper to Pollerd context**

In Pollerd's Spring XML, add:
```xml
<!-- PassiveStatusKeeper — same JVM as Pollerd so PassiveServiceMonitor can access it -->
<bean id="passiveStatusKeeper-init" class="org.springframework.beans.factory.config.MethodInvokingBean">
    <property name="targetClass" value="org.opennms.netmgt.passive.PassiveStatusKeeper"/>
    <property name="targetMethod" value="setInstance"/>
    <property name="arguments">
        <list><ref bean="passiveStatusKeeper"/></list>
    </property>
</bean>
<bean id="passiveStatusKeeper" class="org.opennms.netmgt.passive.PassiveStatusKeeper"
      depends-on="passiveStatusKeeper-init"/>
<bean id="passiveStatusKeeperListener" class="org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter">
    <property name="annotatedListener" ref="passiveStatusKeeper"/>
    <property name="eventSubscriptionService" ref="eventSubscriptionService"/>
</bean>
```

**Step 2: Remove passivestatusd from docker-compose.yml**

Delete the `passivestatusd` service definition and `passivestatusd-data` volume.

**Step 3: Delete daemon-loader-passivestatusd module**

Remove `core/daemon-loader-passivestatusd/` directory and its entry from `core/pom.xml`.

**Step 4: Build and verify**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-pollerd -am install`

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: merge PassiveStatusKeeper into Pollerd container

PassiveServiceMonitor requires PassiveStatusKeeper in the same JVM.
Merging eliminates the standalone PassiveStatusd container."
```

---

## Task 6: Eliminate ActiveMQ

Core embeds an ActiveMQ broker on port 61616. No daemon containers use it anymore (all IPC flows via Kafka). Remove it.

**Files:**
- Delete: `opennms-container/delta-v/core-overlay/etc/opennms-activemq.xml`
- Modify: `opennms-container/delta-v/docker-compose.yml` (remove port 61616 from core, update healthcheck)
- Remove any `OPENNMS_BROKER_URL` env vars from daemon services

**Step 1: Remove AMQ config overlay**

Delete `opennms-container/delta-v/core-overlay/etc/opennms-activemq.xml`.

**Step 2: Update core service in docker-compose.yml**

- Remove `- "61616:61616"` from ports
- Change healthcheck from `</dev/tcp/localhost/61616` to a different check (e.g., check if the Java process is running: `["CMD-SHELL", "pgrep -f opennms"]`)
- Remove the AMQ config volume mount

**Step 3: Remove OPENNMS_BROKER_URL from all services**

Search docker-compose.yml for `OPENNMS_BROKER_URL` and remove any references.

**Step 4: Commit**

```bash
git add -A
git commit -m "feat: eliminate ActiveMQ from Delta-V architecture

All cross-container communication uses Kafka topics. ActiveMQ broker
removed from core container. Port 61616 no longer exposed."
```

---

## Task 7: Eliminate Core Container

With Provisiond and Bsmd extracted, Queued deleted, and AMQ removed, the core container has only Manager + Eventd + EventRouter + KafkaEventConsumer. But:
- **EventRouter** classifies events and publishes to Kafka topics — but no local event producers remain in core
- **KafkaEventConsumer** receives from Kafka and broadcasts locally — but no local listeners remain in core
- **Manager** starts services from service-configuration.xml — but all services are disabled
- **Eventd** TCP/UDP listeners accept events — but in a Kafka world, events should go to Kafka directly

**The core container serves no purpose.** Remove it.

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml` (remove core service)
- Modify: `opennms-container/delta-v/deploy.sh` (remove core from image checks)
- Modify: All daemon service `depends_on` that reference `core` — replace with dependency on `postgres` and `kafka`

**Step 1: Add database init container**

The core container currently handles database initialization (Liquibase schema + `install -dis`). Replace with an init container that runs once:

```yaml
  db-init:
    image: opennms/horizon:${VERSION}
    command: ["-i"]  # install mode only
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      POSTGRES_HOST: postgres
      POSTGRES_PORT: "5432"
      POSTGRES_USER: opennms
      POSTGRES_PASSWORD: opennms
      OPENNMS_DBNAME: opennms
      OPENNMS_DBUSER: opennms
      OPENNMS_DBPASS: opennms
```

**NOTE TO IMPLEMENTER:** Verify the opennms/horizon image supports `-i` (install-only) mode. Check the entrypoint script. If not, use `command: ["/opt/opennms/bin/install", "-dis"]` or similar. The init container should exit 0 after schema migration.

**Step 2: Remove core service from docker-compose.yml**

Delete the entire `core` service definition.

**Step 3: Update depends_on references**

All daemon services that had `depends_on: core: condition: service_healthy` should change to:
```yaml
depends_on:
  db-init:
    condition: service_completed_successfully
  kafka:
    condition: service_healthy
```

**Step 4: Update deploy.sh**

- Remove `opennms/horizon` from image check loop (only `opennms/daemon` and `opennms/minion` remain)
- Update service counts in usage text

Wait — webapp still uses `opennms/horizon`. Keep it in the image check.

**Step 5: Verify compose config**

Run: `cd opennms-container/delta-v && docker compose config --services`

Confirm core is gone, db-init is present, and all services are listed.

**Step 6: Commit**

```bash
git add -A
git commit -m "feat: eliminate core container from Delta-V architecture

All daemons run in standalone opennms/daemon containers. Database
initialization handled by one-shot db-init container. EventRouter
and KafkaEventConsumer are no longer needed — each daemon has its
own Kafka event infrastructure. ActiveMQ broker eliminated."
```

---

## Execution Order and Dependencies

```
Task 1 (Delete Queued) ─────────────────────────────────────┐
Task 2 (daemon-loader-provisiond) ───────────────────────────┤
Task 3 (daemon-loader-bsmd) ─────────────────────────────────┤── all independent
Task 4 (Alarmd → opennms/daemon) ───────────────────────────┤
Task 5 (PassiveStatusKeeper → Pollerd) ─────────────────────┘
                    │
                    ▼
Task 6 (Eliminate ActiveMQ) ─── depends on core still existing but not needing AMQ
                    │
                    ▼
Task 7 (Eliminate Core) ─── depends on ALL above being complete
```

Tasks 1-5 are independent and can be executed in parallel (different modules, no shared files except docker-compose.yml which should be updated centrally after all are done). Task 6 requires AMQ to be unused. Task 7 requires everything else.
