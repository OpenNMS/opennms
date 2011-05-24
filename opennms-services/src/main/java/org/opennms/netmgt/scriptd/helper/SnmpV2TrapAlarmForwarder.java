package org.opennms.netmgt.scriptd.helper;

import java.net.UnknownHostException;

import org.opennms.netmgt.xml.event.Event;

public class SnmpV2TrapAlarmForwarder extends SnmpTrapForwarderHelper implements
		EventForwarder {


	public SnmpV2TrapAlarmForwarder(String ip, int port, String community, SnmpTrapHelper snmpTrapHelper) {
		super(ip, port, community, snmpTrapHelper);
	}

	public void flushEvent(Event event) {
		event =	super.filter(event);
		if (event != null){
		try {
			sendV2AlarmTrap(event, false);
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
			sendV2AlarmTrap(event, true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SnmpTrapHelperException e) {
			e.printStackTrace();
		}
		}
	}

	public void sendStartSync() {
		super.sendV2StartSyncTrap();
	}

	public void sendEndSync() {
		super.sendV2EndSyncTrap();		
	}

	
}
