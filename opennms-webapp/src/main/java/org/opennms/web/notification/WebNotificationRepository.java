/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.notification;

import java.util.Date;

import org.opennms.web.notification.filter.NotificationCriteria;

/**
 * <p>WebNotificationRepository interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface WebNotificationRepository {
    
    /**
     * <p>countMatchingNotifications</p>
     *
     * @param criteria a {@link org.opennms.web.notification.filter.NotificationCriteria} object.
     * @return a int.
     */
    public abstract long countMatchingNotifications(NotificationCriteria criteria);
    
    /**
     * <p>getNotification</p>
     *
     * @param noticeId a int.
     * @return a {@link org.opennms.web.notification.Notification} object.
     */
    public abstract Notification getNotification(int noticeId);
    
    /**
     * <p>getMatchingNotifications</p>
     *
     * @param criteria a {@link org.opennms.web.notification.filter.NotificationCriteria} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     */
    public abstract Notification[] getMatchingNotifications(NotificationCriteria criteria);
    
    /**
     * <p>acknowledgeMatchingNotification</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     * @param criteria a {@link org.opennms.web.notification.filter.NotificationCriteria} object.
     */
    public abstract void acknowledgeMatchingNotification(String user, Date timestamp, NotificationCriteria criteria);
}
