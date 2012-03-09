package org.opennms.core.criteria.restrictions;


public class NullRestriction extends AttributeRestriction {

	public NullRestriction(final String attribute) {
		super(RestrictionType.NULL, attribute);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitNull(this);
	}

	@Override
	public String toString() {
		return "NullRestriction [attribute=" + getAttribute() + "]";
	}

}
