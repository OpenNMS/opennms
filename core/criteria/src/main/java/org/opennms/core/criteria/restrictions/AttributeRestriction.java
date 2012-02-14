package org.opennms.core.criteria.restrictions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class AttributeRestriction extends BaseRestriction {
	private final String m_attribute;
	
	public AttributeRestriction(final RestrictionType type, final String attribute) {
		super(type);
		m_attribute = attribute.intern();
	}
	
	public String getAttribute() {
		return m_attribute;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.appendSuper(super.hashCode())
			.append(lower(m_attribute))
			.toHashCode();
	}

    @Override
    public boolean equals(final Object obj) {
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
                    return false;
            }
            final AttributeRestriction that = (AttributeRestriction) obj;
            return new EqualsBuilder()
            	.appendSuper(super.equals(obj))
            	.append(lower(this.getAttribute()), lower(that.getAttribute()))
            	.isEquals();
    }

    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.appendSuper(super.toString())
    		.append("attribute", getAttribute())
    		.toString();
    }
}
