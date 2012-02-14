package org.opennms.core.criteria.restrictions;

public class AndRestriction extends VarargsRestrictionRestriction {

	public AndRestriction(final Restriction... restrictions) {
		super(RestrictionType.ALL);
	}


}
