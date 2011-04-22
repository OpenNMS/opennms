package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV3InformAlarmForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {


	public SnmpV3InformAlarmForwarder(String ip, int port,int timeout, int retries,int securityLevel, 
			String securityname, String authPassPhrase, String authProtocol,
			String privPassPhrase, String privprotocol, SnmpTrapHelper snmpTrapHelper) {
		super(ip,port,securityLevel,securityname,authPassPhrase,authProtocol,privPassPhrase,privprotocol,timeout, retries, snmpTrapHelper);
	}

	public void flushEvent(Event event) {
		event =	super.filter(event);
		if (event != null) {
		try {
			sendV3AlarmInform(event, false);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		}
	}

	public void flushSyncEvent(Event event) {
		event =	super.filter(event);
		if (event != null) {
		try {
			sendV3AlarmInform(event, true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		}
	}
}
