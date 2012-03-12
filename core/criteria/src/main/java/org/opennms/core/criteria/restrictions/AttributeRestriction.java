package org.opennms.core.criteria.restrictions;

public abstract class AttributeRestriction extends BaseRestriction {
	private final String m_attribute;
	
	public AttributeRestriction(final RestrictionType type, final String attribute) {
		super(type);
		m_attribute = attribute.intern();
	}
	
	public String getAttribute() {
		return m_attribute;
	}

    protected static String lower(final String string) {
    	return string == null? null : string.toLowerCase();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((m_attribute == null) ? 0 : m_attribute.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (!(obj instanceof AttributeRestriction)) return false;
		final AttributeRestriction other = (AttributeRestriction) obj;
		if (m_attribute == null) {
			if (other.m_attribute != null) return false;
		} else if (!m_attribute.equals(other.m_attribute)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "AttributeRestriction [type=" + getType() + ", attribute=" + m_attribute + "]";
	}
}
