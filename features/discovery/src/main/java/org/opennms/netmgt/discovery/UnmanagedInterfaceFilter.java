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

package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.xml.event.Event;

/**
 * Given a list of managed IP addresses, this filter will match IP addresses not in that
 * list, hence it is an unmanaged IP address filter.
 */
public class UnmanagedInterfaceFilter implements IpAddressFilter {

    public static class LocationIpAddressKey {
        private final String m_location;
        /**
         * TODO: Refactor this to {@link InetAddress} when the type is
         * changed inside the {@link Event} object.
         */
        private final String m_ipAddress;

        public LocationIpAddressKey(String location, String ipAddress) {
            m_location = location;
            m_ipAddress = ipAddress;
        }

        public String getIpAddress() {
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

    /**
     * a set of addresses to skip discovery on
     */
    private Set<LocationIpAddressKey> m_managedAddresses = Collections.synchronizedSet(new HashSet<>());

    public void addManagedAddress(String location, String address) {
        synchronized(m_managedAddresses) {
            m_managedAddresses.add(new LocationIpAddressKey(location, address));
        }
    }

    public void removeManagedAddress(String location, String address) {
        synchronized(m_managedAddresses) {
            m_managedAddresses.remove(new LocationIpAddressKey(location, address));
        }
    }

    public int size() {
        synchronized(m_managedAddresses) {
            return m_managedAddresses.size();
        }
    }

    public void setManagedAddresses(Set<LocationIpAddressKey> addresses) {
        synchronized(m_managedAddresses) {
            m_managedAddresses.clear();
            m_managedAddresses.addAll(addresses);
        }
    }

    @Override
    public boolean matches(String location, InetAddress address) {
        return matches(location, InetAddressUtils.str(address));
    }

    @Override
    public boolean matches(String location, String address) {
        synchronized(m_managedAddresses) {
            return !m_managedAddresses.contains(new LocationIpAddressKey(location, address));
        }
    }
}
