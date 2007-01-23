package org.opennms.netmgt.eventd.datablock;

import org.opennms.netmgt.xml.eventconf.Event;

public interface EventConfDao {

	public abstract Event getMatchingEventConf(org.opennms.netmgt.xml.event.Event trapEvent);

}
