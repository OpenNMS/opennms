About OpenNMS Meridian 2015.1.2
===============================

OpenNMS Meridian is a custom build of OpenNMS Horizon designed for stability and long-term support.

Since Meridian 2015.1.1, there have been a number of minor bug fixes, as well as a backport of
numerous configuration files from Meridian 2016.  For a complete list, see the
release notes [here](http://meridian.opennms.com/releasenotes/2015/latest/).

The most notable changes over 2015.1.1 are:

* New data collection, event, and graph configuration files.
* A new tool for (re)compiling Jasper reports (`$OPENNMS_HOME/bin/report-compiler`) has been
  added, and report compilation code has been updated.
* Enlinkd bug fixes.

Using the Updated Configuration Files
=====================================

By default, the new configuration files are written, but not included, to avoid creating any
conflicting `.rpmnew` or `.rpmorig` files on upgrade.

Enabling Additional Event Configs
---------------------------------

The following event configuration files are new in Meridian 2015.1.2:

* `$OPENNMS_HOME/etc/events/A10.AX.events.xml`
* `$OPENNMS_HOME/etc/events/Avocent-DSView.events.xml`
* `$OPENNMS_HOME/etc/events/Ceragon-FA1500.events.xml`
* `$OPENNMS_HOME/etc/events/CitrixNetScaler.events.xml`
* `$OPENNMS_HOME/etc/events/Dell-F10-bgb4-v2.events.xml`
* `$OPENNMS_HOME/etc/events/Dell-F10-chassis.events.xml`
* `$OPENNMS_HOME/etc/events/Dell-F10-copy-config.events.xml`
* `$OPENNMS_HOME/etc/events/Dell-F10-mstp.events.xml`
* `$OPENNMS_HOME/etc/events/Dell-F10-system-component.events.xml`
* `$OPENNMS_HOME/etc/events/DellEquallogic.events.xml`
* `$OPENNMS_HOME/etc/events/Evertz.7780ASI-IP2.events.xml`
* `$OPENNMS_HOME/etc/events/Evertz.7880IP-ASI-IP-FR.events.xml`
* `$OPENNMS_HOME/etc/events/Evertz.7880IP-ASI-IP.events.xml`
* `$OPENNMS_HOME/etc/events/Evertz.7881DEC-MP2-HD.events.xml`
* `$OPENNMS_HOME/etc/events/Infoblox.events.xml`
* `$OPENNMS_HOME/etc/events/Juniper.screen.events.xml`
* `$OPENNMS_HOME/etc/events/MikrotikRouterOS.events.xml`
* `$OPENNMS_HOME/etc/events/Postfix.syslog.events.xml`
* `$OPENNMS_HOME/etc/events/Procmail.syslog.events.xml`
* `$OPENNMS_HOME/etc/events/Raytheon.events.xml`
* `$OPENNMS_HOME/etc/events/Siemens-HiPath3000-HG1500.events.xml`
* `$OPENNMS_HOME/etc/events/Siemens-HiPath3000.events.xml`
* `$OPENNMS_HOME/etc/events/Siemens-HiPath4000.events.xml`
* `$OPENNMS_HOME/etc/events/Siemens-HiPath8000-OpenScapeVoice.events.xml`
* `$OPENNMS_HOME/etc/events/Veeam_Backup-Replication.events.xml`

Additionally, these files contain support for new models of hardware from
already-supported vendors beyond what was already provided in previous
Meridian 2015 releases:

* `$OPENNMS_HOME/etc/events/Meridian-2016.Liebert.events.xml`
* `$OPENNMS_HOME/etc/events/Meridian-2016.NetApp.events.xml`
* `$OPENNMS_HOME/etc/events/Meridian-2016.OpenSSH.syslog.events.xml`

To add any of these configuration files to your OpenNMS Meridian instance,
just put them in `$OPENNMS_HOME/etc/eventconf.xml` somewhere between
the `opennms.events.xml` and `topology-status.events.xml` entries.

For example:

```xml
<events xmlns="http://xmlns.opennms.org/xsd/eventconf">
   ...
   <event-file>events/Meridian-2016.OpenSSH.syslog.events.xml</event-file>

   <event-file>events/topology-status.events.xml</event-file>
   <event-file>events/ncs-component.events.xml</event-file>
   <event-file>events/asset-management.events.xml</event-file>
   <event-file>events/Standard.events.xml</event-file>
   <event-file>events/default.events.xml</event-file>
</events>
```

Enabling Additional Data Collection Configs
-------------------------------------------

The following data collection configuration files are new in Meridian 2015.1.2:

* `$OPENNMS_HOME/etc/datacollection/sonicwall.xml`

Additionally, these files contain support for new models of hardware from
already-supported vendors beyond what was already provided in previous
Meridian 2015 releases:

* `$OPENNMS_HOME/etc/datacollection/meridian-2016-cisco.xml`
* `$OPENNMS_HOME/etc/datacollection/meridian-2016-juniper.xml`
* `$OPENNMS_HOME/etc/datacollection/meridian-2016-mib2.xml`

To add any of these configuration files to your OpenNMS Meridian instance,
just put them in `$OPENNMS_HOME/etc/datacollection-config.xml` before
the `</snmp-collection>` tag near the end.

For example:

```xml
<datacollection-config xmlns="http://xmlns.opennms.org/xsd/config/datacollection" rrdRepository="/opt/opennms/rrd/snmp/">
   <snmp-collection name="default" snmpStorageFlag="select">
      ...
      <include-collection dataCollectionGroup="meridian-2016-cisco"/>
   </snmp-collection>
</datacollection-config>
```

Enabling Additional Graph Configs
---------------------------------

You do not need to perform any action to enable the new `snmp-graph.properties.d`
files included in Meridian 2015.1.2, they will be automatically enabled for
devices that can use them.

