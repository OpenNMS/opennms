package org.opennms.netmgt.dao.stats;

import org.opennms.core.criteria.Criteria;
import org.springframework.transaction.annotation.Transactional;

public interface StatisticsService<T> {
	
	@Transactional(readOnly=true)
    int getTotalCount(final Criteria criteria);

}
