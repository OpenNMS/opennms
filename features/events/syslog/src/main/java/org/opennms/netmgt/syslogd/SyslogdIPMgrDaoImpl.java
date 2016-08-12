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

package org.opennms.netmgt.syslogd;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.syslogd.ManagedInterfaceToNodeMap.LocationIpAddressKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class represents a singular instance that is used to map trap IP
 * addresses to known nodes.
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public class SyslogdIPMgrDaoImpl implements SyslogdIPMgr {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogdIPMgrDaoImpl.class);

    @Autowired
    private NodeDao m_nodeDao;

    private final ManagedInterfaceToNodeMap m_knownips = new ManagedInterfaceToNodeMap();

    private static final AtomicReference<SyslogdIPMgr> s_instance = new AtomicReference<>();

    public static void setInstance(SyslogdIPMgr syslogIpManager) {
        s_instance.set(syslogIpManager);
    }

    /**
     * @deprecated Inject this value instead of using singleton access.
     */
    public static SyslogdIPMgr getInstance() {
        return s_instance.get(); 
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
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
    public int setNodeId(final String location, final InetAddress addr, final int nodeid) {
        if (addr == null || nodeid == -1) {
            return -1;
        }
        return m_knownips.addManagedAddress(location, addr, nodeid);
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
            return -1;
        }
        return m_knownips.removeManagedAddress(location, addr);
    }

} // end SyslodIPMgr
