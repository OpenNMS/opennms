/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.PathOutageDao;
import org.opennms.netmgt.model.OnmsPathOutage;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>PathOutageDaoHibernate class</p>
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 *
 */
public class PathOutageDaoHibernate extends AbstractDaoHibernate<OnmsPathOutage, Integer> implements PathOutageDao{

    /**
     * <p>PathOutageDaoHibernate constructor</p>
     */
    public PathOutageDaoHibernate() {
        super(OnmsPathOutage.class);
    }

    @Override
    public List<Integer> getNodesForPathOutage(final OnmsPathOutage pathOutage) {
        return getNodesForPathOutage(pathOutage.getCriticalPathIp(), pathOutage.getCriticalPathServiceName());
    }
    
    /**
     * This method returns the unique list of critical paths (node label, IP address, and service name)
     */
    @Override
    public List<String[]> getAllCriticalPaths() {
        return getHibernateTemplate().execute(new HibernateCallback<List<String[]>>() {
            @Override
            public List<String[]> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(
                    "select distinct node.label, pathOutage.criticalPathIp, pathOutage.criticalPathServiceName from OnmsPathOutage as pathOutage, OnmsIpInterface as ipInterface join ipInterface.node as node join ipInterface.monitoredServices as monitoredServices join monitoredServices.serviceType as serviceType " +
                    // Make sure that the path outage is on a managed interface
                    "where pathOutage.criticalPathIp = ipInterface.ipAddress and ipInterface.isManaged <> 'D' and " +
                    // And that the service is marked as active
                    "pathOutage.criticalPathServiceName = serviceType.name and monitoredServices.status = 'A' " +
                    // Sort by node label so that the lists are easy to read
                    "order by node.label, pathOutage.criticalPathIp, pathOutage.criticalPathServiceName"
                );
                List<Object[]> queryResults = (List<Object[]>)query.list();
                if (queryResults == null || queryResults.size() == 0) {
                    return Collections.emptyList();
                } else {
                    List<String[]> retval = new ArrayList<String[]>(queryResults.size());
                    for (Object[] row : queryResults) {
                        // Only add the ID to the return value
                        retval.add(new String[] { (String)row[0], InetAddressUtils.str((InetAddress)row[1]), (String)row[2]});
                    }
                    return retval;
                }
            }
        });
    }

    @Override
    public List<Integer> getNodesForPathOutage(final InetAddress ipAddress, final String serviceName) {

        // SELECT count(DISTINCT pathoutage.nodeid) FROM pathoutage, ipinterface WHERE pathoutage.criticalpathip=? AND pathoutage.criticalpathservicename=? AND pathoutage.nodeid=ipinterface.nodeid AND ipinterface.ismanaged!='D'"" +

        return getHibernateTemplate().execute(new HibernateCallback<List<Integer>>() {
            @Override
            public List<Integer> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(
                    "select distinct node.id, node.label from OnmsPathOutage as pathOutage, OnmsIpInterface as ipInterface join pathOutage.node as node join ipInterface.monitoredServices as monitoredServices join monitoredServices.serviceType as serviceType " +
                    // Select the path outage
                    "where pathOutage.criticalPathIp = :ipAddress and pathOutage.criticalPathServiceName = :serviceName and " +
                    // Make sure that the path outage is on a managed interface
                    "pathOutage.criticalPathIp = ipInterface.ipAddress and ipInterface.isManaged <> 'D' and " +
                    // And that the service is marked as active
                    "pathOutage.criticalPathServiceName = serviceType.name and monitoredServices.status = 'A' " +
                    // Sort by node label so that the lists are easy to read
                    "order by node.label" 
                );
                query.setParameter("ipAddress", InetAddressUtils.str(ipAddress));
                query.setParameter("serviceName", serviceName);
                List<Object[]> queryResults = query.list();
                if (queryResults == null || queryResults.size() == 0) {
                    return Collections.emptyList();
                } else {
                    List<Integer> retval = new ArrayList<Integer>(queryResults.size());
                    for (Object[] row : queryResults) {
                        // Only add the ID to the return value
                        retval.add((Integer)row[0]);
                    }
                    return retval;
                }
            }
        });
    }

    @Override
    public List<Integer> getAllNodesDependentOnAnyServiceOnInterface(final InetAddress ipAddress) {
        return getHibernateTemplate().execute(new HibernateCallback<List<Integer>>() {
            @Override
            public List<Integer> doInHibernate(Session session) throws HibernateException, SQLException {
                //Query query = session.createQuery("select distinct node.id from OnmsPathOutage as pathOutage left join pathOutage.node as node left join node.ipInterfaces as ipInterfaces left join ipInterfaces.monitoredServices as monitoredServices where pathOutage.criticalPathIp = :ipAddress and ipInterfaces.isManaged <> 'D' and monitoredServices.status = 'A'");
                Query query = session.createQuery(
                    "select distinct node.id from OnmsPathOutage as pathOutage, OnmsIpInterface as ipInterface join pathOutage.node as node join ipInterface.monitoredServices as monitoredServices " +
                    // Select the path outage
                    "where pathOutage.criticalPathIp = :ipAddress and " +
                    // Make sure that the path outage is on a managed interface
                    "pathOutage.criticalPathIp = ipInterface.ipAddress and ipInterface.isManaged <> 'D' and " +
                    // And that any service is marked as active
                    "monitoredServices.status = 'A'" 
                );
                query.setParameter("ipAddress", InetAddressUtils.str(ipAddress));
                List<Integer> result = (List<Integer>)query.list();
                if (result == null) {
                    return Collections.emptyList();
                } else {
                    return result;
                }
            }
        });
    }

    @Override
    public List<Integer> getAllNodesDependentOnAnyServiceOnNode(final int nodeId) {
        return getHibernateTemplate().execute(new HibernateCallback<List<Integer>>() {
            @Override
            public List<Integer> doInHibernate(Session session) throws HibernateException, SQLException {
                //Query query = session.createQuery("select distinct node.id from OnmsPathOutage as pathOutage, OnmsIpInterface as ipInterface left join pathOutage.node as node where pathOutage.criticalPathIp = ipInterface.ipAddress and ipInterface.node.id = :nodeId and ipInterface.isManaged <> 'D'");
                Query query = session.createQuery(
                    "select distinct node.id from OnmsPathOutage as pathOutage, OnmsIpInterface as ipInterface join pathOutage.node as node join ipInterface.monitoredServices as monitoredServices " +
                    // Select the node from the path outage's IP interface
                    "where ipInterface.node.id = :nodeId and " +
                    // Make sure that the path outage is on a managed interface
                    "pathOutage.criticalPathIp = ipInterface.ipAddress and ipInterface.isManaged <> 'D' and " +
                    // And that any service is marked as active
                    "monitoredServices.status = 'A'"
                );
                query.setParameter("nodeId", nodeId);
                List<Integer> result = (List<Integer>)query.list();
                if (result == null) {
                    return Collections.emptyList();
                } else {
                    return result;
                }
            }
        });
    }
}
