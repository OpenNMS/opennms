package org.opennms.netmgt.xml.eventconf;

interface Field {
	public String get(org.opennms.netmgt.xml.event.Event matchingEvent);
}