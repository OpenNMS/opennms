package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV3TrapEventForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {	

	public SnmpV3TrapEventForwarder(String ip, int port, int securityLevel,
			String securityname, String authPassPhrase, String authProtocol,
			String privPassPhrase, String privprotocol) {
		super(ip,port,securityLevel,securityname,authPassPhrase,authProtocol,privPassPhrase,privprotocol);
	}

	public Event flushEvent(Event event) {
		event =	super.flushEvent(event);
		if (event == null)
			return null;
		try {
			sendV3EventTrap(event);
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
