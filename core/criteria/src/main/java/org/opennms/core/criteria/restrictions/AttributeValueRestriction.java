package org.opennms.core.criteria.restrictions;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.netmgt.model.OnmsRestrictions;
import org.springframework.core.style.ToStringCreator;


public class AttributeValueRestriction extends AttributeRestriction {
	private final Object m_value;

	public AttributeValueRestriction(final RestrictionType type, final String attribute, final Object value) {
		super(type, attribute);
		m_value = value;
	}

	public Object getValue() {
		return m_value;
	}

	@SuppressWarnings("rawtypes")
	public org.hibernate.criterion.Criterion toCriterion() {
		switch(getType()) {
			case BETWEEN: {
				final Object o = getValue();
				if (o instanceof Object[]) {
					final Object[] entries = (Object[])o;
					if (entries.length != 2) {
						throw new UnsupportedOperationException("Criterion type is 'between', but value doesn't contain exactly 2 values: " + o);
					}
					return org.hibernate.criterion.Restrictions.between(getAttribute(), entries[0], entries[1]);
				} else {
					throw new UnsupportedOperationException("Criterion type is 'between' but restriction value is not an array of objects:" + o);
				}
			}
			case EQ: return org.hibernate.criterion.Restrictions.eq(getAttribute(), getValue());
			case GE: return org.hibernate.criterion.Restrictions.ge(getAttribute(), getValue());
			case GT: return org.hibernate.criterion.Restrictions.gt(getAttribute(), getValue());
			case ILIKE: return org.hibernate.criterion.Restrictions.ilike(getAttribute(), getValue());
            case IPLIKE: return OnmsRestrictions.ipLike((String)getValue());
			case IN: {
				final Object o = getValue();
				if (o instanceof List) {
					return org.hibernate.criterion.Restrictions.in(getAttribute(), ((List)getValue()).toArray());
				} else if (o instanceof Object[]) {
					return org.hibernate.criterion.Restrictions.in(getAttribute(), (Object[])getValue());
				} else {
					throw new UnsupportedOperationException("Criterion type is 'in' but value is not an array or list of objects.");
				}
			}
			case LE: return org.hibernate.criterion.Restrictions.le(getAttribute(), getValue());
			case LT: return org.hibernate.criterion.Restrictions.lt(getAttribute(), getValue());
			case LIKE: return org.hibernate.criterion.Restrictions.like(getAttribute(), getValue());
			default: return super.toCriterion();
		}
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
    	return new ToStringCreator(this)
    		.append("type", getType())
    		.append("attribute", getAttribute())
    		.append("value", getValue())
    		.toString();
    }

}
