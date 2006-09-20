package org.opennms.netmgt.mock;


import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.xml.event.Event;

/**
 * Need this class because Event doesn't properly implement hashCode
 */
public class EventWrapper {
    private Event m_event;

    public EventWrapper(Event event) {
        m_event = event;
    }

    public boolean equals(Object o) {
        EventWrapper w = (EventWrapper) o;
        return MockEventUtil.eventsMatch(m_event, w.m_event);
    }

    public Event getEvent() {
        return m_event;
    }

    public int hashCode() {
        return m_event.getUei().hashCode();
    }
    
    public String toString() {
        Event event = m_event;
    		return EventUtils.toString(event);
    }
}