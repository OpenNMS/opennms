# Strike Fighter Completion Design

## Goal

Complete the Strike Fighter microservice architecture by extracting all remaining
ServiceDaemons from the OpenNMS monolith into standalone Karaf-only containers,
deleting dead daemons, and removing the defunct events table infrastructure.

## Current State (7 services)

```
postgres ← core     (Eventd, Provisiond, Enlinkd, Telemetryd, Bsmd, Correlator,
                      Scriptd, + all daemons being extracted/deleted)
         ← webapp   (opennms/horizon, JettyServer + JSPs + Vue UI)
kafka    ← alarmd   (Kafka consumer → alarm creation, TSID=2)
         ← pollerd  (opennms/daemon, Karaf-only, TSID=4)
         ← collectd (opennms/daemon, Karaf-only, TSID=5)
```

## Target State (15 services)

```
postgres ← core              (Eventd, Provisiond, Enlinkd, Telemetryd, Bsmd,
                               Correlator, Scriptd — no extracted daemons)
         ← webapp            (opennms/horizon, JettyServer + JSPs + Vue UI)
kafka    ← alarmd            (Kafka → alarms, TSID=2)
         ← pollerd           (Karaf-only, TSID=4)
         ← collectd          (Karaf-only, TSID=5)
         ← rtcd              (Karaf-only, TSID=6)
         ← passivestatusd    (Karaf-only, TSID=7)
         ← notifd            (Karaf-only, TSID=8)
         ← discovery         (Karaf-only, TSID=9)
         ← trapd             (Karaf-only, TSID=10, UDP :1162)
         ← syslogd           (Karaf-only, TSID=11, UDP :10514)
         ← ticketer          (Karaf-only, TSID=12)
         ← eventtranslator   (Karaf-only, TSID=13)
```

## Daemons Deleted Entirely (4)

Code, configs, tests, and service-configuration.xml entries removed:

| Daemon | Rationale |
|--------|-----------|
| Vacuumd | Primary job was purging events table (now dead). Remaining alarm cleanup handled by Drools rules. |
| Statsd | Unused scheduled statistics reporting. |
| Actiond | Legacy event-to-shell-command execution. Security concern, unused. |
| Ackd | Acknowledgment daemon. Unused in modern deployments. |

## Dead Event Infrastructure Removed

The events table is no longer written to (HibernateEventWriter disconnected from
processor chain in the eventbus-redesign). All event flow goes through Kafka.

| Component | Action |
|-----------|--------|
| `events` table schema (Liquibase) | Drop table migration |
| `HibernateEventWriter` | Delete class |
| `EventWriter` interface/impls | Delete |
| `events_archive` table + `EventArchiveWriter` | Delete (if exists) |
| Event REST endpoints (v1 + v2 EventRestService) | Delete |
| Event list/detail Vue pages | Delete |
| Event list/detail JSP pages | Delete |
| Jasper Reports referencing events table | Delete |
| `EventDao` and Hibernate event mappings | Delete |

## Architecture Decisions

### Webapp stays as opennms/horizon image

The Vue UI is a progressive hybrid — menu components are embedded in every JSP
page via `bootstrap.jsp`, and many admin features only exist as JSPs. The
ServletBridge (ProxyFilter → Felix HTTP Bridge → Karaf CXF) is still needed.
The webapp container runs JettyServer with all daemons disabled.

### One container per daemon

Each extracted daemon gets its own `opennms/daemon` container instance with a
dedicated featuresBoot overlay. Maximum isolation, independent scaling, consistent
with the Pollerd/Collectd/Alarmd pattern.

### Eventd remains in core

Eventd provides local event expansion (eventconf.xml lookup), TSID assignment,
and Kafka publishing for daemons that remain in core (Provisiond, Enlinkd, etc.).
It no longer writes to the events table or broadcasts events — the EventRouter
classifies and routes events to Kafka (FAULT), MessageBus (IPC), or both (DUAL).

### Each daemon container embeds KafkaEventForwarder

Daemon containers use `KafkaEventIpcManagerAdapter` as their sole
`EventIpcManager` implementation. It delegates sends to Kafka via
`KafkaEventForwarder` (with local EventExpander + TsidAssigner) and receives
from Kafka via `KafkaEventSubscriptionService`. No Eventd, no ActiveMQ
dependency.

### Scriptd stays in core

Scriptd listens to all events for custom automation. Keeping it in core alongside
Eventd gives it access to the full local event stream without requiring a
catch-all Kafka consumer.

## Event Flow

```
Daemon containers (Pollerd, Trapd, Syslogd, etc.)
  └─→ KafkaEventForwarder
        ├─→ EventExpander (enrich from eventconf.xml)
        ├─→ TsidAssigner (assign TSID)
        └─→ Kafka producer → opennms-fault-events topic

Core container (Provisiond, Enlinkd, etc.)
  └─→ EventRouter
        ├─→ EventExpander → TsidAssigner
        ├─→ FAULT events → Kafka producer → opennms-fault-events
        ├─→ IPC events → MessageBus (JMS/ActiveMQ)
        └─→ DUAL events → both Kafka + MessageBus

opennms-fault-events topic
  ├─→ Alarmd (creates/reduces alarms in PostgreSQL)
  ├─→ Notifd (sends notifications based on event UEI)
  ├─→ Pollerd/Collectd (schedule updates from nodeGainedService, etc.)
  ├─→ EventTranslator (event translation rules)
  └─→ All other daemon consumers (per consumer group = fanout)
```

## Daemon Extraction Pattern (Template)

Each daemon extraction produces:

### 1. Daemon Loader Bundle

`core/daemon-loader-<name>/` — thin OSGi bundle:

- **POM**: `packaging: bundle`, all deps `scope: provided`, `DynamicImport-Package: *`
- **Spring context**: flat XML with OSGi service references (no nested parent chain)
  - `osgi:reference` for KafkaEventIpcManagerAdapter services
  - `onmsgi:reference` for DAOs from distributed-dao-impl ServiceRegistry
  - Local beans for daemon config factories
  - `DaemonLifecycleManager` as final bean (calls init/start/stop)
- **Manifest**: `Spring-Context: META-INF/opennms/*.xml;publish-context:=false;create-asynchronously:=false`

### 2. Karaf Feature

In `container/features/src/main/resources/features.xml`:

```xml
<feature name="opennms-daemon-<name>" version="${project.version}">
    <feature>opennms-distributed-core-impl</feature>
    <feature>opennms-event-forwarder-kafka</feature>
    <bundle>mvn:org.opennms/opennms-services/${project.version}</bundle>
    <bundle>mvn:org.opennms.core/org.opennms.core.daemon-loader-<name>/${project.version}</bundle>
    <!-- Additional daemon-specific bundles -->
</feature>
```

### 3. Container Overlay

`opennms-container/strike-fighter/<name>-overlay/`:

- `etc/featuresBoot.d/<name>.boot` → `opennms-daemon-<name>`
- `etc/org.opennms.core.event.forwarder.kafka.cfg` → Kafka bootstrap servers, topic
- `etc/org.opennms.features.kafka.producer.client.cfg` → Kafka client config

### 4. Docker Compose Service

```yaml
<name>:
  image: opennms/daemon:${VERSION}
  environment:
    POSTGRES_HOST: postgres
    JAVA_OPTS: -Dorg.opennms.tsid.node-id=<N>
  volumes:
    - ./<name>-overlay:/opt/daemon-overlay:ro
  healthcheck:
    test: ["CMD-SHELL", "curl -sf -u admin:admin http://localhost:8181/sentinel/rest/health/probe || exit 1"]
```

### Special Cases

- **Trapd**: UDP port 1162 exposed, `features/events/traps/` bundles in feature
- **Syslogd**: UDP port 10514 exposed, `features/events/syslog/` bundles in feature
- **Notifd**: Needs notification config files mounted (users.xml, groups.xml, etc.)
- **Discovery**: Needs discovery-configuration.xml mounted

## TSID Node-ID Assignments

| Container | Node-ID |
|-----------|---------|
| Core | 1 |
| Alarmd | 2 |
| Webapp | 3 |
| Pollerd | 4 |
| Collectd | 5 |
| Rtcd | 6 |
| PassiveStatusd | 7 |
| Notifd | 8 |
| Discovery | 9 |
| Trapd | 10 |
| Syslogd | 11 |
| Ticketer | 12 |
| EventTranslator | 13 |

## Implementation Phases

### Phase 1: Delete Dead Daemons + Dead Event Infrastructure

Remove Vacuumd, Statsd, Actiond, Ackd daemon code entirely. Remove events table
writes, EventDao, event REST endpoints, event UI pages, Jasper Reports.

### Phase 2: Extract EASY Daemons

Rtcd and PassiveStatusd — minimal dependencies, no event subscriptions,
simple DAO/config needs. Prove the pattern works for these simple cases.

### Phase 3: Extract MEDIUM Daemons

Notifd, Discovery, Trapd, Syslogd, TroubleTicketer, EventTranslator —
event subscriptions, config factories, UDP listeners.

### Phase 4: Integration and Cleanup

Update Strike Fighter docker-compose.yml with all 15 services. Update core
container to disable all extracted daemons. End-to-end smoke test.

### Parallelization

```
Phase 1 (delete) ────────────────────→ ┐
Phase 2 (easy extractions) ──────────→ ├─→ Phase 4 (integration)
Phase 3 (medium extractions) ────────→ ┘
```

Phases 1-3 are internally parallelizable — each daemon deletion/extraction is
independent. Phase 4 depends on all three completing.
