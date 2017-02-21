/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.AllRestriction;
import org.opennms.core.criteria.restrictions.GtRestriction;
import org.opennms.core.criteria.restrictions.LeRestriction;
import org.opennms.core.criteria.restrictions.NullRestriction;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.netmgt.xml.rtc.Category;
import org.opennms.netmgt.xml.rtc.EuiLevel;
import org.opennms.netmgt.xml.rtc.Header;
import org.opennms.netmgt.xml.rtc.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This implementation of the {@link AvailabilityService} uses database calls
 * and stored procedures to calculate the availability percentages.
 * 
 * @author Seth
 * @author Jesse White <jesse@opennms.org>
 */
public class AvailabilityServiceHibernateImpl implements AvailabilityService {

	private static final Logger LOG = LoggerFactory.getLogger(AvailabilityServiceHibernateImpl.class);

	@Autowired
	private FilterDao m_filterDao;

	@Autowired
	private MonitoredServiceDao m_monitoredServiceDao;

	@Autowired
	private OutageDao m_outageDao;

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
     * Optimized method for calculating the category statistics.
     *
     * We start off by retrieving outages affecting the nodes and services
     * in the given category, and group these by node id.
     *
     * Using the outages, we calculate node-level statistics
     * and tally the values to calculate the category statistics.
     */
    @Override
    @Transactional(readOnly=true)
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

        // window bounds
        final Date windowStart = new Date(curTime - rWindow);
        final Date windowEnd = new Date(curTime);

        // category specifics
        final List<Integer> nodeIds = getNodes(category);
        final List<String> serviceNames = category.getServiceCollection();

        // retrieve the outages associated with the given nodes, only retrieving those that affect our window
        final Map<Integer, List<OnmsOutage>> outagesByNode = getOutages(nodeIds, serviceNames, windowStart, windowEnd);

        // calculate the node level statistics
        for (final int nodeId : nodeIds) {
            List<OnmsOutage> outages = outagesByNode.get(nodeId);
            if (outages == null) {
                outages = Lists.newArrayList();
            }

            // sum the outage time
            final double outageTime = getOutageTimeInWindow(outages, windowStart, windowEnd);

            // determine the number of services
            final int numServices = getNumServices(nodeId, serviceNames);

            // count the number of outstanding outages
            final long numServicesDown = outages.stream()
                    .filter(outage -> outage.getIfRegainedService() == null)
                    .count();

            final Node levelNode = new Node();
            levelNode.setNodeid(nodeId);

            // value for this node for this category
            levelNode.setNodevalue(RTCUtils.getOutagePercentage(outageTime, rWindow, numServices));

            // node service count
            levelNode.setNodesvccount(numServices);

            // node service down count
            levelNode.setNodesvcdowncount(numServicesDown);

            // add the node
            levelCat.getNode().add(levelNode);

            // update the category statistics
            numServicesInCategory += numServices;
            outageTimeInCategory += outageTime;
        }

        // calculate the outage percentage using tallied values
        levelCat.setCatvalue(RTCUtils.getOutagePercentage(outageTimeInCategory, rWindow, numServicesInCategory));

        // add category
        level.getCategory().add(levelCat);

        LOG.debug("Done retrieving availability statistics for {} with {} services.", category.getLabel(), numServicesInCategory);

        return level;
    }

    private static double getOutageTimeInWindow(List<OnmsOutage> outages, Date start, Date end) {
        if (outages == null || outages.size() == 0) {
            return 0.0d;
        }

        final long windowStart = start.getTime();
        final long windowEnd = end.getTime();
        final long windowLength = windowEnd - windowStart;
        Preconditions.checkArgument(0 <= windowStart && windowStart < windowEnd);

        long downtimeInWindow = 0;
        for (final OnmsOutage outage : outages) {
            // When did the service go down?
            // Use the start of the window if the service went down before this
            final long lostAt = Math.max(windowStart, outage.getIfLostService().getTime());

            // When did the service come back up?
            long regainedAt;
            if (outage.getIfRegainedService() == null) {
                // It's still offline - use the end of the window
                regainedAt = windowEnd;
            } else {
                // Use the end of the window if the service came back up after this
                regainedAt = Math.min(windowEnd, outage.getIfRegainedService().getTime());
            }

            downtimeInWindow += (regainedAt - lostAt);
        }

        // Bound the downtime by the length of the window
        return Math.min(downtimeInWindow, windowLength);
    }

    private int getNumServices(int nodeId, List<String> serviceNames) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoredService.class)
            .alias("ipInterface", "ipInterface")
            .alias("ipInterface.node", "node")
            .eq("ipInterface.isManaged", "M")
            .eq("node.id", nodeId);

        if (serviceNames != null && serviceNames.size() > 0) {
            builder.alias("serviceType", "serviceType")
            .in("serviceType.name", serviceNames);
        }

        // Retrieve the services and group them by node id
        return m_monitoredServiceDao.countMatching(builder.toCriteria());
    }

    private Map<Integer, List<OnmsOutage>> getOutages(List<Integer> nodeIds, List<String> serviceNames, Date start, Date end) {
        if (nodeIds == null || nodeIds.size() == 0) {
            return Maps.newHashMap();
        }

        final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class)
            // The outage must have started before the end of the window
            .le("ifLostService", end)
            .or(new NullRestriction("ifRegainedService"), // The outage is ongoing
                new AllRestriction( // or the outage was cleared somewhere in the window
                    new GtRestriction("ifRegainedService", start),
                    new LeRestriction("ifRegainedService", end))
            )
            // Only select outages affecting our nodes
            .alias("monitoredService", "monitoredService")
            .alias("monitoredService.ipInterface", "ipInterface")
            .alias("ipInterface.node", "node")
            .eq("ipInterface.isManaged", "M")
            .in("node.id", nodeIds);

        // Only select outages affecting services with the given names, if set
        if (serviceNames != null && serviceNames.size() > 0) {
            builder.alias("monitoredService.serviceType", "serviceType")
            .in("serviceType.name", serviceNames);
        }

        // Retrieve the outages and group them by node id
        return m_outageDao.findMatching(builder.toCriteria()).stream()
            .collect(Collectors.groupingBy(outage -> outage.getNodeId()));
    }

    private List<Integer> getNodes(RTCCategory category) {
        // Refresh the list of nodes contained inside the RTCCategory
        category.clearNodes();
        category.addAllNodes(RTCUtils.getNodeIdsForCategory(m_filterDao,  category));
        return category.getNodes();
    }
}
