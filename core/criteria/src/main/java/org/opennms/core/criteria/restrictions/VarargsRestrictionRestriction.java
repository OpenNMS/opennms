package org.opennms.core.criteria.restrictions;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class VarargsRestrictionRestriction extends BaseRestriction {

	private Restriction[] m_restrictions;

	public VarargsRestrictionRestriction(final RestrictionType type, final Restriction... restrictions) {
		super(type);
		m_restrictions = restrictions;
	}

	public List<Restriction> getRestrictions() {
		return Arrays.asList(m_restrictions);
	}

    @Override
	public int hashCode() {
		return new HashCodeBuilder()
			.appendSuper(super.hashCode())
			.append(m_restrictions)
			.toHashCode();
	}

    @Override
    public boolean equals(final Object obj) {
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
            	return false;
            }
            final VarargsRestrictionRestriction that = (VarargsRestrictionRestriction) obj;
            return new EqualsBuilder()
            	.appendSuper(super.equals(obj))
            	.append(this.getRestrictions(), that.getRestrictions())
            	.isEquals();
    }

    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.appendSuper(super.toString())
    		.append("restrictions", m_restrictions)
    		.toString();
    }
}
