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
package org.opennms.netmgt.config.notifications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

/**
 * Top-level element for the notifications.xml configuration file.
 */
@XmlRootElement(name = "notifications")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifications.xsd")
public class Notifications implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Header containing information about this configuration file.
     */
    @XmlElement(name = "header", required = true)
    private Header m_header;

    @XmlElement(name = "notification", required = true)
    private List<Notification> m_notifications = new ArrayList<>();

    public Notifications() { }

    public Header getHeader() {
        return m_header;
    }

    public void setHeader(final Header header) {
        m_header = header;
    }

    public List<Notification> getNotifications() {
        return m_notifications;
    }

    public void setNotifications(final List<Notification> notifications) {
        if (notifications == m_notifications) return;
        m_notifications.clear();
        if (notifications != null) m_notifications.addAll(notifications);
    }

    public void addNotification(final Notification notification) {
        m_notifications.add(notification);
    }

    public boolean removeNotification(final Notification notification) {
        return m_notifications.remove(notification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_header, m_notifications);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Notifications) {
            final Notifications that = (Notifications)obj;
            return Objects.equals(this.m_header, that.m_header)
                    && Objects.equals(this.m_notifications, that.m_notifications);
        }
        return false;
    }

}
