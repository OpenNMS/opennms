package org.opennms.netmgt.scriptd.helper;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.xml.event.Event;
/**
 * An EventFilter is a filter of Events
 * An implementation of this interface is a class
 * where you have some criteria to decide if the Event
 * pass the filter or not
 * 
 * @author antonio
 *
 */
public interface EventPolicyRule {

	List<EventMatch> m_filter = new ArrayList<EventMatch>();
	List<Boolean> m_forwardes = new ArrayList<Boolean>();

	/**
	 * 
	 * Method to decide if the event 
	 * should be forwarder
	 * 
	 * @return event
	 * the filtered Event
	 * that can be null or 
	 * with parameter changes
	 * 
	 */

	Event filter(Event event);
	
	void addForwardRule(EventMatch eventMatch);
	
	void addDropRule(EventMatch eventMatch);

}
