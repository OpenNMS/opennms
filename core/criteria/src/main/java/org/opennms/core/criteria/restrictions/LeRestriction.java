package org.opennms.core.criteria.restrictions;

public class LeRestriction extends AttributeValueRestriction {

	public LeRestriction(final String attribute, final Object value) {
		super(RestrictionType.LE, attribute, value);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitLe(this);
	}

	@Override
	public String toString() {
		return "LeRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
	}
}
