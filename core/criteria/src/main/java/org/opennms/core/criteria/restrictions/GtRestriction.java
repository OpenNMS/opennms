package org.opennms.core.criteria.restrictions;

public class GtRestriction extends AttributeValueRestriction {

	public GtRestriction(final String attribute, final Object value) {
		super(RestrictionType.GT, attribute, value);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitGt(this);
	}

	@Override
	public String toString() {
		return "GtRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
	}
}
