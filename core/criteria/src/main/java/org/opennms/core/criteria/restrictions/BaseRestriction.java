package org.opennms.core.criteria.restrictions;

public abstract class BaseRestriction implements Restriction {
	private final RestrictionType m_type;
	
	public BaseRestriction(final RestrictionType type) {
		m_type = type;
	}
	
	public RestrictionType getType() {
		return m_type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof BaseRestriction)) return false;
		final BaseRestriction other = (BaseRestriction) obj;
		if (m_type != other.m_type) return false;
		return true;
	}

	@Override
	public String toString() {
		return "BaseRestriction [type=" + m_type + "]";
	}
}
