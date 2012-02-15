package org.opennms.core.criteria.restrictions;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class BaseRestriction implements Restriction {
	private final RestrictionType m_type;
	
	public BaseRestriction(final RestrictionType type) {
		m_type = type;
	}
	
	public RestrictionType getType() {
		return m_type;
	}
	
    protected static String lower(final String string) {
    	return string == null? null : string.toLowerCase();
    }

    @Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(m_type)
			.toHashCode();
	}

    @Override
    public boolean equals(final Object obj) {
    	if (obj == null) { return false; }
    	if (obj == this) { return true; }
    	if (obj.getClass() != getClass()) {
    		return false;
    	}
    	final BaseRestriction that = (BaseRestriction) obj;
    	return new EqualsBuilder()
    		.append(this.getType(), that.getType())
    		.isEquals();
    }

    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("type", getType())
    		.toString();
    }
}
