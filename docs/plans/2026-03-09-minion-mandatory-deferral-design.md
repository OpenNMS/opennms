# Minion-Mandatory Architecture — Deferral & Near-Term Items

## Context

The eventbus-redesign branch achieved the Strike Fighter goal: 17 standalone daemon
containers communicating via Kafka event transport, end-to-end alarm flow proven
(trap → Trapd → EventCreator → Kafka → EventTranslator → enrich → Kafka → Alarmd →
PostgreSQL).

The next proposed goal was to make Minion the **only** component that touches the
network — eliminating direct SNMP/ICMP/etc. execution from core and daemon containers,
requiring a Minion for every location including "Default."

After analysis, this goal is **deferred** pending prerequisites described below.

## Why Minion-Mandatory Is Deferred

### Non-Distributable ServiceMonitors

Several `ServiceMonitor` implementations cannot run on a Minion because they depend on
in-JVM state, database access, or filesystem resources:

- **PassiveServiceMonitor** — Reads from `PassiveStatusKeeper.getInstance()`, a static
  singleton `HashMap<PassiveStatusKey, PollStatus>` in the same JVM. Comment in source:
  `// this retrieves data from the deamon so it is not Distributable`. Forces
  `getEffectiveLocation()` to return `"Default"` to prevent remote execution.
- **Other monitors TBD** — A full audit of all `ServiceMonitor` implementations is
  required to identify which are Minion-safe and which depend on local JVM state.

### Collectors Route SNMP, Not Logic

The `LocationAwareCollectorClient` pattern does not delegate full collector execution to
Minion. Instead, the collector runs locally and individual SNMP operations are proxied
to Minion via `LocationAwareSnmpClient` / `SnmpUtils`. The `KafkaRpcClientFactory`
decision point (line ~164) routes requests only when location differs:

```java
if (request.getLocation() == null || request.getLocation().equals(location)) {
    return module.execute(request);  // LOCAL execution
}
// else → Kafka RPC to Minion at target location
```

Making Pollerd/Collectd "schedulers only" (Option A) would require all monitors and
collectors to be fully executable on Minion — a much larger abstraction change than
exists today.

### Default Location Semantics

The string `"Default"` is hardcoded in `LocationUtils.DEFAULT_LOCATION_NAME` and
`MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID`, referenced 123 times across 50
files. Nodes provisioned without a location get `"Default"`. This string is preserved
as-is — no rename to "Local" or other alternative.

### Prerequisites for Revisiting

1. Audit all `ServiceMonitor` implementations for Minion compatibility
2. Audit all `ServiceCollector` implementations for full delegation capability
3. Solve PassiveStatusd shared-state problem (see below)
4. Potentially refactor monitors/collectors that depend on in-JVM state to use
   RPC-based communication or shared external state

## PassiveStatusd / Pollerd Shared-State Audit

### The Bug

In delta-v today, PassiveStatusd (TSID=7) and Pollerd (TSID=4) run in separate
containers. Both load the `opennms-services` bundle containing `PassiveStatusKeeper`
and `PassiveServiceMonitor`. Each JVM gets its own `PassiveStatusKeeper.s_instance`
singleton with its own `HashMap`.

When Pollerd executes `PassiveServiceMonitor.poll()`:
1. Calls `PassiveStatusKeeper.getInstance().getStatus(nodeLabel, ipAddr, svcName)`
2. Returns from the **local** (empty, uninitialized) map
3. Default return: `PollStatus.up()` (line 196)

Meanwhile, PassiveStatusd's `PassiveStatusKeeper` is the real instance — subscribed to
`passiveServiceStatus` events, maintaining the actual status table. The two JVMs never
communicate.

### Fix Options

| Option | Approach | Complexity | Notes |
|--------|----------|-----------|-------|
| A | PassiveStatusd publishes state changes to Kafka, PassiveServiceMonitor consumes | Medium | New Kafka topic + consumer in Pollerd |
| B | PassiveStatusd writes status to PostgreSQL, PassiveServiceMonitor reads from DB | Low | Adds DB latency to every poll cycle |
| **C** | **Merge PassiveStatusKeeper into Pollerd, eliminate PassiveStatusd** | **Low** | **Recommended** |
| D | Make PassiveServiceMonitor an RPC call to PassiveStatusd | Medium | New RPC module |

### Recommendation: Option C

PassiveStatusKeeper is fundamentally a Pollerd concern — it feeds poll results into the
polling cycle. The "daemon" wrapper (`PassiveStatusd`) exists only to host event
subscription and the hashtable. Moving `PassiveStatusKeeper` into the Pollerd container
(subscribing to `passiveServiceStatus` events via Kafka) eliminates the cross-container
state problem entirely. PassiveStatusd would be removed as a standalone daemon.

This is a niche feature (external systems injecting poll results) likely unused in most
deployments, so the fix priority is low. Document and defer to post-Strike-Fighter.

## Default Minion in Docker Compose

### Purpose

Add an `opennms/minion` container to the delta-v compose for:
- Proving IPC Sink/RPC/Twin paths work in the containerized deployment
- Receiving traps and syslog from monitored networks (network edge)
- Enabling SNMP RPC delegation for nodes at the Default location
- Future foundation for Minion-mandatory architecture

### Container Configuration

```yaml
minion:
  image: opennms/minion:${VERSION}
  hostname: minion-default-01
  depends_on:
    core:
      condition: service_healthy
  environment:
    MINION_ID: minion-default-01
    MINION_LOCATION: Default
    OPENNMS_HTTP_URL: http://webapp:8980/opennms
    OPENNMS_HTTP_USER: admin
    OPENNMS_HTTP_PASS: admin
    KAFKA_RPC_BOOTSTRAP_SERVERS: kafka:9092
    KAFKA_SINK_BOOTSTRAP_SERVERS: kafka:9092
    JAVA_OPTS: >-
      -Xms256m -Xmx512m
      -Djava.security.egd=file:/dev/./urandom
  volumes:
    - minion-data:/opt/minion/data
  ports:
    - "1162:1162/udp"
    - "1514:1514/udp"
    - "8301:8201"
```

### Key Decisions

- **All profiles**: The Default Minion is included in every profile (core, lite,
  passive, full) since it is the network edge for all deployment modes.
- **Kafka-only transport goal**: The Minion should communicate exclusively via Kafka
  (RPC + Sink + Twin). No gRPC dependency.
- **REST dependency workaround**: The Minion currently requires REST API access for
  Trapd/Syslogd configuration sync. Since core runs without JettyServer in delta-v,
  `OPENNMS_HTTP_URL` points to the webapp container as a workaround.
- **Port conflicts**: Trap port 1162 and syslog port 1514 overlap with standalone
  Trapd/Syslogd containers. When Minion is running, it receives traps/syslog directly
  from the network. The standalone Trapd/Syslogd containers listen on different internal
  ports (10162, 10514) and consume from Minion via Sink API when co-deployed.

### Investigation: Eliminate Minion REST Dependency

The Minion's REST API dependency (for Trapd/Syslogd configuration) is a long-standing
architectural concern. The Minion should operate purely over Kafka.

The IPC Twin API (`TwinSubscriber` / `TwinPublisher`) already provides the mechanism:
- Minion subscribes to a config key at boot
- Core responds with initial config object via RPC (over Kafka)
- Core pushes subsequent config updates via reverse Sink (over Kafka)

**Proposed solution**: Publish Trapd and Syslogd configurations through Twin publishers
on the core side. The Minion's `TwinSubscriber` receives configs at boot and on change.
This eliminates the REST dependency entirely.

**Alternative**: Reverse RPC from Minion — Minion sends an RPC request to a Trapd/Syslogd
topic, the daemon container responds with its configuration. This adds new RPC modules
but avoids coupling config delivery to core.

Both approaches require investigation into what other configurations the Minion fetches
via REST beyond Trapd/Syslogd. Flag as a post-Strike-Fighter investigation.

## Docker Compose Profiles

### Current State

The `deploy.sh` script implements deployment profiles via a bash `case` statement,
selecting which services to pass to `docker compose up -d`. This is fragile and caused
miscommunication during integration testing when profiles were expected to be a
docker-compose native feature.

### Target State

Move profile logic to native Docker Compose `profiles:` declarations.

**Profile mapping:**

| Profile | Services | Use Case |
|---------|----------|----------|
| (default) | postgres, kafka, core, webapp, minion | Infrastructure + Default Minion |
| `lite` | default + alarmd, pollerd, collectd, notifd, discovery, rtcd | Essential daemons |
| `passive` | default + alarmd, trapd, syslogd | Trap/syslog receivers → alarms |
| `full` | all services | Complete deployment |

**Service profile assignments:**

| Service | Profiles | Notes |
|---------|----------|-------|
| postgres, kafka, core, webapp, minion | (none — always start) | Infrastructure |
| alarmd | lite, passive, full | Alarm persistence |
| pollerd, collectd | lite, full | Polling and collection |
| notifd, discovery, rtcd | lite, full | Notifications, discovery, RTC |
| trapd, syslogd | passive, full | UDP listeners |
| passivestatusd | full | Pending merge into Pollerd |
| ticketer, eventtranslator | full | Event processing |
| enlinkd, scriptd | full | Link discovery, scripting |

**deploy.sh simplification:**

```bash
do_up() {
    local profile="${1:-}"
    if [ -n "$profile" ]; then
        docker compose --profile "$profile" up -d
    else
        docker compose up -d  # default: infra + minion only
    fi
}
```

**Usage:**
```bash
docker compose up -d                      # infra + minion
docker compose --profile lite up -d       # + essential daemons
docker compose --profile passive up -d    # + trap/syslog receivers
docker compose --profile full up -d       # everything
```

## Investigate: Alarmd on opennms/daemon Image

### Background

The `opennms/alarmd` image was built first as a proof-of-concept standalone Karaf
container for alarm persistence. The `opennms/daemon` image was generalized later as
the template for all other daemon containers. Alarmd was never migrated.

### Differences

| Aspect | opennms/alarmd | opennms/daemon |
|--------|---------------|----------------|
| Assembly | `opennms-assemblies/alarmd/` | `opennms-assemblies/daemon/` |
| Entrypoint | Direct `karaf server` | `entrypoint.sh` (SSH keys, datasource cfg, overlay) |
| Overlay | Bash `cp` in compose command override | Built-in overlay directories |
| Datasource | Legacy `OPENNMS_BROKER_*` env vars | Runtime `.cfg` file generation |
| User | `alarmd` (10001) | `onmsd` (10001) |

### Investigation Items

1. Can Alarmd's Karaf feature (`opennms-alarm-persistence`) run on the opennms/daemon
   image with just an overlay change (featuresBoot + config files)?
2. What assembly differences exist — does `opennms-assemblies/alarmd/` include bundles
   not available in the daemon assembly?
3. Would unifying eliminate the separate Docker build, Makefile, and assembly module?
4. The ActiveMQ broker URL config (`OPENNMS_BROKER_URL`) — does the daemon image's
   config mechanism support this?

### Recommendation

Investigate migrating Alarmd to the opennms/daemon image to reduce build complexity
(one fewer image to build, push, and maintain). Flag as a post-Strike-Fighter cleanup
task. If the daemon assembly already contains the alarm persistence bundles, this may
be as simple as a new overlay directory and compose service definition.
