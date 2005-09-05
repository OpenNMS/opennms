package org.opennms.netmgt.mock;

import org.opennms.netmgt.xml.event.Event;

/**
 * Need this class because Event doesn't properly implement hashCode
 */
class EventWrapper {
    Event m_event;

    EventWrapper(Event event) {
        m_event = event;
    }

    public boolean equals(Object o) {
        EventWrapper w = (EventWrapper) o;
        return MockUtil.eventsMatch(m_event, w.m_event);
    }

    public Event getEvent() {
        return m_event;
    }

    public int hashCode() {
        return m_event.getUei().hashCode();
    }
}