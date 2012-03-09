package org.opennms.core.criteria.restrictions;

public class LikeRestriction extends AttributeValueRestriction {

	public LikeRestriction(final String attribute, final Object value) {
		super(RestrictionType.LIKE, attribute, value);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitLike(this);
	}

	@Override
	public String toString() {
		return "LikeRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
	}
}
