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
package org.opennms.web.controller.admin.notifications;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.config.notifications.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.google.common.collect.Lists;

public class EventNoticesController extends AbstractController {
    @Autowired
    private EventConfDao m_eventConfDao;

    @Autowired
    private NotificationFactory m_notificationFactory;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // Make sure we have the latest list of events
        m_eventConfDao.reload();

        return new ModelAndView("/admin/notification/noticeWizard/eventNotices",
                "notifications", getNotifications());
    }

    private List<EventNotification> getNotifications() throws IOException {
        List<EventNotification> notifications = Lists.newLinkedList();
        Map<String, Notification> noticeMap = m_notificationFactory.getNotifications();
        for(String name : noticeMap.keySet()) {
            notifications.add(new EventNotification(name, noticeMap.get(name)));
        }
        Collections.sort(notifications);
        return notifications;
    }

    public class EventNotification implements Comparable<EventNotification> {
        final String m_name;
        final String m_uei;
        final Notification m_notification;

        public EventNotification(String name, Notification notification) {
            m_name = name;
            m_notification = notification;
            m_uei = notification.getUei();
        }

        public String getName() {
            return m_name;
        }

        public boolean getIsOn() {
            return "on".equalsIgnoreCase(m_notification.getStatus());
        }

        public String getUei() {
            return m_uei;
        }

        public String getEventLabel() {
            return m_eventConfDao.getEventLabel(m_uei);
        }

        public String getDisplayUei() {
            if (m_uei != null && m_uei.startsWith("~")) {
                return "REGEX: " + m_uei.substring(1);
           } else { 
               return m_uei;
           }
        }

        @Override
        public int compareTo(EventNotification other) {
            return m_name.compareTo(other.m_name);
        }
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }

    public void setNotificationFactory(NotificationFactory notificationFactory) {
        m_notificationFactory = notificationFactory;
    }
}
