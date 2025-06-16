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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.ServiceSelector;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>MonitoredServiceDaoHibernate class.</p>
 *
 * @author david
 */
public class MonitoredServiceDaoHibernate extends AbstractDaoHibernate<OnmsMonitoredService, Integer> implements MonitoredServiceDao {

    @Autowired
    private FilterDao m_filterDao;

    /**
     * <p>Constructor for MonitoredServiceDaoHibernate.</p>
     */
    public MonitoredServiceDaoHibernate() {
		super(OnmsMonitoredService.class);
	}

	/** {@inheritDoc} */
    @Override
	public List<OnmsMonitoredService> findByType(String type) {
		return find("from OnmsMonitoredService svc where svc.serviceType.name = ?", type);
	}

    /** {@inheritDoc} */
    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, String svcName) {
        return findUnique("from OnmsMonitoredService as svc " +
                    "where svc.ipInterface.node.id = ? and svc.ipInterface.ipAddress = ? and svc.serviceType.name = ?",
                   nodeId, ipAddress, svcName);
    }

    /** {@inheritDoc} */
    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, Integer serviceId) {
        return findUnique("from OnmsMonitoredService as svc " +
                    "where svc.ipInterface.node.id = ? and svc.ipInterface.ipAddress = ? and svc.serviceType.id = ?",
                   nodeId, ipAddress, serviceId);
    }

	/** {@inheritDoc} */
    @Override
	public OnmsMonitoredService getPrimaryService(Integer nodeId, String svcName) {
	    return findUnique("from OnmsMonitoredService as svc " +
	                      "where svc.ipInterface.node.id = ? and svc.ipInterface.snmpPrimary= ? and svc.serviceType.name = ?",
	                     nodeId, PrimaryType.PRIMARY.getCharCode(), svcName);
	}

    /** {@inheritDoc} */
    @Override
    public OnmsMonitoredService get(Integer nodeId, InetAddress ipAddr, Integer ifIndex, Integer serviceId) {
        return findUnique("from OnmsMonitoredService as svc " +
                "where svc.ipInterface.node.id = ? and svc.ipInterface.ipAddress = ? and svc.ipInterface.snmpInterface.ifIndex = ? and svc.serviceType.id = ?",
               nodeId, ipAddr, ifIndex, serviceId);
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsMonitoredService> findMatchingServices(ServiceSelector selector) {
        m_filterDao.flushActiveIpAddressListCache();
        Set<InetAddress> matchingAddrs = new HashSet<InetAddress>(m_filterDao.getActiveIPAddressList(selector.getFilterRule()));
        Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());
        
        List<OnmsMonitoredService> matchingServices = new LinkedList<>();
        Collection<OnmsMonitoredService> services = findActive();
        for (OnmsMonitoredService svc : services) {
            if ((matchingSvcs.contains(svc.getServiceName()) || matchingSvcs.isEmpty()) &&
                matchingAddrs.contains(svc.getIpAddress())) {
                
                matchingServices.add(svc);
            }
        }

        return matchingServices;
    }

    private Collection<OnmsMonitoredService> findActive() {
        return find("select distinct svc from OnmsMonitoredService as svc " +
        		"left join fetch svc.serviceType " +
        		"left join fetch svc.ipInterface as ip " +
        		"left join fetch ip.node as node " +
        		"left join fetch node.assetRecord " +
        		"where (svc.status is null or svc.status not in ('F','U','D'))");
    }

    /** {@inheritDoc} */
    @Override
    public Set<OnmsMonitoredService> findByApplication(OnmsApplication application) {
        return application.getMonitoredServices();
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsMonitoredService> findAllServices() {
        return find("select distinct svc from OnmsMonitoredService as svc " +
                "left join fetch svc.serviceType " +
                "left join fetch svc.ipInterface as ip " +
                "left join fetch ip.node as node");
    }

    @Override
    public List<OnmsMonitoredService> findByNode(int nodeId) {
        return find("select distinct svc from OnmsMonitoredService as svc " +
                    "left join fetch svc.ipInterface as iface " +
                    "left join fetch iface.node as node " +
                    "where node.id = ?",
                    nodeId);
    }
}
