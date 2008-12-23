Sample Use of the Event Proxy:

Here is a sample configuration for event proxy using Scriptd:

When an alarm is created a special event arose. (The event are defined in Proxy.events.xml)
This work is done by the provided vacuumd-configuration.xml.
This Events are filtered by script-configuration.xml example find and send throw the Proxy Event to connected clients.
The Scriptd service provide also initialization and startup of the event proxy.

To synchronize with alarm table you can send to the event proxy the script:

LIST_CURRENT_ALARM_REQUEST

You may need authentication at the beginning of the connection to Event Proxy using AUTH String.

see http://www.opennms.org/index.php/InsEventProxy for details

