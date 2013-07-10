package org.opennms.netmgt.config;

import java.io.Serializable;
import java.util.Comparator;

import org.opennms.netmgt.xml.eventconf.Event;

public class EventLabelComparator implements Comparator<Event>, Serializable {
    private static final long serialVersionUID = 7976730920523203921L;

    @Override
    public int compare(final Event e1, final Event e2) {
        return e1.getEventLabel().compareToIgnoreCase(e2.getEventLabel());
    }
}