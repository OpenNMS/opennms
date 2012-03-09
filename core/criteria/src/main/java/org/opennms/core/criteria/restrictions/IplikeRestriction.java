package org.opennms.core.criteria.restrictions;

public class IplikeRestriction extends AttributeValueRestriction {

	public IplikeRestriction(final String attribute, final Object value) {
		super(RestrictionType.IPLIKE, attribute, value);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitIplike(this);
	}

	@Override
	public String toString() {
		return "IplikeRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
	}
}
