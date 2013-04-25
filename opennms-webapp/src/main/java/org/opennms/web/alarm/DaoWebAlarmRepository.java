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

package org.opennms.web.alarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.MemoDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsMemo;
import org.opennms.netmgt.model.OnmsReductionKeyMemo;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.acknowledgments.AckService;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmIdListFilter;
import org.opennms.web.alarm.filter.AlarmCriteria.AlarmCriteriaVisitor;
import org.opennms.web.filter.Filter;
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
public class DaoWebAlarmRepository implements WebAlarmRepository, InitializingBean {

    @Autowired
    AlarmDao m_alarmDao;

    @Autowired
    MemoDao m_memoDao;
    
    @Autowired
    AckService m_ackService;

    @Autowired
    AcknowledgmentDao m_ackDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    private OnmsCriteria getOnmsCriteria(final AlarmCriteria alarmCriteria) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("serviceType", "serviceType", OnmsCriteria.LEFT_JOIN);

        alarmCriteria.visit(new AlarmCriteriaVisitor<RuntimeException>() {

            public void visitAckType(AcknowledgeType ackType) throws RuntimeException {
                if (ackType == AcknowledgeType.ACKNOWLEDGED) {
                    criteria.add(Restrictions.isNotNull("alarmAckUser"));
                } else if (ackType == AcknowledgeType.UNACKNOWLEDGED) {
                    criteria.add(Restrictions.isNull("alarmAckUser"));
                }
            }

            public void visitFilter(Filter filter) throws RuntimeException {
                criteria.add(filter.getCriterion());
            }

            public void visitLimit(int limit, int offset) throws RuntimeException {
                criteria.setMaxResults(limit);
                criteria.setFirstResult(offset);
            }

            public void visitSortStyle(SortStyle sortStyle) throws RuntimeException {
                switch (sortStyle) {
                    case COUNT:
                        criteria.addOrder(Order.desc("counter"));
                        break;
                    case FIRSTEVENTTIME:
                        criteria.addOrder(Order.desc("firstEventTime"));
                        break;
                    case ID:
                        criteria.addOrder(Order.desc("id"));
                        break;
                    case INTERFACE:
                        criteria.addOrder(Order.desc("ipAddr"));
                        break;
                    case LASTEVENTTIME:
                        criteria.addOrder(Order.desc("lastEventTime"));
                        break;
                    case NODE:
                        criteria.addOrder(Order.desc("node.label"));
                        break;
                    case POLLER:
                        criteria.addOrder(Order.desc("distPoller"));
                        break;
                    case SERVICE:
                        criteria.addOrder(Order.desc("serviceType.name"));
                        break;
                    case SEVERITY:
                        criteria.addOrder(Order.desc("severity"));
                        break;
                    case ACKUSER:
                        criteria.addOrder(Order.asc("alarmAckUser"));
                        break;
                    case REVERSE_COUNT:
                        criteria.addOrder(Order.asc("counter"));
                        break;
                    case REVERSE_FIRSTEVENTTIME:
                        criteria.addOrder(Order.asc("firstEventTime"));
                        break;
                    case REVERSE_ID:
                        criteria.addOrder(Order.asc("id"));
                        break;
                    case REVERSE_INTERFACE:
                        criteria.addOrder(Order.asc("ipAddr"));
                        break;
                    case REVERSE_LASTEVENTTIME:
                        criteria.addOrder(Order.asc("lastEventTime"));
                        break;
                    case REVERSE_NODE:
                        criteria.addOrder(Order.asc("node.label"));
                        break;
                    case REVERSE_POLLER:
                        criteria.addOrder(Order.asc("distPoller"));
                        break;
                    case REVERSE_SERVICE:
                        criteria.addOrder(Order.asc("serviceType.name"));
                        break;
                    case REVERSE_SEVERITY:
                        criteria.addOrder(Order.asc("severity"));
                        break;
                    case REVERSE_ACKUSER:
                        criteria.addOrder(Order.desc("alarmAckUser"));
                        break;
                    default:
                        break;
                }
            }
        });

        return criteria;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void acknowledgeAll(String user, Date timestamp) {
        acknowledgeMatchingAlarms(user, timestamp, new AlarmCriteria());
    }

    @Transactional
    public void acknowledgeAlarms(String user, Date timestamp, int[] alarmIds) {
        acknowledgeMatchingAlarms(user, timestamp, new AlarmCriteria(new AlarmIdListFilter(alarmIds)));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void acknowledgeMatchingAlarms(String user, Date timestamp, AlarmCriteria criteria) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(getOnmsCriteria(criteria));

        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while (alarmsIt.hasNext()) {
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.ACKNOWLEDGE);
            m_ackService.processAck(ack);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void clearAlarms(int[] alarmIds, String user, Date timestamp) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(getOnmsCriteria(new AlarmCriteria(new AlarmIdListFilter(alarmIds))));

        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while (alarmsIt.hasNext()) {
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.CLEAR);
            m_ackService.processAck(ack);
            m_alarmDao.update(alarm);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public int countMatchingAlarms(AlarmCriteria criteria) {
        return queryForInt(getOnmsCriteria(criteria));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public int[] countMatchingAlarmsBySeverity(final AlarmCriteria criteria) {
        final int[] alarmCounts = new int[8];
        for (final OnmsSeverity value : OnmsSeverity.values()) {
            alarmCounts[value.getId()] = m_alarmDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("severity", value)));
        }
        return alarmCounts;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void escalateAlarms(int[] alarmIds, String user, Date timestamp) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(getOnmsCriteria(new AlarmCriteria(new AlarmIdListFilter(alarmIds))));

        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while (alarmsIt.hasNext()) {
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.ESCALATE);
            m_ackService.processAck(ack);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public OnmsAlarm getAlarm(int alarmId) {
        return m_alarmDao.get(alarmId);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public OnmsAlarm[] getMatchingAlarms(AlarmCriteria criteria) {
        return m_alarmDao.findMatching(getOnmsCriteria(criteria)).toArray(new OnmsAlarm[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void unacknowledgeAll(String user) {
        unacknowledgeMatchingAlarms(new AlarmCriteria(), user);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void unacknowledgeMatchingAlarms(AlarmCriteria criteria, String user) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(getOnmsCriteria(criteria));

        for (OnmsAlarm alarm : alarms) {
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckAction(AckAction.UNACKNOWLEDGE);
            m_ackService.processAck(ack);
        }

    }

    private int queryForInt(OnmsCriteria onmsCriteria) {
        return m_alarmDao.countMatching(onmsCriteria);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void acknowledgeAlarms(int[] alarmIds, String user, Date timestamp) {
        acknowledgeMatchingAlarms(user, timestamp, new AlarmCriteria(new AlarmIdListFilter(alarmIds)));
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void unacknowledgeAlarms(int[] alarmIds, String user) {
        unacknowledgeMatchingAlarms(new AlarmCriteria(new AlarmIdListFilter(alarmIds)), user);
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
        if (onmsAlarm != null) {
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
        if (onmsAlarm != null) {
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
