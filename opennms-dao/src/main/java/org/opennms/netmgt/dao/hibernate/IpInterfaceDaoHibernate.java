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

package org.opennms.netmgt.dao.hibernate;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.transform.ResultTransformer;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * <p>IpInterfaceDaoHibernate class.</p>
 *
 * @author david
 */
public class IpInterfaceDaoHibernate extends AbstractDaoHibernate<OnmsIpInterface, Integer>  implements IpInterfaceDao {
    private static final Logger LOG = LoggerFactory.getLogger(IpInterfaceDaoHibernate.class);

    String m_findByServiceTypeQuery = null;

    /**
     * <p>Constructor for IpInterfaceDaoHibernate.</p>
     */
    public IpInterfaceDaoHibernate() {
        super(OnmsIpInterface.class);
        
        
        m_findByServiceTypeQuery = System.getProperty("org.opennms.dao.ipinterface.findByServiceType", 
                                                      "select distinct ipInterface from OnmsIpInterface as ipInterface join ipInterface.monitoredServices as monSvc where monSvc.serviceType.name = ?");
        
    }

    /** {@inheritDoc} */
    @Override
    public OnmsIpInterface get(OnmsNode node, String ipAddress) {
        return findUnique("from OnmsIpInterface as ipInterface where ipInterface.node = ? and ipInterface.ipAddress = ?", node, ipAddress);
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsIpInterface> findByIpAddress(String ipAddress) {
        return find("from OnmsIpInterface ipInterface where ipInterface.ipAddress = ?", ipAddress);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<OnmsIpInterface> findByNodeId(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from OnmsIpInterface ipInterface where ipInterface.node.id = ?", nodeId);
    }

    /** {@inheritDoc} */
    @Override
    public OnmsIpInterface findByNodeIdAndIpAddress(Integer nodeId, String ipAddress) {
        return findUnique("select ipInterface from OnmsIpInterface as ipInterface where ipInterface.node.id = ? and ipInterface.ipAddress = ?", 
                          nodeId, 
                          ipAddress);
        
    }

    /** {@inheritDoc} */
    @Override
    public OnmsIpInterface findByForeignKeyAndIpAddress(String foreignSource, String foreignId, String ipAddress) {
        return findUnique("select ipInterface from OnmsIpInterface as ipInterface join ipInterface.node as node where node.foreignSource = ? and node.foreignId = ? and ipInterface.ipAddress = ?", 
                          foreignSource, 
                          foreignId, 
                          ipAddress);
        
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsIpInterface> findByServiceType(String svcName) {
        
        return find(m_findByServiceTypeQuery, svcName);
    }

    /** {@inheritDoc} */
    @Override
    public List<OnmsIpInterface> findHierarchyByServiceType(String svcName) {
        return find("select distinct ipInterface " +
                    "from OnmsIpInterface as ipInterface " +
                    "left join fetch ipInterface.node as node " +
                    "left join fetch node.assetRecord " +
                    "left join fetch ipInterface.node.snmpInterfaces as snmpIf " +
                    "left join fetch snmpIf.ipInterfaces " +
                    "join ipInterface.monitoredServices as monSvc " +
                    "where monSvc.serviceType.name = ?", svcName);
    }

    /**
     * TODO: This function should filter out deleted interfaces
     *
     * @return a {@link java.util.Map} object.
     */
    @Override
    public Map<InetAddress, Integer> getInterfacesForNodes() {
        Map<InetAddress, Integer> map = new HashMap<InetAddress, Integer>();

        // Add all primary addresses first
        @SuppressWarnings("unchecked")
        List<Object[]> l = (List<Object[]>)getHibernateTemplate().find("select distinct ipInterface.ipAddress, ipInterface.node.id from OnmsIpInterface as ipInterface where ipInterface.isSnmpPrimary = 'P'");
        for (Object[] tuple : l) {
            InetAddress ip = (InetAddress) tuple[0];
            Integer nodeId = (Integer) tuple[1];
            map.put(ip, nodeId);
        }

        // Add all non-primary addresses only if those addresses doesn't exist on the map.
        @SuppressWarnings("unchecked")
        List<Object[]> s = (List<Object[]>)getHibernateTemplate().find("select distinct ipInterface.ipAddress, ipInterface.node.id from OnmsIpInterface as ipInterface where ipInterface.isSnmpPrimary != 'P'");
        for (Object[] tuple : s) {
            InetAddress ip = (InetAddress) tuple[0];
            Integer nodeId = (Integer) tuple[1];
            if (!map.containsKey(ip))
                map.put(ip, nodeId);
        }

        return map;
    }

    /**
     * <p>addressExistsWithForeignSource</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean addressExistsWithForeignSource(String ipAddress, String foreignSource) {
        Assert.notNull(ipAddress, "ipAddress cannot be null");
        if (foreignSource == null) {
            return queryInt("select count(ipInterface.id) from OnmsIpInterface as ipInterface " +
                    "join ipInterface.node as node " +
                    "where node.foreignSource is NULL " +
                    "and ipInterface.ipAddress = ? ", ipAddress) > 0;
        } else {
            return queryInt("select count(ipInterface.id) from OnmsIpInterface as ipInterface " +
                    "join ipInterface.node as node " +
                    "where node.foreignSource = ? " +
                    "and ipInterface.ipAddress = ? ", foreignSource, ipAddress) > 0;
        }
    }

    /**
     * This function should be kept similar to {@link OnmsNode#getPrimaryInterface()}.
     */
    @Override
    public OnmsIpInterface findPrimaryInterfaceByNodeId(final Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        // SELECT ipaddr FROM ipinterface WHERE nodeid = ? AND issnmpprimary = 'P'

        List<OnmsIpInterface> primaryInterfaces = find("from OnmsIpInterface as ipInterface where ipInterface.node.id = ? and ipInterface.isSnmpPrimary = 'P' order by ipLastCapsdPoll desc", nodeId);
        if (primaryInterfaces.size() < 1) {
            return null;
        } else {
            OnmsIpInterface retval = primaryInterfaces.iterator().next();
            if (primaryInterfaces.size() > 1) {
                LOG.warn("Multiple primary SNMP interfaces for node {}, returning most recently scanned interface: {}", nodeId, retval.getInterfaceId());
            }
            return retval;
        }
    }

    @Override
    public List<OnmsIpInterface> findInterfacesWithMetadata(String context, String key, String value) {  return getHibernateTemplate().execute(session -> (List<OnmsIpInterface>) session.createSQLQuery("SELECT ip.id FROM ipInterface ip, ipInterface_metadata m WHERE m.id = ip.id AND context = :context AND key = :key AND value = :value ORDER BY ip.id")
            .setString("context", context)
            .setString("key", key)
            .setString("value", value)
            .setResultTransformer(new ResultTransformer() {
                @Override
                public Object transformTuple(Object[] tuple, String[] aliases) {
                    return get((Integer) tuple[0]);
                }

                @SuppressWarnings("rawtypes")
                @Override
                public List transformList(List collection) {
                    return collection;
                }
            }).list());
    }

}
