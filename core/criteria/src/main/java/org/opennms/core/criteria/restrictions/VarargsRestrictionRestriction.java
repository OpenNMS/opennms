package org.opennms.core.criteria.restrictions;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.springframework.core.style.ToStringCreator;

public abstract class VarargsRestrictionRestriction extends BaseRestriction {

	private Restriction[] m_restrictions;

	public VarargsRestrictionRestriction(final RestrictionType type, final Restriction... restrictions) {
		super(type);
		m_restrictions = restrictions;
	}

	public List<Restriction> getRestrictions() {
		return Arrays.asList(m_restrictions);
	}

	@Override
	public Criterion toCriterion() {
	    final Junction junction = getJunction();
	    
	    for (final Restriction restriction : getRestrictions()) {
	        junction.add(restriction.toCriterion());
	    }
	    return junction;
	}

	protected abstract Junction getJunction();
	
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
    	return new ToStringCreator(this)
    		.append("type", getType())
    		.append("restrictions", m_restrictions)
    		.toString();
    }
}
