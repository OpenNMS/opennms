package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public class EventForwarderDefaultImpl extends AbstractEventForwarder implements
		EventForwarder {

	@Override
	/**
	 * This method add a new parm to the
	 */
	protected Event expand(Event event) {
		return event;
	}

}
