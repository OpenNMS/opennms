/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

public class InterfaceToNodeMap {

    public static class LocationIpAddressKey {
        private final String m_location;
        private final InetAddress m_ipAddress;

        public LocationIpAddressKey(String location, InetAddress ipAddress) {
            // Use the default location when location is null
            m_location = location != null ? location : MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
            m_ipAddress = Objects.requireNonNull(ipAddress);
        }

        public InetAddress getIpAddress() {
            return m_ipAddress;
        }

        public String getLocation() {
            return m_location;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) { return false; }
            if (obj == this) { return true; }
            if (obj.getClass() != getClass()) {
                return false;
            }
            LocationIpAddressKey other = (LocationIpAddressKey)obj;
            return Objects.equals(m_ipAddress, other.m_ipAddress)
                    && Objects.equals(m_location, other.m_location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(m_ipAddress, m_location);
        }

        @Override
        public String toString() {
            return String.format("LocationIpAddressKey[location='%s', ipAddress='%s']",
                    m_location, m_ipAddress);
        }
    }

    private final ReadWriteLock m_lock = new ReentrantReadWriteLock();
    private final SetMultimap<LocationIpAddressKey,Integer> m_managedAddresses = HashMultimap.create();

    public void addManagedAddress(String location, InetAddress address, int nodeId) {
        m_lock.writeLock().lock();
        try {
            m_managedAddresses.put(new LocationIpAddressKey(location, address), nodeId);
        } finally {
            m_lock.writeLock().unlock();
        }
    }

    public boolean removeManagedAddress(String location, InetAddress address, int nodeId) {
        m_lock.writeLock().lock();
        try {
            return m_managedAddresses.remove(new LocationIpAddressKey(location, address), nodeId);
        } finally {
            m_lock.writeLock().unlock();
        }
    }

    public int size() {
        m_lock.readLock().lock();
        try {
            return m_managedAddresses.size();
        } finally {
            m_lock.readLock().unlock();
        }
    }

    public Set<Integer> getNodeId(String location, InetAddress address) {
        m_lock.readLock().lock();
        try {
            return m_managedAddresses.get(new LocationIpAddressKey(location, address));
        } finally {
            m_lock.readLock().unlock();
        }
    }

    public void setManagedAddresses(Multimap<LocationIpAddressKey, Integer> addresses) {
        m_lock.writeLock().lock();
        try {
            m_managedAddresses.clear();
            if (!Objects.isNull(addresses)) {
                m_managedAddresses.putAll(addresses);
            }
        } finally {
            m_lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        m_lock.readLock().lock();
        try {
            return String.format("InterfaceToNodeMap[managedAddresses='%s']", m_managedAddresses);
        } finally {
            m_lock.readLock().unlock();
        }
    }
}
