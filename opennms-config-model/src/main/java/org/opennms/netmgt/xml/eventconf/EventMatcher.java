package org.opennms.netmgt.xml.eventconf;

public interface EventMatcher {
	public boolean matches(org.opennms.netmgt.xml.event.Event matchingEvent);
}
