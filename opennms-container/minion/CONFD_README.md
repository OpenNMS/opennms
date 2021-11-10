# Configuring Minion via confd
(instructions for testing/developing confd templates are given at the end of this document)
## Mounting
When starting the Minion container, mount a yaml file to the following path `/opt/minion/minion-config.yaml`.

Any configuration provided to confd will overwrite configuration specified as environment variables. Direct overlay of
specific configuration files will overwrite the corresponding config provided by confd.

## Contents
The following describes the keys that can be specified in `minion-config.yaml` to configure the Minion via confd.
### Minion Controller Config
```yaml
--- 
broker-url: "<broker url>"
http-url: "<http url>"
id: "<id>"
location: "<location>"
```
Config specified will be written to `etc/org.opennms.minion.controller.cfg`.

#### User/Password
Supplying the http or broker username/password via yaml file for configuration via confd is not supported.

### Instance Id
```yaml
---
org.opennms.instance.id: "<instance id>"
```
Config specified will be written to `etc/instance-id.properties`.

### Kafka RPC
```yaml
--- 
ipc:
    rpc:
        kafka:
            boostrap.servers: "127.0.0.1:9092"
            acks: 1
            max.request.size: 5000000
            # Any other keys necessary can be specified here
```
Config specified will be written to `etc/org.opennms.core.ipc.rpc.kafka.cfg`. Additionally, provided the
`bootstrap.servers` key is specified, `etc/featuresBoot.d/kafka-rpc.boot` will also be updated.

### Kafka SINK
```yaml
--- 
ipc:
    sink:
        kafka:
            boostrap.servers: "127.0.0.1:9092"
            compression.type: "gzip"
            request.timeout.ms: 30000
            max.partition.fetch.bytes: 5000000
            auto.offset.reset: "latest"
            max.request.size: 5000000
            # Any other keys necessary can be specified here
```
Config specified will be written to `etc/org.opennms.core.ipc.sink.kafka.cfg`. Additionally, provided the
`bootstrap.servers` key is specified, `etc/featuresBoot.d/kafka-sink.boot` will also be updated.

### Sink Off Heap
```yaml
--- 
ipc:
    sink:
        offheap:
            offHeapSize: "1GB"
            entriesAllowedOnHeap: 100000
            offHeapFilePath: ""
```
Config specified will be written to `etc/org.opennms.core.ipc.sink.offheap.cfg`.

### Single Port Flows
To configure flows on a single port, set the following `enabled` key to `true`. Optionally parameters can be provided
that will be included in the generated config.
```yaml
--- 
telemetry:
    flows:
        single-port-listener:
            # Set to true to add single port listener config, omit or set to false to disable
            enabled: true
            # Parameters can be optionally provided
            parameters:
                # This translates to parameters.port=50000 in the generated config
                port: 50000
```
Config specified will be written to `etc/org.opennms.features.telemtry.listeners-udp-single-port-flows.cfg`.

### Telemetry Flow Listeners
Individual flow listeners can be configured. See the example below for how to specify parameters and parsers. Any number
of uniquely named listeners can be defined.
```yaml
--- 
telemetry:
    flows:
        listeners:
            NXOS-Listener:
                class-name: "org.opennms.netmgt.telemetry.listeners.UdpListener"
                parameters:
                    # List all the parameters you wish to specify here
                    port: 50002
                parsers:
                    # List all the parsers you wish to specify here
                    NXOS:
                        class-name: "org.opennms.netmgt.telemetry.protocols.common.parser.ForwardParser"
                        # Parsers can also have parameters specified
                        #parameters:
```
Config specified will be written to `etc/org.opennms.features.telemtry.listeners-<Listener-Name>.cfg`.

### Syslog
```yaml
--- 
netmgt:
    syslog:
        syslog.listen.interface: "0.0.0.0"
        syslog.listen.port: 1514
        # Any other keys necessary can be specified here
```
Config specified will be written to `etc/org.opennms.netmgt.syslog.cfg`.

### Traps
```yaml
--- 
netmgt:
    traps:
        trapd.listen.interface: "0.0.0.0"
        trapd.listen.port: 1162
        # Any other keys necessary can be specified here
```
Config specified will be written to `etc/org.opennms.netmgt.trapd.cfg`.

### System Properties
```yaml
--- 
system:
    properties:
        jaeger-agent-host: "<host>"
        org.opennms.snmp.snmp4j.allowSNMPv2InV1: true
        # Any other keys necessary can be specified here
```
Config specified will be written to `etc/confd.system.properties` which gets automatically appended to `etc/system.properties`. Additionally, provided the
`jaeger-agent-host` key is specified, `etc/featuresBoot.d/jaeger.boot` will also be updated.

### Karaf Properties
```yaml
---
karaf:
    shell:
        ssh:
            host: "0.0.0.0"
            port: 8201
    management:
        rmi:
            registry:
                host: "127.0.0.1"
                port: 1299
            server:
                host: "127.0.0.1"
                port: 45444
```
Config specified will be written to:
- `etc/org.apache.karaf.shell.cfg` for content under `shell`.
- `etc/org.apache.karaf.management.cfg` for content under `management`.

### Jetty Properties
```yaml
---
jetty:
    web:
        host: "0.0.0.0"
        port: 8181
```
Config specified will be written to `etc/org.ops4j.pax.web.cfg`

### Secure Credentials Vault Provider
```yaml
--- 
scv:
    provider: "dominion"
```
Can be used to override the default SCV provider from the JCEKS implementation (which uses the file system) to a gRPC
based implementation which requests credentials from Dominion. If not specified the default JCEKS will be used.

### Java Options
```yaml
---
process-env:
    java-opts:
        - -Xmx4096m
        - -Xdebug
        - -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=7896
```

Can be used to specify an arbitrary list of Java options. Config specified is written to file `/opt/minion/etc/minion-process.env` that contains `key=value` pairs that are set in the environment of the Minion process..

## Prometheus JMX Exporter

To provide an out of band management of the JVM with the Minion process, the Prometheus JMX exporter is shipped with this container image.
The default configuration is set to the following values and can be set in the `minion-config.yaml` file:

```yaml
---
java:
  agent:
    prom-jmx-exporter:
      jmxUrl: "service:jmx:rmi:///jndi/rmi://127.0.0.1:1299/karaf-minion"
      username: "admin"
      password: "admin"
      lowerCaseOutputName: "true"
      lowercaseOutputLabelNames: "true"
      whitelistObjectNames:
      - "org.opennms.core.ipc.sink.producer:*"
      - "org.opennms.netmgt.dnsresolver.netty:*"
      - "org.opennms.netmgt.telemetry:*"
```

The Minion container images comes with the Prometheus JMX exporter and can be enabled with:

```yaml
---
process-env:
  java-opts:
    - -javaagent:/opt/prom-jmx-exporter/jmx_prometheus_javaagent.jar=9299:/opt/prom-jmx-exporter/config.yaml
```

## Test/Develop confd templates
`confd` template changes can locally be tested by running a Minion container and mapping the corresponding files into the container. The following procedure might be useful:

1. A Minion Docker image is required. It can be downloaded from a build in CircleCI. It is an artifact of the `tarball-assembly` job. 
1. Load the image into Docker: `docker load minion.oci`
1. Create a `docker-compose.yaml` file in the parent folder of the checked out `opennms` repo. An example compose file is given below
1. Start the image: `docker-compose up -d`
1. Open a shell in the container using `docker exec -ti minion bash` or look at the logs `docker logs minion`
1. If the result is not yet satisfactory then remove the container by `docker rm -f minion`, edit the files in your IDE, and start the image again


```yaml
version: '3'
services:
  minion:
    image: minion
    container_name: minion
    volumes:
      - ${PWD}/minion-config.yaml:/opt/minion/minion-config.yaml
      - ${PWD}/opennms/opennms-container/minion/container-fs/confd/conf.d/org.opennms.minion.process-env.toml:/opt/minion/confd/conf.d/org.opennms.minion.process-env.toml
      - ${PWD}/opennms/opennms-container/minion/container-fs/confd/templates/org.opennms.minion.process-env.tmpl:/opt/minion/confd/templates/org.opennms.minion.process-env.tmpl
      - ${PWD}/opennms/opennms-container/minion/container-fs/entrypoint.sh:/entrypoint.sh
```
