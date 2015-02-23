What's New in OpenNMS 15
========================

Since OpenNMS 14, a number of backend architectural changes have occurred, as well as a
UI rewrite.

Java 7 and 8
------------

OpenNMS 14 dropped support for Java 6, which has been EOL'd for some time now.  OpenNMS 15
requires Java 7 or higher to run.

Note that *building* under Java 8 still has some known issues.  It is strongly recommended
that you build and run under Java 7 until Java 8 has had enough time to be well-tested.

Core Updates
------------

As always, many updates and cleanups have been made to the OpenNMS core, through refactoring,
addition of unit tests, and other code modernization.

* A few subsystems have been updated to run properly under OSGi.
* A large number of places where we were still using JDBC calls have been converted
  to use our DAO infrastructure and Hibernate.
* The OpenNMS ReST APIs now support CORS properly to aid in writing web applications that
  consume or update OpenNMS data.

Events
------

New or updated trap definitions have been added for the following classes of devices:

* Citrix NetScaler
* Mikrotik RouterOS
* OpenSSH syslog events
* Procmail syslog events
* Postfix syslog events
* Siemens HiPath
* Veeam Backup/Replication

Web UI and APIs
---------------

* The Jetty container has been upgraded to use Jetty 8.
* The entire web UI has been updated to use bootstrap themeing.  While our initial
  implementation was designed to match the existing OpenNMS UI as much as possible,
  this now makes it MUCH easier to improve the UI more rapidly going forward.


[GNU Affero General Public License 3.0]: http://www.gnu.org/licenses/agpl-3.0.html
[User Restriction Filters]: http://www.opennms.org/wiki/User_Restriction_Filters
[the Hardware Inventory wiki page]: http://www.opennms.org/wiki/Hardware_Inventory_Entity_MIB
