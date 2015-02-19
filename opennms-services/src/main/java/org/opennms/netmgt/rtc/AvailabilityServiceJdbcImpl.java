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
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
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

	@Override
	public Collection<Integer> getNodes(RTCCategory category) {
		// Refresh the list of nodes contained inside the RTCCategory
		category.clearNodes();
		category.addAllNodes(RTCUtils.getNodeIdsForCategory(m_filterDao,  category));
		return category.getNodes();
	}

	/**
	 * Get the value (uptime) for the category in the last 'rollingWindow'
	 * starting at current time
	 *
	 * @param catLabel
	 *            the category to which the node should belong to
	 * @param curTime
	 *            the current time
	 * @param rollingWindow
	 *            the window for which value is to be calculated
	 * @return the value(uptime) for the category in the last 'rollingWindow'
	 *         starting at current time
	 */
	@Override
	public double getValue(RTCCategory category, long curTime, long rollingWindow) {
		double outageTime = 0.0;
		Criteria criteria = createServiceCriteriaForCategory(category);
		List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(criteria);
		for (OnmsMonitoredService service : services) {
			try {
				outageTime += RTCUtils.getOutageTimeInWindow(service.getNodeId(), service.getIpAddressAsString(), service.getServiceId(), new Date(curTime - rollingWindow), new Date(curTime));
			} catch (SQLException e) {
				LOG.warn("Cannot calculate availability for service " + service.toString() + ": " + e.getMessage(), e);
			}
		}
		return RTCUtils.getOutagePercentage(outageTime, rollingWindow, services.size());
	}

	/**
	 * Get the value (uptime) for the nodeid in the last 'rollingWindow' starting
	 * at current time in the context of the passed category
	 *
	 * @param nodeid
	 *            the node for which value is to be calculated
	 * @param catLabel
	 *            the category to which the node should belong to
	 * @param curTime
	 *            the current time
	 * @param rollingWindow
	 *            the window for which value is to be calculated
	 * @return the value(uptime) for the node in the last 'rollingWindow'
	 *         starting at current time in the context of the passed category
	 */
	@Override
	public double getValue(int nodeid, RTCCategory category, long curTime, long rollingWindow) {
		double outageTime = 0.0;
		Criteria criteria = createServiceCriteriaForNodeInCategory(nodeid, category);
		List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(criteria);
		for (OnmsMonitoredService service : services) {
			try {
				outageTime += RTCUtils.getOutageTimeInWindow(service.getNodeId(), service.getIpAddressAsString(), service.getServiceId(), new Date(curTime - rollingWindow), new Date(curTime));
			} catch (SQLException e) {
				LOG.warn("Cannot calculate availability for service " + service.toString() + ": " + e.getMessage(), e);
			}
		}
		return RTCUtils.getOutagePercentage(outageTime, rollingWindow, services.size());
	}

	/**
	 * Get the service count for the nodeid in the context of the passed
	 * category
	 *
	 * @param nodeid
	 *            the node for which service count is to be calculated
	 * @param catLabel
	 *            the category to which the node should belong to
	 * @return the service count for the nodeid in the context of the passed
	 *         category
	 */
	@Override
	public int getServiceCount(int nodeid, RTCCategory category) {
		Criteria criteria = createServiceCriteriaForNodeInCategory(nodeid, category);
		return m_monitoredServiceDao.countMatching(criteria);
	}

	/**
	 * Get the service down count for the nodeid in the context of the passed
	 * category
	 *
	 * @param nodeid
	 *            the node for which service down count is to be calculated
	 * @param catLabel
	 *            the category to which the node should belong to
	 * @return the service down count for the nodeid in the context of the
	 *         passed category
	 */
	@Override
	public int getServiceDownCount(int nodeid, RTCCategory category) {
		int retval = 0;
		Criteria criteria = createServiceCriteriaForNodeInCategory(nodeid, category);
		for (OnmsMonitoredService service : m_monitoredServiceDao.findMatching(criteria)) {
			if (m_outageDao.currentOutageForService(service) != null) {
				retval++;
			}
		}
		return retval;
	}

	/**
	 * <p>getCategories</p>
	 *
	 * @return the categories
	 */
	@Override
	public Map<String, RTCCategory> getCategories() {
		return RTCUtils.createCategoriesMap();
	}

	private Criteria createServiceCriteriaForCategory(RTCCategory category) {
		CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoredService.class);

		Set<Integer> nodes = RTCUtils.getNodeIdsForCategory(m_filterDao, category);
		if (nodes != null && nodes.size() > 0) {
			builder.alias("ipInterface", "ipInterface")
			.alias("ipInterface.node", "node")
			.in("node.id", nodes);
		}

		List<String> services = category.getServiceCollection();
		if (services != null && services.size() > 0) {
			builder.alias("serviceType", "serviceType")
			.in("serviceType.name", category.getServiceCollection());
		}

		return builder.toCriteria();
	}

	private Criteria createServiceCriteriaForNodeInCategory(int nodeId, RTCCategory category) {
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
}
