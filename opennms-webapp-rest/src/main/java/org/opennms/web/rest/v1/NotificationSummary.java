/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.OnmsNotificationCollection;

@XmlRootElement(name="notification-summary")
public class NotificationSummary {
    private String user;
    private int totalCount = 0;
    private int totalUnacknowledgedCount = 0;
    private int userUnacknowledgedCount = 0;
    // Unacknowledged notices, not assigned to current user
    private int teamUnacknowledgedCount = 0;
    private OnmsNotificationCollection userUnacknowledgedNotifications = new OnmsNotificationCollection();

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalUnacknowledgedCount() {
        return totalUnacknowledgedCount;
    }

    public void setTotalUnacknowledgedCount(int totalUnacknowledgedCount) {
        this.totalUnacknowledgedCount = totalUnacknowledgedCount;
    }

    public int getUserUnacknowledgedCount() {
        return userUnacknowledgedCount;
    }

    public void setUserUnacknowledgedCount(int userUnacknowledgedCount) {
        this.userUnacknowledgedCount = userUnacknowledgedCount;
    }

    public void setUserUnacknowledgedNotifications(OnmsNotificationCollection userAcknowledgedNotifications) {
        this.userUnacknowledgedNotifications = userAcknowledgedNotifications;
    }

    public OnmsNotificationCollection getUserUnacknowledgedNotifications() {
        return userUnacknowledgedNotifications;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getTeamUnacknowledgedCount() {
        return teamUnacknowledgedCount;
    }

    public void setTeamUnacknowledgedCount(int teamUnacknowledgedCount) {
        this.teamUnacknowledgedCount = teamUnacknowledgedCount;
    }
}
