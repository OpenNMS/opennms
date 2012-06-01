package org.opennms.core.criteria.restrictions;

public class NotNullRestriction extends AttributeRestriction {

	public NotNullRestriction(final String attribute) {
		super(RestrictionType.NOTNULL, attribute);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitNotNull(this);
	}

	@Override
	public String toString() {
		return "NotNullRestriction [attribute=" + getAttribute() + "]";
	}

}
