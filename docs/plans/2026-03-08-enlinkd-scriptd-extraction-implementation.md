# Enlinkd & Scriptd Daemon Container Extraction Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Extract Enlinkd and Scriptd from the core container into standalone `opennms/daemon` containers using the proven daemon-loader pattern.

**Architecture:** Each daemon gets a new `core/daemon-loader-<name>/` module with a flat Spring XML context that wires the daemon to OSGi services. A new Karaf feature definition installs the daemon-loader bundle plus dependencies. Docker compose adds new service entries and disables the daemons on core.

**Tech Stack:** Java 17, Spring 4.2.x, OSGi/Karaf, Maven Bundle Plugin, Docker Compose

---

## Reference Files

Before starting any task, familiarize yourself with these reference implementations:

- **Reference daemon-loader POM:** `core/daemon-loader-alarmd/pom.xml`
- **Reference daemon-loader Spring XML:** `core/daemon-loader-alarmd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-alarmd.xml`
- **Reference Karaf feature:** Search `features.xml` for `opennms-daemon-alarmd`
- **Reference overlay:** `opennms-container/delta-v/alarmd-overlay/`
- **Enlinkd original Spring context:** `features/enlinkd/daemon/src/main/resources/META-INF/opennms/applicationContext-enhancedLinkd.xml`
- **Scriptd daemon class:** `opennms-services/src/main/java/org/opennms/netmgt/scriptd/Scriptd.java`
- **Core POM (module list):** `core/pom.xml` (lines 64-74)
- **Sentinel POM (installed features):** `features/container/sentinel/pom.xml` (lines 397-408)

---

## Task 1: Create daemon-loader-enlinkd POM

**Files:**
- Create: `core/daemon-loader-enlinkd/pom.xml`
- Modify: `core/pom.xml` (add module)

**Step 1: Create the POM file**

Create `core/daemon-loader-enlinkd/pom.xml` with this exact content:

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
    <artifactId>org.opennms.core.daemon-loader-enlinkd</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Core :: Daemon Loader :: Enlinkd</name>
    <description>
        Karaf-only Enlinkd loader. Creates and starts the EnhancedLinkd daemon in a
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
        <!-- Daemon base (AbstractServiceDaemon, DaemonLifecycleManager) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Enlinkd daemon classes (EnhancedLinkd, EventProcessor, topology updaters) -->
        <dependency>
            <groupId>org.opennms.features.enlinkd</groupId>
            <artifactId>org.opennms.features.enlinkd.daemon</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Enlinkd API (ReloadableTopologyDaemon, EnhancedLinkdConfig) -->
        <dependency>
            <groupId>org.opennms.features.enlinkd</groupId>
            <artifactId>org.opennms.features.enlinkd.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Enlinkd config (EnhancedLinkdConfigFactory) -->
        <dependency>
            <groupId>org.opennms.features.enlinkd</groupId>
            <artifactId>org.opennms.features.enlinkd.config</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Enlinkd service API (topology service interfaces) -->
        <dependency>
            <groupId>org.opennms.features.enlinkd</groupId>
            <artifactId>org.opennms.features.enlinkd.service.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Event API (EventIpcManager, EventSubscriptionService) -->
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

        <!-- SNMP proxy API (LocationAwareSnmpClient) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-snmp-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Topology DAO API (OnmsTopologyDao) -->
        <dependency>
            <groupId>org.opennms.features.topologies</groupId>
            <artifactId>org.opennms.features.topologies.service.api</artifactId>
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

**Step 2: Add module to core/pom.xml**

In `core/pom.xml`, add `<module>daemon-loader-enlinkd</module>` in the `<modules>` section, after the existing daemon-loader entries (after line 74, the `daemon-loader-trapd` line).

**Step 3: Create directory structure**

Run:
```bash
mkdir -p core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms
```

**Step 4: Verify the POM compiles**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home ./maven/bin/mvn -DskipTests -pl core/daemon-loader-enlinkd -am compile`

Expected: BUILD SUCCESS (may fail on missing Spring XML — that's fine, we'll create it next)

**Step 5: Commit**

```bash
git add core/daemon-loader-enlinkd/pom.xml core/pom.xml
git commit -m "feat: add daemon-loader-enlinkd Maven module"
```

---

## Task 2: Create Enlinkd daemon-loader Spring XML

**Files:**
- Create: `core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-enlinkd.xml`

**Step 1: Create the Spring context**

Create `core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-enlinkd.xml` with this exact content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  Karaf-only Enlinkd loader context.

  In a Karaf-only container (no Manager, no Eventd), this context replaces the
  multi-level Spring hierarchy with a flat context that wires EnhancedLinkd beans
  directly to OSGi services.

  All dependencies come from the OSGi service registry:
  - EventIpcManager/EventSubscriptionService from event-forwarder-kafka blueprint
  - DAOs from distributed-dao-impl (enlinkd DAOs shaded via XmlAppendingTransformer)
  - Topology services from distributed-dao-impl
  - LocationAwareSnmpClient from distributed-dao-impl
  - OnmsTopologyDao from distributed-dao-impl
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

    <!-- Enable @Autowired processing for EnhancedLinkd and EventProcessor -->
    <context:annotation-config />

    <!-- ServiceRegistry: import the shared instance from OSGi (registered by
         service-registry module) rather than creating a local one with
         <onmsgi:default-registry/> which would be a different, empty instance -->
    <osgi:reference id="serviceRegistry"
                    interface="org.opennms.core.soa.ServiceRegistry"/>

    <!-- ============================================================== -->
    <!-- OSGi Service References (from other Karaf features)            -->
    <!-- ============================================================== -->

    <!-- Event infrastructure (from opennms-event-forwarder-kafka blueprint) -->
    <osgi:reference id="eventIpcManager"
                    interface="org.opennms.netmgt.events.api.EventIpcManager"/>

    <osgi:reference id="eventSubscriptionService"
                    interface="org.opennms.netmgt.events.api.EventSubscriptionService"/>

    <!-- Enlinkd topology services (from distributed-dao-impl, shaded via XmlAppendingTransformer) -->
    <onmsgi:reference id="nodeTopologyService"
                      interface="org.opennms.netmgt.enlinkd.service.api.NodeTopologyService"/>
    <onmsgi:reference id="bridgeTopologyService"
                      interface="org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService"/>
    <onmsgi:reference id="cdpTopologyService"
                      interface="org.opennms.netmgt.enlinkd.service.api.CdpTopologyService"/>
    <onmsgi:reference id="isisTopologyService"
                      interface="org.opennms.netmgt.enlinkd.service.api.IsisTopologyService"/>
    <onmsgi:reference id="ipNetToMediaTopologyService"
                      interface="org.opennms.netmgt.enlinkd.service.api.IpNetToMediaTopologyService"/>
    <onmsgi:reference id="lldpTopologyService"
                      interface="org.opennms.netmgt.enlinkd.service.api.LldpTopologyService"/>
    <onmsgi:reference id="ospfTopologyService"
                      interface="org.opennms.netmgt.enlinkd.service.api.OspfTopologyService"/>
    <onmsgi:reference id="userDefinedLinkTopologyService"
                      interface="org.opennms.netmgt.enlinkd.service.api.UserDefinedLinkTopologyService"/>

    <!-- Topology DAO (from distributed-dao-impl) -->
    <onmsgi:reference id="onmsTopologyDao"
                      interface="org.opennms.netmgt.topologies.service.api.OnmsTopologyDao"/>

    <!-- SNMP client (from distributed-dao-impl, location-aware — proxies via Minion RPC for non-default locations) -->
    <onmsgi:reference id="locationAwareSnmpClient"
                      interface="org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient"/>

    <!-- ============================================================== -->
    <!-- Enlinkd Configuration                                         -->
    <!-- ============================================================== -->

    <!-- EnhancedLinkdConfigFactory: reads enlinkd-configuration.xml from ${opennms.home}/etc/ -->
    <bean id="linkdConfig" class="org.opennms.netmgt.config.EnhancedLinkdConfigFactory" lazy-init="true"/>

    <!-- ============================================================== -->
    <!-- Topology Updaters                                              -->
    <!-- ============================================================== -->

    <bean id="nodesOnmsTopologyUpdater" class="org.opennms.netmgt.enlinkd.NodesOnmsTopologyUpdater">
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="networkRouterTopologyUpdater" class="org.opennms.netmgt.enlinkd.NetworkRouterTopologyUpdater">
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="userDefinedLinkTopologyUpdater" class="org.opennms.netmgt.enlinkd.UserDefinedLinkTopologyUpdater">
        <constructor-arg ref="userDefinedLinkTopologyService" />
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="bridgeOnmsTopologyUpdater" class="org.opennms.netmgt.enlinkd.BridgeOnmsTopologyUpdater">
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="bridgeTopologyService" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="cdpOnmsTopologyUpdater" class="org.opennms.netmgt.enlinkd.CdpOnmsTopologyUpdater">
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="cdpTopologyService" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="isisOnmsTopologyUpdater" class="org.opennms.netmgt.enlinkd.IsisOnmsTopologyUpdater">
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="isisTopologyService" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="lldpOnmsTopologyUpdater" class="org.opennms.netmgt.enlinkd.LldpOnmsTopologyUpdater">
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="lldpTopologyService" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="ospfOnmsTopologyUpdater" class="org.opennms.netmgt.enlinkd.OspfOnmsTopologyUpdater">
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="ospfTopologyService" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="ospfAreaOnmsTopologyUpdater" class="org.opennms.netmgt.enlinkd.OspfAreaOnmsTopologyUpdater">
        <constructor-arg ref="onmsTopologyDao" />
        <constructor-arg ref="ospfTopologyService" />
        <constructor-arg ref="nodeTopologyService" />
    </bean>

    <bean id="discoveryBridgeDomain" class="org.opennms.netmgt.enlinkd.DiscoveryBridgeDomains">
        <constructor-arg ref="bridgeTopologyService" />
    </bean>

    <!-- ============================================================== -->
    <!-- Enlinkd Daemon                                                 -->
    <!-- ============================================================== -->

    <bean name="daemon" class="org.opennms.netmgt.enlinkd.EnhancedLinkd">
        <property name="linkdConfig"    ref="linkdConfig" />
        <property name="queryManager"   ref="nodeTopologyService" />
        <property name="bridgeTopologyService"   ref="bridgeTopologyService" />
        <property name="cdpTopologyService"   ref="cdpTopologyService" />
        <property name="isisTopologyService"   ref="isisTopologyService" />
        <property name="ipNetToMediaTopologyService"   ref="ipNetToMediaTopologyService" />
        <property name="lldpTopologyService"   ref="lldpTopologyService" />
        <property name="ospfTopologyService"   ref="ospfTopologyService" />
    </bean>

    <!-- Event handler: listens for node add/delete/service events -->
    <bean id="receiver" class="org.opennms.netmgt.enlinkd.EventProcessor" init-method="init">
        <property name="linkd" ref="daemon"/>
    </bean>

    <!-- @EventHandler adapter for daemon event subscriptions -->
    <bean id="daemonListener" class="org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter">
        <property name="annotatedListener" ref="receiver" />
        <property name="eventSubscriptionService" ref="eventSubscriptionService" />
    </bean>

    <!-- ============================================================== -->
    <!-- Daemon Lifecycle (replaces Manager)                            -->
    <!-- ============================================================== -->

    <bean name="daemonLifecycleManager"
          class="org.opennms.core.daemon.loader.DaemonLifecycleManager">
        <constructor-arg ref="daemon"/>
    </bean>

</beans>
```

**Step 2: Verify compilation**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-enlinkd -am install`

Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-enlinkd.xml
git commit -m "feat: add Enlinkd daemon-loader Spring context"
```

---

## Task 3: Add Enlinkd Karaf feature and Sentinel POM entry

**Files:**
- Modify: `container/features/src/main/resources/features.xml`
- Modify: `features/container/sentinel/pom.xml`

**Step 1: Add Karaf feature definition to features.xml**

In `container/features/src/main/resources/features.xml`, add the following feature definition just before the closing `</features>` tag (before the `opennms-daemon-alarmd` feature, or after it — order doesn't matter, but keeping daemon features together is cleaner). Insert it immediately before the `opennms-daemon-alarmd` feature definition:

```xml
    <!-- Karaf-only Enlinkd: loads EnhancedLinkd daemon (LLDP, CDP, OSPF, IS-IS, Bridge topology discovery) without Manager/Eventd -->
    <feature name="opennms-daemon-enlinkd" version="${project.version}"
             description="OpenNMS :: Daemon Loader :: Enlinkd">
        <!-- Spring DM Extender (processes Spring-Context manifest headers) -->
        <feature>opennms-spring-extender</feature>
        <!-- Event transport (Kafka-based, replaces Eventd) -->
        <feature>opennms-event-forwarder-kafka</feature>
        <!-- DAO infrastructure (includes shaded enlinkd persistence + service DAOs) -->
        <feature>opennms-distributed-core-impl</feature>
        <feature>opennms-persistence</feature>
        <!-- Enlinkd config -->
        <feature>opennms-config</feature>
        <!-- Enlinkd API -->
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.api/${project.version}</bundle>
        <!-- Enlinkd daemon (EnhancedLinkd, EventProcessor, topology updaters) -->
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.daemon/${project.version}</bundle>
        <!-- Enlinkd adapter bundles (collectors, updaters, discovers) -->
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.common/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.collectors.bridge/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.collectors.cdp/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.collectors.ipnettomedia/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.collectors.isis/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.collectors.lldp/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.collectors.ospf/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.discovers.bridge/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.updaters.bridge/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.updaters.cdp/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.updaters.isis/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.updaters.lldp/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.updaters.nodes/${project.version}</bundle>
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.adapters.updaters.ospf/${project.version}</bundle>
        <!-- Enlinkd config factory -->
        <bundle>mvn:org.opennms.features.enlinkd/org.opennms.features.enlinkd.config/${project.version}</bundle>
        <!-- Topology service API (OnmsTopologyDao interface) -->
        <bundle>mvn:org.opennms.features.topologies/org.opennms.features.topologies.service.api/${project.version}</bundle>
        <!-- SNMP proxy (LocationAwareSnmpClient) -->
        <bundle>mvn:org.opennms/opennms-snmp-api/${project.version}</bundle>
        <!-- Daemon loader (creates EnhancedLinkd, starts it) -->
        <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-enlinkd/${project.version}</bundle>
    </feature>
```

**Step 2: Add installed feature to Sentinel POM**

In `features/container/sentinel/pom.xml`, add `<feature>opennms-daemon-enlinkd</feature>` after the existing daemon feature entries (after line 408, the `opennms-daemon-passivestatusd` line).

**Step 3: Verify compilation**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-enlinkd,:container-features-opennms -am install`

Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add container/features/src/main/resources/features.xml features/container/sentinel/pom.xml
git commit -m "feat: add opennms-daemon-enlinkd Karaf feature"
```

---

## Task 4: Add Enlinkd Docker Compose service and overlay

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml`
- Create: `opennms-container/delta-v/enlinkd-overlay/etc/featuresBoot.d/enlinkd.boot`
- Create: `opennms-container/delta-v/enlinkd-overlay/etc/org.opennms.core.health.cfg.cfg`
- Create: `opennms-container/delta-v/enlinkd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- Create: `opennms-container/delta-v/enlinkd-overlay/etc/org.opennms.features.kafka.producer.client.cfg`

**Step 1: Create overlay directory and files**

Create `opennms-container/delta-v/enlinkd-overlay/etc/featuresBoot.d/enlinkd.boot`:
```
opennms-daemon-enlinkd
opennms-health-rest-service
```

Create `opennms-container/delta-v/enlinkd-overlay/etc/org.opennms.core.health.cfg.cfg`:
```
ignoreBundleList=org.opennms.features.geocoder.nominatim,org.apache.karaf.diagnostic.boot,io.hawt.hawtio-karaf-terminal,org.opennms.features.events.sink.org.opennms.features.events.sink.dispatcher
```

Create `opennms-container/delta-v/enlinkd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`:
```
# Kafka Event Forwarder configuration for standalone Enlinkd (daemon container)
bootstrap.servers=kafka:9092
topic.name=opennms-fault-events
consumer.group.id=opennms-enlinkd
poll.timeout.ms=100
```

Create `opennms-container/delta-v/enlinkd-overlay/etc/org.opennms.features.kafka.producer.client.cfg`:
```
bootstrap.servers=kafka:9092
```

**Step 2: Add enlinkd service to docker-compose.yml**

Add the following service definition after the `eventtranslator` service (before the `alarmd` service). Also add `enlinkd-data:` to the `volumes:` section at the bottom.

Add `CORE_SERVICE_ENLINKD_ENABLED: "false"` to the core container's environment section (after the existing `CORE_SERVICE_EVENTTRANSLATOR_ENABLED` line).

```yaml
  enlinkd:
    image: opennms/daemon:${VERSION}
    container_name: delta-v-enlinkd
    hostname: enlinkd
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
        -Dorg.opennms.tsid.node-id=14
    volumes:
      - enlinkd-data:/opt/daemon/data
      - ./enlinkd-overlay/etc:/opt/daemon-etc-overlay:ro
    healthcheck:
      test: ["CMD-SHELL", "curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe || exit 1"]
      interval: 15s
      timeout: 10s
      retries: 20
      start_period: 60s
```

**Step 3: Commit**

```bash
git add opennms-container/delta-v/enlinkd-overlay/ opennms-container/delta-v/docker-compose.yml
git commit -m "feat: add Enlinkd standalone daemon container to Delta-V compose"
```

---

## Task 5: Full compilation checkpoint for Enlinkd

**Step 1: Full compile**

Run: `./compile.pl -DskipTests`

Expected: BUILD SUCCESS

If it fails, diagnose the error. Common issues:
- Missing Maven dependency in daemon-loader POM (add the missing artifact)
- Wrong interface name in `<onmsgi:reference>` (check the exact interface in the service API module)
- Missing bundle in Karaf feature (add the missing `<bundle>` entry)

**Step 2: Commit any fixes**

If any fixes were needed, commit them:
```bash
git add -u
git commit -m "fix: resolve Enlinkd daemon-loader compilation issues"
```

---

## Task 6: Create daemon-loader-scriptd POM

**Files:**
- Create: `core/daemon-loader-scriptd/pom.xml`
- Modify: `core/pom.xml` (add module)

**Step 1: Create the POM file**

Create `core/daemon-loader-scriptd/pom.xml` with this exact content:

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
    <artifactId>org.opennms.core.daemon-loader-scriptd</artifactId>
    <packaging>bundle</packaging>
    <name>OpenNMS :: Core :: Daemon Loader :: Scriptd</name>
    <description>
        Karaf-only Scriptd loader. Creates and starts the Scriptd daemon in a
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
        <!-- Daemon base (AbstractServiceDaemon, DaemonLifecycleManager) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Scriptd classes (Scriptd, Executor, BroadcastEventProcessor) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-services</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Event API (EventIpcManager, EventIpcManagerFactory) -->
        <dependency>
            <groupId>org.opennms.features.events</groupId>
            <artifactId>org.opennms.features.events.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- DAO APIs (NodeDao, SessionUtils) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-dao-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Config (ScriptdConfigFactory) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-config</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- BeanUtils (for BeanUtils.setStaticApplicationContext) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.spring</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Spring (for ApplicationContext, InitializingBean) -->
        <dependency>
            <groupId>org.apache.servicemix.bundles</groupId>
            <artifactId>org.apache.servicemix.bundles.spring-beans</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.servicemix.bundles</groupId>
            <artifactId>org.apache.servicemix.bundles.spring-context</artifactId>
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

**Step 2: Add module to core/pom.xml**

In `core/pom.xml`, add `<module>daemon-loader-scriptd</module>` after `daemon-loader-enlinkd`.

**Step 3: Create directory structure**

Run:
```bash
mkdir -p core/daemon-loader-scriptd/src/main/java/org/opennms/core/daemon/loader
mkdir -p core/daemon-loader-scriptd/src/main/resources/META-INF/opennms
```

**Step 4: Commit**

```bash
git add core/daemon-loader-scriptd/pom.xml core/pom.xml
git commit -m "feat: add daemon-loader-scriptd Maven module"
```

---

## Task 7: Create ScriptdContextInitializer Java class

**Files:**
- Create: `core/daemon-loader-scriptd/src/main/java/org/opennms/core/daemon/loader/ScriptdContextInitializer.java`

**Context:** Scriptd uses `BeanUtils.getBeanFactory("daoContext")` to look up `NodeDao` and `SessionUtils`. `BeanUtils.getBeanFactory()` checks if a static `ApplicationContext` was set via `setStaticApplicationContext()`. If it was, it returns that context directly (ignoring the contextId parameter). So we inject the daemon-loader's own `ApplicationContext` into `BeanUtils`, and Scriptd's bean lookups will resolve against it.

Scriptd also uses `EventIpcManagerFactory.getIpcManager()` to subscribe to events. We must call `EventIpcManagerFactory.setIpcManager()` with our Kafka-backed `EventIpcManager`.

**Step 1: Create the initializer class**

Create `core/daemon-loader-scriptd/src/main/java/org/opennms/core/daemon/loader/ScriptdContextInitializer.java`:

```java
/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.daemon.loader;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Initializes static singletons that Scriptd depends on:
 * <ol>
 *   <li>{@link BeanUtils#setStaticApplicationContext} — so Scriptd's
 *       {@code BeanUtils.getBeanFactory("daoContext")} resolves beans from
 *       the daemon-loader's own ApplicationContext (which has nodeDao,
 *       sessionUtils, etc.)</li>
 *   <li>{@link EventIpcManagerFactory#setIpcManager} — so Scriptd's
 *       {@code BroadcastEventProcessor} can subscribe to events via the
 *       Kafka-backed EventIpcManager</li>
 * </ol>
 */
public class ScriptdContextInitializer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptdContextInitializer.class);

    private ApplicationContext applicationContext;
    private EventIpcManager eventIpcManager;

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        this.eventIpcManager = eventIpcManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        LOG.info("Initializing BeanUtils.setStaticApplicationContext for Scriptd");
        BeanUtils.setStaticApplicationContext(applicationContext);

        LOG.info("Initializing EventIpcManagerFactory.setIpcManager for Scriptd");
        EventIpcManagerFactory.setIpcManager(eventIpcManager);
    }
}
```

**Step 2: Commit**

```bash
git add core/daemon-loader-scriptd/src/main/java/org/opennms/core/daemon/loader/ScriptdContextInitializer.java
git commit -m "feat: add ScriptdContextInitializer for BeanUtils and EventIpcManagerFactory"
```

---

## Task 8: Create Scriptd daemon-loader Spring XML

**Files:**
- Create: `core/daemon-loader-scriptd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-scriptd.xml`

**Step 1: Create the Spring context**

Create `core/daemon-loader-scriptd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-scriptd.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  Karaf-only Scriptd loader context.

  In a Karaf-only container (no Manager, no Eventd), this context replaces the
  legacy singleton/JMX lifecycle with a flat context that wires Scriptd
  directly to OSGi services.

  Scriptd uses two legacy static APIs:
  1. BeanUtils.getBeanFactory("daoContext") — for NodeDao and SessionUtils
  2. EventIpcManagerFactory.getIpcManager() — for event subscription

  ScriptdContextInitializer bridges both by:
  - Setting BeanUtils.staticApplicationContext to this context (so bean lookups resolve here)
  - Setting EventIpcManagerFactory.ipcManager to the Kafka-backed EventIpcManager

  All dependencies come from the OSGi service registry:
  - EventIpcManager from KafkaEventIpcManagerAdapter (event-forwarder-kafka feature)
  - NodeDao and SessionUtils from distributed-dao-impl
-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:osgi="http://www.springframework.org/schema/osgi"
       xmlns:onmsgi="http://xmlns.opennms.org/xsd/spring/onms-osgi"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
           http://www.springframework.org/schema/osgi http://www.springframework.org/schema/osgi/spring-osgi.xsd
           http://xmlns.opennms.org/xsd/spring/onms-osgi http://xmlns.opennms.org/xsd/spring/onms-osgi.xsd
       ">

    <!-- ============================================================== -->
    <!-- OSGi Service References (from other Karaf features)            -->
    <!-- ============================================================== -->

    <!-- Event infrastructure (from opennms-event-forwarder-kafka blueprint) -->
    <osgi:reference id="eventIpcManager"
                    interface="org.opennms.netmgt.events.api.EventIpcManager"/>

    <!-- DAOs needed by Scriptd's Executor (fetched via BeanUtils.getBeanFactory("daoContext")) -->
    <onmsgi:reference id="nodeDao"
                      interface="org.opennms.netmgt.dao.api.NodeDao"/>
    <onmsgi:reference id="sessionUtils"
                      interface="org.opennms.netmgt.dao.api.SessionUtils"/>

    <!-- ============================================================== -->
    <!-- Static Singleton Initialization                                -->
    <!-- ============================================================== -->

    <!-- Bridge legacy static APIs to OSGi services.
         Must run before Scriptd.onInit() which calls BeanUtils and EventIpcManagerFactory. -->
    <bean id="scriptdContextInitializer"
          class="org.opennms.core.daemon.loader.ScriptdContextInitializer">
        <property name="eventIpcManager" ref="eventIpcManager"/>
    </bean>

    <!-- ============================================================== -->
    <!-- Scriptd Daemon                                                 -->
    <!-- ============================================================== -->

    <!-- Scriptd is a singleton with a private constructor. Use getInstance(). -->
    <bean id="daemon" class="org.opennms.netmgt.scriptd.Scriptd"
          factory-method="getInstance"
          depends-on="scriptdContextInitializer"/>

    <!-- ============================================================== -->
    <!-- Daemon Lifecycle (replaces Manager)                            -->
    <!-- ============================================================== -->

    <bean name="daemonLifecycleManager"
          class="org.opennms.core.daemon.loader.DaemonLifecycleManager">
        <constructor-arg ref="daemon"/>
    </bean>

</beans>
```

**Step 2: Verify compilation**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-scriptd -am install`

Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add core/daemon-loader-scriptd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-scriptd.xml
git commit -m "feat: add Scriptd daemon-loader Spring context"
```

---

## Task 9: Add Scriptd Karaf feature and Sentinel POM entry

**Files:**
- Modify: `container/features/src/main/resources/features.xml`
- Modify: `features/container/sentinel/pom.xml`

**Step 1: Add Karaf feature definition to features.xml**

Insert the following feature definition near the other daemon features (before or after `opennms-daemon-enlinkd`):

```xml
    <!-- Karaf-only Scriptd: loads Scriptd daemon (BSF script execution on events) without Manager/Eventd -->
    <feature name="opennms-daemon-scriptd" version="${project.version}"
             description="OpenNMS :: Daemon Loader :: Scriptd">
        <!-- Spring DM Extender (processes Spring-Context manifest headers) -->
        <feature>opennms-spring-extender</feature>
        <!-- Event transport (Kafka-based, replaces Eventd) -->
        <feature>opennms-event-forwarder-kafka</feature>
        <!-- DAO infrastructure (provides NodeDao, SessionUtils) -->
        <feature>opennms-distributed-core-impl</feature>
        <feature>opennms-persistence</feature>
        <!-- Config (ScriptdConfigFactory) -->
        <feature>opennms-config</feature>
        <!-- Scriptd classes (inside opennms-services monolithic bundle) -->
        <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
        <!-- Daemon loader (creates Scriptd, starts it) -->
        <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-scriptd/${project.version}</bundle>
    </feature>
```

**Step 2: Add installed feature to Sentinel POM**

In `features/container/sentinel/pom.xml`, add `<feature>opennms-daemon-scriptd</feature>` after `opennms-daemon-enlinkd`.

**Step 3: Verify compilation**

Run: `./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-scriptd,:container-features-opennms -am install`

Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add container/features/src/main/resources/features.xml features/container/sentinel/pom.xml
git commit -m "feat: add opennms-daemon-scriptd Karaf feature"
```

---

## Task 10: Add Scriptd Docker Compose service and overlay

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml`
- Create: `opennms-container/delta-v/scriptd-overlay/etc/featuresBoot.d/scriptd.boot`
- Create: `opennms-container/delta-v/scriptd-overlay/etc/org.opennms.core.health.cfg.cfg`
- Create: `opennms-container/delta-v/scriptd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`
- Create: `opennms-container/delta-v/scriptd-overlay/etc/org.opennms.features.kafka.producer.client.cfg`

**Step 1: Create overlay directory and files**

Create `opennms-container/delta-v/scriptd-overlay/etc/featuresBoot.d/scriptd.boot`:
```
opennms-daemon-scriptd
opennms-health-rest-service
```

Create `opennms-container/delta-v/scriptd-overlay/etc/org.opennms.core.health.cfg.cfg`:
```
ignoreBundleList=org.opennms.features.geocoder.nominatim,org.apache.karaf.diagnostic.boot,io.hawt.hawtio-karaf-terminal,org.opennms.features.events.sink.org.opennms.features.events.sink.dispatcher
```

Create `opennms-container/delta-v/scriptd-overlay/etc/org.opennms.core.event.forwarder.kafka.cfg`:
```
# Kafka Event Forwarder configuration for standalone Scriptd (daemon container)
bootstrap.servers=kafka:9092
topic.name=opennms-fault-events
consumer.group.id=opennms-scriptd
poll.timeout.ms=100
```

Create `opennms-container/delta-v/scriptd-overlay/etc/org.opennms.features.kafka.producer.client.cfg`:
```
bootstrap.servers=kafka:9092
```

**Step 2: Add scriptd service to docker-compose.yml**

Add the following service definition after the enlinkd service. Also add `scriptd-data:` to the `volumes:` section at the bottom.

Add `CORE_SERVICE_SCRIPTD_ENABLED: "false"` to the core container's environment section (after `CORE_SERVICE_ENLINKD_ENABLED`).

```yaml
  scriptd:
    image: opennms/daemon:${VERSION}
    container_name: delta-v-scriptd
    hostname: scriptd
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
        -Dorg.opennms.tsid.node-id=15
    volumes:
      - scriptd-data:/opt/daemon/data
      - ./scriptd-overlay/etc:/opt/daemon-etc-overlay:ro
    healthcheck:
      test: ["CMD-SHELL", "curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe || exit 1"]
      interval: 15s
      timeout: 10s
      retries: 20
      start_period: 60s
```

**Step 3: Commit**

```bash
git add opennms-container/delta-v/scriptd-overlay/ opennms-container/delta-v/docker-compose.yml
git commit -m "feat: add Scriptd standalone daemon container to Delta-V compose"
```

---

## Task 11: Full compilation checkpoint for both daemons

**Step 1: Full compile**

Run: `./compile.pl -DskipTests`

Expected: BUILD SUCCESS

If it fails, diagnose the error. Common issues:
- Missing Maven dependency in daemon-loader POM (add the missing artifact)
- Wrong interface name in Spring XML (check the exact interface in the service API module)
- Missing bundle in Karaf feature (add the missing `<bundle>` entry)
- OSGi import resolution issues (ensure `DynamicImport-Package: *` is in the POM)

**Step 2: Commit any fixes**

If any fixes were needed, commit them:
```bash
git add -u
git commit -m "fix: resolve compilation issues for Enlinkd/Scriptd daemon-loaders"
```

---

## Summary of Deliverables

After completing all tasks:

| Artifact | Path |
|----------|------|
| Enlinkd daemon-loader POM | `core/daemon-loader-enlinkd/pom.xml` |
| Enlinkd daemon-loader Spring XML | `core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-enlinkd.xml` |
| Scriptd daemon-loader POM | `core/daemon-loader-scriptd/pom.xml` |
| ScriptdContextInitializer | `core/daemon-loader-scriptd/src/main/java/org/opennms/core/daemon/loader/ScriptdContextInitializer.java` |
| Scriptd daemon-loader Spring XML | `core/daemon-loader-scriptd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-scriptd.xml` |
| Karaf features | `container/features/src/main/resources/features.xml` (2 new features) |
| Sentinel POM | `features/container/sentinel/pom.xml` (2 new installed features) |
| Core POM | `core/pom.xml` (2 new modules) |
| Enlinkd overlay | `opennms-container/delta-v/enlinkd-overlay/` |
| Scriptd overlay | `opennms-container/delta-v/scriptd-overlay/` |
| Docker compose | `opennms-container/delta-v/docker-compose.yml` (2 new services, 2 disabled on core) |

Total new containers: **Enlinkd (TSID=14)** and **Scriptd (TSID=15)**, both on `opennms/daemon:strike-fighter` image.
