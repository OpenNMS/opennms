## OpenNMS Elasticsearch 2 ReST Plugin

### Maven Project
~~~~
project groupId: org.opennms.plugins
project name:    opennms-es-rest
version:         17.0.1-SNAPSHOT
~~~~

### Description

This project sends opennms data to elastic search using the Jest ReST library
(https://github.com/searchbox-io/Jest)

Three indexes are created; one for alarms, one for alarm change events and one for raw events.

Alarms and alarm change events are only saved if the alarm-change-notifier plugin is also installed to generate alarm change events from the opennms alarms table. 
(https://github.com/gallenc/alarm-change-notifier)

### To install in OpenNMS (Tested on OpenNMS 18.0.0)

#### 1. Add jars to opennms /lib class path

download jar httpasyncclient-osgi-4.0.2.jar 
(from http://repo1.maven.org/maven2/org/apache/httpcomponents/httpasyncclient-osgi/4.0.2/)

copy httpasyncclient-osgi-4.0.2.jar to {opennmshome}/lib

and modify  {opennmshome}/etc/custom.properties to add the following lines into the property org.osgi.framework.system.packages.extra

~~~~
org.osgi.framework.system.packages.extra= ...
...
        org.apache.http.nio.conn;version=4.0.2,\
        org.apache.http.nio.conn.ssl;version=4.0.2,\
        org.apache.http.nio.conn.scheme;version=4.0.2,\
        org.apache.http.impl.nio.client;version=4.3.3,\
        org.apache.http.impl.nio.conn;version=4.0.2,\
...
~~~~


#### 2. Install the plugin in karaf

EITHER

Copy the kar file generated in the kar-package module to the {opennms-home}/deploy directory

(You can see if the plugin has deployed on the karaf terminal (see below) or look at the {opennms-home}/data/log/karaf.log)

OR

You need to add the repo where the feature is installed to the opennms karaf configuration.

Obviously this could point at a remote repository. 
However if you have built on your local machine, add the local repo as follows;
~~~~
sudo vi /opt/opennms/org.ops4j.pax.url.mvn.cfg
~~~~

Change the following property to add file:/home/admin/.m2/repository@snapshots@id=localrepo 
where /home/admin/.m2/repository is the location of local maven repository

~~~~
org.ops4j.pax.url.mvn.repositories= \
    http://repo1.maven.org/maven2@id=central, \
    http://svn.apache.org/repos/asf/servicemix/m2-repo@id=servicemix, \
    http://repository.springsource.com/maven/bundles/release@id=springsource.release, \
    http://repository.springsource.com/maven/bundles/external@id=springsource.external, \
    https://oss.sonatype.org/content/repositories/releases/@id=sonatype, \
    file:/home/admin/.m2/repository@snapshots@id=localrepo
~~~~

Open the karaf command prompt using
~~~~
ssh -p 8101 admin@localhost

(or ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no if no host checking wanted)
~~~~

To install the feature in karaf use

~~~~

karaf@root> features:addurl mvn:org.opennms.plugins/opennms-es-rest/17.0.1-SNAPSHOT/xml/features
karaf@root> features:install opennms-es-rest

(or features:install opennms-es-rest/17.0.1-SNAPSHOT for a specific version of the feature)
~~~~

Example searches to use in Kibana Sense
~~~~
GET /opennms-alarms-*/_search

GET /opennms-alarms-*/

GET opennms-alarms-2016.08/alarmdata/1823

DELETE /opennms-alarms-*/

GET /opennms-events-*/_search

GET /opennms-events-raw*/_search

DELETE /opennms-events-*/

GET /opennms-events-raw*/

GET /opennms-events-alarmchange-*/

GET opennms-events-alarmchange-*/_search

GET opennms-events-alarmchange-2016.08/eventdata/11549

POST opennms-alarms-2016.08/alarmdata/1763/_update 
{"doc_as_upsert":true,"doc":{"suppressedtime":"2016-08-15T20:22:47+01:00","systemid":"00000000-0000-0000-0000-000000000000","dom":"15","severity_text":"Minor","suppresseduntil":"2016-08-15T20:22:47+01:00","description":"Generic Raspberry Pi Alarm Raise 1","mouseovertext":null,"dow":"2","hour":"20","x733probablecause":"1","lasteventid":"11569","lasteventtime":"2016-08-15T20:22:47+01:00","managedobjectinstance":null,"alarmacktime":null,"qosalarmstate":null,"ipaddr":"127.0.0.1","alarmackuser":null,"nodeid":null,"firsteventtime":"2016-08-15T20:22:47+01:00","ifindex":null,"alarmtype":"1","x733alarmtype":null,"logmsg":"Generic Raspberry Pi Alarm Raise 1","tticketid":null,"firstautomationtime":null,"p_PiIoId":"1","clearkey":null,"managedobjecttype":null,"eventuei":"uei.opennms.org\/application\/generic\/piAlarmRaise","counter":"1","applicationdn":null,"operinstruct":"","ossprimarykey":null,"@timestamp":"2016-08-15T20:22:47+01:00","stickymemo":null,"tticketstate":null,"alarmid":"1763","serviceid":null,"reductionkey":"uei.opennms.org\/application\/generic\/piAlarmRaise:0:127.0.0.1:1","suppresseduser":null,"lastautomationtime":null}}


DELETE /opennms-events-alarmchange*/

GET /.kibana/_search


GET _template/eventsindextemplate

DELETE _template/eventsindextemplate


GET /opennms-alarms-*/

GET /opennms-alarms-*/_search

GET /opennms-alarms-*/_search
{
    "script_fields" : {
        "alarm-duration" : {
            "script" : "doc['alarmcleartime'].value - doc['firsteventtime'].value"
        }
    }
        
    
}


~~~~

