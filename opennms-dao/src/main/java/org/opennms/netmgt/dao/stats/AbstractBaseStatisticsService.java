package org.opennms.netmgt.dao.stats;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.OnmsDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public abstract class AbstractBaseStatisticsService<T> implements StatisticsService<T>, InitializingBean {

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(getDao());
    }

    public abstract OnmsDao<T, Integer> getDao();

    @Transactional
	public int getTotalCount(final Criteria criteria) {
        return getDao().countMatching(criteria);
	}


}
