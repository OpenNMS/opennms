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
package org.opennms.netmgt.alarmd.drools;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DefaultAlarmService implements AlarmService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAlarmService.class);

    protected static final String DEFAULT_USER = "admin";

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private AcknowledgmentDao acknowledgmentDao;

    @Autowired
    private AlarmEntityNotifier alarmEntityNotifier;

    @Autowired
    private EventForwarder eventForwarder;

    @Override
    @Transactional
    public void clearAlarm(OnmsAlarm alarm, Date now) {
        LOG.info("Clearing alarm with id: {} with current severity: {} at: {}", alarm.getId(), alarm.getSeverity(), now);
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping clear.", alarm);
            return;
        }
        final OnmsSeverity previousSeverity = alarmInTrans.getSeverity();
        alarmInTrans.setSeverity(OnmsSeverity.CLEARED);
        updateAutomationTime(alarmInTrans, now);
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
            return;
        }
        // If alarm was in Situation, calculate notifications for the Situation
        Map<OnmsAlarm, Set<OnmsAlarm>> priorRelatedAlarms = new HashMap<>();
        if (alarmInTrans.isPartOfSituation()) {
            for (OnmsAlarm situation : alarmInTrans.getRelatedSituations()) {
                priorRelatedAlarms.put(situation, new HashSet<OnmsAlarm>(situation.getRelatedAlarms()));
            }
        }
        alarmDao.delete(alarmInTrans);
        // fire notifications after alarm has been deleted
        for (Entry<OnmsAlarm, Set<OnmsAlarm>> entry : priorRelatedAlarms.entrySet()) {
            alarmEntityNotifier.didUpdateRelatedAlarms(entry.getKey(), entry.getValue());
        }
        alarmEntityNotifier.didDeleteAlarm(alarmInTrans);
    }

    @Override
    @Transactional
    public void unclearAlarm(OnmsAlarm alarm, Date now) {
        LOG.info("Un-clearing alarm with id: {} at: {}", alarm.getId(), now);
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping un-clear.", alarm);
            return;
        }
        final OnmsSeverity previousSeverity = alarmInTrans.getSeverity();
        alarmInTrans.setSeverity(OnmsSeverity.get(alarmInTrans.getLastEvent().getEventSeverity()));
        updateAutomationTime(alarmInTrans, now);
        alarmDao.update(alarmInTrans);
        alarmEntityNotifier.didUpdateAlarmSeverity(alarmInTrans, previousSeverity);
    }

    @Override
    @Transactional
    public void escalateAlarm(OnmsAlarm alarm, Date now) {
        LOG.info("Escalating alarm with id: {} at: {}", alarm.getId(), now);
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
        LOG.info("Acknowledging alarm with id: {} @ {}", alarm.getId(), now);
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping ack.", alarm);
            return;
        }
        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarmInTrans, DEFAULT_USER, now);
        ack.setAckAction(AckAction.ACKNOWLEDGE);
        acknowledgmentDao.processAck(ack);
    }

    @Override
    @Transactional
    public void unacknowledgeAlarm(OnmsAlarm alarm, Date now) {
        LOG.info("Un-Acknowledging alarm with id: {} @ {}", alarm.getId(), now);
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping un-ack.", alarm);
            return;
        }
        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarmInTrans, DEFAULT_USER, now);
        ack.setAckAction(AckAction.UNACKNOWLEDGE);
        acknowledgmentDao.processAck(ack);
    }

    @Override
    @Transactional
    public void setSeverity(OnmsAlarm alarm, OnmsSeverity severity, Date now) {
        LOG.info("Updating severity {} on alarm with id: {}", severity, alarm.getId());
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. Skipping severity update.", alarm);
            return;
        }
        final OnmsSeverity previousSeverity = alarmInTrans.getSeverity();
        alarmInTrans.setSeverity(severity);
        updateAutomationTime(alarm, now);
        alarmDao.update(alarmInTrans);
        alarmEntityNotifier.didUpdateAlarmSeverity(alarmInTrans, previousSeverity);
    }

    @Override
    public void sendEvent(Event e) {
        eventForwarder.sendNow(e);
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

    public void setAcknowledgmentDao(AcknowledgmentDao acknowledgmentDao) {
        this.acknowledgmentDao = acknowledgmentDao;
    }

    public void setAlarmEntityNotifier(AlarmEntityNotifier alarmEntityNotifier) {
        this.alarmEntityNotifier = alarmEntityNotifier;
    }

    public void debug(String message, Object... objects) {
        LOG.debug(message, objects);
    }

    public void info(String message, Object... objects) {
        LOG.info(message, objects);
    }

    public void warn(String message, Object... objects) {
        LOG.warn(message, objects);
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }
}
