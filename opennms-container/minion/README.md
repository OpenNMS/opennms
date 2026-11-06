# Minion Container Configuration

## Telemetry Listener Environment Variables

The following telemetry listener configs are **not enabled by default**. Set the corresponding environment variable to `true` to activate the listener on startup. The listener config is removed if the variable is absent or set to `false`, so toggling it takes effect on the next container restart.

| Environment Variable     | Listener          | Default port | Config file written to `etc/`                                        |
|--------------------------|-------------------|--------------|----------------------------------------------------------------------|
| `JTI_LISTENER_ENABLED`   | JTI (Juniper)     | 50001        | `org.opennms.features.telemetry.listeners-udp-50001-jti.cfg`         |
| `NXOS_LISTENER_ENABLED`  | NX-OS (Cisco)     | 50002        | `org.opennms.features.telemetry.listeners-udp-50002-nxos.cfg`        |
| `FLOWS_LISTENER_ENABLED` | Single-port flows | 50000         | `org.opennms.features.telemetry.listeners-single-port-flows.cfg`     |

The listener port can be overridden independently via `JTI_PORT`, `NXOS_PORT`, and `FLOWS_PORT`.

If you place a file with the same name in the etc overlay (`/opt/minion-etc-overlay`), that file always wins regardless of the environment variable — the overlay is applied after the template is copied.

## Etc Overlay

Mount a directory to `/opt/minion-etc-overlay` to drop config files directly into `$MINION_HOME/etc` at startup. Files in the overlay always take precedence over any environment variable-driven config.

```yaml
services:
  minion:
    volumes:
      - ./my-etc-overlay:/opt/minion-etc-overlay:ro
```

## Prometheus JMX Exporter

The Prometheus JMX exporter is shipped with the image and can be enabled via environment variable:

| Environment Variable       | Default                                              |
|----------------------------|------------------------------------------------------|
| `PROM_JMX_EXPORTER_ENABLED`| `false`                                              |
| `PROM_JMX_EXPORTER_PORT`   | `9299`                                               |
| `PROM_JMX_EXPORTER_CONFIG` | `/opt/prom-jmx-exporter/config.yaml`                 |
| `PROM_JMX_EXPORTER_JAR`    | `/opt/prom-jmx-exporter/jmx_prometheus_javaagent.jar`|

The default config at `/opt/prom-jmx-exporter/config.yaml` scrapes Minion-relevant JMX beans. To use a custom config, mount a replacement file and point `PROM_JMX_EXPORTER_CONFIG` at it.

## Local Development

To test entrypoint or config changes locally:

1. Obtain a Minion Docker image (e.g. download from a CircleCI `tarball-assembly` artifact and `docker load minion.oci`).
2. Create a `docker-compose.yml` alongside your checked-out repo:

```yaml
services:
  minion:
    image: minion
    container_name: minion
    volumes:
      - ./opennms/opennms-container/minion/container-fs/entrypoint.sh:/entrypoint.sh
      - ./my-etc-overlay:/opt/minion-etc-overlay:ro
    environment:
      MINION_ID: "my-minion"
      MINION_LOCATION: "Default"
      OPENNMS_HTTP_USER: "admin"
      OPENNMS_HTTP_PASS: "admin"
```

3. `docker compose up -d`
4. Inspect logs: `docker logs minion`
5. Shell in: `docker exec -ti minion bash`
6. Iterate: `docker rm -f minion`, edit, repeat.
