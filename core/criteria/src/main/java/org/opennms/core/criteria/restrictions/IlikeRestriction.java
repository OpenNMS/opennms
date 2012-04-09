package org.opennms.core.criteria.restrictions;

public class IlikeRestriction extends AttributeValueRestriction {

	public IlikeRestriction(final String attribute, final Object value) {
		super(RestrictionType.ILIKE, attribute, value);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitIlike(this);
	}

	@Override
	public String toString() {
		return "IlikeRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
	}
}
