package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV1TrapAlarmForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {


	public SnmpV1TrapAlarmForwarder(String source_ip, String ip, int port, String community) {
		super(source_ip, ip, port, community);
	}

	public Event flushEvent(Event event) {
		event =	super.flushEvent(event);
		if (event == null)
			return null;
		try {
			sendV1AlarmTrap(event, false);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return event;
		
	}

	public Event flushSyncEvent(Event event) {
		event =	super.flushEvent(event);
		if (event == null)
			return null;
		try {
			sendV1AlarmTrap(event, true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return event;
	}

	
}
