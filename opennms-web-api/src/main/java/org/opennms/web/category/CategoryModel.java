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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>CategoryModel class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CategoryModel extends Object {
	
	
	private static final Logger LOG = LoggerFactory.getLogger(CategoryModel.class);

    /** The name of the category that includes all services and nodes. */
    public static final String OVERALL_AVAILABILITY_CATEGORY = "Overall Service Availability";

    /** The singleton instance of this class. */
    private static CategoryModel m_instance;

    /**
     * Return the <code>CategoryModel</code>.
     *
     * @return a {@link org.opennms.web.category.CategoryModel} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized CategoryModel getInstance() throws IOException, MarshalException, ValidationException {
        if (CategoryModel.m_instance == null) {
            CategoryModel.m_instance = new CategoryModel();
        }

        return m_instance;
    }

    /** A mapping of category names to category instances. */
    private Map<String, Category> m_categoryMap = new HashMap<String, Category>();

    /** A reference to the CategoryFactory to get to category definitions. */
    private CatFactory m_factory = null;

    /**
     * Create the instance of the CategoryModel.
     */
    private CategoryModel() throws IOException, MarshalException, ValidationException {
        CategoryFactory.init();
        m_factory = CategoryFactory.getInstance();

        LOG.debug("The CategoryModel object was created");
    }

    /**
     * Return the <code>Category</code> instance for the given category name.
     * Return null if there is no match for the given name.
     *
     * @param categoryName a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.category.Category} object.
     */
    public Category getCategory(String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return m_categoryMap.get(categoryName);
    }

    /**
     * Return a mapping of category names to instances.
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Category> getCategoryMap() {
        return Collections.unmodifiableMap(new HashMap<String, Category>(m_categoryMap));
    }

    /**
     * Look up the category definition and return the category's normal
     * threshold.
     *
     * @param categoryName a {@link java.lang.String} object.
     * @return a double.
     */
    public double getCategoryNormalThreshold(String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return m_factory.getNormal(categoryName);
    }

    /**
     * Look up the category definition and return the category's warning
     * threshold.
     *
     * @param categoryName a {@link java.lang.String} object.
     * @return a double.
     */
    public double getCategoryWarningThreshold(String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return m_factory.getWarning(categoryName);
    }

    /**
     * Look up the category definition and return the category's description.
     *
     * @param categoryName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getCategoryComment(final String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String comment = null;
        m_factory.getReadLock().lock();
        try {
            org.opennms.netmgt.config.categories.Category category = m_factory.getCategory(categoryName);
    
            if (category != null) {
                comment = category.getComment();
            }
        } finally {
            m_factory.getReadLock().unlock();
        }

        return comment;
    }

    /**
     * Update a category with new values.
     *
     * @param rtcCategory a {@link org.opennms.netmgt.xml.rtc.Category} object.
     */
    public void updateCategory(final org.opennms.netmgt.xml.rtc.Category rtcCategory) {
        if (rtcCategory == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final String categoryName = rtcCategory.getCatlabel();
        
        m_factory.getWriteLock().lock();
        try {
            org.opennms.netmgt.config.categories.Category categoryDef = m_factory.getCategory(categoryName);
            org.opennms.web.category.Category category = new org.opennms.web.category.Category(categoryDef, rtcCategory, new Date());
    
            synchronized (m_categoryMap) {
                m_categoryMap.put(categoryName, category);
            }
        } finally {
            m_factory.getWriteLock().unlock();
        }

        LOG.debug("{} was updated", categoryName);
    }

    /**
     * Return the availability percentage for all managed services on the given
     * node for the last 24 hours. If there are no managed services on this
     * node, then a value of -1 is returned.
     *
     * @param nodeId a int.
     * @return a double.
     * @throws java.sql.SQLException if any.
     */
    public static double getNodeAvailability(int nodeId) throws SQLException {
        Calendar cal = new GregorianCalendar();
        Date now = cal.getTime();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        return getNodeAvailability(nodeId, yesterday, now);
    }

    /**
     * Return the availability percentage for all managed services on the given
     * node from the given start time until the given end time. If there are no
     * managed services on this node, then a value of -1 is returned.
     *
     * @param nodeId a int.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a double.
     * @throws java.sql.SQLException if any.
     */
    static double getNodeAvailability(int nodeId, Date start, Date end) throws SQLException {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (end.before(start)) {
            throw new IllegalArgumentException("Cannot have an end time before the start time.");
        }

        if (end.equals(start)) {
            throw new IllegalArgumentException("Cannot have an end time equal to the start time.");
        }

        double avail = -1;

        final DBUtils d = new DBUtils(CategoryModel.class);
        try {
            Connection conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);
            
            PreparedStatement stmt = conn.prepareStatement("select getManagePercentAvailNodeWindow(?, ?, ?) as avail");
            d.watch(stmt);

            stmt.setInt(1, nodeId);
            // yes, these are supposed to be backwards, the end time first
            stmt.setTimestamp(2, new Timestamp(end.getTime()));
            stmt.setTimestamp(3, new Timestamp(start.getTime()));

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            if (rs.next()) {
                avail = rs.getDouble("avail");
            }
        } catch (final SQLException e) {
            LOG.warn("Failed to get node availability for nodeId {}", nodeId, e);
        } finally {
            d.cleanUp();
        }

        return avail;
    }

    /**
     * Return the availability percentage for all managed services on the given
     * interface for the last 24 hours. If there are no managed services on this
     * interface, then a value of -1 is returned.
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a double.
     * @throws java.sql.SQLException if any.
     */
    public static double getInterfaceAvailability(int nodeId, String ipAddr) throws SQLException {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Calendar cal = new GregorianCalendar();
        Date now = cal.getTime();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        return getInterfaceAvailability(nodeId, ipAddr, yesterday, now);
    }

    /**
     * Return the availability percentage for all managed services on the given
     * interface from the given start time until the given end time. If there
     * are no managed services on this interface, then a value of -1 is
     * returned.
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a double.
     * @throws java.sql.SQLException if any.
     */
    static double getInterfaceAvailability(int nodeId, String ipAddr, Date start, Date end) throws SQLException {
        if (ipAddr == null || start == null || end == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (end.before(start)) {
            throw new IllegalArgumentException("Cannot have an end time before the start time.");
        }

        if (end.equals(start)) {
            throw new IllegalArgumentException("Cannot have an end time equal to the start time.");
        }

        double avail = -1;

        final DBUtils d = new DBUtils(CategoryModel.class);
        try {
            Connection conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement("select getManagePercentAvailIntfWindow(?, ?, ?, ?) as avail");
            d.watch(stmt);

            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            // yes, these are supposed to be backwards, the end time first
            stmt.setTimestamp(3, new Timestamp(end.getTime()));
            stmt.setTimestamp(4, new Timestamp(start.getTime()));

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            if (rs.next()) {
                avail = rs.getDouble("avail");
            }
        } catch (final SQLException e) {
            LOG.warn("Failed to get interface availability for nodeId {}, interface {}", nodeId, ipAddr, e);
        } finally {
            d.cleanUp();
        }

        return avail;
    }

    /**
     * Return the availability percentage for a managed service for the last 24
     * hours. If the service is not managed, then a value of -1 is returned.
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return a double.
     * @throws java.sql.SQLException if any.
     */
    public static double getServiceAvailability(int nodeId, String ipAddr, int serviceId) throws SQLException {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Calendar cal = new GregorianCalendar();
        Date now = cal.getTime();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        return getServiceAvailability(nodeId, ipAddr, serviceId, yesterday, now);
    }

    /**
     * Return the availability percentage for a managed service from the given
     * start time until the given end time. If the service is not managed, then
     * a value of -1.0 is returned.
     * 
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a double.
     * @throws java.sql.SQLException if any.
     */
    static double getServiceAvailability(int nodeId, String ipAddr, int serviceId, Date start, Date end) throws SQLException {
        if (ipAddr == null || start == null || end == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (end.before(start)) {
            throw new IllegalArgumentException("Cannot have an end time before the start time.");
        }

        if (end.equals(start)) {
            throw new IllegalArgumentException("Cannot have an end time equal to the start time.");
        }

        final DBUtils d = new DBUtils(CategoryModel.class);
        try {
            Connection conn = DataSourceFactory.getInstance().getConnection();
            d.watch(conn);
            
            PreparedStatement stmt = conn.prepareStatement("select getPercentAvailabilityInWindow(?, ?, ?, ?, ?) as avail from ifservices, ipinterface, node where ifservices.ipaddr = ipinterface.ipaddr and ifservices.nodeid = ipinterface.nodeid and ifservices.status='A' and ipinterface.ismanaged='M' and ifservices.nodeid = node.nodeid and node.nodetype='A' and ifservices.nodeid=? and ifservices.ipaddr=? and serviceid=?");
            d.watch(stmt);
            
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setInt(3, serviceId);
            // yes, these are supposed to be backwards, the end time first
            stmt.setTimestamp(4, new Timestamp(end.getTime()));
            stmt.setTimestamp(5, new Timestamp(start.getTime()));
            stmt.setInt(6, nodeId);
            stmt.setString(7, ipAddr);
            stmt.setInt(8, serviceId);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            if (rs.next()) {
                return rs.getDouble("avail");
            }
        } catch (final SQLException e) {
            LOG.warn("Failed to get service availability for nodeId {}, interface {}, serviceId {}", nodeId, ipAddr, serviceId, e);
        } finally {
            d.cleanUp();
        }

        return -1.0;
    }
}
