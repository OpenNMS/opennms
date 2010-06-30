//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.notification.bobject;

import java.util.ArrayList;
import java.util.List;

import org.opennms.web.WebSecurityUtils;

/**
 * This class holds the information parsed from the notifications.xml
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 * @since 1.6.12
 */
public class Notification {
    /**
     * The interval to wait between processing target
     */
    private String m_interval;

    /**
     * The name of the notification
     */
    private String m_name;

    /**
     * Comments for the notification
     */
    private String m_comments;

    /**
     * The list of users or other notifications to include in this notification
     */
    private List<NotificationTarget> m_targets;

    /**
     * Default constructor, initializes members
     */
    public Notification() {
        m_targets = new ArrayList<NotificationTarget>();
    }

    /**
     * Sets the name of the notification
     *
     * @param name
     *            the name to be set for this notification.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Returns the name of the notification
     *
     * @return the name of the notification.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Sets the comments for the notification
     *
     * @param comments
     *            comments to be set for this notification.
     */
    public void setComments(String comments) {
        m_comments = comments;
    }

    /**
     * Returns the comments for the notification
     *
     * @return comments for this notification.
     */
    public String getComments() {
        return m_comments;
    }

    /**
     * Sets the interval for the notification
     *
     * @param interval
     *            the interval to be set for this notification.
     */
    public void setInterval(String interval) {
        m_interval = interval;
    }

    /**
     * Returns the string version of the interval
     *
     * @return the interval for this notification.
     */
    public String getInterval() {
        return m_interval;
    }

    /**
     * Returns the interval converted to milliseconds
     *
     * @return the interval in milliseconds
     */
    public long getIntervalMilliseconds() {
        long interval = 0;

        if (!m_interval.equals("all")) {
            // interval = TimeConverter.convertToMillis(m_interval);
            interval = WebSecurityUtils.safeParseInt(m_interval);
        }

        return interval;
    }

    /**
     * Returns the interval in seconds
     *
     * @return the interval in seconds
     */
    public long getIntervalSeconds() {
        return getIntervalMilliseconds() / 1000;
    }

    /**
     * Adds a target to the notification
     *
     * @param target
     *            a target to be added for this notification.
     */
    public void addTarget(NotificationTarget target) {
        m_targets.add(target);
    }

    /**
     * Returns the list of targets
     *
     * @return the list of targets.
     */
    public List<NotificationTarget> getTargets() {
        return m_targets;
    }
}
