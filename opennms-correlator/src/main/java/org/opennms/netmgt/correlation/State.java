package org.opennms.netmgt.correlation;

import org.opennms.netmgt.xml.event.Event;

public interface State {
	
	State processTimerExpired(int timerId);

	State processEvent(Event e);

	boolean isEndState();

}
