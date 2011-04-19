package org.opennms.netmgt.scriptd.helper;

import java.util.Map;

import org.opennms.netmgt.xml.event.Event;

public abstract class AbstractEventPolicyRule implements EventPolicyRule {

	private boolean forward;
	
	public void addForwardRule(EventMatch match) {
		m_filter.add(match);
		m_forwardes.add(new Boolean(true));
	}

	public void addDropRule(EventMatch match) {
		m_filter.add(match);
		m_forwardes.add(new Boolean(false));
	}

	public Event filter(Event event) {
		forward=true;
		int count = 0;
		for (EventMatch filter: m_filter) {
			if (filter.match(event)) {
				forward = m_forwardes.get(count).booleanValue();
				break;
			}
			count++;
		}
		if (forward)
			return expand(event);
		return null;
	}

	protected abstract Event expand(Event event);

}
