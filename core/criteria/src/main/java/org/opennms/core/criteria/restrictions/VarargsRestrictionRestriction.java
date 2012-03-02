package org.opennms.core.criteria.restrictions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.criterion.Criterion;
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
		final List<Restriction> restrictions = new ArrayList<Restriction>(getRestrictions());
		if (restrictions.size() < 2) {
			throw new UnsupportedOperationException("Restriction type is vararg (" + getType().name().toLowerCase() + "), but there aren't enough arguments: " + restrictions);
		}
		Criterion lhs = restrictions.remove(restrictions.size() - 1).toCriterion();
		while (restrictions.size() > 2) {
			final Criterion rhs = restrictions.remove(restrictions.size() - 1).toCriterion();
			lhs = getCriterion(lhs, rhs);
		}
		return getCriterion(lhs, restrictions.remove(0).toCriterion());
	}

	protected abstract Criterion getCriterion(final Criterion lhs, final Criterion rhs);
	
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
