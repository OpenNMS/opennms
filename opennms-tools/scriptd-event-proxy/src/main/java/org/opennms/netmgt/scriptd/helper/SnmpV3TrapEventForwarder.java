package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV3TrapEventForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {	

	public SnmpV3TrapEventForwarder(String ip, int port, int securityLevel,
			String securityname, String authPassPhrase, String authProtocol,
			String privPassPhrase, String privprotocol, SnmpTrapHelper snmpTrapHelper ) {
		super(ip,port,securityLevel,securityname,authPassPhrase,authProtocol,privPassPhrase,privprotocol,snmpTrapHelper);
	}

	public void flushEvent(Event event) {
		event =	super.filter(event);
		if (event != null) {
			try {
				sendV3EventTrap(event);
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
