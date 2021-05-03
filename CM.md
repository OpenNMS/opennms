# CM Endpoint Design

## About this document

This document describes the different endpoints that could be exposed from the Configuration Management (CM) APIs,
how they could be structured, and some considerations to make when designing these.

Example payloads are used to give some idea of what the contents of the configuration could look like but must not be followed strictly.

## Endpoints

> What should be call these? Endpoints, config ids, components, ...? We need consistent terminology when referring to these.

Endpoints include:
1. [/system-properties](#/system-properties)
1. [/vacuumd](#/vacuumd)
1. [/provisiond](#/provisiond)
1. [/eventd](#/eventd)
1. [/events](#/events)
1. [/graphs](#/graphs)
1. [/dnsresolver](#/dnsresolver)
1. [/topology-icons](#/topology-icons)
1. [/poller](#/poller)

## /system-properties

JVM Wide -Dstyle system properties

### Example Payload

```
{
  "sysprop1": "value1",
  "sysprop2": "value2"
}
```

### Notes 

We should include all known system properties and provide some level of validation for their values i.e. int vs str, min/max, one of, regex, etc...

We need a simple way to describe the schema and add/alter/remove sys props.

With this, it should now be possible to programatically enumerate all of the known system properties.
For testing purposes it should also be possible to iterate through some known values.

### Schema References

Incomplete, but some examples:
* https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/features/newts/src/main/resources/META-INF/opennms/applicationContext-timeseries-newts.xml#L18
* https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/core/ipc/sink/common/src/main/java/org/opennms/core/ipc/sink/aggregation/Aggregator.java#L66
* https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/opennms-base-assembly/src/main/filtered/etc/opennms.properties#L45

Code should be audited to find sys props and define them in the scheme.

## /vacuumd

the vacuumd daemon

### Example Payload

```
{
 "period": 83434,
 "statements": [
    {
       "sql": "DELETE FROM snmpInterface WHERE snmpInterface.snmpCollect = 'D';",
       "description": "this deletes all the snmpInterfaces that have been marked as deleted"
    }
 ]
}
```

### Notes

Security: This endpoint give the ability to schedule(&run) arbitrary SQL statements and should be restricted accordingly.

### Schema reference

* [vacuumd-configuration.xsd](https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/opennms-config-model/src/main/resources/xsds/vacuumd-configuration.xsd#L2)

## /eventd

eventd daemon

### Example Payload

```
{
  "TCPAddress": "127.0.0.1",
  "TCPPort": 5817
  "receivers": 5
}
```

### Notes

Security: Controls TCP ports and listen addresses.

Debt: Is the `getNextEventID` SQL still used? Can we use this opportunity to get rid of it if not.

### Schema reference

* [eventd-configuration.xsd](https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/opennms-config-model/src/main/resources/xsds/eventd-configuration.xsd)

## /events

Event definitions

### Example Payload

```
{
  "global": {
    "security": {
       "doNotOverrides": ["logmsg", "operaction"]
    }
  },
  "files_enabled": [
    "events/opennms.snmp.trap.translator.events.xml",
    "events/opennms.ackd.events.xml",
  ],
  "files": {
    "events/opennms.snmp.trap.translator.events.xml": {
      "events": [
        {
          "uei": "uei.opennms.org/generic/traps/SNMP_Cold_Start",
          "event-label": "OpenNMS-defined trap event: SNMP_Cold_Start"
        }
      ]
    }
  }
}
```

### Notes

There are a lot of these, so performance is important for this endpoint.

### Schema reference

* [eventconf.xsd](https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/opennms-config-model/src/main/resources/xsds/eventconf.xsd#L18)

## /graphs

RRD-based graph defininitions defined in `snmp-graph.properties` and in `snmp-graph.properties.d`.

### Example Payload

```
{
  "command.prefix": "/usr/bin/rrdtool graph - --imgformat PNG --font DEFAULT:7 --font TITLE:10 --start {startTime} --end {endTime}"
  "files_enabled": [
    "mib2-graph.properties"
  ],
  "files": {
    "mib2-graph.properties": {
      "reports": [
         "report.mib2.bits.name": "Bits In/Out",
         "report.mib2.bits.columns": "ifInOctets,ifOutOctets"
         "report.mib2.bits.type": "interfaceSnmp",
         "report.mib2.bits.command": "--title="Bits In/Out" \"
      ]
    }
  }
}
```

### Notes

Debt: This could be a opportunity to remodel these and move away from the flattend properties form.

Performance: There are a lot of these.

Security: Remote command execution

### Schema reference

Not sure where schema is defined, see examples:
* [mib2-graph.properties](https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/opennms-base-assembly/src/main/filtered/etc/snmp-graph.properties.d/mib2-graph.properties#L53)

## /dnsresolver

High throughput forward/reverse DNS resolution engine used by Flows.

See [NettyDnsResolver](https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/features/dnsresolver/netty/src/main/java/org/opennms/netmgt/dnsresolver/netty/NettyDnsResolver.java)

### Example Payload

```
{
  "num-contexts": 2,
  "nameservers": "4.2.2.2,1.1.1.1",
  "bulkhead-max-wait-duration": "5s"
}
```

### Notes

Standard OSGi configuration referenced from a Blueprint.

### Schema reference

See [org.opennms.features.dnsresolver.netty](https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/features/dnsresolver/netty/src/main/resources/OSGI-INF/blueprint/blueprint.xml#L11)

## /topology-icons

Icon definitions for the Topology UI

Defines the icon names and mappings from sysoid to icons

### Example Payload

```
{
  "icons": [
    "router",
    "switch",
    "server"
  ],
  "files": {
    "linkd": {
      "linkd.group": "cloud",
      "linkd.system": "generic",
      "linkd.system.snmp.1.3.6.1.4.1.5813.1.13": "opennms_server"
    }
  }
}
```

### Notes

The actual icons are provided separately via .svg files and can be out of scope for this API.

### Schema reference

Examples:
* [org.opennms.features.topology.app.icons.list](https://github.com/OpenNMS/opennms/blob/develop/opennms-base-assembly/src/main/filtered/etc/org.opennms.features.topology.app.icons.list)
* [org.opennms.features.topology.app.icons.linkd.cfg](https://github.com/OpenNMS/opennms/blob/develop/opennms-base-assembly/src/main/filtered/etc/org.opennms.features.topology.app.icons.linkd.cfg)

## /poller

Configuration for pollerd

### Example Payload

```
{
 "threads": 30,
 "nextOutageId": "SELECT nextval('outageNxtId')",
 "node-outage": {
  "status": "on",
  "pollAllIfNoCriticalServiceDefined": "true",
 },
 "packages": [
   {
    "name": "cassandra-via-jmx",
    "filter": "IPADDR != '0.0.0.0'",
    "rrd": {
      "step": 300,
      "rras": [
        "RRA:AVERAGE:0.5:1:2016",
        "RRA:AVERAGE:0.5:12:1488"
      ]
    },
    "services": [
      {
        "name":"JMX-Cassandra",
        "interval":300000,
        "parameters": {
          "port": "7199",
          "retry": "2",
          "timeout": "3000",
          "protocol": "/jmxrmi"
        }
      }
    ]
   }
 ]
}
```

### Notes

Security: Remote code execution possible by configuring the SystemExecuteMonitor?

Validation lines in PollerConfigFactory, for example:
```
Caused by: java.lang.IllegalStateException: ds-name 'selenium-${requisition:web-label}' in service 'Selenium:meta' (poller package 'example1') is greater than 19 characters
        at org.opennms.netmgt.config.PollerConfigFactory.validate(PollerConfigFactory.java:149) ~[opennms-config-28.0.0-SNAPSHOT.jar:?]
        at org.opennms.netmgt.config.PollerConfigFactory.init(PollerConfigFactory.java:137) ~[opennms-config-28.0.0-SNAPSHOT.jar:?]
        at jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[?:?]
        at jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[?:?]
        at jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:?]
```

### Schema reference

* [poller-configuration.xsd](https://github.com/OpenNMS/opennms/blob/opennms-27.1.1-1/opennms-config-jaxb/src/main/resources/xsds/poller-configuration.xsd)

# Misc. thoughts

* Debt: We can move away from using XSDs to define schemas were we see fit, if we find a better alternative for describing these
* Performance: Some of these objects will be large. When updating the database, we should be able to make small edits without having to re-push the whole object (i.e. we should be able to update a single line of a grpah definition without having to update all graph definitions)
* Requirement: We must be able to retrieve the object schema for each endpoint
* Requirement: Let's be prepared to change the name of endpoints between releases or create aliases. Set expectations and versioning for the API.
