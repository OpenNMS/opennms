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
import java.util.Collections;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.LocationUtils;
import org.opennms.netmgt.dao.api.AbstractInterfaceToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.PrimaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;

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

    private static class Key {
        private final String location;
        private final InetAddress ipAddress;

        public Key(String location, InetAddress ipAddress) {
            // Use the default location when location is null
            this.location = LocationUtils.getEffectiveLocationName(location);
            this.ipAddress = Objects.requireNonNull(ipAddress);
        }

        public InetAddress getIpAddress() {
            return ipAddress;
        }

        public String getLocation() {
            return location;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            final Key that = (Key) obj;
            return Objects.equals(this.ipAddress, that.ipAddress)
                    && Objects.equals(this.location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.ipAddress, this.location);
        }

        @Override
        public String toString() {
            return String.format("Key[location='%s', ipAddress='%s']", this.location, this.ipAddress);
        }
    }

    private static class Value implements Comparable<Value> {
        private final int nodeId;
        private final PrimaryType type;


        private Value(final int nodeId,
                      final PrimaryType type) {
            this.nodeId = nodeId;
            this.type = type;
        }

        public int getNodeId() {
            return this.nodeId;
        }

        public PrimaryType getType() {
            return this.type;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            final Value that = (Value) obj;
            return Objects.equals(this.nodeId, that.nodeId)
                    && Objects.equals(this.type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.nodeId, this.type.getCharCode());
        }

        @Override
        public String toString() {
            return String.format("Value[nodeId='%s', type='%s']", this.nodeId, this.type);
        }

        @Override
        public int compareTo(final Value that) {
            return ComparisonChain.start()
                    .compare(this.type, that.type)
                    .compare(this.nodeId, that.nodeId)
                    .result();
        }
    }

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    private final ReadWriteLock m_lock = new ReentrantReadWriteLock();
    private final SortedSetMultimap<Key, Value> m_managedAddresses = Multimaps.newSortedSetMultimap(Maps.newHashMap(), TreeSet::new);

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
     * @throws java.sql.SQLException Thrown if the connection cannot be created or a database
     *                               error occurs.
     */
    @Override
    @Transactional
    public void dataSourceSync() {
        /*
         * Make a new list with which we'll replace the existing one, that way
         * if something goes wrong with the DB we won't lose whatever was already
         * in there
         */
        final SortedSetMultimap<Key, Value> newAlreadyDiscovered = Multimaps.newSortedSetMultimap(Maps.newHashMap(), TreeSet::new);

        // Fetch all non-deleted nodes
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.ne("type", String.valueOf(NodeType.DELETED.value()));

        for (OnmsNode node : m_nodeDao.findMatching(builder.toCriteria())) {
            for (final OnmsIpInterface iface : node.getIpInterfaces()) {
                // Skip deleted interfaces
                // TODO: Refactor the 'D' value with an enumeration
                if ("D".equals(iface.getIsManaged())) {
                    continue;
                }
                LOG.debug("Adding entry: {}:{} -> {}", node.getLocation().getLocationName(), iface.getIpAddress(), node.getId());
                newAlreadyDiscovered.put(new Key(node.getLocation().getLocationName(), iface.getIpAddress()), new Value(node.getId(), iface.getIsSnmpPrimary()));
            }
        }
        m_managedAddresses.clear();
        m_managedAddresses.putAll(newAlreadyDiscovered);
        LOG.info("dataSourceSync: initialized list of managed IP addresses with {} members", m_managedAddresses.size());
    }

    /**
     * Returns the nodeid for the IP Address
     * <p>
     * If multiple nodes hav assigned interfaces with the same IP, this returns all known nodes sorted by the interface
     * management priority.
     *
     * @param address The IP Address to query.
     * @return The node ID of the IP Address if known.
     */
    @Override
    public synchronized Iterable<Integer> getNodeId(final String location, final InetAddress address) {
        if (address == null) {
            return Collections.emptySet();
        }

        m_lock.readLock().lock();
        try {
            return Iterables.transform(m_managedAddresses.get(new Key(location, address)),
                    Value::getNodeId);
        } finally {
            m_lock.readLock().unlock();
        }
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
    public boolean setNodeId(final String location, final InetAddress addr, final int nodeid) {
        if (addr == null || nodeid == -1) {
            return false;
        }

        final OnmsIpInterface iface = m_ipInterfaceDao.findByNodeIdAndIpAddress(nodeid, str(addr));
        if (iface == null) {
            return false;
        }

        LOG.debug("setNodeId: adding IP address to cache: {}:{} -> {}", location, str(addr), nodeid);

        m_lock.writeLock().lock();
        try {
            return m_managedAddresses.put(new Key(location, addr), new Value(nodeid, iface.getIsSnmpPrimary()));
        } finally {
            m_lock.writeLock().unlock();
        }
    }

    /**
     * Removes an address from the node ID map.
     *
     * @param address The address to remove from the node ID map.
     * @return The nodeid that was in the map.
     */
    @Override
    public boolean removeNodeId(final String location, final InetAddress address, final int nodeId) {
        if (address == null) {
            LOG.warn("removeNodeId: null IP address");
            return false;
        }

        LOG.debug("removeNodeId: removing IP address from cache: {}:{}", location, str(address));

        m_lock.writeLock().lock();
        try {
            final Key key = new Key(location, address);
            return m_managedAddresses.remove(key, new Value(nodeId, PrimaryType.PRIMARY)) ||
                    m_managedAddresses.remove(key, new Value(nodeId, PrimaryType.SECONDARY)) ||
                    m_managedAddresses.remove(key, new Value(nodeId, PrimaryType.NOT_ELIGIBLE));
        } finally {
            m_lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        m_lock.readLock().lock();
        try {
            return m_managedAddresses.size();
        } finally {
            m_lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        m_lock.writeLock().lock();
        try {
            m_managedAddresses.clear();
        } finally {
            m_lock.writeLock().unlock();
        }
    }
}
