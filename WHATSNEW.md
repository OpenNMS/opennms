What's New in OpenNMS 14
========================

Since OpenNMS 1.12, a large number of changes have occurred, including architectural
updates, major topology UI updates, a completely rewritten Linkd (called Enhanced Linkd),
and much more.

Licensing
---------

As of OpenNMS 14, OpenNMS is now released under the [GNU Affero General Public License 3.0].
It is identical to the GPLv3 OpenNMS was previously licensed under, with the additional
caveat that users who interact with OpenNMS software over a network are entitled to receive
the source.

Java 7 and 8
------------

OpenNMS 14 drops support for Java 6, which has been EOL'd for some time now.  OpenNMS 14
requires Java 7 or higher to run.

Note that *building* under Java 8 still has some known issues.  It is strongly recommended
that you build and run under Java 7 until Java 8 has had enough time to be well-tested.

Logging System Upgrade
----------------------

Logging has been *completely* revamped in OpenNMS 14.  The locations of various log output
are much more intuitive, and turning on DEBUG/TRACE for specific subsystems is simpler.  The
old `log4j.properties` has been superseded by the `log4j2.xml` file.

Core Updates
------------

As always, many updates and cleanups have been made to the OpenNMS core, through refactoring,
addition of unit tests, and other code modernization.

* The embedded Karaf OSGi container has been updated to 2.4.0.
* Many core OpenNMS modules are now capable of being loaded inside an OSGi container.
* A large number of legacy Castor-based classes have been updated to use JAXB instead.
* SNMP4J has been updated to version 2, which provides improved handling of SNMPv3, in
  addition to a number of other bug fixes and improvements to SNMP handling.
* JRobin graphing has had a number of graphical updates as well as bugfixes.
* The syslog northbounder has been enhanced to be able to do more with alarm data:
  http://issues.opennms.org/browse/NMS-5798
* New or updated monitors:
  * DskTableMonitor: UCD-SNMP-MIB-based monitor for disks with errors
  * HttpPostMonitor: POST data to an HTTP server and evaluate the response headers
  * JolokiaBeanMonitor: monitors a mbean method via the Jolokia agent
  * LaTableMonitor: UCD-SNMP-MIB-based monitor for load average errors
  * LogMatchTableMonitor: UCD-SNMP-MIB-based monitor for log matches
  * PrTableMonitor: UCD-SNMP-MIB-based monitor for processes
  * TcpMonitor: can now match banners using a ~regex
  * WebMonitor: accepts an optional `queryString` parameter for the request

Utility Updates
---------------

* You may now pass `--ifindex` to `send-event.pl` when sending events.
* `provision.pl` can now optionally use the `rescanExisting` flag when performing an
  import.  (See below for details.)
* `runjava` now prefers newer Java versions in `/Library/Java/JavaVirtualMachines` over
  `/Library/Java/Home` on Mac OS X.

Events
------

New or updated trap definitions have been added for the following classes of devices:

* Backup Exec
* Broadcom
* Dell
* D-Link
* Foundry
* Intel (LAN adapters)
* Konica
* Trend Micro

Data Collection
---------------

New or updated collection definitions have been added for the following classes of
devices:

* Foundry
* Konica

Link Discovery
--------------

A much more efficient version of `Linkd`, called `Enhanced Linkd` (`Enlinkd`) has been
created.  Traditionally, `Linkd` could be quite resource-intensive because it kept
large amounts of data in memory as it attempted to infer links between devices.
`Enlinkd` instead stores link information from each end separately and then correlates
them when it's time to draw links on the topological maps.  This is much less memory
intensive and keeps from using CPU until you are actually need link data.

`Enlinkd` can gather link information from:

* Cisco Discovery Protocol (CDP)
* Cisco VLAN Trunk Protocol (VTP)
* MIB II 802.1D BRIDGE-MIB
* MIB II ipNetToMediaTable
* Intermediate-System-to-Intermediate-System (IS-IS)
* Link Layer Discovery Protocol (LLDP)
* Open Shortest Path First (OSPF)

Note that `Enlinkd` does *not* support IP Routes discovery, because it is difficult to
mine for correct data and sometimes ends up with false positives.  The `Linkd` service
has not been removed in OpenNMS 14, so if you still need to generate link data based
on IP Routes discovery, you can re-enable `Linkd` in `service-configuration.xml` and
disable the `Enlinkd` service.

The configuration file for enhanced link scanning is `enlinkd-configuration.xml`.

Provisioning
------------

* You can now assign a foreign-source to discovered nodes by adding
  `foreign-source="foo"` to the `discovery-configuration` tag in
  `$OPENNMS_HOME/etc/discovery-configuration.xml`.
* Categories are now handled better in provisiond, so they do not disappear and then
  reappear during scan.
* New provisioning adapter: opennms-snmp-hardware-inventory-provisioning-adapter
  A provisioning adapter using the ENTITY-MIB for collecting hardware inventory while
  doing a provisioning scan.  For details, see [the Hardware Inventory wiki page].
* In 1.12 we introduced the `rescanExisting` flag when performing an import.  Previously
  this would allow you to push nodes to Provisiond in batches and trigger imports, and it
  would only import any *new* nodes that didn't already exist in the database.  In OpenNMS
  14, this flag has been extended to have 3 choices:
  * `true` (default): Import all nodes in the requisition (and remove nodes no longer in
    the requisition), then perform a scan to apply policies and additional detected
    services to those nodes.
  * `false`: Import all *new* nodes in the requisition, skipping the scan phase.
  * `dbonly`: Import all nodes in the requisition (and remove nodes no longer in the
    requisition), then skip the scan phase.
* The WebDetector can now specify a query string.
* It is now possible to selectively detect services on requisitions based on an IPLIKE
  match.  For more details, see: http://issues.opennms.org/browse/NMS-6829

Web UI and APIs
---------------

* Our Vaadin-based UI components have been updated to Vaadin 7, which provides performance
  improvements and better browser support.
* A new UI for creating JMX datacollection configuration was added.  It is reachable from
  the OpenNMS admin page.
* The node groups in geographical maps now include a donut chart which shows alarm status.
* Many of the Jasper-based reports have been cleaned up and handle cases where there is no
  data more consistently.
* The node UI now shows a timeline rather than just a percentage in the availability box.
* The event and alarm list UIs now let you save their search constraints for future reuse.
* The notification UI can now sort by severity.
* The outage UI can now sort by a node's foreign source.
* Some ReST services have been cleaned up to provide more consistent output.
* The group ReST service now supports querying the users or categories associated with
  a group. (ie, `/opennms/rest/groups/users/` and `/opennms/rest/groups/categories/`)
* The node ReST service now supports manually adding and removing hardware inventory data.
  For details, see [the Hardware Inventory wiki page].
* The snmp ReST service now supports SNMP v3 configurations.
* The Snmp Configuration by IP UI in the admin page has been improved and now supports SNMP v3 configurations.

Dashboard
---------

A new dashboard (the "Ops Board") has been added.  It allows you to create a custom UI
that cycles through interesting monitoring information.  The different "boards" which
are shown are configurable in the admin page ("Ops Board Config Web UI").

The following dashlets are available:

* Alarms (a list of alarms)
* Alarm Details (a more detailed list of alarms)
* Charts
* Image (embed an arbitrary image)
* KSC Report
* Map (geographical maps)
* RRD (RRD graph of your choice)
* RTC (same as the front page availability view)
* Summary (alarm trends by severity and UEI)
* Surveillance
* Topology (topology maps)
* URL (embed an arbitrary URL)

Topology Maps
-------------
* Per-user browser navigation and UI-selection history is now preserved.
* Enhanced the topology view to include node and alarm data, synced with map selection.
* Alarm, node, and link data can now auto-update without reloading the page.
* The topology UI now supports search.
* Links from Enhanced Linkd will be shown if it is enabled.
* Many other bug fixes, performance updates, and visual enhancements.

Removals
--------

The `access-point-monitor` and `sms-reflector` projects have been removed from the default
OpenNMS build, as they have not been used in production for quite some time, and
existed for very specific use cases.

The `acl` project has been removed as well.  It was an unfinished attempt at implementing
ACLs in OpenNMS which are superseded by the [User Restriction Filters] feature first added
in OpenNMS 1.10.x.


[GNU Affero General Public License 3.0]: http://www.gnu.org/licenses/agpl-3.0.html
[User Restriction Filters]: http://www.opennms.org/wiki/User_Restriction_Filters
[the Hardware Inventory wiki page]: http://www.opennms.org/wiki/Hardware_Inventory_Entity_MIB
