# Configuring Horizon via Environment Variables

Configuration is applied at container startup by processing templates and static config files using environment variables.
Overlay config files (mounted to `/opt/opennms-etc-overlay`) are applied afterwards and take final precedence.

## Karaf SSH

| Environment Variable | Config key | Default |
|:---------------------|:-----------|:--------|
| `OPENNMS_KARAF_SSH_PORT` | `sshPort` in `org.apache.karaf.shell.cfg` | `8101` |
| `OPENNMS_KARAF_SSH_HOST` | `sshHost` in `org.apache.karaf.shell.cfg` | `0.0.0.0` |

## Timeseries / RRD

| Environment Variable | Config key | Default |
|:---------------------|:-----------|:--------|
| `OPENNMS_TIMESERIES_STRATEGY` | `org.opennms.timeseries.strategy` | `rrd` |
| `OPENNMS_RRD_STOREBYFOREIGNSOURCE` | `org.opennms.rrd.storeByForeignSource` | `true` |
| `OPENNMS_RRD_STRATEGYCLASS` | `org.opennms.rrd.strategyClass` | `org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy` |
| `OPENNMS_RRD_INTERFACEJAR` | `org.opennms.rrd.interfaceJar` | `/usr/share/java/jrrd2.jar` |
| `OPENNMS_LIBRARY_JRRD2` | `opennms.library.jrrd2` | `/usr/lib/jni/libjrrd2.so` |

Config written to `etc/opennms.properties.d/_container.timeseries.properties`.

## Newts / Cassandra

| Environment Variable | Config key | Default |
|:---------------------|:-----------|:--------|
| `OPENNMS_CASSANDRA_HOSTNAME` | `org.opennms.newts.config.hostname` | `hostname` |
| `OPENNMS_CASSANDRA_KEYSPACE` | `org.opennms.newts.config.keyspace` | `newts` |
| `OPENNMS_CASSANDRA_PORT` | `org.opennms.newts.config.port` | `9042` |
| `OPENNMS_CASSANDRA_USERNAME` | `org.opennms.newts.config.username` | `cassandra` |
| `OPENNMS_CASSANDRA_PASSWORD` | `org.opennms.newts.config.password` | `cassandra` |
| `OPENNMS_CASSANDRA_DATACENTER` | `org.opennms.newts.config.datacenter` | `datacenter1` |

Config written to `etc/opennms.properties.d/_container.newts.properties`.

## Trapd

| Environment Variable | XML attribute | Default |
|:---------------------|:--------------|:--------|
| `OPENNMS_TRAPD_ADDRESS` | `snmp-trap-address` | `*` |
| `OPENNMS_TRAPD_PORT` | `snmp-trap-port` | `1162` |
| `OPENNMS_TRAPD_NEW_SUSPECT_ON_TRAP` | `new-suspect-on-trap` | `false` |
| `OPENNMS_TRAPD_INCLUDE_RAW_MESSAGE` | `include-raw-message` | `false` |
| `OPENNMS_TRAPD_THREADS` | `threads` | `0` |
| `OPENNMS_TRAPD_QUEUE_SIZE` | `queue-size` | `10000` |
| `OPENNMS_TRAPD_BATCH_SIZE` | `batch-size` | `1000` |
| `OPENNMS_TRAPD_BATCH_INTERVAL` | `batch-interval` | `500` |

Config written to `etc/trapd-configuration.xml`.

> **Note:** These variables only take effect when the database is first initialized or upgraded (`-i`/`-s` without an existing `etc/configured`). At that point the database installer imports `etc/trapd-configuration.xml` into the database and moves the file to `etc_archive/`. From then on, OpenNMS reads Trapd's configuration from the database, so later changes to `OPENNMS_TRAPD_*` have no effect, even though the entrypoint still writes the file at every startup. Change an existing installation through *Integrations -> Trap Configuration* or the REST v2 API (`/api/v2/trapd`). The same was true with confd.

## Service configuration

Each OpenNMS service can be enabled or disabled via an environment variable. Set the variable to `true` or `false`.

| Environment Variable | Service | Default |
|:---------------------|:--------|:--------|
| `CORE_SERVICE_ALARMD_ENABLED` | Alarmd | `true` |
| `CORE_SERVICE_BSMD_ENABLED` | Bsmd | `true` |
| `CORE_SERVICE_TICKETER_ENABLED` | Ticketer | `true` |
| `CORE_SERVICE_CORRELATOR_ENABLED` | Correlator | `false` |
| `CORE_SERVICE_QUEUED_ENABLED` | Queued | `true` |
| `CORE_SERVICE_ACTIOND_ENABLED` | Actiond | `true` |
| `CORE_SERVICE_NOTIFD_ENABLED` | Notifd | `true` |
| `CORE_SERVICE_SCRIPTD_ENABLED` | Scriptd | `true` |
| `CORE_SERVICE_RTCD_ENABLED` | Rtcd | `true` |
| `CORE_SERVICE_POLLERD_ENABLED` | Pollerd | `true` |
| `CORE_SERVICE_SNMPPOLLER_ENABLED` | SnmpPoller | `false` |
| `CORE_SERVICE_ENHANCEDLINKD_ENABLED` | EnhancedLinkd | `true` |
| `CORE_SERVICE_COLLECTD_ENABLED` | Collectd | `true` |
| `CORE_SERVICE_DISCOVERY_ENABLED` | Discovery | `true` |
| `CORE_SERVICE_VACUUMD_ENABLED` | Vacuumd | `true` |
| `CORE_SERVICE_EVENTTRANSLATOR_ENABLED` | EventTranslator | `true` |
| `CORE_SERVICE_PASSIVESTATUSD_ENABLED` | PassiveStatusd | `true` |
| `CORE_SERVICE_STATSD_ENABLED` | Statsd | `true` |
| `CORE_SERVICE_PROVISIOND_ENABLED` | Provisiond | `true` |
| `CORE_SERVICE_ACKD_ENABLED` | Ackd | `true` |
| `CORE_SERVICE_JETTYSERVER_ENABLED` | JettyServer | `true` |
| `CORE_SERVICE_KARAFSTARTUPMONITOR_ENABLED` | KarafStartupMonitor | `true` |
| `CORE_SERVICE_SYSLOGD_ENABLED` | Syslogd | `false` |
| `CORE_SERVICE_TELEMETRYD_ENABLED` | Telemetryd | `true` |
| `CORE_SERVICE_TRAPD_ENABLED` | Trapd | `true` |
| `CORE_SERVICE_PERSPECTIVEPOLLER_ENABLED` | PerspectivePoller | `true` |

Config written to `etc/service-configuration.xml`.

## Prometheus JMX Exporter

The JMX exporter is disabled by default. Enable it with `PROM_JMX_EXPORTER_ENABLED=true`.

| Environment Variable | Description | Default |
|:---------------------|:------------|:--------|
| `PROM_JMX_EXPORTER_ENABLED` | Enable the JMX exporter | `false` |
| `PROM_JMX_EXPORTER_PORT` | Port to expose metrics on | `9299` |
| `PROM_JMX_EXPORTER_JAR` | Path to the agent JAR | `/opt/prom-jmx-exporter/jmx_prometheus_javaagent.jar` |
| `PROM_JMX_EXPORTER_CONFIG` | Path to the config YAML | `/opt/prom-jmx-exporter/config.yaml` |
| `PROM_JMX_START_DELAY_SECONDS` | Seconds to wait before collecting metrics | `0` |
| `PROM_JMX_LOWERCASE_OUTPUT_NAME` | Lowercase metric names | `true` |
| `PROM_JMX_LOWERCASE_OUTPUT_LABEL_NAMES` | Lowercase label names | `true` |
| `PROM_JMX_AUTO_EXCLUDE_OBJECT_NAME_ATTRIBUTES` | Auto-exclude non-numeric MBean attributes | `true` |

When the exporter is enabled and `PROM_JMX_EXPORTER_CONFIG` is left at its default, `/opt/prom-jmx-exporter/config.yaml` is generated at startup from a template (a file you mount there is used as is and never overwritten). The generated config exposes `java.lang:*`, `OpenNMS:*`, `org.opennms.*:*`, and `com.zaxxer.hikari:*`.
To customise `includeObjectNames`, `excludeObjectNames`, or `rules`, mount a full YAML file and point `PROM_JMX_EXPORTER_CONFIG` at it.

## Migration from confd

Earlier images rendered these files with confd, which read the same environment variables (confd's `env` backend). Most variable names are unchanged. When upgrading, note:

- Five Trapd variables were renamed. The old names still work as a fallback, but the new name wins when both are set:

  | Old name | New name |
  |:---------|:---------|
  | `OPENNMS_TRAPD_NEWSUSPECTONTRAP` | `OPENNMS_TRAPD_NEW_SUSPECT_ON_TRAP` |
  | `OPENNMS_TRAPD_INCLUDERAWMESSAGE` | `OPENNMS_TRAPD_INCLUDE_RAW_MESSAGE` |
  | `OPENNMS_TRAPD_QUEUESIZE` | `OPENNMS_TRAPD_QUEUE_SIZE` |
  | `OPENNMS_TRAPD_BATCHSIZE` | `OPENNMS_TRAPD_BATCH_SIZE` |
  | `OPENNMS_TRAPD_BATCHINTERVAL` | `OPENNMS_TRAPD_BATCH_INTERVAL` |

- `OPENNMS_NOTIFD_SLACK_CHANNEL` and `OPENNMS_NOTIFD_MATTERMOST_CHANNEL` are no longer supported, because the Slack and Mattermost notification strategies were removed. Use the webhook notification strategy instead (see "Migrating from Slack and Mattermost" in the Webhook Notifications docs).
- Legacy `_confd.*.properties` files left in a mounted `etc/` volume are automatically removed at startup to prevent stale settings.
- A `_confd.<name>.properties` file supplied through an overlay still replaces the generated settings, as it did with confd: the matching `_container.<name>.properties` is removed and a warning is logged. Rename it to `_container.<name>.properties` to silence the warning.
- Numeric and boolean variables (`OPENNMS_TRAPD_*`, `OPENNMS_CASSANDRA_PORT`, `OPENNMS_RRD_STOREBYFOREIGNSOURCE`, all `CORE_SERVICE_*_ENABLED`, and `PROM_JMX_*` when the exporter config is generated) are validated at startup. Booleans accept `true`/`false` in any case (`True` is normalised to `true`). An invalid value (e.g. `"yes"` or `"1"` instead of `"true"`) prints a clear `ERROR:` message and stops the container before OpenNMS starts. The file that uses the invalid value isn't written, but files rendered earlier in the same startup may already have been updated.
