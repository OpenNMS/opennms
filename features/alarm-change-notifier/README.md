# Alarm Change Notifier OpenNMS plugin

## Maven project

~~~~
project groupId: org.opennms.plugins
project name:    alarm-change-notifier
version:         23.0.0-SNAPSHOT
~~~~

## Description
This project generates new OpenNMS events corresponding to changes in alarms
The new events are defined in the AlarmChangeNotifierEvents.xml file

These events contain a json copy of the database table before changes in %parm[oldalarmvalues]%
and after changes in %parm[newalarmvalues]%

%parm[alarmid]% contains the alarmid of the alarm which has changed

The generated event itself references copies of the nodeid, interface and service contained in the original alarm. 
This way the alarm change events are associated with the otiginal source of the alarm.

Alarm change events have a severity of normal since they only reflect changes to the alarm.

Events from the alarm-change-notifier are used by the opennms-es-rest plugin to send alarm history to Elasticsearch
(See https://github.com/gallenc/opennms-es-rest)


## To install on OpenNMS

### 1. add events definition
copy /misc AlarmChangeNotifierEvents.xml to {opennms-home}/etc/events

add the following line to {opennms-home}/etc/eventconf.xml

~~~~
<event-file>events/AlarmChangeNotifierEvents.xml</event-file>
~~~~

### 2. install the plugin

EITHER

Copy the kar file generated in the kar-package module to the {opennms-home}/deploy directory

(You can see if the plugin has deployed on the karaf terminal (see below) or look at the {opennms-home}/data/log/karaf.log)

OR

You need to add the repo where the feature is installed to the opennms karaf configuration.
Obviously this could point at a remote repository
However if you have built on your local machine, add the local repo as follows;
~~~~
sudo vi /opt/opennms/org.ops4j.pax.url.mvn.cfg
~~~~

change the following property to add file:/home/admin/.m2/repository@snapshots@id=localrepo 
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

open karaf command prompt using
~~~~
ssh -p 8101 admin@localhost

(or ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no if no host checking wanted)
~~~~

to install the feature in karaf use

~~~~
karaf@root> feature:addurl mvn:org.opennms.plugins/alarm-change-notifier/23.0.0-SNAPSHOT/xml/features
karaf@root> feature:install alarm-change-notifier

(or feature:install alarm-change-notifier/23.0.0-SNAPSHOT for a specific version of the feature)
~~~~




