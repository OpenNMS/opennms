package org.opennms.core.criteria.restrictions;

import org.hibernate.criterion.Criterion;

public class OrRestriction extends VarargsRestrictionRestriction {

	public OrRestriction(final Restriction... restrictions) {
		super(RestrictionType.ANY, restrictions);
	}

	protected Criterion getCriterion(final Criterion lhs, final Criterion rhs) {
		return org.hibernate.criterion.Restrictions.or(lhs, rhs);
	}
}
