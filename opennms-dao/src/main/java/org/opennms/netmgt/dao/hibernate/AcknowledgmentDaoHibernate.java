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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.ObjectNotFoundException;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.Acknowledgeable;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNotification;

/**
 * Hibernate implementation of Acknowledgment DAO
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class AcknowledgmentDaoHibernate extends AbstractDaoHibernate<OnmsAcknowledgment, Integer> implements AcknowledgmentDao {

    /**
     * <p>Constructor for AcknowledgmentDaoHibernate.</p>
     */
    public AcknowledgmentDaoHibernate() {
        super(OnmsAcknowledgment.class);
    }

    /** {@inheritDoc} */
    public void updateAckable(Acknowledgeable ackable) {
        getHibernateTemplate().update(ackable);
    }

    /** {@inheritDoc} */
    public List<Acknowledgeable> findAcknowledgables(final OnmsAcknowledgment ack) {
        List<Acknowledgeable> ackables = new ArrayList<Acknowledgeable>();
        
        if (ack == null || ack.getAckType() == null) {
            return ackables;
        }

        if (ack.getAckType().equals(AckType.ALARM)) {
            final OnmsAlarm alarm = findAlarm(ack);

            try {
                if (alarm != null && alarm.getAckId() != null) {
                    ackables.add(alarm);
                    List<OnmsNotification> notifs = findRelatedNotifications(alarm);
                    
                    if (notifs != null) {
                        for (OnmsNotification notif : notifs) {
                            try {
                                if (notif.getAckId() != null) {
                                    ackables.add(notif);
                                }
                            } catch (final ObjectNotFoundException e) {
                                LogUtils.warnf(this, e, "found ackables for alarm #%d but ackable was invalid", ack.getRefId());
                            }
                        }
                    }
                }
            } catch (final ObjectNotFoundException e) {
                LogUtils.warnf(this, e, "unable to find alarm with ID %d", ack.getRefId());
            }
        }

        else if (ack.getAckType().equals(AckType.NOTIFICATION)) {
            final OnmsNotification notif = findNotification(ack);

            try {
                if (notif != null && notif.getAckId() != null) {
                    ackables.add(notif);
                    try {
                        if (notif.getEvent() != null) {
                            final OnmsAlarm alarm = notif.getEvent().getAlarm();
                            if (alarm != null) {
                                ackables.add(alarm);
                            }
                        }
                    } catch (final ObjectNotFoundException e) {
                        LogUtils.warnf(this, e, "unable to find alarm for notification #%d", notif.getNotifyId());
                    }
                }
            } catch (final ObjectNotFoundException e) {
                LogUtils.warnf(this, e, "unable to find notification with ID %d", ack.getRefId());
            }
        }
        
        return ackables;
    }
    
    private List<OnmsNotification> findRelatedNotifications(final OnmsAlarm alarm) {
        final String hql = "from OnmsNotification as n where n.event.alarm = ?";
        return findObjects(OnmsNotification.class, hql, alarm);
    }

    private OnmsAlarm findAlarm(final OnmsAcknowledgment ack) {
//      hql = "from OnmsAlarm as alarms where alarms.id = ?";        
//      return findUnique(OnmsAlarm.class, hql, ack.getRefId());
        try {
            if (ack != null) {
                return (OnmsAlarm) getHibernateTemplate().load(OnmsAlarm.class, ack.getRefId());
            }
        } catch (final Exception e) {
            LogUtils.warnf(this, e, "unable to find alarm with ID %d", ack.getRefId());
        }
        return null;
    }

    private OnmsNotification findNotification(final OnmsAcknowledgment ack) {
//      hql = "from OnmsAlarm as alarms where alarms.id = ?";        
//      return findUnique(OnmsAlarm.class, hql, ack.getRefId());
        try {
            if (ack != null) {
                return (OnmsNotification) getHibernateTemplate().load(OnmsNotification.class, ack.getRefId());
            }
        } catch (final Exception e) {
            LogUtils.warnf(this, e, "unable to find notification with ID %d", ack.getRefId());
        }
        return null;
    }

}
