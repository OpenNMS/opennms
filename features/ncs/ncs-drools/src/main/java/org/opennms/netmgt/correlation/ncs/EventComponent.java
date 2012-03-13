package org.opennms.netmgt.correlation.ncs;


import org.opennms.netmgt.xml.event.Event;

public class EventComponent {
    
    private Component m_component;
    private Event     m_event;
    
    public EventComponent(Component component, Event event) {
        m_component = component;
        m_event = event;
    }
    
    public Component getComponent() {
        return m_component;
    }
    
    public void setComponent(Component component) {
        m_component = component;
    }
    
    public Event getEvent() {
		return m_event;
	}

	public void setEvent(Event event) {
		m_event = event;
	}

	@Override
	public String toString() {
		return "EventComponent [" +
				"component=" + m_component + 
				", event=" + m_event.getUei() + "(" + m_event.getDbid() + ")" +
				"]";
	}


}
