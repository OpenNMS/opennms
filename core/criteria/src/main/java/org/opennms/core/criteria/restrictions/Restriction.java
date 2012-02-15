package org.opennms.core.criteria.restrictions;


public interface Restriction {
	public static enum RestrictionType { NULL, NOTNULL, EQ, GT, GE, LT, LE, ALL, ANY, LIKE, ILIKE, IN, NOT, BETWEEN }

	public RestrictionType getType();
	public org.hibernate.criterion.Criterion toCriterion();
	
}
