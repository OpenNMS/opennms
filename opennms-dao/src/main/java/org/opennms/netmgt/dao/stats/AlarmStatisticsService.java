package org.opennms.netmgt.dao.stats;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.transaction.annotation.Transactional;

public interface AlarmStatisticsService extends StatisticsService<OnmsAlarm> {

	@Transactional(readOnly=true)
    public int getAcknowledgedCount(final Criteria criteria);

    @Transactional(readOnly=true)
    public OnmsAlarm getAcknowledged(final Criteria criteria);

    @Transactional(readOnly=true)
    public OnmsAlarm getUnacknowledged(final Criteria criteria);

}
