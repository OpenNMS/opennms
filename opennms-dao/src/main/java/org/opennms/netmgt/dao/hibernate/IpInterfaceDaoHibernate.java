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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import joptsimple.internal.Strings;
import org.hibernate.transform.ResultTransformer;
import org.opennms.netmgt.config.api.collection.IColumn;
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
    public List<OnmsIpInterface> findByMacLinksOfNode(Integer nodeId) {
        Assert.notNull(nodeId, "nodeId cannot be null");
        return find("from OnmsIpInterface ipInterface where ipInterface.ipAddress in (select ipNetToMedia.netAddress from IpNetToMedia ipNetToMedia where ipNetToMedia.physAddress in (select l.macAddress from BridgeMacLink l where l.node.id = ?))", nodeId);
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
        List<Object[]> l = (List<Object[]>)getHibernateTemplate().find("select distinct ipInterface.ipAddress, ipInterface.node.id from OnmsIpInterface as ipInterface where ipInterface.snmpPrimary = 'P'");
        for (Object[] tuple : l) {
            InetAddress ip = (InetAddress) tuple[0];
            Integer nodeId = (Integer) tuple[1];
            map.put(ip, nodeId);
        }

        // Add all non-primary addresses only if those addresses doesn't exist on the map.
        @SuppressWarnings("unchecked")
        List<Object[]> s = (List<Object[]>)getHibernateTemplate().find("select distinct ipInterface.ipAddress, ipInterface.node.id from OnmsIpInterface as ipInterface where ipInterface.snmpPrimary != 'P'");
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

        List<OnmsIpInterface> primaryInterfaces = find("from OnmsIpInterface as ipInterface where ipInterface.node.id = ? and ipInterface.snmpPrimary = 'P' order by ipLastCapsdPoll desc", nodeId);
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
    public List<OnmsIpInterface> findInterfacesWithMetadata(final String context, String key, final String value, final boolean matchEnumeration) {
        // what is happening here?
        // 1. in the case matchEnumeration is set to true, we try to find the given value by using the following regular expression: (?:^[ ,]*|,[ ]*)stringToSearchFor(?=[ ]*,|,?[ ]*$)
        // 2. of course the value to search for needs to be escaped, so we use REGEXP_REPLACE(:value, '([\.\+\*\?\^\$\(\)\[\]\{\}\|\\])', '\\\1', 'g') here
        return getHibernateTemplate().execute(session -> (List<OnmsIpInterface>) session.createSQLQuery("SELECT ip.id FROM ipInterface ip, ipInterface_metadata m WHERE m.id = ip.id AND context = :context AND key = :key AND value " + (matchEnumeration ? "~ CONCAT('(?:^[ ,]*|,[ ]*)', REGEXP_REPLACE(:value, '([\\.\\+\\*\\?\\^\\$\\(\\)\\[\\]\\{\\}\\|\\\\])', '\\\\\\1', 'g'), '(?=[ ]*,|,?[ ]*$)')" : "= :value" ) + " ORDER BY ip.id")
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

    @Override
    public List<OnmsIpInterface> findByIpAddressAndLocation(String address, String location) {
        return find(
                "from OnmsIpInterface i where i.ipAddress = ? and exists (from OnmsNode n where n.id = i.node.id AND n.location.locationName = ?)",
                address, location
        );
    }
}
