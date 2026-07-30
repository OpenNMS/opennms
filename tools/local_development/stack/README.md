# OpenNMS developer stack (Docker Compose)

PostgreSQL, OpenNMS, VictoriaMetrics, and Grafana. Metrics are stored and read
through the Time Series Integration Layer using the
[Prometheus RemoteWrite plugin][plugin] — no RRD files involved.

## Quick start

```bash
cd tools/local_development/stack
cp .env.example .env             # optional; the defaults work as-is
docker compose up -d
docker compose logs -f opennms   # first boot initializes the database, 2-4 min
```

| Service | URL | Credentials |
|---|---|---|
| OpenNMS | http://localhost:8980/opennms | `admin` / `admin` |
| VictoriaMetrics | http://localhost:8428/vmui | — |
| Grafana | http://localhost:3000 | `admin` / `admin` (anonymous viewing on) |
| Karaf shell | `ssh admin@localhost -p 8101` | `admin` / `admin` |

Traps (`1162/udp`) and syslog (`10514/udp`) are published as well; PostgreSQL is
not. Every host port is overridable in `.env`, so this can run beside a locally
built OpenNMS that already owns 8980 and 8101.

With no devices configured the stack still collects JVM metrics over JMX, the
`OpenNMS-DB` JDBC collections, and ICMP response times for the built-in
`selfmonitor` node, so there is something to graph. First data lands after one
collection cycle (five minutes).

`docker compose down` keeps the data; add `-v` to wipe it.

## Running the image from your branch

```bash
# from the repository root
./assemble.pl -Dopennms.home=/opt/opennms -DskipTests
make -C opennms-container/core image      # tags opennms/horizon:37.0.0-SNAPSHOT

cd tools/local_development/stack
docker compose -f compose.yaml -f compose.local.yml up -d
```

## How the time series path is wired

- `OPENNMS_TIMESERIES_STRATEGY=integration` in `compose.yaml` is the only place
  the strategy is set; the image renders it into
  `etc/opennms.properties.d/_confd.timeseries.properties`.
- `plugin-init` downloads the plugin KAR into the volume mounted at
  `/opt/opennms/deploy`. Keep the file name
  `opennms-prometheus-remotewrite-plugin.kar`: Karaf derives the KAR name from
  it, and `featuresBoot.d/prometheus-remotewrite.boot` names it in its
  `wait-for-kar` attribute so the feature installs only once the KAR is unpacked.
- `overlay/etc/org.opennms.plugins.tss.prometheus.cfg` points the plugin at
  VictoriaMetrics — `/api/v1/write` to write, `/prometheus/api/v1` to read.
- `overlay/etc/opennms.conf` enables remote JMX on 18980, which the shipped
  self-monitoring needs (upstream ships those lines commented out).
- `timeseries-tss.properties` shortens the searcher cache TTL to 30s so new
  resources appear in the UI promptly. Raise it back up outside a dev stack.

Everything under `overlay/etc/` is mounted read-only at
`/opt/opennms-etc-overlay` and copied into `etc/` on every start: edit a file
there, then `docker compose restart opennms`.

Plugin documentation lives in
`docs/modules/deployment/pages/time-series-storage/timeseries/prometheus-remotewrite.adoc`.

## Verifying

```bash
# Is the plugin installed?
docker compose exec opennms /opt/opennms/bin/client -u admin -p admin \
  "feature:list -i | grep remotewrite"

# Are samples arriving? Non-zero after the first collection cycle.
curl -s 'http://localhost:8428/api/v1/series' \
  --data-urlencode 'match[]={resourceId!=""}' --data-urlencode 'start=0' | jq '.data | length'
```

For the read path, open a node in the UI and choose **Resource Graphs** — those
go through `/rest/measurements` into VictoriaMetrics.

## Troubleshooting

| Symptom | Check |
|---|---|
| No series in VictoriaMetrics | `docker compose exec opennms grep -iE 'prometheus\|tss' /opt/opennms/logs/karaf.log` |
| Feature not started | Confirm the KAR arrived: `docker compose exec opennms ls -l /opt/opennms/deploy` |
| Graphs empty but series exist | `readUrl` must be the query API base (`/prometheus/api/v1`), not the write URL |
| New resources missing from the UI | The searcher cache TTL has to expire first; the samples are already stored |
| OpenNMS restarting / OOM | Raise `ONMS_JAVA_OPTS` in `.env`; give Docker at least 6 GB |
| First boot fails on the database | `docker compose logs db`; reset with `docker compose down -v` |

[plugin]: https://github.com/OpenNMS-Plugins/opennms-prometheus-remotewrite-plugin
