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
 * Modifications:
 * 
 * Created: January 19, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.Acknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNotification;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Hibernate implementation of Acknowledgment DAO
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class AcknowledgmentDaoHibernate extends AbstractDaoHibernate<Acknowledgment, Integer> implements AcknowledgmentDao {

    public AcknowledgmentDaoHibernate() {
        super(Acknowledgment.class);
    }

    public List<Acknowledgeable> findAcknowledgables(final Acknowledgment ack) {
        
        List<Acknowledgeable> ackables = new ArrayList<Acknowledgeable>();
        
        if (ack.getAckType().equals(AckType.Alarm)) {
            final OnmsAlarm alarm = findAlarm(ack);
            
            if (alarm != null) {
                ackables.add(alarm);
                
                List<OnmsNotification> notifs = findRelatedNotifications(alarm);
                
                if (notifs != null) {
                    ackables.addAll(notifs);
                }
            }
            
            
        }
        
        if (ack.getAckType().equals(AckType.Notification)) {
            final String hql = "from OnmsNotification as notifications where notifications.id = :notifyId";
            Object result = findAck(ack, hql);
            OnmsNotification notification = OnmsNotification.class.cast(result);
            ackables.add(notification);
        }
        
        return ackables;

    }

    private List<OnmsNotification> findRelatedNotifications(
            final OnmsAlarm alarm) {
        final String hql = "from OnmsNotification as notifications " +
        		     "inner join notifications.event as events " +
        		     "where events.alarm.id = :alarmId";
        
        HibernateCallback callback = new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setParameter(1, alarm.getId());
                List<OnmsNotification> results = query.list();
                return results;
            }
        };
        
        Object object = getHibernateTemplate().execute(callback);
        List<OnmsNotification> notifs = (List<OnmsNotification>)object;
        return notifs;
    }

    private OnmsAlarm findAlarm(final Acknowledgment ack) {
        String hql = "from OnmsAlarm as alarms where alarms.id = :alarmId";
        Object result = findAck(ack, hql);
        OnmsAlarm alarm = OnmsAlarm.class.cast(result);
        return alarm;
    }

    private Object findAck(final Acknowledgment ack, final String hql) {
        Object result = getHibernateTemplate().execute(createUniqueAckResultCallBack(ack, hql));
        return result;
    }

    private HibernateCallback createUniqueAckResultCallBack(final Acknowledgment ack, final String hql) {
        HibernateCallback callback = new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setParameter(1, ack.getRefId());
                return query.uniqueResult();
            }
            
        };
        return callback;
    }

    public void updateAckable(Acknowledgeable ackable) {
        // TODO Auto-generated method stub
        
    }

}
