package org.opennms.netmgt.dao.stats;

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.OnmsDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AlarmStatisticsService extends AbstractBaseStatisticsService<OnmsAlarm> {

    @Autowired AlarmDao m_alarmDao;

    @Override
    public OnmsDao<OnmsAlarm, Integer> getDao() {
        return m_alarmDao;
    }

    @Transactional
    public int getAcknowledgedCount(final Criteria criteria) {
    	criteria.addRestriction(org.opennms.core.criteria.restrictions.Restrictions.isNotNull("alarmAckUser"));
        return m_alarmDao.countMatching(criteria);
    }

    @Transactional
    public OnmsAlarm getAcknowledged(final Criteria criteria) {
        criteria.addRestriction(org.opennms.core.criteria.restrictions.Restrictions.isNotNull("alarmAckUser"));
        criteria.setLimit(1);
        final List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);
        if (alarms.size() == 0) return null;
        return alarms.get(0);
    }

    @Transactional
    public OnmsAlarm getUnacknowledged(final Criteria criteria) {
        criteria.addRestriction(org.opennms.core.criteria.restrictions.Restrictions.isNull("alarmAckUser"));
        criteria.setLimit(1);
        final List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);
        if (alarms.size() == 0) return null;
        return alarms.get(0);
    }

}
