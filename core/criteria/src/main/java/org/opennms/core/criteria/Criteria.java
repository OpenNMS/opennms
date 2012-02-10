package org.opennms.core.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Criteria {

	public enum FetchType { DEFAULT, LAZY, EAGER }

	private Class<?> m_class;
	private Collection<Order> m_orders = new ArrayList<Order>();
	private Map<String,FetchType> m_fetchTypes = new HashMap<String,FetchType>();

	public Criteria(final Class<?> clazz) {
		m_class = clazz;
	}

	public Class<?> getCriteriaClass() {
		return m_class;
	}
	
	public Collection<Order> getOrders() {
		return m_orders;
	}

	public void setOrders(final Collection<Order> orderCollection) {
		m_orders.clear();
		m_orders.addAll(orderCollection);
	}

	public Map<String,FetchType> getFetchTypes() {
		return m_fetchTypes;
	}

	public void setFetchTypes(final Map<String, FetchType> types) {
		m_fetchTypes.clear();
		m_fetchTypes.putAll(types);
	}

}
