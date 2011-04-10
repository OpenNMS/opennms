package org.opennms.netmgt.scriptd.helper;

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

	/**
	 * The list of eventFilter
	 */
	List<EventFilter> m_filters = new ArrayList<EventFilter>();
	
	/**
	 * Method to add an EventFilter
	 * to filter forwarding
	 * @param filter
	 */
	void addEventFilter(EventFilter filter);	

	/**
	 * 
	 * Method used to flush Event
	 * 
	 * @param event
	 * @return event
	 * 
	 */
	Event flushEvent(Event event);

}
