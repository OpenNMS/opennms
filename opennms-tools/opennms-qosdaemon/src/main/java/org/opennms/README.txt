NOTE 
----
A copy of the org.opennms.web.alarm.Alarm class is included here
to provide a severity mapping for openNMS alarms. 
This is done because at the time of writing OpenNMS did not generate
a webapp jar which could be accessed through MAVEN. 
If the webapp is made into a maven package then this can be replaced with
a reference in the pom file. 
Alternatively ( and better ) OpenNMS could include the severity
mapping centrally in the OnmsAlarm model.