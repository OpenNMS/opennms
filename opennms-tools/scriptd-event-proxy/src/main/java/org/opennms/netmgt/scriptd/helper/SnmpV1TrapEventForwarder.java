package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV1TrapEventForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {	

	public SnmpV1TrapEventForwarder(String source_ip, String ip, int port, String community, SnmpTrapHelper snmpTrapHelper) {
		super(source_ip,ip, port, community, snmpTrapHelper);
	}

	public void flushEvent(Event event) {
		event =	super.filter(event);
		if (event != null) {
		try {
			sendV1EventTrap(event);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		}		
	}

	public void flushSyncEvent(Event event) {
		flushEvent(event);
	}

	
}
