package org.opennms.netmgt.dao.stats;

import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.model.OnmsCriteria;
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
    public int getTotalCount(final OnmsCriteria criteria) {
        return getDao().countMatching(criteria);
    }
}
