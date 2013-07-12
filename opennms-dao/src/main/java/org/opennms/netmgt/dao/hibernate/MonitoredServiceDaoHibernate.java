/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.ServiceSelector;
/**
 * <p>MonitoredServiceDaoHibernate class.</p>
 *
 * @author david
 */
public class MonitoredServiceDaoHibernate extends AbstractDaoHibernate<OnmsMonitoredService, Integer>  implements MonitoredServiceDao {

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

    

    @Override
    public OnmsMonitoredService get(Integer nodeId, String ipAddr, Integer serviceId) {
        return findUnique("from OnmsMonitoredService as svc " +
			    "where svc.ipInterface.node.id = ? and svc.ipInterface.ipAddress = ? and svc.serviceType.id = ?",
			   nodeId, ipAddr, serviceId);
    }
    
	/** {@inheritDoc} */
    @Override
	public OnmsMonitoredService getPrimaryService(Integer nodeId, String svcName) {
	    return findUnique("from OnmsMonitoredService as svc " +
	                      "where svc.ipInterface.node.id = ? and svc.ipInterface.isSnmpPrimary= ? and svc.serviceType.name = ?",
	                     nodeId, PrimaryType.PRIMARY, svcName);
	}

	/** {@inheritDoc} */
	@Override
	public OnmsMonitoredService get(Integer nodeId, String ipAddr, Integer ifIndex, Integer serviceId) {
		return findUnique("from OnmsMonitoredService as svc " +
			    "where svc.ipInterface.node.id = ? and svc.ipInterface.ipAddress = ? and svc.ipInterface.snmpInterface.ifIndex = ? and svc.serviceType.id = ?",
			   nodeId, ipAddr, ifIndex, serviceId);
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
        Set<InetAddress> matchingAddrs = new HashSet<InetAddress>(FilterDaoFactory.getInstance().getActiveIPAddressList(selector.getFilterRule()));
        Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());
        
        List<OnmsMonitoredService> matchingServices = new LinkedList<OnmsMonitoredService>();
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

}
