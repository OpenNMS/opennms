package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV2InformEventForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {	

	public SnmpV2InformEventForwarder(String ip, int port, String community, int timeout, int retries, SnmpTrapHelper snmpTrapHelper) {
		super(ip, port, community, timeout, retries, snmpTrapHelper);
	}

	public void flushEvent(Event event) {
		event =	super.filter(event);
		if (event != null) {
		try {
			sendV2EventInform(event);
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

	public void sendStartSync() {
		throw new UnsupportedOperationException();
	}

	public void sendEndSync() {
		throw new UnsupportedOperationException();
	}

	
}
