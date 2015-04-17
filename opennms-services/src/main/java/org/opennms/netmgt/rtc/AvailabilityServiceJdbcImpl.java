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

package org.opennms.netmgt.rtc;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.netmgt.xml.rtc.Category;
import org.opennms.netmgt.xml.rtc.EuiLevel;
import org.opennms.netmgt.xml.rtc.Header;
import org.opennms.netmgt.xml.rtc.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This implementation of the {@link AvailabilityService} uses database calls
 * and stored procedures to calculate the availability percentages.
 * 
 * @author Seth
 */
public class AvailabilityServiceJdbcImpl implements AvailabilityService {

	private static final Logger LOG = LoggerFactory.getLogger(AvailabilityServiceJdbcImpl.class);

	@Autowired
	private FilterDao m_filterDao;

	@Autowired
	private MonitoredServiceDao m_monitoredServiceDao;

	@Autowired
	private OutageDao m_outageDao;

	@Autowired
	private JdbcTemplate m_jdbcTemplate;

    /**
     * Builds a map of configured categories, keyed by label.
     *
     * @return the categories
     */
	@Override
	public Map<String, RTCCategory> getCategories() {
		return RTCUtils.createCategoriesMap();
	}

    /**
     * Optimized method for calculating the category statistics
     * that aims to reduce the number of calls to the database.
     *
     * In its current form, getOutageTimeInWindow() is called for
     * every service in the category, which is not ideal.
     */
    @Override
    public synchronized EuiLevel getEuiLevel(RTCCategory category) {
        final Header header = new Header();
        header.setVer("1.9a");
        header.setMstation("");

        // current time
        final Date curDate = new Date();
        final long curTime = curDate.getTime();

        // get the rolling window
        final long rWindow = 24L * 60L * 60L * 1000L;

        LOG.debug("Retrieving availability statistics for {} with current date: {} and rolling window: {}",
                    category.getLabel(), curDate, rWindow);

        // create the data
        final EuiLevel level = new EuiLevel();

        // set created in m_header and add to level
        header.setCreated(EventConstants.formatToString(curDate));
        level.setHeader(header);

        final Category levelCat = new Category();

        // category label
        levelCat.setCatlabel(category.getLabel());

        double outageTimeInCategory = 0.0;
        int numServicesInCategory = 0;

        final Date windowStart = new Date(curTime - rWindow);
        final Date windowEnd = new Date(curTime);

        // nodes in this category
        for (final int nodeID : getNodes(category)) {

            // find the list of monitored services for this node
            final Criteria criteria = createServiceCriteriaForNodeInCategory(nodeID, category);
            final List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(criteria);

            // sum the outage time for all of the node's services
            final double outageTime = services.stream()
                    .mapToDouble(service -> getOutageTimeInWindow(service, windowStart, windowEnd))
                    .sum();

            // count the number of services that are currently down
            final long numServicesDown = services.stream()
                    .filter(service -> m_outageDao.currentOutageForService(service) != null)
                    .count();

            final Node levelNode = new Node();
            levelNode.setNodeid(nodeID);

            // value for this node for this category
            levelNode.setNodevalue(RTCUtils.getOutagePercentage(outageTime, rWindow, services.size()));

            // node service count
            levelNode.setNodesvccount(services.size());

            // node service down count
            levelNode.setNodesvcdowncount(numServicesDown);

            // add the node
            levelCat.addNode(levelNode);

            // update the category statistics
            numServicesInCategory += services.size();
            outageTimeInCategory += outageTime;
        }

        // calculate the outage percentage using tallied values
        levelCat.setCatvalue(RTCUtils.getOutagePercentage(outageTimeInCategory, rWindow, numServicesInCategory));

        // add category
        level.addCategory(levelCat);

        LOG.debug("Done retrieving availability statistics for {} with {} services.", category.getLabel(), numServicesInCategory);

        return level;
    }

    private Collection<Integer> getNodes(RTCCategory category) {
        // Refresh the list of nodes contained inside the RTCCategory
        category.clearNodes();
        category.addAllNodes(RTCUtils.getNodeIdsForCategory(m_filterDao,  category));
        return category.getNodes();
    }

	private static Criteria createServiceCriteriaForNodeInCategory(int nodeId, RTCCategory category) {
		CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoredService.class)
		.alias("ipInterface", "ipInterface")
		.alias("ipInterface.node", "node")
		.eq("node.id", nodeId); // Add an extra restriction on the node ID

		/*
		 * NOTE: This assumes that the category contains the current list of nodes.
		 * This value is added as a side-effect of calling {@link #getNodes(RTCCategory)}.
		 */
		Collection<Integer> nodes = category.getNodes();
		if (nodes != null && nodes.size() > 0) {
			builder.in("node.id", nodes);
		}

		List<String> services = category.getServiceCollection();
		if (services != null && services.size() > 0) {
			builder.alias("serviceType", "serviceType")
			.in("serviceType.name", category.getServiceCollection());
		}

		return builder.toCriteria();
	}

    private static double getOutageTimeInWindow(final OnmsMonitoredService service, final Date start, final Date end) {
        try {
            return RTCUtils.getOutageTimeInWindow(service.getNodeId(), service.getIpAddressAsString(), service.getServiceId(), start, end);
        } catch (SQLException e) {
            LOG.warn("Cannot calculate availability for service {}", service, e);
            return 0.0d;
        }
    }
}
