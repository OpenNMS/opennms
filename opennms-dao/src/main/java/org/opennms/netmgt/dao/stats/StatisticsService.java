package org.opennms.netmgt.dao.stats;

import org.opennms.core.criteria.Criteria;

public interface StatisticsService<T> {

    int getTotalCount(final Criteria criteria);

}
