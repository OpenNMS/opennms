package org.opennms.core.criteria.restrictions;

public class AllRestriction extends VarargsRestrictionRestriction {

	public AllRestriction(final Restriction... restrictions) {
		super(RestrictionType.ALL, restrictions);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitAll(this);
		for (final Restriction restriction : getRestrictions()) {
			restriction.visit(visitor);
		}
		visitor.visitAllComplete(this);
	}

	@Override
	public String toString() {
		return "AllRestriction [restrictions=" + getRestrictions() + "]";
	}
}
