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
package org.opennms.netmgt.model.availability;

import java.io.Serializable;
import java.util.Objects;

/**
 * Availability of one node within a category over a time window: how many
 * services of the node the category covers, how many of them are down right
 * now, and how much service downtime accrued inside the window.
 *
 * The availability percentage treats every covered service as equally
 * weighted: it is the share of the window during which the services, taken
 * together, were up.
 */
public final class NodeAvailability implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int m_nodeId;
    private final long m_serviceCount;
    private final long m_servicesDown;
    private final long m_downtimeMillis;
    private final double m_availability;

    /**
     * @param nodeId         the node ID
     * @param serviceCount   number of covered services on the node
     * @param servicesDown   number of covered services with an open outage
     * @param downtimeMillis summed service downtime inside the window, in milliseconds
     * @param windowMillis   length of the window, in milliseconds
     */
    public NodeAvailability(final int nodeId, final long serviceCount, final long servicesDown, final long downtimeMillis, final long windowMillis) {
        m_nodeId = nodeId;
        m_serviceCount = serviceCount;
        m_servicesDown = servicesDown;
        m_downtimeMillis = downtimeMillis;
        m_availability = percentage(downtimeMillis, windowMillis, serviceCount);
    }

    /**
     * Availability percentage for a set of services over a window.
     *
     * @param downtimeMillis summed service downtime inside the window
     * @param windowMillis   length of the window
     * @param serviceCount   number of services
     * @return a value between 0.0 and 100.0; 100.0 when there are no services
     */
    public static double percentage(final long downtimeMillis, final long windowMillis, final long serviceCount) {
        if (serviceCount <= 0 || windowMillis <= 0) {
            return 100.0;
        }
        final double total = (double) windowMillis * (double) serviceCount;
        final double value = 100.0 * (1.0 - ((double) downtimeMillis / total));
        return Math.max(0.0, Math.min(100.0, value));
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public long getServiceCount() {
        return m_serviceCount;
    }

    public long getServicesDown() {
        return m_servicesDown;
    }

    public long getDowntimeMillis() {
        return m_downtimeMillis;
    }

    public double getAvailability() {
        return m_availability;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeAvailability)) return false;
        final NodeAvailability that = (NodeAvailability) o;
        return m_nodeId == that.m_nodeId
                && m_serviceCount == that.m_serviceCount
                && m_servicesDown == that.m_servicesDown
                && m_downtimeMillis == that.m_downtimeMillis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_nodeId, m_serviceCount, m_servicesDown, m_downtimeMillis);
    }

    @Override
    public String toString() {
        return "NodeAvailability[nodeId=" + m_nodeId
                + ", serviceCount=" + m_serviceCount
                + ", servicesDown=" + m_servicesDown
                + ", downtimeMillis=" + m_downtimeMillis
                + ", availability=" + m_availability + "]";
    }
}
