package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public class EventForwarderDefaultImpl extends AbstractEventForwarder implements
		EventForwarder {

	public void flushEvent(Event event) {
		super.filter(event);
	}

	public void flushSyncEvent(Event event) {
		super.filter(event);
	}
		
	public void sendStartSync() {
	}

	public void sendEndSync() {
	} 
	
}
