# Minion-Mandatory Near-Term Items Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add Docker Compose profiles, a Default Minion container, and update deploy.sh — delivering the actionable near-term items from the Minion-mandatory deferral design.

**Architecture:** Native Docker Compose `profiles:` replace deploy.sh's bash case-statement. A Default-location Minion (`opennms/minion`) joins all profiles as the network edge. The Minion uses Kafka for RPC/Sink/Twin IPC and falls back to webapp's REST API for config sync until Twin API migration.

**Tech Stack:** Docker Compose profiles, opennms/minion image, Kafka IPC transport, bash.

**Design Doc:** `docs/plans/2026-03-09-minion-mandatory-deferral-design.md`

---

## Task 1: Add Docker Compose Profiles to Existing Services

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml`

Infrastructure services (postgres, kafka, core, webapp) have NO `profiles:` — they always start. Each daemon service gets profile labels controlling which deployment modes include it.

**Step 1: Add `profiles:` to each daemon service**

Add the `profiles:` key after the service name (before `image:`) for each daemon service according to this mapping:

| Service | profiles |
|---------|----------|
| postgres | (none — always starts) |
| kafka | (none — always starts) |
| core | (none — always starts) |
| webapp | (none — always starts) |
| alarmd | `[lite, passive, full]` |
| pollerd | `[lite, full]` |
| collectd | `[lite, full]` |
| rtcd | `[lite, full]` |
| passivestatusd | `[full]` |
| notifd | `[lite, full]` |
| discovery | `[lite, full]` |
| trapd | `[passive, full]` |
| syslogd | `[passive, full]` |
| ticketer | `[full]` |
| eventtranslator | `[full]` |
| enlinkd | `[full]` |
| scriptd | `[full]` |
| alarmd | `[lite, passive, full]` |

For each service that needs profiles, add the `profiles:` key as the first property under the service name. Example for pollerd:

```yaml
  pollerd:
    profiles: [lite, full]
    image: opennms/daemon:${VERSION}
    # ... rest unchanged
```

Example for trapd:

```yaml
  trapd:
    profiles: [passive, full]
    image: opennms/daemon:${VERSION}
    # ... rest unchanged
```

Do NOT add `profiles:` to postgres, kafka, core, or webapp — they must always start.

**Step 2: Verify compose config parses**

Run: `cd opennms-container/delta-v && docker compose config --profiles full --services`

Expected: All 17 services listed (postgres, kafka, core, webapp, alarmd, pollerd, collectd, rtcd, passivestatusd, notifd, discovery, trapd, syslogd, ticketer, eventtranslator, enlinkd, scriptd).

**Step 3: Verify profile subsets**

Run: `cd opennms-container/delta-v && docker compose config --profiles lite --services`

Expected: postgres, kafka, core, webapp, alarmd, pollerd, collectd, notifd, discovery, rtcd (10 services).

Run: `cd opennms-container/delta-v && docker compose config --profiles passive --services`

Expected: postgres, kafka, core, webapp, alarmd, trapd, syslogd (7 services).

Run: `cd opennms-container/delta-v && docker compose config --services`

Expected: postgres, kafka, core, webapp (4 services — only always-on infrastructure).

**Step 4: Commit**

```bash
git add opennms-container/delta-v/docker-compose.yml
git commit -m "feat: add native Docker Compose profiles to delta-v services

Profiles: lite (10 svc), passive (7 svc), full (17 svc).
Infrastructure (postgres, kafka, core, webapp) always starts.
Replaces algorithmic profile selection in deploy.sh."
```

---

## Task 2: Add Default Minion Container

**Files:**
- Modify: `opennms-container/delta-v/docker-compose.yml`

**Step 1: Add minion service definition**

Add the following service definition after the `webapp` service and before `pollerd` in docker-compose.yml. The minion has NO `profiles:` — it always starts (it's infrastructure, like core and webapp):

```yaml
  minion:
    image: opennms/minion:${VERSION}
    container_name: delta-v-minion
    hostname: minion-default-01
    depends_on:
      core:
        condition: service_healthy
      webapp:
        condition: service_healthy
      kafka:
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
      - "8301:8201"
    healthcheck:
      test: ["CMD", "/health.sh"]
      interval: 30s
      timeout: 10s
      retries: 10
      start_period: 60s
```

Note: Trap port (1162/udp) and syslog port (1514/udp) are NOT mapped on the Minion by default. The standalone Trapd and Syslogd containers already claim these host ports. If a user wants the Minion to receive traps/syslog directly (instead of via standalone Trapd/Syslogd containers), they can add port mappings manually or use a custom override file.

**Step 2: Add minion-data volume**

Add `minion-data:` to the `volumes:` section at the bottom of the file, alongside the existing volume declarations:

```yaml
volumes:
  pgdata:
  kafkadata:
  opennms-etc:
  opennms-data:
  webapp-etc:
  webapp-data:
  minion-data:
  pollerd-data:
  # ... rest unchanged
```

**Step 3: Update deploy.sh image check**

In `opennms-container/delta-v/deploy.sh`, update the image verification loop (line 38) to include the minion image:

```bash
    for img in "opennms/horizon:$VERSION" "opennms/daemon:$VERSION" "opennms/alarmd:$VERSION" "opennms/minion:$VERSION"; do
```

**Step 4: Verify compose config parses with minion**

Run: `cd opennms-container/delta-v && docker compose config --services`

Expected: postgres, kafka, core, webapp, minion (5 always-on services).

Run: `cd opennms-container/delta-v && docker compose config --profiles full --services`

Expected: All 18 services (17 previous + minion).

**Step 5: Commit**

```bash
git add opennms-container/delta-v/docker-compose.yml opennms-container/delta-v/deploy.sh
git commit -m "feat: add Default Minion container to delta-v compose

Minion registers at location 'Default' with Kafka IPC transport.
Included in all profiles (no profiles: key = always starts).
REST config sync points to webapp container as workaround
until Twin API migration eliminates REST dependency.
Trap/syslog ports not mapped (standalone Trapd/Syslogd own those)."
```

---

## Task 3: Update deploy.sh to Use Compose Profiles

**Files:**
- Modify: `opennms-container/delta-v/deploy.sh`

**Step 1: Replace do_up() function**

Replace the entire `do_up()` function (lines 34-67) with:

```bash
do_up() {
    log "Starting Delta-V (version $VERSION)..."

    # Check images exist
    for img in "opennms/horizon:$VERSION" "opennms/daemon:$VERSION" "opennms/alarmd:$VERSION" "opennms/minion:$VERSION"; do
        docker image inspect "$img" >/dev/null 2>&1 || err "Image $img not found. Run ./build.sh first."
    done

    local profile="${1:-}"
    if [ -n "$profile" ]; then
        log "Using profile: $profile"
        docker compose --profile "$profile" up -d
    else
        log "Starting infrastructure only (core + webapp + minion)"
        docker compose up -d
    fi

    log "Waiting for services to start..."
    log "Run './deploy.sh status' to check progress."
    log "Web UI: http://localhost:8980/opennms (admin/admin)"
}
```

**Step 2: Update usage text**

Replace the `Profiles:` section in the `usage()` function with:

```bash
Profiles:
  (none)    Infrastructure only: postgres + kafka + core + webapp + minion
  lite      + essential daemons (alarmd, pollerd, collectd, notifd, discovery, rtcd)
  passive   + trap/syslog receivers (alarmd, trapd, syslogd)
  full      All 18 services
```

And update the Examples section:

```bash
Examples:
  ./deploy.sh up                    # Infrastructure only (5 services)
  ./deploy.sh up full               # Start everything (18 services)
  ./deploy.sh up passive            # Trap/syslog receivers with auto-discovery
  ./deploy.sh up lite               # Core + essential daemons (11 services)
  ./deploy.sh logs alarmd           # Tail alarmd logs
  ./deploy.sh shell core            # Karaf shell on core
  ./deploy.sh test                  # Verify deployment
  ./deploy.sh reset && ./deploy.sh up  # Fresh start
```

**Step 3: Verify deploy.sh syntax**

Run: `bash -n opennms-container/delta-v/deploy.sh && echo "SYNTAX OK"`

Expected: `SYNTAX OK`

**Step 4: Commit**

```bash
git add opennms-container/delta-v/deploy.sh
git commit -m "refactor: deploy.sh uses native Docker Compose profiles

Removes bash case-statement profile selection. Profiles are now
declared in docker-compose.yml. deploy.sh passes --profile flag
directly to docker compose. Default (no profile) starts infra only."
```

---

## Task 4: Verify End-to-End (Manual Smoke Test)

This task is manual — not automatable in CI without running Docker.

**Step 1: Test default profile (infrastructure only)**

Run: `cd opennms-container/delta-v && ./deploy.sh up`

Verify 5 services start: postgres, kafka, core, webapp, minion.

Run: `./deploy.sh status`

Expected: All 5 services running/healthy.

**Step 2: Test minion registration**

Once webapp is healthy, check that the Minion registered:

Run: `curl -sf -u admin:admin http://localhost:8980/opennms/rest/minions | python3 -m json.tool`

Expected: One minion with `id: minion-default-01`, `location: Default`, `status: UP`.

If the Minion fails to register (REST dependency issue), check minion logs:

Run: `docker compose logs minion --tail=50`

Document any errors — this validates the REST workaround (pointing at webapp).

**Step 3: Test lite profile**

Run: `./deploy.sh reset && ./deploy.sh up lite`

Verify 11 services start: infrastructure (5) + alarmd, pollerd, collectd, notifd, discovery, rtcd.

**Step 4: Test full profile**

Run: `./deploy.sh reset && ./deploy.sh up full`

Verify all 18 services start.

**Step 5: Run existing test suite**

Run: `./deploy.sh test`

Expected: All 5 tests pass (services running, web UI, REST API, PostgreSQL, Kafka topics).
