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

/**
 * A NotificationTarget representing another notifcation group parsed from the
 * notifications.xml.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 * @since 1.8.1
 */
public class NotifTarget extends NotificationTarget {
    /**
     * The name of the notification
     */
    private String m_notifName;

    /**
     * The Notification object associated with this target
     */
    private Notification m_notification;

    /**
     * The overriding interval, takes precedence over the Notification interval.
     */
    private String m_interval;

    /**
     * Default Constructor
     */
    public NotifTarget() {
    }

    /**
     * Sets the name of the notification target
     *
     * @param name
     *            the name of the notification target to be set.
     */
    public void setNotifName(String name) {
        m_notifName = name;
    }

    /**
     * Returns the name of the notification
     *
     * @return the name of the notification target.
     */
    public String getNotifName() {
        return m_notifName;
    }

    /**
     * Sets the Notification object for this target
     *
     * @param notification
     *            the notification object to be set.
     */
    public void setNotification(Notification notification) {
        m_notification = notification;
    }

    /**
     * Returns the notification for this target
     *
     * @return the notification object.
     */
    public Notification getNotification() {
        return m_notification;
    }

    /**
     * Sets the overriding interval for this target
     *
     * @param interval
     *            the overriding interval to be set.
     */
    public void setInterval(String interval) {
        m_interval = interval;
    }

    /**
     * Returns the interval to use for this target. If an overriding interval
     * was set that will be returned, otherwise the interval from the
     * Notification will be returned.
     *
     * @return the interval for this object.
     */
    public String getInterval() {
        String interval = null;

        if (m_interval != null) {
            interval = m_interval;
        } else if (m_notification != null) {
            interval = m_notification.getInterval();
        }

        return interval;
    }

    /**
     * Returns the type of the target
     *
     * @return the target type, compare to NotificationTask.TARGET_TYPE_NOTIF.
     */
    public int getType() {
        return TARGET_TYPE_NOTIF;
    }
}
