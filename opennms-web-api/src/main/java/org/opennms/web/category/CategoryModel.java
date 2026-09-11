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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.CategoryGroup;
import org.opennms.netmgt.dao.api.CategoryAvailabilitySnapshotDao;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;
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
     */
    public static synchronized CategoryModel getInstance() throws IOException {
        if (CategoryModel.m_instance == null) {
            CategoryModel.m_instance = new CategoryModel();
        }

        return m_instance;
    }

    /** Explicit snapshot store for tests; null means look it up in the DAO context on every call. */
    private final CategoryAvailabilitySnapshotDao m_snapshotDao;

    /** A reference to the CategoryFactory to get to category definitions. */
    private final CatFactory m_factory;

    /**
     * Create the instance of the CategoryModel.
     */
    private CategoryModel() throws IOException {
        CategoryFactory.init();
        m_factory = CategoryFactory.getInstance();
        m_snapshotDao = null;

        LOG.debug("The CategoryModel object was created");
    }

    /** For tests: build a model on explicit collaborators. */
    CategoryModel(final CatFactory factory, final CategoryAvailabilitySnapshotDao snapshotDao) {
        m_factory = factory;
        m_snapshotDao = snapshotDao;
    }

    /**
     * The snapshot store. Looked up on every call rather than cached: the
     * lookup is a map access against the current Spring context, and caching
     * it would pin this singleton to a context that may since have been
     * replaced.
     */
    private CategoryAvailabilitySnapshotDao snapshotDao() {
        if (m_snapshotDao != null) {
            return m_snapshotDao;
        }
        return BeanUtils.getBean("daoContext", "categoryAvailabilitySnapshotDao", CategoryAvailabilitySnapshotDao.class);
    }

    private org.opennms.netmgt.config.categories.Category definition(final String categoryName) {
        m_factory.getReadLock().lock();
        try {
            return m_factory.getCategory(categoryName);
        } finally {
            m_factory.getReadLock().unlock();
        }
    }

    /**
     * Return the <code>Category</code> for the given name with its headline
     * figures but without its node list. Returns null when categories.xml does
     * not define a category of that name. A defined category without a
     * snapshot yet is returned with no data.
     */
    public Category getCategory(final String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        final org.opennms.netmgt.config.categories.Category def = definition(categoryName);
        if (def == null) {
            return null;
        }
        return new Category(def, snapshotDao().findSummary(categoryName).orElse(null));
    }

    /**
     * Like {@link #getCategory(String)} but with every member node loaded.
     * Prefer {@link #getCategoryNodes(String, int, int)} for large categories.
     */
    public Category getCategoryWithNodes(final String categoryName) {
        if (categoryName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        final org.opennms.netmgt.config.categories.Category def = definition(categoryName);
        if (def == null) {
            return null;
        }
        return new Category(def, snapshotDao().findWithNodes(categoryName).orElse(null));
    }

    /**
     * A page of a category's member nodes sorted by node ID, with the total
     * count and offset set on the returned list.
     *
     * @param limit maximum nodes to return; zero or negative returns them all
     */
    public NodeList getCategoryNodes(final String categoryName, final int offset, final int limit) {
        final Optional<CategoryAvailability> summary = snapshotDao().findSummary(categoryName);
        final NodeList nodes = NodeList.forNodes(summary.isPresent() ? snapshotDao().findNodes(categoryName, offset, limit) : Collections.emptyList());
        nodes.setTotalCount((int) summary.map(CategoryAvailability::getNodeCount).orElse(0L).longValue());
        nodes.setOffset(offset);
        return nodes;
    }

    /** One member node of a category, or null if the node is not in it. */
    public AvailabilityNode getCategoryNode(final String categoryName, final long nodeId) {
        final long windowMillis = snapshotDao().findSummary(categoryName).map(CategoryAvailability::getWindowMillis).orElse(0L);
        if (windowMillis <= 0) {
            return null;
        }
        return snapshotDao().findNode(categoryName, (int) nodeId).map(AvailabilityNode::new).orElse(null);
    }

    /**
     * Return a mapping of category names to instances for every category
     * defined in categories.xml, each with its headline figures if a snapshot
     * exists.
     */
    public Map<String, Category> getCategoryMap() {
        final Map<String, CategoryAvailability> summaries = new HashMap<>();
        for (final CategoryAvailability summary : snapshotDao().findAllSummaries()) {
            summaries.put(summary.getLabel(), summary);
        }
        final Map<String, Category> categories = new HashMap<>();
        m_factory.getReadLock().lock();
        try {
            for (final CategoryGroup group : m_factory.getConfig().getCategoryGroups()) {
                for (final org.opennms.netmgt.config.categories.Category def : group.getCategories()) {
                    categories.put(def.getLabel(), new Category(def, summaries.get(def.getLabel())));
                }
            }
        } finally {
            m_factory.getReadLock().unlock();
        }
        return Collections.unmodifiableMap(categories);
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
                comment = category.getComment().orElse(null);
            }
        } finally {
            m_factory.getReadLock().unlock();
        }

        return comment;
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
            
            PreparedStatement stmt = conn.prepareStatement("select getPercentAvailabilityInWindow(ifservices.id, ?, ?) as avail from ifservices, ipinterface, node where ifservices.ipInterfaceId = ipinterface.id and ipInterface.nodeid = node.nodeid and ifservices.status='A' and ipinterface.ismanaged='M' and node.nodetype='A' and node.nodeid=? and ipInterface.ipaddr=? and ifServices.serviceid=?");
            d.watch(stmt);
            
            // yes, these are supposed to be backwards, the end time first
            stmt.setTimestamp(1, new Timestamp(end.getTime()));
            stmt.setTimestamp(2, new Timestamp(start.getTime()));
            stmt.setInt(3, nodeId);
            stmt.setString(4, ipAddr);
            stmt.setInt(5, serviceId);

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
