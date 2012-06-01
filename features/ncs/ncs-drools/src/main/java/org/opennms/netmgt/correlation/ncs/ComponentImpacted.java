package org.opennms.netmgt.correlation.ncs;

import static org.opennms.netmgt.correlation.ncs.Utils.nullSafeEquals;

public class ComponentImpacted {

	private Component m_target;
	private ComponentDownEvent m_cause;
	
	public ComponentImpacted() {}
	
	public ComponentImpacted(Component target, ComponentDownEvent cause)
	{
		m_target = target;
		m_cause = cause;
	}
	
	public Component getTarget() {
		return m_target;
	}
	public void setTarget(Component target) {
		m_target = target;
	}
	public ComponentDownEvent getCause() {
		return m_cause;
	}
	public void setCause(ComponentDownEvent cause) {
		m_cause = cause;
	}

	@Override
	public String toString() {
		return "ComponentImpacted[ target=" + m_target + 
				", cause=" + m_cause +
				" ]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_cause == null) ? 0 : m_cause.hashCode());
		result = prime * result
				+ ((m_target == null) ? 0 : m_target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj instanceof ComponentImpacted) {
			ComponentImpacted o = (ComponentImpacted)obj;
			return nullSafeEquals(m_target, o.m_target)
				&& nullSafeEquals(m_cause, o.m_cause);
		}
		return false;
	}
	
	
	
	

}