package org.opennms.core.criteria.restrictions;

import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

public class AndRestriction extends VarargsRestrictionRestriction {

	public AndRestriction(final Restriction... restrictions) {
		super(RestrictionType.ALL, restrictions);
	}

    @Override
    protected Junction getJunction() {
        return Restrictions.conjunction();
    }

}
