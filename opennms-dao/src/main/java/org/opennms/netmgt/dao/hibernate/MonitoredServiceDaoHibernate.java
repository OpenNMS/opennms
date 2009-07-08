/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.dao.hibernate;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ServiceSelector;
/**
 * @author david
 *
 */
public class MonitoredServiceDaoHibernate extends AbstractDaoHibernate<OnmsMonitoredService, Integer>  implements MonitoredServiceDao {

    public MonitoredServiceDaoHibernate() {
		super(OnmsMonitoredService.class);
	}

	public Collection<OnmsMonitoredService> findByType(String type) {
		return find("from OnmsMonitoredService svc where svc.serviceType.name = ?", type);
	}

	public OnmsMonitoredService get(Integer nodeId, String ipAddress, String svcName) {
		return findUnique("from OnmsMonitoredService as svc " +
				    "where svc.ipInterface.node.id = ? and svc.ipInterface.ipAddress = ? and svc.serviceType.name = ?",
				   nodeId, ipAddress, svcName);
	}

	public OnmsMonitoredService get(Integer nodeId, String ipAddr, Integer ifIndex, Integer serviceId) {
		return findUnique("from OnmsMonitoredService as svc " +
			    "where svc.ipInterface.node.id = ? and svc.ipInterface.ipAddress = ? and svc.ipInterface.snmpInterface.ifIndex = ? and svc.serviceType.id = ?",
			   nodeId, ipAddr, ifIndex, serviceId);
	}

    public Collection<OnmsMonitoredService> findMatchingServices(ServiceSelector selector) {
        Set<String> matchingIps = new HashSet<String>(FilterDaoFactory.getInstance().getIPList(selector.getFilterRule()));
        Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());
        
        List<OnmsMonitoredService> matchingServices = new LinkedList<OnmsMonitoredService>();
        Collection<OnmsMonitoredService> services = findAll();
        for (OnmsMonitoredService svc : services) {
            if ((matchingSvcs.contains(svc.getServiceName()) || matchingSvcs.isEmpty()) &&
                matchingIps.contains(svc.getIpAddress())) {
                
                matchingServices.add(svc);
            }
            
        }
        
        
        return matchingServices;
    }

    public Collection<OnmsMonitoredService> findByApplication(OnmsApplication application) {
        return find("select distinct svc from OnmsMonitoredService as svc "
                    + "join svc.applications a "
                    + "where a.name = ?",
                    application.getName());
    }

}
