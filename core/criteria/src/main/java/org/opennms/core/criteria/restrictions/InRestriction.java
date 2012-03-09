package org.opennms.core.criteria.restrictions;

import java.util.List;

public class InRestriction extends AttributeValueRestriction {

	public InRestriction(final String attribute, final List<?> value) {
		super(RestrictionType.IN, attribute, value);
	}

	public List<?> getValues() {
		return (List<?>)this.getValue();
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitIn(this);
	}

	@Override
	public String toString() {
		return "InRestriction [attribute=" + getAttribute() + ", values=" + getValues() + "]";
	}
}
