What's New in OpenNMS 16
========================

Since OpenNMS 15, many features have been refactored to increase modularity of the OpenNMS code. New reporting capabilities have also been added to
support integration with external graphing engines such as Grafana.

Java 8
------

All build and dependency issues with Java 8 have been resolved and now OpenNMS 16 requires Java 8 as the runtime environment. To run OpenNMS 16,
we recommend the most recent version of Oracle JDK 8 for your platform.

Capsd Has Been Removed
----------------------

The legacy capability scanner, Capsd, has been removed from OpenNMS. It was deprecated in OpenNMS 1.12 and disabled by default in 
OpenNMS 14 in favor of the provisioning systems of OpenNMS. The Capsd code has now been removed completely which will enable us to
streamline the data access code of OpenNMS. This will make the system easier to maintain and may improve performance down the road.

Linkd is Deprecated
-------------------

The original link scanning daemon, Linkd, has been superseded by Enhanced Linkd since it was introduced in OpenNMS 15. 
Enhanced Linkd is a refactored replacement that is more efficient than the original Linkd code. 
Linkd is disabled by default in OpenNMS 15 and 16 and we consider it deprecated. 
The code for Linkd will be removed completely in OpenNMS 17. 
All users should migrate over to using Enhanced Linkd as a replacement.

API Changes
-----------

Several classes changed location in OpenNMS 16 and these changes may require you to update configuration files or scripts with the new names.

* The *IndexStorageStrategy* and *PersistAllSelectorStrategy* classes moved into the *org.opennms.netmgt.collection.support* Java package. These classes 
  are heavily referenced in data collection configuration files. There is an upgrade task that should update all data collection files when you 
  run the *install* command after upgrading OpenNMS.
* All *EventUtils* class methods were consolidated inside the *org.opennms.netmgt.model.events.EventUtils* class.

New Features
------------

* There is a new REST service that can be used to export performance data. This service can be used to easily export OpenNMS metrics into external
  graphing engines such as Grafana. See the REST API documentation for details.

Dependency Updates
------------------

A number of internal libraries have been upgraded for bugfixes and new features. None of these updates should require configuration changes.

* Spring has been upgraded from 3.2.9 to 4.0.5.
* Spring Security has been upgraded from 3.1.7 to 3.2.7.
* Drools has been upgraded from 5.1.1 to 6.0.1.
* Apache Camel has been upgraded from 2.13.2 to 2.14.1.
* Smack (used for XMPP notifications) has been upgraded from 3.0.4 to 4.0.6.
* The webapp schemas have all been updated to the Servlet 3.0 specification.
* Smack (used for XMPP notifications) has been upgraded from 3.0.4 to 4.0.6.

Internal Updates
----------------

Various parts of the OpenNMS system were rewritten in OpenNMS 16 to improve maintainability or performance of the code.

* The JMX detector, monitor, and collector were refactored for modularity.
* The Dashboard was rewritten using the Vaadin UI toolkit to improve and modernize its look-and-feel.
* Bean Scripting Framework (BSF) notifications and the BSFMonitor were optimized and are now much more efficient. (Thanks to David Schlenk for this contribution!)
* RTC, which calculates the availability percentages for the category panel on the main page, was rewritten using Spring for initialization and 
  using database calls to perform availability calculations. This will improve its maintainability.
* The web controllers for provisioning, RANCID integration, reports, the node list, and Remote Poller administration were rewritten
  to modernize their code.
* The REST portion of the OpenNMS webapp was modularized into its own project.
* The web UI service layer was separated from the main web UI to improve modularity.


[GNU Affero General Public License 3.0]: http://www.gnu.org/licenses/agpl-3.0.html
