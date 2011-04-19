package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV2TrapEventForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {	

	public SnmpV2TrapEventForwarder(String source_ip, String ip, int port, String community) {
		super(source_ip, ip, port, community);
	}

	public Event flushEvent(Event event) {
		event =	super.flushEvent(event);
		if (event == null)
			return null;
		try {
			sendV2EventTrap(event);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		return event;
		
	}

	public Event flushSyncEvent(Event event) {
		return flushEvent(event);
	}

	
}
