package org.opennms.netmgt.correlation.ncs;

import org.opennms.netmgt.xml.event.Event;

public class ComponentEvent {

	protected Component m_component;
	protected Event m_event;
	
	protected ComponentEvent(Component component, Event event) {
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
		return getClass().getSimpleName() + " [" +
				"component=" + m_component + 
				", event=" + m_event.getUei() + "(" + m_event.getDbid() + ")" +
				"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_component == null) ? 0 : m_component.hashCode());
		//result = prime * result + ((m_event == null) ? 0 : m_event.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		ComponentEvent other = (ComponentEvent) obj;
		if (m_component == null) {
			if (other.m_component != null)
				return false;
		} else if (!m_component.equals(other.m_component))
			return false;
		
//		if (m_event == null) {
//			if (other.m_event != null)
//				return false;
//		} else if (!m_event.equals(other.m_event))
//			return false;

		return true;
	}

}
