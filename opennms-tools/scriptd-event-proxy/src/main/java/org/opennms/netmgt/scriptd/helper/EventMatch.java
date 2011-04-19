package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;
/**
 * An EventMatch is an Interface that 
 * is able to specify criteria to match Events
 * An implementation of this interface is a class
 * where you have some criteria to decide if the Event
 * matches or not
 * 
 * @author antonio
 *
 */
public interface EventMatch {

	/**
	 * Method to decide if the Event matches
	 * 
	 * @param event
	 * @return
	 */
	boolean match(Event event);

}
