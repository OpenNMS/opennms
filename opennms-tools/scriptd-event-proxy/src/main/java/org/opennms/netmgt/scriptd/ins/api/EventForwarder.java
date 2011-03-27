package org.opennms.netmgt.scriptd.ins.api;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.xml.event.Event;
/**
 * Interface to forward events.
 * 
 * @author antonio
 *
 */
public interface EventForwarder {

	List<EventFilter> m_filters = new ArrayList<EventFilter>();
	
	void addEventFilter(EventFilter filter);	

	void flushEvent(Event event);

}
