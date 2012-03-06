package org.opennms.netmgt.correlation.ncs;

public class DependenciesNeeded {
	
	private Component m_component;
	private Object m_requestor;
	
	public DependenciesNeeded(Component component, Object requestor) {
		m_component = component;
		m_requestor = requestor;
	}

	public Component getComponent() {
		return m_component;
	}

	public void setComponent(Component component) {
		m_component = component;
	}

	public Object getRequestor() {
		return m_requestor;
	}

	public void setRequestor(Object requestor) {
		m_requestor = requestor;
	}

	@Override
	public String toString() {
		return "DependenciesNeeded [component=" + m_component
				+ ", requestor=" + m_requestor + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_component == null) ? 0 : m_component.hashCode());
		result = prime * result
				+ ((m_requestor == null) ? 0 : m_requestor.hashCode());
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
		DependenciesNeeded other = (DependenciesNeeded) obj;
		if (m_component == null) {
			if (other.m_component != null)
				return false;
		} else if (!m_component.equals(other.m_component))
			return false;
		if (m_requestor == null) {
			if (other.m_requestor != null)
				return false;
		} else if (!m_requestor.equals(other.m_requestor))
			return false;
		return true;
	}
	
	
	
	

}
