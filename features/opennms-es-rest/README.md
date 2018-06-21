## OpenNMS Elasticsearch 2 ReST Plugin

### Maven Project
~~~~
project groupId: org.opennms.plugins
project name:    opennms-es-rest
version:         22.0.1
~~~~

### Description

This project sends opennms data to Elasticsearch using the Jest ReST library
(https://github.com/searchbox-io/Jest)

Three indexes are created; one for alarms, one for alarm change events and one for raw events.

Alarms and alarm change events are only saved if the alarm-change-notifier plugin is also installed to generate alarm change events from the opennms alarms table. 
(https://github.com/gallenc/alarm-change-notifier)

### To install in OpenNMS 

Open the karaf command prompt using
~~~~
ssh -p 8101 admin@localhost

(or ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no if no host checking wanted)
~~~~

To install the feature in karaf use

~~~~

karaf@root> feature:addurl mvn:org.opennms.features/org.opennms.features.opennms-es-rest/22.0.1/xml/features
karaf@root> feature:install opennms-es-rest

(or feature:install opennms-es-rest/22.0.1 for a specific version of the feature)
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

