# To run the nrtg feature follow this steps
# All commands marked as osgi> are for the karaf console.
# To connect to the karaf console ssh in like this
# ssh -p 8101 admin@localhost
# password is admin too
1. Start an activemq broker on localhost

  There are two options for this:

  a. download the apache activemq distribution and run it (bin/activemq console)

  b. start one inside the opennms osgi container by running the
     following commands:

     osgi> features:install activemq-blueprint
     osgi> activemq:create-broker -t blueprint

2. Install the nrtg feature in the opennms osgi container

  You may start this feature by running

  osgi> features:install nrtg

  alternatively you may install the individual features for nrtg

  osgi> features:install osgi-nrtg-api osgi-nrtg-nrtcollector osgi-nrtg-protocolcollector-snmp

3. Install NRTG Web components
  - Provide the org.opennms.features.nrtg/nrtg-web/1.11.3-SNAPSHOT to the local .m2/repository 
    or to the opennms/system folder in the same structure like in .m2/repository
  - The nrtg servlet sample can be installed by doing the following:

    osgi> bundle:install mvn:org.apache.activemq/activemq-web/5.6.0
    osgi> bundle:install mvn:org.opennms.features.nrtg/nrtg-web/1.11.3-SNAPSHOT

    osgi> bundle:start ${bundle Id of activemq-web}
    osgi> bundle:start ${bundle Id of NRTG Web Interface}
