# Configuring Minion via confd
## Mounting
When starting the Minion container, mount a yaml file to the following path `/opt/minion/minion-config.yaml`.

Any configuration provided to confd will overwrite configuration specified as environment variables. Direct overlay of
specific configuration files will overwrite the corresponding config provided by confd.

## Contents
The following describes the keys that can be specified in `minion-config.yaml` to configure the Minion via confd.
### Minion Controller Config
```
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
```
---
org.opennms.instance.id: "<instance id>"
```
Config specified will be written to `etc/instance-id.properties`.

### AWS SQS
```
---
aws:
    aws_region: "us-east-1"
    aws_access_key_id: "XXXXXXXXXXX"
    aws_secret_access_key: "XXXXXXXXXXX"

ipc:
    sqs:
        sink.DelaySeconds: 0
        sink.MaximumMessageSize: 262144
        sink.FifoQueue: true
        rpc.DelaySeconds: 0
        rpc.MaximumMessagesize: 262144
        # Any other keys necessary can be specified here
```
Config specified will be written to `etc/org.opennms.core.ipc.aws.sqs.cfg`.

### Kafka RPC
```
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
```
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
```
--- 
ipc:
    sink:
        offheap:
            offHeapSize: "1GB"
            entriesAllowedOnHeap: 100000
            offHeapFilePath: ""
```
Config specified will be written to `etc/org.opennms.core.ipc.sink.offheap.cfg`.

### Flows
To configure flows on a single port, the following key can be provided.
```
--- 
telemetry:
    flows:
        single-port: <port num>
```
Config specified will be written to `etc/org.opennms.features.telemtry.listeners-udp-single-port.cfg`.

### Syslog
```
--- 
netmgt:
    syslog:
        syslog.listen.interface: "0.0.0.0"
        syslog.listen.port: 1514
        # Any other keys necessary can be specified here
```
Config specified will be written to `etc/org.opennms.netmgt.syslog.cfg`.

### Traps
```
--- 
netmgt:
    traps:
        trapd.listen.interface: "127.0.0.1"
        trapd.listen.port: 1162
        # Any other keys necessary can be specified here
```
Config specified will be written to `etc/org.opennms.netmgt.trapd.cfg`.

### System Properties
```
--- 
system:
    properties:
        jaeger-agent-host: "<host>"
        org.opennms.snmp.snmp4j.allowSNMPv2InV1: true
        # Any other keys necessary can be specified here
```
Config specified will be written to `etc/confd.system.properties` which gets automatically appended to `etc/system.properties`. Additionally, provided the
`jaeger-agent-host` key is specified, `etc/featuresBoot.d/jaeger.boot` will also be updated.