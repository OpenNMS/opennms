/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.alarmd.drools;

import java.util.Date;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DefaultAlarmService implements AlarmService {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmService.class);

    protected static final String DEFAULT_USER = "admin";

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private AlarmEntityNotifier alarmEntityNotifier;

    @Override
    @Transactional
    public void clearAlarm(OnmsAlarm alarm, Date clearTime) {
        LOG.info("Clearing alarm with id: {} with current severity: {} at: {}", alarm.getId(), alarm.getSeverity(), clearTime);
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping clear.", alarm);
            return;
        }
        final OnmsSeverity previousSeverity = alarmInTrans.getSeverity();
        alarmInTrans.setSeverity(OnmsSeverity.CLEARED);
        updateAutomationTime(alarmInTrans, clearTime);
        alarmDao.update(alarmInTrans);
        alarmEntityNotifier.didUpdateAlarmSeverity(alarmInTrans, previousSeverity);
    }

    @Override
    @Transactional
    public void deleteAlarm(OnmsAlarm alarm) {
        LOG.info("Deleting alarm with id: {} with severity: {}", alarm.getId(), alarm.getSeverity());
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping delete.", alarm);
        }
        alarmDao.delete(alarmInTrans);
        alarmEntityNotifier.didDeleteAlarm(alarmInTrans);
    }

    @Override
    @Transactional
    public void unclearAlarm(OnmsAlarm alarm) {
        LOG.info("Un-clearing alarm with id: {} at: {}", alarm.getId(), alarm.getLastEventTime());
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping clear.", alarm);
            return;
        }
        final OnmsSeverity previousSeverity = alarmInTrans.getSeverity();
        alarmInTrans.setSeverity(OnmsSeverity.get(alarmInTrans.getLastEvent().getEventSeverity()));
        alarmDao.update(alarmInTrans);
        alarmEntityNotifier.didUpdateAlarmSeverity(alarmInTrans, previousSeverity);
    }

    @Override
    @Transactional
    public void escalateAlarm(OnmsAlarm alarm, Date now) {
        LOG.info("Escalating alarm with id: {}", alarm.getId());
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping escalate.", alarm);
            return;
        }
        final OnmsSeverity previousSeverity = alarmInTrans.getSeverity();
        alarmInTrans.setSeverity(OnmsSeverity.get(previousSeverity.getId() + 1));
        updateAutomationTime(alarmInTrans, now);
        alarmDao.update(alarmInTrans);
        alarmEntityNotifier.didUpdateAlarmSeverity(alarmInTrans, previousSeverity);
    }

    @Override
    @Transactional
    public void acknowledgeAlarm(OnmsAlarm alarm, Date now) {
        LOG.info("Acknowledging alarm with id: {}", alarm.getId());
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping ack.", alarm);
            return;
        }
        final String previousAckUser = alarmInTrans.getAlarmAckUser();
        final Date previousAckTime = alarmInTrans.getAlarmAckTime();
        alarmInTrans.setAlarmAckUser(DEFAULT_USER);
        alarmInTrans.setAlarmAckTime(now);
        alarmDao.update(alarmInTrans);
        alarmEntityNotifier.didAcknowledgeAlarm(alarmInTrans, previousAckUser, previousAckTime);
    }

    private static void updateAutomationTime(OnmsAlarm alarm, Date now) {
        if (alarm.getFirstAutomationTime() == null) {
            alarm.setFirstAutomationTime(now);
        }
        alarm.setLastAutomationTime(now);
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    public void setAlarmEntityNotifier(AlarmEntityNotifier alarmEntityNotifier) {
        this.alarmEntityNotifier = alarmEntityNotifier;
    }
}
