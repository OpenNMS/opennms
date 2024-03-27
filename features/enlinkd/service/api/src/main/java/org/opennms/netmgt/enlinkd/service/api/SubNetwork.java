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
package org.opennms.netmgt.enlinkd.service.api;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.springframework.util.Assert;

public class SubNetwork {

    private final InetAddress m_network;
    private final InetAddress m_netmask;

    private final Map<Integer, Set<InetAddress>> m_nodeInterfaceMap = new HashMap<>();

    public static SubNetwork createSubNetwork(IpInterfaceTopologyEntity ip) throws IllegalArgumentException {
        Assert.notNull(ip);
        return createSubNetwork(ip.getNodeId(), ip.getIpAddress(), ip.getNetMask());
    }

    public static SubNetwork createSubNetwork(Integer nodeid, InetAddress ip, InetAddress mask) throws IllegalArgumentException {
        Assert.notNull(ip);
        Assert.notNull(mask);
        Assert.notNull(nodeid);
        return new SubNetwork(nodeid, ip, mask);
    }

    private SubNetwork(final Integer nodeid, final InetAddress  ip, final InetAddress netmask) {
        m_network=InetAddressUtils.getNetwork(ip,netmask);
        m_netmask=netmask;
        m_nodeInterfaceMap.put(nodeid, new HashSet<>());
        m_nodeInterfaceMap.get(nodeid).add(ip);
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

    public int getNetworkPrefix() {
        return InetAddressUtils.convertInetAddressMaskToCidr(m_netmask);
    }

    public boolean isIpV4Subnetwork() {
        return m_network.getAddress().length == 4;
    }

    public Set<Integer> getNodeIds() {
        return m_nodeInterfaceMap.keySet();
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

    public boolean add(Integer nodeid, InetAddress ip) {
        if (isInRange(ip)) {
            if (!m_nodeInterfaceMap.containsKey(nodeid)) {
                m_nodeInterfaceMap.put(nodeid, new HashSet<>());
            }
            return m_nodeInterfaceMap.get(nodeid).add(ip);
        }
        return false;
    }

    public boolean remove(Integer nodeid, InetAddress ip) {
        if (m_nodeInterfaceMap.containsKey(nodeid)) {
            boolean removed = m_nodeInterfaceMap.get(nodeid).remove(ip);
            if (removed && m_nodeInterfaceMap.get(nodeid).size() == 0) {
                m_nodeInterfaceMap.remove(nodeid);
            }
            return removed;
        }
        return false;
    }

    public boolean isInRange(InetAddress ip) {
        return InetAddressUtils.inSameNetwork(ip,m_network,m_netmask);
    }

    public boolean hasDuplicatedAddress() {
        Set<InetAddress> ips = new HashSet<>();
        for (Set<InetAddress> addresses: m_nodeInterfaceMap.values()) {
            for (InetAddress address: addresses) {
                if (ips.contains(address)) {
                    return true;
                }
                ips.add(address);
            }
        }
        return false;
    }
    @Override
    public String toString() {
        return "SubNetwork{ " +
                getCidr() +
                ". NodeMap: " +
                m_nodeInterfaceMap +
                "}";
    }
}