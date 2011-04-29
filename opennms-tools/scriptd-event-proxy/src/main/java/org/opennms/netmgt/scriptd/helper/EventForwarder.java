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

	 /**
	  * This method should be invoked before
	  * flushing sync events
	  * The class implementation should
	  * send the "startSync" event
	  * in the preferred format
	  * 
	  */
	 public void sendStartSync();
	 
	 /**
	  * This method should be invoked after
	  * flushing sync events.
	  * The class implementation should 
	  * send the "endSync" event
	  * in the preferred format
	  * 
	  */
	 public void sendEndSync();
}
