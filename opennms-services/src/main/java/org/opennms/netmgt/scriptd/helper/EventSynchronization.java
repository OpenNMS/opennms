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
	 * This method just add an event forwarder
	 * to forward sync events
	 * 
	 * @param forwarder
	 */

	void addEventForwarder(EventForwarder forwarder);
	
	/**
	 * 
	 * Calling this method get the synchronization
	 * Events 
	 * 
	 */
	List<Event> getEvents();

	/**
	 * 
	 * this method sync
	 * Events 
	 * 
	 */
	void sync();

}
