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
package org.opennms.netmgt.dao.hibernate;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.api.PathOutageConfig;
import org.opennms.netmgt.dao.api.CriticalPath;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.PathOutageDao;
import org.opennms.netmgt.dao.api.PathOutageManager;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsPathOutage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The source for all path outage business objects (nodes, critical path IPs,
 * critical path service names). Encapsulates all lookup functionality for
 * these business objects in one place.
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class PathOutageManagerDaoImpl implements PathOutageManager {

	@Autowired
	private NodeDao nodeDao;
	
	@Autowired
	private PathOutageDao pathOutageDao;
	
	@Autowired
	private MonitoredServiceDao monitoredServiceDao;
	
	@Autowired
	private OutageDao outageDao;

    @Autowired
    private PathOutageConfig pathOutageConfig;

    public static PathOutageManager getInstance() {
        return BeanUtils.getBean("daoContext", "pathOutageManager", PathOutageManager.class);
    }

    /**
     * <p>
     * Retrieve all the critical paths
     * from the database
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public List<String[]> getAllCriticalPaths() throws SQLException {
        return pathOutageDao.getAllCriticalPaths();
    }

    /**
     * <p>
     * Retrieve critical path by nodeid
     * from the database
     *
     * @param nodeID a int.
     * @return a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public String getPrettyCriticalPath(int nodeID) {
        OnmsPathOutage out = pathOutageDao.get(nodeID);

        if (out == null) {
            return NO_CRITICAL_PATH;
        } else {
            return InetAddressUtils.str(out.getCriticalPathIp()) + " " + out.getCriticalPathServiceName();
        }
    }

    @Override
    public CriticalPath getCriticalPath(int nodeId) {
        //"SELECT criticalpathip, criticalpathservicename FROM pathoutage WHERE nodeid=?";
        String location = null;
        InetAddress pathIp = pathOutageConfig.getDefaultCriticalPathIp();
        String serviceName = "ICMP";

        final OnmsNode node = nodeDao.get(nodeId);
        if (node != null) {
            location = MonitoringLocationUtils.getLocationNameOrNullIfDefault(node);
        }

        final OnmsPathOutage out = pathOutageDao.get(nodeId);
        if (out != null) {
            if (out.getCriticalPathIp() != null) {
                pathIp = out.getCriticalPathIp();
            }
            if (out.getCriticalPathServiceName() != null && !"".equals(out.getCriticalPathServiceName().trim())) {
                serviceName = out.getCriticalPathServiceName();
            }
        }
        return new CriticalPath(location, pathIp, serviceName);
    }

    /**
     * <p>
     * Retrieve all the nodes in a critical path
     * from the database
     *
     * @param criticalPathIp
     *            IP address of the critical path
     * @param criticalPathServiceName
     *            service name for the critical path
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public Set<Integer> getNodesInPath(String criticalPathIp, String criticalPathServiceName) {
        return new LinkedHashSet<Integer>(pathOutageDao.getNodesForPathOutage(InetAddressUtils.addr(criticalPathIp), criticalPathServiceName));
    }

    /**
     * This method is responsible for determining the
     * node label of a node, and the up/down status
     * and status color
     *
     * @param nodeIDStr a {@link java.lang.String} object.
     * @param conn a {@link java.sql.Connection} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public String[] getLabelAndStatus(String nodeIDStr, Connection conn) {
        String[] result = new String[3];
        result[1] = "Cleared";
        result[2] = "Unmanaged";

        int nodeID = WebSecurityUtils.safeParseInt(nodeIDStr);

        OnmsNode node = nodeDao.get(nodeID);
        if (node == null) {
            // TODO Log that node could not be found in database
            return result;
        }

        result[0] = node.getLabel();

        final org.opennms.core.criteria.Criteria crit = new org.opennms.core.criteria.Criteria(OnmsMonitoredService.class)
        .setAliases(Arrays.asList(new Alias[] {
            new Alias("ipInterface","ipInterface", JoinType.INNER_JOIN)
        }))
        .addRestriction(new EqRestriction("status", "A"))
        .addRestriction(new EqRestriction("ipInterface.node", node));

        // Get all active services on the node
        List<OnmsMonitoredService> services = monitoredServiceDao.findMatching(crit);

        int countManagedSvcs = services.size();

        // Count how many of these services have open outages
        int countOutages = 0;
        for (OnmsMonitoredService service : services) {
            OnmsOutage out = outageDao.currentOutageForService(service);
            if (out != null) {
                countOutages++;
            }
        }

        if(countManagedSvcs == countOutages) {
            result[1] = "Critical";
            result[2] = "All Services Down";
        } else if(countOutages == 0) {
            result[1] = "Normal";
            result[2] = "All Services Up";
        } else {
            result[1] = "Minor";
            result[2] = "Some Services Down";
        }

        return result;
    }

    /**
     * This method is responsible for determining the
     * data related to the critical path:
     * node label, nodeId, the number of nodes
     * dependent on this path, and the managed state
     * of the path
     *
     * @param criticalPathIp a {@link java.lang.String} object.
     * @param criticalPathServiceName a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public String[] getCriticalPathData(String criticalPathIp, String criticalPathServiceName) {
        String[] result = new String[4];

        // Fetch all non-deleted nodes that have the critical path IP address

        List<OnmsNode> nList = nodeDao.findByIpAddressAndService(InetAddressUtils.addr(criticalPathIp), criticalPathServiceName);

        if(nList.size() < 1) {
            // Didn't find the critical node so just return an empty result
            return result;
        } else if(nList.size() == 1) {
            OnmsNode node = nList.get(0);
            result[0] = node.getLabel();
            result[1] = node.getNodeId();
        } else if(nList.size() > 1) {
            OnmsNode node = nList.get(0);
            result[0] = "(" + nList.size() + " nodes have this IP)";
            result[1] = node.getNodeId();
        }

        result[2] = String.valueOf(pathOutageDao.getNodesForPathOutage(InetAddressUtils.addr(criticalPathIp), criticalPathServiceName).size());

        OnmsMonitoredService service = monitoredServiceDao.get(Integer.valueOf(result[1]), InetAddressUtils.addr(criticalPathIp), criticalPathServiceName);

        if (service != null) {
            OnmsOutage outage = outageDao.currentOutageForService(service);

            if(outage == null) {
                result[3] = "Normal";
            } else {
                result[3] = "Critical";
            }
        } else {
            result[3] = "Cleared";
        }
        
        return result;
    }

    @Override
    public Set<Integer> getAllNodesDependentOnAnyServiceOnInterface(String criticalPathip) {
        return new LinkedHashSet<Integer>(pathOutageDao.getAllNodesDependentOnAnyServiceOnInterface(InetAddressUtils.addr(criticalPathip)));
    }

    @Override
    public Set<Integer> getAllNodesDependentOnAnyServiceOnNode(int nodeId) {
        return new LinkedHashSet<Integer>(pathOutageDao.getAllNodesDependentOnAnyServiceOnNode(nodeId));
    }

    @Override
    public InetAddress getDefaultCriticalPathIp() {
        return pathOutageConfig.getDefaultCriticalPathIp();
    }
}
