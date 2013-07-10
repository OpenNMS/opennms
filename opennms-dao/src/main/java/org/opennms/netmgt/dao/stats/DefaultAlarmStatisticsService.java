/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.stats;

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DefaultAlarmStatisticsService extends AbstractBaseStatisticsService<OnmsAlarm> implements AlarmStatisticsService {

    @Autowired AlarmDao m_alarmDao;

    @Override
    public AlarmDao getDao() {
        return m_alarmDao;
    }

    @Transactional
    @Override
    public int getAcknowledgedCount(final Criteria criteria) {
    	criteria.addRestriction(org.opennms.core.criteria.restrictions.Restrictions.isNotNull("alarmAckUser"));
        return m_alarmDao.countMatching(criteria);
    }

    @Transactional
    @Override
    public OnmsAlarm getAcknowledged(final Criteria criteria) {
        criteria.addRestriction(org.opennms.core.criteria.restrictions.Restrictions.isNotNull("alarmAckUser"));
        criteria.setLimit(1);
        final List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);
        if (alarms.size() == 0) return null;
        return alarms.get(0);
    }

    @Transactional
    @Override
    public OnmsAlarm getUnacknowledged(final Criteria criteria) {
        criteria.addRestriction(org.opennms.core.criteria.restrictions.Restrictions.isNull("alarmAckUser"));
        criteria.setLimit(1);
        final List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);
        if (alarms.size() == 0) return null;
        return alarms.get(0);
    }

}
