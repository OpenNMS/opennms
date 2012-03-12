package org.opennms.core.criteria.restrictions;

public class AnyRestriction extends VarargsRestrictionRestriction {

	public AnyRestriction(final Restriction... restrictions) {
		super(RestrictionType.ANY, restrictions);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitAny(this);
		for (final Restriction restriction : getRestrictions()) {
			restriction.visit(visitor);
		}
		visitor.visitAnyComplete(this);
	}

	@Override
	public String toString() {
		return "AnyRestriction [restrictions=" + getRestrictions() + "]";
	}

}
