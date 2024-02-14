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
