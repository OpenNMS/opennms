package org.opennms.core.criteria.restrictions;

public class SqlRestriction extends AttributeRestriction {

	public SqlRestriction(final String attribute) {
		super(RestrictionType.SQL, attribute);
	}

	@Override
	public void visit(final RestrictionVisitor visitor) {
		visitor.visitSql(this);
	}

	@Override
	public String toString() {
		return "SqlRestriction [attribute=" + getAttribute() + "]";
	}

}
