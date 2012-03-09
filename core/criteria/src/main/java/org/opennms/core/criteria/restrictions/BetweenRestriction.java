package org.opennms.core.criteria.restrictions;

import java.util.Arrays;
import java.util.List;


public class BetweenRestriction extends AttributeValueRestriction {

	public BetweenRestriction(final String attribute, final Object begin, Object end) {
		super(RestrictionType.BETWEEN, attribute, Arrays.asList(new Object[] { begin, end }));
	}

	public Object getBegin() {
		@SuppressWarnings("unchecked")
		final List<Object> value = (List<Object>)getValue();
		return value.get(0);
	}

	public Object getEnd() {
		@SuppressWarnings("unchecked")
		final List<Object> value = (List<Object>)getValue();
		return value.get(1);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitBetween(this);
	}

	@Override
	public String toString() {
		return "BetweenRestriction [attribute=" + getAttribute() + ", begin=" + getBegin() + ", end=" + getEnd() + "]";
	}
}
