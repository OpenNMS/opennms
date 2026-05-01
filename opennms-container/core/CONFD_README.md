# Configuring Horizon via Environment Variables

Configuration is applied at container startup by processing templates and static config files using environment variables.
Overlay config files (mounted to `/opt/opennms-etc-overlay`) are applied afterwards and take final precedence.

## Karaf SSH

| Environment Variable | Config key | Default |
|:---------------------|:-----------|:--------|
| `KARAF_SSH_PORT` | `sshPort` in `org.apache.karaf.shell.cfg` | `8101` |
| `KARAF_SSH_HOST` | `sshHost` in `org.apache.karaf.shell.cfg` | `0.0.0.0` |

## Timeseries / RRD

| Environment Variable | Config key | Default |
|:---------------------|:-----------|:--------|
| `OPENNMS_TIMESERIES_STRATEGY` | `org.opennms.timeseries.strategy` | `rrd` |
| `OPENNMS_RRD_STORE_BY_FOREIGN_SOURCE` | `org.opennms.rrd.storeByForeignSource` | `true` |
| `OPENNMS_RRD_STRATEGY_CLASS` | `org.opennms.rrd.strategyClass` | `org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy` |
| `OPENNMS_RRD_INTERFACE_JAR` | `org.opennms.rrd.interfaceJar` | `/usr/share/java/jrrd2.jar` |
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

## Slack notifications

| Environment Variable | Config key | Default |
|:---------------------|:-----------|:--------|
| `OPENNMS_NOTIFD_SLACK_WEBHOOKURL` | `org.opennms.netmgt.notifd.slack.webhookURL` | `Webhook URL` |
| `OPENNMS_NOTIFD_SLACK_CHANNEL` | `org.opennms.netmgt.notifd.slack.channel` | `Webhook` |
| `OPENNMS_NOTIFD_SLACK_USERNAME` | `org.opennms.netmgt.notifd.slack.username` | `none` |
| `OPENNMS_NOTIFD_SLACK_ICONEMOJI` | `org.opennms.netmgt.notifd.slack.iconEmoji` | _(empty)_ |
| `OPENNMS_NOTIFD_SLACK_ICONURL` | `org.opennms.netmgt.notifd.slack.iconURL` | _(empty)_ |
| `OPENNMS_NOTIFD_SLACK_USESYSTEMPROXY` | `org.opennms.netmgt.notifd.slack.useSystemProxy` | `true` |

Config written to `etc/opennms.properties.d/_container.slack.properties`.

## Mattermost notifications

| Environment Variable | Config key | Default |
|:---------------------|:-----------|:--------|
| `OPENNMS_NOTIFD_MATTERMOST_WEBHOOKURL` | `org.opennms.netmgt.notifd.mattermost.webhookURL` | `Webhook URL` |
| `OPENNMS_NOTIFD_MATTERMOST_CHANNEL` | `org.opennms.netmgt.notifd.mattermost.channel` | `Webhook` |
| `OPENNMS_NOTIFD_MATTERMOST_USERNAME` | `org.opennms.netmgt.notifd.mattermost.username` | `none` |
| `OPENNMS_NOTIFD_MATTERMOST_ICONEMOJI` | `org.opennms.netmgt.notifd.mattermost.iconEmoji` | _(empty)_ |
| `OPENNMS_NOTIFD_MATTERMOST_ICONURL` | `org.opennms.netmgt.notifd.mattermost.iconURL` | _(empty)_ |
| `OPENNMS_NOTIFD_MATTERMOST_USESYSTEMPROXY` | `org.opennms.netmgt.notifd.mattermost.useSystemProxy` | `true` |

Config written to `etc/opennms.properties.d/_container.mattermost.properties`.

## Prometheus JMX Exporter

The JMX exporter is disabled by default. Enable it with `PROM_JMX_EXPORTER_ENABLED=true`.

| Environment Variable | Description | Default |
|:---------------------|:------------|:--------|
| `PROM_JMX_EXPORTER_ENABLED` | Enable the JMX exporter | `false` |
| `PROM_JMX_EXPORTER_PORT` | Port to expose metrics on | `9299` |
| `PROM_JMX_EXPORTER_JAR` | Path to the agent JAR | `/opt/prom-jmx-exporter/jmx_prometheus_javaagent.jar` |
| `PROM_JMX_EXPORTER_CONFIG` | Path to the config YAML | `/opt/prom-jmx-exporter/config.yaml` |

The default config at `/opt/prom-jmx-exporter/config.yaml` exposes `java.lang:*`, `OpenNMS:*`, `org.opennms.*:*`, and `com.zaxxer.hikari:*`.
Mount a custom YAML file and point `PROM_JMX_EXPORTER_CONFIG` at it to override the full configuration.

## Migration from confd

If you are upgrading from a version that used `horizon-config.yaml` / confd:

- The `horizon-config.yaml` mount is no longer used. Switch to environment variables using the tables above.
- Legacy `_confd.*.properties` files left in a mounted `etc/` volume are automatically removed at startup to prevent stale settings.
