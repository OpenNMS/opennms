# Minion-Mandatory RPC Migration Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all Local*Client stubs in daemon containers with real LocationAware*ClientImpl RPC implementations backed by KafkaRpcClientFactory, making Minion delegation mandatory.

**Architecture:** A shared Spring XML fragment (`core/daemon-loader-shared/`) provides KafkaRpcClientFactory wiring that all daemon-loaders import. A `force-remote` flag in KafkaRpcClientFactory ensures daemon containers never execute RPC locally. Each daemon's local stub is replaced with the real RPC implementation, one daemon at a time.

**Tech Stack:** Spring XML, KafkaRpcClientFactory, OSGi bundles (Karaf), Docker Compose

**Design Spec:** `docs/plans/2026-03-12-minion-mandatory-rpc-migration-design.md`

---

## Chunk 1: Infrastructure

### Task 1: Create `core/daemon-loader-shared` module

This module provides the shared `kafka-rpc-client-factory.xml` Spring XML fragment and the consolidated `NoOpTracerRegistry` class. All daemon-loaders that use Kafka RPC will import this fragment.

**Files:**
- Create: `core/daemon-loader-shared/pom.xml`
- Create: `core/daemon-loader-shared/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java`
- Create: `core/daemon-loader-shared/src/main/java/org/opennms/core/daemon/loader/LocalServiceDetectorRegistry.java`
- Create: `core/daemon-loader-shared/src/main/resources/kafka-rpc-client-factory.xml`
- Modify: `core/pom.xml` (add module)

- [ ] **Step 1: Create POM**

Create `core/daemon-loader-shared/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.opennms</groupId>
        <artifactId>org.opennms.core</artifactId>
        <version>36.0.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.opennms.core</groupId>
    <artifactId>org.opennms.core.daemon-loader-shared</artifactId>
    <name>OpenNMS :: Core :: Daemon Loader Shared</name>
    <packaging>bundle</packaging>

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
                    </instructions>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <!-- TracerRegistry interface -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.tracing.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- OpenTracing API (for GlobalTracer) -->
        <dependency>
            <groupId>io.opentracing</groupId>
            <artifactId>opentracing-api</artifactId>
            <version>${opentracingVersion}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>io.opentracing</groupId>
            <artifactId>opentracing-util</artifactId>
            <version>${opentracingVersion}</version>
            <scope>provided</scope>
        </dependency>
        <!-- ServiceDetectorRegistry + ServiceDetectorFactory interfaces -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-provision-api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- SLF4J for logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Create NoOpTracerRegistry**

Create `core/daemon-loader-shared/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java`:

```java
package org.opennms.core.daemon.loader;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.opennms.core.tracing.api.TracerRegistry;

/**
 * No-op TracerRegistry for standalone daemon containers.
 * Returns the GlobalTracer (which defaults to NoopTracer).
 * Satisfies KafkaRpcClientFactory's @Autowired TracerRegistry.
 */
public class NoOpTracerRegistry implements TracerRegistry {

    @Override
    public Tracer getTracer() {
        return GlobalTracer.get();
    }

    @Override
    public void init(String serviceName) {
        // No-op — standalone daemon containers don't use distributed tracing
    }
}
```

- [ ] **Step 3: Create LocalServiceDetectorRegistry**

Create `core/daemon-loader-shared/src/main/java/org/opennms/core/daemon/loader/LocalServiceDetectorRegistry.java`:

```java
package org.opennms.core.daemon.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.ServiceDetectorFactory;
import org.opennms.netmgt.provision.ServiceDetectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local ServiceDetectorRegistry for standalone daemon containers.
 * Discovers ServiceDetectorFactory implementations via ServiceLoader.
 * Used by both Provisiond and Discovery daemon-loaders.
 */
public class LocalServiceDetectorRegistry implements ServiceDetectorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(LocalServiceDetectorRegistry.class);
    private final Map<String, ServiceDetectorFactory<?>> factoryByClassName = new HashMap<>();

    public LocalServiceDetectorRegistry() {
        ServiceLoader<ServiceDetectorFactory> loader = ServiceLoader.load(ServiceDetectorFactory.class);
        for (ServiceDetectorFactory<?> factory : loader) {
            String className = factory.getDetectorClass().getCanonicalName();
            factoryByClassName.put(className, factory);
            LOG.info("Registered detector factory: {} -> {}", className, factory.getClass().getCanonicalName());
        }
        LOG.info("Loaded {} detector factories via ServiceLoader", factoryByClassName.size());
    }

    @Override
    public Map<String, String> getTypes() {
        Map<String, String> types = new HashMap<>();
        for (Map.Entry<String, ServiceDetectorFactory<?>> entry : factoryByClassName.entrySet()) {
            types.put(entry.getKey(), entry.getValue().getDetectorClass().getCanonicalName());
        }
        return types;
    }

    @Override
    public ServiceDetectorFactory<?> getDetectorFactoryByClassName(String className) {
        return factoryByClassName.get(className);
    }
}
```

- [ ] **Step 4: Create shared Spring XML fragment**

Create `core/daemon-loader-shared/src/main/resources/kafka-rpc-client-factory.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  Shared KafkaRpcClientFactory wiring for standalone daemon containers.

  CONTRACT: The importing daemon-loader context MUST provide:
    - distPollerDao (onmsgi:reference to DistPollerDao)
    - <context:annotation-config/> (for @Autowired processing)

  This fragment provides:
    - rpcIdentity, rpcLocation (from distPollerDao.whoami())
    - kafkaRpcMetricRegistry (MetricRegistry for RPC monitoring)
    - rpcTargetHelper (routes RPC calls to correct Minion location)
    - tracerRegistry (NoOpTracerRegistry for KafkaRpcClientFactory's @Autowired)
    - rpcClientFactory (KafkaRpcClientFactory with start/stop lifecycle)

  KafkaRpcClientFactory reads bootstrap.servers from system property:
    -Dorg.opennms.core.ipc.rpc.kafka.bootstrap.servers=kafka:9092
-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
       ">

    <!-- Identity resolution from DistPollerDao -->
    <bean id="rpcIdentity" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="targetObject" ref="distPollerDao"/>
        <property name="targetMethod" value="whoami"/>
    </bean>
    <bean id="rpcLocation" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="targetObject" ref="rpcIdentity"/>
        <property name="targetMethod" value="getLocation"/>
    </bean>

    <!-- Shared infrastructure -->
    <bean id="kafkaRpcMetricRegistry" class="com.codahale.metrics.MetricRegistry"/>
    <bean id="rpcTargetHelper" class="org.opennms.core.rpc.utils.RpcTargetHelper"/>

    <!-- No-op tracer (KafkaRpcClientFactory has @Autowired TracerRegistry) -->
    <bean id="tracerRegistry"
          class="org.opennms.core.daemon.loader.NoOpTracerRegistry"/>

    <!-- KafkaRpcClientFactory: reads bootstrap.servers from system property
         org.opennms.core.ipc.rpc.kafka.bootstrap.servers -->
    <bean id="rpcClientFactory"
          class="org.opennms.core.ipc.rpc.kafka.KafkaRpcClientFactory"
          init-method="start" destroy-method="stop">
        <property name="location" ref="rpcLocation"/>
        <property name="metrics" ref="kafkaRpcMetricRegistry"/>
    </bean>

</beans>
```

- [ ] **Step 5: Add module to core/pom.xml**

In `core/pom.xml`, add `<module>daemon-loader-shared</module>` in the modules section, immediately before the other daemon-loader modules. Insert after line 64 (`<module>encrypt-util</module>`), before `<module>daemon-loader-alarmd</module>`:

```xml
    <module>daemon-loader-shared</module>
    <module>daemon-loader-alarmd</module>
```

- [ ] **Step 6: Build and verify**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-shared -am install
```

Expected: BUILD SUCCESS. The JAR should contain `NoOpTracerRegistry.class` and `kafka-rpc-client-factory.xml`.

- [ ] **Step 7: Commit**

```bash
git add core/daemon-loader-shared/ core/pom.xml
git commit -m "feat(delta-v): add daemon-loader-shared module with KafkaRpcClientFactory XML fragment

Provides shared kafka-rpc-client-factory.xml Spring XML fragment,
NoOpTracerRegistry, and LocalServiceDetectorRegistry for all
daemon-loaders that use Kafka RPC."
```

---

### Task 2: Add force-remote flag to KafkaRpcClientFactory

Add a system property `org.opennms.core.ipc.rpc.force-remote` that prevents local RPC execution in daemon containers. When true, all RPC requests route through Kafka to a Minion, even when the location matches the daemon's own location.

**Files:**
- Modify: `core/ipc/rpc/kafka/src/main/java/org/opennms/core/ipc/rpc/kafka/KafkaRpcClientFactory.java`

- [ ] **Step 1: Modify getClient() method**

In `core/ipc/rpc/kafka/src/main/java/org/opennms/core/ipc/rpc/kafka/KafkaRpcClientFactory.java`, find the `getClient()` method (around line 158). Replace the local-execution check and the subsequent topic routing:

Find this block (lines 163-170):
```java
            @Override
            public CompletableFuture<T> execute(S request) {
                if (request.getLocation() == null || request.getLocation().equals(location)) {
                    // The request is for the current location, invoke it directly
                    return module.execute(request);
                }

                Span span = buildAndStartSpan(request);
                String requestTopic = topicProvider.getRequestTopicAtLocation(request.getLocation(), module.getId());
```

Replace with:
```java
            @Override
            public CompletableFuture<T> execute(S request) {
                boolean forceRemote = Boolean.getBoolean("org.opennms.core.ipc.rpc.force-remote");
                if (!forceRemote && (request.getLocation() == null || request.getLocation().equals(location))) {
                    // The request is for the current location, invoke it directly
                    return module.execute(request);
                }

                // Default null location to daemon's own location for Kafka topic routing
                String effectiveLocation = request.getLocation();
                if (effectiveLocation == null) {
                    effectiveLocation = location;
                }

                Span span = buildAndStartSpan(request);
                String requestTopic = topicProvider.getRequestTopicAtLocation(effectiveLocation, module.getId());
```

Also update the ResponseHandler creation (around line 183) to use `effectiveLocation`:

Find:
```java
                ResponseHandler<S, T> responseHandler = new ResponseHandler<S, T>(future, module, rpcId,
                        expirationTime, loggingContext, request.getLocation(), span);
```

Replace:
```java
                ResponseHandler<S, T> responseHandler = new ResponseHandler<S, T>(future, module, rpcId,
                        expirationTime, loggingContext, effectiveLocation, span);
```

Update the TRACE log (around line 221) to use `effectiveLocation`:

Find:
```java
                                LOG.trace("RPC Request {} with id {} chunk {} sent to minion at location {}", request, rpcId, chunkNum, request.getLocation());
```

Replace:
```java
                                LOG.trace("RPC Request {} with id {} chunk {} sent to minion at location {}", request, rpcId, chunkNum, effectiveLocation);
```

Update `addMetrics()` (around line 247) to use `effectiveLocation`. Change the method signature and calls:

Find:
```java
                addMetrics(request, messageInBytes.length);
```

Replace:
```java
                addMetrics(effectiveLocation, module.getId(), messageInBytes.length);
```

Find:
```java
            private void addMetrics(RpcRequest request, int messageLen) {
                final Meter requestSentMeter = getMetrics().meter(MetricRegistry.name(request.getLocation(), module.getId(), RPC_REQUEST_SENT));
                requestSentMeter.mark();
                final Histogram rpcRequestSize = getMetrics().histogram(MetricRegistry.name(request.getLocation(), module.getId(), RPC_REQUEST_SIZE));
                rpcRequestSize.update(messageLen);
            }
```

Replace:
```java
            private void addMetrics(String location, String moduleId, int messageLen) {
                final Meter requestSentMeter = getMetrics().meter(MetricRegistry.name(location, moduleId, RPC_REQUEST_SENT));
                requestSentMeter.mark();
                final Histogram rpcRequestSize = getMetrics().histogram(MetricRegistry.name(location, moduleId, RPC_REQUEST_SIZE));
                rpcRequestSize.update(messageLen);
            }
```

- [ ] **Step 2: Build and verify**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests --projects :org.opennms.core.ipc.rpc.kafka -am install
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add core/ipc/rpc/kafka/src/main/java/org/opennms/core/ipc/rpc/kafka/KafkaRpcClientFactory.java
git commit -m "feat(delta-v): add force-remote flag to KafkaRpcClientFactory

When -Dorg.opennms.core.ipc.rpc.force-remote=true, all RPC requests
route through Kafka to a Minion, even when location matches the
daemon's own location. Null locations default to the daemon's own
location for topic routing."
```

---

### Task 3: Add daemon-loader-shared to Karaf features and Sentinel assembly

The shared module's bundle must be included in every daemon Karaf feature that uses RPC, and the Sentinel assembly must know to place the JAR in `system/`.

**Files:**
- Modify: `container/features/src/main/resources/features.xml`
- Modify: `features/container/sentinel/pom.xml`

- [ ] **Step 1: Add bundle to each RPC-using daemon feature in features.xml**

In `container/features/src/main/resources/features.xml`, add the shared bundle to each of these features: `opennms-daemon-provisiond`, `opennms-daemon-discovery`, `opennms-daemon-pollerd`, `opennms-daemon-collectd`, `opennms-daemon-enlinkd`, `opennms-daemon-perspectivepoller`.

For each feature, add **two lines** inside the `<feature>` block — the shared bundle AND the `opennms-core-ipc-rpc-kafka` feature dependency (which provides the `KafkaRpcClientFactory` class):

```xml
        <feature>opennms-core-ipc-rpc-kafka</feature>
        <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-shared/${project.version}</bundle>
```

Note: `opennms-daemon-perspectivepoller` already has `<feature>opennms-core-ipc-rpc-kafka</feature>` — for that feature, only add the `daemon-loader-shared` bundle.

Add right after the existing daemon-loader bundle line in each feature. For example, in `opennms-daemon-provisiond`:

```xml
        <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-provisiond/${project.version}</bundle>
        <feature>opennms-core-ipc-rpc-kafka</feature>
        <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-shared/${project.version}</bundle>
```

Repeat for all 6 daemon features listed above (adding both lines to provisiond, discovery, pollerd, collectd, enlinkd; adding only the bundle to perspectivepoller).

- [ ] **Step 2: Verify daemon-loader-shared JAR is placed in system/**

After building `container/features` in Step 3, verify that the daemon-loader-shared JAR ends up in the assembled tarball. The `<installedFeatures>` in `features/container/sentinel/pom.xml` already lists the daemon features that now reference the shared bundle. Maven resolves bundle coordinates from features transitively, so no POM change should be needed. But verify:

```bash
# After Step 3 build, check the tarball
jar tf features/container/sentinel/target/sentinel-*.tar.gz | grep daemon-loader-shared
```

Expected: A path like `system/org/opennms/core/org.opennms.core.daemon-loader-shared/36.0.0-SNAPSHOT/org.opennms.core.daemon-loader-shared-36.0.0-SNAPSHOT.jar`.

If the JAR is NOT present, add an explicit dependency to `features/container/sentinel/pom.xml` in the `<dependencies>` section:

```xml
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon-loader-shared</artifactId>
            <version>${project.version}</version>
        </dependency>
```

- [ ] **Step 3: Build features module**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests -pl container/features install
```

Expected: BUILD SUCCESS. The generated `features.xml` should contain the `daemon-loader-shared` bundle references.

- [ ] **Step 4: Commit**

```bash
git add container/features/src/main/resources/features.xml
git commit -m "feat(delta-v): add daemon-loader-shared bundle to RPC daemon Karaf features"
```

---

## Chunk 2: Daemon Migrations

### Task 4: Migrate Provisiond to real RPC clients

Replace `LocalSnmpClient`, `LocalDetectorClient`, `LocalDnsLookupClient` with real `LocationAware*ClientImpl` implementations. Create `LocalServiceDetectorRegistry` for detector discovery.

**Files:**
- Modify: `core/daemon-loader-provisiond/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-provisiond.xml`
- Modify: `core/daemon-loader-provisiond/pom.xml`
- Delete: `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalSnmpClient.java`
- Delete: `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalDetectorClient.java`
- Delete: `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalDnsLookupClient.java`
- Delete: `core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java` (consolidated into shared module)

Note: `LocalServiceDetectorRegistry` was created in Task 1 (daemon-loader-shared module) since both Provisiond and Discovery need it.

**Pre-condition check:** Verify that Provisiond's XML has `<context:annotation-config/>` (required for @Autowired processing of LocationAware*ClientImpl fields). It does — it's in the existing context.

- [ ] **Step 1: Add dependencies to Provisiond POM**

In `core/daemon-loader-provisiond/pom.xml`, add these dependencies (the shared module + RPC implementation modules):

```xml
        <!-- Shared KafkaRpcClientFactory wiring -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon-loader-shared</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- SNMP RPC implementation -->
        <dependency>
            <groupId>org.opennms.core.snmp</groupId>
            <artifactId>org.opennms.core.snmp.proxy-rpc-impl</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Detector RPC implementation -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-detectorclient-rpc</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Kafka RPC client factory -->
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.kafka</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- RPC API -->
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
```

- [ ] **Step 2: Modify Provisiond Spring XML**

In `core/daemon-loader-provisiond/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-provisiond.xml`:

**Add the shared fragment import** (after the `<beans>` opening tag and namespace declarations):

```xml
    <!-- Shared KafkaRpcClientFactory wiring (provides rpcClientFactory, rpcTargetHelper, tracerRegistry) -->
    <import resource="classpath:kafka-rpc-client-factory.xml"/>
```

**Add distPollerDao** (in the OSGi Service References section, near the other onmsgi:references):

```xml
    <onmsgi:reference id="distPollerDao"
                      interface="org.opennms.netmgt.dao.api.DistPollerDao"/>
```

**Replace the 3 local client beans and NoOpTracerRegistry** (around lines 130-140). Find:

```xml
    <!-- LocationAware clients - local implementations for standalone container -->
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
```

Replace with:

```xml
    <!-- LocationAware clients - real RPC implementations via KafkaRpcClientFactory -->

    <!-- SNMP: SnmpProxyRpcModule.INSTANCE is a static singleton, no bean needed -->
    <bean id="locationAwareSnmpClient"
          class="org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl"/>

    <!-- Detector: requires ServiceDetectorRegistry + scanExecutor -->
    <bean id="serviceDetectorRegistry"
          class="org.opennms.core.daemon.loader.LocalServiceDetectorRegistry"/>
    <bean id="detectorClientRpcModule"
          class="org.opennms.netmgt.provision.detector.client.rpc.DetectorClientRpcModule"/>
    <bean id="locationAwareDetectorClient"
          class="org.opennms.netmgt.provision.detector.client.rpc.LocationAwareDetectorClientRpcImpl"/>

    <!-- DNS: DnsLookupClientRpcModule with 4-thread pool -->
    <bean id="dnsLookupClientRpcModule"
          class="org.opennms.netmgt.provision.dns.client.rpc.DnsLookupClientRpcModule">
        <constructor-arg value="4"/>
    </bean>
    <bean id="locationAwareDnsLookupClient"
          class="org.opennms.netmgt.provision.dns.client.rpc.LocationAwareDnsLookupClientRpcImpl"/>

    <bean id="snmpProfileMapper"
          class="org.opennms.core.daemon.loader.NoOpSnmpProfileMapper"/>
    <!-- tracerRegistry provided by shared fragment import -->
```

Note: Provisiond already has a `scanExecutor` bean (around line 168) that satisfies `DetectorClientRpcModule`'s `@Qualifier("scanExecutor")` requirement.

- [ ] **Step 3: Delete local client files**

```bash
rm core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalSnmpClient.java
rm core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalDetectorClient.java
rm core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/LocalDnsLookupClient.java
rm core/daemon-loader-provisiond/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java
```

- [ ] **Step 4: Add force-remote and bootstrap.servers to Provisiond's Docker Compose JAVA_OPTS**

In `opennms-container/delta-v/docker-compose.yml`, find the provisiond service's JAVA_OPTS (around line 545-549). Add these two system properties:

```
      -Dorg.opennms.core.ipc.rpc.kafka.bootstrap.servers=kafka:9092
      -Dorg.opennms.core.ipc.rpc.force-remote=true
```

- [ ] **Step 5: Build and verify**

Build the full daemon container chain:

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-provisiond -am install
```

Then build the container image (if doing container verification):

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests -pl container/features install && \
  cd features/container/sentinel && ../../../maven/bin/mvn -DskipTests install && cd ../../.. && \
  cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install && cd ../..
```

Extract and build Docker image:

```bash
cd opennms-container/sentinel && \
  rm -rf tarball-root && mkdir tarball-root && \
  tar xzf ../../opennms-assemblies/daemon/target/*-daemon.tar.gz -C tarball-root --strip-components=1 && \
  docker build -t opennms/daemon:delta-v -t opennms/daemon:36.0.0-SNAPSHOT . && \
  cd ../..
```

Then start Provisiond and verify health:

```bash
cd opennms-container/delta-v && \
  docker volume rm delta-v_provisiond-data 2>/dev/null; \
  docker compose up -d --force-recreate provisiond && \
  sleep 30 && \
  docker compose exec provisiond curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe
```

Expected: `"Everything is awesome"` with exit code 0.

Check karaf.log for KafkaRpcClientFactory initialization:

```bash
docker compose exec provisiond grep -i "KafkaRpcClientFactory\|bootstrap.servers\|force-remote" /opt/sentinel/data/log/karaf.log
```

- [ ] **Step 6: Commit**

```bash
git add core/daemon-loader-provisiond/ opennms-container/delta-v/docker-compose.yml
git commit -m "feat(delta-v): migrate Provisiond to real RPC clients via KafkaRpcClientFactory

Replace LocalSnmpClient, LocalDetectorClient, LocalDnsLookupClient
with LocationAwareSnmpClientRpcImpl, LocationAwareDetectorClientRpcImpl,
LocationAwareDnsLookupClientRpcImpl. All operations now delegate to
Minions via Kafka RPC.

- Import shared kafka-rpc-client-factory.xml fragment
- Add distPollerDao onmsgi:reference
- Create LocalServiceDetectorRegistry (ServiceLoader-based)
- Delete 3 Local*Client classes + NoOpTracerRegistry (consolidated)
- Add force-remote + bootstrap.servers to JAVA_OPTS"
```

---

### Task 5: Migrate Discovery to real RPC clients

Replace `LocalLocationAwarePingClient` with `LocationAwarePingClientImpl` and wire `LocationAwareDetectorClientRpcImpl` directly (instead of via onmsgi:reference).

**Files:**
- Modify: `core/daemon-loader-discovery/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-discovery.xml`
- Modify: `core/daemon-loader-discovery/pom.xml`
- Delete: `core/daemon-loader-discovery/src/main/java/org/opennms/core/daemon/loader/LocalLocationAwarePingClient.java`

- [ ] **Step 1: Add dependencies to Discovery POM**

In `core/daemon-loader-discovery/pom.xml`, add:

```xml
        <!-- Shared KafkaRpcClientFactory wiring -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon-loader-shared</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- ICMP RPC implementation (LocationAwarePingClientImpl) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-icmp-proxy-rpc-impl</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Detector RPC implementation -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-detectorclient-rpc</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- ICMP best-match pinger (JNA, pure Java) -->
        <dependency>
            <groupId>org.opennms</groupId>
            <artifactId>opennms-icmp-best</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Kafka RPC -->
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.kafka</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- MATE engine (EntityScopeProvider for LocationAwareDetectorClientRpcImpl) -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.mate.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
```

- [ ] **Step 2: Modify Discovery Spring XML**

In `core/daemon-loader-discovery/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-discovery.xml`:

**Add the shared fragment import** (after the `<beans>` tag):

```xml
    <!-- Shared KafkaRpcClientFactory wiring -->
    <import resource="classpath:kafka-rpc-client-factory.xml"/>
```

**Add distPollerDao and entityScopeProvider** (in the OSGi references section):

```xml
    <onmsgi:reference id="distPollerDao"
                      interface="org.opennms.netmgt.dao.api.DistPollerDao"/>
    <onmsgi:reference id="entityScopeProvider"
                      interface="org.opennms.core.mate.api.EntityScopeProvider"/>
```

**Replace LocalLocationAwarePingClient** (around lines 55-59). Find:

```xml
    <!-- ICMP ping client — local implementation using InetAddress.isReachable()
         (the RPC-based LocationAwarePingClientImpl requires the full ICMP+RPC stack
         which is not available in standalone daemon containers) -->
    <bean id="locationAwarePingClient"
          class="org.opennms.core.daemon.loader.LocalLocationAwarePingClient"/>
```

Replace with:

```xml
    <!-- Ping client — real RPC via KafkaRpcClientFactory to Minion -->
    <bean id="pingerFactory"
          class="org.opennms.netmgt.icmp.best.BestMatchPingerFactory"/>
    <bean id="pingProxyRpcModule"
          class="org.opennms.netmgt.icmp.proxy.PingProxyRpcModule"/>
    <bean id="pingSweepRpcModule"
          class="org.opennms.netmgt.icmp.proxy.PingSweepRpcModule"/>
    <bean id="locationAwarePingClient"
          class="org.opennms.netmgt.icmp.proxy.LocationAwarePingClientImpl"/>
```

**Replace onmsgi:reference for LocationAwareDetectorClient** (around line 62-63). Find:

```xml
    <!-- Detector client (from provisioning feature, optional — not all deployments have it) -->
    <onmsgi:reference id="locationAwareDetectorClient"
                      interface="org.opennms.netmgt.provision.LocationAwareDetectorClient"/>
```

Replace with:

```xml
    <!-- Detector client — real RPC via KafkaRpcClientFactory to Minion -->
    <bean id="serviceDetectorRegistry"
          class="org.opennms.core.daemon.loader.LocalServiceDetectorRegistry"/>
    <bean id="scanExecutor" class="java.util.concurrent.Executors"
          factory-method="newCachedThreadPool"/>
    <bean id="detectorClientRpcModule"
          class="org.opennms.netmgt.provision.detector.client.rpc.DetectorClientRpcModule"/>
    <bean id="locationAwareDetectorClient"
          class="org.opennms.netmgt.provision.detector.client.rpc.LocationAwareDetectorClientRpcImpl"/>
```

Note: `LocalServiceDetectorRegistry` lives in `daemon-loader-shared` (created in Task 1), so it's on the classpath for both Provisiond and Discovery via the shared module dependency.

- [ ] **Step 3: Delete local client file**

```bash
rm core/daemon-loader-discovery/src/main/java/org/opennms/core/daemon/loader/LocalLocationAwarePingClient.java
```

- [ ] **Step 4: Add force-remote and bootstrap.servers to Discovery's Docker Compose JAVA_OPTS**

In `opennms-container/delta-v/docker-compose.yml`, find the discovery service's JAVA_OPTS (around line 299-303). Add:

```
      -Dorg.opennms.core.ipc.rpc.kafka.bootstrap.servers=kafka:9092
      -Dorg.opennms.core.ipc.rpc.force-remote=true
```

- [ ] **Step 5: Build, start container, verify health**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-discovery -am install
```

Then rebuild image chain and verify:

```bash
cd opennms-container/delta-v && \
  docker volume rm delta-v_discovery-data 2>/dev/null; \
  docker compose up -d --force-recreate discovery && \
  sleep 30 && \
  docker compose exec discovery curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe
```

Expected: `"Everything is awesome"`.

- [ ] **Step 6: Commit**

```bash
git add core/daemon-loader-discovery/ opennms-container/delta-v/docker-compose.yml
git commit -m "feat(delta-v): migrate Discovery to real RPC clients via KafkaRpcClientFactory

Replace LocalLocationAwarePingClient with LocationAwarePingClientImpl
(real ICMP RPC to Minion). Replace onmsgi:reference for DetectorClient
with direct LocationAwareDetectorClientRpcImpl bean.

- Import shared kafka-rpc-client-factory.xml fragment
- Add distPollerDao, entityScopeProvider onmsgi:references
- Add BestMatchPingerFactory for PingProxyRpcModule injection
- Delete LocalLocationAwarePingClient
- Add force-remote + bootstrap.servers to JAVA_OPTS"
```

---

### Task 6: Migrate Pollerd to real RPC client

Replace `LocalPollerClient` with `LocationAwarePollerClientImpl`.

**Files:**
- Modify: `core/daemon-loader-pollerd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-pollerd.xml`
- Modify: `core/daemon-loader-pollerd/pom.xml`
- Delete: `core/daemon-loader-pollerd/src/main/java/org/opennms/core/daemon/loader/LocalPollerClient.java`
- Delete: `core/daemon-loader-pollerd/src/main/java/org/opennms/core/daemon/loader/LocalPollerRequestBuilder.java`

- [ ] **Step 1: Add dependencies to Pollerd POM**

In `core/daemon-loader-pollerd/pom.xml`, add:

```xml
        <!-- Shared KafkaRpcClientFactory wiring -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon-loader-shared</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Poller RPC implementation -->
        <dependency>
            <groupId>org.opennms.features.poller</groupId>
            <artifactId>org.opennms.features.poller.client-rpc</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Kafka RPC -->
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.kafka</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
```

- [ ] **Step 2: Modify Pollerd Spring XML**

In `core/daemon-loader-pollerd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-pollerd.xml`:

**Add the shared fragment import** (after the `<beans>` tag):

```xml
    <!-- Shared KafkaRpcClientFactory wiring -->
    <import resource="classpath:kafka-rpc-client-factory.xml"/>
```

**Add distPollerDao, entityScopeProvider, and filterDao** (in the OSGi references section):

```xml
    <onmsgi:reference id="distPollerDao"
                      interface="org.opennms.netmgt.dao.api.DistPollerDao"/>
    <onmsgi:reference id="entityScopeProvider"
                      interface="org.opennms.core.mate.api.EntityScopeProvider"/>
    <osgi:reference id="filterDao"
                    interface="org.opennms.netmgt.filter.api.FilterDao"/>
```

**Replace LocalPollerClient with LocationAwarePollerClientImpl** (around lines 107-113). Find:

```xml
    <bean id="locationAwarePollerClient"
          class="org.opennms.core.daemon.loader.LocalPollerClient">
        <constructor-arg ref="serviceMonitorRegistry"/>
        <constructor-arg ref="pollerExecutor"/>
    </bean>
    <onmsgi:service interface="org.opennms.netmgt.poller.LocationAwarePollerClient"
                    ref="locationAwarePollerClient"/>
```

Replace with:

```xml
    <!-- Poller RPC module: serializes/deserializes poller request/response DTOs -->
    <bean id="pollerClientRpcModule"
          class="org.opennms.netmgt.poller.client.rpc.PollerClientRpcModule"/>

    <!-- Real LocationAwarePollerClient — delegates polls to Minions via Kafka RPC -->
    <bean id="locationAwarePollerClient"
          class="org.opennms.netmgt.poller.client.rpc.LocationAwarePollerClientImpl"/>
    <onmsgi:service interface="org.opennms.netmgt.poller.LocationAwarePollerClient"
                    ref="locationAwarePollerClient"/>
```

Note: `PollerClientRpcModule` needs `@Autowired ServiceMonitorRegistry` (satisfied by existing `serviceMonitorRegistry` bean) and `@Autowired @Qualifier("pollerExecutor") Executor` (satisfied by existing `pollerExecutor` bean). `LocationAwarePollerClientImpl` needs `@Autowired RpcClientFactory` (from shared fragment), `@Autowired RpcTargetHelper` (from shared fragment), and `@Autowired EntityScopeProvider` (added above).

- [ ] **Step 3: Delete local client files**

```bash
rm core/daemon-loader-pollerd/src/main/java/org/opennms/core/daemon/loader/LocalPollerClient.java
rm core/daemon-loader-pollerd/src/main/java/org/opennms/core/daemon/loader/LocalPollerRequestBuilder.java
```

- [ ] **Step 4: Add force-remote and bootstrap.servers to Pollerd's Docker Compose JAVA_OPTS**

In `opennms-container/delta-v/docker-compose.yml`, find the pollerd service's JAVA_OPTS (around line 191-195). Add:

```
      -Dorg.opennms.core.ipc.rpc.kafka.bootstrap.servers=kafka:9092
      -Dorg.opennms.core.ipc.rpc.force-remote=true
```

- [ ] **Step 5: Build, start container, verify health**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-pollerd -am install
```

Rebuild image and verify:

```bash
cd opennms-container/delta-v && \
  docker volume rm delta-v_pollerd-data 2>/dev/null; \
  docker compose up -d --force-recreate pollerd && \
  sleep 30 && \
  docker compose exec pollerd curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe
```

Expected: `"Everything is awesome"`.

- [ ] **Step 6: Commit**

```bash
git add core/daemon-loader-pollerd/ opennms-container/delta-v/docker-compose.yml
git commit -m "feat(delta-v): migrate Pollerd to real RPC client via KafkaRpcClientFactory

Replace LocalPollerClient with LocationAwarePollerClientImpl. Polls
now delegate to Minions via Kafka RPC.

- Import shared kafka-rpc-client-factory.xml fragment
- Add distPollerDao, entityScopeProvider, filterDao references
- Add PollerClientRpcModule bean
- Delete LocalPollerClient + LocalPollerRequestBuilder
- Add force-remote + bootstrap.servers to JAVA_OPTS"
```

---

### Task 7: Migrate Collectd to real RPC client

Replace `LocalCollectorClient` with `LocationAwareCollectorClientImpl`.

**Files:**
- Modify: `core/daemon-loader-collectd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-collectd.xml`
- Modify: `core/daemon-loader-collectd/pom.xml`
- Delete: `core/daemon-loader-collectd/src/main/java/org/opennms/core/daemon/loader/LocalCollectorClient.java`
- Delete: `core/daemon-loader-collectd/src/main/java/org/opennms/core/daemon/loader/LocalCollectorRequestBuilder.java`

- [ ] **Step 1: Add dependencies to Collectd POM**

In `core/daemon-loader-collectd/pom.xml`, add:

```xml
        <!-- Shared KafkaRpcClientFactory wiring -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon-loader-shared</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Collector RPC implementation -->
        <dependency>
            <groupId>org.opennms.features.collection</groupId>
            <artifactId>org.opennms.features.collection.client-rpc</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Kafka RPC -->
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.kafka</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
```

- [ ] **Step 2: Modify Collectd Spring XML**

In `core/daemon-loader-collectd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-collectd.xml`:

**Add the shared fragment import** (after the `<beans>` tag):

```xml
    <!-- Shared KafkaRpcClientFactory wiring -->
    <import resource="classpath:kafka-rpc-client-factory.xml"/>
```

**Add distPollerDao** (in the OSGi references section — entityScopeProvider is already present in Collectd):

```xml
    <onmsgi:reference id="distPollerDao"
                      interface="org.opennms.netmgt.dao.api.DistPollerDao"/>
```

Verify that `entityScopeProvider` is already present. If not, add:

```xml
    <onmsgi:reference id="entityScopeProvider"
                      interface="org.opennms.core.mate.api.EntityScopeProvider"/>
```

**Replace LocalCollectorClient** (around lines 99-105). Find:

```xml
    <bean id="locationAwareCollectorClient"
          class="org.opennms.core.daemon.loader.LocalCollectorClient">
        <constructor-arg ref="serviceCollectorRegistry"/>
        <constructor-arg ref="collectorExecutor"/>
    </bean>
    <onmsgi:service interface="org.opennms.netmgt.collection.api.LocationAwareCollectorClient"
                    ref="locationAwareCollectorClient"/>
```

Replace with:

```xml
    <!-- Collector RPC module: serializes/deserializes collector request/response DTOs -->
    <bean id="collectorClientRpcModule"
          class="org.opennms.netmgt.collection.client.rpc.CollectorClientRpcModule"/>

    <!-- Real LocationAwareCollectorClient — delegates collections to Minions via Kafka RPC -->
    <bean id="locationAwareCollectorClient"
          class="org.opennms.netmgt.collection.client.rpc.LocationAwareCollectorClientImpl"/>
    <onmsgi:service interface="org.opennms.netmgt.collection.api.LocationAwareCollectorClient"
                    ref="locationAwareCollectorClient"/>
```

Note: `CollectorClientRpcModule` needs `@Autowired ServiceCollectorRegistry` (existing `serviceCollectorRegistry` bean) and `@Autowired @Qualifier("collectorExecutor") Executor` (existing `collectorExecutor` bean).

- [ ] **Step 3: Delete local client files**

```bash
rm core/daemon-loader-collectd/src/main/java/org/opennms/core/daemon/loader/LocalCollectorClient.java
rm core/daemon-loader-collectd/src/main/java/org/opennms/core/daemon/loader/LocalCollectorRequestBuilder.java
```

- [ ] **Step 4: Add force-remote and bootstrap.servers to Collectd's Docker Compose JAVA_OPTS**

In `opennms-container/delta-v/docker-compose.yml`, find the collectd service's JAVA_OPTS (around line 228-232). Add:

```
      -Dorg.opennms.core.ipc.rpc.kafka.bootstrap.servers=kafka:9092
      -Dorg.opennms.core.ipc.rpc.force-remote=true
```

- [ ] **Step 5: Build, start container, verify health**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-collectd -am install
```

Rebuild image chain and verify (same as Task 4 Step 5):

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests -pl container/features install && \
  cd features/container/sentinel && ../../../maven/bin/mvn -DskipTests install && cd ../../.. && \
  cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install && cd ../.. && \
  cd opennms-container/sentinel && \
  rm -rf tarball-root && mkdir tarball-root && \
  tar xzf ../../opennms-assemblies/daemon/target/*-daemon.tar.gz -C tarball-root --strip-components=1 && \
  docker build -t opennms/daemon:delta-v -t opennms/daemon:36.0.0-SNAPSHOT . && \
  cd ../..
```

Verify health:

```bash
cd opennms-container/delta-v && \
  docker volume rm delta-v_collectd-data 2>/dev/null; \
  docker compose up -d --force-recreate collectd && \
  sleep 30 && \
  docker compose exec collectd curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe
```

Expected: `"Everything is awesome"`.

- [ ] **Step 6: Commit**

```bash
git add core/daemon-loader-collectd/ opennms-container/delta-v/docker-compose.yml
git commit -m "feat(delta-v): migrate Collectd to real RPC client via KafkaRpcClientFactory

Replace LocalCollectorClient with LocationAwareCollectorClientImpl.
Collections now delegate to Minions via Kafka RPC.

- Import shared kafka-rpc-client-factory.xml fragment
- Add distPollerDao onmsgi:reference
- Add CollectorClientRpcModule bean
- Delete LocalCollectorClient + LocalCollectorRequestBuilder
- Add force-remote + bootstrap.servers to JAVA_OPTS"
```

---

## Chunk 3: Enlinkd, PerspectivePollerd Retrofit, and Verification

### Task 8: Migrate Enlinkd to real RPC client

Replace `onmsgi:reference` for `LocationAwareSnmpClient` with direct `LocationAwareSnmpClientRpcImpl` bean.

**Files:**
- Modify: `core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-enlinkd.xml`
- Modify: `core/daemon-loader-enlinkd/pom.xml`

- [ ] **Step 1: Add dependencies to Enlinkd POM**

In `core/daemon-loader-enlinkd/pom.xml`, add:

```xml
        <!-- Shared KafkaRpcClientFactory wiring -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon-loader-shared</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- SNMP RPC implementation -->
        <dependency>
            <groupId>org.opennms.core.snmp</groupId>
            <artifactId>org.opennms.core.snmp.proxy-rpc-impl</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- Kafka RPC -->
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.kafka</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.opennms.core.ipc.rpc</groupId>
            <artifactId>org.opennms.core.ipc.rpc.api</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
```

- [ ] **Step 2: Modify Enlinkd Spring XML**

In `core/daemon-loader-enlinkd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-enlinkd.xml`:

**Add the shared fragment import** (after the `<beans>` tag):

```xml
    <!-- Shared KafkaRpcClientFactory wiring -->
    <import resource="classpath:kafka-rpc-client-factory.xml"/>
```

**Add distPollerDao** (in the OSGi references section):

```xml
    <onmsgi:reference id="distPollerDao"
                      interface="org.opennms.netmgt.dao.api.DistPollerDao"/>
```

**Replace onmsgi:reference for LocationAwareSnmpClient** (around line 72-73). Find:

```xml
    <!-- SNMP client (from SNMP proxy feature) -->
    <onmsgi:reference id="locationAwareSnmpClient"
                      interface="org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient"/>
```

Replace with:

```xml
    <!-- SNMP client — real RPC via KafkaRpcClientFactory to Minion.
         SnmpProxyRpcModule.INSTANCE is a static singleton — no bean needed. -->
    <bean id="locationAwareSnmpClient"
          class="org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl"/>
```

- [ ] **Step 3: Add force-remote and bootstrap.servers to Enlinkd's Docker Compose JAVA_OPTS**

In `opennms-container/delta-v/docker-compose.yml`, find the enlinkd service's JAVA_OPTS (around line 477-481). Add:

```
      -Dorg.opennms.core.ipc.rpc.kafka.bootstrap.servers=kafka:9092
      -Dorg.opennms.core.ipc.rpc.force-remote=true
```

- [ ] **Step 4: Build, start container, verify health**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-enlinkd -am install
```

Rebuild image chain (same as Task 4 Step 5):

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests -pl container/features install && \
  cd features/container/sentinel && ../../../maven/bin/mvn -DskipTests install && cd ../../.. && \
  cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install && cd ../.. && \
  cd opennms-container/sentinel && \
  rm -rf tarball-root && mkdir tarball-root && \
  tar xzf ../../opennms-assemblies/daemon/target/*-daemon.tar.gz -C tarball-root --strip-components=1 && \
  docker build -t opennms/daemon:delta-v -t opennms/daemon:36.0.0-SNAPSHOT . && \
  cd ../..
```

Verify health:

```bash
cd opennms-container/delta-v && \
  docker volume rm delta-v_enlinkd-data 2>/dev/null; \
  docker compose up -d --force-recreate enlinkd && \
  sleep 30 && \
  docker compose exec enlinkd curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe
```

Expected: `"Everything is awesome"`.

- [ ] **Step 5: Commit**

```bash
git add core/daemon-loader-enlinkd/ opennms-container/delta-v/docker-compose.yml
git commit -m "feat(delta-v): migrate Enlinkd to real RPC client via KafkaRpcClientFactory

Replace onmsgi:reference for LocationAwareSnmpClient with direct
LocationAwareSnmpClientRpcImpl bean. SNMP walks now delegate to
Minions via Kafka RPC.

- Import shared kafka-rpc-client-factory.xml fragment
- Add distPollerDao onmsgi:reference
- Add force-remote + bootstrap.servers to JAVA_OPTS"
```

---

### Task 9: Retrofit PerspectivePollerd to use shared fragment

Replace PerspectivePollerd's inline KafkaRpcClientFactory block with the shared fragment import.

**Files:**
- Modify: `core/daemon-loader-perspectivepoller/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-perspectivepoller.xml`
- Modify: `core/daemon-loader-perspectivepoller/pom.xml`
- Delete: `core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java` (consolidated into shared module)

- [ ] **Step 1: Add shared module dependency to PerspectivePollerd POM**

In `core/daemon-loader-perspectivepoller/pom.xml`, add:

```xml
        <!-- Shared KafkaRpcClientFactory wiring -->
        <dependency>
            <groupId>org.opennms.core</groupId>
            <artifactId>org.opennms.core.daemon-loader-shared</artifactId>
            <version>${project.version}</version>
            <scope>provided</scope>
        </dependency>
```

- [ ] **Step 2: Modify PerspectivePollerd Spring XML**

In `core/daemon-loader-perspectivepoller/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-perspectivepoller.xml`:

**Add the shared fragment import** (after the `<beans>` tag, near the top):

```xml
    <!-- Shared KafkaRpcClientFactory wiring -->
    <import resource="classpath:kafka-rpc-client-factory.xml"/>
```

**Remove the inline RPC infrastructure beans** (lines 125-148 and 167-169). Delete these three blocks:

Block 1 — `rpcTargetHelper` (line 125-127):
```xml
    <!-- RPC target helper: routes RPC calls to the right Minion location -->
    <bean id="rpcTargetHelper"
          class="org.opennms.core.rpc.utils.RpcTargetHelper"/>
```

Block 2 — identity/location/metrics/rpcClientFactory (lines 129-148):
```xml
    <!-- Kafka RPC client factory: dispatches RPC calls to Minions via Kafka.
         KafkaRpcClientFactory.start() reads bootstrap.servers from system property
         org.opennms.core.ipc.rpc.kafka.bootstrap.servers (set via JAVA_OPTS).
         The core/ipc/rpc/kafka bundle has no Spring-Context header, so we create
         the factory directly here instead of using osgi:reference. -->
    <bean id="identity" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="targetObject" ref="distPollerDao"/>
        <property name="targetMethod" value="whoami"/>
    </bean>
    <bean id="location" class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="targetObject" ref="identity"/>
        <property name="targetMethod" value="getLocation"/>
    </bean>
    <bean id="kafkaRpcMetricRegistry" class="com.codahale.metrics.MetricRegistry"/>
    <bean id="rpcClientFactory"
          class="org.opennms.core.ipc.rpc.kafka.KafkaRpcClientFactory"
          init-method="start" destroy-method="stop">
        <property name="location" ref="location"/>
        <property name="metrics" ref="kafkaRpcMetricRegistry"/>
    </bean>
```

Block 3 — `tracerRegistry` (lines 167-169):
```xml
    <!-- No-op tracer (same as provisiond standalone) -->
    <bean id="tracerRegistry"
          class="org.opennms.core.daemon.loader.NoOpTracerRegistry"/>
```

All three are now provided by the shared fragment import. The bean IDs differ slightly (`identity`→`rpcIdentity`, `location`→`rpcLocation` in the shared fragment) but this is safe — `LocationAwarePollerClientImpl` uses `@Autowired` by type, not by bean ID.

- [ ] **Step 3: Delete NoOpTracerRegistry from PerspectivePollerd**

```bash
rm core/daemon-loader-perspectivepoller/src/main/java/org/opennms/core/daemon/loader/NoOpTracerRegistry.java
```

- [ ] **Step 4: Add force-remote to PerspectivePollerd's Docker Compose JAVA_OPTS**

In `opennms-container/delta-v/docker-compose.yml`, find the perspectivepollerd service's JAVA_OPTS (around line 620-625). It already has `bootstrap.servers`. Add:

```
      -Dorg.opennms.core.ipc.rpc.force-remote=true
```

- [ ] **Step 5: Build, start container, verify health**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests --projects :org.opennms.core.daemon-loader-perspectivepoller -am install
```

Rebuild image and verify health.

- [ ] **Step 6: Commit**

```bash
git add core/daemon-loader-perspectivepoller/ opennms-container/delta-v/docker-compose.yml
git commit -m "refactor(delta-v): retrofit PerspectivePollerd to use shared RPC fragment

Replace inline KafkaRpcClientFactory block with shared fragment import.
Delete NoOpTracerRegistry (consolidated into daemon-loader-shared).
Add force-remote flag to JAVA_OPTS."
```

---

### Task 10: End-to-end verification

Verify all migrated daemons start healthy and RPC flows through Minion.

**Files:** None (verification only)

- [ ] **Step 1: Rebuild full image**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home \
  ./compile.pl -DskipTests -pl container/features install && \
  cd features/container/sentinel && ../../../maven/bin/mvn -DskipTests install && cd ../../.. && \
  cd opennms-assemblies/daemon && ../../maven/bin/mvn -DskipTests install && cd ../.. && \
  cd opennms-container/sentinel && \
  rm -rf tarball-root && mkdir tarball-root && \
  tar xzf ../../opennms-assemblies/daemon/target/*-daemon.tar.gz -C tarball-root --strip-components=1 && \
  docker build -t opennms/daemon:delta-v -t opennms/daemon:36.0.0-SNAPSHOT . && \
  cd ../..
```

- [ ] **Step 2: Delete all daemon data volumes and recreate containers**

```bash
cd opennms-container/delta-v && \
  docker compose down provisiond discovery pollerd collectd enlinkd perspectivepollerd && \
  docker volume rm delta-v_provisiond-data delta-v_discovery-data delta-v_pollerd-data \
    delta-v_collectd-data delta-v_enlinkd-data delta-v_perspectivepollerd-data 2>/dev/null; \
  docker compose up -d provisiond discovery pollerd collectd enlinkd perspectivepollerd
```

- [ ] **Step 3: Verify all containers are healthy**

Wait 90 seconds (some daemons have `start_period: 90s`), then check each:

```bash
for svc in provisiond discovery pollerd collectd enlinkd perspectivepollerd; do
  echo "=== $svc ==="
  docker compose exec "$svc" curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe 2>&1 || echo "FAILED"
  echo
done
```

Expected: All 6 return `"Everything is awesome"`.

- [ ] **Step 4: Verify KafkaRpcClientFactory in logs**

```bash
for svc in provisiond discovery pollerd collectd enlinkd perspectivepollerd; do
  echo "=== $svc ==="
  docker compose exec "$svc" grep "KafkaRpcClientFactory" /opt/sentinel/data/log/karaf.log | tail -3
  echo
done
```

Expected: Each shows `KafkaRpcClientFactory` starting with `bootstrap.servers=kafka:9092`.

- [ ] **Step 5: Run Minion E2E test**

```bash
cd opennms-container/delta-v && \
  docker compose stop bsmd collectd discovery scriptd syslogd pollerd enlinkd rtcd ticketer perspectivepollerd && \
  ./test-minion-e2e.sh
```

Expected: 11 tests pass across 3 phases. The key verification is Phase 1: coldStart → newSuspect → Provisiond node scan (which now uses Minion for SNMP walks via Kafka RPC).

- [ ] **Step 6: Verify RPC routing through Minion (negative test)**

Check Minion logs for SNMP RPC requests from Provisiond:

```bash
docker compose logs minion 2>&1 | grep -i "SNMP\|RPC\|module" | tail -20
```

Expected: Evidence of SNMP RPC requests being received and executed by the Minion.
