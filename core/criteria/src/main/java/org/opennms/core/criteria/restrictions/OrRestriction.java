package org.opennms.core.criteria.restrictions;

import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

public class OrRestriction extends VarargsRestrictionRestriction {

	public OrRestriction(final Restriction... restrictions) {
		super(RestrictionType.ANY, restrictions);
	}

    @Override
    protected Junction getJunction() {
        return Restrictions.disjunction();
    }

}
