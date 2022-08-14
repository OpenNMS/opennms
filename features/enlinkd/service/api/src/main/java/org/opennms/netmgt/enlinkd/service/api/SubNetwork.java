/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.api;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.springframework.util.Assert;

public class SubNetwork {

    private final InetAddress m_network;
    private final InetAddress m_netmask;

    private final Set<IpInterfaceTopologyEntity> m_ipInterfaces = new HashSet<>();

    public static SubNetwork createSubNetwork(IpInterfaceTopologyEntity ip) throws IllegalArgumentException {
        Assert.notNull(ip);
        return createSubNetwork(ip.getIpAddress(),ip.getNetMask());
    }

    public static SubNetwork createSubNetwork(InetAddress ip, InetAddress mask) throws IllegalArgumentException {
        Assert.notNull(ip);
        Assert.notNull(mask);
        return new SubNetwork(InetAddressUtils.getNetwork(ip,mask),mask);
    }

    private SubNetwork(final InetAddress  network, final InetAddress netmask) {
        m_network=network;
        m_netmask=netmask;
    }

    public InetAddress getNetwork() {
        return m_network;
    }

    public InetAddress getNetmask() {
        return m_netmask;
    }

    public String getCidr() {
        return m_network.getHostAddress()+"/"+InetAddressUtils.convertInetAddressMaskToCidr(m_netmask);
    }

    public Set<Integer> getNodeIds() {
        Set<Integer> set = new HashSet<>();
        m_ipInterfaces.forEach(ip -> set.add(ip.getNodeId()));
        return set;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubNetwork that = (SubNetwork) o;

        if (!m_network.equals(that.m_network)) return false;
        return m_netmask.equals(that.m_netmask);
    }

    @Override
    public int hashCode() {
        int result = m_network.hashCode();
        result = 31 * result + m_netmask.hashCode();
        return result;
    }

    public boolean add(IpInterfaceTopologyEntity ipInterfaceTopologyEntity) {
        if (isInRange(ipInterfaceTopologyEntity.getIpAddress())) {
            return m_ipInterfaces.add(ipInterfaceTopologyEntity);
        }
        return false;
    }

    public boolean remove(IpInterfaceTopologyEntity ipInterfaceTopologyEntity) {
        return m_ipInterfaces.remove(ipInterfaceTopologyEntity);
    }

    public boolean isInRange(InetAddress ip) {
        return InetAddressUtils.inSameNetwork(ip,m_network,m_netmask);
    }

    @Override
    public String toString() {
        return "SubNetwork{" +
                getCidr() +
                ", number of ipInterfaces:" + m_ipInterfaces.size() +
                ", nodeIds: " + getNodeIds() +
                '}';
    }
}