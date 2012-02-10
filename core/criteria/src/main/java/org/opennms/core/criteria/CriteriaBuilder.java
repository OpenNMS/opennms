package org.opennms.core.criteria;

import java.util.HashMap;
import java.util.Map;


public class CriteriaBuilder {
	private Class<?> m_class;
	private OrderBuilder m_orderBuilder = new OrderBuilder();
	private Map<String, Criteria.FetchType> m_fetch = new HashMap<String, Criteria.FetchType>();

	public CriteriaBuilder(final Class<?> clazz) {
		m_class = clazz;
	}

	public CriteriaBuilder fetch(final String attribute) {
		m_fetch.put(attribute, Criteria.FetchType.DEFAULT);
		return this;
	}

	public CriteriaBuilder fetch(final String attribute, final Criteria.FetchType type) {
		m_fetch.put(attribute, type);
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

	public Criteria toCriteria() {
		final Criteria criteria = new Criteria(m_class);
		criteria.setOrders(m_orderBuilder.getOrderCollection());
		criteria.setFetchTypes(m_fetch);
		return criteria;
	}

}
