package org.opennms.netmgt.scriptd.ins.api;

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
public interface EventFilter {

	/**
	 * Method to decide if the filter
	 * must be applied
	 * 
	 * @param event
	 * @return
	 */
	boolean match(Event event);

	/**
	 * Method to decide if the event 
	 * should be filtered or not
	 * 
	 * @param event
	 * @return
	 */
	boolean filter();

}
