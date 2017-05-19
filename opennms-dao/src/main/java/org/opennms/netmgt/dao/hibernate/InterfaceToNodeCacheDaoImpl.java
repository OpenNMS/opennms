/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2014 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AbstractInterfaceToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeMap;
import org.opennms.netmgt.dao.api.InterfaceToNodeMap.LocationIpAddressKey;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class represents a singular instance that is used to map IP
 * addresses to known nodes.
 *
 * @author Seth
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public class InterfaceToNodeCacheDaoImpl extends AbstractInterfaceToNodeCache implements InterfaceToNodeCache {

    private static final Logger LOG = LoggerFactory.getLogger(InterfaceToNodeCacheDaoImpl.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    private final InterfaceToNodeMap m_knownips = new InterfaceToNodeMap();

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    /**
     * Clears and synchronizes the internal known IP address cache with the
     * current information contained in the database. To synchronize the cache
     * the method opens a new connection to the database, loads the address,
     * and then closes it's connection.
     *
     * @throws java.sql.SQLException
     *             Thrown if the connection cannot be created or a database
     *             error occurs.
     */
    @Override
    @Transactional
    public void dataSourceSync() {
        /*
         * Make a new list with which we'll replace the existing one, that way
         * if something goes wrong with the DB we won't lose whatever was already
         * in there
         */
        Map<LocationIpAddressKey,Integer> newAlreadyDiscovered = new HashMap<>();
        // Fetch all non-deleted nodes
        CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.ne("type", String.valueOf(NodeType.DELETED.value()));
        for (OnmsNode node : m_nodeDao.findMatching(builder.toCriteria())) {
            for (OnmsIpInterface iface : node.getIpInterfaces()) {
                // Skip deleted interfaces
                // TODO: Refactor the 'D' value with an enumeration
                if ("D".equals(iface.getIsManaged())) {
                    continue;
                }
                LOG.debug("Adding entry: {}:{} -> {}", node.getLocation().getLocationName(), iface.getIpAddress(), node.getId());
                newAlreadyDiscovered.put(new LocationIpAddressKey(node.getLocation().getLocationName(), iface.getIpAddress()), node.getId());
            }
        }
        m_knownips.setManagedAddresses(newAlreadyDiscovered);
        LOG.info("dataSourceSync: initialized list of managed IP addresses with {} members", m_knownips.size());
    }

    /**
     * Returns the nodeid for the IP Address
     *
     * @param addr The IP Address to query.
     * @return The node ID of the IP Address if known.
     */
    @Override
    public synchronized int getNodeId(final String location, final InetAddress addr) {
        if (addr == null) {
            return -1;
        }
        return m_knownips.getNodeId(location, addr);
    }

    /**
     * Sets the IP Address and Node ID in the Map.
     *
     * @param addr   The IP Address to add.
     * @param nodeid The Node ID to add.
     * @return The nodeid if it existed in the map.
     */
    @Override
    @Transactional
    public int setNodeId(final String location, final InetAddress addr, final int nodeid) {
        if (addr == null || nodeid == -1) {
            return -1;
        }

        // Only add the address if it doesn't exist on the map. If it exists, only replace
        // the current one if the new address is primary.
        if (m_knownips.getNodeId(location, addr) < 1) {
            LOG.debug("setNodeId: adding IP address to cache: {}:{} -> {}", location, str(addr), nodeid);
            return m_knownips.addManagedAddress(location, addr, nodeid);
        } else {
            final OnmsIpInterface intf = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeid, str(addr));
            if (intf != null && intf.isPrimary()) {
                LOG.info("setNodeId: updating SNMP primary IP address in cache: {}:{} -> {}", location, str(addr), nodeid);
                return m_knownips.addManagedAddress(location, addr, nodeid);
            } else {
                LOG.debug("setNodeId: IP address {}:{} is not primary, avoiding cache update", location, str(addr));
                return -1;
            }
        }
    }

    /**
     * Removes an address from the node ID map.
     *
     * @param addr The address to remove from the node ID map.
     * @return The nodeid that was in the map.
     */
    @Override
    public int removeNodeId(final String location, final InetAddress addr) {
        if (addr == null) {
            LOG.warn("removeNodeId: null IP address");
            return -1;
        }
        LOG.debug("removeNodeId: removing IP address from cache: {}:{}", location, str(addr));
        return m_knownips.removeManagedAddress(location, addr);
    }

    @Override
    public int size() {
        return m_knownips.size();
    }

    @Override
    public void clear() {
        m_knownips.setManagedAddresses(new HashMap<>());
    }
}
