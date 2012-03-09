package org.opennms.core.criteria.restrictions;

public abstract class AttributeValueRestriction extends AttributeRestriction {
	protected final Object m_value;

	public AttributeValueRestriction(final RestrictionType type, final String attribute, final Object value) {
		super(type, attribute);
		m_value = value;
	}

	public Object getValue() {
		return m_value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof AttributeValueRestriction)) return false;
		final AttributeValueRestriction other = (AttributeValueRestriction) obj;
		if (m_value == null) {
			if (other.m_value != null) return false;
		} else if (!m_value.equals(other.m_value)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AttributeValueRestriction [type=" + getType() + ", attribute=" + getAttribute() + ", value=" + m_value + "]";
	}

}
