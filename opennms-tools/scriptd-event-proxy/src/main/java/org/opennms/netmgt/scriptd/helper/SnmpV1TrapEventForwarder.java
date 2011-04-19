package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV1TrapEventForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {	

	public SnmpV1TrapEventForwarder(String ip, int port, String community) {
		super(ip, port, community);
	}

	public Event flushEvent(Event event) {
		event =	super.flushEvent(event);
		if (event == null)
			return null;
		try {
			sendV1EventTrap(event);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return event;
		
	}

	public Event flushSyncEvent(Event event) {
		return flushEvent(event);
	}

	
}
