package org.opennms.core.criteria.restrictions;

import org.hibernate.criterion.Criterion;

public class AndRestriction extends VarargsRestrictionRestriction {

	public AndRestriction(final Restriction... restrictions) {
		super(RestrictionType.ALL);
	}

	protected Criterion getCriterion(final Criterion lhs, final Criterion rhs) {
		return org.hibernate.criterion.Restrictions.and(lhs, rhs);
	}

}
