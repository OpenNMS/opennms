# Next Session Prompt: PerspectivePollerd Migration & Dead Daemon Cleanup

> Copy everything below the line into the next Claude Code conversation.

---

## Context

We're on the `eventbus-redesign` branch of OpenNMS Horizon (Delta-V architecture). The Strike Fighter plan, Phase A core elimination, EventDao/Notifd/Minion REST elimination, and Minion E2E pipeline are all complete. The Delta-V architecture has 17 services running on 3 images (horizon, daemon, minion) with Kafka-only event transport.

Three cleanup items remain. A design spec has been written, reviewed (3 rounds of spec review — all issues resolved), and approved:

**Design spec:** `docs/plans/2026-03-12-perspectivepollerd-cleanup-design.md`

## What to Implement

### Task 1: PerspectivePollerd Daemon-Loader Module (the bulk of the work)

Create `core/daemon-loader-perspectivepoller/` following the established daemon-loader pattern (14 existing modules as reference). PerspectivePollerd is the last remaining daemon to extract.

**Key details (from the spec):**
- PerspectivePollerd implements `SpringServiceDaemon` → needs `SpringDaemonLifecycleManager` (like Ticketer)
- Uses real `LocationAwarePollerClient` (not LocalPollerClient like Pollerd) for Minion RPC delegation
- `LocationAwarePollerClientImpl` is wired directly in the daemon-loader XML (the `poller.client-rpc` bundle has no Spring-Context header)
- `LocationAwarePollerClientImpl` has 5 `@Autowired` fields: `ServiceMonitorRegistry`, `PollerClientRpcModule`, `RpcClientFactory`, `RpcTargetHelper`, `EntityScopeProvider`
- `RpcClientFactory` comes from `osgi:reference` (Kafka RPC Blueprint). All DAOs come from `onmsgi:reference`.
- `ServiceRegistry` from `osgi:reference` is required for all `onmsgi:reference` proxies to resolve
- `PollerClientRpcModule` needs a `pollerExecutor` bean (`@Qualifier("pollerExecutor")`)
- Event subscription via `AnnotationBasedEventListenerAdapter` for both daemon and tracker
- TSID node-id = 7
- `<context:annotation-config/>` required (constructor injection via `@Autowired`)

**Reference implementations:**
- `core/daemon-loader-pollerd/` — closest match (also a poller, same base dependencies)
- `core/daemon-loader-ticketer/` — same lifecycle pattern (`SpringDaemonLifecycleManager`)
- `core/daemon-loader-provisiond/` — same `NoOpTracerRegistry` and `EntityScopeProvider` pattern

**Files to create:**
- `core/daemon-loader-perspectivepoller/pom.xml` — bundle packaging, `Import-Package: *;resolution:=optional`, `DynamicImport-Package: *`, `Spring-Context` with `create-asynchronously:=true`
- `core/daemon-loader-perspectivepoller/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-perspectivepoller.xml` — flat Spring XML with all OSGi refs and local beans

**Files to modify:**
- `core/pom.xml` — add `<module>daemon-loader-perspectivepoller</module>`

### Task 2: Karaf Feature + Sentinel Assembly + Docker Compose

**Karaf feature** in `container/features/src/main/resources/features.xml`:
- New `opennms-daemon-perspectivepoller` feature
- Must include: `opennms-spring-extender`, `opennms-event-forwarder-kafka`, `opennms-distributed-core-impl`, `opennms-persistence`, `opennms-config`, `opennms-core-daemon`, `opennms-core-ipc-rpc-kafka`
- Bundles: collection API, thresholding API, icmp-api, poller-api, kv-store-api, poll-outages-api, mate-api, poller.client-rpc, opennms-services, perspectivepoller, daemon-loader-perspectivepoller

**Sentinel assembly** (`features/container/sentinel/pom.xml`):
- Add `opennms-daemon-perspectivepoller` to `<installedFeatures>`

**Docker Compose** (`opennms-container/delta-v/docker-compose.yml`):
- New `perspectivepollerd` service (image: `opennms/daemon:delta-v`, TSID=7, profiles: [full])
- POSTGRES_* env vars, db-init dependency, JAR overlay volumes (event-forwarder-kafka, features.xml, events.daemon)
- Overlay at `./perspectivepollerd-overlay/etc:/opt/sentinel-etc-overlay:ro`

**Container overlay** (`opennms-container/delta-v/perspectivepollerd-overlay/`):
- `etc/featuresBoot.d/perspectivepoller.boot`
- `etc/org.opennms.core.health.cfg.cfg` (health ignore: distributed.datasource, core-impl, dao-impl)

**Named volume:** `perspectivepollerd-data:`

This brings Delta-V from 17 → 18 services.

### Task 3: Remove Dead Notifd Mbean from JMX Config

Delete the `OpenNMS.Notifd` mbean block (lines 57-68) from `opennms-base-assembly/src/main/filtered/etc/jmx-datacollection-config.xml`. Notifd was fully eliminated — its JMX MBean definition is dead.

### Task 4: Verification

- Grep `opennms-base-assembly/src/main/filtered/etc/` for deleted daemon references (Notifd, Queued, Vacuumd, Statsd, Actiond, Ackd) — should be zero
- Confirm `promoteQueueData` absent from active configs
- Build the daemon-loader module
- Rebuild Sentinel assembly + Docker images
- Start PerspectivePollerd container and verify health

## Approach

Use the **writing-plans** skill to create a detailed implementation plan from the design spec, then use **subagent-driven-development** to execute it. The design spec has been through 3 rounds of review — all issues resolved. Do NOT re-brainstorm; go straight to planning and implementation.

## Key Files to Reference

- **Design spec:** `docs/plans/2026-03-12-perspectivepollerd-cleanup-design.md`
- **Reference daemon-loader (Pollerd):** `core/daemon-loader-pollerd/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-pollerd.xml`
- **Reference daemon-loader (Ticketer):** `core/daemon-loader-ticketer/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-ticketer.xml`
- **Karaf features:** `container/features/src/main/resources/features.xml` (Pollerd feature at line 2106)
- **Sentinel assembly:** `features/container/sentinel/pom.xml`
- **Docker Compose:** `opennms-container/delta-v/docker-compose.yml`
- **PerspectivePollerd source:** `features/perspectivepoller/src/main/java/org/opennms/netmgt/perspectivepoller/PerspectivePollerd.java`
- **LocationAwarePollerClientImpl:** `features/poller/client-rpc/src/main/java/org/opennms/netmgt/poller/client/rpc/LocationAwarePollerClientImpl.java`
- **JMX config:** `opennms-base-assembly/src/main/filtered/etc/jmx-datacollection-config.xml`
