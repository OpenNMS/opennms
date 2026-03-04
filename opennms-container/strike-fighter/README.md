# Strike Fighter: OpenNMS Microservice Mode

Run OpenNMS in split-architecture mode with **Core** (all daemons except Alarmd) and a **standalone Alarmd** consuming fault events from Kafka.

```
┌──────────┐    ┌──────────┐    ┌───────────────────────────────┐    ┌───────────────┐
│ postgres │◄───┤  kafka   │◄───┤  core (Alarmd disabled)       │    │    alarmd      │
│          │    │  (KRaft) │    │  ┌──────────────────────────┐ │    │               │
│  events  │    │          │    │  │ Kafka Producer           │ │    │  Consumes     │
│  alarms  │    │  fault   │───►│  │ → publishes fault events │ │    │  Kafka topic  │
│  nodes   │    │  events  │    │  └──────────────────────────┘ │    │  Creates      │
│          │    │  topic   │    │  ┌──────────────────────────┐ │    │  alarms       │
│          │◄───┤          │    │  │ ActiveMQ (TCP :61616)    │─┼───►│               │
│          │    │          │    │  │ → MessageBus JMS IPC     │ │    │  ActiveMQ     │
│          │    └──────────┘    │  └──────────────────────────┘ │    │  subscriber   │
│          │◄───────────────────┤  Web UI :8980                 │    │               │
│          │◄───────────────────┼───────────────────────────────┼────┤  DB writer    │
└──────────┘                    └───────────────────────────────┘    └───────────────┘
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
```

## Quick Start

```bash
cd opennms-container/strike-fighter
docker compose up -d
```

Wait for all services to become healthy:

```bash
docker compose ps
```

Access the Web UI at **http://localhost:8980/opennms** (admin / admin).

## Services

| Service  | Image                         | Ports       | Purpose                        |
|----------|-------------------------------|-------------|--------------------------------|
| postgres | postgres:15                   | 5432        | Shared database                |
| kafka    | bitnami/kafka:3.6 (KRaft)     | —           | Event bus (fault events topic) |
| core     | opennms/horizon:${VERSION}    | 8980, 8101  | OpenNMS Core (Alarmd disabled) |
| alarmd   | opennms/alarmd:${VERSION}     | 8201        | Standalone Alarmd              |

## Verification

```bash
# 1. Confirm Alarmd is disabled in Core
docker compose exec core grep -i "alarmd" /opt/opennms/etc/service-configuration.xml
# Should show: enabled="false"

# 2. List Kafka topics
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list

# 3. Send a test event
curl -u admin:admin -X POST http://localhost:8980/opennms/rest/events \
  -H "Content-Type: application/xml" \
  -d '<event><uei>uei.opennms.org/alarms/trigger</uei><severity>7</severity></event>'

# 4. Check that an alarm was created (by standalone Alarmd)
docker compose exec postgres psql -U opennms -d opennms \
  -c "SELECT alarm_id, alarm_uei, severity FROM alarms ORDER BY lasteventtime DESC LIMIT 5;"
```

## Karaf Shell Access

```bash
# Core Karaf (port 8101)
ssh -p 8101 -o StrictHostKeyChecking=no admin@localhost

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

## Troubleshooting

**Images not found:** Ensure you've built the container images per the "Building Images" section above. Run `docker images | grep opennms` to verify.

**Port conflicts:** If ports 8980, 5432, or 8101 are already in use, either stop the conflicting service or adjust the port mappings in `docker-compose.yml`.

**Core not starting:** Check logs with `docker compose logs core --tail=100`. The most common issue is PostgreSQL not being ready — the healthcheck dependency should handle this, but the DB init can take time on first run.

**Alarmd not connecting:** Check `docker compose logs alarmd --tail=100`. Verify that Core's ActiveMQ TCP transport is up by looking for "openwire" in Core logs. The `failover:` protocol will retry automatically.

**Kafka not ready:** The Kafka healthcheck has a 30-second start period. If it fails, check `docker compose logs kafka --tail=50` for KRaft initialization errors.
