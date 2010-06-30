//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All
// rights reserved.
// OpenNMS(R) is a derivative work, containing both original code,
// included code and modified code that was published under the GNU
// General Public License. Copyrights for modified and included code
// are below.
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

package org.opennms.web.category;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

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
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public class Category {
    /** The category definition (from the categories.xml file). */
    protected org.opennms.netmgt.config.categories.Category m_categoryDef;

    /**
     * An update from the RTC about the service level availability for this
     * category.
     */
    protected org.opennms.netmgt.xml.rtc.Category m_rtcCategory;

    /**
     * The last time this category was updated. Note that with the current way
     * this class and the CategoryModel are implemented, this value does not
     * change because a new instance of this class is created for each RTC
     * update.
     */
    protected Date m_lastUpdated;

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
     * A cached value of the ratio of services that are up on notes beloging to
     * this category to all nodes belonging in this category.
     */
    protected Double m_servicePercentage;

    /**
     * Create an empty category with nothing other than a name. This represents
     * a category with no RTC data.
     *
     * @param categoryName a {@link java.lang.String} object.
     */
    protected Category(String categoryName) {
        m_categoryDef = new org.opennms.netmgt.config.categories.Category();
        m_categoryDef.setLabel(categoryName);
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
    protected Category(org.opennms.netmgt.config.categories.Category categoryDef, org.opennms.netmgt.xml.rtc.Category rtcCategory, Date lastUpdated) {
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
    }

    /**
     * Return the unique name for this category.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_categoryDef.getLabel();
    }

    /**
     * Return the value considered to be the minimum "normal" value.
     *
     * @return a double.
     */
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
    public double getWarningThreshold() {
        return m_categoryDef.getWarning();
    }

    /**
     * Return a description explaining this category.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComment() {
        return m_categoryDef.getComment();
    }

    /**
     * Return the date and time this category was last updated by the RTC.
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getLastUpdated() {
        return m_lastUpdated;
    }

    /**
     * Return the current service level availability for this category.
     *
     * @return a double.
     */
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
     * Return the number of services contained within this category.
     *
     * @return a long.
     */
    public long getServiceCount() {
        if (m_serviceCount == null) {
            if (m_rtcCategory == null) {
                m_serviceCount = new Long(0);
                m_serviceDownCount = new Long(0);
                m_servicePercentage = new Double(0);
            } else {
                long[] counts = getServiceCounts(m_rtcCategory);

                m_serviceCount = new Long(counts[0]);
                m_serviceDownCount = new Long(counts[1]);

                if (m_serviceCount.longValue() == 0) {
                    m_servicePercentage = new Double(100.0);
                } else {
                    m_servicePercentage = new Double(((double) (m_serviceCount.longValue() - m_serviceDownCount.longValue())) / (double) m_serviceCount.longValue() * 100.0);
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
    public long getServiceDownCount() {
        if (m_serviceDownCount == null) {
            // This will initialize m_serviceDownCount
            getServiceCount();
        }

        return m_serviceDownCount.longValue();
    }

    /**
     * Return a percentage of the ratio of services that are up to all services
     * in this category.
     *
     * @return a double.
     */
    public double getServicePercentage() {
        if (m_servicePercentage == null) {
            // This will initialize m_servicePercentage
            getServiceCount();
        }

        return m_servicePercentage.doubleValue();
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
    public String getOutageClass() throws IOException, MarshalException, ValidationException {
        if (m_lastUpdated == null) {
            return "lightblue";
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
    public String getAvailClass() throws IOException, MarshalException, ValidationException {
        if (m_lastUpdated == null) {
            return "lightblue";
        } else {
            return CategoryUtil.getCategoryClass(this);
        }
    }

    /**
     * Returns the outage text for this category ("X of Y" nodes down).
     *
     * @return a {@link java.lang.String} object.
     */
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

    /**
     * Returns an enumeration of the Castor-generated Node objects tied to this
     * category.
     *
     * <p>
     * Note, LJK Dec 5,2001: I'm not really happy about exposing the Castor
     * objects this way. We do it all over the place, but I've already started
     * hiding them in this particular case (the rtceui.xsd objects). I'm not
     * very pleased with this half approach. I'd rather hide them completely or
     * not at all, but I don't want to introduce a new pass-through object.
     * </p>
     *
     * @return a {@link java.util.Enumeration} object.
     */
    public Enumeration enumerateNode() {
        return m_rtcCategory.enumerateNode();
    }

    /**
     * Convenience method to count the number of services under a category and
     * the number of those services that are currently down.
     *
     * @param category a {@link org.opennms.netmgt.xml.rtc.Category} object.
     * @return an array of long.
     */
    protected static long[] getServiceCounts(org.opennms.netmgt.xml.rtc.Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        long count = 0;
        long downCount = 0;

        Enumeration nodeEnum = category.enumerateNode();

        while (nodeEnum.hasMoreElements()) {
            org.opennms.netmgt.xml.rtc.Node node = (org.opennms.netmgt.xml.rtc.Node) nodeEnum.nextElement();

            count += node.getNodesvccount();
            downCount += node.getNodesvcdowncount();
        }

        return new long[] { count, downCount };
    }
}
