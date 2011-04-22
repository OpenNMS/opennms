package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV2InformAlarmForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {


	public SnmpV2InformAlarmForwarder(String ip, int port, String community, int timeout, int retries, SnmpTrapHelper snmpTrapHelper) {
		super(ip, port, community, timeout, retries, snmpTrapHelper);
	}

	public void flushEvent(Event event) {
		event =	super.filter(event);
		if (event != null){
		try {
			sendV2AlarmInform(event, false);
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
			sendV2AlarmInform(event, true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		}
	}

	
}
