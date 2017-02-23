/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.xml.rtc.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapts the functionality of the category definition and RTC category updates
 * into one simple interface. Also adds many convenience methods.
 *
 * <p>
 * The category definition is read from the categories.xml file by the
 * {@link org.opennms.netmgt.config.CategoryFactory CategoryFactory}. The RTC
 * category updates are periodically sent from the RTC to the WebUI.
 * </p>
 * 
 * @deprecated This is awful... a 3rd Category model object that combines the other
 * two model objects???? These all need to be merged into a single object.
 *
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */

@XmlRootElement(name="category")
@XmlAccessorType(XmlAccessType.NONE)
public class Category {
    private static final Logger LOG = LoggerFactory.getLogger(Category.class);

    /** The category definition (from the categories.xml file). */
    protected final org.opennms.netmgt.config.categories.Category m_categoryDef;

    /**
     * An update from the RTC about the service level availability for this
     * category.
     */
    protected final org.opennms.netmgt.xml.rtc.Category m_rtcCategory;

    /**
     * The last time this category was updated. Note that with the current way
     * this class and the CategoryModel are implemented, this value does not
     * change because a new instance of this class is created for each RTC
     * update.
     */
    protected final Date m_lastUpdated;

    /**
     * A cached value of the total number of services on nodes belonging to this
     * category.
     */
    protected Long m_serviceCount;

    /**
     * A cached value of the total number of services on nodes belonging to this
     * category that are currently down.
     */
    protected Long m_serviceDownCount;

    /**
     * A cached value of the ratio of services that are up on notes belonging to
     * this category to all nodes belonging in this category.
     */
    protected Double m_servicePercentage;

    protected Category() {
        m_categoryDef = new org.opennms.netmgt.config.categories.Category();
        m_rtcCategory = null;
        m_lastUpdated = null;
    }

    /**
     * Create an empty category with nothing other than a name. This represents
     * a category with no RTC data.
     *
     * @param categoryName a {@link java.lang.String} object.
     */
    protected Category(String categoryName) {
        m_categoryDef = new org.opennms.netmgt.config.categories.Category();
        m_categoryDef.setLabel(categoryName);
        m_rtcCategory = null;
        m_lastUpdated = null;
    }

    /**
     * Create a new instance to wrapper information from the categories.xml file
     * (that defines a category) and information from the RTC (that gives
     * current service level availability).
     *
     * @param categoryDef a {@link org.opennms.netmgt.config.categories.Category} object.
     * @param rtcCategory a {@link org.opennms.netmgt.xml.rtc.Category} object.
     * @param lastUpdated a {@link java.util.Date} object.
     */
    protected Category(final org.opennms.netmgt.config.categories.Category categoryDef, final org.opennms.netmgt.xml.rtc.Category rtcCategory, final Date lastUpdated) {
        if (categoryDef == null || rtcCategory == null || lastUpdated == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (categoryDef.getLabel() == null || !categoryDef.getLabel().equals(rtcCategory.getCatlabel())) {
            throw new IllegalArgumentException("Cannot take category " + "definition and rtc category " + "value whose names do not " + "match.");
        }

        m_categoryDef = categoryDef;
        m_rtcCategory = rtcCategory;
        m_lastUpdated = lastUpdated;

        m_serviceCount = null;
        m_serviceDownCount = null;
        m_servicePercentage = null;
    }

    /**
     * Return the unique name for this category.
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlAttribute(name="name")
    public String getName() {
        return m_categoryDef.getLabel();
    }

    /**
     * Return the value considered to be the minimum "normal" value.
     *
     * @return a double.
     */
    @XmlAttribute(name="normal-threshold")
    public double getNormalThreshold() {
        return m_categoryDef.getNormal();
    }

    /**
     * Return the value considered to be the minimum value below the "normal"
     * value where only a warning is necessary. Below this value the category's
     * value will be considered unacceptable.
     *
     * @return a double.
     */
    @XmlAttribute(name="warning-threshold")
    public double getWarningThreshold() {
        return m_categoryDef.getWarning();
    }

    /**
     * Return a description explaining this category.
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="comment")
    public String getComment() {
        return m_categoryDef.getComment();
    }

    /**
     * Return the date and time this category was last updated by the RTC.
     *
     * @return a {@link java.util.Date} object.
     */
    @XmlElement(name="last-updated")
    public Date getLastUpdated() {
        return m_lastUpdated;
    }

    /**
     * Return the current service level availability for this category.
     *
     * @return a double.
     */
    @XmlElement(name="availability")
    public double getValue() {
        if (m_rtcCategory == null) {
            return 0.0;
        } else {
            return m_rtcCategory.getCatvalue();
        }
    }

    /**
     * Package protected implementation method that exposes the internal
     * representation (a Castor-generated object) of the data from the RTC,
     * strictly for use in marshalling the data back to XML (via Castor). In
     * other words, this method is only for debugging purposes, please do not
     * use in normal situations. Instead please use the public methods of this
     * class.
     */
    org.opennms.netmgt.xml.rtc.Category getRtcCategory() {
        return m_rtcCategory;
    }

    /**
     * Return the number of services contained within this category. This is
     * synchronized because it updates several instance variables as it executes.
     *
     * @return a long.
     */
    private synchronized long getServiceCount() {
        if (m_serviceCount == null) {
            if (m_rtcCategory == null) {
                m_serviceCount = Long.valueOf(0);
                m_serviceDownCount = Long.valueOf(0);
                m_servicePercentage = Double.valueOf(0);
            } else {
                long[] counts = getServiceCounts(m_rtcCategory);

                m_serviceCount = Long.valueOf(counts[0]);
                m_serviceDownCount = Long.valueOf(counts[1]);

                if (m_serviceCount.longValue() == 0) {
                    m_servicePercentage = Double.valueOf(100.0);
                } else {
                    m_servicePercentage = Double.valueOf(((double) (m_serviceCount.longValue() - m_serviceDownCount.longValue())) / (double) m_serviceCount.longValue() * 100.0);
                }
            }
        }

        return m_serviceCount.longValue();
    }

    /**
     * Return the number of services that are currently down with this category.
     *
     * @return a long.
     */
    @XmlElement(name="service-down-count")
    public synchronized long getServiceDownCount() {
        if (m_serviceDownCount == null) {
            // This will initialize m_serviceDownCount
            getServiceCount();
        }

        if (m_serviceDownCount == null) {
            LOG.warn("Could not fetch service down count for category: {}", m_rtcCategory.getCatlabel());
            return 0;
        } else {
            return m_serviceDownCount.longValue();
        }
    }

    /**
     * Return a percentage of the ratio of services that are up to all services
     * in this category.
     *
     * @return a double.
     */
    @XmlElement(name="service-percentage")
    public synchronized double getServicePercentage() {
        if (m_servicePercentage == null) {
            // This will initialize m_servicePercentage
            getServiceCount();
        }

        if (m_servicePercentage == null) {
            LOG.warn("Could not fetch service percentage for category: {}", m_rtcCategory.getCatlabel());
            return 0.0;
        } else {
            return m_servicePercentage.doubleValue();
        }
    }

    /**
     * Returns the outage background color for this category.
     *
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getOutageColor() throws IOException, MarshalException, ValidationException {
        if (m_lastUpdated == null) {
            return "lightblue";
        } else {
            return CategoryUtil.getCategoryColor(this, getServicePercentage());
        }
    }

    /**
     * Returns the availability background color for this category.
     *
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getAvailColor() throws IOException, MarshalException, ValidationException {
        if (m_lastUpdated == null) {
            return "lightblue";
        } else {
            return CategoryUtil.getCategoryColor(this);
        }
    }

    /**
     * Returns the outage CSS class for this category.
     *
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @XmlElement(name="outage-class")
    public String getOutageClass() throws IOException, MarshalException, ValidationException {
        if (m_lastUpdated == null) {
            return "Indeterminate";
        } else {
            return CategoryUtil.getCategoryClass(this, getServicePercentage());
        }
    }

    /**
     * Returns the availability CSS class for this category.
     *
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @XmlElement(name="availability-class")
    public String getAvailClass() throws IOException, MarshalException, ValidationException {
        if (m_lastUpdated == null) {
            return "Indeterminate";
        } else {
            return CategoryUtil.getCategoryClass(this);
        }
    }

    /**
     * Returns the outage text for this category ("X of Y" nodes down).
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="outage-text")
    public String getOutageText() {
        if (m_lastUpdated == null) {
            return "Calculating...";
        } else {
            return getServiceDownCount() + " of " + getServiceCount();
        }
    }

    /**
     * Returns the availability text for this category ("XXX.XX%").
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="availability-text")
    public String getAvailText() {
        if (m_lastUpdated == null) {
            return "Calculating...";
        } else {
            return CategoryUtil.valueFormat.format(getValue()) + "%";
        }
    }

    /**
     * Returns the category comment if there is one, otherwise, its name.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTitle() {
        if (getComment() != null) {
            return getComment();
        } else {
            return getName();
        }
    }

    @XmlElementWrapper(name="nodes")
    @XmlElement(name="node")
    public List<Long> getNodeIds() {
        final List<Long> nodeIds = new ArrayList<>();
        if (m_rtcCategory != null) {
            for (final Node node : m_rtcCategory.getNode()) {
                nodeIds.add(node.getNodeid());
            }
        }
        return nodeIds;
    }

    public List<Node> getNode() {
        return m_rtcCategory.getNode();
    }

    public NodeList getNodes() {
        if (m_rtcCategory != null) {
            return NodeList.forNodes(m_rtcCategory.getNode());
        }
        return new NodeList();
    }

    public AvailabilityNode getNode(final Long nodeId) {
        if (m_rtcCategory != null) {
            for (final Node node : m_rtcCategory.getNode()) {
                if (node.getNodeid() == nodeId) {
                    return new AvailabilityNode(node);
                }
            }
        }
        return null;
    }

    /**
     * Convenience method to count the number of services under a category and
     * the number of those services that are currently down.
     *
     * @param category a {@link org.opennms.netmgt.xml.rtc.Category} object.
     * @return an array of long.
     */
    protected static long[] getServiceCounts(final org.opennms.netmgt.xml.rtc.Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        long count = 0;
        long downCount = 0;

        for (Node node : category.getNode()) {
            count += node.getNodesvccount();
            downCount += node.getNodesvcdowncount();
        }

        return new long[] { count, downCount };
    }
}
