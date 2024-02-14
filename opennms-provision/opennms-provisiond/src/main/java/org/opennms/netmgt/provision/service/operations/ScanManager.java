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
package org.opennms.netmgt.provision.service.operations;

import static org.opennms.core.utils.LocationUtils.DEFAULT_LOCATION_NAME;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.HostnameResolver;
import org.opennms.netmgt.provision.service.snmp.IfTable;
import org.opennms.netmgt.provision.service.snmp.IfXTable;
import org.opennms.netmgt.provision.service.snmp.IpAddrTable;
import org.opennms.netmgt.provision.service.snmp.IpAddressTable;
import org.opennms.netmgt.provision.service.snmp.SystemGroup;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ScanManager {
    private static final Logger LOG = LoggerFactory.getLogger(ScanManager.class);
    
    private final LocationAwareSnmpClient m_locationAwareSnmpClient;
    private final InetAddress m_address;
    private SystemGroup m_systemGroup;
    private IfTable m_ifTable;
    private IpAddrTable m_ipAddrTable;
    private IpAddressTable m_ipAddressTable;
    private IfXTable m_ifXTable;

    ScanManager(LocationAwareSnmpClient locationAwareSnmpClient, InetAddress address) {
        m_locationAwareSnmpClient = Objects.requireNonNull(locationAwareSnmpClient);
        m_address = address;
    }

    /**
     * <p>getSystemGroup</p>
     *
     * @return a {@link org.opennms.netmgt.provision.service.snmp.SystemGroup} object.
     */
    public SystemGroup getSystemGroup() {
        return m_systemGroup;
    }

    /**
     * <p>getIfTable</p>
     *
     * @return the ifTable
     */
    public IfTable getIfTable() {
        return m_ifTable;
    }

    /**
     * <p>getIpAddrTable</p>
     *
     * @return the ipAddrTable
     */
    public IpAddrTable getIpAddrTable() {
        return m_ipAddrTable;
    }

    public IpAddressTable getIpAddressTable() {
    	return m_ipAddressTable;
    }

    /**
     * <p>getIfXTable</p>
     *
     * @return the ifXTable
     */
    public IfXTable getIfXTable() {
        return m_ifXTable;
    }

    InetAddress getNetMask(final int ifIndex) {
    	final InetAddress ipAddressNetmask = getIpAddressTable().getNetMask(ifIndex);
    	if (ipAddressNetmask == null) {
    		return getIpAddrTable().getNetMask(ifIndex);
    	} else {
    		return ipAddressNetmask;
    	}
    }

    boolean isSnmpDataForInterfacesUpToDate() {
        return (!getIfTable().failed() && !getIpAddrTable().failed()) || !getIpAddressTable().failed();
    }

    boolean isSnmpDataForNodeUpToDate() {
        return !getSystemGroup().failed();
    }

    void updateSnmpData(final OnmsNode node, final HostnameResolver hostnameResolver) {
        
        try {

            m_systemGroup = new SystemGroup(m_address);

            final Set<SnmpInstId> ipAddrs = new TreeSet<>();
            final Set<InetAddress> ipAddresses = new HashSet<>();

            for(final OnmsIpInterface iface : node.getIpInterfaces()) {
            	final InetAddress addr = iface.getIpAddress();

            	if (addr != null && addr instanceof Inet4Address) {
            		ipAddrs.add(new SnmpInstId(InetAddressUtils.toOid(addr)));
            	}

            	ipAddresses.add(addr);
            }

            m_ipAddrTable = new IpAddrTable(m_address, ipAddrs);
            m_ipAddressTable = IpAddressTable.createTable(m_address, ipAddresses);

            AggregateTracker tracker = new AggregateTracker(Lists.newArrayList(m_systemGroup, m_ipAddrTable, m_ipAddressTable));
            final SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(m_address, MonitoringLocationUtils.getLocationNameOrNullIfDefault(node));
            try {
                m_locationAwareSnmpClient.walk(agentConfig, tracker)
                    .withDescription("system/ipAddrTable/ipAddressTable")
                    .withLocation(node.getLocation() == null ? null : node.getLocation().getLocationName())
                    .execute()
                    .get();
            } catch (ExecutionException e) {
                // pass
            }

            final Set<SnmpInstId> ifIndices = new TreeSet<>();

            for(final Integer ifIndex : m_ipAddrTable.getIfIndices()) {
                ifIndices.add(new SnmpInstId(ifIndex));
            }

            m_ifTable = new IfTable(m_address, ifIndices);
            m_ifXTable = new IfXTable(m_address, ifIndices);
            tracker = new AggregateTracker(Lists.newArrayList(m_systemGroup, m_ifTable, m_ifXTable));
            try {
                m_locationAwareSnmpClient.walk(agentConfig, tracker)
                    .withDescription("ifTable/ifXTable")
                    .withLocation(node.getLocation() == null ? null : node.getLocation().getLocationName())
                    .execute()
                    .get();
            } catch (ExecutionException e) {
                // pass
            }

            m_systemGroup.updateSnmpDataForNode(node);
        
            for(final SnmpInstId ifIndex : ifIndices) {
                m_ifTable.updateSnmpInterfaceData(node, ifIndex.toInt());
            }

            for(final SnmpInstId ifIndex : ifIndices) {
                m_ifXTable.updateSnmpInterfaceData(node, ifIndex.toInt());
            }

            for(final SnmpInstId ipAddr : ipAddrs) {
                InetAddress inetAddress = InetAddressUtils.addr(ipAddr.toString());
                boolean newIpInterfaceCreated = m_ipAddrTable.updateIpInterfaceData(node, inetAddress);
                if(newIpInterfaceCreated) {
                    setHostNameOnIpInterface(inetAddress, node, hostnameResolver);
                }
            }

            for (final InetAddress addr : ipAddresses) {
                boolean newIpInterfaceCreated = m_ipAddressTable.updateIpInterfaceData(node, addr);
                if(newIpInterfaceCreated) {
                    setHostNameOnIpInterface(addr, node, hostnameResolver);
                }
            }
        } catch (final InterruptedException e) {
            LOG.info("thread interrupted while updating SNMP data", e);
            Thread.currentThread().interrupt();

        }

    }

    private void setHostNameOnIpInterface(InetAddress inetAddress, OnmsNode node, HostnameResolver hostnameResolver) {

        OnmsIpInterface ipInterface = node.getIpInterfaceByIpAddress(inetAddress);
        String location = DEFAULT_LOCATION_NAME;
        if(node.getLocation() != null) {
           location = node.getLocation().getLocationName();
        }
        String hostName = hostnameResolver.getHostname(inetAddress, location);
        ipInterface.setIpHostName(hostName);
    }

    /**
     * <p>createCollectionTracker</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.AggregateTracker} object.
     */
    public AggregateTracker createCollectionTracker() {
        return new AggregateTracker(new CollectionTracker[] { getSystemGroup(), getIfTable(), getIpAddrTable(), getIfXTable(), getIpAddressTable() });
    }

}
