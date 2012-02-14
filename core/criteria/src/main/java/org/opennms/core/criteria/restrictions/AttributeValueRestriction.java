package org.opennms.core.criteria.restrictions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


public class AttributeValueRestriction extends AttributeRestriction {
	private final Object m_value;

	public AttributeValueRestriction(final RestrictionType type, final String attribute, final Object value) {
		super(type, attribute);
		m_value = value;
	}

	public Object getValue() {
		return m_value;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.appendSuper(super.hashCode())
			.append(m_value)
			.toHashCode();
	}

    @Override
    public boolean equals(final Object obj) {
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
                    return false;
            }
            final AttributeValueRestriction that = (AttributeValueRestriction) obj;
            return new EqualsBuilder()
            	.appendSuper(super.equals(obj))
            	.append(this.getValue(), that.getValue())
            	.isEquals();
    }

    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.appendSuper(super.toString())
    		.append("value", getValue())
    		.toString();
    }

}
