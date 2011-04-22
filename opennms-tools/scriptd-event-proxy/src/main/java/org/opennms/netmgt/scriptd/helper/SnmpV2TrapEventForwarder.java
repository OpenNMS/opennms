package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV2TrapEventForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {	

	public SnmpV2TrapEventForwarder(String ip, int port, String community, SnmpTrapHelper snmpTrapHelper) {
		super(ip, port, community, snmpTrapHelper);
	}

	public void flushEvent(Event event) {
		event =	super.filter(event);
		if (event != null) {
		try {
			sendV2EventTrap(event);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		}
		
	}

	public void flushSyncEvent(Event event) {
		flushEvent(event);
	}

	
}
