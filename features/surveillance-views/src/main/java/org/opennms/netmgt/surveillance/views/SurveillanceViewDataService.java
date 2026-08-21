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
package org.opennms.netmgt.surveillance.views;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.SurveillanceStatus;

/**
 * Computes the data a surveillance view renders: the grid of cell statuses,
 * plus the alarm / notification / node-RTC drill-downs for a selected
 * row-and-column category intersection.
 *
 * <p>This is the non-UI core of the retired Vaadin
 * {@code SurveillanceViewService}. Drill-downs take category <em>names</em>
 * (a view row's or column's {@code categories}) and return detached value
 * objects, fully materialized inside the service's transaction, so callers
 * never touch lazy Hibernate state.
 */
public interface SurveillanceViewDataService {

    /**
     * The status of every cell of the view: for each row/column pair, the
     * count of nodes with at least one down service ("down") out of all nodes
     * ("total") in the intersection of the row's and column's categories.
     * Indexed {@code [row][column]} in definition order.
     *
     * @throws IllegalArgumentException if the view references a category name
     *                                  that does not exist
     */
    SurveillanceStatus[][] calculateCellStatus(SurveillanceView view);

    /**
     * The most recent unacknowledged alarms (up to 100) on non-deleted nodes
     * in the intersection of the two category sets.
     */
    List<SurveillanceAlarm> getAlarmsForCategories(Set<String> rowCategories, Set<String> columnCategories);

    /**
     * Notifications for nodes in the intersection of the two category sets,
     * bucketed the way the legacy dashboard did: unresponded and older than 15
     * minutes are "Critical", unresponded but newer are "Minor", and responded
     * ones from the last week are "Normal" (see {@code getSeverity()}).
     */
    List<SurveillanceNotification> getNotificationsForCategories(Set<String> rowCategories, Set<String> columnCategories);

    /**
     * Per-node 24-hour availability (RTC) for nodes in the intersection of the
     * two category sets.
     */
    List<NodeRtc> getNodeRtcsForCategories(Set<String> rowCategories, Set<String> columnCategories);

    /** An unacknowledged alarm, flattened for display. */
    class SurveillanceAlarm {
        private final Integer id;
        private final String uei;
        private final String severity;
        private final Integer nodeId;
        private final String nodeLabel;
        private final String logMessage;
        private final Date lastEventTime;
        private final Integer count;

        public SurveillanceAlarm(Integer id, String uei, String severity, Integer nodeId, String nodeLabel, String logMessage, Date lastEventTime, Integer count) {
            this.id = id;
            this.uei = uei;
            this.severity = severity;
            this.nodeId = nodeId;
            this.nodeLabel = nodeLabel;
            this.logMessage = logMessage;
            this.lastEventTime = lastEventTime;
            this.count = count;
        }

        public Integer getId() {
            return id;
        }

        public String getUei() {
            return uei;
        }

        public String getSeverity() {
            return severity;
        }

        public Integer getNodeId() {
            return nodeId;
        }

        public String getNodeLabel() {
            return nodeLabel;
        }

        public String getLogMessage() {
            return logMessage;
        }

        public Date getLastEventTime() {
            return lastEventTime;
        }

        public Integer getCount() {
            return count;
        }
    }

    /** A notification with its dashboard severity bucket. */
    class SurveillanceNotification {
        private final Integer id;
        private final Integer nodeId;
        private final String nodeLabel;
        private final String serviceName;
        private final String textMessage;
        private final Date pageTime;
        private final Date respondTime;
        private final String answeredBy;
        private final String severity;

        public SurveillanceNotification(Integer id, Integer nodeId, String nodeLabel, String serviceName, String textMessage, Date pageTime, Date respondTime, String answeredBy, String severity) {
            this.id = id;
            this.nodeId = nodeId;
            this.nodeLabel = nodeLabel;
            this.serviceName = serviceName;
            this.textMessage = textMessage;
            this.pageTime = pageTime;
            this.respondTime = respondTime;
            this.answeredBy = answeredBy;
            this.severity = severity;
        }

        public Integer getId() {
            return id;
        }

        public Integer getNodeId() {
            return nodeId;
        }

        public String getNodeLabel() {
            return nodeLabel;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getTextMessage() {
            return textMessage;
        }

        public Date getPageTime() {
            return pageTime;
        }

        public Date getRespondTime() {
            return respondTime;
        }

        public String getAnsweredBy() {
            return answeredBy;
        }

        public String getSeverity() {
            return severity;
        }
    }

    /** A node's 24-hour availability: service counts and the up-time ratio (0..1). */
    class NodeRtc {
        private final Integer nodeId;
        private final String nodeLabel;
        private final int serviceCount;
        private final int downServiceCount;
        private final double availability;

        public NodeRtc(Integer nodeId, String nodeLabel, int serviceCount, int downServiceCount, double availability) {
            this.nodeId = nodeId;
            this.nodeLabel = nodeLabel;
            this.serviceCount = serviceCount;
            this.downServiceCount = downServiceCount;
            this.availability = availability;
        }

        public Integer getNodeId() {
            return nodeId;
        }

        public String getNodeLabel() {
            return nodeLabel;
        }

        public int getServiceCount() {
            return serviceCount;
        }

        public int getDownServiceCount() {
            return downServiceCount;
        }

        public double getAvailability() {
            return availability;
        }
    }
}
