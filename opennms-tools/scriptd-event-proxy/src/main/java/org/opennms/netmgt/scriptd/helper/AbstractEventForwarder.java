package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public abstract class AbstractEventForwarder implements EventForwarder {

	public void addEventFilter(EventFilter filter) {
		m_filters.add(filter);

	}

	public Event flushEvent(Event event) {
		for (EventFilter filter: m_filters) {
			if (filter.match(event))
				event = filter.filter(event);
		}
		if (event != null )
			event = expand(event);
		return event;
	}

	protected abstract Event expand(Event event);

}
