package org.opennms.netmgt.dao;

import org.opennms.core.criteria.Criteria;

public interface CriteriaConverter<T> {
	public T convert(Criteria criteria);
}
