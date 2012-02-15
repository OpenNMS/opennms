package org.opennms.core.criteria.restrictions;

import java.util.List;

import org.opennms.core.criteria.restrictions.Restriction.RestrictionType;

public class Restrictions {

	public static AttributeRestriction isNull(final String attribute) {
		return new AttributeRestriction(RestrictionType.NULL, attribute);
	}
	
	public static Restriction isNotNull(final String attribute) {
		return new AttributeRestriction(RestrictionType.NOTNULL, attribute);
	}

	public static Restriction id(final Integer id) {
		return eq("id", id);
	}

	public static AttributeValueRestriction eq(final String attribute, final Object comparator) {
		return new AttributeValueRestriction(RestrictionType.EQ, attribute, comparator);
	}

	public static AttributeValueRestriction gt(final String attribute, final Object comparator) {
		return new AttributeValueRestriction(RestrictionType.GT, attribute, comparator);
	}


	public static AttributeValueRestriction ge(final String attribute, final Object comparator) {
		return new AttributeValueRestriction(RestrictionType.GE, attribute, comparator);
	}

	public static AttributeValueRestriction lt(final String attribute, final Object comparator) {
		return new AttributeValueRestriction(RestrictionType.LT, attribute, comparator);
	}

	public static AttributeValueRestriction le(final String attribute, final Object comparator) {
		return new AttributeValueRestriction(RestrictionType.LE, attribute, comparator);
	}

	public static AttributeValueRestriction like(final String attribute, final Object comparator) {
		return new AttributeValueRestriction(RestrictionType.LIKE, attribute, comparator);
	}

	public static AttributeValueRestriction ilike(final String attribute, final Object comparator) {
		return new AttributeValueRestriction(RestrictionType.ILIKE, attribute, comparator);
	}

	public static Restriction in(final String attribute, final List<?> list) {
		return new AttributeValueRestriction(RestrictionType.IN, attribute, list);
	}

	public static Restriction between(final String attribute, final Object begin, final Object end) {
		return new AttributeValueRestriction(RestrictionType.BETWEEN, attribute, new Object[] { begin, end });
	}

	public static Restriction not(final Restriction restriction) {
		return new NotRestriction(restriction);
	}

	public static Restriction and(final Restriction lhs, final Restriction rhs) {
		return new AndRestriction(lhs, rhs);
	}

	public static Restriction or(final Restriction lhs, final Restriction rhs) {
		return new OrRestriction(lhs, rhs);
	}

}
