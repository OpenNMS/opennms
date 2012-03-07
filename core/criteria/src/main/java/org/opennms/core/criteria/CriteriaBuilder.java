package org.opennms.core.criteria;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.utils.LogUtils;


public class CriteriaBuilder {
	private Class<?> m_class;
	private OrderBuilder m_orderBuilder = new OrderBuilder();
	private Map<String, Criteria.FetchType> m_fetch = new HashMap<String, Criteria.FetchType>();
	private AliasBuilder m_aliasBuilder = new AliasBuilder();
	private boolean m_distinct = false;
	private Set<Restriction> m_restrictions = new LinkedHashSet<Restriction>();
	private boolean m_negateNext = false;
	private Integer m_limit = null;
	private Integer m_offset = null;
	private String m_matchType = "all";

	public CriteriaBuilder(final Class<?> clazz) {
		m_class = clazz;
	}

	public Criteria toCriteria() {
		final Criteria criteria = new Criteria(m_class);
		criteria.setMatchType(m_matchType);
		criteria.setOrders(m_orderBuilder.getOrderCollection());
		criteria.setAliases(m_aliasBuilder.getAliasCollection());
		criteria.setFetchTypes(m_fetch);
		criteria.setRestrictions(m_restrictions);
		criteria.setDistinct(m_distinct);
		criteria.setLimit(m_limit);
		criteria.setOffset(m_offset);
		return criteria;
	}

	public CriteriaBuilder match(final String type) {
		if ("all".equals(type) || "any".equals(type)) {
			m_matchType  = type;
		} else {
			throw new IllegalArgumentException("match type must be 'all' or 'any'");
		}
		return this;
	}

	public CriteriaBuilder fetch(final String attribute) {
		m_fetch.put(attribute, Criteria.FetchType.DEFAULT);
		return this;
	}

	public CriteriaBuilder fetch(final String attribute, final Criteria.FetchType type) {
		m_fetch.put(attribute, type);
		return this;
	}

	public CriteriaBuilder join(final String associationPath, final String alias) {
		return alias(associationPath, alias, JoinType.LEFT_JOIN);
	}

	public CriteriaBuilder alias(final String associationPath, final String alias) {
		return alias(associationPath, alias, JoinType.LEFT_JOIN);
	}

	public CriteriaBuilder join(final String associationPath, final String alias, final JoinType type) {
		return alias(associationPath, alias, type);
	}

	public CriteriaBuilder alias(final String associationPath, final String alias, final JoinType type) {
		m_aliasBuilder.alias(associationPath, alias, type);
		return this;
	}
	public CriteriaBuilder limit(final Integer limit) {
		m_limit = ((limit == null || limit == 0)? null : limit);
		return this;
	}
	
	public CriteriaBuilder offset(final Integer offset) {
		m_offset = ((offset == null || offset == 0)? null : offset);
		return this;
	}

	public CriteriaBuilder clearOrder() {
		m_orderBuilder.clear();
		return this;
	}

	public CriteriaBuilder orderBy(final String attribute) {
		return orderBy(attribute, true);
	}

	public CriteriaBuilder orderBy(final String attribute, final boolean ascending) {
		m_orderBuilder.append(new Order(attribute, ascending));
		return this;
	}

	public CriteriaBuilder asc() {
		m_orderBuilder.asc();
		return this;
	}

	public CriteriaBuilder desc() {
		m_orderBuilder.desc();
		return this;
	}

	public CriteriaBuilder distinct() {
		m_distinct = true;
		return this;
	}

	public CriteriaBuilder count() {
	    m_orderBuilder.clear();
	    m_limit = null;
	    m_offset = null;
	    return this;
	}
	
	public CriteriaBuilder distinct(final boolean isDistinct) {
		m_distinct = isDistinct;
		return this;
	}

	private boolean addRestriction(final Restriction restriction) {
		if (m_negateNext) {
			m_negateNext = false;
			return m_restrictions.add(Restrictions.not(restriction));
		} else {
			return m_restrictions.add(restriction);
		}
	}

	public CriteriaBuilder isNull(final String attribute) {
		addRestriction(Restrictions.isNull(attribute));
		return this;
	}

	public CriteriaBuilder isNotNull(final String attribute) {
		addRestriction(Restrictions.isNotNull(attribute));
		return this;
	}

	public CriteriaBuilder id(final Integer id) {
		addRestriction(Restrictions.id(id));
		return this;
	}

	public CriteriaBuilder eq(final String attribute, final Object comparator) {
		addRestriction(Restrictions.eq(attribute, comparator));
		return this;
	}

	public CriteriaBuilder ne(final String attribute, final Object comparator) {
		if (m_negateNext) {
			m_negateNext = false;
			addRestriction(Restrictions.eq(attribute, comparator));
		} else {
			addRestriction(Restrictions.not(Restrictions.eq(attribute, comparator)));
		}
		return this;
	}

	public CriteriaBuilder gt(final String attribute, final Object comparator) {
		addRestriction(Restrictions.gt(attribute, comparator));
		return this;
	}

	public CriteriaBuilder ge(final String attribute, final Object comparator) {
		addRestriction(Restrictions.ge(attribute, comparator));
		return this;
	}

	public CriteriaBuilder lt(final String attribute, final Object comparator) {
		addRestriction(Restrictions.lt(attribute, comparator));
		return this;
	}

	public CriteriaBuilder le(final String attribute, final Object comparator) {
		addRestriction(Restrictions.le(attribute, comparator));
		return this;
	}

	public CriteriaBuilder like(final String attribute, final Object comparator) {
		addRestriction(Restrictions.like(attribute, comparator));
		return this;
	}

    public CriteriaBuilder ilike(final String attribute, final Object comparator) {
        addRestriction(Restrictions.ilike(attribute, comparator));
        return this;
    }

    public CriteriaBuilder iplike(final String attribute, final Object comparator) {
        addRestriction(Restrictions.iplike(attribute, comparator));
        return this;
    }

	public CriteriaBuilder contains(final String attribute, final Object comparator) {
		addRestriction(Restrictions.ilike(attribute, "%" + comparator + "%"));
		return this;
	}

	public CriteriaBuilder in(final String attribute, final List<?> list) {
		addRestriction(Restrictions.in(attribute, list));
		return this;
	}

	public CriteriaBuilder between(final String attribute, final Object begin, final Object end) {
		addRestriction(Restrictions.between(attribute, begin, end));
		return this;
	}

	public CriteriaBuilder sql(final Object sql) {
		if (sql instanceof String) {
			addRestriction(Restrictions.sql((String)sql));
		} else {
			LogUtils.warnf(this, "sql(): " + sql.getClass().getName() + " is not a string type, can't add");
		}
		return this;
	}

	public CriteriaBuilder not() {
		m_negateNext = true;
		return this;
	}

	public CriteriaBuilder and(final Restriction lhs, final Restriction rhs) {
		addRestriction(Restrictions.and(lhs, rhs));
		return this;
	}

	public CriteriaBuilder or(final Restriction lhs, final Restriction rhs) {
		final Restriction restriction = Restrictions.or(lhs, rhs);
		addRestriction(restriction);
		return this;
	}

}
