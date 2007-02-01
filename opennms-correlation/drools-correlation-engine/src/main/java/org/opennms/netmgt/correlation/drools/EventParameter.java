package org.opennms.netmgt.correlation.drools;

import org.opennms.netmgt.xml.event.Event;

public class EventParameter {
    private String m_name;
    private Object m_value;
    private Event m_event;
    
    public Event getEvent() {
        return m_event;
    }
    public void setEvent(Event event) {
        m_event = event;
    }
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    public Object getValue() {
        return m_value;
    }
    public void setValue(Object value) {
        m_value = value;
    }
}
