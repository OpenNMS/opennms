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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 */
public class InterfaceToNodeMap {

    public static class LocationIpAddressKey {
        private final String m_location;
        private final InetAddress m_ipAddress;

        public LocationIpAddressKey(String location, InetAddress ipAddress) {
            m_location = location;
            m_ipAddress = ipAddress;
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
            LocationIpAddressKey key = (LocationIpAddressKey)obj;
            return new EqualsBuilder()
                .append(m_ipAddress, key.getIpAddress())
                .append(m_location, key.getLocation())
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                .append(m_ipAddress)
                .append(m_location)
                .toHashCode();
        }
    }

    private final Map<LocationIpAddressKey,Integer> m_managedAddresses = Collections.synchronizedMap(new HashMap<>());

    public int addManagedAddress(String location, InetAddress address, int nodeId) {
        synchronized(m_managedAddresses) {
            Integer retval = m_managedAddresses.put(new LocationIpAddressKey(location, address), nodeId);
            return retval == null ? -1 : retval.intValue();
        }
    }

    public int removeManagedAddress(String location, InetAddress address) {
        synchronized(m_managedAddresses) {
            Integer retval = m_managedAddresses.remove(new LocationIpAddressKey(location, address));
            return retval == null ? -1 : retval.intValue();
        }
    }

    public int size() {
        synchronized(m_managedAddresses) {
            return m_managedAddresses.size();
        }
    }

    public int getNodeId(String location, InetAddress address) {
        synchronized(m_managedAddresses) {
            Integer retval = m_managedAddresses.get(new LocationIpAddressKey(location, address));
            return retval == null ? -1 : retval.intValue();
        }
    }

    public void setManagedAddresses(Map<LocationIpAddressKey,Integer> addresses) {
        synchronized(m_managedAddresses) {
            m_managedAddresses.clear();
            m_managedAddresses.putAll(addresses);
        }
    }
}
