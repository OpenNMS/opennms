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
package org.opennms.web.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.NotificationDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.filter.NotificationCriteria;
import org.opennms.web.notification.filter.NotificationCriteria.NotificationCriteriaVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DaoWebNotificationRepository implements WebNotificationRepository {
    
    @Autowired
    NotificationDao m_notificationDao;
    
    private OnmsCriteria getOnmsCriteria(final NotificationCriteria notificationCriteria){
        final OnmsCriteria criteria = new OnmsCriteria(OnmsNotification.class);
        criteria.createAlias("node", "node", OnmsCriteria.LEFT_JOIN);
        criteria.createAlias("serviceType", "serviceType", OnmsCriteria.LEFT_JOIN);
        
        notificationCriteria.visit(new NotificationCriteriaVisitor<RuntimeException>(){

            public void visitAckType(AcknowledgeType ackType) throws RuntimeException {
                if(ackType == AcknowledgeType.ACKNOWLEDGED) {
                    criteria.add(Restrictions.isNotNull("answeredBy"));
                } else if (ackType == AcknowledgeType.UNACKNOWLEDGED) {
                   criteria.add(Restrictions.isNull("answeredBy")); 
                }
                // AcknowledgeType.BOTH just adds no restriction
            }

            public void visitGroupBy() throws RuntimeException {
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
                    case USER:
                        criteria.addOrder(Order.desc("user"));
                        break;
                    case RESPONDER:
                        criteria.addOrder(Order.desc("responder"));        
                        break;
                    case PAGETIME:
                        criteria.addOrder(Order.desc("pagetime"));
                        break;
                    case RESPONDTIME:
                        criteria.addOrder(Order.desc("respondTime"));
                        break;
                    case NODE:
                        criteria.addOrder(Order.desc("nodeId"));
                        break;
                    case INTERFACE:
                        criteria.addOrder(Order.desc("interfaceId"));
                        break;
                    case SERVICE:
                        criteria.addOrder(Order.desc("serviceId"));
                        break;
                    case ID:
                        criteria.addOrder(Order.desc("notifyId"));
                        break;
                    case REVERSE_USER:
                        criteria.addOrder(Order.asc("user"));
                        break;
                    case REVERSE_RESPONDER:
                        criteria.addOrder(Order.asc("responder"));            
                        break;
                    case REVERSE_PAGETIME:
                        criteria.addOrder(Order.asc("pagetime"));
                        break;
                    case REVERSE_RESPONDTIME:
                        criteria.addOrder(Order.asc("respondTimer"));
                        break;
                    case REVERSE_NODE:
                        criteria.addOrder(Order.asc("nodeId"));
                        break;
                    case REVERSE_INTERFACE:
                        criteria.addOrder(Order.asc("interfaceId"));
                        break;
                    case REVERSE_SERVICE:
                        criteria.addOrder(Order.asc("serviceId"));
                        break;
                    case REVERSE_ID:
                        criteria.addOrder(Order.asc("notifyId"));
                        break;
                    
                }
                
            }
            
        });
        
        return criteria;
    }

    private Notification mapOnmsNotificationToNotification(OnmsNotification onmsNotification){
        if(onmsNotification != null){
            Notification notif = new Notification();
            notif.m_eventId = onmsNotification.getEvent() != null ? onmsNotification.getEvent().getId() : 0;
            notif.m_interfaceID = onmsNotification.getIpAddress();
            notif.m_nodeID = onmsNotification.getNode() != null ? onmsNotification.getNode().getId() : 0;
            notif.m_notifyID = onmsNotification.getNotifyId();
            notif.m_numMsg = onmsNotification.getNumericMsg();
            notif.m_responder = onmsNotification.getAnsweredBy();
            notif.m_serviceId = onmsNotification.getServiceType() != null ? onmsNotification.getServiceType().getId() : 0;
            notif.m_serviceName = onmsNotification.getServiceType() != null ? onmsNotification.getServiceType().getName() : "";
            notif.m_timeReply = onmsNotification.getRespondTime() != null ? onmsNotification.getRespondTime().getTime() : 0;
            notif.m_timeSent = onmsNotification.getPageTime() != null ? onmsNotification.getPageTime().getTime() : 0;
            notif.m_txtMsg = onmsNotification.getTextMsg();
            
            return notif;
        }else{
            return null;
        }
    }
    
    @Transactional
    public void acknowledgeMatchingNotification(String user, Date timestamp, NotificationCriteria criteria) {
        List<OnmsNotification> notifs = m_notificationDao.findMatching(getOnmsCriteria(criteria));
        
        for (OnmsNotification notif : notifs) {
            notif.setAnsweredBy(user);
            notif.setRespondTime(timestamp);
            m_notificationDao.update(notif);
        }
    }
    
    @Transactional
    public int countMatchingNotifications(NotificationCriteria criteria) {
        return queryForInt(getOnmsCriteria(criteria));
    }

    @Transactional
    public Notification[] getMatchingNotifications(NotificationCriteria criteria) {
        List<Notification> notifications = new ArrayList<Notification>();
        List<OnmsNotification> onmsNotifs = m_notificationDao.findMatching(getOnmsCriteria(criteria));

        for (OnmsNotification notif : onmsNotifs) {
            notifications.add(mapOnmsNotificationToNotification(notif));
        }
        
        return notifications.toArray(new Notification[0]);
    }
    
    @Transactional
    public Notification getNotification(int noticeId) {
        return mapOnmsNotificationToNotification(m_notificationDao.get(noticeId));
    }
    
    private int queryForInt(OnmsCriteria onmsCriteria) {
        return m_notificationDao.countMatching(onmsCriteria);
    }

}
