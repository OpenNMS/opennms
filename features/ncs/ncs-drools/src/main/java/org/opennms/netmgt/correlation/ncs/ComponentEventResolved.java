package org.opennms.netmgt.correlation.ncs;

public class ComponentEventResolved {
	
	private ComponentDownEvent m_cause;
	private ComponentUpEvent m_resolution;
	
	public ComponentEventResolved(ComponentDownEvent cause, ComponentUpEvent resolution) {
		m_cause = cause;
		m_resolution = resolution;
	}

	public ComponentDownEvent getCause() {
		return m_cause;
	}

	public void setCause(ComponentDownEvent cause) {
		m_cause = cause;
	}

	public ComponentUpEvent getResolution() {
		return m_resolution;
	}

	public void setResolution(ComponentUpEvent resolution) {
		m_resolution = resolution;
	}

	@Override
	public String toString() {
		return "Resolved[ " +
				"cause=" + m_cause + 
				", resolution="
				+ m_resolution + " ]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_cause == null) ? 0 : m_cause.hashCode());
		result = prime * result
				+ ((m_resolution == null) ? 0 : m_resolution.hashCode());
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
		ComponentEventResolved other = (ComponentEventResolved) obj;
		if (m_cause == null) {
			if (other.m_cause != null)
				return false;
		} else if (!m_cause.equals(other.m_cause))
			return false;
		if (m_resolution == null) {
			if (other.m_resolution != null)
				return false;
		} else if (!m_resolution.equals(other.m_resolution))
			return false;
		return true;
	}
	
	


}
