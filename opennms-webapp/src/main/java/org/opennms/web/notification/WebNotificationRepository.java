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
    public abstract int countMatchingNotifications(NotificationCriteria criteria);
    
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
