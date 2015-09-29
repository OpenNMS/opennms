What's New in OpenNMS 17
========================

System Requirements
-------------------
* **Java 8**: OpenNMS 17 requires Java 8 as the runtime environment. To run OpenNMS 17, we recommend the most recent version of Oracle JDK 8 for your platform.
* **PostgreSQL 9.1 or higher**: OpenNMS 17 now requires PostgreSQL 9.1 or higher. All older versions of PostgreSQL are past their end-of-life support date.

Important Upgrade Notes
-----------------------

* **Clean Up Events and Alarms**: Please clean up your event and alarm lists as much as possible before performing an upgrade to OpenNMS 17. The upgrade process contains a large number of database schema migrations which will cause rewrites of all of the data in some tables. If you prune unused data from these tables beforehand, it will make the upgrade process much quicker.
* **Remote Poller API Change**: Due to internal API changes, the Remote Poller API has changed in OpenNMS 17. If you upgrade to OpenNMS 17, you will also need to upgrade all Remote Pollers attached to the system to version 17 as well.

API Changes
-----------
* Monitoring locations for the Remote Poller are now viewed and configured in the UI under `/locations/index.jsp`. Monitoring locations were previously configured in the `etc/monitoring-locations.xml` file but are now stored in `monitoringlocations` database table.
* All data previously in the `location_monitors` table is now in the `monitoringsystems` table.
* The `ifIndex` column has been removed from the `ipInterface` table. To fetch the ifIndex, you need to join to the `snmpInterface` table by `nodeId`.
* The `nodeId` and `ipAddr` columns have been removed from the `ifServices` table. To fetch these columns, you need to join to the `ipInterface` table.
* The `nodeId`, `ipAddr` and `serviceId` columns have been removed from the `outages` table. To fetch these columns, you need to join to the `ifServices` table.

New Features
------------
* **FreeIPA Kerberos Authentication**: We've added a sample configuration for FreeIPA Kerberos SSO to our `jetty-webapps/opennms/WEB-INF/spring-security.d` directory.
* **Alarm Heatmap**: A heatmap visualization has been added that lets you quickly visualize alarm status and outages within a category or on a node.
* **JMX Configuration UI and CLI**: The JMX Configuration web UI and CLI have been rewritten to make it easier to generate complex JMX data collection configurations.
* **Grafana Panel**: An optional panel has been added to the UI to allow you to integrate links to Grafana graphs in the UI.
* **JMS Alarm Northbounder**: The implementation of a JMS northbounder for sending OpenNMS Alarms to external JMS listeners has been completed. Thanks to David Schlenk for this contribution!
* **Easier Remote Poller configuration**: Monitoring locations can now be associated with multiple polling and collection packages. This can make some Remote Poller scenarios easier to configure.

Events
------
* A10 AX Load Balancer
* Infoblox
* Raytheon NXU-2A
* Avocent DSView
* Evertz 7800FR Multiframe
  * 7880-IP-ASI-IP
  * 7881-DEC-MP2
  * 7780-ASI-IP2
* Dell Force 10
* ~50 new NetApp Events


Data Collection
---------------
* Fortinet Fortigate
* Sonicwall

Retired Features
----------------
* **Linkd**: Linkd was the original implementation of the link scanning daemon for OpenNMS. It has been superseded by Enhanced Linkd (Enlinkd) in OpenNMS 14 and higher. Linkd has been removed as of OpenNMS 17.
* **SVG Maps**: The SVG map feature relied on Linkd's code for drawing links between items on the map so it was also removed in OpenNMS 17.
* **Xmlrpcd**: Xmlrpcd was a daemon that relayed inventory and polling events to an external system over the XML-RPC protocol. Because you can accomplish almost all of its use cases by using the provisioning REST service, it has been removed.

CLI Changes
-----------
* `opennms-assemblies/jmx-config-generator-onejar` does not exist anymore. The onejar project is now located in `features/jmx-config-generator`.

Internal Updates
----------------
* The resource API for data storage has undergone heavy refactoring.
* The REST interface was refactored to be based on Apache CXF 3.1.1.
* The Dashboard has been rewritten using the Vaadin toolkit to modernize its look-and-feel.

[GNU Affero General Public License 3.0]: http://www.gnu.org/licenses/agpl-3.0.html
