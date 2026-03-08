# Karaf-Only Daemon Assembly Design

## Problem

Standalone daemon containers (Pollerd, Collectd) using the `opennms/horizon` image
cannot receive cross-container events. This is because of daemon startup ordering:

1. Manager starts, starts Eventd → `EventIpcManagerDefaultImpl` registers as OSGi service
2. Manager starts Pollerd/Collectd → daemons resolve `EventForwarder` to `EventIpcManagerDefaultImpl`
3. Karaf features install → `KafkaEventIpcManagerAdapter` registers (too late)

Since daemons bind to `EventIpcManagerDefaultImpl`, `KafkaEventSubscriptionService`
never delivers events to them. They can SEND events via Kafka (the forwarder works),
but they can't RECEIVE events from other containers.

**Impact:** Pollerd/Collectd don't receive `nodeGainedService`, `nodeDown`, etc. from
Provisiond/Core. They read service schedules from the DB at startup only. A restart
is required to pick up newly provisioned services.

## Goal

Enable standalone daemon containers to both send AND receive events via Kafka,
eliminating the need for a shared Eventd/ActiveMQ infrastructure.

## Architecture Context

### Current: opennms/horizon Image Pattern

```
opennms/horizon JVM
  ├── Manager (always on)
  ├── Eventd (always on) → registers EventIpcManagerDefaultImpl [FIRST]
  ├── Pollerd (enabled) → binds to EventIpcManagerDefaultImpl
  ├── Karaf features
  │   └── KafkaEventIpcManagerAdapter → registers [SECOND, unused]
  └── All other daemons: disabled
```

### Target: Karaf-Only Assembly Pattern (alarmd model)

```
Karaf JVM
  ├── Boot features install [FIRST]
  │   ├── opennms-event-forwarder-kafka → KafkaEventIpcManagerAdapter [SOLE provider]
  │   ├── opennms-distributed-dao → DAOs, SessionFactory
  │   └── opennms-daemon-pollerd → loads Pollerd Spring context
  ├── Spring Extender activates contexts from bundles
  └── Pollerd daemon starts → binds to KafkaEventIpcManagerAdapter [ONLY option]
```

No Manager, no Eventd. Karaf IS the runtime. Features install before daemon contexts
load, so `KafkaEventIpcManagerAdapter` is the sole `EventIpcManager`.

## Existing Pattern: Alarmd Karaf Assembly

The alarmd assembly (`opennms-assemblies/alarmd/`) already demonstrates this:

- **Base runtime:** Sentinel Karaf container (`features/container/sentinel/`)
- **Packaging:** `karaf-assembly` Maven packaging type
- **Entry point:** `bin/karaf server` (pure Karaf, no Manager/Eventd)
- **Spring contexts:** Loaded by Spring Extender from bundles with `Spring-Context` headers
- **Assembly descriptor:** `src/assembly/alarmd.xml` — layers base scripts + container-shared + sentinel tarball

### Key Files

```
opennms-assemblies/alarmd/
  pom.xml                          # Dependencies: base-assembly, container-shared, sentinel
  src/assembly/alarmd.xml          # Assembly descriptor

opennms-container/alarmd/
  Dockerfile                       # ENTRYPOINT ["/opt/alarmd/bin/karaf", "server"]
  Makefile                         # Build automation
  tarball-root/                    # Config overlay (features.cfg, featuresBoot.d/, etc.)
```

## Technical Challenge: Pollerd/Collectd Context Loading

### Spring Context Hierarchy

Pollerd and Collectd require a 7-level Spring context parent chain:

```
soaContext          → core/soa       (OSGi registry setup)
  └── commonContext → opennms-config (property placeholders, config init)
    └── daemonContext → core/daemon  (event IPC, ActiveMQ, pinger)
      └── daoContext → opennms-dao   (Hibernate SessionFactory, all DAOs)
        └── pollerConfigContext → opennms-services (PollOutagesDao)
          └── thresholdingContext → features/collection/thresholding/impl
            └── pollerdContext/collectdContext → opennms-services
```

### What's Already Available as Karaf Features

| Context Level | Module | OSGi Bundle? | Spring-Context Header? | Karaf Feature? |
|---------------|--------|-------------|----------------------|---------------|
| soaContext | core/soa | Yes | No (Activator) | opennms-core-soa |
| commonContext | opennms-config | Yes | No | opennms-config |
| daemonContext | core/daemon | Yes | No | opennms-core-daemon |
| daoContext | opennms-dao | Yes | No | opennms-dao |
| distributed DAO | features/distributed/dao/impl | Yes | **Yes** | opennms-distributed-core-impl |
| pollerConfigContext | opennms-services | Yes | No | — |
| thresholdingContext | features/.../thresholding/impl | Yes | ? | opennms-collection-thresholding-impl |
| pollerdContext | opennms-services | Yes | No | — |
| collectdContext | opennms-services | Yes | No | — |

**Key discovery:** `opennms-services` uses `<packaging>bundle</packaging>` — it IS an
OSGi bundle. But it has NO `Spring-Context` manifest header, so Gemini Blueprint
won't auto-load any of its 9 context files. Currently, context loading is driven by
`beanRefContext.xml` and `AbstractSpringContextJmxServiceDaemon`, both of which
require the Manager.

### The Core Problem

In the alarmd model, the Spring Extender auto-loads contexts from bundles that declare
a `Spring-Context` manifest header. For Pollerd/Collectd, we need to either:

1. Add `Spring-Context` headers to opennms-services (loads ALL 9 contexts — bad)
2. Create a selective context loading mechanism
3. Create daemon-specific bundles that wrap the context files

## Proposed Approach: Daemon Loader Bundles

Create thin OSGi bundles (one per daemon) that selectively load only the needed
Spring contexts from the classpath. Each loader bundle:

1. Declares a `Spring-Context` header pointing to its own blueprint XML
2. The blueprint creates the daemon's Spring context chain programmatically
3. Depends on infrastructure Karaf features (dao, config, event-forwarder-kafka)

### Example: `opennms-daemon-pollerd` Loader Bundle

```
core/daemon-loader-pollerd/
  pom.xml                    # packaging: bundle, depends on opennms-services
  src/main/resources/
    OSGI-INF/blueprint/
      blueprint-pollerd.xml  # Creates pollerd context chain
```

**blueprint-pollerd.xml** would:
- Reference OSGi services: EventIpcManager, DataSource, MessageBus
- Create `ClassPathXmlApplicationContext` for pollerConfigContext, thresholdingContext, pollerdContext
- Wire parent contexts in order
- Register the Poller daemon bean

### Karaf Feature Definition

```xml
<feature name="opennms-daemon-pollerd" version="${project.version}">
    <feature>opennms-distributed-core-impl</feature>   <!-- DAO, config, daemon infra -->
    <feature>opennms-event-forwarder-kafka</feature>    <!-- Kafka event transport -->
    <feature>opennms-collection-thresholding-impl</feature>
    <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-pollerd/${project.version}</bundle>
</feature>
```

### New Assembly: `opennms-assemblies/daemon`

A generalized Karaf assembly (replacing per-daemon assemblies):

```
opennms-assemblies/daemon/
  pom.xml                          # Same deps as alarmd: base-assembly, container-shared, sentinel
  src/assembly/daemon.xml          # Same structure as alarmd.xml

opennms-container/daemon/
  Dockerfile                       # Same as alarmd: ENTRYPOINT ["bin/karaf", "server"]
  Makefile
  tarball-root/                    # Base Karaf config
```

The specific daemon to run is selected by `featuresBoot.d/` overlay:
- `featuresBoot.d/pollerd.boot` → `opennms-daemon-pollerd`
- `featuresBoot.d/collectd.boot` → `opennms-daemon-collectd`
- `featuresBoot.d/alarmd.boot` → existing alarmd features

This means ONE Docker image (`opennms/daemon`) runs ANY daemon based on config overlay.

## Alternative Approaches Considered

### A. Disable Eventd + Race on OSGi Timeout

Keep the `opennms/horizon` image. Disable Eventd so `EventIpcManagerDefaultImpl`
never registers. Rely on OSGi's `<onmsgi:reference>` timeout (5 min default) to
wait for `KafkaEventIpcManagerAdapter` from Karaf features.

**Problem:** In the Horizon image, the Manager starts daemons BEFORE Karaf features
finish installing. The daemon's OSGi references would block and wait, but this creates
fragile timing dependencies and long startup delays. Not reliable.

### B. Add Spring-Context Header to opennms-services

Add `Spring-Context: META-INF/opennms/applicationContext-pollerd.xml` to the
opennms-services bundle manifest.

**Problem:** This loads Pollerd in EVERY container where opennms-services is deployed,
including Core where it should be disabled. The `Spring-Context` header is all-or-nothing
per bundle — there's no way to selectively enable contexts based on environment.

### C. Split opennms-services into Per-Daemon Bundles

Extract each daemon's classes and contexts into separate Maven modules:
- `opennms-services-pollerd/` with only Pollerd classes
- `opennms-services-collectd/` with only Collectd classes

**Problem:** Massive refactoring effort. opennms-services has shared code, cross-daemon
dependencies, and 9 daemon contexts in one module. Not practical as a first step.

## Implementation Phases

### Phase 1: Loader Bundle Prototype (Pollerd)

1. Create `core/daemon-loader-pollerd/` module
2. Blueprint XML that creates the Pollerd context chain
3. Karaf feature `opennms-daemon-pollerd`
4. Test in existing alarmd Karaf assembly (just add the feature)
5. Verify Pollerd starts and receives Kafka events

### Phase 2: Generic Daemon Assembly

1. Create `opennms-assemblies/daemon/` (copy alarmd pattern)
2. Create `opennms-container/daemon/` with Dockerfile
3. Feature overlay mechanism for daemon selection
4. Update Strike Fighter compose to use `opennms/daemon` image

### Phase 3: Remaining Daemon Loaders

1. Create `core/daemon-loader-collectd/`
2. Create loaders for other extractable daemons
3. Migrate alarmd to use the generic assembly

### Phase 4: Eliminate Eventd

1. All daemons run as Karaf-only containers with Kafka transport
2. Core container disables Eventd
3. Remove ActiveMQ dependency for event transport
4. EventIpcManagerDefaultImpl becomes unused

## Open Questions

1. **Context parent chain in Karaf:** When the loader blueprint creates
   `ClassPathXmlApplicationContext`, do the parent context XML files need to be
   on the bundle's classpath? The Spring Extender may need `DynamicImport-Package: *`
   or explicit `Import-Package` for context resources.

2. **DataSource availability:** The distributed DAO feature provides a DataSource
   via OSGi. Does the legacy `applicationContext-shared.xml` (Hibernate SessionFactory)
   work with an OSGi-provided DataSource, or does it expect a JNDI DataSource?

3. **Thresholding context:** Is `opennms-collection-thresholding-impl` already a
   working Karaf feature with Spring-Context header? If so, the loader bundle
   only needs to create pollerConfigContext and pollerdContext.

4. **Manager dependency:** Do any beans in the Pollerd context chain reference
   the Manager daemon? If so, those references need to be made optional or
   removed for Karaf-only operation.
