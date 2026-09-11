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
package org.opennms.web.category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;

/**
 * A category as the web tier presents it: the definition from categories.xml
 * (name, comment, thresholds) together with the most recent availability
 * snapshot, if one has been computed yet.
 *
 * The node list is only present when the category was loaded with nodes; on
 * a summary it is omitted from the marshalled output rather than sent empty,
 * because a category can contain every node in the system.
 */
@XmlRootElement(name="category")
@XmlAccessorType(XmlAccessType.NONE)
public class Category {

    /** The category definition (from the categories.xml file). */
    protected final org.opennms.netmgt.config.categories.Category m_categoryDef;

    /** The most recent snapshot, or null when none has been computed. */
    protected final CategoryAvailability m_availability;

    protected Category() {
        m_categoryDef = new org.opennms.netmgt.config.categories.Category();
        m_availability = null;
    }

    /**
     * Create an empty category with nothing other than a name. This represents
     * a category that is displayed but not defined in categories.xml.
     */
    protected Category(final String categoryName) {
        m_categoryDef = new org.opennms.netmgt.config.categories.Category();
        m_categoryDef.setLabel(categoryName);
        m_categoryDef.setNormalThreshold(0d);
        m_categoryDef.setWarningThreshold(0d);
        m_availability = null;
    }

    /**
     * Wrap a definition from categories.xml and its snapshot.
     *
     * @param categoryDef  the definition, required
     * @param availability the snapshot, or null when none has been computed yet
     */
    protected Category(final org.opennms.netmgt.config.categories.Category categoryDef, final CategoryAvailability availability) {
        if (categoryDef == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        if (availability != null && (categoryDef.getLabel() == null || !categoryDef.getLabel().equals(availability.getLabel()))) {
            throw new IllegalArgumentException("Category definition '" + categoryDef.getLabel() + "' does not match snapshot '" + availability.getLabel() + "'");
        }
        m_categoryDef = categoryDef;
        m_availability = availability;
    }

    /** Return the unique name for this category. */
    @XmlAttribute(name="name")
    public String getName() {
        return m_categoryDef == null ? null : m_categoryDef.getLabel();
    }

    /** Return the value considered to be the minimum "normal" value. */
    @XmlAttribute(name="normal-threshold")
    public double getNormalThreshold() {
        final var ret = m_categoryDef == null ? null : m_categoryDef.getNormalThreshold();
        return ret == null ? 0 : ret;
    }

    /**
     * Return the value considered to be the minimum value below the "normal"
     * value where only a warning is necessary. Below this value the category's
     * value will be considered unacceptable.
     */
    @XmlAttribute(name="warning-threshold")
    public double getWarningThreshold() {
        final var ret = m_categoryDef == null ? null : m_categoryDef.getWarningThreshold();
        return ret == null ? 0 : ret;
    }

    /** Return a description explaining this category. */
    @XmlElement(name="comment")
    public String getComment() {
        return m_categoryDef == null ? null : m_categoryDef.getComment().orElse(null);
    }

    /** When the snapshot was computed, or null when none exists. */
    @XmlElement(name="last-updated")
    public Date getLastUpdated() {
        return m_availability == null ? null : m_availability.getComputedAt();
    }

    /** Length of the window the snapshot covers in milliseconds, or null when none exists. */
    public Long getWindowMillis() {
        return m_availability == null ? null : m_availability.getWindowMillis();
    }

    /** Whether a snapshot has been computed for this category. */
    public boolean hasData() {
        return m_availability != null;
    }

    /** The snapshot, or null. */
    public CategoryAvailability getAvailabilityData() {
        return m_availability;
    }

    /** Return the current service level availability for this category. */
    @XmlElement(name="availability")
    public double getValue() {
        return m_availability == null ? 0.0 : m_availability.getAvailability();
    }

    /** Return the number of services contained within this category. */
    public long getServiceCount() {
        return m_availability == null ? 0 : m_availability.getServiceCount();
    }

    /** Return the number of services that are currently down within this category. */
    @XmlElement(name="service-down-count")
    public long getServiceDownCount() {
        return m_availability == null ? 0 : m_availability.getServicesDown();
    }

    /** Return a percentage of the ratio of services that are up to all services in this category. */
    @XmlElement(name="service-percentage")
    public double getServicePercentage() {
        if (m_availability == null) {
            return 0.0;
        }
        final long count = m_availability.getServiceCount();
        if (count == 0) {
            return 100.0;
        }
        return ((double) (count - m_availability.getServicesDown())) / (double) count * 100.0;
    }

    /** Returns the outage CSS class for this category. */
    @XmlElement(name="outage-class")
    public String getOutageClass() throws IOException {
        if (m_availability == null) {
            return "Indeterminate";
        }
        return CategoryUtil.getCategoryClass(this, getServicePercentage());
    }

    /** Returns the availability CSS class for this category. */
    @XmlElement(name="availability-class")
    public String getAvailClass() throws IOException {
        if (m_availability == null) {
            return "Indeterminate";
        }
        return CategoryUtil.getCategoryClass(this);
    }

    /** Returns the outage text for this category ("X of Y" services down). */
    @XmlElement(name="outage-text")
    public String getOutageText() {
        if (m_availability == null) {
            return "Calculating...";
        }
        return getServiceDownCount() + " of " + getServiceCount();
    }

    /** Returns the availability text for this category ("XXX.XX%"). */
    @XmlElement(name="availability-text")
    public String getAvailText() {
        if (m_availability == null) {
            return "Calculating...";
        }
        return CategoryUtil.valueFormat.format(getValue()) + "%";
    }

    /** Returns the category comment if there is one, otherwise, its name. */
    public String getTitle() {
        return getComment() != null ? getComment() : getName();
    }

    /** Number of nodes in the category, known even when the nodes are not loaded; null without a snapshot. */
    @XmlElement(name="node-count")
    public Long getNodeCount() {
        return m_availability == null ? null : m_availability.getNodeCount();
    }

    /**
     * IDs of the member nodes, or null when the category was loaded without
     * its nodes so that the element is omitted from the output.
     */
    @XmlElementWrapper(name="nodes")
    @XmlElement(name="node")
    public List<Long> getNodeIds() {
        if (m_availability == null || !m_availability.hasNodes()) {
            return null;
        }
        final List<Long> nodeIds = new ArrayList<>();
        for (final NodeAvailability node : m_availability.getNodes()) {
            nodeIds.add((long) node.getNodeId());
        }
        return nodeIds;
    }

    /** Per-node figures, empty unless the category was loaded with its nodes. */
    public List<NodeAvailability> getNodeAvailabilities() {
        return m_availability == null ? Collections.emptyList() : m_availability.getNodes();
    }

    /** Per-node figures as REST nodes, empty unless the category was loaded with its nodes. */
    public NodeList getNodes() {
        return NodeList.forNodes(getNodeAvailabilities());
    }

    /** One loaded member node, or null if it is not in the loaded list. */
    public AvailabilityNode getNode(final Long nodeId) {
        if (nodeId == null) {
            return null;
        }
        for (final NodeAvailability node : getNodeAvailabilities()) {
            if (node.getNodeId() == nodeId.longValue()) {
                return new AvailabilityNode(node);
            }
        }
        return null;
    }
}
