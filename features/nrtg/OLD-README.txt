#####
# Build root project first
#####
mvn clean install

######
# Running as jar
#####
cd jar/satellit
mvn assembly:assembly
java -jar target/org.opennms.nrtg.jar.nrtcollector-1.0-SNAPSHOT-jar-with-dependencies.jar

################
# Install in karaf 
################
get latest karaf http://karaf.apache.org/

copy config.properties to etc (javax.xml.bind.* with version? from InetAddressXmlAdapter, opennms-utils)

# install activemq
feature:addurl mvn:org.apache.activemq/activemq-karaf/5.6.0/xml/features
feature:install activemq

# felxjson
bundle:install wrap:mvn:net.sf.flexjson/flexjson

# install our api
feature:addurl mvn:org.opennms.nrtg.osgi/osgi-nrtg-api//xml/features
feature:install osgi-nrtg-api

# and the implementation
feature:addurl mvn:org.opennms.nrtg.osgi/osgi-nrtg-nrtcollector//xml/features
feature:install osgi-nrtg-nrtcollector

# from opennms-utils
install mvn:commons-io/commons-io//
install mvn:commons-lang/commons-lang//

# dnsjava
bundle:install wrap:mvn:junit/junit
bundle:install wrap:mvn:org.snmp4j/snmp4j

# install the snmp collector
feature:addurl mvn:org.opennms.nrtg.osgi.protocolcollector/osgi-nrtg-protocolcollector-snmp//xml/features
feature:install osgi-nrtg-protocolcollector-snmp

## Optinal
# install the karaf web gui
feature:install war

# install the activemq-webgui
feature:install activemq-web-console 

##########
# JSM Broker
##########

download activemq and start it (the broker)
./bin/activemq start

or start the broker inside karaf:

# create a broker
k# feature:install activemq-spring
k# activemq:create-broker



########
# TODO:
########

- configurable broker url
- build karaf archive/maven goal to start
- Protocollcollector shouldn't be mandatory
