# PerspectivePollerd Migration & Dead Daemon Cleanup — Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract PerspectivePollerd into a standalone daemon container (TSID=7) and remove the dead Notifd JMX mbean, bringing Delta-V from 17 to 18 services.

**Architecture:** PerspectivePollerd is the last daemon to extract from the monolith. Unlike Pollerd (which uses a local `LocalPollerClient`), PerspectivePollerd requires the real `LocationAwarePollerClientImpl` wired to Kafka RPC for dispatching polls to remote Minions. The daemon-loader pattern: flat Spring XML, OSGi/onmsgi references, `SpringDaemonLifecycleManager`.

**Tech Stack:** Java 17, Spring 4.2 XML, OSGi/Karaf, Maven Bundle Plugin, Docker Compose

---

## File Structure

**Created:**
| File | Responsibility |
|------|---------------|
| `core/daemon-loader-perspectivepoller/pom.xml` | Bundle POM with provided deps, Spring-Context header |
| `core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/LocalServiceMonitorRegistry.java` | ServiceLoader-based ServiceMonitorRegistry (copied from pollerd) |
| `core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java` | No-op TracerRegistry stub (copied from provisiond) |
| `core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/SpringDaemonLifecycleManager.java` | SpringServiceDaemon lifecycle driver (copied from bsmd) |
| `core/daemon-loader-perspectivepoller/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-perspectivepoller.xml` | Flat Spring XML wiring all beans |
| `opennms-container/delta-v/perspectivepollerd-overlay/etc/featuresBoot.d/perspectivepoller.boot` | Karaf feature boot |
| `opennms-container/delta-v/perspectivepollerd-overlay/etc/org.opennms.core.health.cfg.cfg` | Health ignore list |

**Modified:**
| File | Change |
|------|--------|
| `core/pom.xml:27-80` | Add `<module>daemon-loader-perspectivepoller</module>` |
| `container/features/src/main/resources/features.xml:2132` | Add `opennms-daemon-perspectivepoller` feature |
| `features/container/sentinel/pom.xml:410` | Add to `<installedFeatures>` |
| `opennms-container/delta-v/docker-compose.yml:563,639-657` | Add perspectivepollerd service + volume |
| `opennms-base-assembly/src/main/filtered/etc/jmx-datacollection-config.xml:57-68` | Remove Notifd mbean |

---

## Chunk 1: Daemon-Loader Module + Core POM

### Task 1: Create the daemon-loader POM

**Files:**
- Create: `core/daemon-loader-perspectivepoller/pom.xml`

- [ ] **Step 1: Create the POM**

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
    <artifactId>org.opennms.core.daemon-loader-perspectivepoller</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Core :: Daemon Loader :: PerspectivePollerd</name>
    <description>
        Karaf-only PerspectivePollerd loader. Creates and starts PerspectivePollerd
        in a Karaf container, wiring LocationAwarePollerClient via Kafka RPC
        for multi-location polling.
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
                        <Import-Package>*;resolution:=optional</Import-Package>
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
        <!-- Daemon base (SpringServiceDaemon interface) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- PerspectivePollerd daemon classes -->
        <dependency>
            <groupId>org.opennms.features</groupId>
            <artifactId>org.opennms.features.perspectivepoller</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Poller client RPC (LocationAwarePollerClientImpl, PollerClientRpcModule) -->
        <dependency>
            <groupId>org.opennms.features.poller</groupId>
            <artifactId>org.opennms.features.poller.client-rpc</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Poller daemon classes (DefaultSnmpCollectionAgentFactory, ServiceMonitor impls) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-services</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Event API (EventIpcManager interface) -->
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

        <!-- Poller config -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-config</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Collection API (PersisterFactory, CollectionAgentFactory) -->
        <dependency>
            <groupId>org.opennms.features.collection</groupId>
            <artifactId>org.opennms.features.collection.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Collection core (AbstractCollectionAgentFactory) -->
        <dependency>
            <groupId>org.opennms.features.collection</groupId>
            <artifactId>org.opennms.features.collection.core</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Thresholding API -->
        <dependency>
            <groupId>org.opennms.features.collection</groupId>
            <artifactId>org.opennms.features.collection.thresholding.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- ICMP API -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-icmp-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Poller API (ServiceMonitorRegistry) -->
        <dependency>
            <groupId>org.opennms.features.poller</groupId>
            <artifactId>org.opennms.features.poller.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- KV Store API -->
        <dependency>
            <groupId>org.opennms.features.distributed</groupId>
            <artifactId>org.opennms.features.distributed.kv-store.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Poll outages API -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>org.opennms.config-dao.poll-outages.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- MATE API (EntityScopeProvider) -->
        <dependency>
            <groupId>org.opennms.core.mate</groupId>
            <artifactId>org.opennms.core.mate.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- RPC API (RpcClientFactory) -->
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Tracing API (TracerRegistry) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.tracing-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Spring (for InitializingBean, DisposableBean) -->
        <dependency>
            <groupId>org.apache.servicemix.bundles</groupId>
            <artifactId>org.apache.servicemix.bundles.spring-beans</artifactId>
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

- [ ] **Step 2: Register module in core/pom.xml**

Add `<module>daemon-loader-perspectivepoller</module>` after `daemon-loader-bsmd` in `core/pom.xml` (after line 77).

- [ ] **Step 3: Verify POM resolves**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-perspectivepoller -am install`
Expected: BUILD SUCCESS (may take a while for first -am build)

- [ ] **Step 4: Commit**

```bash
git add core/daemon-loader-perspectivepoller/pom.xml core/pom.xml
git commit -m "feat(delta-v): add daemon-loader-perspectivepoller POM (TSID=7)"
```

### Task 2: Create Java helper classes

Each daemon-loader bundle must contain its own copies of helper classes — these are NOT shared via OSGi because only one daemon-loader feature is installed per container.

**Files:**
- Create: `core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/LocalServiceMonitorRegistry.java`
- Create: `core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java`
- Create: `core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/SpringDaemonLifecycleManager.java`

- [ ] **Step 1: Copy LocalServiceMonitorRegistry from daemon-loader-pollerd**

```bash
cp core/daemon-loader-pollerd/src/main/java/org/opennms/core/daemon/loader/LocalServiceMonitorRegistry.java \
   core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/LocalServiceMonitorRegistry.java
```

Identical copy — discovers ServiceMonitor implementations via Java ServiceLoader.

- [ ] **Step 2: Copy NoOpTracerRegistry from daemon-loader-provisiond**

```bash
cp core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java \
   core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java
```

Identical copy — returns GlobalTracer.get() (noop) when no tracing is configured.

- [ ] **Step 3: Copy SpringDaemonLifecycleManager from daemon-loader-bsmd**

```bash
cp core/daemon-loader-bsmd/src/main/java/org/opennms/core/daemon/loader/SpringDaemonLifecycleManager.java \
   core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/SpringDaemonLifecycleManager.java
```

Identical copy — drives afterPropertiesSet() → start() → destroy() lifecycle for SpringServiceDaemon.

- [ ] **Step 4: Commit**

```bash
git add core/daemon-loader-perspectivepoller/src/main/java/
git commit -m "feat(delta-v): add PerspectivePollerd daemon-loader helper classes"
```

### Task 3: Create the Spring XML context

**Files:**
- Create: `core/daemon-loader-perspectivepoller/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-perspectivepoller.xml`

**Critical wiring details:**
- `PerspectivePollerd` uses `@Autowired` constructor injection — `<context:annotation-config/>` is required
- `LocationAwarePollerClientImpl` uses `@Autowired` field injection — same reason
- `PollerClientRpcModule` uses `@Autowired @Qualifier("pollerExecutor")` — bean name must match
- `DefaultSnmpCollectionAgentFactory` extends `AbstractCollectionAgentFactory` with `@Autowired` fields for `NodeDao`, `IpInterfaceDao`, `PlatformTransactionManager` — all must be in context
- `EventIpcManager` implements `EventForwarder` and `EventSubscriptionService` — use aliases
- `SpringDaemonLifecycleManager` (not `DaemonLifecycleManager`) because `PerspectivePollerd` implements `SpringServiceDaemon`
- `RpcClientFactory` comes from `osgi:reference` (Kafka RPC Blueprint registers it in OSGi Framework registry)
- `AnnotationBasedEventListenerAdapter` for both daemon AND tracker (both have `@EventListener` + `@EventHandler` annotations)

- [ ] **Step 1: Create the Spring XML**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  Karaf-only PerspectivePollerd loader context.

  In a Karaf-only container (no Manager, no Eventd), this context replaces the
  Spring hierarchy with a flat context that wires PerspectivePollerd beans
  directly to OSGi services.

  Key difference from Pollerd: uses LocationAwarePollerClientImpl (real Kafka
  RPC to Minions) instead of LocalPollerClient. PerspectivePollerd polls
  services from multiple geographic locations via Minion delegation.

  IRON RULE: Events are NEVER written to PostgreSQL. Events flow via Kafka only.
-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:osgi="http://www.springframework.org/schema/osgi"
       xmlns:onmsgi="http://xmlns.opennms.org/xsd/spring/onms-osgi"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
           http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.2.xsd
           http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.2.xsd
           http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi.xsd
           http://xmlns.opennms.org/xsd/spring/onms-osgi http://xmlns.opennms.org/xsd/spring/onms-osgi.xsd
       ">

    <!-- Enable @Autowired processing for PerspectivePollerd constructor,
         LocationAwarePollerClientImpl fields, PollerClientRpcModule fields,
         and DefaultSnmpCollectionAgentFactory fields -->
    <context:annotation-config />
    <tx:annotation-driven />

    <!-- ServiceRegistry: import the shared instance from OSGi (registered by
         service-registry module) rather than creating a local one with
         <onmsgi:default-registry/> which would be a different, empty instance -->
    <osgi:reference id="serviceRegistry"
                    interface="org.opennms.core.soa.ServiceRegistry"/>

    <!-- ============================================================== -->
    <!-- OSGi Service References (from other Karaf features)            -->
    <!-- ============================================================== -->

    <!-- Event infrastructure (from opennms-event-forwarder-kafka blueprint).
         KafkaEventIpcManagerAdapter implements EventIpcManager, EventForwarder,
         AND EventSubscriptionService — aliases let @Autowired match by type -->
    <osgi:reference id="eventIpcManager"
                    interface="org.opennms.netmgt.events.api.EventIpcManager"/>
    <alias name="eventIpcManager" alias="eventForwarder"/>
    <alias name="eventIpcManager" alias="eventSubscriptionService"/>

    <!-- Kafka RPC (from opennms-core-ipc-rpc-kafka blueprint).
         Provides RpcClientFactory for LocationAwarePollerClientImpl to
         dispatch polls to Minions at perspective locations. -->
    <osgi:reference id="rpcClientFactory"
                    interface="org.opennms.core.rpc.api.RpcClientFactory"/>

    <!-- DAOs (from opennms-distributed-core-impl via SOA ServiceRegistry) -->
    <onmsgi:reference id="sessionUtils"
                      interface="org.opennms.netmgt.dao.api.SessionUtils"/>
    <onmsgi:reference id="monitoringLocationDao"
                      interface="org.opennms.netmgt.dao.api.MonitoringLocationDao"/>
    <onmsgi:reference id="monitoredServiceDao"
                      interface="org.opennms.netmgt.dao.api.MonitoredServiceDao"/>
    <onmsgi:reference id="applicationDao"
                      interface="org.opennms.netmgt.dao.api.ApplicationDao"/>
    <onmsgi:reference id="outageDao"
                      interface="org.opennms.netmgt.dao.api.OutageDao"/>
    <onmsgi:reference id="nodeDao"
                      interface="org.opennms.netmgt.dao.api.NodeDao"/>
    <onmsgi:reference id="ipInterfaceDao"
                      interface="org.opennms.netmgt.dao.api.IpInterfaceDao"/>

    <!-- Collection persistence (from timeseries feature) -->
    <onmsgi:reference id="persisterFactory"
                      interface="org.opennms.netmgt.collection.api.PersisterFactory"/>

    <!-- Thresholding (from sentinel-thresholding-service feature) -->
    <onmsgi:reference id="thresholdingService"
                      interface="org.opennms.netmgt.threshd.api.ThresholdingService"/>

    <!-- Entity scope provider (MATE engine, for LocationAwarePollerClientImpl) -->
    <onmsgi:reference id="entityScopeProvider"
                      interface="org.opennms.core.mate.api.EntityScopeProvider"/>

    <!-- Transaction management -->
    <onmsgi:reference id="transactionManager"
                      interface="org.springframework.transaction.PlatformTransactionManager"/>

    <!-- ============================================================== -->
    <!-- Poller Configuration (local factory — opennms-config has no    -->
    <!-- Spring-Context header, so Spring-DM won't auto-load it)       -->
    <!-- ============================================================== -->

    <bean id="init-pollerConfig-factory"
          class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="staticMethod">
            <value>org.opennms.netmgt.config.PollerConfigFactory.init</value>
        </property>
    </bean>
    <bean id="pollerConfig" class="org.opennms.netmgt.config.PollerConfigFactory"
          depends-on="init-pollerConfig-factory" factory-method="getInstance"/>

    <!-- ============================================================== -->
    <!-- LocationAwarePollerClient (real Kafka RPC — NOT LocalPollerClient) -->
    <!-- ============================================================== -->

    <!-- Service monitor registry (discovers monitors via ServiceLoader) -->
    <bean id="serviceMonitorRegistry"
          class="org.opennms.core.daemon.loader.LocalServiceMonitorRegistry"/>

    <!-- Thread pool for PollerClientRpcModule (@Qualifier("pollerExecutor")) -->
    <bean id="pollerExecutor" class="java.util.concurrent.Executors"
          factory-method="newCachedThreadPool"/>

    <!-- RPC module: serializes/deserializes poller request/response DTOs -->
    <bean id="pollerClientRpcModule"
          class="org.opennms.netmgt.poller.client.rpc.PollerClientRpcModule"/>

    <!-- RPC target helper: routes RPC calls to the right Minion location -->
    <bean id="rpcTargetHelper"
          class="org.opennms.core.rpc.utils.RpcTargetHelper"/>

    <!-- The real LocationAwarePollerClient that delegates to Minions via Kafka RPC.
         afterPropertiesSet() creates the RPC delegate from rpcClientFactory + pollerClientRpcModule.
         All 5 @Autowired fields are resolved by <context:annotation-config/>:
           - serviceMonitorRegistry, pollerClientRpcModule, rpcClientFactory,
             rpcTargetHelper, entityScopeProvider -->
    <bean id="locationAwarePollerClient"
          class="org.opennms.netmgt.poller.client.rpc.LocationAwarePollerClientImpl"/>

    <!-- ============================================================== -->
    <!-- Local Beans                                                    -->
    <!-- ============================================================== -->

    <!-- CollectionAgentFactory: required by PerspectivePollerd constructor.
         @Autowired fields (nodeDao, ipInterfaceDao, transMgr) resolved by annotation-config -->
    <bean id="collectionAgentFactory"
          class="org.opennms.netmgt.collectd.DefaultSnmpCollectionAgentFactory"/>

    <!-- No-op tracer (same as provisiond standalone) -->
    <bean id="tracerRegistry"
          class="org.opennms.core.daemon.loader.NoOpTracerRegistry"/>

    <!-- ============================================================== -->
    <!-- PerspectiveServiceTracker + PerspectivePollerd                 -->
    <!-- ============================================================== -->

    <!-- Tracker: monitors application/service perspective changes in DB.
         Constructor-injected with SessionUtils and ApplicationDao via @Autowired -->
    <bean id="tracker" class="org.opennms.netmgt.perspectivepoller.PerspectiveServiceTracker"/>

    <!-- Tracker event listener: subscribes to node/service/application events
         that trigger tracker refresh (NODE_GAINED_SERVICE, SERVICE_DELETED, etc.) -->
    <bean id="serviceTrackerListener" class="org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter">
        <property name="annotatedListener" ref="tracker"/>
        <property name="eventSubscriptionService" ref="eventIpcManager"/>
    </bean>

    <!-- PerspectivePollerd daemon: constructor-injected with 13 parameters via @Autowired.
         Implements SpringServiceDaemon (start/destroy lifecycle) -->
    <bean id="daemon" class="org.opennms.netmgt.perspectivepoller.PerspectivePollerd"/>

    <!-- Daemon event listener: subscribes to reloadDaemonConfig, perspectiveNodeLost/Regained -->
    <bean id="daemonListener" class="org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter">
        <property name="annotatedListener" ref="daemon"/>
        <property name="eventSubscriptionService" ref="eventIpcManager"/>
    </bean>

    <!-- ============================================================== -->
    <!-- Daemon Lifecycle (SpringServiceDaemon — calls start/destroy)   -->
    <!-- ============================================================== -->

    <bean name="daemonLifecycleManager"
          class="org.opennms.core.daemon.loader.SpringDaemonLifecycleManager">
        <constructor-arg ref="daemon"/>
        <constructor-arg value="PerspectivePollerd"/>
    </bean>

</beans>
```

- [ ] **Step 2: Build the daemon-loader module**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-perspectivepoller -am install`
Expected: BUILD SUCCESS — the bundle JAR should be created in `core/daemon-loader-perspectivepoller/target/`

- [ ] **Step 3: Commit**

```bash
git add core/daemon-loader-perspectivepoller/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-perspectivepoller.xml
git commit -m "feat(delta-v): add PerspectivePollerd daemon-loader Spring XML context"
```

---

## Chunk 2: Karaf Feature + Sentinel Assembly

### Task 4: Add Karaf feature definition

**Files:**
- Modify: `container/features/src/main/resources/features.xml:2132`

The new feature goes immediately after `opennms-daemon-pollerd` (line 2132). This is the logical location — both are poller-family daemons.

- [ ] **Step 1: Add the feature XML**

Insert after line 2132 (after `</feature>` closing `opennms-daemon-pollerd`):

```xml

    <!-- Karaf-only PerspectivePollerd: polls from multiple locations via Minion RPC -->
    <feature name="opennms-daemon-perspectivepoller" version="${project.version}"
             description="OpenNMS :: Daemon Loader :: PerspectivePollerd">
        <!-- Spring DM Extender (processes Spring-Context manifest headers) -->
        <feature>opennms-spring-extender</feature>
        <!-- Event transport (Kafka-based, replaces Eventd) -->
        <feature>opennms-event-forwarder-kafka</feature>
        <!-- DAO infrastructure -->
        <feature>opennms-distributed-core-impl</feature>
        <feature>opennms-persistence</feature>
        <!-- Poller + daemon config -->
        <feature>opennms-config</feature>
        <!-- Core daemon (SpringServiceDaemon, DaemonTools) -->
        <feature>opennms-core-daemon</feature>
        <!-- Kafka RPC (provides RpcClientFactory for LocationAwarePollerClient) -->
        <feature>opennms-core-ipc-rpc-kafka</feature>
        <!-- API bundles needed by onmsgi:reference proxies (class loading) -->
        <bundle>mvn:org.opennms.features.collection/org.opennms.features.collection.thresholding.api/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.collection/org.opennms.features.collection.api/${project.version}</bundle>
        <bundle>mvn:org.opennms/opennms-icmp-api/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.poller/org.opennms.features.poller.api/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.distributed/org.opennms.features.distributed.kv-store.api/${project.version}</bundle>
        <bundle>mvn:org.opennms/org.opennms.config-dao.poll-outages.api/${project.version}</bundle>
        <bundle>mvn:org.opennms.core.mate/org.opennms.core.mate.api/${project.version}</bundle>
        <!-- Poller RPC client (LocationAwarePollerClientImpl, PollerClientRpcModule) -->
        <bundle>mvn:org.opennms.features.poller/org.opennms.features.poller.client-rpc/${project.version}</bundle>
        <!-- Poller daemon classes (ServiceMonitor impls, DaemonTools) -->
        <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
        <!-- PerspectivePoller feature bundle -->
        <bundle>mvn:org.opennms.features/org.opennms.features.perspectivepoller/${project.version}</bundle>
        <!-- Daemon loader (creates PerspectivePollerd, starts it) -->
        <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-perspectivepoller/${project.version}</bundle>
    </feature>
```

- [ ] **Step 2: Commit**

```bash
git add container/features/src/main/resources/features.xml
git commit -m "feat(delta-v): add opennms-daemon-perspectivepoller Karaf feature"
```

### Task 5: Add to Sentinel assembly installedFeatures

**Files:**
- Modify: `features/container/sentinel/pom.xml:410`

- [ ] **Step 1: Add the feature to installedFeatures**

Add `<feature>opennms-daemon-perspectivepoller</feature>` after `opennms-daemon-bsmd` (line 410) in the `<installedFeatures>` block:

```xml
                        <feature>opennms-daemon-perspectivepoller</feature>
```

- [ ] **Step 2: Build container/features and sentinel assembly**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./compile.pl -DskipTests --projects org.opennms.karaf:opennms,org.opennms.features.container:sentinel -am install`
Expected: BUILD SUCCESS — all PerspectivePollerd JARs placed in `system/`

- [ ] **Step 3: Commit**

```bash
git add features/container/sentinel/pom.xml
git commit -m "feat(delta-v): add PerspectivePollerd to Sentinel assembly installedFeatures"
```

---

## Chunk 3: Docker Compose + Container Overlay

### Task 6: Create container overlay files

**Files:**
- Create: `opennms-container/delta-v/perspectivepollerd-overlay/etc/featuresBoot.d/perspectivepoller.boot`
- Create: `opennms-container/delta-v/perspectivepollerd-overlay/etc/org.opennms.core.health.cfg.cfg`

- [ ] **Step 1: Create the featuresBoot file**

Content:
```
opennms-daemon-perspectivepoller
opennms-health-rest-service
```

- [ ] **Step 2: Create the health ignore config**

Content (single line, no trailing newline):
```
ignoreBundleList=org.opennms.features.geocoder.nominatim,org.apache.karaf.diagnostic.boot,io.hawt.hawtio-karaf-terminal,org.opennms.features.events.sink.org.opennms.features.events.sink.dispatcher,org.opennms.features.distributed.org.opennms.features.distributed.datasource,org.opennms.features.distributed.core-impl,org.opennms.features.distributed.dao-impl,org.opennms.opennms-config
```

- [ ] **Step 3: Commit**

```bash
git add opennms-container/delta-v/perspectivepollerd-overlay/
git commit -m "feat(delta-v): add PerspectivePollerd container overlay (boot + health)"
```

### Task 7: Add perspectivepollerd service to docker-compose.yml

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml`

- [ ] **Step 1: Update the header comment**

Change line 2 from `16 services` to `18 services` (adding PerspectivePollerd). Update the service list comment to include `+ PerspectivePollerd`.

- [ ] **Step 2: Add perspectivepollerd service**

Insert BEFORE `alarmd:` (before line 604). This keeps daemon services in alphabetical-ish order (all the non-special daemons before alarmd which comes last as the most critical):

```yaml
  perspectivepollerd:
    profiles: [full]
    image: opennms/daemon:${VERSION}
    container_name: delta-v-perspectivepollerd
    hostname: perspectivepollerd
    depends_on:
      db-init:
        condition: service_completed_successfully
      kafka:
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
        -Dorg.opennms.tsid.node-id=7
      KAFKA_IPC_BOOTSTRAP_SERVERS: kafka:9092
    volumes:
      - perspectivepollerd-data:/opt/sentinel/data
      - ./perspectivepollerd-overlay/etc:/opt/sentinel-etc-overlay:ro
      # daemon-loader JAR
      - ../../core/daemon-loader-perspectivepoller/target/org.opennms.core.daemon-loader-perspectivepoller-36.0.0-SNAPSHOT.jar:/opt/sentinel/system/org/opennms/core/org.opennms.core.daemon-loader-perspectivepoller/36.0.0-SNAPSHOT/org.opennms.core.daemon-loader-perspectivepoller-36.0.0-SNAPSHOT.jar:ro
      # event-forwarder-kafka JAR
      - ../../core/event-forwarder-kafka/target/org.opennms.core.event-forwarder-kafka-36.0.0-SNAPSHOT.jar:/opt/sentinel/system/org/opennms/core/org.opennms.core.event-forwarder-kafka/36.0.0-SNAPSHOT/org.opennms.core.event-forwarder-kafka-36.0.0-SNAPSHOT.jar:ro
      # features.xml
      - ../../container/features/target/classes/features.xml:/opt/sentinel/system/org/opennms/karaf/opennms/36.0.0-SNAPSHOT/opennms-36.0.0-SNAPSHOT-features.xml:ro
      # events daemon JAR
      - ../../features/events/daemon/target/org.opennms.features.events.daemon-36.0.0-SNAPSHOT.jar:/opt/sentinel/system/org/opennms/features/events/org.opennms.features.events.daemon/36.0.0-SNAPSHOT/org.opennms.features.events.daemon-36.0.0-SNAPSHOT.jar:ro
    healthcheck:
      test: ["CMD-SHELL", "curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe || exit 1"]
      interval: 15s
      timeout: 10s
      retries: 20
      start_period: 90s

```

- [ ] **Step 3: Add named volume**

Add `perspectivepollerd-data:` to the `volumes:` section at the bottom, after `bsmd-data:`.

- [ ] **Step 4: Commit**

```bash
git add opennms-container/delta-v/docker-compose.yml
git commit -m "feat(delta-v): add perspectivepollerd service to docker-compose (TSID=7, 18 services)"
```

---

## Chunk 4: JMX Cleanup + Verification

### Task 8: Remove dead Notifd mbean from JMX config

**Files:**
- Modify: `opennms-base-assembly/src/main/filtered/etc/jmx-datacollection-config.xml:57-68`

- [ ] **Step 1: Delete the Notifd mbean block**

Remove lines 57-68 (the entire `<mbean name="OpenNMS.Notifd" ...>...</mbean>` block) from `jmx-datacollection-config.xml`.

- [ ] **Step 2: Commit**

```bash
git add opennms-base-assembly/src/main/filtered/etc/jmx-datacollection-config.xml
git commit -m "fix(delta-v): remove dead Notifd mbean from JMX datacollection config"
```

### Task 9: Verify no dead daemon references remain

- [ ] **Step 1: Grep for dead daemon names in active configs**

Run these greps against `opennms-base-assembly/src/main/filtered/etc/`:

```bash
grep -ri 'notifd\|queued\|vacuumd\|statsd\|actiond\|ackd' opennms-base-assembly/src/main/filtered/etc/ --include='*.xml' --include='*.properties' --include='*.cfg' | grep -v '<!--'
```

Expected: Zero matches (or only in commented-out lines)

- [ ] **Step 2: Verify promoteQueueData is absent**

```bash
grep -ri 'promoteQueueData' opennms-base-assembly/src/main/filtered/etc/
```

Expected: Zero matches

- [ ] **Step 3: Verify TSID node-id assignments are unique**

```bash
grep -r 'tsid.node-id' opennms-container/delta-v/docker-compose.yml
```

Expected: Each ID (1-17) appears exactly once, with 7 assigned to perspectivepollerd. Confirm no duplicates.

### Task 10: Build and start PerspectivePollerd container

- [ ] **Step 1: Rebuild the Sentinel assembly and Docker images**

```bash
cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install
cd ../..
# Build daemon Docker image
cd opennms-container/sentinel && docker build -t opennms/daemon:delta-v -t opennms/daemon:36.0.0-SNAPSHOT .
cd ../..
```

- [ ] **Step 2: Delete stale volume and start**

```bash
cd opennms-container/delta-v
docker volume rm delta-v_perspectivepollerd-data 2>/dev/null || true
COMPOSE_PROFILES=full docker compose up -d perspectivepollerd
```

- [ ] **Step 3: Check container health**

```bash
docker compose logs -f perspectivepollerd
# Wait for "Health Check passed" or check:
curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe
```

Expected: Container starts, Karaf boots, `opennms-daemon-perspectivepoller` feature installs, health probe returns OK.

- [ ] **Step 4: Final commit (if any fixes needed)**

If any adjustments were needed during verification, commit them.

---

## Implementation Notes

**Why `osgi:reference` for RpcClientFactory (not `onmsgi:reference`):**
The Kafka RPC implementation registers `RpcClientFactory` via Aries Blueprint `<service>` in the OSGi Framework registry. `onmsgi:reference` only searches the OpenNMS SOA `ServiceRegistry` (HashMap singleton), which won't find it. Must use `osgi:reference`.

**Why `DefaultSnmpCollectionAgentFactory` needs `NodeDao` and `IpInterfaceDao`:**
`AbstractCollectionAgentFactory` has `@Autowired` fields for these DAOs. Even though PerspectivePollerd doesn't actively call the factory at runtime, the constructor requires a non-null `CollectionAgentFactory`, and Spring's `annotation-config` will attempt to inject the `@Autowired` fields during bean creation.

**Why `KAFKA_IPC_BOOTSTRAP_SERVERS` (not `KAFKA_RPC_BOOTSTRAP_SERVERS`):**
The Sentinel entrypoint's `parseEnvironment()` only handles `KAFKA_IPC_*` prefix. Individual `KAFKA_RPC_*`/`KAFKA_SINK_*`/`KAFKA_TWIN_*` vars require confd + `minion-config.yaml`, which daemon containers don't use.

**Why `profiles: [full]` (not `[lite, full]`):**
PerspectivePollerd requires Minion RPC, which means there must be Minions at perspective locations. This is an advanced feature only relevant in the full deployment profile.
