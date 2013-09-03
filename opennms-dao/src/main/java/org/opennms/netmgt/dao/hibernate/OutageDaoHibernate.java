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
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.springframework.orm.hibernate3.HibernateCallback;

public class OutageDaoHibernate extends AbstractDaoHibernate<OnmsOutage, Integer> implements OutageDao {

    /**
     * <p>Constructor for OutageDaoHibernate.</p>
     */
    public OutageDaoHibernate() {
        super(OnmsOutage.class);
    }

    /**
     * <p>currentOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer currentOutageCount() {
        return queryInt("select count(*) from OnmsOutage as o where o.ifRegainedService is null");
    }

    /**
     * <p>currentOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> currentOutages() {
        return find("from OnmsOutage as o where o.ifRegainedService is null");
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsOutage>)getHibernateTemplate().execute(new HibernateCallback<Collection<OnmsOutage>>() {

            @SuppressWarnings("unchecked")
            @Override
            public Collection<OnmsOutage> doInHibernate(final Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsOutage.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }

        });
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> matchingCurrentOutages(final ServiceSelector selector) {
        final Set<InetAddress> matchingAddrs = new HashSet<InetAddress>(FilterDaoFactory.getInstance().getIPAddressList(selector.getFilterRule()));
        final Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());

        final List<OnmsOutage> matchingOutages = new LinkedList<OnmsOutage>();
        final Collection<OnmsOutage> outages = currentOutages();
        for (final OnmsOutage outage : outages) {
            final OnmsMonitoredService svc = outage.getMonitoredService();
            if ((matchingSvcs.contains(svc.getServiceName()) || matchingSvcs.isEmpty()) && matchingAddrs.contains(svc.getIpAddress())) {
                matchingOutages.add(outage);
            }

        }

        return matchingOutages;
    }

    /** {@inheritDoc} */
    @Override
    public int countOutagesByNode() {
        return getNodeOutageSummaries(0).size();
    }

    // final int nodeId, final String nodeLabel, final Date timeDown, final Date timeUp, final Date timeNow
    /** {@inheritDoc} */
    @Override
    public List<OutageSummary> getNodeOutageSummaries(final int rows) {
        final List<OutageSummary> outages = findObjects(
            OutageSummary.class,
            "SELECT DISTINCT new org.opennms.netmgt.model.outage.OutageSummary(node.id, node.label, max(outage.ifLostService)) " +
            "FROM OnmsOutage AS outage " +
            "LEFT JOIN outage.monitoredService AS monitoredService " +
            "LEFT JOIN monitoredService.ipInterface AS ipInterface " + 
            "LEFT JOIN ipInterface.node AS node " +
            "WHERE outage.ifRegainedService IS NULL " +
            "GROUP BY node.id, node.label " +
            "ORDER BY max(outage.ifLostService) DESC, node.label ASC, node.id ASC"
        );
        if (rows == 0 || outages.size() < rows) {
            return outages;
        } else {
            return outages.subList(0, rows);
        }
    }
    
    public List<OnmsOutage> findbyNodeId(Integer nodeId) {
        String query = "from OnmsOutage as outage where outage.nodeId = ?";
        return find(query, nodeId);
    }
    
    @Override
    public List<OnmsOutage> findbyNodeIdIpAddrServiceId(Integer nodeId, String ipAddr, Integer serviceId) {
        String query = "from OnmsOutage as outage where outage.nodeId = ? and outage.ipAddress = ? and outage.serviceId = ?";
        return find(query, nodeId, ipAddr, serviceId);
    }

    @Override
    public List<OnmsOutage> findbyNodeIdAndIpAddr(Integer nodeId, String ipAddr) {
        String query = "from OnmsOutage as outage where outage.nodeId = ? and outage.ipAddress = ?";
        return find(query, nodeId, ipAddr);
    }
    
    public List<OnmsOutage> findbyNodeIdIpAddrAndServiceName(Integer nodeId, String ipAddr, String serviceName) {
        //select outages.outageid from outages, service 
        //  where outages.nodeid = ? AND outages.ipaddr = ? AND outages.serviceid = service.serviceId AND service.servicename = ?
        String query = "from OnmsOutage as outage, OnmsServiceType as svcType " +
        		"where outage.nodeId = ? AND outage.ipAddress = ? AND svcType.name = ? " +
        		"AND outage.serviceId = svcType.id";
        return find(query, nodeId, ipAddr, serviceName);
    }
    
    public OnmsOutage findByServiceStatus() {
        //select outages.outageid from outages, ifservices 
        //where ((outages.nodeid = ifservices.nodeid) AND (outages.ipaddr = ifservices.ipaddr) 
        //        AND (outages.serviceid = ifservices.serviceid) 
        //        AND ((ifservices.status = 'D') OR (ifservices.status = 'F') OR (ifservices.status = 'U')) 
        //        AND (outages.ifregainedservice IS NULL)
        String query = "from OnmsOutage as outage, OnmsMonitoredService as svc " +
        		"where outage.nodeId = svc.ipInterface.node.id and outage.ipAddress = svc.ipInterface.ipAddress " +
        		"outage.serviceId = svc.serviceType and " +
        		"(svc.status = 'D' or svc.status = 'F' or svc.status = 'U') " +
        		"and outage.ifRegainedService IS NULL";
        return findUnique(query);
    }
    
    public OnmsOutage findByIpInterfaceIsManaged () {
        //select outages.outageid from outages, ipinterface 
        //where ((outages.nodeid = ipinterface.nodeid) AND (outages.ipaddr = ipinterface.ipaddr) 
        //        AND ((ipinterface.ismanaged = 'F') OR (ipinterface.ismanaged = 'U')) 
        //        AND (outages.ifregainedservice IS NULL)        
        String query = "from OnmsOutage as outage, OnmsIpInterface as ipInterface " +
                        "where outage.nodeId = ipInterface.node.id and outage.ipAddress = ipInterface.ipAddress " +
                        "and (ipInterface.isManaged = 'D' or ipInterface.isManaged = 'U') " +
                        "and outage.ifRegainedService IS NULL";
        return findUnique(query);
    }
}
