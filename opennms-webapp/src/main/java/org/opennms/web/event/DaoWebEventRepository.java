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

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.web.event.EventFactory.AcknowledgeType;
import org.opennms.web.event.EventFactory.SortStyle;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventCriteria.EventCriteriaVisitor;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;

public class DaoWebEventRepository implements WebEventRepository {
    
    @Autowired
    EventDao m_eventDao;
    
    private OnmsCriteria getOnmsCriteria(final EventCriteria eventCriteria){
        final OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class);
        criteria.createAlias("alarm", "alarm", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("serviceType", "serviceType", OnmsCriteria.LEFT_JOIN);
        
        eventCriteria.visit(new EventCriteriaVisitor<RuntimeException>(){

            public void visitAckType(AcknowledgeType ackType) throws RuntimeException {
                criteria.add(Restrictions.isNotNull(ackType.getName()));
            }

            public void visitFilter(Filter filter) throws RuntimeException {
                criteria.add(filter.getCriterion());
            }

            public void visitLimit(int limit, int offset) throws RuntimeException {
                criteria.setMaxResults(limit);
                criteria.setFirstResult(offset);
                
            }

            public void visitSortStyle(SortStyle sortStyle) throws RuntimeException {
                criteria.addOrder(Order.asc(sortStyle.getName()));
            }
            
        });
        
        return criteria;
    }
    
    private Event mapOnmsEventToEvent(OnmsEvent onmsEvent){
        Event event = new Event();
        
        return event;
    }
    
    public void acknowledgeAll(String user, Date timestamp) {

    }

    public void acknowledgeMatchingEvents(String user, Date timestamp, EventCriteria criteria) {

    }

    public int countMatchingEvents(EventCriteria criteria) {
        return 0;
    }

    public int[] countMatchingEventsBySeverity(EventCriteria criteria) {
        return null;
    }

    public Event getEvent(int eventId) {
        return mapOnmsEventToEvent(m_eventDao.get(eventId));
    }

    public Event[] getMatchingEvents(EventCriteria criteria) {
        List<Event> events = new ArrayList<Event>();
        List<OnmsEvent> onmsEvents = m_eventDao.findMatching(getOnmsCriteria(criteria));
        
        if(onmsEvents.size() > 0){
            Iterator<OnmsEvent> eventIt = onmsEvents.iterator();
            
            while(eventIt.hasNext()){
                events.add(mapOnmsEventToEvent(eventIt.next()));
            }   
        }
        
        return events.toArray(new Event[0]);
    }

    public void unacknowledgeAll() {

    }

    public void unacknowledgeMatchingEvents(EventCriteria criteria) {

    }

}
