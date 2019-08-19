## OpenNMS Elasticsearch 2 ReST Plugin

### Maven Project
~~~~
project groupId: org.opennms.plugins
project name:    opennms-es-rest
version:         25.0.0-SNAPSHOT
~~~~

### Description

This project sends opennms data to Elasticsearch using the Jest ReST library
(https://github.com/searchbox-io/Jest)

### To install in OpenNMS 

Open the karaf command prompt using
~~~~
ssh -p 8101 admin@localhost

(or ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no if no host checking wanted)
~~~~

To install the feature in karaf use

~~~~

karaf@root> feature:addurl mvn:org.opennms.features/org.opennms.features.opennms-es-rest/25.0.0-SNAPSHOT/xml/features
karaf@root> feature:install opennms-es-rest

(or feature:install opennms-es-rest/25.0.0-SNAPSHOT for a specific version of the feature)
~~~~

Example searches to use in Kibana Sense
~~~~
GET /opennms-events-*/_search

GET /opennms-events-raw*/_search

DELETE /opennms-events-*/

GET /opennms-events-raw*/

GET /.kibana/_search


GET _template/eventsindextemplate

DELETE _template/eventsindextemplate

~~~~

