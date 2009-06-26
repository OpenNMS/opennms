/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.alarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.acknowledgments.AckService;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmIdListFilter;
import org.opennms.web.alarm.filter.AlarmCriteria.AlarmCriteriaVisitor;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DaoWebAlarmRepository implements WebAlarmRepository {
    
    @Autowired
    AlarmDao m_alarmDao;
    
    @Autowired
    AckService m_ackService;
    
    private OnmsCriteria getOnmsCriteria(final AlarmCriteria alarmCriteria) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
        criteria.createAlias("node", "node",  OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("serviceType", "serviceType", OnmsCriteria.LEFT_JOIN);
        
        alarmCriteria.visit(new AlarmCriteriaVisitor<RuntimeException>(){

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
                default:
                    break;
                }
            }
            
        });
        
        return criteria;
    }
    
    private Alarm mapOnmsAlarmToAlarm(OnmsAlarm onmsAlarm) {
        if (onmsAlarm == null) { return null; }
        Alarm alarm = new Alarm();
        alarm.id = onmsAlarm.getId();
        alarm.uei = onmsAlarm.getUei();
        alarm.dpName = onmsAlarm.getDistPoller() != null ? onmsAlarm.getDistPoller().getName() : "";

        // node id can be null, in which case nodeID will be 0
        alarm.nodeID = onmsAlarm.getNode() != null ? onmsAlarm.getNode().getId() : 0;
        alarm.ipAddr = onmsAlarm.getIpAddr();

        // This causes serviceID to be null if the column in the database is null
        alarm.serviceID = onmsAlarm.getServiceType() != null ? onmsAlarm.getServiceType().getId() : 0;
        alarm.reductionKey = onmsAlarm.getReductionKey();
        alarm.count = onmsAlarm.getCounter();
        alarm.severity = onmsAlarm.getSeverity();
        alarm.lastEventID = onmsAlarm.getLastEvent().getId();
        alarm.firsteventtime = onmsAlarm.getFirstEventTime();
        alarm.lasteventtime = onmsAlarm.getLastEventTime();
        alarm.description = onmsAlarm.getDescription();
        alarm.logMessage = onmsAlarm.getLogMsg();
        alarm.operatorInstruction = onmsAlarm.getOperInstruct();
        alarm.troubleTicket = onmsAlarm.getTTicketId();
        alarm.troubleTicketState = onmsAlarm.getTTicketState();
      
        alarm.mouseOverText = onmsAlarm.getMouseOverText();
        alarm.suppressedUntil = onmsAlarm.getSuppressedUntil();
        alarm.suppressedUser = onmsAlarm.getSuppressedUser();
        alarm.suppressedTime = onmsAlarm.getSuppressedTime();
        alarm.acknowledgeUser = onmsAlarm.getAckUser();
        alarm.acknowledgeTime = onmsAlarm.getAckTime();

        alarm.nodeLabel = onmsAlarm.getNode() != null ? onmsAlarm.getNode().getLabel() : ""; 
        alarm.serviceName = onmsAlarm.getServiceType() != null ? onmsAlarm.getServiceType().getName() : "";
        return alarm;
    }
    
    @Transactional
    public void acknowledgeAll(String user, Date timestamp) {
        acknowledgeMatchingAlarms(user, timestamp, new AlarmCriteria());
    }
    
    @Transactional
    void acknowledgeAlarms(String user, Date timestamp, int[] alarmIds) {
        acknowledgeMatchingAlarms(user, timestamp, new AlarmCriteria(new AlarmIdListFilter(alarmIds)));
    }
    
    @Transactional
    public void acknowledgeMatchingAlarms(String user, Date timestamp, AlarmCriteria criteria) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(getOnmsCriteria(criteria));
        
        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while(alarmsIt.hasNext()) {
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.ACKNOWLEDGE);
            m_ackService.processAck(ack);
        }
    }
    
    @Transactional
    public void clearAlarms(int[] alarmIds, String user, Date timestamp) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(getOnmsCriteria(new AlarmCriteria(new AlarmIdListFilter(alarmIds))));
        
        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while(alarmsIt.hasNext()){
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.CLEAR);
            m_ackService.processAck(ack);
            m_alarmDao.update(alarm);
        }
    }
    
    @Transactional
    public int countMatchingAlarms(AlarmCriteria criteria) {
        return queryForInt(getOnmsCriteria(criteria));
    }
    
    @Transactional
    public int[] countMatchingAlarmsBySeverity(AlarmCriteria criteria) {
        OnmsCriteria crit = getOnmsCriteria(criteria).setProjection(Projections.groupProperty("severityId"));
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(crit);
        
        int[] alarmCounts = new int[8];
        alarmCounts[OnmsSeverity.CLEARED.getId()] = m_alarmDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("severityId", OnmsSeverity.CLEARED.getId())));
        alarmCounts[OnmsSeverity.CRITICAL.getId()] = m_alarmDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("severityId", OnmsSeverity.CRITICAL.getId())));
        alarmCounts[OnmsSeverity.INDETERMINATE.getId()] = m_alarmDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("severityId", OnmsSeverity.INDETERMINATE.getId())));
        alarmCounts[OnmsSeverity.MAJOR.getId()] = m_alarmDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("severityId", OnmsSeverity.MAJOR.getId())));
        alarmCounts[OnmsSeverity.MINOR.getId()] = m_alarmDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("severityId", OnmsSeverity.MINOR.getId())));
        alarmCounts[OnmsSeverity.NORMAL.getId()] = m_alarmDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("severityId", OnmsSeverity.NORMAL.getId())));
        alarmCounts[OnmsSeverity.WARNING.getId()] = m_alarmDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("severityId", OnmsSeverity.WARNING.getId())));
        return alarmCounts;
    }
    
    @Transactional
    public void escalateAlarms(int[] alarmIds, String user, Date timestamp) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(getOnmsCriteria(new AlarmCriteria(new AlarmIdListFilter(alarmIds))));
        
        Iterator<OnmsAlarm> alarmsIt = alarms.iterator();
        while(alarmsIt.hasNext()){
            OnmsAlarm alarm = alarmsIt.next();
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckTime(timestamp);
            ack.setAckAction(AckAction.ESCALATE);
            m_ackService.processAck(ack);
        }

    }
    
    @Transactional
    public Alarm getAlarm(int alarmId) {
        return mapOnmsAlarmToAlarm(m_alarmDao.get(alarmId));
    }
    
    @Transactional
    public Alarm[] getMatchingAlarms(AlarmCriteria criteria) {
        List<Alarm> alarms = new ArrayList<Alarm>();
        List<OnmsAlarm> onmsAlarms = m_alarmDao.findMatching(getOnmsCriteria(criteria));
        
        for(OnmsAlarm onmsAlarm : onmsAlarms) {
            alarms.add(mapOnmsAlarmToAlarm(onmsAlarm));
        }
        
        return alarms.toArray(new Alarm[0]);
    }
    
    @Transactional
    public void unacknowledgeAll(String user) {
        unacknowledgeMatchingAlarms(new AlarmCriteria(), user);
    }
    
    @Transactional
    public void unacknowledgeMatchingAlarms(AlarmCriteria criteria, String user) {
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(getOnmsCriteria(criteria));
        
        for(OnmsAlarm alarm : alarms) {
            OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
            ack.setAckAction(AckAction.UNACKNOWLEDGE);
            m_ackService.processAck(ack);
        }

    }
    
    private int queryForInt(OnmsCriteria onmsCriteria) {
        return m_alarmDao.countMatching(onmsCriteria);
    }

    @Transactional
    public void acknowledgeAlarms(int[] alarmIds, String user, Date timestamp) {
        acknowledgeMatchingAlarms(user, timestamp, new AlarmCriteria(new AlarmIdListFilter(alarmIds)));
    }

    @Transactional
    public void unacknowledgeAlarms(int[] alarmIds, String user) {
        unacknowledgeMatchingAlarms(new AlarmCriteria(new AlarmIdListFilter(alarmIds)), user);
    }

}
