package org.opennms.netmgt.dao.stats;

import org.opennms.netmgt.model.OnmsCriteria;

public interface StatisticsService<T> {

    int getTotalCount(final OnmsCriteria criteria);

}
