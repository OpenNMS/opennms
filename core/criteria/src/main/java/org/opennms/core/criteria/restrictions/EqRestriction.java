package org.opennms.core.criteria.restrictions;

public class EqRestriction extends AttributeValueRestriction {

	public EqRestriction(final String attribute, final Object value) {
		super(RestrictionType.EQ, attribute, value);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitEq(this);
	}

	@Override
	public String toString() {
		return "EqRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
	}
}
