# Configuring Horizon via confd
(instructions for testing/developing confd templates are given at the end of this document)
## Mounting
When starting the Horizon container, mount a yaml file to the following path `/opt/opennms/horizon-config.yaml`.

Any configuration provided to confd will overwrite configuration specified as environment variables. Direct overlay of
specific configuration files will overwrite the corresponding config provided by confd.

## Contents
The following describes the keys that can be specified in `horizon-config.yaml` to configure Horizon via confd.

### Newts

```
---
opennms:
  cassandra: 
    hostname: localhost 
    keyspace: newts
    port: 9042
    username: cassandra
    password: cassandra
```

Config specified will be written to `etc/opennms.properties.d/_confd.newts.properties`. 
