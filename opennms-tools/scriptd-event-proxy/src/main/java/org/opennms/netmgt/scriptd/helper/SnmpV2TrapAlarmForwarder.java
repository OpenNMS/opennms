package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV2TrapAlarmForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {


	public SnmpV2TrapAlarmForwarder(String ip, int port, String community) {
		super(ip, port, community);
	}

	public Event flushEvent(Event event) {
		event =	super.flushEvent(event);
		if (event == null)
			return null;
		try {
			sendV2AlarmTrap(event, false);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		return event;
		
	}

	public Event flushSyncEvent(Event event) {
		event =	super.flushEvent(event);
		if (event == null)
			return null;
		try {
			sendV2AlarmTrap(event, true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		return event;
	}

	
}
