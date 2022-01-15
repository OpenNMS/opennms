/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Top-level element for the notifications.xml configuration file.
 */
public class Notifications implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Header containing information about this configuration file.
     */
    private Header header;

    @JsonProperty("notification")
    private List<Notification> notifications = new ArrayList<>();

    public Header getHeader() {
        return this.header;
    }

    public void setHeader(final Header header) {
        this.header = header;
    }

    public List<Notification> getNotifications() {
        return this.notifications;
    }

    public void setNotifications(final List<Notification> notifications) {
        if (notifications == this.notifications) return;
        this.notifications.clear();
        if (notifications != null) this.notifications.addAll(notifications);
    }

    public void addNotification(final Notification notification) {
        this.notifications.add(notification);
    }

    public boolean removeNotification(final Notification notification) {
        return this.notifications.remove(notification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.header, this.notifications);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Notifications) {
            final Notifications that = (Notifications) obj;
            return Objects.equals(this.header, that.header)
                    && Objects.equals(this.notifications, that.notifications);
        }
        return false;
    }
}
