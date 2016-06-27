About OpenNMS Meridian 2015.1.1
===============================

OpenNMS Meridian is a custom build of OpenNMS Horizon designed for stability and long-term support.

Since Meridian 2015.1.0, there have been tons of minor bug fixes.  For a complete list, see the
release notes [here](http://meridian.opennms.com/releasenotes/2015/latest/).

The most notable changes over 2015.1.0 are:

* Security: RMI now listens on localhost only, by default.  This can be overridden by setting the
  property `opennms.poller.server.serverHost=` in opennms.conf or opennms.properties.  The old
  behavior can be restored by setting the property to: `opennms.poller.server.serverHost=0.0.0.0`.
* Security: Old crypto jars meant for compatibility with ancient JDKs which caused some more
  modern ciphers to not be available in portions of OpenNMS were removed.
* Linkd and Enlinkd have both received a number of fixes and enhancements.
* The JMX configuration tool has been updated and fixed.
* Multiple scanning or failure issues in Provisiond have been fixed.
* Performance has been improved in the topology UI.
* A race condition that could cause dropped ICMP packets under high load was fixed.
