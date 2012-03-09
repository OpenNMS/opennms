package org.opennms.core.criteria.restrictions;

public class LtRestriction extends AttributeValueRestriction {

	public LtRestriction(final String attribute, final Object value) {
		super(RestrictionType.LT, attribute, value);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitLt(this);
	}

	@Override
	public String toString() {
		return "LtRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
	}
}
