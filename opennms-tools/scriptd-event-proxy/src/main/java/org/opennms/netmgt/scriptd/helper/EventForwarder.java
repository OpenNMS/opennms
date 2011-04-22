package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

/**
 * Interface to forward events.
 * 
 * @author antonio
 *
 */

public interface EventForwarder {
	
	/**
	 * Method to add a policy rule
	 * to match event to be forwarded or dropped
	 * @param filter
	 */
	public void setEventPolicyRule(EventPolicyRule filter);	

	/**
	 * 
	 * Method used to flush Event
	 * 
	 * @param event
	 * 
	 */
	 public void flushEvent(Event event);

	/**
	 * 
	 * Method used to flush Sync Event
	 * 
	 * @param event
	 * 
	 */
	 public void flushSyncEvent(Event event);

}
