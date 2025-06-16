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
package org.opennms.netmgt.rtc;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.AllRestriction;
import org.opennms.core.criteria.restrictions.GtRestriction;
import org.opennms.core.criteria.restrictions.LeRestriction;
import org.opennms.core.criteria.restrictions.NullRestriction;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.criteria.restrictions.SqlRestriction;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventDatetimeFormatter;
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

    private static final EventDatetimeFormatter FORMATTER = EventConstants.getEventDatetimeFormatter();

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
        header.setCreated(FORMATTER.format(curDate));
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
        final List<String> serviceNames = category.getServices();

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

        return downtimeInWindow;
    }

    private int getNumServices(int nodeId, List<String> serviceNames) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoredService.class)
            .alias("ipInterface", "ipInterface")
            .alias("ipInterface.node", "node")
            .eq("ipInterface.isManaged", "M")
            .eq("node.id", nodeId);

        if (serviceNames != null && serviceNames.size() > 0) {
            builder.alias("serviceType", "serviceType").or(getServiceNameRestrictions(serviceNames));
        }

        // Retrieve the services and group them by node id
        return m_monitoredServiceDao.countMatching(builder.toCriteria());
    }

    private Restriction[] getServiceNameRestrictions(final List<String> serviceNames) {
        return serviceNames.stream().map(e -> {
            if (e.startsWith("~")) {
                return Restrictions.regExp("serviceType.name", e.substring(1));
            } else {
                return Restrictions.eq("serviceType.name", e);
            }
        }).toArray(Restriction[]::new);
    }

    private Map<Integer, List<OnmsOutage>> getOutages(List<Integer> nodeIds, List<String> serviceNames, Date start, Date end) {
        if (nodeIds == null || nodeIds.size() == 0) {
            return Maps.newHashMap();
        }

        final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class)
            // The outage must have started before the end of the window
            .isNull("perspective")
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
            builder.alias("monitoredService.serviceType", "serviceType").or(getServiceNameRestrictions(serviceNames));
        }

        // Retrieve the outages and group them by node id
        return m_outageDao.findMatching(builder.toCriteria()).stream()
            .collect(Collectors.groupingBy(OnmsOutage::getNodeId));
    }

    private List<Integer> getNodes(RTCCategory category) {
        // Refresh the list of nodes contained inside the RTCCategory
        category.clearNodes();
        category.addAllNodes(RTCUtils.getNodeIdsForCategory(m_filterDao,  category));
        return category.getNodes();
    }
}
