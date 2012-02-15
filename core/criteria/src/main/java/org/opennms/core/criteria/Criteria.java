package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.criteria.restrictions.Restriction;

public class Criteria {

	public enum FetchType { DEFAULT, LAZY, EAGER }
	private Class<?> m_class;
	private List<Order> m_orders = new ArrayList<Order>();
	private List<Alias> m_aliases = new ArrayList<Alias>();
	private Map<String,FetchType> m_fetchTypes = new HashMap<String,FetchType>();
	private Set<Restriction> m_restrictions = new LinkedHashSet<Restriction>();
	private boolean m_distinct = false;
	private Integer m_limit = null;
	private Integer m_offset = null;

	public Criteria(final Class<?> clazz) {
		m_class = clazz;
	}

	public Class<?> getCriteriaClass() {
		return m_class;
	}
	
	public List<Order> getOrders() {
		return Collections.unmodifiableList(m_orders);
	}

	public void setOrders(final Collection<Order> orderCollection) {
		m_orders.clear();
		m_orders.addAll(orderCollection);
	}

	public Map<String,FetchType> getFetchTypes() {
		return Collections.unmodifiableMap(m_fetchTypes);
	}

	public void setFetchTypes(final Map<String, FetchType> types) {
		m_fetchTypes.clear();
		m_fetchTypes.putAll(types);
	}

	public List<Alias> getAliases() {
		return Collections.unmodifiableList(m_aliases);
	}

	public void setAliases(final Collection<Alias> aliases) {
		m_aliases.clear();
		m_aliases.addAll(aliases);
	}

	public Set<Restriction> getRestrictions() {
		return Collections.unmodifiableSet(m_restrictions);
	}

	public void setRestrictions(Collection<Restriction> restrictions) {
		m_restrictions.clear();
		m_restrictions.addAll(restrictions);
	}

	public boolean isDistinct() {
		return m_distinct ;
	}

	public void setDistinct(final boolean distinct) {
		m_distinct = distinct;
	}

	public Integer getLimit() {
		return m_limit;
	}

	public void setLimit(final Integer limit) {
		m_limit = limit;
	}
	
	public Integer getOffset() {
		return m_offset;
	}
	
	public void setOffset(final Integer offset) {
		m_offset = offset;
	}

}
