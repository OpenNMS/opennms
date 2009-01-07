package org.opennms.netmgt.alarmd;

import org.opennms.netmgt.xml.event.Event;

public interface AlarmPersister {

    public abstract void persist(Event event);

}