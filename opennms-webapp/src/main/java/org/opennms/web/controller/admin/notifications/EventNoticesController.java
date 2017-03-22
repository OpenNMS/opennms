/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

        public String getEscapedName() {
            return StringEscapeUtils.escapeJavaScript(m_name);
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
