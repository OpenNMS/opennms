package org.opennms.netmgt.correlation.ncs;

import org.opennms.netmgt.xml.event.Event;

public class Resolved {
	private Event m_cause;
	private Event m_resolution;
	
	public Resolved(Event cause, Event resolution) {
		m_cause = cause;
		m_resolution = resolution;
	}

	public Event getCause() {
		return m_cause;
	}

	public void setCause(Event cause) {
		m_cause = cause;
	}

	public Event getResolution() {
		return m_resolution;
	}

	public void setResolution(Event resolution) {
		m_resolution = resolution;
	}

	@Override
	public String toString() {
		return "Resolved[ " +
				"cause=" + m_cause + 
				", resolution="
				+ m_resolution + " ]";
	}
	
	
	
	

}
