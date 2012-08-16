# All of the nrtg bundles are now included in the opennms system
  directory.  The features are not yet started by default.  There are
  two steps to running the code

1. Start an activemq broker on localhost

There are two options for this:

a. downlaod the apache activemq distribution and run it (bin/activemq
console)

b. start one inside the opennms osgi container by running the
following commands:

features:install activemq-blueprint
activemq:create-broker -t blueprint

2. Install the nrtg feature in the opennms osgi container

You may start this feature by running

features:install nrtg

alternatively you may install the individual features for nrtg

features:install osgi-nrtg-api osgi-nrtg-nrtcollector osgi-nrtg-protocolcollector-snmp

NRTG Web

1.  The nrtg servlet sample can be installed by doing the following:

bundle:install mvn:org.apache.activemq/activemq-web/5.6.0
bundle:install mvn:org.opennms.features.nrtg/nrtg-web/1.11.2-SNAPSHOT

bundle:start activemq-web (by id)
bundle:start NRTG Web Interface (by id)

Then visit the page bellow substituing your resourdId and report  

http://localhost:8980/opennms/osgi/nrt/starter?resourceId=node[26].nodeSnmp[]&report=mib2.tcperrs

