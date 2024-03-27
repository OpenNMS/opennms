/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.stats;

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmStatisticsService;
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
