package org.opennms.netmgt.scriptd.helper;

import org.opennms.netmgt.xml.event.Event;

public abstract class AbstractEventForwarder implements EventForwarder {

	EventPolicyRule m_filter;
	public void setEventPolicyRule(EventPolicyRule filter) {
		m_filter = filter;
	}

	public Event flushEvent(Event event) {
		return m_filter.filter(event);
	}

}
