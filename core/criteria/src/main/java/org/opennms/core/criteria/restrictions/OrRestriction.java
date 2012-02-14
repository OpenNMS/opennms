package org.opennms.core.criteria.restrictions;

public class OrRestriction extends VarargsRestrictionRestriction {

	public OrRestriction(final Restriction... restrictions) {
		super(RestrictionType.ANY, restrictions);
	}

}
