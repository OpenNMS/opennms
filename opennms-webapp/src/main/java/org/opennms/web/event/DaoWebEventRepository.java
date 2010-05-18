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
package org.opennms.web.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventDisplayFilter;
import org.opennms.web.event.filter.EventCriteria.EventCriteriaVisitor;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DaoWebEventRepository implements WebEventRepository {
    
    @Autowired
    EventDao m_eventDao;
    
    private OnmsCriteria getOnmsCriteria(final EventCriteria eventCriteria){
        final OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class);
        criteria.createAlias("alarm", "alarm", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("serviceType", "serviceType", OnmsCriteria.LEFT_JOIN);
        
        criteria.add(new EventDisplayFilter("Y").getCriterion());
        
        eventCriteria.visit(new EventCriteriaVisitor<RuntimeException>(){

            public void visitAckType(AcknowledgeType ackType) throws RuntimeException {
                if(ackType == AcknowledgeType.ACKNOWLEDGED){
                    criteria.add(Restrictions.isNotNull("eventAckUser"));
                }else if(ackType == AcknowledgeType.UNACKNOWLEDGED){
                    criteria.add(Restrictions.isNull("eventAckUser"));
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
                switch(sortStyle){
                case ID:
                    criteria.addOrder(Order.desc("id"));
                     break;
                case INTERFACE:
                    criteria.addOrder(Order.desc("ipAddr"));
                    break;
                case NODE:
                    criteria.addOrder(Order.desc("node.label"));
                    break;
                case POLLER:
                    criteria.addOrder(Order.desc("dispPoller"));
                    break;
                case SERVICE:
                    criteria.addOrder(Order.desc("serviceType.name"));
                    break;
                case SEVERITY:
                    criteria.addOrder(Order.desc("eventSeverity"));
                    break;
                case TIME:
                    criteria.addOrder(Order.desc("eventTime"));
                    break;
                case REVERSE_ID:
                    criteria.addOrder(Order.asc("id"));
                    break;
                case REVERSE_INTERFACE:
                    criteria.addOrder(Order.asc("ipAddr"));
                    break;
                case REVERSE_NODE:
                    criteria.addOrder(Order.asc("node.label"));
                    break;
                case REVERSE_POLLER:
                    criteria.addOrder(Order.asc("dispPoller"));
                    break;
                case REVERSE_SERVICE:
                    criteria.addOrder(Order.desc("serviceType.name"));
                    break;
                case REVERSE_SEVERITY:
                    criteria.addOrder(Order.asc("eventSeverity"));
                    break;
                case REVERSE_TIME:
                    criteria.addOrder(Order.asc("eventTime"));
                    break;
                
                }
            }
            
        });
        
        return criteria;
    }
    
    private Event mapOnmsEventToEvent(OnmsEvent onmsEvent){
        log().debug("Mapping OnmsEvent to WebEvent for event with database id " + onmsEvent.getId());
        Event event = new Event();
        event.acknowledgeTime = onmsEvent.getEventAckTime();
        event.acknowledgeUser = onmsEvent.getEventAckUser();
        event.alarmId = onmsEvent.getAlarm() != null ? onmsEvent.getAlarm().getId() : 0;
        event.autoAction = onmsEvent.getEventAutoAction();
        event.createTime = onmsEvent.getEventCreateTime();
        event.description = onmsEvent.getEventDescr();
        event.dpName = onmsEvent.getDistPoller() != null ? onmsEvent.getDistPoller().getName() : "";
        event.eventDisplay = Boolean.valueOf(onmsEvent.getEventDisplay().equals("Y"));
        event.forward = onmsEvent.getEventForward();
        event.host = onmsEvent.getEventHost();
        event.id = onmsEvent.getId();
        event.ipAddr = onmsEvent.getIpAddr();
        event.logGroup = onmsEvent.getEventLogGroup();
        event.logMessage = onmsEvent.getEventLogMsg();
        event.mouseOverText = onmsEvent.getEventMouseOverText();
        event.nodeLabel = getNodeLabelFromNode(onmsEvent);
        log().debug("Found NodeLabel for mapped event:" + event.getNodeLabel());
        event.nodeID = getNodeIdFromNode(onmsEvent);
        log().debug("Found NodeId for mapped event:" + event.getNodeId());
        event.notification = onmsEvent.getEventNotification();
        event.operatorAction = onmsEvent.getEventOperAction();
        event.operatorActionMenuText = onmsEvent.getEventOperActionMenuText();
        event.operatorInstruction = onmsEvent.getEventOperInstruct();
        event.parms = onmsEvent.getEventParms();
        event.serviceID = onmsEvent.getServiceType() != null ? onmsEvent.getServiceType().getId() : 0;
        event.serviceName = onmsEvent.getServiceType() != null ? onmsEvent.getServiceType().getName() : "";
        event.severity = OnmsSeverity.get(onmsEvent.getEventSeverity());
        event.snmp = onmsEvent.getEventSnmp();
        event.snmphost = onmsEvent.getEventSnmpHost();
        event.time = onmsEvent.getEventTime();
        event.troubleTicket = onmsEvent.getEventTTicket();
        event.troubleTicketState = onmsEvent.getEventTTicketState();
        event.uei = onmsEvent.getEventUei();
        return event;
    }

    private Integer getNodeIdFromNode(OnmsEvent onmsEvent) {
        try {
            return onmsEvent.getNode() != null ? onmsEvent.getNode().getId() : 0;            
        } catch (org.hibernate.ObjectNotFoundException e) {
            log().debug("No node found in database for event with id: " + onmsEvent.getId());
            return 0;
        }
    }
    
    private String getNodeLabelFromNode(OnmsEvent onmsEvent) {
        try {
            return onmsEvent.getNode() != null ? onmsEvent.getNode().getLabel() : "";                    
        } catch (org.hibernate.ObjectNotFoundException e) {
            log().debug("No node found in database for event with id: " + onmsEvent.getId());
            return "";
        } 
    }
    
    @Transactional
    public void acknowledgeAll(String user, Date timestamp) {
        acknowledgeMatchingEvents(user, timestamp, new EventCriteria());
    }
    
    @Transactional
    public void acknowledgeMatchingEvents(String user, Date timestamp, EventCriteria criteria) {
        List<OnmsEvent> events = m_eventDao.findMatching(getOnmsCriteria(criteria));
        
        Iterator<OnmsEvent> eventsIt = events.iterator();
        while(eventsIt.hasNext()){
            OnmsEvent event = eventsIt.next();
            event.setEventAckUser(user);
            event.setEventAckTime(timestamp);
            m_eventDao.update(event);
        }
    }
    
    @Transactional
    public int countMatchingEvents(EventCriteria criteria) {
        return m_eventDao.countMatching(getOnmsCriteria(criteria));
    }
    
    @Transactional
    public int[] countMatchingEventsBySeverity(EventCriteria criteria) {
        //OnmsCriteria crit = getOnmsCriteria(criteria).setProjection(Projections.groupProperty("severityId"));
        
        int[] eventCounts = new int[8];
        eventCounts[OnmsSeverity.CLEARED.getId()] = m_eventDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("eventSeverity", OnmsSeverity.CLEARED.getId())));
        eventCounts[OnmsSeverity.CRITICAL.getId()] = m_eventDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("eventSeverity", OnmsSeverity.CRITICAL.getId())));
        eventCounts[OnmsSeverity.INDETERMINATE.getId()] = m_eventDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("eventSeverity", OnmsSeverity.INDETERMINATE.getId())));
        eventCounts[OnmsSeverity.MAJOR.getId()] = m_eventDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("eventSeverity", OnmsSeverity.MAJOR.getId())));
        eventCounts[OnmsSeverity.MINOR.getId()] = m_eventDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("eventSeverity", OnmsSeverity.MINOR.getId())));
        eventCounts[OnmsSeverity.NORMAL.getId()] = m_eventDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("eventSeverity", OnmsSeverity.NORMAL.getId())));
        eventCounts[OnmsSeverity.WARNING.getId()] = m_eventDao.countMatching(getOnmsCriteria(criteria).add(Restrictions.eq("eventSeverity", OnmsSeverity.WARNING.getId())));
        return eventCounts;
    }
    
    @Transactional
    public Event getEvent(int eventId) {
        return mapOnmsEventToEvent(m_eventDao.get(eventId));
    }
    
    @Transactional
    public Event[] getMatchingEvents(EventCriteria criteria) {
        List<Event> events = new ArrayList<Event>();
        log().debug("getMatchingEvents: try to get events fr Criteria: " + criteria.toString());
        List<OnmsEvent> onmsEvents = m_eventDao.findMatching(getOnmsCriteria(criteria));

        log().debug("getMatchingEvents: found " + onmsEvents.size() + " events");

        if(onmsEvents.size() > 0){
            Iterator<OnmsEvent> eventIt = onmsEvents.iterator();
            
            while(eventIt.hasNext()){
                events.add(mapOnmsEventToEvent(eventIt.next()));
            }   
        }
        
        return events.toArray(new Event[0]);
    }
    
    @Transactional
    public void unacknowledgeAll() {
        unacknowledgeMatchingEvents(new EventCriteria());
    }
    
    @Transactional
    public void unacknowledgeMatchingEvents(EventCriteria criteria) {
        List<OnmsEvent> events = m_eventDao.findMatching(getOnmsCriteria(criteria));
        
        for(OnmsEvent event : events) {
            event.setEventAckUser(null);
            event.setEventAckTime(null);
            m_eventDao.update(event);
        }
    }
    
    private static ThreadCategory log() {
        return ThreadCategory.getInstance(DaoWebEventRepository.class);
    }


}
