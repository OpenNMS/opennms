package org.opennms.netmgt.correlation.ncs;

import static org.opennms.netmgt.correlation.ncs.Utils.nullSafeEquals;

import org.opennms.netmgt.xml.event.Event;

public class Impacted {

	private Component m_target;
	private Event m_cause;
	
	public Impacted() {}
	
	public Impacted(Component target, Event cause)
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
	public Event getCause() {
		return m_cause;
	}
	public void setCause(Event cause) {
		m_cause = cause;
	}

	@Override
	public String toString() {
		return "Impacted[ target=" + m_target + 
				", cause=" + m_cause.getUei() + "(" + m_cause.getDbid() + ")" +
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
		
		if (obj instanceof Impacted) {
			Impacted o = (Impacted)obj;
			return nullSafeEquals(m_target, o.m_target)
				&& m_cause == null 
				? o.m_cause == null
				: o.m_cause == null
				? false
			    : m_cause.getDbid() == null
			    ? o.m_cause.getDbid() == null
			    : o.m_cause.getDbid() == null
			    ? false
			    : m_cause.getDbid().equals(o.m_cause.getDbid());
		}
		return false;
	}
	
	
	
	

}