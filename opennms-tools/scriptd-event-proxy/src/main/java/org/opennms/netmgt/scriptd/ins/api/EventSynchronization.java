package org.opennms.netmgt.scriptd.ins.api;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;

/**
 * Interface to use for triggering
 * a generic event synchronization
 *  
 * @author antonio
 *
 */
public interface EventSynchronization {

	/**
	 * Add an eventForwarder
	 * to forward Events
	 * 
	 * Multiple eventForwarders can be used to forward events
	 * 
	 * @param eventForwarder
	 */
//	void addEventForwarder(EventForwarder eventForwarder);
	
	/**
	 * Criteria to be used to get the sync events
	 * 
	 * @param criteria
	 */
	void setCriteriaRestriction(String criteria);
	
	/**
	 * Calling this method get the synchronization
	 */
	List<Event> sync();

}
