package org.opennms.netmgt.correlation;

import org.opennms.netmgt.xml.event.Event;

public interface StateActions {
	
	void sendEvent(Event e);
	
	int setTimer(long intervalInMillis);

}
