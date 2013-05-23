/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.MemoDao;
import org.opennms.netmgt.dao.AlarmRepository;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsReductionKeyMemo;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>DaoWebAlarmRepository class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AlarmRepositoryHibernate implements AlarmRepository, InitializingBean {

    @Autowired
    AlarmDao m_alarmDao;

    @Autowired
    MemoDao m_memoDao;
    
    @Autowired
    AcknowledgmentDao m_ackDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void acknowledgeAll(String user, Date timestamp) {
        acknowledgeMatchingAlarms(user, timestamp, new OnmsCriteria(OnmsAlarm.class));
    }

    @Transactional
    public void acknowledgeAlarms(String user, Date timestamp, int[] alarmIds) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
        criteria.add(Restrictions.in("id", Arrays.asList(ArrayUtils.toObject(alarmIds))));
        acknowledgeMatchingAlarms(user, timestamp, criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void acknowledgeMatchingAlarms(String user, Date timestamp, OnmsCriteria criteria) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);

        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while (alarmsIt.hasNext()) {
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.ACKNOWLEDGE);
            m_ackDao.processAck(ack);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void clearAlarms(int[] alarmIds, String user, Date timestamp) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
        criteria.add(Restrictions.in("id", Arrays.asList(ArrayUtils.toObject(alarmIds))));
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);

        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while (alarmsIt.hasNext()) {
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.CLEAR);
            m_ackDao.processAck(ack);
            m_alarmDao.update(alarm);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public int countMatchingAlarms(OnmsCriteria criteria) {
        return m_alarmDao.countMatching(criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public int[] countMatchingAlarmsBySeverity(final OnmsCriteria criteria) {
        final int[] alarmCounts = new int[8];
        for (final OnmsSeverity value : OnmsSeverity.values()) {
            alarmCounts[value.getId()] = m_alarmDao.countMatching(criteria.doClone().add(Restrictions.eq("severity", value)));
        }
        return alarmCounts;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void escalateAlarms(int[] alarmIds, String user, Date timestamp) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
        criteria.add(Restrictions.in("id", Arrays.asList(ArrayUtils.toObject(alarmIds))));
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);

        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while (alarmsIt.hasNext()) {
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.ESCALATE);
            m_ackDao.processAck(ack);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public OnmsAlarm getAlarm(int alarmId) {
        return m_alarmDao.get(alarmId);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public OnmsAlarm[] getMatchingAlarms(OnmsCriteria criteria) {
        return m_alarmDao.findMatching(criteria).toArray(new OnmsAlarm[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void unacknowledgeAll(String user) {
        unacknowledgeMatchingAlarms(new OnmsCriteria(OnmsAlarm.class), user);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void unacknowledgeMatchingAlarms(OnmsCriteria criteria, String user) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);

        for (OnmsAlarm alarm : alarms) {
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckAction(AckAction.UNACKNOWLEDGE);
            m_ackDao.processAck(ack);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void acknowledgeAlarms(int[] alarmIds, String user, Date timestamp) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
        criteria.add(Restrictions.in("id", Arrays.asList(ArrayUtils.toObject(alarmIds))));
        acknowledgeMatchingAlarms(user, timestamp, criteria);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void unacknowledgeAlarms(int[] alarmIds, String user) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
        criteria.add(Restrictions.in("id", Arrays.asList(ArrayUtils.toObject(alarmIds))));
        unacknowledgeMatchingAlarms(criteria, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateStickyMemo(Integer alarmId, String body, String user) {
        OnmsAlarm onmsAlarm = m_alarmDao.get(alarmId);
        if (onmsAlarm != null) {
            if (onmsAlarm.getStickyMemo() == null) {
                onmsAlarm.setStickyMemo(new OnmsMemo());
                onmsAlarm.getStickyMemo().setCreated(new Date());
            }
            onmsAlarm.getStickyMemo().setBody(body);
            onmsAlarm.getStickyMemo().setAuthor(user);
            onmsAlarm.getStickyMemo().setUpdated(new Date());
            m_alarmDao.saveOrUpdate(onmsAlarm);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateReductionKeyMemo(Integer alarmId, String body, String user) {
        OnmsAlarm onmsAlarm = m_alarmDao.get(alarmId);
        if (onmsAlarm != null) {
            OnmsReductionKeyMemo memo = onmsAlarm.getReductionKeyMemo();
            if (memo == null) {
                memo = new OnmsReductionKeyMemo();
                memo.setCreated(new Date());
            }
            memo.setBody(body);
            memo.setAuthor(user);
            memo.setReductionKey(onmsAlarm.getReductionKey());
            memo.setUpdated(new Date());
            m_memoDao.saveOrUpdate(memo);
            onmsAlarm.setReductionKeyMemo(memo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void removeStickyMemo(Integer alarmId) {
        OnmsAlarm onmsAlarm = m_alarmDao.get(alarmId);
        if (onmsAlarm != null && onmsAlarm.getStickyMemo() != null) {
            m_memoDao.delete(onmsAlarm.getStickyMemo());
            onmsAlarm.setStickyMemo(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void removeReductionKeyMemo(int alarmId) {
        OnmsAlarm onmsAlarm = m_alarmDao.get(alarmId);
        if (onmsAlarm != null && onmsAlarm.getReductionKeyMemo() != null) {
            m_memoDao.delete(onmsAlarm.getReductionKeyMemo());
            onmsAlarm.setReductionKeyMemo(null);
        }
    }

    @Override
    @Transactional
    public List<OnmsAcknowledgment> getAcknowledgments(int alarmId) {
        CriteriaBuilder cb = new CriteriaBuilder(OnmsAcknowledgment.class);
        cb.eq("refId", alarmId);
        cb.eq("ackType", AckType.ALARM);
        return m_ackDao.findMatching(cb.toCriteria());
    }

    @Override
    @Transactional
    public List<AlarmSummary> getCurrentNodeAlarmSummaries() {
        return m_alarmDao.getNodeAlarmSummaries();
    }

}
