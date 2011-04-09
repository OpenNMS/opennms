package org.opennms.netmgt.scriptd.helper;

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
	 * Criteria to be used to get the sync events
	 * 
	 * @param criteria
	 * Generic criteria
	 * 
	 */
	void setCriteriaRestriction(String criteria);
	
	/**
	 * 
	 * Calling this method get the synchronization
	 * Events 
	 * 
	 */
	List<Event> sync();

}
