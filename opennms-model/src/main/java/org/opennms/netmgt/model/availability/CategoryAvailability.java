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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Availability of one category over a time window, with the per-node
 * breakdown it was computed from. Category totals are the sums of the node
 * figures, and the category percentage weights every covered service equally.
 *
 * Instances are immutable. Nodes are held sorted by node ID. A summary
 * instance created through {@link #withTotals} carries the stored totals and
 * node count but no node list, for callers that only need the headline
 * figures of a very large category.
 */
public final class CategoryAvailability implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String m_label;
    private final Date m_windowStart;
    private final Date m_windowEnd;
    private final Date m_computedAt;
    private final List<NodeAvailability> m_nodes;
    private final long m_nodeCount;
    private final long m_serviceCount;
    private final long m_servicesDown;
    private final long m_downtimeMillis;
    private final double m_availability;

    public CategoryAvailability(final String label, final Date windowStart, final Date windowEnd, final Date computedAt, final List<NodeAvailability> nodes) {
        m_label = Objects.requireNonNull(label, "label");
        m_windowStart = new Date(Objects.requireNonNull(windowStart, "windowStart").getTime());
        m_windowEnd = new Date(Objects.requireNonNull(windowEnd, "windowEnd").getTime());
        if (!m_windowStart.before(m_windowEnd)) {
            throw new IllegalArgumentException("windowStart must be before windowEnd");
        }
        m_computedAt = new Date(Objects.requireNonNull(computedAt, "computedAt").getTime());

        final List<NodeAvailability> sorted = new ArrayList<>(Objects.requireNonNull(nodes, "nodes"));
        sorted.sort(Comparator.comparingInt(NodeAvailability::getNodeId));
        m_nodes = Collections.unmodifiableList(sorted);

        long serviceCount = 0;
        long servicesDown = 0;
        long downtime = 0;
        for (final NodeAvailability node : m_nodes) {
            serviceCount += node.getServiceCount();
            servicesDown += node.getServicesDown();
            downtime += node.getDowntimeMillis();
        }
        m_nodeCount = m_nodes.size();
        m_serviceCount = serviceCount;
        m_servicesDown = servicesDown;
        m_downtimeMillis = downtime;
        m_availability = NodeAvailability.percentage(downtime, getWindowMillis(), serviceCount);
    }

    private CategoryAvailability(final String label, final Date windowStart, final Date windowEnd, final Date computedAt,
            final long nodeCount, final long serviceCount, final long servicesDown, final long downtimeMillis) {
        m_label = Objects.requireNonNull(label, "label");
        m_windowStart = new Date(Objects.requireNonNull(windowStart, "windowStart").getTime());
        m_windowEnd = new Date(Objects.requireNonNull(windowEnd, "windowEnd").getTime());
        if (!m_windowStart.before(m_windowEnd)) {
            throw new IllegalArgumentException("windowStart must be before windowEnd");
        }
        m_computedAt = new Date(Objects.requireNonNull(computedAt, "computedAt").getTime());
        m_nodes = Collections.emptyList();
        m_nodeCount = nodeCount;
        m_serviceCount = serviceCount;
        m_servicesDown = servicesDown;
        m_downtimeMillis = downtimeMillis;
        m_availability = NodeAvailability.percentage(downtimeMillis, getWindowMillis(), serviceCount);
    }

    /**
     * Create a summary that carries stored totals without the node list.
     * {@link #getNodes()} on the result is empty while {@link #getNodeCount()}
     * reports the real number of member nodes.
     */
    public static CategoryAvailability withTotals(final String label, final Date windowStart, final Date windowEnd, final Date computedAt,
            final long nodeCount, final long serviceCount, final long servicesDown, final long downtimeMillis) {
        return new CategoryAvailability(label, windowStart, windowEnd, computedAt, nodeCount, serviceCount, servicesDown, downtimeMillis);
    }

    public String getLabel() {
        return m_label;
    }

    public Date getWindowStart() {
        return new Date(m_windowStart.getTime());
    }

    public Date getWindowEnd() {
        return new Date(m_windowEnd.getTime());
    }

    public long getWindowMillis() {
        return m_windowEnd.getTime() - m_windowStart.getTime();
    }

    public Date getComputedAt() {
        return new Date(m_computedAt.getTime());
    }

    /**
     * Nodes in the category, sorted by node ID. Never null, but empty on a
     * summary created with {@link #withTotals}; compare with {@link #getNodeCount()}.
     */
    public List<NodeAvailability> getNodes() {
        return m_nodes;
    }

    /** Number of member nodes, whether or not they are loaded. */
    public long getNodeCount() {
        return m_nodeCount;
    }

    /** True when {@link #getNodes()} holds every member node. */
    public boolean hasNodes() {
        return m_nodeCount == 0 || !m_nodes.isEmpty();
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
    public String toString() {
        return "CategoryAvailability[label=" + m_label
                + ", window=" + m_windowStart + ".." + m_windowEnd
                + ", nodeCount=" + m_nodeCount
                + ", nodesLoaded=" + m_nodes.size()
                + ", serviceCount=" + m_serviceCount
                + ", servicesDown=" + m_servicesDown
                + ", downtimeMillis=" + m_downtimeMillis
                + ", availability=" + m_availability + "]";
    }
}
