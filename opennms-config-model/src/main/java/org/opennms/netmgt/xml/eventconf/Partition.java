package org.opennms.netmgt.xml.eventconf;

import java.util.List;

public interface Partition {
	List<String> group(Event eventConf);
	String group(org.opennms.netmgt.xml.event.Event matchingEvent);
}