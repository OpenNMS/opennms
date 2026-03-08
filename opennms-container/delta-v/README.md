# Delta-V: OpenNMS Microservice Mode

Run OpenNMS in split-architecture mode with **Core** (remaining daemons), **Webapp** (JettyServer only), **standalone Alarmd** consuming fault events from Kafka, **standalone Pollerd** polling services, and **standalone Collectd** collecting performance data independently.

```
┌──────────┐  ┌──────────┐  ┌──────────────────────────────────┐  ┌───────────────┐
│ postgres │  │  kafka   │  │  core (opennms/horizon)          │  │    alarmd      │
│          │  │  (KRaft) │  │  Daemons: Eventd, Provisiond,    │  │               │
│  events  │◄─┤          │◄─┤  Daemons: Eventd, Provisiond,    │  │  Consumes     │
│  alarms  │  │  fault   │  │  etc.                             │  │  Kafka topic  │
│  nodes   │  │  events  │  │  Alarmd: DISABLED                │  │  Creates      │
│          │  │  topic   │  │  Pollerd: DISABLED               │  │  alarms       │
│          │  │          │  │  Collectd: DISABLED              │  │               │
│          │  │          │  │  JettyServer: DISABLED            │  └───────────────┘
│          │  │          │  │  ActiveMQ TCP: :61616             │
│          │  │          │  └──────────────────────────────────┘
│          │  │          │                    ▲ ActiveMQ
│          │  │          │                    │
│          │  │          │  ┌─────────────────┴─────────────────┐
│          │  │          │◄─┤  pollerd (opennms/daemon)         │
│          │  │          │  │  Karaf-only: Pollerd via OSGi     │
│          │  │          │  │  No Manager/Eventd overhead       │
│          │  │          │  └───────────────────────────────────┘
│          │  │          │
│          │  │          │  ┌───────────────────────────────────┐
│          │  │          │◄─┤  collectd (opennms/horizon)       │
│          │  └──────────┘  │  Daemons: Collectd ONLY           │
│          │                │  Publishes fault events to Kafka   │
│          │                └───────────────────────────────────┘
│          │
│          │  ┌─────────────────────────────────────────────────┐
│          │◄─┤  webapp (opennms/horizon)                       │
│          │  │  Daemons: JettyServer ONLY                      │
│          │  │  All other daemons: DISABLED                    │
│          │  │  Web UI: :8980                                  │
└──────────┘  └─────────────────────────────────────────────────┘
```

## Prerequisites

- Docker Engine 24+ with Docker Compose v2
- Built container images (see below)

## Building Images

From the repository root:

```bash
# 1. Compile the project
./compile.pl -DskipTests

# 2. Assemble the distribution
./assemble.pl -Dopennms.home=/opt/opennms -DskipTests -p dir

# 3. Build Core (Horizon) image
cd opennms-container/core && make image

# 4. Build Alarmd image
cd ../alarmd && make image

# 5. Build Daemon image (for Pollerd)
cd ../daemon && make image
```

## Quick Start

```bash
cd opennms-container/delta-v
docker compose up -d
```

Wait for all services to become healthy:

```bash
docker compose ps
```

Access the Web UI at **http://localhost:8980/opennms** (admin / admin).

## Services

| Service  | Image                         | Ports        | Purpose                                                |
|----------|-------------------------------|--------------|--------------------------------------------------------|
| postgres | postgres:15                   | 5432         | Shared database                                        |
| kafka    | apache/kafka:latest (KRaft)   | —            | Event bus (fault events topic)                         |
| core     | opennms/horizon:${VERSION}    | 8101, 61616  | OpenNMS daemons (Alarmd + Pollerd + Collectd + Jetty disabled) |
| webapp   | opennms/horizon:${VERSION}    | 8980, 8102   | JettyServer (Web UI only)                              |
| pollerd  | opennms/daemon:${VERSION}     | 8103         | Karaf-only Pollerd (OSGi wiring, no Manager/Eventd)    |
| collectd | opennms/horizon:${VERSION}    | 8104         | Standalone Collectd (collects data, publishes to Kafka) |
| alarmd   | opennms/alarmd:${VERSION}     | 8201         | Standalone Alarmd (consumes Kafka, creates alarms)     |

## Verification

```bash
# 1. All 7 services healthy
docker compose ps

# 2. Verify Pollerd is disabled in Core
docker compose exec core grep -i "pollerd" /opt/opennms/etc/service-configuration.xml
# Should show: enabled="false"

# 3. Verify Pollerd daemon feature is installed (Karaf-only container)
docker compose exec pollerd /opt/daemon/bin/client "feature:list -i | grep pollerd"
# Should show: opennms-daemon-pollerd

# 4. Access Web UI (served by webapp container)
curl -u admin:admin http://localhost:8980/opennms/rest/info

# 5. Send a test event via REST API (webapp → Eventd)
curl -u admin:admin -X POST http://localhost:8980/opennms/rest/events \
  -H "Content-Type: application/xml" \
  -d '<event><uei>uei.opennms.org/alarms/trigger</uei><severity>7</severity></event>'

# 6. Check that an alarm was created (by standalone Alarmd)
docker compose exec postgres psql -U opennms -d opennms \
  -c "SELECT alarm_id, alarm_uei, severity FROM alarms ORDER BY lasteventtime DESC LIMIT 5;"

# 7. Check Pollerd container logs
docker compose logs pollerd --tail=50

# 8. Verify Collectd is disabled in Core
docker compose exec core grep -i "collectd" /opt/opennms/etc/service-configuration.xml
# Should show: enabled="false"

# 9. Check Collectd container logs
docker compose logs collectd --tail=50
```

## Karaf Shell Access

```bash
# Core Karaf (port 8101)
ssh -p 8101 -o StrictHostKeyChecking=no admin@localhost

# Webapp Karaf (port 8102)
ssh -p 8102 -o StrictHostKeyChecking=no admin@localhost

# Pollerd Karaf (port 8103, daemon container SSH port 8201 mapped to 8103)
ssh -p 8103 -o StrictHostKeyChecking=no admin@localhost

# Collectd Karaf (port 8104)
ssh -p 8104 -o StrictHostKeyChecking=no admin@localhost

# Alarmd Karaf (port 8201)
ssh -p 8201 -o StrictHostKeyChecking=no admin@localhost
```

Default credentials: admin / admin

## Shutdown

```bash
# Stop containers (preserves data volumes)
docker compose down

# Stop and remove all data (clean slate)
docker compose down -v
```

## Known Limitations

**Event subscription (Pollerd):** The daemon-based Pollerd uses `KafkaEventForwarder` which enables cross-container event delivery via Kafka. It can receive events from Core (e.g., `nodeGainedService`) and send events back (e.g., `nodeLostService`). The `opennms-daemon-pollerd` feature installs the full event forwarding stack.

**Event subscription (Collectd):** The Collectd container still uses `opennms/horizon` and does not receive real-time events from Core. It reads schedules from the database at startup. To pick up newly provisioned services, restart the Collectd container. A future iteration will migrate Collectd to the daemon image as well.

## Troubleshooting

**Images not found:** Ensure you've built the container images per the "Building Images" section above. Run `docker images | grep opennms` to verify.

**Port conflicts:** If ports 8980, 5432, or 8101 are already in use, either stop the conflicting service or adjust the port mappings in `docker-compose.yml`.

**Core not starting:** Check logs with `docker compose logs core --tail=100`. The most common issue is PostgreSQL not being ready — the healthcheck dependency should handle this, but the DB init can take time on first run.

**Webapp not starting:** Check `docker compose logs webapp --tail=100`. The webapp depends on Core being healthy (DB schema must be initialized). Look for Spring context errors or OSGi resolution failures in the log output.

**Pollerd not starting:** Check `docker compose logs pollerd --tail=100`. The pollerd container uses the Karaf-only `opennms/daemon` image. Look for OSGi feature resolution errors (read backwards from "Unable to resolve root"). Verify Kafka connectivity and database access. Check that `opennms-daemon-pollerd` feature is installed via `feature:list -i`.

**Collectd not starting:** Check `docker compose logs collectd --tail=100`. Same pattern as Pollerd — depends on Core being healthy. Verify Kafka connectivity.

**Alarmd not connecting:** Check `docker compose logs alarmd --tail=100`. Verify that Core's ActiveMQ TCP transport is up by looking for "openwire" in Core logs. The `failover:` protocol will retry automatically.

**Kafka not ready:** The Kafka healthcheck has a 30-second start period. If it fails, check `docker compose logs kafka --tail=50` for KRaft initialization errors.

**Classloader conflicts:** The webapp container exists specifically to isolate the Jetty webapp classloader from Karaf's OSGi bundles. If you see `UtilNamespaceHandler` or `NamespaceHandler` classloader errors, verify that JettyServer is disabled in Core and enabled only in the webapp container.
