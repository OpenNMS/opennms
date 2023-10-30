# Configuring Sentinel via confd
(instructions for testing/developing confd templates are given at the end of this document)
## Mounting
When starting the Sentinel container, mount a yaml file to the following path `/opt/sentinel/sentinel-config.yaml`.

Any configuration provided to confd will overwrite configuration specified as environment variables. Direct overlay of
specific configuration files will overwrite the corresponding config provided by confd.

## Contents
The following describes the keys that can be specified in `sentinel-config.yaml` to configure the Sentinel via confd.

### Sentinel Controller Config
```yaml
--- 
broker-url: "<broker url>"
id: "<id>"
location: "<location>"
```
Config specified will be written to `etc/org.opennms.sentinel.controller.cfg`.

#### User/Password
Supplying the broker username/password via yaml file for configuration via confd is not supported.

### Sentinel Elasticsearch Config
```yaml
--- 
elasticsearch:
  url: "http://elastic-ip:9200"
  index-strategy: "hourly"
  replicas: 0
  conn-timeout: 30000
  read-timeout: 60000
```
Config specified will be written to `etc/org.opennms.features.flows.persistence.elastic.cfg`.

### Sentinel Datasource Config
```yaml
---
datasource:
  url: "jdbc:postgresql://localhost:5432/opennms"
  username: "postgres"
  password: "postgres"
  database-name: "opennms"
```
Config specified will be written to `etc/org.opennms.netmgt.distributed.datasource.cfg`.

### Sentinel Kafka Config
```yaml
---
ipc:
  kafka:
    bootstrap.servers: "my-kafka-ip-1:9092,my-kafka-ip-2:9092"
    group.id: "OpenNMS"
```
Config specified will be written to `etc/org.opennms.core.ipc.sink.kafka.cfg` and `etc/org.opennms.core.ipc.sink.kafka.consumer.cfg`.

### Telemetry Flow Adapters
Individual flow adapters can be configured. See the example below for how to specify parameters and parsers. Any number
of uniquely named listeners can be defined.
```yaml
---
telemetry:
  flows:
    adapters:
      NetFlow-5:
        class-name: "org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.Netflow5Adapter"
        parameters: 
          some-key: "some-value"
```
Config specified will be written to `deploy/confd-flows-feature.xml`.

### Instance Id
```yaml
---
org.opennms.instance.id: "<instance id>"
```
Config specified will be written to `etc/custom.system.properties`.