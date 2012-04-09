package org.opennms.core.criteria.restrictions;


public class GeRestriction extends AttributeValueRestriction {

	public GeRestriction(final String attribute, final Object value) {
		super(RestrictionType.GE, attribute, value);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitGe(this);
	}

	@Override
	public String toString() {
		return "GeRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
	}

}
