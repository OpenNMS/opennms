package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public class EventForwarderDefaultImpl extends AbstractEventForwarder implements
		EventForwarder {

	public Event flushEvent(Event event) {
		return super.flushEvent(event);
	}

	public Event flushSyncEvent(Event event) {
		return super.flushEvent(event);
	}
	
	

}
