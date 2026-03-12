# Minion E2E Pipeline — Comprehensive Work Report

## Overview

**Objective:** Get the `test-minion-e2e.sh` end-to-end test passing, validating the full Minion trap pipeline:

```
Minion → Kafka Sink → Trapd → EventCreator → KafkaEventForwarder → Kafka
  → EventTranslator → KafkaEventForwarder(+alarm-data) → Kafka → Alarmd → PostgreSQL
```

**Result:** 11/11 tests passing across 3 phases.

**Timeline:** Work spanned two sessions (Mar 11 evening → Mar 12 early morning)
- Session 1 (`6b45c103e67`): Mar 11, 20:15 — initial test script, Minion compose config, KafkaSinkBridge
- Session 2 (`5a35633ac63`): Mar 12, 02:24 — debugging and fixing the three pipeline bugs

---

## Bugs Found and Fixed

### Bug 1: Provisiond PlatformTransactionManager Race Condition
**Symptom:** Provisiond container failed to start — `Provisioner.start()` → `@Transactional` call threw `IllegalStateException` because no transaction manager was available.

**Root Cause:** The `<onmsgi:reference>` for `PlatformTransactionManager` creates a non-blocking proxy. The daemon-loader-provisiond Spring context reached `Provisioner.start()` at 06:43:55, but `dao-impl` didn't create `HibernateTransactionManager` until 06:43:59 — a 4-second gap.

**Fix:** Changed to `<osgi:reference>` in `applicationContext-daemon-loader-provisiond.xml`, which blocks context startup until the OSGi Framework service is registered.

**File:** `core/daemon-loader-provisiond/src/main/resources/META-INF/opennms/applicationContext-daemon-loader-provisiond.xml`

---

### Bug 2: EventConfInitializer Blueprint Race Condition
**Symptom:** All daemon containers forwarded events to Kafka **without severity or alarm-data**. Events on `opennms-fault-events` had no `<alarm-data>` element, so Alarmd couldn't create alarms.

**Root Cause:** The Blueprint `init-method` for `EventConfInitializer` ran at 06:44:01, but `EventConfEventDao` (from `distributed-dao-impl`) wasn't registered in the OSGi Framework registry until 06:44:03. The Blueprint reference was `availability="optional"`, so the initializer got a proxy that immediately threw `ServiceUnavailableException`. EventConf enrichment was permanently disabled for the container's lifetime.

**Fix:** Replaced synchronous `init()` with a daemon-thread retry loop (max 10 attempts, 3-second interval). On the EventTranslator container, attempt 1 fails, attempt 2 succeeds after 3 seconds — loading 156 event definitions from the database.

**File:** `core/event-forwarder-kafka/src/main/java/org/opennms/core/event/forwarder/kafka/EventConfInitializer.java`

---

### Bug 3: AlarmPersisterImpl Null systemId (Minion Not Registered)
**Symptom:** Alarmd received events with alarm-data but threw `DataIntegrityViolationException`: `null value in column "systemid" of relation "alarms" violates not-null constraint`.

**Root Cause:** The event's `<dist-poller>` was `minion-default-01`. `AlarmPersisterImpl.resolveDistPoller()` called `DistPollerDao.get("minion-default-01")`, which returned null for two reasons:
1. `DistPollerDao` queries with discriminator `"OpenNMS"`, but Minion entries use discriminator `"Minion"` (single-table inheritance)
2. In Delta-V, Minions don't register via REST API — the `monitoringsystems` table only had the default OpenNMS entry

**Fix:** Added fallback to `whoami()` when `DistPollerDao.get()` returns null, preventing the NOT NULL constraint violation. The Minion identity is still preserved in the alarm's `last_event_data` XML.

**File:** `opennms-alarms/daemon/src/main/java/org/opennms/netmgt/alarmd/AlarmPersisterImpl.java`

---

## New Code Written

### KafkaSinkBridge (162 lines)
**File:** `core/daemon-loader-trapd/src/main/java/org/opennms/core/daemon/loader/KafkaSinkBridge.java`

Bridges Kafka Sink topic consumption to the local `LocalMessageConsumerManager`. In Delta-V, the Minion forwards traps to `OpenNMS.Sink.Trap` via Kafka. This bridge:
- Waits for `TrapSinkConsumer` to register (provides the SinkModule)
- Creates a `KafkaConsumer` subscribed to `OpenNMS.Sink.Trap`
- Deserializes `SinkMessage` protobuf → `byte[]` → `Message` via module's `unmarshal()`
- Dispatches to `LocalMessageConsumerManager` → `TrapSinkConsumer`

### test-minion-e2e.sh (353 lines)
**File:** `opennms-container/delta-v/test-minion-e2e.sh`

3-phase end-to-end integration test:
- **Phase 1:** coldStart trap → Minion → Kafka Sink → Trapd → newSuspect → Provisiond
- **Phase 2:** linkDown trap → EventTranslator → SNMP_Link_Down alarm created
- **Phase 3:** linkUp trap → alarm CLEARED

Features: Kafka consumer monitoring (3 topics), webapp-optional REST checks, InterfaceToNodeCache refresh wait, `--verbose`/`--cleanup` flags.

---

## Build Times

| Task | Duration | Exit Code |
|------|----------|-----------|
| Full Maven build (excl. db-init) | ~45 min (prior session) | 0 |
| Horizon + Daemon assembly | ~15 min (prior session) | 0 |
| Docker image builds (Horizon, Daemon, db-init) | ~10 min (prior session) | 0 |
| Build `event-forwarder-kafka` module | **9 min 36 sec** | 0 |
| Build `opennms-alarmd` module | **9 min 57 sec** | 0 |
| Build `daemon-loader-trapd` module | ~8 min (prior session) | 0 |
| Build `daemon-loader-provisiond` module | ~8 min (prior session) | 0 |
| Container restart (eventtranslator + trapd + alarmd) | **~30 sec** | — |
| Container restart (alarmd only) | **~20 sec** | — |
| E2E test execution | **~3 min 30 sec** | 0 |

---

## Debugging Timeline (This Session)

| Time | Activity |
|------|----------|
| Start | Assessed container state — 16 containers using 13.5/15.8 GiB RAM |
| +2 min | Stopped 9 non-essential containers → freed ~5 GiB (down to ~9 GiB) |
| +5 min | Discovered Minion trap listener active (listening on 0.0.0.0:1162) |
| +8 min | Verified Kafka topics have data (300 Sink messages, fault-events populated) |
| +12 min | **First test run** — 9 pass, 2 fail (alarm not in PostgreSQL) |
| +15 min | Identified alarm-data missing from translated events on Kafka |
| +20 min | Found EventConfInitializer race condition in eventtranslator Karaf logs |
| +25 min | Explored EventConfInitializer code + Blueprint wiring |
| +30 min | Implemented retry logic in EventConfInitializer |
| +40 min | Built event-forwarder-kafka (9:36), restarted containers |
| +45 min | Verified EventConfInitializer retry succeeds (attempt 2, 156 event defs) |
| +48 min | **Second test run** — 9 pass, 2 fail (still no alarm) |
| +50 min | Found AlarmPersisterImpl `null systemId` constraint violation in Alarmd logs |
| +55 min | Traced to `DistPollerDao.get("minion-default-01")` → null (discriminator mismatch) |
| +58 min | Implemented `whoami()` fallback in `resolveDistPoller()` |
| +68 min | Built opennms-alarmd (9:57), added JAR bind mount, restarted alarmd |
| +72 min | **Third test run — 11 pass, 0 fail** |
| +75 min | Committed all changes, updated memory |

---

## Files Modified (36 total across both commits)

**Core Java fixes (3 files, +76/-12 lines):**
- `EventConfInitializer.java` — retry loop for Blueprint race condition
- `AlarmPersisterImpl.java` — null systemId fallback
- `applicationContext-daemon-loader-provisiond.xml` — osgi:reference for transactionManager

**New Minion pipeline (3 files, +214 lines):**
- `KafkaSinkBridge.java` — Kafka Sink → LocalMessageConsumerManager bridge
- `LocalMessageConsumerManager.java` — dispatch integration with KafkaSinkBridge
- `applicationContext-daemon-loader-trapd.xml` — KafkaSinkBridge wiring

**Docker Compose & test (3 files, +107/-25 lines):**
- `docker-compose.yml` — alarmd JAR bind mount, Trapd cache refresh, Minion config
- `test-minion-e2e.sh` — webapp-optional, Minion health check via `docker ps`, cache wait
- `minion-overlay/features.boot` — Minion Karaf feature boot config

**Overlay & config (26 files, +1224/-94 lines):**
- 14 daemon health ignore configs
- Webapp overlay JARs (updated after EventDao/Notifd removal)
- Features.xml, features.cfg, build.sh, full-assembly POM cleanups
