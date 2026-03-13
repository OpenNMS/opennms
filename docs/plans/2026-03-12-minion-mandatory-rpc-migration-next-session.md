# Next Session Prompt: Minion-Mandatory RPC Migration

## Context

We are on branch `eventbus-redesign` in the Delta-V fork (`pbrane/delta-v`). The previous session completed the PerspectivePollerd standalone daemon (PR #15) and then designed + planned the next major initiative: **migrating all daemon containers from Local*Client stubs to real KafkaRpcClientFactory-backed RPC implementations**, making Minion delegation mandatory for all polling, collection, detection, SNMP, ping, and DNS operations.

## What's Ready

- **Design spec**: `docs/plans/2026-03-12-minion-mandatory-rpc-migration-design.md` (committed)
- **Implementation plan**: `docs/plans/2026-03-12-minion-mandatory-rpc-migration-implementation.md` (committed, reviewed, all issues resolved)
- **Plan has 10 tasks across 3 chunks**, all with complete code, exact file paths, and build/verification commands

## What To Do

Execute the implementation plan using `superpowers:subagent-driven-development`. The plan file has the `> **For agentic workers:** REQUIRED:` header that triggers this workflow.

```
Please execute the implementation plan at docs/plans/2026-03-12-minion-mandatory-rpc-migration-implementation.md
```

## Task Summary

**Chunk 1 — Infrastructure (Tasks 1-3):**
1. Create `core/daemon-loader-shared` module (POM, NoOpTracerRegistry, LocalServiceDetectorRegistry, kafka-rpc-client-factory.xml)
2. Add `force-remote` flag to `KafkaRpcClientFactory` (prevents local RPC execution in daemon containers)
3. Add daemon-loader-shared bundle + `opennms-core-ipc-rpc-kafka` feature to 6 daemon Karaf features

**Chunk 2 — Daemon Migrations (Tasks 4-7):**
4. Provisiond → 3 clients (SNMP, Detector, DNS)
5. Discovery → 2 clients (Ping, Detector)
6. Pollerd → 1 client (Poller)
7. Collectd → 1 client (Collector)

**Chunk 3 — Enlinkd, Retrofit, Verification (Tasks 8-10):**
8. Enlinkd → 1 client (SNMP, replacing empty onmsgi:reference proxy)
9. Retrofit PerspectivePollerd to use shared fragment (replace inline block)
10. End-to-end verification (all 6 containers healthy + Minion E2E test)

## Key Architecture Notes

- **Shared XML fragment** (`kafka-rpc-client-factory.xml`): All 6 daemon-loaders import this. Contract: importing context must provide `distPollerDao` bean.
- **force-remote flag**: `-Dorg.opennms.core.ipc.rpc.force-remote=true` in JAVA_OPTS. All RPC routes through Kafka to Minion, even for same-location requests. Null locations default to daemon's own location.
- **PerspectivePollerd is the reference implementation** — already uses KafkaRpcClientFactory with real LocationAwarePollerClientImpl.
- **Build chain**: daemon-loader module → `container/features` → `features/container/sentinel` → `opennms-assemblies/daemon` → docker build. ~10-15 min per cycle.
- **Each daemon migration is independent** — can be verified individually after the infrastructure (Chunk 1) is in place.

## Build Tip

```bash
# Full image rebuild chain (needed after each daemon migration):
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
